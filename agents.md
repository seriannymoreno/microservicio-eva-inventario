# 📦 API de Inventario — Documentación del Proyecto

> **Spring Boot 4.2 · Java 21 · JDBC Template · MySQL 8**

---

## Descripción General

API REST desarrollada con **Spring Boot** para gestionar el inventario de productos. Permite **consultar** (con paginación) y **eliminar** registros del inventario, conectándose a una base de datos **MySQL** mediante **JDBC Template**.

---

## Estructura del Proyecto

```
api/
├── src/
│   ├── main/
│   │   ├── java/com/microservicioeva/api/
│   │   │   ├── ApiApplication.java           # Clase principal Spring Boot
│   │   │   ├── config/
│   │   │   │   └── CorsConfig.java           # Configuración global de CORS
│   │   │   ├── controller/
│   │   │   │   └── InventarioController.java # RestController – endpoints REST
│   │   │   ├── dto/
│   │   │   │   └── PageResponse.java         # DTO genérico para respuestas paginadas
│   │   │   ├── exception/
│   │   │   │   ├── ApiException.java         # Excepción personalizada de negocio
│   │   │   │   ├── ErrorResponse.java        # Estructura JSON de error estandarizada
│   │   │   │   └── GlobalExceptionHandler.java # Manejador global (@RestControllerAdvice)
│   │   │   ├── model/
│   │   │   │   └── Inventario.java           # Modelo de dominio (entidad inventario)
│   │   │   ├── repository/
│   │   │   │   └── InventarioRepository.java # Acceso a datos con JdbcTemplate
│   │   │   └── service/
│   │   │       └── InventarioService.java    # Lógica de negocio y validaciones
│   │   └── resources/
│   │       └── application.properties        # Configuración de la aplicación y logs
│   └── test/
│       └── java/com/microservicioeva/api/
│           └── ApiApplicationTests.java
├── logs/
│   └── inventario-api.log                    # Archivo de log generado en runtime
├── pom.xml
└── agents.md                                 # Este archivo
```

---

## Capas de la Arquitectura

```
Controller  →  Service  →  Repository  →  MySQL (JDBC)
   ↓                ↓
 CORS          Validaciones       JdbcTemplate
 HTTP Codes    Logs (SLF4J)      RowMapper
 Try/Catch     ApiException
```

| Capa | Clase | Responsabilidad |
|------|-------|----------------|
| **Controller** | `InventarioController` | Recibir peticiones HTTP, aplicar CORS, retornar HTTP 200/400/500 |
| **Service** | `InventarioService` | Lógica de negocio, validaciones, logging |
| **Repository** | `InventarioRepository` | Consultas SQL con `JdbcTemplate`, mapeo de resultados |
| **Model** | `Inventario` | Entidad de dominio con campos del inventario |
| **DTO** | `PageResponse<T>` | Envolver respuestas paginadas en JSON estructurado |
| **Exception** | `GlobalExceptionHandler` | Captura global de errores 400 / 500 |
| **Config** | `CorsConfig` | Configuración global de CORS para peticiones externas |

---

## Endpoints de la API

### Base URL
```
http://localhost:8080/api/v1/inventario
```

---

### 1. 📋 Consultar Inventario con Paginación

```http
GET /api/v1/inventario?page={page}&size={size}
```

**Parámetros de Query:**

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `page` | `int` | No | `0` | Número de página (0-based) |
| `size` | `int` | No | `10` | Registros por página (máx. 100) |

**Respuesta exitosa (200 OK):**
```json
{
  "content": [
    {
      "id_inventario": 1,
      "id_producto": 10,
      "cantidad": 50.0,
      "stock_minimo": 5,
      "stock_maximo": 200
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

**Error 400 (parámetros inválidos):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "El parámetro 'size' debe estar entre 1 y 100."
}
```

---

### 2. 🔍 Consultar Inventario por ID

```http
GET /api/v1/inventario/{id}
```

**Respuesta exitosa (200 OK):**
```json
{
  "id_inventario": 1,
  "id_producto": 10,
  "cantidad": 50.0,
  "stock_minimo": 5,
  "stock_maximo": 200
}
```

---

### 3. 🗑️ Eliminar Producto del Inventario

```http
DELETE /api/v1/inventario/{id}
```

**Respuesta exitosa (200 OK):**
```json
{
  "status": 200,
  "message": "Inventario con ID 1 eliminado exitosamente."
}
```

---

## Manejo de Errores

| Código | Situación | Clase responsable |
|--------|-----------|-------------------|
| `200 OK` | Operación exitosa | `InventarioController` |
| `400 Bad Request` | ID inválido, parámetros incorrectos, registro no encontrado | `GlobalExceptionHandler`, `ApiException` |
| `500 Internal Server Error` | Error de BD, error inesperado del servidor | `GlobalExceptionHandler` |

**Estructura estándar de error:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Descripción del error",
  "path": "/api/v1/inventario/99",
  "timestamp": "2026-11-23T15:30:00"
}
```

---

## Configuración de CORS

| Parámetro | Valor |
|-----------|-------|
| Origins | `*` (todos los orígenes) |
| Methods | `GET`, `DELETE`, `OPTIONS` |
| Headers | `*` (todos los headers) |
| Max Age  | `3600` segundos (1 hora) |

---

## Conexión a Base de Datos MySQL

| Parámetro | Valor |
|-----------|-------|
| Host | `localhost` |
| Puerto | `3306` |
| Base de datos | `inventario_db` |
| Usuario | `root` |
| Contraseña | *(vacía)* |
| Driver | `com.mysql.cj.jdbc.Driver` |
| Pool | HikariCP (max 10 conexiones) |

**Script SQL para crear la tabla:**
```sql
CREATE DATABASE IF NOT EXISTS inventario_db;
USE inventario_db;

CREATE TABLE IF NOT EXISTS inventario (
    id_inventario INT AUTO_INCREMENT PRIMARY KEY,
    id_producto   INT NOT NULL,
    cantidad      DOUBLE NOT NULL DEFAULT 0,
    stock_minimo  INT NOT NULL DEFAULT 0,
    stock_maximo  INT NOT NULL DEFAULT 0
);
```

---

## Sistema de Logs

| Nivel | Uso |
|-------|-----|
| `DEBUG` | Consultas SQL, parámetros de paginación |
| `INFO` | Operaciones exitosas |
| `WARN` | Validaciones fallidas, registros no encontrados |
| `ERROR` | Errores de BD, excepciones con stack trace |

**Archivos de log:**
- `logs/inventario-api.log` — log activo
- `logs/inventario-api-yyyy-MM-dd.i.log` — logs archivados por fecha
- Rotación: **10 MB** por archivo · **30 días** de historial · **100 MB** total

---

## Dependencias Principales (pom.xml)

| Dependencia | Uso |
|-------------|-----|
| `spring-boot-starter-webmvc` | Motor MVC para API REST |
| `spring-boot-starter-jdbc` | JdbcTemplate para acceso JDBC |
| `mysql-connector-j` | Driver JDBC para MySQL |

---

## Cómo Ejecutar el Proyecto

```bash
# 1. Crear la base de datos en MySQL
mysql -u root -e "CREATE DATABASE IF NOT EXISTS inventario_db;"

# 2. Ejecutar el proyecto
./mvnw spring-boot:run

# 3. Probar endpoints
curl "http://localhost:8080/api/v1/inventario?page=0&size=10"
curl "http://localhost:8080/api/v1/inventario/1"
curl -X DELETE "http://localhost:8080/api/v1/inventario/1"
```

---

*Generado automáticamente — Proyecto API Inventario · 2026*
