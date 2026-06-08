# 📖 Documentación de la API - Itera (Servicio Core)

Esta documentación describe los endpoints disponibles en el microservicio principal de Itera, desarrollado en Scala/Play.

## 🔐 Autenticación
La mayoría de los endpoints requieren un token **JWT** en la cabecera de la petición:
`Authorization: Bearer <tu_token_jwt>`

---

## 🛠️ Resumen de Endpoints

| Módulo | Endpoint | Método | Descripción | Requiere Auth |
| :--- | :--- | :---: | :--- | :---: |
| **Salud** | `/health` | `GET` | Estado del servicio y conexiones (IA, Lógica). | No |
| **Auth** | `/api/core/auth/register` | `POST` | Registro de nuevos usuarios. | No |
| **Auth** | `/api/core/auth/login` | `POST` | Obtención de token JWT. | No |
| **Perfil** | `/api/core/profile` | `GET` | Obtener perfil académico completo. | Sí |
| **Perfil** | `/api/core/profile/initialize` | `POST` | Configuración inicial del perfil. | Sí |
| **Perfil** | `/api/core/profile` | `PUT` | Actualización de datos del perfil. | Sí |
| **Metas** | `/api/core/goals` | `GET` | Listar metas del estudiante. | Sí |
| **Metas** | `/api/core/goals` | `POST` | Definir nuevas metas. | Sí |
| **Catálogos** | `/api/core/catalogs/institutions` | `GET` | Listar universidades/instituciones. | No |
| **Progreso** | `/api/core/progress/evidence` | `POST` | Subir evidencia de cumplimiento. | Sí |

---

## 📝 Detalles Técnicos

### 1. Autenticación

#### **Registro (`POST /api/core/auth/register`)**
- **Cuerpo (JSON):**
  ```json
  {
    "email": "usuario@ejemplo.com",
    "password": "Password123"
  }
  ```
- **Respuesta (201 Created):** Datos del usuario y token.

#### **Login (`POST /api/core/auth/login`)**
- **Cuerpo (JSON):** Igual al registro.
- **Respuesta (200 OK):**
  ```json
  {
    "token": "eyJhbG...",
    "userId": "uuid-...",
    "email": "usuario@ejemplo.com"
  }
  ```

### 2. Perfil Académico

#### **Inicializar Perfil (`POST /api/core/profile/initialize`)**
- **Cuerpo (JSON):**
  ```json
  {
    "userId": "uuid-del-usuario",
    "fullName": "Nombre Apellido",
    "currentInstitution": "Universidad X",
    "targetRole": "Software Engineer",
    "skills": [
      { "name": "Scala", "level": "Beginner" }
    ]
  }
  ```

### 3. Progreso

#### **Subir Evidencia (`POST /api/core/progress/evidence`)**
- **Cuerpo (JSON):**
  ```json
  {
    "userId": "uuid-del-usuario",
    "nodeId": "uuid-del-nodo-roadmap",
    "evidenceUrl": "https://github.com/usuario/repo",
    "description": "Completé el módulo de testing."
  }
  ```

---

## 🚀 Guía de Pruebas Rápida (CURL)

```bash
# 1. Registro
curl -X POST http://localhost:8080/api/core/auth/register -H "Content-Type: application/json" -d '{"email":"test@itera.com","password":"Password123"}'

# 2. Login
curl -X POST http://localhost:8080/api/core/auth/login -H "Content-Type: application/json" -d '{"email":"test@itera.com","password":"Password123"}'
```
