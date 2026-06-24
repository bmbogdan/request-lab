# RequestLab

A lightweight, offline-first HTTP client for Android — a "Postman-lite" you carry in your pocket. Build and send HTTP requests, organize them into collections, manage environments with token substitution, and review past responses, all from a clean Jetpack Compose interface.

## Screenshots

| Request Builder | Response Viewer | History |
|:---:|:---:|:---:|
| ![Builder](docs/screenshots/01-builder.png) | ![Response](docs/screenshots/02-response.png) | ![History](docs/screenshots/03-history.png) |

| Collections | Docs |
|:---:|:---:|
| ![Collections](docs/screenshots/04-collections.png) | ![Docs](docs/screenshots/05-docs.png) |

## Features

- **Request Builder** — Compose requests with any HTTP method, headers, query params, body, and auth, then inspect the full response.
- **Collections** — Group related requests so they're easy to find and reuse.
- **Environments** — Define variables and inject them into requests via `{{token}}` substitution; switch environments without rewriting URLs.
- **History** — Every sent request is recorded for quick replay and review.
- **Docs** — Built-in reference articles bundled with the app.
- **Settings** — App-wide preferences persisted across launches.
- **Secure secrets** — Sensitive values are encrypted at rest using the Android Keystore (AES/GCM).

## Tech Stack

| Concern            | Choice                                            |
|--------------------|---------------------------------------------------|
| Language           | Kotlin (JVM 17)                                   |
| UI                 | Jetpack Compose + Material 3                       |
| Architecture       | MVVM, feature-based modules, `UiState` + events   |
| Dependency Injection | Hilt                                             |
| Persistence        | Room (with KSP) + DataStore (preferences)         |
| Networking         | OkHttp                                             |
| Serialization      | kotlinx.serialization (JSON)                       |
| Crypto             | Android Keystore (AES/GCM secret cipher)          |
| Build              | Gradle (Kotlin DSL) + version catalog             |

## Requirements

- **Android Studio** (latest stable recommended)
- **JDK 17**
- **Android SDK** — `compileSdk` / `targetSdk` **36**, `minSdk` **26**

> `minSdk 26` is required for Keystore AES/GCM, `java.time`, and SAF persistable URI permissions.

## Project Structure

```
app/src/main/java/eu/mihaibadea/requestlab/
├── core/                 # Cross-cutting infrastructure
│   ├── common/           # Result/error model, dispatchers, connectivity
│   ├── crypto/           # Keystore-backed secret cipher
│   ├── database/         # Room database, DAOs, type converters
│   ├── datastore/        # Preferences storage
│   ├── designsystem/     # Theme, tokens, reusable components
│   ├── navigation/       # NavHost + destinations
│   └── network/          # HttpEngine (OkHttp) + DI
└── feature/              # Feature modules
    ├── builder/          # Request builder & response viewer
    ├── collections/      # Saved request collections
    ├── docs/             # In-app documentation
    ├── environments/     # Environments & token substitution
    ├── history/          # Request history
    └── settings/         # App settings
```

## Getting Started

```bash
# Clone
git clone https://github.com/bmbogdan/request-lab.git
cd request-lab

# Build a debug APK
./gradlew assembleDebug

# Install on a connected device/emulator
./gradlew installDebug
```

Or open the project in Android Studio and run the **app** configuration.

## Testing

```bash
# Unit tests (ViewModels, repositories, use cases)
./gradlew test

# Instrumented tests (requires a connected device/emulator)
./gradlew connectedAndroidTest
```

## License

```
MIT License

Copyright (c) 2026 Mihai Badea

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
