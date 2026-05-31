# SMS Frontend (React)

React + TypeScript frontend for the Student Management System microservices backend.

## Prerequisites

- Node.js 18+
- Backend running (`docker compose up -d` from project root)
- API Gateway on http://localhost:8080

## Quick start

```powershell
cd frontend
npm install
npm run dev
```

Open http://localhost:5173

## Build for production

```powershell
npm run build
npm run preview
```

Set `VITE_API_BASE_URL` in `.env`:

- **Dev (recommended):** leave it **empty** — Vite proxies `/api` to the gateway (no CORS issues).
- **Production:** set to your API Gateway URL, e.g. `http://localhost:8080`.

## Features

- Login and registration (validate code + set password)
- Role-based routing (Admin, Student, Teacher)
- Admin CRUD for students, teachers, admins, courses, notifications
- Student and teacher dashboards

## First-time admin setup

1. Start backend and open MailHog (http://localhost:8025) for the bootstrap registration code
2. Go to http://localhost:5173/register and complete registration for `admin@sms.local`
3. Sign in at http://localhost:5173/login
