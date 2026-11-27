# La Casa JoJo: backend de microservicios

Spring Boot 4.2 · Java 21 · Spring Cloud Gateway · JPA · H2 (o MySQL) · JWT

```
                 App Android (Retrofit)
                          │  http://10.0.2.2:8080  (emulador)
                          ▼
                 ┌──────────────────┐
                 │  gateway :8080   │  una sola URL base, CORS
                 └──────────────────┘
        /api/auth, /api/usuarios │ /api/productos │ /api/pedidos
            ▼                    ▼                 ▼
   ┌────────────────┐  ┌────────────────┐  ┌────────────────┐
   │ ms-usuarios    │  │ ms-productos   │  │ ms-pedidos     │
   │ :8081          │  │ :8082          │  │ :8083          │
   │ BD usuarios    │  │ BD productos   │  │ BD pedidos     │
   └────────────────┘  └────────────────┘  └────────────────┘
            ▲  /internal/** (X-Internal-Key)  │        │
            └───────────────────────────────────┘        │
                         ms-pedidos ─────────────────────► ms-productos
```

- Cada microservicio tiene **su propia base de datos** (`./data/usuarios`, `./data/productos`, `./data/pedidos`).
- **ms-pedidos** llama a **ms-usuarios** (datos del cliente) y a **ms-productos** (precios y disponibilidad vigentes)
  con `RestClient`. **ms-usuarios** llama a **ms-pedidos** antes de eliminar un usuario.
- Las rutas `/internal/**` son solo para llamadas entre servicios. El gateway no las publica y exigen el header `X-Internal-Key`.
- `casajojo-common` es código compartido: enums, errores, JWT, CORS y el cliente interno.

## Ejecutar

```bash
./iniciar-todo.sh          # Mac/Linux
iniciar-todo.bat           # Windows (abre una ventana por servicio)
./mvnw test                # 15 pruebas (H2 en memoria, sin red)
```

Desde IntelliJ también puedes correr cada `*Application.java`: primero `ms-usuarios`, `ms-productos` y `ms-pedidos`, y al final `gateway`.

Para comprobar que todo está arriba: http://localhost:8080/salud y `http://localhost:808{1,2,3}/salud`.
Consola H2 de cada servicio: `http://localhost:8081/h2-console` (JDBC `jdbc:h2:file:./data/usuarios`, usuario `sa`).

**MySQL (opcional):** `./mvnw -pl ms-usuarios spring-boot:run -Dspring-boot.run.profiles=mysql`, con las variables `DB_URL`, `DB_USER` y `DB_PASSWORD`.

**Variables de entorno** (todas tienen un valor por defecto para desarrollo): `JWT_SECRET` (el mismo en los 3 servicios, 32 caracteres o más),
`INTERNAL_KEY`, `SEED_PASSWORD` y `MS_USUARIOS_URL`/`MS_PRODUCTOS_URL`/`MS_PEDIDOS_URL`.

## Usuarios de demostración (clave `casajojo123`)

| Correo | Rol |
|---|---|
| admin@lacasajojo.cl | ADMINISTRADOR |
| cocina@lacasajojo.cl | COCINERO |
| caja@lacasajojo.cl | CAJERO |
| cliente@lacasajojo.cl | CLIENTE |

El menú de ejemplo (12 productos) se carga solo si la base está vacía.

## Roles y privilegios

| Acción | ADMIN | COCINERO | CAJERO | CLIENTE |
|---|:-:|:-:|:-:|:-:|
| Ver menú publicado | ✓ | ✓ | ✓ | ✓ (sin login) |
| Ver menú completo (ocultos) | ✓ | ✓ | ✓ | |
| Crear, editar, publicar o eliminar productos | ✓ | | | |
| Marcar producto agotado/disponible | ✓ | ✓ | ✓ | |
| Crear pedido | | | | ✓ |
| Ver pedidos | todos | todos | todos | solo los suyos |
| RECIBIDO → EN_PREPARACION → LISTO_RETIRO | ✓ | ✓ | | |
| LISTO_RETIRO → ENTREGADO | ✓ | | ✓ | |
| Foto del pedido | ✓ | ✓ | | |
| Eliminar pedido | ✓ | | | |
| Listar usuarios, crear personal, cambiar rol, eliminar | ✓ | | | |
| Ver y modificar su propio perfil, foto y contraseña | ✓ | ✓ | ✓ | ✓ |

## Autenticación

1. `POST /api/auth/login` responde `{ token, expiraEn, usuario }`.
2. La app envía `Authorization: Bearer <token>` en cada petición (con un interceptor de OkHttp).
3. Cada microservicio valida el token por su cuenta con la clave compartida, sin llamar a ms-usuarios.
   Si el token es inválido o venció responde 401, y si el rol no tiene permiso responde 403.

## Endpoints (todos a través de http://localhost:8080)

**ms-usuarios**

| Método | Ruta | Acceso | Cuerpo / notas |
|---|---|---|---|
| POST | `/api/auth/registro` | público | `{nombreCompleto, correo, password}` crea un CLIENTE. Con `rol` distinto se exige token de admin |
| POST | `/api/auth/login` | público | `{correo, password}` → `{token, expiraEn, usuario}` |
| GET | `/api/auth/yo` | token | perfil del dueño del token |
| POST | `/api/auth/recuperar` | público | `{correo}` → `{mensaje, codigoDemo, expiraEn}` (6 dígitos, 15 min) |
| POST | `/api/auth/restablecer` | público | `{correo, codigo, nuevaPassword}` |
| GET | `/api/usuarios?rol=` | admin | |
| POST | `/api/usuarios` | admin | `{nombreCompleto, correo, password, rol}` |
| GET / PUT | `/api/usuarios/{id}` | él mismo o admin | PUT: `{nombreCompleto, correo}` |
| PUT | `/api/usuarios/{id}/password` | él mismo | `{passwordActual, nuevaPassword}` → 204 |
| PATCH | `/api/usuarios/{id}/foto` | él mismo o admin | `{foto}` |
| PATCH | `/api/usuarios/{id}/rol` | admin | `{rol}` (siempre debe quedar un admin) |
| DELETE | `/api/usuarios/{id}` | admin | 409 si tiene pedidos |

**ms-productos**

| Método | Ruta | Acceso | Notas |
|---|---|---|---|
| GET | `/api/productos` | público | publicados, ordenados por categoría |
| GET | `/api/productos?publicados=false` | personal | todos |
| GET | `/api/productos/{id}` | público | |
| POST / PUT | `/api/productos` · `/{id}` | admin | `{nombre, categoria, descripcion?, precio, disponible?, publicado?, fotoProducto?}` |
| PATCH | `/api/productos/{id}/publicado` | admin | `{valor}` |
| PATCH | `/api/productos/{id}/disponible` | personal | `{valor}` |
| DELETE | `/api/productos/{id}` | admin | los pedidos guardan copia de nombre y precio |

**ms-pedidos** (todos exigen token)

| Método | Ruta | Acceso | Notas |
|---|---|---|---|
| GET | `/api/pedidos?estado=&clienteId=` | todos | al cliente siempre se le muestran solo los suyos |
| GET | `/api/pedidos/{id}` | personal o dueño | |
| GET | `/api/pedidos/pendientes-notificar` | token | pedidos del usuario con cambios de estado sin avisar |
| POST | `/api/pedidos` | cliente | `{id (UUID), seleccion: {"PLATO_FONDO":1,"BEBIDA":11}, especificaciones?, horaRetiro}` → 201. Responde 200 sin duplicar si el id ya existía |
| PATCH | `/api/pedidos/{id}/estado` | según tabla de roles | `{estado}` |
| PATCH | `/api/pedidos/{id}/notificado` | dueño | `{estado}` |
| PATCH | `/api/pedidos/{id}/foto` | admin, cocinero | `{foto}` |
| DELETE | `/api/pedidos/{id}` | admin | |

**Errores** (todos los servicios):
`{"status":400,"mensaje":"...","errores":{"campo":"mensaje"},"timestamp":...}`.
Si otro microservicio está apagado, la respuesta es 503.

## Cambios necesarios en la app Android

- Agregar `COCINERO` y `CAJERO` al enum `Rol`, y `ENTREGADO` al enum `EstadoPedido` (Gson no reconoce valores nuevos).
- URL base `http://10.0.2.2:8080/`, permiso `INTERNET` y tráfico HTTP en claro (solo en debug).
- Guardar el `token` del login (por ejemplo en DataStore) y enviarlo con un interceptor de OkHttp.
