# Reading Groups

A web application for creating reading groups, sharing PDF documents, and discussing texts with other members.

Built with React, TypeScript, Spring Boot, and PostgreSQL. Containerized with Docker and deployed using GitHub Actions.

![Reading Groups](screenshots/RG_title.png)

## Features

- User registration and JWT-based authentication
- Reading group creation with PDF uploads
- Group membership and invitations
- In-browser PDF viewing
- Comments associated with passages in a document
- Group-based access control

## Tech Stack

| Component | Technologies |
|---|---|
| Frontend | React, TypeScript, Vite |
| Backend | Java 17, Spring Boot, Spring Security, Spring Data JPA |
| Database | PostgreSQL |
| API | REST, JWT, Springdoc OpenAPI |
| Infrastructure | Docker Compose, Caddy, GitHub Actions, GHCR |
| Testing | JUnit 5, Mockito |

## Screenshots

### Reading and Discussion

![PDF reader and comments](screenshots/RG_full.png)

### Creating a Group

![Create reading group](screenshots/RG_group.png)

### Adding a Comment

![Add comment](screenshots/RG_comment.png)

### Login

![Login page](screenshots/RG_login.png)

## Architecture

The React frontend communicates with a Spring Boot REST API, which handles authentication, authorization, PDF storage, and database operations.

PostgreSQL stores application data, while uploaded PDFs are stored separately in a persistent volume.

In production, Caddy serves the frontend, proxies API requests to Spring Boot, and handles HTTPS.

## Running Locally

### Requirements

- Docker
- Docker Compose

Clone the repository and navigate to its root directory.

Copy `.env.example` to `.env` and fill in every active variable. The example contains explicit local
settings, including empty SMTP credentials because Mailpit does not require authentication. Shell
variables may override `.env`; neither Compose nor the backend supplies environment-specific defaults.

Use a securely generated JWT secret.

Build and start the application:

```bash
docker compose -f compose.yaml -f compose.local.yaml up -d --build
```

The application will be available at **http://localhost**. `APP_BASE_URL` is shared by Caddy and the backend;
use an origin without a trailing slash or path.

Local email goes to Mailpit instead of real recipients; no Resend key is needed. Open
**http://localhost:8025** to inspect captured messages. Verification links open a confirmation page.
Group join links guide users through login or registration before joining.

Developer access is bound to your machine's loopback interface:

- Backend: `http://localhost:8080`
- PostgreSQL: `localhost:5432`, using the database credentials in `.env`
- Mailpit: `http://localhost:8025`

Use the normal Caddy address to test public routing. Swagger/OpenAPI return 404 through Caddy in both
environments; the direct backend port provides local access. Mailpit SMTP stays internal at `mailpit:1025`.

To view the logs:

```bash
docker compose -f compose.yaml -f compose.local.yaml logs -f
```

To stop the containers:

```bash
docker compose -f compose.yaml -f compose.local.yaml down
```

Database data and uploaded files are preserved in Docker volumes.

### Frontend Development with Live Updates

Add the development override to run Vite behind Caddy:

```bash
docker compose -f compose.yaml -f compose.local.yaml -f compose.dev.yaml up -d --build
```

Open the same **http://localhost** address. Changes in `frontend/src`, `frontend/public`, and
`frontend/index.html` appear without rebuilding. API requests and documentation restrictions still
pass through Caddy. Vite has no published host port.

Dependencies are installed when building the development image, not on every startup. After changing
package files or frontend configuration, rebuild the `frontend` service:

```bash
docker compose -f compose.yaml -f compose.local.yaml -f compose.dev.yaml up -d --build frontend
```

Use the same three `-f` arguments for logs and shutdown. To return to production-style local testing,
stop the development stack first, then use the original two-file command:

```bash
docker compose -f compose.yaml -f compose.local.yaml -f compose.dev.yaml down
docker compose -f compose.yaml -f compose.local.yaml up -d --build
```

Volumes are preserved when switching modes. The existing local setup still builds and serves the
static frontend for production-style testing.

### Running the Frontend Separately

The frontend can also be started with Vite:

```bash
cd frontend
npm ci
npm run dev
```

This requires a running backend and appropriate API proxy configuration.

## API Documentation

The REST API is documented using Springdoc OpenAPI and Swagger UI. Endpoints cover authentication, users, groups, comments, and invitations.

![Swagger API documentation](screenshots/swagger.png)

When running the application locally with Docker Compose:

- [Swagger UI](http://localhost:8080/swagger-ui/index.html)
- [OpenAPI specification](http://localhost:8080/v3/api-docs)

## Testing

Run the backend tests from the `backend` directory:

```bash
./mvnw test
```

Run the frontend linter:

```bash
cd frontend
npm run lint
```

## Deployment

The application can be deployed to a VPS using Docker Compose.

GitHub Actions runs the backend tests, builds the frontend and backend Docker images, publishes them to GitHub Container Registry, and deploys updated images to the VPS.

Configure the container image names, deployment directory, and repository secrets for your own environment.

`compose.yaml` defines shared services, volumes, and environment forwarding. Combine it with
`compose.local.yaml` for local builds, Mailpit, and developer ports, or `compose.prod.yaml` for published
production images. The base file is not intended to run by itself. Caddy handles HTTPS and reverse proxying, while PostgreSQL and uploaded files use persistent Docker volumes.

Start from `.env.example` on the VPS, retaining the database and JWT variables, and replace the local
settings with your production values. For example:

```env
APP_BASE_URL=https://app.example.com
EMAIL_FROM="Reading Groups <no-reply@notifications.example.com>"
SMTP_HOST=smtp.resend.com
SMTP_PORT=587
SMTP_USERNAME=resend
SMTP_PASSWORD=your_resend_api_key
SMTP_AUTH=true
SMTP_STARTTLS_ENABLE=true
SMTP_STARTTLS_REQUIRED=true
WEB_IMAGE=ghcr.io/your-account/reading-groups-web:latest
API_IMAGE=ghcr.io/your-account/reading-groups-api:latest
```

Production sends through Resend SMTP on port 587 with authentication and required STARTTLS. Only
Caddy publishes ports; the backend and database remain internal, and Mailpit is not included.
Use both Compose files for deployment:

```bash
docker compose -f compose.yaml -f compose.prod.yaml pull
docker compose -f compose.yaml -f compose.prod.yaml up -d
```

Update both Compose files on the VPS and deploy rebuilt images to apply these changes. CI pulls images
but does not copy updated Compose files or `.env` to the VPS.

### Group icons

Groups can optionally include an icon at creation.
