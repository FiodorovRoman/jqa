JQA Flutter Frontend (Web + Android)

This is a minimal Flutter UI for the JQA backend located in the repository root. It supports:
- Listing questions
- Viewing question details
- Voting on questions and answers
- Creating new questions and answers
- Marking an answer as right
- Authentication via Basic Auth (username/password). It can also forward X-Google-Id and X-Facebook-Id headers if you wire it up.

Backend prerequisites
- Run the Spring Boot backend on a reachable host (default assumed is http://localhost:8080).
- CORS is enabled in this repository so the Flutter web app can call the backend during development.

Project layout
- lib/api.dart — HTTP client calling the backend endpoints
- lib/main.dart — Basic UI using Material 3
- web/index.html — Minimal bootstrapping for Flutter Web
- pubspec.yaml — Minimal dependencies (flutter + http)

How to run (Web)
1) Install Flutter (3.22+ recommended) and enable web: `flutter config --enable-web`
2) From this folder: `flutter pub get`
3) Run: `flutter run -d chrome -t lib/main.dart`
4) In the app top bar, set the backend Base URL if not default (e.g., http://localhost:8080) and set username/password (defaults are alice/secret from tests).

How to build (Web)
- `flutter build web` — outputs to `build/web`. You can host these static files behind any web server or configure Spring Boot to serve them.

How to run (Android)
1) Install Android SDK and set up a device/emulator
2) From this folder: `flutter pub get`
3) Create platform folders (if missing): `flutter create .`
4) Run on a device: `flutter run -d <device_id>`

Notes
- For Android, ensure the device can reach your backend host. If the backend is on your dev machine, use your machine IP (e.g., http://192.168.x.x:8080) instead of localhost. Update it in the app top bar.
- If you prefer OAuth stubs instead of Basic, populate either `googleId` or `facebookId` on the ApiClient (e.g., in a small settings screen) — the client already supports sending `X-Google-Id` or `X-Facebook-Id` headers.
- Input validations in the UI mirror backend constraints (question content <= 2000 chars; answer content <= 500 chars).

Optional: Serve built web UI from Spring Boot
- Build: `flutter build web`
- Copy `build/web` into Spring Boot's static resources (e.g., `src/main/resources/static/`), or configure an external reverse proxy.
