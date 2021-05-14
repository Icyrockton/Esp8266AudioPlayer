#include <Arduino.h>
#include "SPI.h"
#include "Wire.h"
#include "ESP8266HTTPClient.h"
#include "ESP8266WiFi.h"
#include "SD.h"
#include "DHT.h"
#include "AudioFileSource.h"
#include "EspDHT.h"
#include "PubSubClient.h"
#include "ArduinoJson.h"
#include "AudioFileSourceICYStream.h"
#include "AudioGeneratorMP3.h"
#include "AudioOutputI2S.h"
#include "AudioFileSourceBuffer.h"

#define SSID  "2006_2.4G"
#define PASSWORD  "22342234"
#define DHTPIN 0     // IO0 连接DHT11
#define DHTTYPE DHT11   // DHT 11
#define CAR_ENA 12
#define CAR_ENB 13
#define CAR_IN_1 5
#define CAR_IN_2 4
#define CAR_IN_3 16
#define CAR_IN_4 14
// MQTT
IPAddress brokerIP(192, 168, 123, 179); //broker的地址
WiFiClient wifiClient;

PubSubClient mqttClient(wifiClient);
DHT dht(DHTPIN, DHTTYPE);
//MP3播放
AudioFileSourceHTTPStream *mp3File;
AudioFileSourceBuffer *buffer;
AudioOutputI2S *out;
AudioGeneratorMP3 *mp3;
bool newMusic = false; //是否有新的音乐
bool playingMusic = false; //播放音乐ing
void connectWifi() {
    Serial.print("Wifi Connecting to");
    Serial.println(SSID);
    WiFi.mode(WIFI_STA);
    WiFi.begin(SSID, PASSWORD);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }
    Serial.println("");
    Serial.println("WiFi connected");
    Serial.println("IP address: ");
    Serial.println(WiFi.localIP());
}

const char *MusicPlayTopic = "music/play";
const char *MusicPauseTopic = "music/pause";
const char *MusicResumeTopic = "music/resume";
const char *MusicCancelTopic = "music/cancel";
const char *MusicVolumeTopic = "music/volume";
const char *CarForwardTopic = "car/forward";
const char *CarBackwardTopic = "car/backward";
const char *CarLeftTopic = "car/left";
const char *CarRightTopic = "car/right";
const char *CarForwardLeftTopic = "car/forwardLeft";
const char *CarForwardRightTopic = "car/forwardRight";
const char *CarBackwardLeftTopic = "car/backwardLeft";
const char *CarBackwardRightTopic = "car/backwardRight";
const char *CarAccelerateTopic = "car/accelerate";
const char *CarAccelerateX2Topic = "car/accelerateX2";
const char *CarBrakeTopic = "car/brake";
const char *CarDecelerateTopic = "car/decelerate";
DynamicJsonDocument jsonParseDocument(512);
char musicUrl[256]; //音乐地址

void stopMusicPlay() { //停止音乐的播放
    if (mp3) {
        mp3->stop();
        delete mp3;
        mp3 = nullptr;
    }
    if (buffer) {
        buffer->close();
        delete buffer;
        buffer = nullptr;
    }
    if (mp3File) {
        mp3File->close();
        delete mp3File;
        mp3File = nullptr;
    }
    Serial.println("mp3 play stop...");
}

//小车

double CAR_NORMAL_SPEED = 800.0; //1~1024
bool doubleAccelerate = false;
enum CarDirection {
    forward,
    forwardLeft,
    forwardRight,
    backward,
    backwardLeft,
    backwardRight,
    right,
    left,

};

CarDirection carDirection = forward;

void setCarPin() {
//    pinMode(CAR_ENA, OUTPUT);
//    pinMode(CAR_ENB, OUTPUT);
    pinMode(CAR_IN_1, OUTPUT);
    pinMode(CAR_IN_2, OUTPUT);
    pinMode(CAR_IN_3, OUTPUT);
    pinMode(CAR_IN_4, OUTPUT);
    pinMode(CAR_ENA, OUTPUT);
    pinMode(CAR_ENB, OUTPUT);
}

void carGoForward() {  //小车前进
    Serial.println("forward...");
    carDirection = forward;
}


void carGoBackward() {  //小车后退
    Serial.println("backward...");
    carDirection = backward;
}

void carGoLeft() {
    Serial.println("left...");
    carDirection = left;
}

void carGoRight() {
    Serial.println("right...");
    carDirection = right;
}

void carGoForwardLeft() {
    Serial.println("ForwardLeft...");
    carDirection = forwardLeft;
}

void carGoForwardRight() {
    Serial.println("ForwardRight...");
    carDirection = forwardRight;
}

void carGoBackwardLeft() {
    Serial.println("BackwardLeft...");
    carDirection = backwardLeft;
}

void carGoBackwardRight() {
    Serial.println("BackwardRight...");
    carDirection = backwardRight;
}

double carCurrentSpeed = 0;
bool carIsDecelerating =  false;  //是否在减速
unsigned long carPreviousMillis = 0;


void carAccelerate(double factor) {
    Serial.println("forward...");
    carIsDecelerating = false;
    carCurrentSpeed = CAR_NORMAL_SPEED * factor;
    switch (carDirection) {
        case forward:
            digitalWrite(CAR_IN_1, LOW);
            digitalWrite(CAR_IN_2, HIGH);
            analogWrite(CAR_ENA, carCurrentSpeed);
            digitalWrite(CAR_IN_3, LOW);
            digitalWrite(CAR_IN_4, HIGH);
            analogWrite(CAR_ENB, carCurrentSpeed);
            break;
        case forwardLeft:
            digitalWrite(CAR_IN_1, LOW);
            digitalWrite(CAR_IN_2, HIGH);
            analogWrite(CAR_ENA, carCurrentSpeed);
            digitalWrite(CAR_IN_3, LOW);
            digitalWrite(CAR_IN_4, HIGH);
            analogWrite(CAR_ENB, 700);
            break;

        case forwardRight:
            digitalWrite(CAR_IN_1, LOW);
            digitalWrite(CAR_IN_2, HIGH);
            analogWrite(CAR_ENA, 700);
            digitalWrite(CAR_IN_3, LOW);
            digitalWrite(CAR_IN_4, HIGH);
            analogWrite(CAR_ENB, carCurrentSpeed);

            break;
        case backward:
            digitalWrite(CAR_IN_1, HIGH);
            digitalWrite(CAR_IN_2, LOW);
            analogWrite(CAR_ENA, carCurrentSpeed);
            digitalWrite(CAR_IN_3, HIGH);
            digitalWrite(CAR_IN_4, LOW);
            analogWrite(CAR_ENB, carCurrentSpeed);
            break;
        case backwardLeft:
            digitalWrite(CAR_IN_1, HIGH);
            digitalWrite(CAR_IN_2, LOW);
            analogWrite(CAR_ENA, carCurrentSpeed);
            digitalWrite(CAR_IN_3, HIGH);
            digitalWrite(CAR_IN_4, LOW);
            analogWrite(CAR_ENB, 700);
            break;
        case backwardRight:
            digitalWrite(CAR_IN_1, HIGH);
            digitalWrite(CAR_IN_2, LOW);
            analogWrite(CAR_ENA, 700);
            digitalWrite(CAR_IN_3, HIGH);
            digitalWrite(CAR_IN_4, LOW);
            analogWrite(CAR_ENB, carCurrentSpeed);

            break;

        case left:
            digitalWrite(CAR_IN_1, LOW);
            digitalWrite(CAR_IN_2, HIGH);
            analogWrite(CAR_ENA, carCurrentSpeed);
            digitalWrite(CAR_IN_3, LOW);
            digitalWrite(CAR_IN_4, LOW);
            analogWrite(CAR_ENB, carCurrentSpeed);
            break;

        case right:
            digitalWrite(CAR_IN_1, LOW);
            digitalWrite(CAR_IN_2, LOW);
            analogWrite(CAR_ENA, carCurrentSpeed);
            digitalWrite(CAR_IN_3, LOW);
            digitalWrite(CAR_IN_4, HIGH);
            analogWrite(CAR_ENB, carCurrentSpeed);

            break;
    }
}

void carAccelerateX2() {
    carAccelerate(1.25);
}


void carBeginDecelerate() {
    carIsDecelerating = true;
}

void carDecelerate(){
    Serial.println("decelerate ...");

    carCurrentSpeed -= 200;
    analogWrite(CAR_ENA, carCurrentSpeed);
    analogWrite(CAR_ENB, carCurrentSpeed);
    if (carCurrentSpeed <= 500) {
        Serial.println("decelerate brake...");

        carIsDecelerating = false;
        digitalWrite(CAR_IN_1, LOW);
        digitalWrite(CAR_IN_2, LOW);
        digitalWrite(CAR_IN_3, LOW);
        digitalWrite(CAR_IN_4, LOW);
    }
}

void carBrake() {
    Serial.println("brake...");
    digitalWrite(CAR_IN_1, LOW);
    digitalWrite(CAR_IN_2, LOW);
    digitalWrite(CAR_IN_3, LOW);
    digitalWrite(CAR_IN_4, LOW);
}

void mqttCallback(char *topic, byte *payload, unsigned int length) {
    Serial.print("Message arrived [");
    Serial.print(topic);
    Serial.print("] ");
    for (int i = 0; i < length; i++) {
        Serial.print((char) payload[i]);
    }
    Serial.println();
    if (strcmp(topic, MusicPlayTopic) == 0) { //音乐播放
        jsonParseDocument.clear();
        deserializeJson(jsonParseDocument, payload);
        strcpy(musicUrl, jsonParseDocument["songUrl"]);
        newMusic = true;
        playingMusic = true;
        Serial.print("new music URL:");
        Serial.println(musicUrl);
        stopMusicPlay(); //停止播放音乐
    } else if (strcmp(topic, MusicPauseTopic) == 0) { //音乐暂停
        Serial.println("pause music:");
        playingMusic = false;
    } else if (strcmp(topic, MusicResumeTopic) == 0) { //音乐继续播放
        Serial.println("resume music:");
        playingMusic = true;
    } else if (strcmp(topic, MusicCancelTopic) == 0) { //音乐取消
        Serial.println("cancel music:");
        stopMusicPlay();
    } else if (strcmp(topic, MusicVolumeTopic) == 0) { //调整音量
        jsonParseDocument.clear();
        deserializeJson(jsonParseDocument, payload);
        int volume = jsonParseDocument["volume"];
        Serial.print("change volume : ");
        Serial.println(volume);
        out->SetGain((float) volume / 10);

    } else if (strcmp(topic, CarForwardTopic) == 0) {
        carGoForward();
    } else if (strcmp(topic, CarBackwardTopic) == 0) {
        carGoBackward();
    } else if (strcmp(topic, CarLeftTopic) == 0) {
        carGoLeft();
    } else if (strcmp(topic, CarRightTopic) == 0) {
        carGoRight();
    } else if (strcmp(topic, CarForwardLeftTopic) == 0) {
        carGoForwardLeft();
    } else if (strcmp(topic, CarForwardRightTopic) == 0) {
        carGoForwardRight();
    } else if (strcmp(topic, CarBackwardLeftTopic) == 0) {
        carGoBackwardLeft();
    } else if (strcmp(topic, CarBackwardRightTopic) == 0) {
        carGoBackwardRight();
    } else if (strcmp(topic, CarAccelerateTopic) == 0) {
        carAccelerate(1.0);
    } else if (strcmp(topic, CarAccelerateX2Topic) == 0) {
        carAccelerateX2();
    } else if (strcmp(topic, CarBrakeTopic) == 0) {
        carBrake();
    } else if (strcmp(topic, CarDecelerateTopic) == 0) {
        carBeginDecelerate();
    }

}


void connectMqtt() {
    Serial.println("connecting MQTT server");
//    设置broker
    mqttClient.setServer(brokerIP, 1883);
    mqttClient.setKeepAlive(120);
//    设置回调
    mqttClient.setCallback(mqttCallback);

    while (!mqttClient.connected()) {
        Serial.print("Attempting MQTT connection...");
        String clientId = "ESP8266Client-";
        clientId += String(random(0xffff), HEX);
        if (mqttClient.connect(clientId.c_str())) {
            Serial.println("connected");
            mqttClient.publish("status", "hello world");
            if (mqttClient.subscribe("inTopic"))
                Serial.println("subscribe inTopic successes");
            else
                Serial.println("subscribe inTopic failed");
            mqttClient.subscribe("music/play"); //音乐播放
            mqttClient.subscribe("music/pause"); //音乐暂停
            mqttClient.subscribe("music/volume"); //调整音乐声音
            mqttClient.subscribe("music/cancel"); //音乐取消
            mqttClient.subscribe("music/resume"); //音乐继续播放

            mqttClient.subscribe("car/forward");
            mqttClient.subscribe("car/backward");
            mqttClient.subscribe("car/left");
            mqttClient.subscribe("car/right");
            mqttClient.subscribe("car/forwardLeft");
            mqttClient.subscribe("car/forwardRight");
            mqttClient.subscribe("car/backwardLeft");
            mqttClient.subscribe("car/backwardRight");
            mqttClient.subscribe("car/accelerate");
            mqttClient.subscribe("car/accelerateX2");
            mqttClient.subscribe("car/brake");
            mqttClient.subscribe("car/decelerate");
        } else {
            Serial.print("failed, rc=");
            Serial.print(mqttClient.state());
            Serial.println(" try again in 5 seconds");
            delay(5000);
        }
    }
}

void MDCallback(void *cbData, const char *type, bool isUnicode, const char *string) {
    const char *ptr = reinterpret_cast<const char *>(cbData);
    (void) isUnicode; // Punt this ball for now
    // Note that the type and string may be in PROGMEM, so copy them to RAM for printf
    char s1[32], s2[64];
    strncpy_P(s1, type, sizeof(s1));
    s1[sizeof(s1) - 1] = 0;
    strncpy_P(s2, string, sizeof(s2));
    s2[sizeof(s2) - 1] = 0;
    Serial.printf("METADATA(%s) '%s' = '%s'\n", ptr, s1, s2);
    Serial.flush();
}

void StatusCallback(void *cbData, int code, const char *string) {
    const char *ptr = reinterpret_cast<const char *>(cbData);
    // Note that the string may be in PROGMEM, so copy it to RAM for printf
    char s1[64];
    strncpy_P(s1, string, sizeof(s1));
    s1[sizeof(s1) - 1] = 0;
    Serial.printf("STATUS(%s) '%d' = '%s'\n", ptr, code, s1);
    Serial.flush();
}

const char *URL = "http://m10.music.126.net/20210504163607/3a5d2a822419d170228cbe9ce24e9702/ymusic/obj/w5zDlMODwrDDiGjCn8Ky/3047366729/c22a/6e45/ff05/31dbb8ef2bffa556d44aa24306e0ce1f.mp3";


void initAudioSetting() {
    audioLogger = &Serial; //日志
    mp3File = new AudioFileSourceICYStream(URL);
    mp3File->RegisterMetadataCB(MDCallback, (void *) "ICY");

    buffer = new AudioFileSourceBuffer(mp3File, 1024 * 10);
    buffer->RegisterStatusCB(StatusCallback, (void *) "buffer");

    out = new AudioOutputI2S();
    out->SetPinout(15, 2, 3);
    out->SetGain(1.0f);
    mp3 = new AudioGeneratorMP3();
    mp3->begin(buffer, out);
}


const long DHTInterval = 1000 * 10;  //温湿度采集间隔
unsigned long DHTPreviousMillis = 0;        // will store last time LED was updated

String jsonData;
StaticJsonDocument<200> jsonDocument;
enum ESPState {
    sensor, musicPlayer
};   //模式
ESPState currentState = musicPlayer;

void mp3ChangeUrl() {  //更换URL
    newMusic = false;
    stopMusicPlay();
    Serial.printf_P(PSTR("Changing URL to: %s\n"), musicUrl);
    mp3File = new AudioFileSourceICYStream(musicUrl);
    mp3File->RegisterMetadataCB(MDCallback, (void *) "ICY");
    buffer = new AudioFileSourceBuffer(mp3File, 1024 * 10);
    buffer->RegisterStatusCB(StatusCallback, (void *) "buffer");
    mp3 = new AudioGeneratorMP3();
    out->SetGain(1.0f);
    mp3->begin(buffer, out);
    //音量
}

void mp3Play() {
    static int lastms = 0;

    if (playingMusic && mp3 && mp3->isRunning()) {
        if (millis() - lastms > 1000) {
            lastms = millis();
            Serial.printf("Running for %d ms...\n", lastms);
            Serial.flush();
        }

        if (!mp3->loop()) {
            mp3->stop();
            stopMusicPlay(); //停止音乐播放   
        }
    }
}

void setup() {
    Serial.begin(115200);
    connectWifi(); //连接WIFI
    connectMqtt();
    setCarPin();
    dht.begin();
    initAudioSetting();
    delay(2000);

}


void loop() {
    unsigned long currentMillis = millis();
    mqttClient.loop();

    if(currentMillis - carPreviousMillis >= 200) {
        carPreviousMillis = currentMillis;
        if (carIsDecelerating){
            carDecelerate();
        }
    }


    if (currentState == sensor) {
        //温湿度
        if (currentMillis - DHTPreviousMillis >= DHTInterval) {
            DHTPreviousMillis = currentMillis;
            float humidity = dht.readHumidity(); //读取湿度
            float temperature = dht.readTemperature(); //读取温度
            if (isnan(humidity) || isnan(temperature)) {
                Serial.println("humidity temperature read Nan");
                return;
            }
            //发送温度
            jsonDocument["data"] = temperature;
            serializeJson(jsonDocument, jsonData);
            mqttClient.publish("sensor/temperature", jsonData.c_str());
            Serial.println(jsonData);

            jsonDocument.clear();
            jsonData.clear();
            //发送湿度
            jsonDocument["data"] = humidity;
            serializeJson(jsonDocument, jsonData);
            mqttClient.publish("sensor/humidity", jsonData.c_str());
            Serial.println(jsonData);
        }
    } else if (currentState == musicPlayer) {
        if (newMusic) {
            mp3ChangeUrl();
        }

        mp3Play(); //播放mp3 产生波

    }

}