# CRUD Demo — Spring Boot + MySQL + Auth

A Task manager (title, description, due date) with full CRUD, user
registration/login via JWT, and per-user task privacy — backed by MySQL.

## 1. Set up the database in MySQL Workbench

1. Open MySQL Workbench, connect to your local MySQL server.
2. Open a new SQL tab and run:

   ```sql
   CREATE DATABASE taskdb;
   ```

   (You can skip this — the app is configured with
   `createDatabaseIfNotExist=true`, so it'll create `taskdb` automatically
   on first run.)

3. You don't need to create the `users` or `tasks` tables yourself —
   Hibernate creates them automatically on startup because of
   `spring.jpa.hibernate.ddl-auto=update` in `application.properties`.

## 2. Configure your MySQL credentials

Open `src/main/resources/application.properties` and update:

```properties
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

## 3. Run locally

Requires Java 17+, Maven, and a running local MySQL server.

```bash
mvn spring-boot:run
```

Check MySQL Workbench: refresh `taskdb` → `Tables`, and you should see both
`users` and `tasks`.

## Auth endpoints

| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/auth/register` | Create an account, returns a JWT |
| POST | `/api/auth/login` | Log in, returns a JWT |

Register body:
```json
{ "username": "sameer", "email": "sameer@example.com", "password": "secret123" }
```

Login body:
```json
{ "username": "sameer", "password": "secret123" }
```

Both return:
```json
{ "token": "eyJhbGciOi...", "username": "sameer", "email": "sameer@example.com" }
```

## Task endpoints (all require a JWT)

Every request below must include a header:
`Authorization: Bearer <token>`

Tasks are private per-user — you'll only ever see and modify your own.

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/tasks` | List your tasks |
| GET | `/api/tasks/{id}` | Get one of your tasks |
| POST | `/api/tasks` | Create a task |
| PUT | `/api/tasks/{id}` | Update your task |
| DELETE | `/api/tasks/{id}` | Delete your task |

Test with curl:

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"sameer","email":"sameer@example.com","password":"secret123"}'

# Copy the "token" from the response, then:
TOKEN="paste-token-here"

curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Finish report","description":"Q3 summary","dueDate":"2026-07-15"}'

curl http://localhost:8080/api/tasks -H "Authorization: Bearer $TOKEN"
```

## Deploying live (full guide)

Three pieces need to go live: the MySQL database, this API, and (separately)
the React frontend. Do them in this order.

### Step 1 — Create a free MySQL database on Aiven

1. Go to https://aiven.io and sign up (GitHub or Google login works, no
   credit card needed).
2. Click **Create service** → choose **MySQL**.
3. Pick any cloud/region close to you, select the **Free** plan.
4. Name it and create it.
5. Once running, open the service → **Overview** tab for connection details.
6. Make sure **Allowed IP Addresses** includes `0.0.0.0/0` (or your Render
   deploy region) so your live backend can actually reach it — this tripped
   us up before, so don't skip it.

### Step 2 — Deploy the backend to Render

1. Push this `crud-demo` project to a GitHub repo.
2. On Render: **New** → **Web Service** → connect the repo.
3. Set **Language** to **Docker** (it'll pick up the included `Dockerfile`).
4. Scroll to **Environment Variables** and add:

   | Key | Value |
   |-----|-------|
   | `DB_URL` | `jdbc:mysql://<aiven-host>:<aiven-port>/<database-name>?sslMode=REQUIRED` |
   | `DB_USERNAME` | your Aiven username (usually `avnadmin`) |
   | `DB_PASSWORD` | your Aiven password |
   | `JWT_SECRET` | a long random string (32+ characters) — don't reuse the dev default |

   Copy the host/port/database name straight from Aiven's **Connection
   information** panel (the individual fields, not the combined Service URI).

5. Click **Create Web Service**. Watch the logs for Hibernate creating the
   `users` and `tasks` tables.
6. Once live, note your API URL, e.g. `https://crud-demo.onrender.com`.

### Step 3 — Point the frontend at the live API

In the React app (`crud-react`), update `API_BASE_URL` in `src/api.js` to
your Render URL, then deploy the frontend to Vercel or Netlify as described
in its own README.

### Step 4 — Tighten CORS (optional but recommended)

Once you know your frontend's live URL, change the allowed origin pattern
in `SecurityConfig.java` from `"*"` to your actual frontend URL, e.g.
`List.of("https://your-app.vercel.app")`, then push the change so Render
redeploys.

## Security notes for a real app

This is a solid learning setup, but if you ever take this beyond a demo:
- Generate a proper random `JWT_SECRET` (e.g. `openssl rand -base64 48`) and
  never commit it — set it only as an environment variable.
- Consider shortening the token lifetime (`jwt.expiration`) and adding a
  refresh-token flow.
- Add rate limiting on `/api/auth/login` to slow down brute-force attempts.
