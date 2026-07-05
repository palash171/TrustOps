import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'

/**
 * Vite reads this file when the frontend development server starts.
 * It configures React, the backend proxy, and the frontend test environment.
 */
export default defineConfig({
  // Lets Vite understand React's TSX files and update the browser while we code.
  plugins: [react()],

  server: {
    proxy: {
      // Browser /api requests go to Vite on 5173; Vite forwards them to Spring on 8080.
      '/api': 'http://localhost:8080',
    },
  },

  test: {
    // Node has no real browser document. jsdom creates a small fake browser for tests.
    environment: 'jsdom',

    // Run this once before each test file to install matchers and automatic cleanup.
    setupFiles: ['./src/test/setup.ts'],
  },
})
