# PRAGMATRIX 2026 — Render & Docker Deployment Guide

This guide details how to deploy the PRAGMATRIX 2026 multi-module Jakarta EE application to **Render** using Docker, backed by an **Aiven MySQL** cloud database instance.

---

## 1. Architecture Overview

The repository consists of two separate deployable web applications packaged as Jakarta WARs running on Apache Tomcat 10.1 (Java 17):

1. **Public Web Service** (`Dockerfile.public`):
   - Participant-facing team registration, team OTP login, round criteria viewer, and live leaderboard.
   - Deployed at the root path (`/`) on its own Render Web Service (e.g. `https://pragmatrix.onrender.com`).
2. **Admin Web Service** (`Dockerfile.admin`):
   - Admin authentication (restricted 2-email OTP login), round management, judging criteria management, and live score entry.
   - Deployed at the root path (`/`) on a separate Render Web Service (e.g. `https://pragmatrix-admin.onrender.com`).
3. **Database Layer (Shared Aiven MySQL)**:
   - Both web services connect to the same Aiven-hosted MySQL database over mandatory TLS/SSL (`sslMode=REQUIRED`) using HikariCP connection pooling.

---

## 2. Required Environment Variables

Configure these environment variables in your Render Web Service settings (under **Environment** tab):

### A. Database Connection (Aiven MySQL)

| Variable | Description | Example / Default |
|---|---|---|
| `DB_HOST` | Aiven MySQL Hostname | `mysql-2c7c6b80-ananthakshay2006-ce0e.k.aivencloud.com` |
| `DB_PORT` | Aiven MySQL Port | `23975` |
| `DB_NAME` | Database Name | `defaultdb` |
| `DB_USER` | MySQL Username | `avnadmin` |
| `DB_PASSWORD` | MySQL Password | *(Your Aiven secret password)* |
| `DB_POOL_MAX_SIZE` | *(Optional)* Max connections in HikariCP pool | `10` (conservative limit for cloud plans) |
| `DB_POOL_MIN_IDLE` | *(Optional)* Minimum idle connections | `2` |
| `DB_POOL_TIMEOUT` | *(Optional)* Connection acquisition timeout (ms) | `30000` |

### B. Transactional Email API (Brevo HTTPS REST API)

Render container environments may restrict outbound SMTP ports (25, 465, 587). PRAGMATRIX 2026 uses **Brevo Transactional Email REST API** over HTTPS (`POST https://api.brevo.com/v3/smtp/email`) by default.

| Variable | Description | Example / Default |
|---|---|---|
| `BREVO_API_KEY` | Brevo v3 Transactional API Key *(Primary)* | `xkeysib-xxxxxxxxxxxxxxxxxxxx` |
| `SENDER_EMAIL` | Verified Sender Email Address in Brevo | `pragmatrix2k26@gmail.com` |
| `SENDER_NAME` | Sender Display Name | `PRAGMATRIX 2026` |
| `PUBLIC_APP_URL` | Public Portal URL (included in registration emails) | `https://pragmatrix.onrender.com` |

*(Note: `EMAIL_FROM_ADDRESS` and `EMAIL_FROM_NAME` are also supported as aliases for `SENDER_EMAIL` and `SENDER_NAME`)*

#### SMTP Fallback (Optional / Local Dev Only)

| Variable | Description | Example / Default |
|---|---|---|
| `SMTP_HOST` | SMTP Server Host | `smtp.gmail.com` |
| `SMTP_PORT` | SMTP Server Port | `587` (STARTTLS) or `465` (SSL) |
| `SMTP_USERNAME` | SMTP User / Email | `your-email@gmail.com` |
| `SMTP_PASSWORD` | SMTP App Password / Token | *(16-character Google App Password)* |

### C. Web Service Port Configuration

| Variable | Service | Description | Example |
|---|---|---|---|
| `PORT` | Public & Admin | Container listening port (Render injects this automatically) | `10000` (Render default) |

---

## 3. Render Web Service Setup

### Service 1: Public App (Team Portal & Leaderboard)
1. In Render Dashboard, click **New +** -> **Web Service**.
2. Connect your GitHub repository: `https://github.com/AnanthAkshay/PRAGMATRIX`.
3. Configure settings:
   - **Name**: `pragmatrix-public`
   - **Language / Runtime**: `Docker`
   - **Dockerfile Path**: `Dockerfile.public`
   - **Docker Build Context**: `.` (root directory)
   - **Health Check Path**: `/health`
4. Add all environment variables listed in Section 2 (Database, Brevo, and `PUBLIC_APP_URL`).
5. Click **Create Web Service**.

### Service 2: Admin App (Dashboard & Scoring)
1. Click **New +** -> **Web Service**.
2. Connect the same repository: `https://github.com/AnanthAkshay/PRAGMATRIX`.
3. Configure settings:
   - **Name**: `pragmatrix-admin`
   - **Language / Runtime**: `Docker`
   - **Dockerfile Path**: `Dockerfile.admin`
   - **Docker Build Context**: `.` (root directory)
   - **Health Check Path**: `/health`
4. Add all environment variables listed in Section 2 (Database, Brevo, and `PUBLIC_APP_URL`).
5. Click **Create Web Service**.

---

## 4. Local Testing with Docker

Before deploying to Render, you can verify both Docker images locally.

### Build Public App Container
```bash
docker build -t pragmatrix-public -f Dockerfile.public .
```

### Run Public App Container
```bash
docker run -p 8080:8080 \
  -e DB_HOST="mysql-2c7c6b80-ananthakshay2006-ce0e.k.aivencloud.com" \
  -e DB_PORT="23975" \
  -e DB_NAME="defaultdb" \
  -e DB_USER="avnadmin" \
  -e DB_PASSWORD="<YOUR_AIVEN_PASSWORD>" \
  -e BREVO_API_KEY="<YOUR_BREVO_KEY>" \
  -e SENDER_EMAIL="pragmatrix2k26@gmail.com" \
  -e SENDER_NAME="PRAGMATRIX 2026" \
  -e PUBLIC_APP_URL="http://localhost:8080" \
  pragmatrix-public
```
- Access at: `http://localhost:8080/`
- Health check: `http://localhost:8080/health` (should return `OK`)

---

### Build Admin App Container
```bash
docker build -t pragmatrix-admin -f Dockerfile.admin .
```

### Run Admin App Container
```bash
docker run -p 8081:8080 \
  -e DB_HOST="mysql-2c7c6b80-ananthakshay2006-ce0e.k.aivencloud.com" \
  -e DB_PORT="23975" \
  -e DB_NAME="defaultdb" \
  -e DB_USER="avnadmin" \
  -e DB_PASSWORD="<YOUR_AIVEN_PASSWORD>" \
  -e BREVO_API_KEY="<YOUR_BREVO_KEY>" \
  -e SENDER_EMAIL="pragmatrix2k26@gmail.com" \
  -e SENDER_NAME="PRAGMATRIX 2026" \
  pragmatrix-admin
```
- Access at: `http://localhost:8081/` (or `http://localhost:8081/login`)
- Health check: `http://localhost:8081/health` (should return `OK`)

---

## 5. Health Check Endpoints

Both apps include a zero-dependency health check servlet at:
- `GET /health` -> `HTTP 200 OK` (Body: `OK`)
- `GET /healthz` -> `HTTP 200 OK` (Body: `OK`)

These endpoints confirm the Tomcat container and servlet engine are healthy without hitting database connection pools.
