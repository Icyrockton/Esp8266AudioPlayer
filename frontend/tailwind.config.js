module.exports = {
  purge: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  darkMode: false, // or 'media' or 'class'
  theme: {
    extend: {
      animation : {
        "spin-slow" : "spin 15s linear infinite"
      }
    },
  },
  variants: {
    extend: {},
  },
  plugins: [],
}
