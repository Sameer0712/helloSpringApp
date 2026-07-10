# CRUD Demo — Spring Boot + MySQL

A simple Task manager (title, description, due date) with full CRUD
(Create, Read, Update, Delete), backed by MySQL.

## 1. Set up the database in MySQL Workbench

1. Open MySQL Workbench, connect to your local MySQL server.
2. Open a new SQL tab and run:

   ```sql
   CREATE DATABASE taskdb;
   ```

   (You can skip this — the app is configured with
   `createDatabaseIfNotExist=true`, so it'll create `taskdb` automatically
   on first run. But creating it manually lets you confirm the connection
   works first.)

3. You don't need to create the `tasks` table yourself — Hibernate creates
   it automatically on startup because of `spring.jpa.hibernate.ddl-auto=update`
   in `application.properties`.

## 2. Configure your MySQL credentials

Open `src/main/resources/application.properties` and update:

```properties
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

Use whatever username/password you set up in MySQL Workbench (often `root`
plus the password you chose during MySQL installation).

## 3. Run locally

Requires Java 17+, Maven, and a running local MySQL server.

```bash
mvn spring-boot:run
```

On startup, check the logs for `Hibernate: create table tasks...` — that
confirms it connected and created the table. Then check MySQL Workbench:
refresh your schema list, expand `taskdb` → `Tables`, and you should see
`tasks`.

## API endpoints

| Method | URL                  | Description       |
|--------|----------------------|--------------------|
| GET    | `/api/tasks`         | List all tasks     |
| GET    | `/api/tasks/{id}`    | Get one task       |
| POST   | `/api/tasks`         | Create a task      |
| PUT    | `/api/tasks/{id}`    | Update a task      |
| DELETE | `/api/tasks/{id}`    | Delete a task      |

Example POST body:

```json
{
  "title": "Finish report",
  "description": "Q3 summary for the team",
  "dueDate": "2026-07-15"
}
```

Test quickly with curl:

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Finish report","description":"Q3 summary","dueDate":"2026-07-15"}'

curl http://localhost:8080/api/tasks
```

## Deploying live (full guide)

Three pieces need to go live: the MySQL database, this API, and (separately)
the React frontend. Do them in this order.

### Step 1 — Create a free MySQL database on Aiven

1. Go to https://aiven.io and sign up (GitHub or Google login works, no
   credit card needed).
2. Click **Create service** → choose **MySQL**.
3. Pick any cloud/region close to you, select the **Free** plan.
4. Name it (e.g. `taskdb-service`) and create it. It takes a minute or two
   to spin up.
5. Once it's running, open the service → **Overview** tab. You'll see
   connection details: **Host**, **Port**, **User**, **Password**,
   **Default database name**.
6. Open MySQL Workbench → **New Connection** → paste in that Host/Port/User,
   connect using the password shown. This is the same database, just now
   reachable from the internet instead of only `localhost`. You can create
   the `taskdb` database here the same way you did locally, or just use the
   default database Aiven already created for you.

### Step 2 — Deploy the backend to Render

1. Push this `crud-demo` project to a GitHub repo (same process as your
   earlier app).
2. On Render: **New** → **Web Service** → connect the repo.
3. Set **Language** to **Docker** (it'll pick up the included `Dockerfile`).
4. Scroll to **Environment Variables** and add:

   | Key | Value |
   |-----|-------|
   | `DB_URL` | `jdbc:mysql://<aiven-host>:<aiven-port>/<database-name>?sslMode=REQUIRED` |
   | `DB_USERNAME` | your Aiven username (usually `avnadmin`) |
   | `DB_PASSWORD` | your Aiven password |

   Copy the host/port/database name straight from the Aiven Overview tab.
   Aiven requires SSL, hence `sslMode=REQUIRED` in the URL.

5. Click **Create Web Service**. Watch the logs — you should see Hibernate
   connect and create the `tasks` table, same as it did locally.
6. Once live, note your API URL, e.g. `https://crud-demo.onrender.com`.

### Step 3 — Point the frontend at the live API

In the React app (`crud-react`), update `API_BASE_URL` in `src/App.jsx` to
your Render URL, then deploy the frontend to Vercel or Netlify as described
in its own README.

### Step 4 — Tighten CORS (optional but recommended)

Once you know your frontend's live URL, change
`@CrossOrigin(origins = "*")` in `TaskController.java` to your actual
frontend URL, e.g. `@CrossOrigin(origins = "https://your-app.vercel.app")`,
then push the change so Render redeploys.
