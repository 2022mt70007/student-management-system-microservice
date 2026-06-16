# Go Live on GCP — VM + Docker Compose (M.Tech Demo)

Step-by-step guide to deploy SMS on a **single GCP VM** without a domain, without Jenkins, and with **AWS SES** for email.

> **Prerequisites:** GCP CLI installed, AWS SES working locally, project code on GitHub or your PC.

---

## Overview

```
Your PC (gcloud CLI)
        │
        ▼
GCP Compute Engine VM (Ubuntu)
  ├── docker compose up  →  9 backend containers
  ├── frontend build     →  port 5173 or nginx :80
  └── .env               →  AWS SES + secrets

AWS SES (email)          ←  admin-service SMTP
```

**No domain needed.** Access via `http://VM_EXTERNAL_IP:5173` and `http://VM_EXTERNAL_IP:8080`.

---

## Step 1 — Login and set up GCP project

Open **PowerShell** on your PC:

```powershell
gcloud auth login
gcloud auth application-default login
```

Create a project (or use existing):

```powershell
$PROJECT_ID = "sms-mtech-demo-12345"   # change to something unique
gcloud projects create $PROJECT_ID --name="SMS MTech Demo"
gcloud billing accounts list
# Link billing (required — uses free $300 credit):
gcloud billing projects link $PROJECT_ID --billing-account=YOUR_BILLING_ACCOUNT_ID
gcloud config set project $PROJECT_ID
```

Enable Compute Engine API:

```powershell
gcloud services enable compute.googleapis.com
```

---

## Step 2 — Create firewall rules

Allow HTTP traffic to API and frontend:

```powershell
gcloud compute firewall-rules create sms-allow-api `
  --allow=tcp:8080 `
  --target-tags=sms-server `
  --description="SMS API Gateway"

gcloud compute firewall-rules create sms-allow-frontend `
  --allow=tcp:5173 `
  --target-tags=sms-server `
  --description="SMS React frontend"

# Optional: SSH only from your IP (recommended)
gcloud compute firewall-rules create sms-allow-ssh `
  --allow=tcp:22 `
  --target-tags=sms-server `
  --description="SSH access"
```

---

## Step 3 — Create the VM

```powershell
$ZONE = "asia-south1-a"   # Mumbai — close to AWS SES region

gcloud compute instances create sms-vm `
  --zone=$ZONE `
  --machine-type=e2-medium `
  --image-family=ubuntu-2204-lts `
  --image-project=ubuntu-os-cloud `
  --boot-disk-size=30GB `
  --tags=sms-server
```

Get the external IP:

```powershell
gcloud compute instances describe sms-vm --zone=$ZONE --format="get(networkInterfaces[0].accessConfigs[0].natIP)"
```

Save this IP — e.g. `34.x.x.x`. Call it `$VM_IP` below.

---

## Step 4 — SSH into the VM

```powershell
gcloud compute ssh sms-vm --zone=$ZONE
```

You are now on the Ubuntu VM.

---

## Step 5 — Install Docker on the VM

Run **inside the VM** (SSH session):

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin git

sudo usermod -aG docker $USER
# Log out and back in for group to apply:
exit
```

SSH in again:

```powershell
gcloud compute ssh sms-vm --zone=$ZONE
```

Verify:

```bash
docker --version
docker compose version
```

---

## Step 6 — Clone your project on the VM

```bash
git clone https://github.com/YOUR_USER/NewProject.git
cd NewProject
```

If the repo is private, use a personal access token or `gcloud compute scp` to copy files from your PC (Step 6b).

### Step 6b — Alternative: copy from your PC (if not on GitHub)

From **PowerShell on your PC**:

```powershell
gcloud compute scp --recurse D:\NewProject sms-vm:~/NewProject --zone=asia-south1-a
```

Then SSH in and `cd ~/NewProject`.

---

## Step 7 — Create `.env` on the VM

```bash
nano .env
```

Paste (use your real values):

```env
# AWS SES (Mumbai)
SES_MAIL_HOST=email-smtp.ap-south-1.amazonaws.com
SES_MAIL_PORT=587
SES_SMTP_USERNAME=YOUR_SES_SMTP_USERNAME
SES_SMTP_PASSWORD="YOUR_SES_SMTP_PASSWORD"
MAIL_FROM=2022mt70007@wilp.bits-pilani.ac.in

# Secrets — generate strong values, not dev defaults
JWT_SECRET=CHANGE_TO_LONG_RANDOM_STRING_64_CHARS
APP_ENCRYPTION_KEY=CHANGE_TO_32_BYTE_RANDOM_KEY

MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
```

Save: `Ctrl+O`, Enter, `Ctrl+X`.

**Important:** Replace `JWT_SECRET` and `APP_ENCRYPTION_KEY` with new random values for cloud deployment.

---

## Step 8 — Update docker-compose for cloud URLs

Edit `docker-compose.yml` on the VM — update **auth-service** environment:

```yaml
REGISTRATION_BASE_URL: http://YOUR_VM_IP:5173/register
```

Replace `YOUR_VM_IP` with your actual external IP (e.g. `34.56.78.90`).

Also update **api-gateway** CORS (add env or rebuild with application config). Set in `api-gateway` environment if you add support, or use Firebase later.

For quick demo, add to `api-gateway` in docker-compose:

```yaml
APP_SECURITY_ALLOWED_ORIGINS: http://YOUR_VM_IP:5173
```

Check if gateway reads this — from CorsConfig: `app.security.allowed-origins`. Spring maps:

```yaml
environment:
  APP_SECURITY_ALLOWED_ORIGINS: http://34.x.x.x:5173
```

---

## Step 9 — Start the backend

```bash
cd ~/NewProject   # or ~/NewProject
docker compose up --build -d
```

First run takes **10–20 minutes** (Maven build inside Docker).

Check status:

```bash
docker compose ps
docker logs sms-admin --tail 20
```

All containers should be `Up`.

---

## Step 10 — Get bootstrap admin registration code

```bash
docker logs sms-admin | grep -i "registration code"
```

Or:

```bash
docker logs sms-admin --tail 30
```

Look for `Registration code: XXXXXXX`.

Register admin at: `http://YOUR_VM_IP:5173/register`  
(Frontend must be running — Step 11.)

---

## Step 11 — Build and run the frontend on the VM

```bash
# Install Node on VM
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt-get install -y nodejs

cd ~/NewProject/frontend
npm install
VITE_API_BASE_URL=http://YOUR_VM_IP:8080 npm run build
```

Run frontend (simple — for demo):

```bash
npm install -g serve
serve -s dist -l 5173
```

Keep this terminal open, or run in background:

```bash
nohup serve -s dist -l 5173 > ~/frontend.log 2>&1 &
```

---

## Step 12 — Open in browser

From your PC browser:

| Page | URL |
|------|-----|
| Frontend | `http://YOUR_VM_IP:5173` |
| API Gateway | `http://YOUR_VM_IP:8080` |

1. Register admin (`http://YOUR_VM_IP:5173/register`) with code from logs
2. Login as admin
3. Create student with **SES-verified** email
4. Check inbox for registration email

---

## Step 13 — Verify everything works

```bash
# On VM
docker compose ps
docker logs sms-admin --tail 10   # "Registration email sent to ..."
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/auth/login
```

Checklist:

- [ ] All 10 containers running
- [ ] Frontend loads in browser
- [ ] Admin login works
- [ ] Create student → email received (AWS SES)
- [ ] Student can register and login

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Can't open `http://VM_IP:5173` | Check firewall rules (Step 2) and `serve` is running |
| CORS error in browser | Set `APP_SECURITY_ALLOWED_ORIGINS=http://VM_IP:5173` on gateway, rebuild |
| Email fails | Check `.env` SES creds; verify recipient in AWS SES sandbox |
| `docker compose` build slow | Normal first time; e2-medium may need 15–20 min |
| Out of memory | Upgrade to `e2-standard-2` (8 GB RAM) |

---

## Stop / restart

```bash
docker compose down          # stop backend
docker compose up -d         # start again
# frontend: kill serve process and re-run
```

---

## Cost

| Resource | Approx. monthly |
|----------|-----------------|
| e2-medium VM (Mumbai) | ~$25–35 |
| 30 GB disk | ~$3 |
| **With $300 free credit** | Often **$0 for months** |

Stop VM when not demoing to save money:

```powershell
gcloud compute instances stop sms-vm --zone=asia-south1-a
```

Start again:

```powershell
gcloud compute instances start sms-vm --zone=asia-south1-a
```

---

## What you are NOT doing (fine for M.Tech)

- Jenkins / CI/CD (manual deploy is OK)
- Custom domain (using VM IP)
- GKE / Kubernetes (optional for report architecture diagram only)
- MailHog (using AWS SES)

---

## Next steps after go-live

1. Take screenshots for report (login, admin dashboard, email, registration)
2. Document VM IP and architecture in report
3. Stop VM when not needed to save credits

---

*See also: [GCP_DEPLOYMENT.md](GCP_DEPLOYMENT.md) (full GKE path) · [ARCHITECTURE.md](ARCHITECTURE.md) (diagrams)*
