# ── Stage 1: Build Angular ─────────────────────────────────────────────────
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci --legacy-peer-deps

COPY . .
RUN npm run build -- --configuration=production

# ── Stage 2: Serve with nginx ───────────────────────────────────────────────
FROM nginx:alpine

# Copy built Angular app
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

# nginx.conf is mounted as a volume from docker-compose (reverse proxy config)
EXPOSE 80

