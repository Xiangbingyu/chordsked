/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#1A237E',
        secondary: '#FF6F00',
        background: '#F5F7FA',
        success: '#4CAF50',
        warning: '#FFC107',
        error: '#F44336',
        text: {
            main: '#212121',
            secondary: '#757575',
            hint: '#BDBDBD'
        }
      },
    },
  },
  plugins: [],
}
