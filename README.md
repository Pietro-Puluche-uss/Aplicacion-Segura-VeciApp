# Aplicacion Segura VeciApp

Aplicacion movil Android de VeciApp construida con Kotlin, Jetpack Compose y Retrofit.

La app consume la API desplegada en Render:

- `https://veciapp-9jtw.onrender.com/`

## Modulos incluidos

- Login y registro
- Inicio con panel principal
- Emergencias
- Reporte de incidentes
- Historial
- Perfil y ubicacion
- Suscripciones
- Grupo familiar

## Configuracion de la API

La URL base esta configurada en:

- `app/build.gradle.kts`

Campo actual:

- `BuildConfig.API_BASE_URL = "https://veciapp-9jtw.onrender.com/"`

## Compilacion local

1. Instala o configura Android SDK
2. Usa JDK 21
3. Ejecuta:

```powershell
.\gradlew.bat :app:assembleDebug
```

APK generado:

- `app/build/outputs/apk/debug/app-debug.apk`

## Nota para Windows

Si Gradle falla al empaquetar por rutas demasiado largas, mueve el proyecto a una ruta mas corta o compila desde una unidad temporal con `subst`.
