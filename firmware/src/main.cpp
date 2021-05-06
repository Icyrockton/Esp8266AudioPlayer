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

#define SSID  "1500M_2.4G"
#define PASSWORD  "meilianbuyi"
#define DHTPIN 0     // IO0 连接DHT11
#define DHTTYPE DHT11   // DHT 11
// MQTT
IPAddress brokerIP(192, 168, 6, 179); //broker的地址
WiFiClient wifiClient;

PubSubClient mqttClient(wifiClient);
DHT dht(DHTPIN, DHTTYPE);
//MP3播放
AudioFileSourceHTTPStream *mp3File;
AudioFileSourceBuffer *buffer;
AudioOutputI2S *out;
AudioGeneratorMP3 *mp3;
bool newMusic = false; //是否有新的音乐
bool playingMusic = false ; //播放音乐ing
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
    }
    else if (strcmp(topic,MusicPauseTopic) == 0){ //音乐暂停
        Serial.println("pause music:");
        playingMusic = false ;
    }
    else if (strcmp(topic,MusicResumeTopic) == 0){ //音乐继续播放
        Serial.println("resume music:");
        playingMusic = true;
    }
    else if (strcmp(topic,MusicCancelTopic) == 0){ //音乐取消
        Serial.println("cancel music:");
        stopMusicPlay();
    }
    else if (strcmp(topic,MusicVolumeTopic) == 0 ){ //调整音量
        jsonParseDocument.clear();
        deserializeJson(jsonParseDocument, payload);
        int volume = jsonParseDocument["volume"];
        Serial.print("change volume : ");
        Serial.println(volume);
        out->SetGain((float)volume / 10);
        mp3File->getPos()
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

void initAudioSetting(){
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

void setup() {
    Serial.begin(115200);
    connectWifi(); //连接WIFI
    connectMqtt();
    dht.begin();
    initAudioSetting();
    delay(2000);

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

void loop() {
    unsigned long currentMillis = millis();
    mqttClient.loop();
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