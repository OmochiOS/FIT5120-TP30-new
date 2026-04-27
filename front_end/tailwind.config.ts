import type { Config } from 'tailwindcss';

export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{vue,ts}'],
  theme: {
    extend: {
      colors: {
        sense: {
          ink: '#16363f',
          teal: '#2f6c70',
          mint: '#dff1ea',
          foam: '#f4faf7',
          sand: '#f2eadc',
          rose: '#f5dcd5',
          amber: '#e2b15b',
          coral: '#bd7152',
          night: '#0f1f28',
        },
      },
      boxShadow: {
        soft: '0 22px 55px rgba(19, 52, 58, 0.09)',
      },
      fontFamily: {
        sans: ['Aptos', 'Trebuchet MS', 'Segoe UI', 'sans-serif'],
        display: ['Iowan Old Style', 'Palatino Linotype', 'Book Antiqua', 'serif'],
      },
      backgroundImage: {
        haze:
          'radial-gradient(circle at top left, rgba(223,241,234,0.88), transparent 38%), radial-gradient(circle at bottom right, rgba(242,234,220,0.9), transparent 45%)',
      },
    },
  },
  plugins: [],
} satisfies Config;
