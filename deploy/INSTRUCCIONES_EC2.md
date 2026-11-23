# AutoMarket — AWS Academy Learner Lab / EC2 / MongoDB

## 1. EC2

- Learner Lab → **Start Lab** → **AWS** → EC2 → Launch Instance.
- Nombre: `DSY1104-AUTOMARKET`; AMI: **Amazon Linux 2023 x86_64**.
- `t3.micro` solo si está permitida; **1 GiB de RAM es muy justo** para MongoDB y Spring Boot juntos. Si Learner Lab permite 2 GiB o más, elegir esa opción.
- Red: IP pública; puerto 22 (solo tu IP), puerto 80 (público); 443 para HTTPS. **No abrir 8080 ni 27017 públicamente**.
- Clave `.pem` privada; no subirla a GitHub.

## 2. Preparar EC2

```bash
sudo dnf update -y
sudo dnf install -y nginx git unzip java-21-amazon-corretto-headless java-21-amazon-corretto-devel maven
sudo systemctl enable --now nginx
```

En instancias de baja RAM, si hace falta espacio swap temporal:

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

## 3. Instalar MongoDB Community Server

Usar el repositorio oficial compatible con Amazon Linux 2023 y arquitectura x86_64. En la documentación oficial de MongoDB, confirmar la versión estable compatible antes de instalarla; no usar un repositorio destinado a otra distribución.

- Guía oficial: https://www.mongodb.com/docs/manual/tutorial/install-mongodb-on-amazon/
- Instalar `mongodb-org` conforme a esa guía.
- Iniciar y habilitar servicio:

```bash
sudo systemctl enable --now mongod
sudo systemctl status mongod
```

MongoDB debe permanecer escuchando solo en `127.0.0.1:27017`. Verificar:

```bash
sudo ss -lntp | grep 27017
```

En una VM compartida para prácticas, no exponer MongoDB al público. El backend local se conectará por `mongodb://127.0.0.1:27017/automarket_fullstack`.

## 4. Subir repositorios de código

Publicar dos repositorios públicos (frontend y backend) en GitHub **sin** claves `.pem`, `.env`, contraseñas ni `node_modules`:

```bash
cd ~
git clone URL_REPO_FRONTEND frontend
git clone URL_REPO_BACKEND backend
```

## 5. Backend Spring Boot

```bash
cd ~/backend
mvn clean package -DskipTests
sudo mkdir -p /opt/automarket
sudo cp target/automarket-api-1.0.0.jar /opt/automarket/automarket-api.jar
openssl rand -hex 48
sudo nano /etc/automarket.env
```

Archivo `/etc/automarket.env` (sustituir valores de ejemplo):

```ini
MONGODB_URI=mongodb://127.0.0.1:27017/automarket_fullstack
JWT_SECRET=SECRETO_ALEATORIO_GENERADO
ADMIN_EMAIL=admin@automarket.cl
ADMIN_PASSWORD=PASSWORD_SEGURA_DE_ADMIN
COOKIE_SECURE=false
```

```bash
sudo chmod 600 /etc/automarket.env
sudo cp ~/backend/deploy/automarket.service /etc/systemd/system/automarket.service
sudo systemctl daemon-reload
sudo systemctl enable --now automarket
sudo systemctl status automarket
curl http://127.0.0.1:8080/api/vehiculos
```

`COOKIE_SECURE=false` es exclusivamente para pruebas en HTTP; los datos de acceso son vulnerables en tránsito sin HTTPS. Para publicar con credenciales reales configurar HTTPS y `COOKIE_SECURE=true`.

## 6. Frontend y Nginx

Instalar Node.js compatible con Vite (revisar `frontend/package.json`). Ejemplo con Node.js 22 desde NodeSource:

```bash
curl -fsSL https://rpm.nodesource.com/setup_22.x | sudo bash -
sudo dnf install -y nodejs
cd ~/frontend
npm ci
npm run build
sudo cp -r dist/. /usr/share/nginx/html/
sudo cp ~/backend/deploy/nginx-automarket.conf /etc/nginx/conf.d/automarket.conf
sudo nginx -t
sudo systemctl restart nginx
```

Si Nginx sirve su página de bienvenida, comprobar el servidor por defecto en `/etc/nginx/nginx.conf` y corregir el conflicto. React usa `/api` relativo al servidor, no `localhost` desde el navegador.

## 7. Comprobaciones

- `http://IP_PUBLICA/` — React.
- `http://IP_PUBLICA/api/vehiculos` — vehículos MongoDB.
- `http://IP_PUBLICA/swagger-ui/index.html` — Swagger.
- Registro, login, recarga, roles CLIENTE/ADMIN, CRUD administrativo y envío del formulario de contacto.

Comprobar MongoDB desde EC2 mediante `mongosh` si está instalado:

```javascript
use automarket_fullstack
show collections
db.vehiculos.find().limit(5)
db.usuarios.countDocuments({})
db.consultas.countDocuments({})
```

## 8. Evidencias

Capturar: instancia Running/IP, Swagger y endpoints CRUD, frontend público, inicio de sesión y restricciones por rol, CRUD reflejado en MongoDB, consultas guardadas, repositorios públicos, servicio Spring Boot activo. No mostrar secretos.

**AWS Academy:** no utilizar `Reset`; la IP pública puede cambiar tras detener la VM. Los servicios activos pueden consumir presupuesto. Presionar `End Lab` al terminar según la guía del curso.
