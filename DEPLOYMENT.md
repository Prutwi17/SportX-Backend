# 🚀 SportX Backend — Production Deployment Guide (Render + Aiven MySQL)

This document provides step-by-step instructions to deploy the `SportX-Backend` Spring Boot application to **Render Web Service** connected to **Aiven MySQL** and **Razorpay Payment Gateway**.

---

## 🛠️ Target Deployment Architecture
- **Host**: Render (Web Service via Docker)
- **Database**: Aiven MySQL Cloud Instance (Existing 59 products)
- **Security**: JWT Authentication + BCrypt
- **Payments**: Razorpay Gateway (Backend Verification)
- **Frontend URL**: Vercel (`https://your-app.vercel.app`)

---

## 1. Aiven MySQL Setup
Ensure your Aiven MySQL database connection parameters are ready:
- Host: `mysql-<project>-<user>.aivencloud.com`
- Port: `10000` to `28000` (e.g. `12345`)
- User: `avnadmin`
- Password: `<aiven-mysql-password>`
- Database Name: `sportx_db`

**JDBC Connection String Format**:
```text
jdbc:mysql://<aiven-host>:<aiven-port>/sportx_db?useSSL=true&requireSSL=true&verifyServerCertificate=false&serverTimezone=UTC
```

---

## 2. Render Web Service Deployment

1. Log in to [Render Dashboard](https://dashboard.render.com/).
2. Click **New +** → Select **Web Service**.
3. Connect your GitHub Repository: `https://github.com/Prutwi17/SportX-Backend`.
4. Configure Service Settings:
   - **Name**: `sportx-backend`
   - **Region**: Oregon (or nearest to your database)
   - **Branch**: `main`
   - **Runtime**: `Docker`
   - **Dockerfile Path**: `Dockerfile`
5. Click **Advanced** → **Environment Variables** and add the following keys:

| Environment Variable Key | Production Example Value | Notes |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `prod` | Activates `application-prod.properties` |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://<aiven-host>:<port>/sportx_db?useSSL=true...` | Aiven MySQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `avnadmin` | Aiven MySQL Username |
| `SPRING_DATASOURCE_PASSWORD` | `<your-aiven-password>` | Aiven MySQL Password |
| `APP_JWT_SECRET` | `<random-64-character-jwt-secret>` | Secret key for signing JWTs |
| `RAZORPAY_KEY_ID` | `rzp_live_...` or `rzp_test_...` | Razorpay Dashboard Key ID |
| `RAZORPAY_KEY_SECRET` | `<your-razorpay-secret>` | Razorpay Dashboard Key Secret |
| `APP_CORS_ALLOWED_ORIGINS` | `https://sportx-frontend.vercel.app` | Vercel Frontend Production Domain |
| `SEED_DEMO_DATA` | `false` | Prevents overwriting existing database |
| `SEED_ADMIN` | `false` | Prevents overwriting existing admin account |

6. Click **Create Web Service**. Render will execute the multi-stage Docker build and start the Spring Boot application on dynamic `${PORT}`.

---

## 3. Verification & Health Check
Once Render finishes deploying:
```bash
curl https://sportx-backend.onrender.com/api/v1/products?page=0&size=1
```
Expected response: HTTP 200 OK returning total elements (59 products).
