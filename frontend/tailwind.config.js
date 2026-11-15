/** @type {import('tailwindcss').Config} */
export default {
  content: [
    './index.html',
    './src/**/*.{vue,js,ts,jsx,tsx}'
  ],
  theme: {
    extend: {
      colors: {
        uv: {
          azul: '#18529D',
          'azul-oscuro': '#123E78',
          'azul-claro': '#EDF3FB',
          verde: '#28AD56',
          'verde-oscuro': '#1E8A43',
          'verde-claro': '#EAF8EF'
        }
      }
    }
  },
  plugins: []
}
