# Guía de Despliegue en DonWeb - Portfolio con Spring Boot

## 📋 Índice

1. [Identificar tu tipo de hosting](#1-identificar-tu-tipo-de-hosting)
2. [Escenario A: Hosting Compartido (Solo Frontend)](#2-escenario-a-hosting-compartido-solo-frontend)
3. [Escenario B: VPS/Cloud (Frontend + Backend completo)](#3-escenario-b-vpscloud-frontend--backend-completo)
4. [Alternativa: Backend en otro proveedor](#4-alternativa-backend-en-otro-proveedor)
5. [Configuración de dominio](#5-configuración-de-dominio)
6. [Troubleshooting](#6-troubleshooting)

---

## 1. Identificar tu tipo de hosting

### ¿Cómo saber qué tipo de hosting tienes?

1. **Accede a tu panel de DonWeb**: https://clientes.donweb.com/
2. **Mira las características de tu plan**:
   - Si ves **cPanel** → Hosting Compartido (solo PHP/MySQL)
   - Si ves **SSH access**, **root access** → VPS/Cloud (puedes instalar Java)
   - Si dice **Plesk** o **Windows** → Hosting Windows

### Verificación rápida por SSH

Si tienes acceso SSH, conéctate y ejecuta:

```bash
ssh usuario@tudominio.com

# Una vez conectado, verifica:
java -version
```

- **Si muestra versión Java 17+**: ✅ Puedes desplegar el backend
- **Si da error "command not found"**: ❌ No puedes ejecutar Java directamente

---

## 2. Escenario A: Hosting Compartido (Solo Frontend)

**⚠️ IMPORTANTE**: El hosting compartido de DonWeb **NO soporta aplicaciones Java/Spring Boot**.

En este caso, puedes desplegar solo el frontend estático, pero el formulario de contacto **NO funcionará** a menos que uses un backend externo.

### 2.1 Preparar archivos para subir

En tu computadora, necesitas solo estos archivos:

```
portfolio/
├── index.html
└── styles.css
```

### 2.2 Subir archivos por FTP

#### Opción A: Usar FileZilla (Recomendado)

1. **Descargar FileZilla**: https://filezilla-project.org/
2. **Obtener credenciales FTP desde tu panel DonWeb**:
   - Host: `ftp.tudominio.com` o IP del servidor
   - Usuario: Tu usuario FTP (ej: `usuario@tudominio.com`)
   - Contraseña: Tu contraseña FTP
   - Puerto: 21

3. **Conectar con FileZilla**:
   - Archivo → Gestor de sitios → Nuevo sitio
   - Protocolo: FTP
   - Cifrado: Usar FTP simple
   - Tipo de acceso: Normal
   - Introduce usuario y contraseña

4. **Subir archivos**:
   - En el panel derecho, navega a `public_html` o `www`
   - Arrastra `index.html` y `styles.css` desde el panel izquierdo
   - Espera a que se complete la transferencia

#### Opción B: Usar el administrador de archivos de cPanel

1. Accede a tu cPanel de DonWeb
2. Busca "Administrador de archivos" o "File Manager"
3. Navega a `public_html`
4. Haz clic en "Subir" o "Upload"
5. Selecciona `index.html` y `styles.css`
6. Espera a que se suban

### 2.3 Verificar el despliegue

Abre tu navegador y ve a: `https://tudominio.com`

Deberías ver tu portfolio funcionando.

### 2.4 Problema: El formulario no funcionará

Como el backend (Spring Boot) no está desplegado, el formulario mostrará un error:

```
"No se ha podido contactar con el servidor. ¿Está el backend arrancado?"
```

**Soluciones**:

1. **Desactivar el formulario temporalmente**: Elimina la sección de contacto del HTML
2. **Usar servicio externo**: Formspree, Netlify Forms, EmailJS
3. **Desplegar backend en otro lugar**: Railway, Render, Heroku (ver Escenario C)

### 2.5 Solución temporal: Usar Formspree

Formspree permite enviar emails sin backend propio:

1. **Regístrate en Formspree**: https://formspree.io/ (plan gratuito)
2. **Crea un formulario** y obtén tu endpoint
3. **Modifica el JavaScript del formulario**:

```javascript
// Reemplazar en index.html, línea ~640
const response = await fetch("https://formspree.io/f/TU_CODIGO_AQUI", {
    method: "POST",
    headers: {
        "Content-Type": "application/json",
    },
    body: JSON.stringify({ nombre, email, mensaje }),
});
```

---

## 3. Escenario B: VPS/Cloud (Frontend + Backend completo)

**✅ RECOMENDADO**: Si tienes un VPS o Cloud de DonWeb, puedes desplegar la aplicación completa.

### 3.1 Requisitos previos

- **VPS con Ubuntu/Debian**
- **Acceso SSH root o sudo**
- **Mínimo 1GB RAM**
- **Java 17 o superior instalado**

### 3.2 Conectar por SSH

```bash
ssh root@tu-ip-vps
# O con usuario no-root:
ssh usuario@tu-ip-vps
```

### 3.3 Instalar Java 17

```bash
# Actualizar sistema
sudo apt update && sudo apt upgrade -y

# Instalar Java 17
sudo apt install openjdk-17-jdk -y

# Verificar instalación
java -version
# Debe mostrar: openjdk version "17.x.x"
```

### 3.4 Instalar Maven

```bash
sudo apt install maven -y
mvn -version
```

### 3.5 Subir el proyecto al servidor

#### Opción A: Con Git (Recomendado)

1. **En tu computadora, sube tu proyecto a GitHub**:

```bash
cd C:\Users\mmbal\Documents\Prueba_Cursor

# Inicializar git si no lo has hecho
git init
git add .
git commit -m "Initial commit"

# Crear repositorio en GitHub y subir
git remote add origin https://github.com/tu-usuario/portfolio.git
git branch -M main
git push -u origin main
```

2. **En el servidor VPS, clonar el repositorio**:

```bash
cd /opt
sudo git clone https://github.com/tu-usuario/portfolio.git
cd portfolio
```

#### Opción B: Con SCP (Sin Git)

Desde tu computadora Windows (PowerShell):

```powershell
# Comprimir el proyecto
cd C:\Users\mmbal\Documents\Prueba_Cursor
tar -czf portfolio.tar.gz pom.xml src/ index.html styles.css

# Subir al servidor
scp portfolio.tar.gz usuario@tu-ip-vps:/opt/

# Conectar por SSH y descomprimir
ssh usuario@tu-ip-vps
cd /opt
tar -xzf portfolio.tar.gz
cd portfolio
```

### 3.6 Compilar el backend

```bash
cd /opt/portfolio
mvn clean package -DskipTests

# El JAR se genera en:
ls -la target/portfolio-backend-0.0.1-SNAPSHOT.jar
```

### 3.7 Configurar para producción

Crear archivo de configuración de producción:

```bash
sudo nano src/main/resources/application-prod.properties
```

Contenido:

```properties
server.port=8080

# CORS: Solo tu dominio
spring.web.cors.allowed-origins=https://tudominio.com,https://www.tudominio.com

# Base de datos MySQL (si la usas)
spring.datasource.url=jdbc:mysql://localhost:3306/portfolio_db?useSSL=true
spring.datasource.username=portfolio_user
spring.datasource.password=TU_CONTRASEÑA_SEGURA

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Seguridad
server.error.include-message=never
server.error.include-binding-errors=never
server.error.include-stacktrace=never

# Logging
logging.level.root=WARN
logging.level.com.manuelbalbas.portfolio=INFO
```

### 3.8 Instalar MySQL (si no tienes H2)

```bash
# Instalar MySQL
sudo apt install mysql-server -y

# Acceder a MySQL
sudo mysql

# Crear base de datos y usuario
CREATE DATABASE portfolio_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'portfolio_user'@'localhost' IDENTIFIED BY 'TU_CONTRASEÑA_SEGURA';
GRANT ALL PRIVILEGES ON portfolio_db.* TO 'portfolio_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 3.9 Crear servicio systemd para el backend

```bash
sudo nano /etc/systemd/system/portfolio-backend.service
```

Contenido:

```ini
[Unit]
Description=Portfolio Backend Spring Boot
After=syslog.target network.target

[Service]
User=www-data
WorkingDirectory=/opt/portfolio
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod /opt/portfolio/target/portfolio-backend-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
StandardOutput=journal
StandardError=journal
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Activar y arrancar el servicio:

```bash
sudo systemctl daemon-reload
sudo systemctl enable portfolio-backend
sudo systemctl start portfolio-backend

# Verificar estado
sudo systemctl status portfolio-backend

# Ver logs
sudo journalctl -u portfolio-backend -f
```

### 3.10 Instalar y configurar Nginx

Nginx actuará como proxy inverso:

```bash
# Instalar Nginx
sudo apt install nginx -y

# Crear configuración del sitio
sudo nano /etc/nginx/sites-available/portfolio
```

Contenido:

```nginx
server {
    listen 80;
    server_name tudominio.com www.tudominio.com;

    # Frontend estático
    root /opt/portfolio;
    index index.html;

    # Servir archivos estáticos
    location / {
        try_files $uri $uri/ =404;
    }

    # Proxy para el backend (API)
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Activar el sitio:

```bash
# Crear enlace simbólico
sudo ln -s /etc/nginx/sites-available/portfolio /etc/nginx/sites-enabled/

# Eliminar sitio por defecto
sudo rm /etc/nginx/sites-enabled/default

# Verificar configuración
sudo nginx -t

# Reiniciar Nginx
sudo systemctl restart nginx
```

### 3.11 Configurar firewall

```bash
# Permitir HTTP, HTTPS y SSH
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable

# Verificar estado
sudo ufw status
```

### 3.12 Instalar certificado SSL con Let's Encrypt

```bash
# Instalar Certbot
sudo apt install certbot python3-certbot-nginx -y

# Obtener certificado
sudo certbot --nginx -d tudominio.com -d www.tudominio.com

# Seguir las instrucciones (introducir email, aceptar términos)
```

Certbot configurará automáticamente Nginx para usar HTTPS y renovará el certificado automáticamente.

### 3.13 Actualizar URL del backend en el frontend

Edita `index.html` línea ~640:

```javascript
// Cambiar de localhost a tu dominio
const response = await fetch("https://tudominio.com/api/contact", {
    method: "POST",
    headers: {
        "Content-Type": "application/json",
    },
    body: JSON.stringify({ nombre, email, mensaje }),
});
```

Sube el archivo actualizado al servidor:

```bash
# En el servidor
sudo nano /opt/portfolio/index.html
# Hacer el cambio manualmente
```

### 3.14 Verificar el despliegue

1. Abre tu navegador: `https://tudominio.com`
2. Navega por el portfolio
3. Prueba el formulario de contacto
4. Verifica que funcione correctamente

### 3.15 Comandos útiles para gestionar el servicio

```bash
# Ver logs del backend
sudo journalctl -u portfolio-backend -f

# Reiniciar el backend
sudo systemctl restart portfolio-backend

# Detener el backend
sudo systemctl stop portfolio-backend

# Ver estado
sudo systemctl status portfolio-backend

# Reiniciar Nginx
sudo systemctl restart nginx
```

---

## 4. Alternativa: Backend en otro proveedor

Si tu hosting de DonWeb no soporta Java, puedes desplegar el backend en servicios gratuitos:

### 4.1 Railway (Recomendado)

**Ventajas**: Fácil, gratuito hasta 500h/mes, soporta Java/Spring Boot

1. **Crear cuenta en Railway**: https://railway.app/
2. **Nuevo proyecto → Deploy from GitHub**
3. **Conectar tu repositorio**
4. Railway detectará automáticamente Spring Boot
5. **Variables de entorno**:
   ```
   SPRING_PROFILES_ACTIVE=prod
   ```
6. Railway te dará una URL: `https://tu-app.railway.app`

### 4.2 Render

**Ventajas**: 750h gratis/mes

1. **Crear cuenta en Render**: https://render.com/
2. **New → Web Service**
3. **Conectar GitHub**
4. **Build Command**: `mvn clean package -DskipTests`
5. **Start Command**: `java -jar target/portfolio-backend-0.0.1-SNAPSHOT.jar`
6. Te dará una URL: `https://tu-app.onrender.com`

### 4.3 Actualizar frontend para usar backend externo

En `index.html`, cambiar la URL:

```javascript
const response = await fetch("https://tu-app.railway.app/api/contact", {
    // O https://tu-app.onrender.com/api/contact
    method: "POST",
    headers: {
        "Content-Type": "application/json",
    },
    body: JSON.stringify({ nombre, email, mensaje }),
});
```

### 4.4 Configurar CORS en el backend

En `application.properties` o en `ContactController.java`:

```java
@CrossOrigin(
    origins = {"https://tudominio.com", "https://www.tudominio.com"},
    allowedHeaders = {"Content-Type", "Accept"},
    methods = {RequestMethod.POST, RequestMethod.OPTIONS}
)
```

---

## 5. Configuración de dominio

### 5.1 Si tu dominio está en DonWeb

1. Accede al panel de DonWeb
2. **Dominios → Administrar DNS**
3. **Registros A**:
   - `@` → IP del servidor VPS
   - `www` → IP del servidor VPS

### 5.2 Esperar propagación DNS

Puede tardar entre 1-48 horas. Verifica con:

```bash
nslookup tudominio.com
ping tudominio.com
```

---

## 6. Troubleshooting

### El backend no arranca

```bash
# Ver logs completos
sudo journalctl -u portfolio-backend -n 100 --no-pager

# Verificar que el puerto 8080 no esté ocupado
sudo netstat -tulpn | grep 8080

# Probar manualmente
cd /opt/portfolio
java -jar target/portfolio-backend-0.0.1-SNAPSHOT.jar
```

### Nginx devuelve 502 Bad Gateway

El backend no está corriendo:

```bash
sudo systemctl status portfolio-backend
sudo systemctl start portfolio-backend
```

### CORS errors en el navegador

Verifica que la configuración CORS incluya tu dominio:

```java
@CrossOrigin(
    origins = {"https://tudominio.com", "https://www.tudominio.com"},
    // ...
)
```

### El formulario no envía datos

1. Abre la consola del navegador (F12)
2. Ve a la pestaña "Network"
3. Intenta enviar el formulario
4. Busca la petición a `/api/contact`
5. Verifica el status code y la respuesta

### Permisos en el servidor

```bash
# Dar permisos correctos
sudo chown -R www-data:www-data /opt/portfolio
sudo chmod -R 755 /opt/portfolio
```

---

## 📞 Soporte de DonWeb

Si necesitas ayuda específica con tu hosting:

- **Soporte técnico DonWeb**: https://www.donweb.com/soporte
- **Email**: soporte@donweb.com
- **Teléfono**: Consultar en su web según tu país

---

## ✅ Checklist de despliegue

### Hosting Compartido (Solo Frontend)
- [ ] Archivos `index.html` y `styles.css` subidos a `public_html`
- [ ] Sitio accesible en `https://tudominio.com`
- [ ] Formulario deshabilitado o conectado a servicio externo

### VPS/Cloud (Completo)
- [ ] Java 17+ instalado
- [ ] Proyecto compilado con Maven
- [ ] MySQL instalado y configurado (si aplica)
- [ ] Servicio systemd creado y activo
- [ ] Nginx configurado como proxy inverso
- [ ] Firewall configurado (puertos 80, 443, 22)
- [ ] Certificado SSL instalado (Let's Encrypt)
- [ ] URL del backend actualizada en el frontend
- [ ] CORS configurado correctamente
- [ ] Formulario de contacto funcionando

---

## 🎯 Próximos pasos después del despliegue

1. **Monitoreo**: Configurar alertas para saber si el servicio cae
2. **Backups**: Hacer copias de seguridad de la base de datos
3. **Actualizaciones**: Mantener Java y dependencias actualizadas
4. **Optimización**: Configurar caché en Nginx
5. **Analytics**: Añadir Google Analytics al frontend

---

**¿Necesitas ayuda específica?** Dime qué tipo de hosting tienes en DonWeb y te guío con los pasos exactos para tu caso.

