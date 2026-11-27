# La Casa JoJo — Gestión digital de comandas y pedidos (Android)

Aplicación móvil Android para **La Casa JoJo SpA**, un negocio de almuerzos y menús diarios en Viña del Mar. Reemplaza la gestión de pedidos por WhatsApp y papelitos con un sistema digital:

- el administrador publica el menú del día;
- el cliente arma su pedido y elige la hora de retiro;
- la cocina y la caja gestionan las comandas cambiando su estado.

La app se conecta a un **backend propio de microservicios Spring Boot** y además consume una **API externa pública**.

Proyecto académico — **DSY1105 · Desarrollo de Aplicaciones Móviles** (CITT Viña del Mar).

## Integrantes

| Integrante ||
|---|---|
| Tomás Silva ||
| Josafat Mencia ||
| Elías Teran ||
| Felipe Oyarzún ||

- Planificación en Trello: ENLACE_AL_TABLERO
- Repositorio del backend (microservicios): ENLACE_AL_REPO_BACKEND

## Equipo y responsabilidades

| # | Tarea | Responsable |
|---|-------|-------------|
| 1 | Crear repositorio del proyecto | Tomy |
| 2 | Preparar estructura base de la aplicación | Josafat |
| 3 | Incorporar animaciones | Tomy |
| 4 | Incorporar periféricos | Elías |
| 5 | Implementar persistencia local con SQLite | Benja |
| 6 | Backend de microservicios (Spring Boot) | RESPONSABLE |
| 7 | Integración de la app con los microservicios (Retrofit) | RESPONSABLE |
| 8 | Recuperación de contraseña, perfil y 4 roles | RESPONSABLE |
| 9 | API externa (Open-Meteo) | RESPONSABLE |
| 10 | Pruebas unitarias de ViewModels (JUnit 5 + MockK) | RESPONSABLE |
| 11 | Firma del APK | RESPONSABLE |

## Tecnologías

**App**
- **Kotlin 2.0.21**, **Jetpack Compose** (BOM 2024.12.01) y **Material Design 3**.
- **MVVM** con ViewModels y `StateFlow`.
- **Room 2.6.1 (SQLite)** con KSP, esquemas exportados y migraciones.
- **Retrofit + OkHttp + Gson** para los microservicios propios y la API externa.
- **Navigation Compose**, **Kotlin Coroutines y Flow**.
- **JUnit 5, MockK y Turbine** para las pruebas, y **Kover** para la cobertura.
- Android Gradle Plugin 8.7.3, `minSdk 26`, `targetSdk / compileSdk 35`.

**Backend** (repositorio aparte)
- Spring Boot 4.2 con Java 21.
- Spring Cloud Gateway.
- JPA con H2 o MySQL.
- JWT y BCrypt.

## Arquitectura de datos: local vs. externo

| Dato | Dónde vive | Por qué |
|---|---|---|
| Usuarios y credenciales | Remoto (ms-usuarios) | Una cuenta sirve en cualquier dispositivo; la clave se valida en el servidor (BCrypt) |
| Token de sesión | Local (SesionStore) | Mantener la sesión al cerrar y abrir la app |
| Menú del día | Remoto (ms-productos) + caché en Room | El administrador lo cambia y todos lo ven; sin conexión se muestra el último menú guardado |
| Pedidos | Remoto (ms-pedidos) + caché en Room | El cliente pide desde su teléfono y la cocina gestiona desde otro |
| Fotos | Local (carpeta privada de la app) | Solo se sincroniza el nombre del archivo |
| Clima (API externa) | Solo en memoria | No se mezcla con los datos propios |

## Funcionalidades

**Autenticación (todos los roles)**
- Registro, inicio de sesión, **recuperación de contraseña** con código de 6 dígitos y **modificación del perfil** (nombre, correo, contraseña y foto).
- Validaciones de campos, formato de correo, contraseñas iguales y correo único (sin distinguir mayúsculas).
- Las contraseñas se guardan en el servidor con BCrypt y la sesión con un token JWT.

**Roles y privilegios**

| Rol | Puede |
|---|---|
| Administrador | Panel resumen, menú del día (CRUD), gestión de usuarios y roles, todas las comandas, carga del menú de demostración |
| Cocinero | Ver comandas, avanzarlas (Recibido → En preparación → Listo para retiro), foto del pedido preparado, marcar productos agotados |
| Cajero | Entregar pedidos listos (Listo para retiro → Entregado), marcar productos agotados |
| Cliente | Menú publicado por categoría, nuevo pedido con hora de retiro, historial y detalle, ubicación del local, notificaciones |

**Cliente**
- El menú publicado se muestra agrupado por categoría: plato de fondo, acompañamiento, ensalada, postre y bebida. Los productos agotados no se pueden elegir.
- El pedido nuevo tiene especificaciones, selector de hora nativo, total automático y confirmación.
- El pedido guarda el nombre y el precio de cada producto al momento de pedir.
- Clima del día (API externa) en la pantalla de inicio.
- Notificaciones locales cuando cambia el estado del pedido.

## Endpoints usados

### Propios (vía gateway `http://10.0.2.2:8080`)

| Microservicio | Endpoints principales |
|---|---|
| ms-usuarios | `POST /api/auth/registro` · `POST /api/auth/login` · `POST /api/auth/recuperar` · `POST /api/auth/restablecer` · `GET/PUT /api/usuarios/{id}` · `PUT /api/usuarios/{id}/password` · `PATCH /api/usuarios/{id}/rol` · `DELETE /api/usuarios/{id}` |
| ms-productos | `GET /api/productos` · `POST /api/productos` · `PUT /api/productos/{id}` · `PATCH /api/productos/{id}/disponible` · `DELETE /api/productos/{id}` |
| ms-pedidos | `GET /api/pedidos` · `POST /api/pedidos` · `PATCH /api/pedidos/{id}/estado` · `PATCH /api/pedidos/{id}/foto` · `DELETE /api/pedidos/{id}` |

La tabla completa, con los permisos de cada rol, está en el README del backend.

### Externo

- **Open-Meteo** (clima actual, sin API key): `GET https://api.open-meteo.com/v1/forecast?latitude=..&longitude=..&current=temperature_2m,weather_code`

## Tareas del equipo

### 1. Repositorio del proyecto — Tomy
Repositorio en GitHub con `.gitignore`, historial de commits y README del proyecto.

### 2. Estructura base de la aplicación — Josafat
- Proyecto Android con Gradle Kotlin DSL y catálogo de versiones (`libs.versions.toml`).
- Arquitectura por capas y navegación centralizada (`Rutas`, `AppNavHost`).
- Inyección manual de dependencias mediante `AppContainer` y `ViewModelFactories`.
- Tema Material 3 con modo claro y oscuro, y componentes reutilizables (`Estructura`, `Tarjetas`, `Formularios`).

### 3. Animaciones — Tomy
Animaciones propias dibujadas con Canvas y `rememberInfiniteTransition`, en `ui/components`:
- `SartenCargando.kt`: sartén animada para los estados de carga.
- `CacerolaCargando.kt`: cacerola animada para los estados de carga.
- `Animaciones.kt`: animación de éxito al confirmar un pedido.

### 4. Periféricos — Elías
- **Cámara** (`Camara.kt`, `AlmacenFotos.kt`): toma fotos con la app de cámara del sistema (`ActivityResultContracts.TakePicture`) y `FileProvider`. No requiere el permiso `CAMERA`, así que la app funciona aunque el equipo no tenga cámara.
- **Galería**: selección de imágenes con `PickVisualMedia`, sin permisos de almacenamiento.
- **Notificaciones locales** (`NotificadorAndroid`, `NotificadorPedidos`): usan el permiso `POST_NOTIFICATIONS` en Android 13 o superior.
- **Ubicación del local** (`Ubicacion.kt`): abre Google Maps (o el navegador) con la dirección del local mediante un enlace.
- **Selector de hora nativo** para el retiro.
- Las fotos se guardan en la carpeta privada de la app y se referencian desde la base de datos.

### 5. Persistencia local con SQLite — Benja
- **Room** sobre SQLite (`casajojo.db`), con `exportSchema = true` (esquemas en `app/schemas`).
- **Entidades:** `UsuarioEntity`, `ProductoEntity`, `PedidoEntity` y `DetallePedidoEntity`.
- **DAOs:** `UsuarioDao`, `ProductoDao` y `PedidoDao`, con consultas reactivas mediante Flow.
- **Repositorios:** `AuthRepository`, `UsuarioRepository`, `MenuRepository` y `PedidoRepository`.
- **Migraciones** que conservan los datos: v1→v2 (fotos de perfil y de pedido) y v2→v3 (foto del producto).
- `TypeConverters` para enums y fechas.
- `allowBackup` desactivado para proteger los datos personales de los clientes.

### 6. Backend de microservicios — RESPONSABLE
Gateway (8080), ms-usuarios (8081), ms-productos (8082) y ms-pedidos (8083), cada uno con su propia base de datos. Los servicios se comunican entre sí y la autenticación usa JWT con 4 roles.

### 7 a 11. COMPLETAR
Describir cada tarea igual que las anteriores cuando esté terminada.

## Estructura del proyecto

```
app/src/main/java/cl/lacasajojo/app/
├── MainActivity.kt, CasaJoJoApplication.kt
├── data/
│   ├── database/      AppDatabase, Converters
│   ├── dao/           UsuarioDao, ProductoDao, PedidoDao
│   ├── entity/        Usuario, Producto, Pedido, Enums
│   ├── remote/        CasaJoJoApi, RetrofitClient, DTOs, API externa (clima)
│   ├── repository/    Auth, Usuario, Menu, Pedido, Clima, SesionStore
│   ├── AppContainer.kt, DatosDemostracion.kt
├── viewmodel/         ViewModels y factories
├── ui/
│   ├── navigation/    Rutas, AppNavHost
│   ├── theme/         Theme, Type
│   ├── components/    Cámara, animaciones, tarjetas, formularios
│   └── screens/       auth/ · admin/ · cocina/ · caja/ · cliente/
└── util/              Validaciones, Formato, Tiempo, AlmacenFotos,
                       Ubicacion, Notificadores
```

## Pantallas

| Rol | Pantallas |
|-----|-----------|
| Común | Inicio, Login, Registro, Recuperar contraseña, Mi perfil |
| Administrador | Panel, Menú del día, Formulario de producto, Comandas, Usuarios |
| Cocinero | Comandas de cocina |
| Cajero | Pedidos por entregar |
| Cliente | Inicio (con clima), Menú, Nuevo pedido, Pedido confirmado, Historial, Detalle de pedido |

## Requisitos

- Android Studio (versión compatible con AGP 8.7.3) y JDK 17 para la app.
- JDK 21 para el backend.
- Emulador o teléfono con **Android 8.0 (API 26)** o superior.

## Cómo ejecutar

1. **Backend:** clonar el repositorio del backend y ejecutar `iniciar-todo.bat` (Windows) o `./iniciar-todo.sh`. Comprobar que responde en http://localhost:8080/salud.
2. **App:** clonar este repositorio, abrirlo en Android Studio y esperar la sincronización de Gradle.
3. Seleccionar un emulador y presionar **Run**. La app apunta a `http://10.0.2.2:8080/`.
4. **Usuarios de prueba** (clave `casajojo123`): admin@, cocina@, caja@ y cliente@lacasajojo.cl.

Compilar desde terminal:

```bash
./gradlew assembleDebug
```

## Pruebas

```bash
./gradlew testDebugUnitTest        # unitarias (JVM)
./gradlew koverHtmlReportDebug     # reporte de cobertura en app/build/reports/kover/
./gradlew connectedAndroidTest     # instrumentadas (requieren emulador o dispositivo)
```

- **Unitarias** (`app/src/test`): ViewModels con JUnit 5, MockK y Turbine, más la lógica y las utilidades. Cobertura de ViewModels: **XX %**.
- **Instrumentadas** (`app/src/androidTest`) con Room real: autenticación, menú y pedidos, fotos y persistencia.

## APK firmado

- **APK:** `apk/casajojo-release.apk`
- **Keystore:** `keystore/casajojo-release.jks`
- **Configuración:** `signingConfigs` en `app/build.gradle.kts`. Las contraseñas se leen desde `keystore.properties`, que **no** se sube al repositorio.
- **Para generarlo:** `./gradlew assembleRelease`

## Guía rápida de uso

1. Iniciar sesión como **admin@lacasajojo.cl** y revisar o editar el menú del día.
2. Iniciar sesión como **cliente@lacasajojo.cl**, ver el clima, armar un pedido, elegir la hora de retiro y confirmar.
3. Iniciar sesión como **cocina@lacasajojo.cl** y avanzar la comanda hasta "Listo para retiro".
4. Iniciar sesión como **caja@lacasajojo.cl** y marcar el pedido como "Entregado".
5. Como cliente, revisar la notificación, el historial y la ubicación del local.

## Limitaciones conocidas

- Las notificaciones se generan localmente cuando la app detecta el cambio de estado.
- La ubicación del local se abre mediante un enlace a Google Maps; no se integra el SDK de mapas.
- La recuperación de contraseña muestra el código en pantalla (modo demo), porque el proyecto no tiene un servidor de correo.
