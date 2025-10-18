# Multi-stage build: Flutter Web → Nginx static site
# 1) Build the Flutter web app
FROM ghcr.io/cirruslabs/flutter:3.22.2 AS build
WORKDIR /app
# Pre-download dependencies
COPY pubspec.yaml ./
RUN flutter pub get
# Copy sources
COPY . .
# Build for web (release)
RUN flutter build web --release

# 2) Serve with Nginx
FROM nginx:alpine
# Copy custom nginx config (SPA + reverse proxy to backend service `app`)
COPY nginx.conf /etc/nginx/conf.d/default.conf
# Copy build output to Nginx html directory
COPY --from=build /app/build/web/ /usr/share/nginx/html/
# Basic healthcheck
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost/ || exit 1
# Expose default HTTP port
EXPOSE 80
