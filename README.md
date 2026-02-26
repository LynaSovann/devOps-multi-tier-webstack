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

---

## References

https://github.com/hkhcoder/vprofile-project
