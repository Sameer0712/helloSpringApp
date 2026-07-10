# Spring Boot Demo

A minimal Spring Boot REST API with three endpoints:
- `GET /` — welcome message
- `GET /hello?name=YourName` — greeting
- `GET /api/status` — JSON health/status check

## Run locally

Requires Java 17+ and Maven.

```bash
mvn spring-boot:run
```

Visit `http://localhost:8080/hello?name=Sameer`.

## Deploy live (free options)

### Option A: Render.com (easiest, no credit card)

1. Push this project to a GitHub repo.
2. Go to https://render.com → New → Web Service → connect your repo.
3. Render auto-detects the `Dockerfile` (included in this project). If it asks for settings manually:
   - **Environment**: Docker
   - **Build Command**: (leave blank, Docker handles it)
   - **Start Command**: (leave blank, Docker handles it)
4. Render sets a `PORT` environment variable automatically — the app already reads it via `server.port=${PORT:8080}` in `application.properties`, so no changes needed.
5. Click **Create Web Service**. First build takes a few minutes. You'll get a live URL like `https://your-app.onrender.com`.

Note: Render's free tier spins the service down after inactivity, so the first request after idling takes ~30-50 seconds to wake up.

### Option B: Railway.app

1. Push to GitHub.
2. Go to https://railway.app → New Project → Deploy from GitHub repo.
3. Railway detects the Dockerfile automatically and builds/deploys it.
4. It assigns a public domain under Settings → Networking → Generate Domain.

### Option C: Fly.io

1. Install the `flyctl` CLI and run `fly launch` inside this project folder — it detects the Dockerfile.
2. `fly deploy` to push it live.
3. Fly gives you a `https://your-app.fly.dev` URL.

## Test locally with Docker

```bash
docker build -t demo-app .
docker run -p 8080:8080 demo-app
```

Then visit `http://localhost:8080`.
