# SD LYGANE

Launcher multimedia Android adaptable a pantallas grandes y utilizable dentro de Samsung DeX. Proyecto nativo Kotlin + Jetpack Compose.

## Objetivo de esta versión

- Una misma aplicación para Android (incluida la navegación tipo TV) y Samsung DeX.
- Pantalla principal con accesos a YouTube, Netflix, juegos y biblioteca de aplicaciones.
- Detección de actividades que aparecen en el lanzador de Android, con iconos y apertura de aplicaciones.
- Sección de juegos basada en la categoría declarada por cada aplicación Android.
- Interfaz redimensionable para ventana/pantalla externa, con navegación por ratón, teclado y mandos que emulen teclas de dirección/Enter.
- Configuración informativa del dispositivo.

## Requisitos

- Android Studio compatible con Android Gradle Plugin 8.9.2.
- JDK 17.
- Android SDK 35.
- Samsung Galaxy S22 5G con One UI 6.1 para las pruebas DeX.

## Abrir y compilar

1. Descomprime el ZIP y abre la carpeta `SD-LYGANE` en Android Studio.
2. Espera la sincronización de Gradle (requiere conexión para descargar dependencias).
3. Ejecuta `app` en el teléfono o emulador.
4. Para compilar desde terminal: `./gradlew assembleDebug` (Linux/macOS) o `gradlew.bat assembleDebug` (Windows con Gradle instalado).

El proyecto usa Gradle 8.11.1. El script `gradlew` incluido descarga esa distribución si no existe una instalación local; necesita `curl` o `wget` y `unzip`. Se incluye `gradle-wrapper.properties`, pero no el JAR oficial del wrapper porque no estaba presente en el archivo original y no se pudo descargar durante esta edición.

## Uso en Samsung DeX

Conecta el S22 a una pantalla y abre SD LYGANE desde DeX. La actividad es redimensionable y se adapta al espacio de la ventana. Android/Samsung siguen controlando el escritorio, las barras del sistema y la elección de qué aplicación se abre; una app normal no puede reemplazar el launcher protegido de DeX ni forzar su apertura automáticamente al conectar HDMI.

## Visibilidad de aplicaciones

El manifiesto declara consultas de actividades `MAIN` + `LAUNCHER`, en lugar de solicitar `QUERY_ALL_PACKAGES`. Android solo devuelve las aplicaciones que coinciden con esa consulta y permite verlas según las reglas de visibilidad del sistema. La categoría Juegos depende de que cada app declare `CATEGORY_GAME`; algunos juegos podrían no clasificarse automáticamente.

## Limitaciones / siguientes pruebas

- Probar sincronización y compilación en Android Studio; este entorno de edición no dispone de Android SDK/Gradle para ejecutar un build.
- Verificar apertura de YouTube/Netflix según las versiones instaladas y el perfil del dispositivo.
- Validar clasificación de juegos, foco con mando Bluetooth y comportamiento al cambiar entre pantalla del teléfono y DeX.
- No se ha implementado inicio automático al conectar HDMI ni sustitución del escritorio DeX.
