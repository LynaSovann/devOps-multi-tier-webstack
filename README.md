# DevOps Production Webstack

A fully automated production-ready webstack deployment using **Vagrant**, **VirtualBox**, **PostgreSQL**, **MinIO**, **Spring Boot (Java)**, **Next.js (Node.js)**, and **NGINX**. This setup creates isolated VMs for each service with manual provisioning.

---

## 🚀 Features

- Multi-VM architecture:
  - `db01` – PostgreSQL 16 +
  - `minio01` – MinIO object storage
  - `backend01` – Spring Boot backend
  - `frontend01` – Next.js frontend
  - `web01` – NGINX reverse proxy
- Production-grade configuration with systemd services.
- Fully isolated network using private IPs.
- Easy configuration for database, MinIO, and environment variables.

---

## 📌 Prerequisites

- [Oracle VM VirtualBox](https://www.virtualbox.org/)
- [Vagrant](https://www.vagrantup.com/)
- Vagrant plugin:
  ```bash
  vagrant plugin install vagrant-hostmanager
  ```
- Git Bash or equivalent terminal

---

### 🖥️ VM Setup

1. Clone the repository

```bash
git clone -b main https://github.com/LynaSovann/devOps-production-webstack.git
```

2. Navigate to the Vagrant folder:

```bash
cd devOps-production-webstack/vagrant/manual-provisioning/
```

3. Bring up all VMs:

```bash
vagrant up
```

<img src="/docs/vms.png">

#### 🛠️ Services Setup Order

1. PostgreSQL (Database Server)
2. MinIO (Object Storage)
3. Backend (Spring Boot / Java)
4. Frontend (Next.js / Node.js)
5. NGINX (Web Server / Reverse Proxy)
   ⚠️ Ensure each service is running successfully before proceeding to the next.

---

### 1️⃣ PostgreSQL Setup

1. SSH into DB VM and Switch to root user:

```bash
vagrant ssh db01
```

```bash
sudo -i
```

2. Verify **/etc/hosts** contains: (do this every vm)

```bash
192.168.56.10 web01
192.168.56.11 frontend01
192.168.56.12 backend01
192.168.56.13 db01
192.168.56.14 minio01
```

3. Update OS & disable default PostgreSQL module:

```bash
dnf update -y
```

```bash
dnf -qy module disable postgresql
```

4. Install PostgreSQL 16 repository & server:

```bash
dnf install -y https://download.postgresql.org/pub/repos/yum/reporpms/EL-9-x86_64/pgdg-redhat-repo-latest.noarch.rpm
dnf install -y postgresql16-server postgresql16
/usr/pgsql-16/bin/postgresql-16-setup initdb
systemctl enable --now postgresql-16
```

5. Create database and user:

```bash
sudo -i -u postgres
```

```bash
CREATE DATABASE accounts;
CREATE USER admin WITH ENCRYPTED PASSWORD 'admin123';
GRANT ALL PRIVILEGES ON DATABASE accounts TO admin;
```

```bash
\q
```

6. Create tables in **accounts**:

```bash
psql -d accounts

CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(100),
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(150) NOT NULL
);

CREATE TABLE user_infos (
    user_info_id SERIAL PRIMARY KEY,
    firstname VARCHAR(100) NOT NULL,
    lastname VARCHAR(100) NOT NULL,
    profile_image TEXT,
    bio TEXT,
    user_id INTEGER NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id)
    REFERENCES users (user_id) ON DELETE CASCADE
);
```

7. Update ownership:

```bash
ALTER DATABASE accounts OWNER TO admin;
ALTER TABLE users OWNER TO admin;
ALTER TABLE user_infos OWNER TO admin;
```

8. Configure PostgreSQL
   - **postgresql.conf**

   ```bash
   listen_addresses = '*'
   ```

   - **pg.hba.conf**

   ```bash
   host all all 192.168.56.0/24 md5
   ```

   - Restart PostgreSQL

   ```bash
   systemctl restart postgresql-16
   ```

---

### 2️⃣ MinIO Setup

1. SSH into MinIO VM

```bash
vagrant ssh minio01
```

2. Install MioIO

```bash
dnf install wget -y
wget https://dl.min.io/server/minio/release/linux-amd64/minio
chmod +x minio
sudo mv minio /usr/local/bin/
```

3. Create User and data directory

```bash
sudo useradd -r minio-user -s /sbin/nologin
sudo mkdir /data
sudo chown -R minio-user:minio-user /data
```

4. Environment file **/etc/default/minio**

```bash
MINIO_ROOT_USER=admin
MINIO_ROOT_PASSWORD=admin123
MINIO_VOLUMES="/data"
MINIO_OPTS="--address :9000 --console-address :9001"
```

5. Systemd service **/etc/systemd/system/minio.service**

```bash
[Unit]
Description=MinIO
Documentation=https://docs.min.io
Wants=network-online.target
After=network-online.target
AssertFileIsExecutable=/usr/local/bin/minio

[Service]
WorkingDirectory=/usr/local/

User=minio-user
Group=minio-user

EnvironmentFile=/etc/default/minio
ExecStart=/usr/local/bin/minio server $MINIO_VOLUMES $MINIO_OPTS

Restart=always
LimitNOFILE=65536
TasksMax=infinity
TimeoutStopSec=infinity
SendSIGKILL=no

[Install]
WantedBy=multi-user.target
```

6. Enable and start MinIO

```bash
systemctl daemon-reload
systemctl enable --now minio
```

7. Connect Spring Boot backend:

```bash
minio.url=http://minio01:9000
minio.access.name=admin
minio.access.secret=admin123
minio.bucket.name=profile-image
```

---

### 3️⃣ Backend (Spring Boot)

1. SSH into backend VM

```bash
vagrant ssh backend01
```

2. Install Java & Maven

```bash
dnf install java-17-openjdk java-17-openjdk-devel wget unzip git -y
wget https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip
unzip apache-maven-3.9.9-bin.zip
sudo mv apache-maven-3.9.9 /usr/local/maven
```

3. Clone the project

```bash
git clone -b main https://github.com/LynaSovann/devOps-production-webstack.git
```

4. Update **application.properties**

```bash
vi devOps-production-webstack/app/backend/src/main/resources/application.properties
```

```bash
spring.application.name=backend

spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

minio.url=${MINIO_URL}
minio.access.name=${MINIO_ACCESS_NAME}
minio.access.secret=${MINIO_ACCESS_SECRET}
minio.bucket.name=${MINIO_BUCKET_NAME}

app.cors.allowed-origins=${APP_CORS_ALLOWED_ORIGINS}
```

5. Create Environment file

```bash
sudo vim /etc/backend.conf
```

```bash
SPRING_PROFILES_ACTIVE=prod

SPRING_DATASOURCE_URL=jdbc:postgresql://db01:5432/accounts
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=admin123

MINIO_URL=http://minio01:9000
MINIO_ACCESS_NAME=admin
MINIO_ACCESS_SECRET=admin123
MINIO_BUCKET_NAME=profile-image

APP_CORS_ALLOWED_ORIGINS=http://192.168.56.10
```

6. Go to the project directory, build & package

```bash
devOps-production-webstack/app/backend
```

```bash
/usr/local/maven/bin/mvn clean package
```

7. Move JAR to **/opt/backend**

```bash
sudo mkdir -p /opt/backend
sudo cp target/*.jar /opt/backend/backend.jar
```

8. Create Dedicated user and set ownership

```bash
sudo useradd -r -m -d /opt/backend -s /sbin/nologin backend-user
```

```bash
sudo chown -R backend-user:backend-user /opt/backend
```

9. Create systemd service **/etc/systemd/system/backend.service**

```bash
[Unit]
Description=Spring Boot Backend Application
After=network.target

[Service]
User=backend-user
Group=backend-user

WorkingDirectory=/opt/backend
EnvironmentFile=/etc/backend.conf

ExecStart=/usr/bin/java -Xms256m -Xmx512m -jar /opt/backend/backend.jar
SuccessExitStatus=143

Restart=always
RestartSec=5

LimitNOFILE=65536
TimeoutStopSec=30

[Install]
WantedBy=multi-user.target
```

10. Enable and start service

```bash
sudo systemctl daemon-reload
sudo systemctl enable backend
sudo systemctl start backend
```

11. Check Status

```bash
sudo systemctl status backend
```

---

### 4️⃣ Frontend (Next.js)

1. SSH into frontend VM

```bash
vagrant ssh frontend01
```

2. Install Node 22+ & Yarn:

```bash
curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
sudo apt install -y nodejs
corepack enable
corepack prepare yarn@stable --activate
```

3. Clone the project

```bash
git clone -b main https://github.com/LynaSovann/devOps-production-webstack.git
```

4. Go to the project directory and replace **.env**

```bash
cd devOps-production-webstack/app/frontend/
```

```bash
NEXTAUTH_SECRET=saD7UbMx0RCjQN9YFDtyTsDdw5RbbhJcDfeXH55dq0A=
NEXTAUTH_URL=http://192.168.56.10
NEXT_PUBLIC_BACKEND_URL=http://192.168.56.12:8080
```

5. Install dependencies and build

```bash
yarn
```

```bash
yarn build
```

6. Create Dedicated User

```bash
sudo adduser --system --group --home /home/frontend --shell /bin/bash frontend
```

7. Move project to other location and change the ownership to **frontend** user

```bash
sudo mkdir -p /opt/frontend
sudo mv /root/devOps-production-webstack /opt/frontend
```

```bash
sudo chown -R frontend:frontend /opt/frontend
```

8. Test as frontend user,

```bash
sudo -u frontend -i
node -v
yarn -v
exit
```

9. Create config directory (optional)

```bash
sudo mkdir -p /etc/frontend
sudo nano /etc/frontend/frontend.env
```

```bash
NODE_ENV=production
NEXTAUTH_SECRET=saD7UbMx0RCjQN9YFDtyTsDdw5RbbhJcDfeXH55dq0A=
NEXTAUTH_URL=http://192.168.56.10
NEXT_PUBLIC_BACKEND_URL=http://192.168.56.12:8080
```

10. Secure the config directory

```bash
sudo chown root:frontend /etc/frontend/frontend.env
sudo chmod 640 /etc/frontend/frontend.env
```

11. Update systemd service

```bash
sudo vim /etc/systemd/system/frontend.service
```

```bash
[Unit]
Description=Next.js Frontend
After=network.target

[Service]
User=frontend
Group=frontend
WorkingDirectory=/opt/frontend/devOps-production-webstack/app/frontend
EnvironmentFile=/etc/frontend/frontend.env
ExecStart=/usr/bin/yarn start -H 0.0.0.0 -p 3000
Restart=always
RestartSec=5
Environment=NODE_ENV=production

[Install]
WantedBy=multi-user.target
```

12. Start the service

```bash
sudo systemctl daemon-reload
sudo systemctl enable frontend
sudo systemctl restart frontend
```

13. Check status

```bash
sudo systemctl status frontend
```

14. View Log

```bash
journalctl -u frontend
```

---

### 5️⃣ NGINX (Reverse Proxy)

1. SSH into web VM

```bash
vagrant ssh web01
sudo -i
```

2. Install NGINX

```bash
apt update && apt install nginx -y
```

3. Create NGINX config **/etc/nginx/sites-available/app**

```bash
upstream frontend {
    server frontend01:3000;
}

server {
    listen 80;
    server_name _;

    location / {
        proxy_pass http://frontend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }
}
```

4. Enable config and restart NGINX

```bash
rm -f /etc/nginx/sites-enabled/default
ln -s /etc/nginx/sites-available/app /etc/nginx/sites-enabled/app
systemctl restart nginx
```

5. Check NGINX status

```bash
systemctl status nginx
```

### Access App

```bash
http://192.168.56.10
```

### Flow

**User → web01 (NGINX) → frontend01 (Next.js) → backend01 (Spring Boot) → db01 (PostgreSQL)
backend01 → minio01 (Object storage)**

---

## References

- https://nginx.org/en/docs/
- https://www.postgresql.org/docs/
- https://docs.min.io/enterprise/aistor-object-store/
- https://spring.io/projects/spring-boot
- https://nextjs.org/docs
- https://github.com/hkhcoder/vprofile-project
