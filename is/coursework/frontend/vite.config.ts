import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/gateway': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        rewrite: path => path.replace(/^\/gateway/, ''),
        configure: (proxy, _options) => {
          proxy.on('proxyReq', (proxyReq, _req, _res) => {
            proxyReq.setHeader('X-Forwarded-Host', 'localhost:5173');
            proxyReq.setHeader('X-Forwarded-Proto', 'http');
            proxyReq.setHeader('X-Forwarded-Port', '5173');
          });
        },
      },
      '/login': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/oauth2': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/logout': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true,
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/ws/, '/resource/ws'),
      }
    }
  }
})