# ⚙️ Itera - Backend Principal (Scala/Play)

API REST principal y API Gateway para la plataforma Itera, desarrollado con Scala y Play Framework.

## 🚀 Stack Tecnológico

- **Scala 2.13**
- **Play Framework 2.9**
- **PostgreSQL** (via JDBC)
- **Anorm** (acceso a datos)
- **Flyway** (migraciones de base de datos)
- **JWT** (autenticación)
- **BCrypt** (hash de contraseñas)

## 📋 Requisitos

- **JDK 17+**
- **sbt 1.10+**
- **PostgreSQL 14+** (o Docker)

## 🏁 Inicio Rápido

### Opción 1: Con Docker (Recomendado)

```bash
# Construir imagen
docker build -t itera-scala .

# Ejecutar (requiere PostgreSQL)
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/itera \
  -e DB_USER=postgres \
  -e DB_PASSWORD=postgres \
  -e PLAY_SECRET_KEY=changeme \
  itera-scala
```

### Opción 2: Desarrollo Local con sbt

```bash
# 1. Asegúrate de tener PostgreSQL corriendo
# 2. Configura las variables en conf/application.conf o usa .env
# 3. Ejecutar
sbt run

# El servidor estará en http://localhost:8080
```

### Opción 3: Docker Compose (Todo el ecosistema)

Desde la raíz del workspace:

```bash
cd ../  # itera-workspace/
docker-compose up db backend-scala
```

## 📁 Estructura del Proyecto

```
Itera/
├── app/
│   ├── controllers/        # Controladores HTTP
│   │   └── HealthController.scala
│   ├── models/             # Case classes y dominio
│   ├── repositories/       # Acceso a datos (Anorm)
│   ├── services/           # Lógica de negocio
│   └── modules/            # Módulos Guice
│       └── FlywayModule.scala
├── conf/
│   ├── application.conf    # Configuración principal
│   ├── routes              # Definición de rutas
│   ├── logback.xml         # Configuración de logs
│   └── db/migration/       # Scripts Flyway
│       └── V1__create_users.sql
├── project/                # Configuración sbt
│   ├── build.properties
│   └── plugins.sbt
├── public/                 # Assets estáticos
├── build.sbt               # Dependencias
├── Dockerfile              # Imagen Docker
└── README.md               # Este archivo
```

## 🔌 API Endpoints

### Health Check

```http
GET /health
```

**Respuesta:**
```json
{
  "status": "ok",
  "service": "itera-scala"
}
```

### Próximos Endpoints (Sprints siguientes)

- `POST /api/auth/register` - Registro de usuarios
- `POST /api/auth/login` - Inicio de sesión
- `GET /api/progress` - Progreso del usuario
- `PUT /api/progress/:id` - Actualizar progreso
- `GET /api/gateway/*` - Proxy a microservicios

## ⚙️ Variables de Entorno

| Variable | Descripción | Default |
|----------|-------------|---------|
| `DB_URL` | JDBC URL de PostgreSQL | `jdbc:postgresql://localhost:5432/itera` |
| `DB_USER` | Usuario de PostgreSQL | `postgres` |
| `DB_PASSWORD` | Contraseña de PostgreSQL | `postgres` |
| `PLAY_SECRET_KEY` | Secret key de Play Framework | `changeme` |
| `JWT_SECRET` | Secret para tokens JWT | `changeme-change-this...` |
| `PYTHON_API_URL` | URL del backend Python | `http://localhost:8000` |
| `PROLOG_URL` | URL del motor Prolog | `http://localhost:9000` |

## 🧪 Tests

```bash
sbt test
```

## 🐳 Docker

### Build

```bash
docker build -t itera-scala:latest .
```

### Run

```bash
docker run -p 8080:8080 itera-scala:latest
```

## 🔗 Integración con Otros Servicios

Este servicio es consumido por:

- **itera-angular** (Frontend) - Consume API REST en :8080
- **Itera-python** (Backend auxiliar) - Proxy via /api/gateway
- **itera-prolog** (Motor de lógica) - Proxy via /api/gateway

## 📄 Licencia

MIT

---

**Puerto:** 8080  
**Última actualización:** Mayo 2026
