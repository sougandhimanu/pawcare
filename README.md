# Pawcare

Android app for booking pet care services (daycare, vet visits, grooming), with
Firebase authentication/database and Google Maps + Places for finding nearby
providers.

- `Pawcare/` – Android Studio project (Gradle, Java, `com.example.pawcare`)
- `pawcare_images/` – all working screenshots of the app are in this folder

## Setup

The build needs two pieces of configuration that are **not** committed:

### 1. Google Maps / Places API key

Add your key to `Pawcare/Pawcare/local.properties`:

```properties
MAPS_API_KEY=your_google_maps_api_key
```

It is injected at build time into the manifest (`${MAPS_API_KEY}`) and exposed to
code as `R.string.google_maps_key` (see `app/build.gradle.kts`). Restrict the key
to the app's package name and signing SHA-1 in the Google Cloud Console.

### 2. Firebase config

Download `google-services.json` for your Firebase project and place it at
`Pawcare/Pawcare/app/google-services.json`. See
`app/google-services.json.example` for the expected shape.

## Build

```
cd Pawcare/Pawcare
./gradlew assembleDebug
```
