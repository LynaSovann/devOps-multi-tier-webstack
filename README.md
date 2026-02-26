# DevOps Production Webstack

A fully automated production-ready webstack deployment using **Vagrant**, **VirtualBox**, **PostgreSQL**, **MinIO**, **Spring Boot (Java)**, **Next.js (Node.js)**, and **NGINX**. This setup creates isolated VMs for each service with manual provisioning.

---

## 🚀 Features

- Multi-VM architecture:
  - `db01` – PostgreSQL 16.3
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

2. Verify **/etc/hosts** contains:

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

   - \*\*Restart PostgreSQL

   ```bash
   systemctl restart postgresql-16
   ```

---

### 2️⃣ MinIO Setup

---

## References

https://github.com/hkhcoder/vprofile-project
