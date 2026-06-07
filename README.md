# ⚙️ Itera - Backend Principal (Scala/Play)

API REST principal y API Gateway para la plataforma Itera, desarrollado con Scala y Play Framework. Adhiere a **Vertical Slice Architecture** y **CQRS**.

## 🚀 Stack Tecnológico

- **Scala 2.13**
- **Play Framework 2.9**
- **PostgreSQL** (via Doobie / Neon.tech)
- **Cats Effect 3** (programación funcional)
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
sbt run
# El servidor estará en http://localhost:8080
```

## 📁 Estructura del Proyecto

El proyecto sigue una arquitectura de **Vertical Slices**:

```
Itera/
├── app/
│   ├── controllers/        # Adaptadores Play para las rutas
│   ├── modules/            # Módulos Guice para DI
├── src/main/scala/itera/
│   ├── features/           # Slices verticales por funcionalidad
│   │   └── auth/           # Dominio, Comandos y Queries de Auth
│   ├── shared/             # Infraestructura y dominio compartido
├── conf/
│   ├── application.conf    # Configuración principal
│   ├── routes              # Definición de rutas Play
│   └── db/migration/       # Scripts Flyway
├── build.sbt               # Dependencias
└── Dockerfile              # Imagen Docker
```

## 🔌 API Endpoints

### Auth
- `POST /api/v1/auth/register` - Registro de usuarios
- `POST /api/v1/auth/login` - Inicio de sesión

### Health Check
- `GET /health`

## ⚙️ Variables de Entorno

Consulte el archivo `.env` en la raíz del workspace.
