# Manual de Desarrollo: Aplicación Web Portafolio con Frontend + Backend

## 📋 Índice

1. [Introducción y arquitectura general](#1-introducción-y-arquitectura-general)
2. [Requisitos previos](#2-requisitos-previos)
3. [Fase 1: Planificación y diseño](#3-fase-1-planificación-y-diseño)
4. [Fase 2: Configuración del proyecto backend (Spring Boot)](#4-fase-2-configuración-del-proyecto-backend-spring-boot)
5. [Fase 3: Desarrollo del modelo de datos (JPA)](#5-fase-3-desarrollo-del-modelo-de-datos-jpa)
6. [Fase 4: Implementación del controlador REST](#6-fase-4-implementación-del-controlador-rest)
7. [Fase 5: Configuración de seguridad y CORS](#7-fase-5-configuración-de-seguridad-y-cors)
8. [Fase 6: Desarrollo del frontend (HTML/CSS/JavaScript)](#8-fase-6-desarrollo-del-frontend-htmlcssjavascript)
9. [Fase 7: Integración frontend-backend](#9-fase-7-integración-frontend-backend)
10. [Fase 8: Pruebas y validación](#10-fase-8-pruebas-y-validación)
11. [Fase 9: Despliegue](#11-fase-9-despliegue)
12. [Buenas prácticas y recomendaciones](#12-buenas-prácticas-y-recomendaciones)
13. [Troubleshooting común](#13-troubleshooting-común)
14. [Referencias y recursos](#14-referencias-y-recursos)

---

## 1. Introducción y arquitectura general

### 1.1 Descripción del proyecto

Este proyecto es una aplicación web de portafolio personal que consta de:

- **Frontend**: Página web estática HTML5/CSS3/JavaScript vanilla
- **Backend**: API REST desarrollada con Spring Boot 3 y Java 17
- **Base de datos**: H2 en memoria (desarrollo) / PostgreSQL o MySQL (producción)
- **Funcionalidad principal**: Formulario de contacto con persistencia de mensajes

### 1.2 Arquitectura del sistema

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENTE                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │    index.html (Frontend estático)                    │   │
│  │    - HTML5 estructura                                │   │
│  │    - CSS3 estilos                                    │   │
│  │    - JavaScript vanilla (fetch API)                  │   │
│  └──────────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP/HTTPS
                         │ POST /api/contact
                         │ JSON
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    SERVIDOR (Backend)                       │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Spring Boot 3 Application                           │   │
│  │  ┌────────────────────────────────────────────────┐  │   │
│  │  │  ContactController (@RestController)           │  │   │
│  │  │  - Validación de entrada                       │  │   │
│  │  │  - Rate limiting por IP                        │  │   │
│  │  │  - Manejo de CORS                              │  │   │
│  │  └─────────────────┬──────────────────────────────┘  │   │
│  │                    │                                  │   │
│  │  ┌─────────────────▼──────────────────────────────┐  │   │
│  │  │  ContactMessageRepository (JPA)                │  │   │
│  │  │  - CRUD operations                             │  │   │
│  │  └─────────────────┬──────────────────────────────┘  │   │
│  └────────────────────┼──────────────────────────────────┘   │
│                       │                                      │
│  ┌────────────────────▼──────────────────────────────────┐   │
│  │  Base de Datos (H2 / PostgreSQL / MySQL)            │   │
│  │  - Tabla: contact_messages                           │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 1.3 Stack tecnológico

#### Backend
- **Java**: 17 (LTS)
- **Spring Boot**: 3.2.3
- **Spring Data JPA**: Para persistencia
- **Spring Validation**: Para validación de datos
- **H2 Database**: Base de datos en memoria para desarrollo
- **Maven**: Gestión de dependencias y build

#### Frontend
- **HTML5**: Estructura semántica
- **CSS3**: Estilos modernos con variables CSS, Grid y Flexbox
- **JavaScript ES6+**: Fetch API para llamadas AJAX

---

## 2. Requisitos previos

### 2.1 Software necesario

1. **JDK 17 o superior**
   ```bash
   java -version
   # Debe mostrar: java version "17" o superior
   ```

2. **Maven 3.6+**
   ```bash
   mvn -version
   # Debe mostrar: Apache Maven 3.6.x o superior
   ```

3. **Editor de código**
   - IntelliJ IDEA (recomendado para Java)
   - VS Code
   - Eclipse
   - Cualquier editor con soporte para Java y Spring Boot

4. **Navegador web moderno**
   - Chrome, Firefox, Edge o Safari actualizados

5. **Git** (opcional pero recomendado)
   ```bash
   git --version
   ```

### 2.2 Conocimientos previos recomendados

- **Java básico**: Clases, objetos, herencia, interfaces
- **Spring Boot fundamentals**: Anotaciones, inyección de dependencias, autoconfiguration
- **JPA/Hibernate básico**: Entidades, repositorios, consultas
- **HTTP y REST**: Verbos HTTP, códigos de estado, formato JSON
- **HTML/CSS básico**: Estructura, selectores, box model
- **JavaScript**: Eventos, async/await, fetch API

---

## 3. Fase 1: Planificación y diseño

### 3.1 Definición de requisitos funcionales

1. **Página de portafolio estática** con:
   - Sección de presentación personal
   - Resumen de experiencia y formación
   - Listado de proyectos
   - Formulario de contacto funcional

2. **API REST para el formulario de contacto** que:
   - Reciba datos del formulario (nombre, email, mensaje)
   - Valide los datos de entrada
   - Implemente rate limiting básico
   - Persista los mensajes en base de datos
   - Devuelva respuestas en formato JSON

### 3.2 Requisitos no funcionales

- **Seguridad**: Validación de entrada, sanitización, rate limiting
- **Usabilidad**: Interfaz responsive, mensajes de error claros
- **Rendimiento**: Respuestas rápidas (< 200ms)
- **Mantenibilidad**: Código limpio, comentado, separación de concerns

### 3.3 Diseño de la base de datos

```sql
-- Tabla: contact_messages
CREATE TABLE contact_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(80) NOT NULL,
    email VARCHAR(120) NOT NULL,
    mensaje VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    client_ip VARCHAR(64)
);
```

### 3.4 Diseño del API REST

**Endpoint**: `POST /api/contact`

**Request Body**:
```json
{
  "nombre": "Juan Pérez",
  "email": "juan@ejemplo.com",
  "mensaje": "Hola, me gustaría contactar contigo..."
}
```

**Response Success (200 OK)**:
```json
{
  "status": "ok",
  "message": "Mensaje recibido correctamente."
}
```

**Response Error (400 Bad Request)**:
```json
{
  "status": "error",
  "message": "Datos no válidos en el formulario."
}
```

**Response Error (429 Too Many Requests)**:
```json
{
  "status": "error",
  "message": "Demasiadas solicitudes. Inténtalo de nuevo más tarde."
}
```

---

## 4. Fase 2: Configuración del proyecto backend (Spring Boot)

### 4.1 Estructura del proyecto Maven

```
portfolio-backend/
├── pom.xml                              # Configuración Maven
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── manuelbalbas/
│   │   │           └── portfolio/
│   │   │               ├── PortfolioApplication.java
│   │   │               └── contact/
│   │   │                   ├── ContactController.java
│   │   │                   ├── ContactRequest.java
│   │   │                   ├── ContactMessage.java
│   │   │                   └── ContactMessageRepository.java
│   │   └── resources/
│   │       └── application.properties    # Configuración Spring Boot
│   └── test/
│       └── java/
└── target/                              # Artefactos compilados (generado)
```

### 4.2 Creación del archivo pom.xml

El `pom.xml` es el archivo de configuración de Maven que gestiona las dependencias del proyecto:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <!-- Identificación del proyecto -->
  <groupId>com.manuelbalbas</groupId>
  <artifactId>portfolio-backend</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <name>portfolio-backend</name>

  <!-- Propiedades del proyecto -->
  <properties>
    <java.version>17</java.version>
    <spring.boot.version>3.2.3</spring.boot.version>
  </properties>

  <!-- Gestión de versiones de Spring Boot -->
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>${spring.boot.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <!-- Dependencias del proyecto -->
  <dependencies>
    <!-- Spring Boot Web: Para crear REST APIs -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Data JPA: Para persistencia con JPA/Hibernate -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- H2 Database: Base de datos en memoria para desarrollo -->
    <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
      <scope>runtime</scope>
    </dependency>
    
    <!-- Spring Validation: Para validación de entrada -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Logback: Para logging (ya incluido por Spring Boot) -->
    <dependency>
      <groupId>ch.qos.logback</groupId>
      <artifactId>logback-classic</artifactId>
    </dependency>
    
    <!-- Spring Boot Test: Para testing -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <!-- Configuración del build -->
  <build>
    <plugins>
      <!-- Plugin de Spring Boot para empaquetar la aplicación -->
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
      
      <!-- Plugin del compilador de Java -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <configuration>
          <source>${java.version}</source>
          <target>${java.version}</target>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

### 4.3 Archivo application.properties

El archivo `src/main/resources/application.properties` contiene la configuración de Spring Boot:

```properties
# Configuración del servidor
server.port=8080
server.error.include-message=never
server.error.include-binding-errors=never

# CORS para desarrollo: aceptar cualquier origen (incluido file://)
spring.web.cors.allowed-origins=*
spring.web.cors.allowed-methods=GET,POST,OPTIONS
spring.web.cors.allowed-headers=Content-Type,Accept

# Configuración de la base de datos H2 en memoria
spring.datasource.url=jdbc:h2:mem:portfolio-db;DB_CLOSE_DELAY=-1
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Configuración de JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# Consola H2 (para desarrollo)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

**Explicación de las propiedades clave**:

- `server.port=8080`: Puerto donde escucha la aplicación
- `spring.datasource.url`: URL de conexión a H2 en memoria
- `spring.jpa.hibernate.ddl-auto=update`: Hibernate crea/actualiza el esquema automáticamente
- `spring.h2.console.enabled=true`: Habilita consola web de H2 en http://localhost:8080/h2-console

### 4.4 Clase principal: PortfolioApplication.java

Esta es la clase que arranca la aplicación Spring Boot:

```java
package com.manuelbalbas.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación Spring Boot del portafolio.
 * 
 * @SpringBootApplication es una anotación compuesta que incluye:
 * - @Configuration: Marca la clase como fuente de beans
 * - @EnableAutoConfiguration: Activa la autoconfiguración de Spring Boot
 * - @ComponentScan: Escanea el paquete actual y subpaquetes en busca de componentes
 */
@SpringBootApplication
public class PortfolioApplication {

    /**
     * Arranca el contexto de Spring Boot y el servidor web embebido (Tomcat).
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(PortfolioApplication.class, args);
    }
}
```

### 4.5 Compilación inicial

Una vez creados los archivos básicos, compilar el proyecto:

```bash
# En la raíz del proyecto (donde está pom.xml)
mvn clean compile

# Si quieres crear el JAR ejecutable:
mvn clean package
```

---

## 5. Fase 3: Desarrollo del modelo de datos (JPA)

### 5.1 Entidad: ContactMessage.java

Esta clase representa un mensaje de contacto en la base de datos:

```java
package com.manuelbalbas.portfolio.contact;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Entidad JPA que representa un mensaje de contacto.
 * 
 * Anotaciones JPA principales:
 * - @Entity: Marca la clase como una entidad JPA
 * - @Table: Especifica el nombre de la tabla en la BD
 * - @Id: Marca el campo como clave primaria
 * - @GeneratedValue: Indica que el ID se genera automáticamente
 * - @Column: Configura restricciones de columna (nullable, length, etc.)
 */
@Entity
@Table(name = "contact_messages")
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(nullable = false, length = 2000)
    private String mensaje;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(length = 64)
    private String clientIp;

    /**
     * Constructor protegido sin parámetros requerido por JPA.
     * No debe usarse directamente en el código.
     */
    protected ContactMessage() {
    }

    /**
     * Constructor público para crear instancias desde el código de negocio.
     *
     * @param nombre   nombre de la persona que contacta
     * @param email    email de contacto
     * @param mensaje  contenido del mensaje
     * @param clientIp IP resuelta del cliente
     */
    public ContactMessage(String nombre, String email, String mensaje, String clientIp) {
        this.nombre = nombre;
        this.email = email;
        this.mensaje = mensaje;
        this.clientIp = clientIp;
        this.createdAt = Instant.now(); // Marca temporal automática
    }

    // Getters (no setters para inmutabilidad)
    
    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getClientIp() {
        return clientIp;
    }
}
```

**Decisiones de diseño**:

1. **Inmutabilidad**: Solo getters, no setters → una vez creado, el mensaje no se modifica
2. **Instant para fechas**: `java.time.Instant` es más preciso y moderno que `java.util.Date`
3. **Longitudes específicas**: Restricciones de longitud coherentes con la validación del frontend
4. **clientIp opcional**: Puede ser null si no se puede determinar

### 5.2 DTO de entrada: ContactRequest.java

Este DTO (Data Transfer Object) se usa para recibir y validar los datos del formulario:

```java
package com.manuelbalbas.portfolio.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para el endpoint de contacto.
 * 
 * Separa la capa de presentación (API) de la capa de persistencia (entidad).
 * Las anotaciones de validación se procesan automáticamente por Spring Validation.
 */
public class ContactRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre no puede superar 80 caracteres")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 120, message = "El email no puede superar 120 caracteres")
    private String email;

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 2000, message = "El mensaje no puede superar 2000 caracteres")
    private String mensaje;

    // Getters y setters

    public String getNombre() {
        return nombre;
    }

    /**
     * Aplica trim() para eliminar espacios en blanco al inicio/final.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre != null ? nombre.trim() : null;
    }

    public String getEmail() {
        return email;
    }

    /**
     * Normaliza el email eliminando espacios sobrantes.
     */
    public void setEmail(String email) {
        this.email = email != null ? email.trim() : null;
    }

    public String getMensaje() {
        return mensaje;
    }

    /**
     * El mensaje se deja con trim() pero sin modificar saltos de línea.
     */
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje != null ? mensaje.trim() : null;
    }
}
```

**Anotaciones de validación**:

- `@NotBlank`: Campo no puede ser null, vacío o solo espacios en blanco
- `@Size`: Longitud mínima/máxima del campo
- `@Email`: Valida que el campo tenga formato de email válido

### 5.3 Repositorio: ContactMessageRepository.java

Interface que extiende `JpaRepository` para operaciones CRUD automáticas:

```java
package com.manuelbalbas.portfolio.contact;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para la entidad ContactMessage.
 * 
 * Spring Data JPA proporciona automáticamente implementaciones para:
 * - save(entity): Guardar o actualizar
 * - findById(id): Buscar por ID
 * - findAll(): Listar todos
 * - delete(entity): Eliminar
 * - Y muchos más...
 * 
 * No necesitamos escribir código de implementación.
 */
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    // Por ahora no necesitamos métodos personalizados adicionales
    // Pero podríamos agregar, por ejemplo:
    // List<ContactMessage> findByEmail(String email);
    // List<ContactMessage> findByCreatedAtBetween(Instant start, Instant end);
}
```

**Ventajas de JpaRepository**:

1. **CRUD automático**: No escribimos SQL ni implementaciones
2. **Query methods**: Podemos definir métodos siguiendo convenciones de nombres
3. **Paginación**: Incluye soporte para `Pageable` y `Page<T>`
4. **Sorting**: Incluye soporte para ordenamiento con `Sort`

---

## 6. Fase 4: Implementación del controlador REST

### 6.1 Controlador: ContactController.java

Este es el componente que expone el endpoint REST:

```java
package com.manuelbalbas.portfolio.contact;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controlador REST que expone el endpoint de contacto.
 * 
 * Anotaciones principales:
 * - @RestController: Combina @Controller + @ResponseBody
 * - @RequestMapping: Define el path base (/api/contact)
 * - @CrossOrigin: Configura CORS para permitir peticiones desde otros orígenes
 */
@RestController
@RequestMapping("/api/contact")
@CrossOrigin(
    origins = {"*", "null"}, // Permite todos los orígenes (desarrollo)
    allowedHeaders = {"Content-Type", "Accept"},
    methods = {org.springframework.web.bind.annotation.RequestMethod.POST, 
               org.springframework.web.bind.annotation.RequestMethod.OPTIONS}
)
public class ContactController {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    // ============================================
    // RATE LIMITING SIMPLE EN MEMORIA
    // ============================================
    // En producción, usar Redis o similar para rate limiting distribuido
    
    private static final int MAX_REQUESTS = 5;           // Máximo 5 peticiones
    private static final long WINDOW_MILLIS = 5 * 60 * 1000L; // En 5 minutos

    // Mapa: IP -> Cola de timestamps de peticiones
    private final Map<String, Deque<Long>> requestsPerIp = new ConcurrentHashMap<>();
    
    private final ContactMessageRepository repository;

    /**
     * Constructor: Spring inyecta automáticamente el repositorio.
     */
    public ContactController(ContactMessageRepository repository) {
        this.repository = repository;
    }

    /**
     * Endpoint POST /api/contact
     * 
     * @param request Objeto validado con los datos del formulario
     * @param bindingResult Resultado de la validación automática
     * @param httpRequest Request HTTP para obtener la IP del cliente
     * @return ResponseEntity con el resultado de la operación
     */
    @PostMapping
    public ResponseEntity<?> handleContact(
            @Valid @RequestBody ContactRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest
    ) {
        // 1. VALIDACIÓN DE ENTRADA
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Datos no válidos en el formulario."
            ));
        }

        // 2. RATE LIMITING POR IP
        String clientIp = resolveClientIp(httpRequest);
        if (!isAllowed(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "status", "error",
                "message", "Demasiadas solicitudes. Inténtalo de nuevo más tarde."
            ));
        }

        // 3. PERSISTENCIA EN BASE DE DATOS
        ContactMessage entity = new ContactMessage(
            request.getNombre(),
            request.getEmail(),
            request.getMensaje(),
            clientIp
        );
        repository.save(entity);

        // 4. LOGGING (NO incluir datos sensibles sin sanitización)
        log.info(
            "Nuevo mensaje de contacto desde IP {}: nombre='{}', email='{}', longitudMensaje={}",
            clientIp,
            safeLogValue(request.getNombre(), 80),
            safeLogValue(request.getEmail(), 120),
            request.getMensaje() != null ? request.getMensaje().length() : 0
        );

        // 5. RESPUESTA EXITOSA
        return ResponseEntity.ok(Map.of(
            "status", "ok",
            "message", "Mensaje recibido correctamente."
        ));
    }

    /**
     * Resuelve la IP real del cliente, considerando proxies y load balancers.
     * 
     * @param request HttpServletRequest
     * @return IP del cliente
     */
    private String resolveClientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isBlank()) {
            // X-Forwarded-For puede contener múltiples IPs: "client, proxy1, proxy2"
            return header.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Verifica si una IP ha excedido el límite de peticiones.
     * 
     * @param ip IP del cliente
     * @return true si está permitido, false si ha excedido el límite
     */
    private boolean isAllowed(String ip) {
        long now = Instant.now().toEpochMilli();
        Deque<Long> deque = requestsPerIp.computeIfAbsent(ip, k -> new ArrayDeque<>());

        synchronized (deque) {
            // Eliminar timestamps fuera de la ventana temporal
            while (!deque.isEmpty() && now - deque.peekFirst() > WINDOW_MILLIS) {
                deque.pollFirst();
            }
            
            // Verificar si se ha alcanzado el límite
            if (deque.size() >= MAX_REQUESTS) {
                return false;
            }
            
            // Registrar esta petición
            deque.addLast(now);
            return true;
        }
    }

    /**
     * Trunca valores largos para logging seguro.
     * 
     * @param value Valor original
     * @param maxLen Longitud máxima
     * @return Valor truncado si es necesario
     */
    private String safeLogValue(String value, int maxLen) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen) + "...";
    }
}
```

**Puntos clave del controlador**:

1. **@Valid**: Activa la validación automática del DTO
2. **BindingResult**: Contiene los errores de validación
3. **Rate limiting**: Implementación simple en memoria (en producción usar Redis)
4. **Resolución de IP**: Considera proxies con header `X-Forwarded-For`
5. **Logging seguro**: Trunca valores largos y no expone datos sensibles completos
6. **Respuestas JSON**: Usa `Map.of()` para crear respuestas JSON simples

### 6.2 Prueba del endpoint con curl

Antes de conectar el frontend, probar el endpoint directamente:

```bash
# Arrancar la aplicación
mvn spring-boot:run

# En otra terminal, enviar una petición de prueba
curl -X POST http://localhost:8080/api/contact \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Test User",
    "email": "test@example.com",
    "mensaje": "Este es un mensaje de prueba"
  }'

# Respuesta esperada:
# {"status":"ok","message":"Mensaje recibido correctamente."}
```

---

## 7. Fase 5: Configuración de seguridad y CORS

### 7.1 CORS (Cross-Origin Resource Sharing)

**Problema**: Por defecto, los navegadores bloquean peticiones AJAX desde orígenes diferentes (dominio, protocolo o puerto distintos).

**Solución**: Configurar CORS en el backend para permitir peticiones desde el frontend.

**Opciones de configuración CORS**:

#### Opción 1: Anotación @CrossOrigin en el controlador (usada en este proyecto)

```java
@CrossOrigin(
    origins = {"*", "null"}, // "*" permite todos los orígenes
    allowedHeaders = {"Content-Type", "Accept"},
    methods = {RequestMethod.POST, RequestMethod.OPTIONS}
)
@RestController
@RequestMapping("/api/contact")
public class ContactController {
    // ...
}
```

**Ventajas**: Simple, específico para cada controlador
**Desventajas**: Se repite en cada controlador

#### Opción 2: Configuración global de CORS

Crear una clase de configuración:

```java
package com.manuelbalbas.portfolio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .maxAge(3600);
    }
}
```

**Ventajas**: Configuración centralizada
**Desventajas**: Aplica a todos los endpoints bajo el patrón

### 7.2 Consideraciones de seguridad para producción

**En producción, NUNCA usar `origins = "*"`**. En su lugar:

```java
@CrossOrigin(
    origins = {"https://midominio.com", "https://www.midominio.com"},
    allowedHeaders = {"Content-Type", "Accept"},
    methods = {RequestMethod.POST, RequestMethod.OPTIONS}
)
```

**Otras medidas de seguridad importantes**:

1. **HTTPS**: Siempre usar HTTPS en producción
2. **Validación robusta**: Validar todos los inputs
3. **Rate limiting distribuido**: Usar Redis o similar
4. **Sanitización**: Escapar HTML si se muestra contenido de usuarios
5. **Content Security Policy**: Configurar headers de seguridad
6. **Actualizar dependencias**: Mantener Spring Boot y librerías actualizadas

### 7.3 Configuración adicional en application.properties

```properties
# Producción: No exponer mensajes de error detallados
server.error.include-message=never
server.error.include-binding-errors=never
server.error.include-stacktrace=never
server.error.include-exception=false

# Configurar logging apropiadamente
logging.level.root=INFO
logging.level.com.manuelbalbas.portfolio=INFO
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

---

## 8. Fase 6: Desarrollo del frontend (HTML/CSS/JavaScript)

### 8.1 Estructura del HTML

El archivo `index.html` contiene:

1. **Header con navegación**: Logo y enlaces a secciones
2. **Sección Hero**: Presentación principal
3. **Sección Sobre mí**: Información personal y profesional
4. **Sección CV**: Experiencia y formación
5. **Sección Proyectos**: Tarjetas de proyectos con enlaces
6. **Sección Contacto**: Formulario funcional
7. **Footer**: Copyright e información adicional

**Estructura básica**:

```html
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Portafolio de Manuel María Balbás Naveira</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <header class="header">
        <!-- Navegación -->
    </header>

    <main>
        <section id="inicio" class="hero">
            <!-- Presentación principal -->
        </section>

        <section id="sobre-mi" class="section">
            <!-- Información personal -->
        </section>

        <section id="cv" class="section section--alt">
            <!-- Currículum -->
        </section>

        <section id="proyectos" class="section">
            <!-- Proyectos -->
        </section>

        <section id="contacto" class="section section--alt">
            <!-- Formulario de contacto -->
            <form id="contact-form" class="contact-form">
                <!-- Campos del formulario -->
            </form>
        </section>
    </main>

    <footer class="footer">
        <!-- Pie de página -->
    </footer>

    <script>
        // JavaScript para el formulario
    </script>
</body>
</html>
```

### 8.2 Formulario de contacto

**HTML del formulario**:

```html
<form id="contact-form" class="contact-form">
    <div class="form-row">
        <div class="form-field">
            <label for="nombre">Nombre</label>
            <input 
                id="nombre" 
                name="nombre" 
                type="text" 
                placeholder="Tu nombre" 
                required 
            />
        </div>
        <div class="form-field">
            <label for="email">Email</label>
            <input 
                id="email" 
                name="email" 
                type="email" 
                placeholder="tucorreo@ejemplo.com" 
                required 
            />
        </div>
    </div>
    <div class="form-field">
        <label for="mensaje">Mensaje</label>
        <textarea 
            id="mensaje" 
            name="mensaje" 
            rows="4" 
            placeholder="Escribe tu mensaje aquí" 
            required
        ></textarea>
    </div>
    <button type="submit" class="btn btn--primary">
        Enviar
    </button>
</form>
```

**Atributos HTML importantes**:

- `id`: Identificador único para seleccionar con JavaScript
- `name`: Nombre del campo (debe coincidir con el DTO del backend)
- `required`: Validación HTML5 nativa del navegador
- `type="email"`: Validación de formato de email por el navegador

### 8.3 CSS moderno con variables CSS

Extracto de `styles.css`:

```css
:root {
    /* Paleta de colores */
    --color-primary: #4f46e5;
    --color-primary-hover: #4338ca;
    --color-background: #0f172a;
    --color-surface: #1e293b;
    --color-text: #e2e8f0;
    --color-text-muted: #94a3b8;
    
    /* Espaciado */
    --spacing-xs: 0.5rem;
    --spacing-sm: 1rem;
    --spacing-md: 1.5rem;
    --spacing-lg: 2rem;
    --spacing-xl: 3rem;
    
    /* Tipografía */
    --font-base: 16px;
    --font-family: system-ui, -apple-system, sans-serif;
}

/* Reset básico */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    font-family: var(--font-family);
    font-size: var(--font-base);
    color: var(--color-text);
    background-color: var(--color-background);
    line-height: 1.6;
}

/* Formulario de contacto */
.contact-form {
    max-width: 600px;
    margin: 0 auto;
}

.form-field {
    margin-bottom: var(--spacing-md);
}

.form-field label {
    display: block;
    margin-bottom: var(--spacing-xs);
    font-weight: 600;
    color: var(--color-text);
}

.form-field input,
.form-field textarea {
    width: 100%;
    padding: var(--spacing-sm);
    background-color: var(--color-surface);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 4px;
    color: var(--color-text);
    font-family: inherit;
    font-size: 1rem;
}

.form-field input:focus,
.form-field textarea:focus {
    outline: none;
    border-color: var(--color-primary);
}

/* Botón primario */
.btn--primary {
    background-color: var(--color-primary);
    color: white;
    padding: var(--spacing-sm) var(--spacing-lg);
    border: none;
    border-radius: 4px;
    font-size: 1rem;
    cursor: pointer;
    transition: background-color 0.2s;
}

.btn--primary:hover {
    background-color: var(--color-primary-hover);
}
```

**Ventajas de usar variables CSS**:

1. **Mantenibilidad**: Cambiar un color en un solo lugar
2. **Consistencia**: Mismo espaciado y colores en toda la app
3. **Tematización**: Fácil implementar modo claro/oscuro

### 8.4 JavaScript para el formulario

**Código completo del manejo del formulario**:

```javascript
// Actualizar año del copyright dinámicamente
document.getElementById("year").textContent = new Date().getFullYear();

// Lógica del formulario de contacto
const form = document.getElementById("contact-form");

form.addEventListener("submit", async (event) => {
    // Prevenir el envío tradicional del formulario
    event.preventDefault();

    // 1. OBTENER VALORES DE LOS CAMPOS
    const nombre = document.getElementById("nombre").value.trim();
    const email = document.getElementById("email").value.trim();
    const mensaje = document.getElementById("mensaje").value.trim();

    // 2. VALIDACIÓN BÁSICA EN CLIENTE
    if (!nombre || !email || !mensaje) {
        alert("Por favor, completa todos los campos.");
        return;
    }

    // 3. ENVIAR DATOS AL BACKEND CON FETCH API
    try {
        const response = await fetch("http://localhost:8080/api/contact", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ nombre, email, mensaje }),
        });

        // 4. PROCESAR RESPUESTA
        // Intentar parsear JSON (si falla, data será null)
        const data = await response.json().catch(() => null);

        if (!response.ok) {
            // Error del servidor (4xx, 5xx)
            alert(
                (data && data.message) ||
                "Ha ocurrido un error al enviar el mensaje. Inténtalo de nuevo más tarde."
            );
            return;
        }

        // 5. ÉXITO
        alert((data && data.message) || "Mensaje enviado correctamente.");
        form.reset(); // Limpiar formulario

    } catch (error) {
        // 6. ERROR DE RED (servidor caído, sin internet, etc.)
        console.error("Error de red:", error);
        alert("No se ha podido contactar con el servidor. ¿Está el backend arrancado?");
    }
});
```

**Flujo del código JavaScript**:

1. **Prevenir envío tradicional**: `event.preventDefault()`
2. **Obtener valores**: `.value.trim()` elimina espacios sobrantes
3. **Validación cliente**: Verificar que no estén vacíos
4. **Fetch API**: Llamada asíncrona al backend con `async/await`
5. **Headers**: `Content-Type: application/json` indica que enviamos JSON
6. **Body**: `JSON.stringify()` convierte el objeto JS a JSON
7. **Manejo de respuesta**: Diferenciar entre éxito, error del servidor y error de red
8. **Feedback al usuario**: `alert()` (en producción usar notificaciones más elegantes)

**Mejoras opcionales para producción**:

1. **Loading state**: Deshabilitar botón durante el envío
2. **Notificaciones elegantes**: Usar librerías como Toastify o SweetAlert2
3. **Validación avanzada**: Regex para email, longitud mínima, etc.
4. **Reintentos automáticos**: En caso de errores de red temporales
5. **CAPTCHA**: Para prevenir spam (Google reCAPTCHA, hCaptcha)

---

## 9. Fase 7: Integración frontend-backend

### 9.1 Flujo completo de una petición

```
Usuario rellena formulario
       ↓
Hace clic en "Enviar"
       ↓
JavaScript: event.preventDefault()
       ↓
JavaScript: Recopila valores de los campos
       ↓
JavaScript: Validación cliente (campos no vacíos)
       ↓
JavaScript: fetch("http://localhost:8080/api/contact", {...})
       ↓
═══════════════════════════════════════════════════════
       ↓ HTTP Request (JSON)
Backend: Recibe POST /api/contact
       ↓
Backend: CORS filter verifica origen
       ↓
Backend: @Valid valida el DTO ContactRequest
       ↓
Backend: Rate limiting verifica IP
       ↓
Backend: Crea entidad ContactMessage
       ↓
Backend: repository.save(entity)
       ↓
Backend: Hibernate ejecuta INSERT en BD
       ↓
Backend: Devuelve ResponseEntity con JSON
       ↓ HTTP Response (JSON)
═══════════════════════════════════════════════════════
       ↓
JavaScript: Recibe response
       ↓
JavaScript: Parsea JSON
       ↓
JavaScript: Verifica response.ok
       ↓
JavaScript: Muestra mensaje al usuario
       ↓
JavaScript: form.reset() limpia campos
       ↓
Usuario ve confirmación
```

### 9.2 Testing manual de la integración

**Pasos para probar**:

1. **Arrancar el backend**:
   ```bash
   cd portfolio-backend
   mvn spring-boot:run
   ```
   
   Esperar a ver: `Started PortfolioApplication in X seconds`

2. **Abrir el frontend**:
   - Opción 1: Abrir `index.html` directamente en el navegador (file://)
   - Opción 2: Usar un servidor HTTP local:
     ```bash
     # Con Python 3
     python -m http.server 3000
     
     # Con Node.js (si tienes http-server instalado)
     npx http-server -p 3000
     ```
     Luego abrir: http://localhost:3000

3. **Abrir las herramientas de desarrollador**:
   - Chrome/Edge: F12
   - Firefox: F12
   - Ir a la pestaña "Network" o "Red"

4. **Rellenar el formulario y enviar**:
   - Completar los 3 campos
   - Clic en "Enviar"
   - Observar en la pestaña Network:
     - Request Method: POST
     - Status Code: 200 OK
     - Request Payload: JSON con los datos
     - Response: JSON con status "ok"

5. **Verificar en la consola del backend**:
   ```
   INFO - Nuevo mensaje de contacto desde IP 0:0:0:0:0:0:0:1: nombre='Test', ...
   ```

6. **Verificar en la base de datos** (opcional):
   - Acceder a: http://localhost:8080/h2-console
   - JDBC URL: `jdbc:h2:mem:portfolio-db`
   - User: `sa`
   - Password: (vacío)
   - Ejecutar: `SELECT * FROM contact_messages;`

### 9.3 Casos de prueba importantes

| Caso de prueba | Input | Resultado esperado |
|----------------|-------|-------------------|
| Envío exitoso | Todos los campos válidos | Status 200, mensaje de éxito |
| Campo vacío | Nombre vacío | Alert "completa todos los campos" |
| Email inválido | email: "asdf" | Status 400, mensaje de error |
| Rate limiting | 6 envíos en < 5 min | Status 429, mensaje rate limit |
| Backend caído | Backend no está corriendo | Alert "no se ha podido contactar" |
| Mensaje muy largo | mensaje: 2001 caracteres | Status 400, mensaje de error |

---

## 10. Fase 8: Pruebas y validación

### 10.1 Pruebas unitarias con JUnit (opcional pero recomendado)

Crear archivo: `src/test/java/com/manuelbalbas/portfolio/contact/ContactControllerTest.java`

```java
package com.manuelbalbas.portfolio.contact;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testContactFormSuccess() throws Exception {
        String json = """
            {
                "nombre": "Test User",
                "email": "test@example.com",
                "mensaje": "Test message"
            }
            """;

        mockMvc.perform(post("/api/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void testContactFormInvalidEmail() throws Exception {
        String json = """
            {
                "nombre": "Test User",
                "email": "invalid-email",
                "mensaje": "Test message"
            }
            """;

        mockMvc.perform(post("/api/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }
}
```

**Ejecutar tests**:
```bash
mvn test
```

### 10.2 Pruebas de integración con Postman

**Colección Postman de ejemplo**:

1. **Request**: POST Contact - Success
   - URL: `http://localhost:8080/api/contact`
   - Method: POST
   - Body (raw JSON):
     ```json
     {
       "nombre": "Juan Pérez",
       "email": "juan@example.com",
       "mensaje": "Hola, me gustaría contactar contigo."
     }
     ```
   - Expected: Status 200, response contains "status": "ok"

2. **Request**: POST Contact - Invalid Email
   - Body: `{"nombre": "Test", "email": "invalid", "mensaje": "Test"}`
   - Expected: Status 400

3. **Request**: POST Contact - Missing Fields
   - Body: `{"nombre": "Test"}`
   - Expected: Status 400

### 10.3 Checklist de validación

Antes de considerar el proyecto completo, verificar:

- [ ] Backend compila sin errores: `mvn clean compile`
- [ ] Backend arranca correctamente: `mvn spring-boot:run`
- [ ] Endpoint responde correctamente con datos válidos
- [ ] Endpoint rechaza datos inválidos (email, campos vacíos)
- [ ] Rate limiting funciona (6° petición es rechazada)
- [ ] CORS permite peticiones desde file:// o localhost
- [ ] Frontend HTML valida correctamente (https://validator.w3.org/)
- [ ] Frontend CSS no tiene errores
- [ ] JavaScript maneja correctamente éxitos y errores
- [ ] Formulario se resetea después de envío exitoso
- [ ] Mensajes se persisten en la base de datos
- [ ] Console de H2 es accesible y muestra los datos
- [ ] No hay errores en la consola del navegador
- [ ] No hay warnings de seguridad en la consola del backend

---

## 11. Fase 9: Despliegue

### 11.1 Preparación para producción

#### Backend: Crear JAR ejecutable

```bash
# Compilar y empaquetar
mvn clean package

# El JAR se genera en:
# target/portfolio-backend-0.0.1-SNAPSHOT.jar

# Probar el JAR localmente
java -jar target/portfolio-backend-0.0.1-SNAPSHOT.jar
```

#### Backend: Configuración para producción

Crear `application-prod.properties`:

```properties
server.port=8080

# Base de datos PostgreSQL (ejemplo)
spring.datasource.url=jdbc:postgresql://localhost:5432/portfolio_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# CORS: Solo dominios específicos
spring.web.cors.allowed-origins=https://midominio.com,https://www.midominio.com

# Seguridad
server.error.include-message=never
server.error.include-binding-errors=never
server.error.include-stacktrace=never

# Logging
logging.level.root=WARN
logging.level.com.manuelbalbas.portfolio=INFO
```

Ejecutar con perfil de producción:
```bash
java -jar -Dspring.profiles.active=prod target/portfolio-backend-0.0.1-SNAPSHOT.jar
```

### 11.2 Opciones de despliegue del backend

#### Opción 1: Heroku

```bash
# 1. Instalar Heroku CLI
# 2. Login
heroku login

# 3. Crear app
heroku create mi-portfolio-backend

# 4. Configurar buildpack de Java
heroku buildpacks:set heroku/java

# 5. Agregar base de datos PostgreSQL
heroku addons:create heroku-postgresql:mini

# 6. Deploy
git push heroku main

# 7. Verificar logs
heroku logs --tail
```

#### Opción 2: Railway

1. Conectar repositorio de GitHub en https://railway.app
2. Railway detecta automáticamente el proyecto Spring Boot
3. Configurar variables de entorno
4. Deploy automático con cada push

#### Opción 3: VPS (DigitalOcean, Linode, etc.)

```bash
# En el servidor (Ubuntu/Debian)
# 1. Instalar Java
sudo apt update
sudo apt install openjdk-17-jre

# 2. Copiar JAR al servidor
scp target/portfolio-backend-0.0.1-SNAPSHOT.jar usuario@servidor:/opt/portfolio/

# 3. Crear servicio systemd
sudo nano /etc/systemd/system/portfolio-backend.service
```

Contenido del servicio:
```ini
[Unit]
Description=Portfolio Backend
After=syslog.target

[Service]
User=portfolio
ExecStart=/usr/bin/java -jar /opt/portfolio/portfolio-backend-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
```

```bash
# 4. Habilitar y arrancar
sudo systemctl enable portfolio-backend
sudo systemctl start portfolio-backend
sudo systemctl status portfolio-backend
```

### 11.3 Opciones de despliegue del frontend

#### Opción 1: GitHub Pages

1. Crear repositorio en GitHub
2. Push de `index.html` y `styles.css`
3. Settings → Pages → Source: main branch
4. Actualizar URL del backend en el JavaScript

#### Opción 2: Netlify

```bash
# 1. Instalar Netlify CLI
npm install -g netlify-cli

# 2. Deploy
netlify deploy --prod --dir=.
```

#### Opción 3: Vercel

1. Instalar Vercel CLI: `npm install -g vercel`
2. En la carpeta del frontend: `vercel`
3. Seguir las instrucciones

### 11.4 Configuración de DNS y dominio

Una vez desplegado:

1. **Comprar dominio** (Namecheap, GoDaddy, etc.)
2. **Configurar DNS**:
   - A record: `@` → IP del servidor backend
   - CNAME: `www` → Dominio del frontend
3. **Actualizar CORS** en el backend con el dominio real
4. **Actualizar URL** del backend en el frontend JavaScript

### 11.5 HTTPS con Let's Encrypt (para VPS)

```bash
# 1. Instalar Certbot
sudo apt install certbot python3-certbot-nginx

# 2. Obtener certificado
sudo certbot --nginx -d midominio.com -d www.midominio.com

# 3. Renovación automática (Certbot crea un cron job automáticamente)
sudo certbot renew --dry-run
```

---

## 12. Buenas prácticas y recomendaciones

### 12.1 Código limpio (Clean Code)

1. **Nombres descriptivos**: `contactMessage` en lugar de `msg`
2. **Funciones pequeñas**: Cada función hace una cosa
3. **Comentarios útiles**: Explican el "por qué", no el "qué"
4. **DRY (Don't Repeat Yourself)**: No duplicar código
5. **SOLID principles**:
   - Single Responsibility: Cada clase tiene una responsabilidad
   - Open/Closed: Abierto a extensión, cerrado a modificación
   - Liskov Substitution: Las subclases deben ser sustituibles
   - Interface Segregation: Interfaces específicas
   - Dependency Inversion: Depender de abstracciones

### 12.2 Seguridad

1. **Validación de entrada**: Siempre validar en backend
2. **Sanitización**: Escapar HTML si se muestra contenido de usuarios
3. **Rate limiting**: Prevenir abuso
4. **HTTPS**: Obligatorio en producción
5. **Headers de seguridad**:
   ```properties
   # En application.properties
   server.servlet.session.cookie.http-only=true
   server.servlet.session.cookie.secure=true
   ```
6. **Actualizar dependencias**: Usar `mvn versions:display-dependency-updates`
7. **No exponer información sensible**: En logs ni respuestas de error

### 12.3 Rendimiento

1. **Índices en BD**: En columnas frecuentemente consultadas
2. **Paginación**: Para listados grandes
3. **Cache**: Redis para datos frecuentes
4. **Compresión**: Gzip para respuestas HTTP
5. **CDN**: Para archivos estáticos del frontend
6. **Minificación**: CSS y JavaScript minificados en producción

### 12.4 Mantenibilidad

1. **Documentación**: README.md actualizado
2. **Comentarios Javadoc**: Para clases y métodos públicos
3. **Tests**: Cobertura > 80% (ideal)
4. **Versionado semántico**: MAJOR.MINOR.PATCH
5. **Changelog**: Documentar cambios entre versiones
6. **CI/CD**: GitHub Actions, Jenkins, GitLab CI

### 12.5 Monitoreo y logging

```properties
# Logging estructurado
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# Niveles de log apropiados
logging.level.root=INFO
logging.level.com.manuelbalbas.portfolio=DEBUG
logging.level.org.springframework.web=INFO
logging.level.org.hibernate=WARN
```

**Herramientas de monitoreo recomendadas**:
- **Backend**: Spring Boot Actuator, Prometheus, Grafana
- **Frontend**: Google Analytics, Sentry (errores JavaScript)
- **Logs centralizados**: ELK Stack (Elasticsearch, Logstash, Kibana)

---

## 13. Troubleshooting común

### 13.1 Errores de CORS

**Síntoma**: Error en consola del navegador:
```
Access to fetch at 'http://localhost:8080/api/contact' from origin 'null' 
has been blocked by CORS policy
```

**Solución**:
1. Verificar que `@CrossOrigin` incluye el origen correcto
2. Para `file://`, asegurarse de incluir `"null"` en origins
3. Verificar que los headers permitidos incluyen `Content-Type`

### 13.2 Backend no arranca

**Síntoma**: Error al ejecutar `mvn spring-boot:run`

**Causas comunes**:
1. **Puerto ocupado**: Otro proceso usa el puerto 8080
   ```bash
   # Windows
   netstat -ano | findstr :8080
   taskkill /PID <PID> /F
   
   # Linux/Mac
   lsof -i :8080
   kill -9 <PID>
   ```

2. **Versión de Java incorrecta**:
   ```bash
   java -version  # Debe ser 17+
   ```

3. **Dependencias no descargadas**:
   ```bash
   mvn clean install -U
   ```

### 13.3 Formulario no envía datos

**Síntoma**: Al hacer clic en "Enviar", no pasa nada

**Diagnóstico**:
1. Abrir consola del navegador (F12)
2. Buscar errores JavaScript
3. Ir a pestaña Network, verificar si se envía la petición

**Causas comunes**:
1. **Backend no está corriendo**: Arrancar con `mvn spring-boot:run`
2. **URL incorrecta**: Verificar que sea `http://localhost:8080/api/contact`
3. **Error de JavaScript**: Revisar consola para syntax errors

### 13.4 Datos no se persisten

**Síntoma**: El endpoint responde OK pero los datos no aparecen en H2

**Diagnóstico**:
1. Acceder a http://localhost:8080/h2-console
2. JDBC URL: `jdbc:h2:mem:portfolio-db`
3. Ejecutar: `SELECT * FROM contact_messages;`

**Causas comunes**:
1. **Base de datos en memoria se resetea**: Normal en H2, los datos se pierden al reiniciar
2. **URL de H2 incorrecta**: Verificar en `application.properties`
3. **Tabla no se crea**: Verificar que `spring.jpa.hibernate.ddl-auto=update`

### 13.5 Rate limiting demasiado estricto

**Síntoma**: No puedo enviar más de 5 mensajes en 5 minutos

**Solución temporal para desarrollo**:
Modificar constantes en `ContactController.java`:
```java
private static final int MAX_REQUESTS = 100;  // Aumentar límite
private static final long WINDOW_MILLIS = 1 * 60 * 1000L;  // 1 minuto
```

### 13.6 Errores de validación

**Síntoma**: Siempre recibo "Datos no válidos en el formulario"

**Diagnóstico**:
1. Verificar logs del backend para detalles de validación
2. Verificar que los nombres de campos coincidan: `nombre`, `email`, `mensaje`
3. Verificar que `Content-Type: application/json`

**Ejemplo de log útil**:
```java
if (bindingResult.hasErrors()) {
    bindingResult.getAllErrors().forEach(error -> 
        log.error("Validation error: {}", error.getDefaultMessage())
    );
    // ...
}
```

---

## 14. Referencias y recursos

### 14.1 Documentación oficial

- **Spring Boot**: https://spring.io/projects/spring-boot
- **Spring Data JPA**: https://spring.io/projects/spring-data-jpa
- **Hibernate**: https://hibernate.org/orm/documentation/
- **Maven**: https://maven.apache.org/guides/
- **MDN Web Docs**: https://developer.mozilla.org/

### 14.2 Tutoriales recomendados

- **Spring Boot REST API Tutorial**: https://spring.io/guides/tutorials/rest/
- **Building a RESTful Web Service**: https://spring.io/guides/gs/rest-service/
- **Accessing Data with JPA**: https://spring.io/guides/gs/accessing-data-jpa/
- **JavaScript Fetch API**: https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API

### 14.3 Libros recomendados

- **"Clean Code" - Robert C. Martin**: Principios de código limpio
- **"Spring in Action" - Craig Walls**: Guía completa de Spring Framework
- **"Effective Java" - Joshua Bloch**: Mejores prácticas en Java
- **"You Don't Know JS" - Kyle Simpson**: JavaScript en profundidad

### 14.4 Herramientas útiles

- **Postman**: https://www.postman.com/ - Testing de APIs
- **H2 Console**: Interfaz web para H2 Database
- **IntelliJ IDEA**: https://www.jetbrains.com/idea/ - IDE para Java
- **VS Code**: https://code.visualstudio.com/ - Editor de código
- **Git**: https://git-scm.com/ - Control de versiones
- **DBeaver**: https://dbeaver.io/ - Cliente universal de bases de datos

### 14.5 Comunidades y soporte

- **Stack Overflow**: https://stackoverflow.com/questions/tagged/spring-boot
- **Spring Community**: https://spring.io/community
- **Reddit r/java**: https://www.reddit.com/r/java/
- **Reddit r/webdev**: https://www.reddit.com/r/webdev/

---

## Conclusión

Este manual cubre el ciclo completo de desarrollo de una aplicación web moderna con frontend estático y backend Spring Boot. Los conceptos y patrones aquí descritos son aplicables a proyectos de cualquier escala.

**Próximos pasos recomendados**:

1. **Añadir autenticación**: Spring Security con JWT
2. **Implementar paginación**: Para listado de mensajes
3. **Dashboard de administración**: Para ver mensajes recibidos
4. **Tests automatizados**: Aumentar cobertura de tests
5. **CI/CD**: Automatizar build y deployment
6. **Monitoreo**: Implementar métricas y alertas
7. **Internacionalización (i18n)**: Soporte multi-idioma
8. **PWA**: Convertir el frontend en Progressive Web App

**Recuerda**:
- Escribir código limpio y documentado
- Probar antes de desplegar
- Mantener las dependencias actualizadas
- Seguir las mejores prácticas de seguridad
- Iterar y mejorar continuamente

¡Éxito en tus desarrollos! 🚀

---

**Versión del manual**: 1.0
**Fecha**: Marzo 2026
**Autor**: Manuel María Balbás Naveira
**Licencia**: Este documento es de uso libre para fines educativos

