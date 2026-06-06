# GCP Deployment Guide — Student Management System

Step-by-step guide to deploy the SMS microservices stack on **Google Cloud Platform (GCP)**.

> **General production concepts:** [PRODUCTION_DEPLOYMENT.md](PRODUCTION_DEPLOYMENT.md)  
> **Local development:** [LOCAL_SETUP.md](LOCAL_SETUP.md)

---

## Table of contents

1. [Recommended GCP architecture](#1-recommended-gcp-architecture)
2. [GKE vs App Engine — which to use](#2-gke-vs-app-engine--which-to-use)
3. [Prerequisites](#3-prerequisites)
4. [Phase 1 — GCP project and IAM](#phase-1--gcp-project-and-iam)
5. [Phase 2 — Networking (VPC)](#phase-2--networking-vpc)
6. [Phase 3 — Cloud SQL (PostgreSQL)](#phase-3--cloud-sql-postgresql)
7. [Phase 4 — Secret Manager](#phase-4--secret-manager)
8. [Phase 5 — Artifact Registry](#phase-5--artifact-registry)
9. [Phase 6 — GKE cluster](#phase-6--gke-cluster)
10. [Phase 7 — Deploy microservices to GKE](#phase-7--deploy-microservices-to-gke)
11. [Phase 8 — Frontend (Firebase or Cloud CDN)](#phase-8--frontend-firebase-or-cloud-cdn)
12. [Phase 9 — Load balancer, DNS, and TLS](#phase-9--load-balancer-dns-and-tls)
13. [Phase 10 — Email (SMTP)](#phase-10--email-smtp)
14. [Phase 11 — CI/CD with Cloud Build](#phase-11--cicd-with-cloud-build)
15. [Phase 12 — Monitoring, logging, and alerts](#phase-12--monitoring-logging-and-alerts)
16. [Phase 13 — Security hardening](#phase-13--security-hardening)
17. [Cost estimate](#cost-estimate)
18. [Post-deploy checklist](#post-deploy-checklist)
19. [Starter files in this repo](#starter-files-in-this-repo)

---

## 1. Recommended GCP architecture

```
Users
  │
  ▼
Cloud DNS  →  Global HTTPS Load Balancer  (+ optional Cloud Armor WAF)
  │                    │
  │                    ├── app.yourdomain.com  → Firebase Hosting / Cloud CDN (React SPA)
  │                    └── api.yourdomain.com → GKE Ingress → api-gateway Service
  │
  ▼
GKE Cluster (private nodes)
  ├── eureka-server          (ClusterIP, internal)
  ├── api-gateway            (LoadBalancer or Ingress backend)
  ├── auth-service           (ClusterIP)
  ├── admin-service          (ClusterIP)
  ├── student-service        (ClusterIP)
  ├── teacher-service        (ClusterIP)
  ├── course-service         (ClusterIP)
  └── notification-service   (ClusterIP)
          │
          ▼ (Cloud SQL Auth Proxy sidecar or Private IP)
Cloud SQL PostgreSQL 16
  └── auth_db, admin_db, student_db, teacher_db, course_db, notification_db

Artifact Registry  ←  Cloud Build (CI/CD)
Secret Manager     ←  JWT, DB password, encryption key, SMTP
Cloud Monitoring + Cloud Logging
```

### GCP services used

| GCP service | Purpose |
|-------------|---------|
| **GKE** | Run 8 backend microservices + Eureka |
| **Cloud SQL** | Managed PostgreSQL (6 databases) |
| **Artifact Registry** | Store Docker images |
| **Secret Manager** | JWT secret, DB password, encryption key |
| **Cloud Build** | CI/CD pipeline |
| **Cloud Load Balancing** | HTTPS for API and frontend |
| **Cloud DNS** | Domain records |
| **Cloud Monitoring / Logging** | Metrics, logs, alerts |
| **Firebase Hosting** or **Cloud Storage + CDN** | React static frontend |
| **IAM** | Least-privilege service accounts |
| **Cloud Armor** (optional) | WAF, rate limiting |
| **VPC + private GKE** | Network isolation |

---

## 2. GKE vs App Engine — which to use

| | **GKE (recommended)** | **App Engine** |
|--|----------------------|----------------|
| Fits 9 interdependent Java services | ✅ Excellent | ❌ Poor fit |
| Eureka service discovery | ✅ Works as-is | ⚠️ Awkward |
| Docker images you already have | ✅ Direct reuse | ⚠️ Custom runtime only |
| Independent scaling per service | ✅ Per-deployment HPA | ❌ Limited |
| Operational complexity | Medium | Low |
| Cost at small scale | ~$70–150/mo | Lower for 1 app only |

**Recommendation:** Use **GKE Autopilot** or a small **GKE Standard** cluster. App Engine is better for a single monolith, not this microservices + Eureka architecture.

**Alternative for MVP:** **Cloud Run** per service (serverless containers) — possible but requires replacing Eureka with direct URLs or a service mesh. GKE is the path of least change for this codebase.

---

## 3. Prerequisites

| Item | Details |
|------|---------|
| GCP account | With billing enabled |
| `gcloud` CLI | [Install SDK](https://cloud.google.com/sdk/docs/install) |
| `kubectl` | Installed via `gcloud components install kubectl` |
| Domain name | e.g. `yourdomain.com` |
| Git repository | Code pushed to GitHub / Cloud Source Repositories |

Set defaults (replace values):

```bash
export PROJECT_ID=sms-prod-12345
export REGION=us-central1
export ZONE=us-central1-a
export CLUSTER_NAME=sms-gke
export AR_REPO=sms-images

gcloud config set project $PROJECT_ID
gcloud config set compute/region $REGION
gcloud config set compute/zone $ZONE
```

---

## Phase 1 — GCP project and IAM

### Step 1.1 — Create project

```bash
gcloud projects create $PROJECT_ID --name="Student Management System"
gcloud billing projects link $PROJECT_ID --billing-account=YOUR_BILLING_ACCOUNT_ID
```

Or create the project in [Google Cloud Console](https://console.cloud.google.com).

### Step 1.2 — Enable required APIs

```bash
gcloud services enable \
  container.googleapis.com \
  sqladmin.googleapis.com \
  secretmanager.googleapis.com \
  artifactregistry.googleapis.com \
  cloudbuild.googleapis.com \
  servicenetworking.googleapis.com \
  compute.googleapis.com \
  dns.googleapis.com \
  monitoring.googleapis.com \
  logging.googleapis.com \
  iam.googleapis.com
```

### Step 1.3 — Create deployment service accounts

**CI/CD service account** (Cloud Build):

```bash
gcloud iam service-accounts create sms-cloudbuild \
  --display-name="SMS Cloud Build"

gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:sms-cloudbuild@${PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/container.developer"

gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:sms-cloudbuild@${PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/artifactregistry.writer"
```

**Workload service account** (pods access secrets + Cloud SQL):

```bash
gcloud iam service-accounts create sms-workload \
  --display-name="SMS GKE Workload"

gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:sms-workload@${PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"

gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:sms-workload@${PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/cloudsql.client"
```

### Step 1.4 — IAM best practices

| Practice | Action |
|----------|--------|
| Least privilege | One SA per workload; no default compute SA for apps |
| No user keys in git | Use Workload Identity, not downloaded JSON keys |
| Separate projects | `sms-dev`, `sms-staging`, `sms-prod` |
| Audit | Enable Cloud Audit Logs for admin activity |

---

## Phase 2 — Networking (VPC)

GKE and Cloud SQL should communicate over **private IP**, not the public internet.

### Step 2.1 — Reserve IP range for Cloud SQL peering

```bash
gcloud compute addresses create google-managed-services-default \
  --global \
  --purpose=VPC_PEERING \
  --prefix-length=16 \
  --network=default

gcloud services vpc-peerings connect \
  --service=servicenetworking.googleapis.com \
  --ranges=google-managed-services-default \
  --network=default
```

### Step 2.2 — Private GKE cluster (recommended)

When creating the cluster (Phase 6), use:

- **Private nodes** enabled
- **Authorized networks** for kubectl access (your office IP / Cloud Shell)
- **Workload Identity** enabled (maps K8s SA → GCP SA)

This keeps Eureka and internal services off the public internet.

---

## Phase 3 — Cloud SQL (PostgreSQL)

### Step 3.1 — Create instance

```bash
gcloud sql instances create sms-postgres \
  --database-version=POSTGRES_16 \
  --tier=db-custom-2-7680 \
  --region=$REGION \
  --network=default \
  --no-assign-ip \
  --enable-point-in-time-recovery \
  --backup-start-time=03:00 \
  --storage-auto-increase
```

| Setting | Dev/staging | Production |
|---------|-------------|------------|
| Tier | `db-f1-micro` (tiny) | `db-custom-2-7680` or higher |
| HA | Zonal | Regional (HA) |
| Backups | On | On + PITR |
| Public IP | Off | Off |

### Step 3.2 — Create database user

```bash
# Generate a strong password and store it — you will put this in Secret Manager
gcloud sql users create sms_user \
  --instance=sms-postgres \
  --password=GENERATE_STRONG_PASSWORD_HERE
```

### Step 3.3 — Create six databases

Connect via Cloud SQL Auth Proxy locally, or run:

```bash
for DB in auth_db admin_db student_db teacher_db course_db notification_db; do
  gcloud sql databases create $DB --instance=sms-postgres
done
```

SQL equivalent (from `docker/init-databases.sql`):

```sql
CREATE DATABASE auth_db;
CREATE DATABASE admin_db;
CREATE DATABASE student_db;
CREATE DATABASE teacher_db;
CREATE DATABASE course_db;
CREATE DATABASE notification_db;
```

### Step 3.4 — Connect from GKE

**Option A — Cloud SQL Auth Proxy sidecar** (simplest to start):

Add a sidecar container to each service deployment that needs DB access. See `deploy/gcp/README.md` for a deployment template.

**Option B — Private IP** (production):

- Cloud SQL has private IP on the VPC peered network
- Set `DB_HOST` to the Cloud SQL private IP address
- No proxy sidecar needed; lower latency

### Step 3.5 — Database operations checklist

- [ ] Automated daily backups enabled
- [ ] Point-in-time recovery enabled
- [ ] No public IP on Cloud SQL instance
- [ ] Plan Flyway migrations before first prod deploy (see [PRODUCTION_DEPLOYMENT.md](PRODUCTION_DEPLOYMENT.md#6-database-setup))
- [ ] Test restore procedure once

---

## Phase 4 — Secret Manager

Store all sensitive values here — never in Kubernetes manifests or git.

### Step 4.1 — Create secrets

```bash
# JWT secret (same value used by auth-service and api-gateway)
echo -n "$(openssl rand -base64 48)" | \
  gcloud secrets create jwt-secret --data-file=-

# DB password
echo -n "YOUR_STRONG_DB_PASSWORD" | \
  gcloud secrets create db-password --data-file=-

# Field encryption key (admin, student, teacher services)
echo -n "$(openssl rand -base64 32)" | \
  gcloud secrets create app-encryption-key --data-file=-

# SMTP password (if your provider uses one)
echo -n "YOUR_SMTP_PASSWORD" | \
  gcloud secrets create smtp-password --data-file=-
```

### Step 4.2 — Grant workload access

```bash
for SECRET in jwt-secret db-password app-encryption-key smtp-password; do
  gcloud secrets add-iam-policy-binding $SECRET \
    --member="serviceAccount:sms-workload@${PROJECT_ID}.iam.gserviceaccount.com" \
    --role="roles/secretmanager.secretAccessor"
done
```

### Step 4.3 — Inject secrets into pods

Use one of:

1. **External Secrets Operator** (recommended at scale)
2. **CSI Secret Store driver** for GKE
3. **Init container** that reads secrets and writes env files (starter approach in `deploy/gcp/`)

Map to application env vars:

| Secret | Env var(s) |
|--------|------------|
| `jwt-secret` | `JWT_SECRET` (auth-service, api-gateway) |
| `db-password` | `DB_PASSWORD` (all DB services) |
| `app-encryption-key` | `APP_ENCRYPTION_KEY` (admin, student, teacher) |
| `smtp-password` | `MAIL_PASSWORD` (admin-service) |

Non-secret config can stay in Kubernetes ConfigMaps:

| ConfigMap key | Example value |
|---------------|---------------|
| `DB_HOST` | Cloud SQL private IP or `127.0.0.1` (if using proxy sidecar) |
| `DB_PORT` | `5432` |
| `EUREKA_HOST` | `eureka-server` (K8s service name) |
| `REGISTRATION_BASE_URL` | `https://app.yourdomain.com/register` |
| `MAIL_HOST` | `smtp.sendgrid.net` |
| `MAIL_PORT` | `587` |
| `MAIL_FROM` | `noreply@yourdomain.com` |
| `ALLOWED_ORIGINS` | `https://app.yourdomain.com` |

---

## Phase 5 — Artifact Registry

### Step 5.1 — Create repository

```bash
gcloud artifacts repositories create $AR_REPO \
  --repository-format=docker \
  --location=$REGION \
  --description="SMS microservice images"

gcloud auth configure-docker ${REGION}-docker.pkg.dev
```

### Step 5.2 — Image naming convention

```
${REGION}-docker.pkg.dev/${PROJECT_ID}/${AR_REPO}/<service>:<tag>
```

Example:

```
us-central1-docker.pkg.dev/sms-prod-12345/sms-images/api-gateway:v1.0.0
```

### Step 5.3 — Build and push manually (first time)

```bash
cd /path/to/NewProject

SERVICES="eureka-server api-gateway auth-service admin-service \
          student-service teacher-service course-service notification-service"

for SVC in $SERVICES; do
  docker build --build-arg MODULE=$SVC \
    -t ${REGION}-docker.pkg.dev/${PROJECT_ID}/${AR_REPO}/${SVC}:latest .
  docker push ${REGION}-docker.pkg.dev/${PROJECT_ID}/${AR_REPO}/${SVC}:latest
done
```

---

## Phase 6 — GKE cluster

### Step 6.1 — Create Autopilot cluster (easiest)

```bash
gcloud container clusters create-auto $CLUSTER_NAME \
  --region=$REGION \
  --release-channel=regular \
  --network=default \
  --subnetwork=default
```

### Step 6.2 — Or Standard cluster with Workload Identity

```bash
gcloud container clusters create $CLUSTER_NAME \
  --zone=$ZONE \
  --num-nodes=2 \
  --machine-type=e2-standard-4 \
  --workload-pool=${PROJECT_ID}.svc.id.goog \
  --enable-ip-alias \
  --enable-private-nodes \
  --master-ipv4-cidr=172.16.0.0/28 \
  --enable-master-authorized-networks \
  --master-authorized-networks=YOUR_OFFICE_IP/32
```

### Step 6.3 — Get kubectl credentials

```bash
gcloud container clusters get-credentials $CLUSTER_NAME --region=$REGION
kubectl cluster-info
```

### Step 6.4 — Enable Workload Identity binding

```bash
kubectl create namespace sms

kubectl create serviceaccount sms-ksa -n sms

gcloud iam service-accounts add-iam-policy-binding \
  sms-workload@${PROJECT_ID}.iam.gserviceaccount.com \
  --role roles/iam.workloadIdentityUser \
  --member "serviceAccount:${PROJECT_ID}.svc.id.goog[sms/sms-ksa]"
```

Annotate the Kubernetes service account:

```bash
kubectl annotate serviceaccount sms-ksa -n sms \
  iam.gke.io/gcp-service-account=sms-workload@${PROJECT_ID}.iam.gserviceaccount.com
```

---

## Phase 7 — Deploy microservices to GKE

### Step 7.1 — Deployment order

Apply manifests in this order (wait for pods ready between steps):

```
1. namespace + configmaps + secrets
2. eureka-server
3. auth-service, course-service, notification-service
4. student-service, teacher-service
5. admin-service
6. api-gateway
7. ingress (public API)
```

Starter manifests live in `deploy/gcp/`. Customize image URLs and secrets before applying.

```bash
kubectl apply -f deploy/gcp/namespace.yaml
kubectl apply -f deploy/gcp/configmap.yaml
# secrets — use External Secrets or deploy/gcp/secrets guidance
kubectl apply -f deploy/gcp/eureka-server.yaml
kubectl apply -f deploy/gcp/auth-service.yaml
# ... remaining services
kubectl apply -f deploy/gcp/api-gateway.yaml
kubectl apply -f deploy/gcp/ingress.yaml
```

### Step 7.2 — Kubernetes service types

| Service | K8s Service type | Exposure |
|---------|------------------|----------|
| eureka-server | ClusterIP | Internal only |
| auth, admin, student, teacher, course, notification | ClusterIP | Internal only |
| api-gateway | ClusterIP + Ingress | Public via Ingress / LB |
| All others | No Ingress rule | Never public |

### Step 7.3 — Environment variables per service

Use the matrix from [PRODUCTION_DEPLOYMENT.md](PRODUCTION_DEPLOYMENT.md#quick-reference--environment-variable-matrix). On GKE:

- `EUREKA_HOST=eureka-server` (Kubernetes DNS name)
- `DB_HOST` = Cloud SQL private IP or `127.0.0.1` with Auth Proxy sidecar
- `SPRING_PROFILES_ACTIVE=prod` (after you add prod profiles)
- `JWT_SECRET` from Secret Manager
- Gateway: `app.security.allowed-origins=https://app.yourdomain.com`

### Step 7.4 — Health probes

Add to each deployment:

```yaml
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 10
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 90
  periodSeconds: 30
```

> **Note:** Add Spring Boot Actuator to services that do not have it yet.

### Step 7.5 — Horizontal Pod Autoscaler (api-gateway + auth)

```bash
kubectl autoscale deployment api-gateway -n sms \
  --cpu-percent=70 --min=2 --max=6

kubectl autoscale deployment auth-service -n sms \
  --cpu-percent=70 --min=2 --max=4
```

---

## Phase 8 — Frontend (Firebase or Cloud CDN)

### Option A — Firebase Hosting (simplest)

```bash
npm install -g firebase-tools
cd frontend
firebase login
firebase init hosting
# Public directory: dist
# Single-page app: Yes

VITE_API_BASE_URL=https://api.yourdomain.com npm run build
firebase deploy --only hosting
```

Map custom domain `app.yourdomain.com` in Firebase Console → Hosting → Add custom domain.

### Option B — Cloud Storage + Cloud CDN

```bash
gsutil mb -l $REGION gs://${PROJECT_ID}-sms-frontend
cd frontend && VITE_API_BASE_URL=https://api.yourdomain.com npm run build
gsutil -m cp -r dist/* gs://${PROJECT_ID}-sms-frontend/
gsutil web set -m index.html -e index.html gs://${PROJECT_ID}-sms-frontend
```

Put a backend bucket behind the same HTTPS load balancer as the API.

---

## Phase 9 — Load balancer, DNS, and TLS

### Step 9.1 — GKE Ingress for API

`deploy/gcp/ingress.yaml` creates a Google HTTP(S) Load Balancer with a managed certificate.

```yaml
# Managed certificate (example)
apiVersion: networking.gke.io/v1
kind: ManagedCertificate
metadata:
  name: sms-api-cert
  namespace: sms
spec:
  domains:
    - api.yourdomain.com
```

### Step 9.2 — Cloud DNS

```bash
gcloud dns managed-zones create sms-zone \
  --dns-name=yourdomain.com. \
  --description="SMS production DNS"

# After Ingress external IP is assigned:
gcloud dns record-sets transaction start --zone=sms-zone
gcloud dns record-sets transaction add \
  --zone=sms-zone --name=api.yourdomain.com. --type=A --ttl=300 \
  INGRESS_EXTERNAL_IP
gcloud dns record-sets transaction execute --zone=sms-zone
```

Repeat for `app.yourdomain.com` pointing to Firebase or CDN IP.

### Step 9.3 — TLS

- **GKE Ingress:** Google-managed certificates (free, auto-renew)
- **Firebase Hosting:** Automatic HTTPS
- Ensure all HTTP redirects to HTTPS

---

## Phase 10 — Email (SMTP)

MailHog is for local dev only. On GCP, use a real provider.

### Recommended providers

| Provider | Notes |
|----------|-------|
| **SendGrid** | Simple SMTP, free tier |
| **Mailgun** | Good API + SMTP |
| **SendGrid via GCP Marketplace** | Integrated billing |

### Configure admin-service

```yaml
MAIL_HOST: smtp.sendgrid.net
MAIL_PORT: "587"
MAIL_FROM: noreply@yourdomain.com
# MAIL_PASSWORD from Secret Manager (SendGrid API key)
```

Set `REGISTRATION_BASE_URL=https://app.yourdomain.com/register` on auth-service.

Add SPF, DKIM, and DMARC DNS records from your email provider.

---

## Phase 11 — CI/CD with Cloud Build

### Step 11.1 — Connect repository

In Cloud Console → Cloud Build → Triggers → Connect GitHub repository.

Or use the starter `deploy/gcp/cloudbuild.yaml` in this repo.

### Step 11.2 — Pipeline stages

```
┌──────────────┐   ┌─────────────┐   ┌──────────────┐   ┌─────────────┐
│ mvn verify   │ → │ docker build│ → │ push to AR   │ → │ kubectl     │
│ (all modules)│   │ (8 services)│   │ (Artifact Reg)│   │ rollout GKE │
└──────────────┘   └─────────────┘   └──────────────┘   └─────────────┘
        │                                                        │
        └──────────── npm run build (frontend) ──────────────────┘
                              Firebase / GCS deploy
```

### Step 11.3 — Create build trigger

```bash
gcloud builds triggers create github \
  --name=sms-deploy-main \
  --repo-name=NewProject \
  --repo-owner=YOUR_GITHUB_USER \
  --branch-pattern="^main$" \
  --build-config=deploy/gcp/cloudbuild.yaml \
  --substitutions=_REGION=$REGION,_CLUSTER=$CLUSTER_NAME,_AR_REPO=$AR_REPO
```

### Step 11.4 — Deployment environments

| Branch | Environment | Cluster / namespace |
|--------|-------------|---------------------|
| `develop` | Staging | `sms-staging` namespace |
| `main` | Production | `sms` namespace (manual approval gate) |

Use separate GCP projects for staging and production.

### Step 11.5 — Manual first deploy

```bash
gcloud builds submit --config=deploy/gcp/cloudbuild.yaml .
```

---

## Phase 12 — Monitoring, logging, and alerts

### Step 12.1 — Cloud Logging (automatic on GKE)

All container stdout/stderr is collected. Filter by:

```
resource.type="k8s_container"
resource.labels.namespace_name="sms"
resource.labels.container_name="api-gateway"
```

### Step 12.2 — Cloud Monitoring dashboards

Create dashboards for:

| Metric | Source |
|--------|--------|
| Pod CPU / memory | GKE metrics |
| HTTP request count / latency | Ingress metrics |
| 5xx error rate | Load balancer backend metrics |
| Cloud SQL connections | Cloud SQL monitoring |
| Login failures | Log-based metric from auth-service logs |

### Step 12.3 — Uptime checks

Cloud Monitoring → Uptime checks:

- `https://app.yourdomain.com` — frontend
- `https://api.yourdomain.com/actuator/health` — API health

### Step 12.4 — Alert policies

| Alert | Condition |
|-------|-----------|
| API down | Uptime check fails > 2 min |
| High 5xx rate | > 5% for 5 minutes |
| Cloud SQL disk | > 85% |
| Pod crash loop | Restart count > 3 in 10 min |
| Email send failures | Log match `Failed to send email` |

### Step 12.5 — Optional — Error Reporting

Enable [Error Reporting](https://cloud.google.com/error-reporting) for unhandled Java exceptions in logs.

### Step 12.6 — Optional — Distributed tracing

Enable [Cloud Trace](https://cloud.google.com/trace) with Micrometer Tracing in Spring Boot for end-to-end request visibility.

---

## Phase 13 — Security hardening

### Network security

| Control | Implementation |
|---------|----------------|
| Private GKE nodes | No public node IPs |
| Internal services | ClusterIP only for Eureka and microservices |
| Cloud SQL | Private IP, no public access |
| Cloud Armor | Rate limit `/api/auth/login` on load balancer |
| VPC firewall | Deny all ingress except LB and authorized kubectl |

### Application security

| Control | Action |
|---------|--------|
| Secrets | Secret Manager only; rotate quarterly |
| CORS | `app.security.allowed-origins=https://app.yourdomain.com` |
| Swagger | Disable via `SPRING_PROFILES_ACTIVE=prod` |
| JWT | 256-bit+ secret; review `jwt.expiration-ms` |
| Encryption key | `APP_ENCRYPTION_KEY` in Secret Manager |
| TLS | Managed certs on Ingress and Firebase |

### IAM security

| Control | Action |
|---------|--------|
| Workload Identity | No GCP service account keys in containers |
| Least privilege | `sms-workload` SA only has `secretAccessor` + `cloudsql.client` |
| Human access | Use Google groups + IAM roles; enable MFA |
| Audit | Review Cloud Audit Logs monthly |

### Binary Authorization (optional)

Require only Cloud Build-signed images to run on GKE.

---

## Cost estimate

Rough monthly cost for a **small production** deployment on GCP (us-central1):

| Resource | Spec | Est. cost |
|----------|------|-----------|
| GKE Autopilot | 8 services, low traffic | $70–120 |
| Cloud SQL | db-custom-2-7680, zonal | $50–80 |
| Load Balancer | 1 HTTPS LB | $18–25 |
| Artifact Registry | < 10 GB | $1–5 |
| Secret Manager | < 10 secrets | < $1 |
| Cloud Build | 100 min/month | Free tier often covers |
| Firebase Hosting | Low traffic | Free tier |
| Cloud Monitoring | Basic | Free tier |
| **Total** | | **~$140–230/month** |

Reduce cost for staging/dev: smaller SQL tier, fewer GKE replicas, single-zone.

---

## Post-deploy checklist

- [ ] All pods `Running` in `sms` namespace: `kubectl get pods -n sms`
- [ ] Eureka shows all services registered (port-forward `8761` internally)
- [ ] `https://app.yourdomain.com` loads React app
- [ ] `POST https://api.yourdomain.com/api/auth/login` works
- [ ] Bootstrap admin registration email received (real SMTP)
- [ ] Create student → email in inbox; complete registration flow
- [ ] CORS blocks requests from unknown origins
- [ ] Swagger not reachable on public API
- [ ] Cloud SQL backups and PITR verified
- [ ] Uptime checks and alerts firing correctly in test
- [ ] No secrets in git or Kubernetes manifests (only Secret Manager refs)

---

## Starter files in this repo

| Path | Purpose |
|------|---------|
| [deploy/gcp/README.md](../deploy/gcp/README.md) | Quick start for K8s manifests |
| [deploy/gcp/namespace.yaml](../deploy/gcp/namespace.yaml) | `sms` namespace + K8s service account |
| [deploy/gcp/configmap.yaml](../deploy/gcp/configmap.yaml) | Non-secret environment config |
| [deploy/gcp/eureka-server.yaml](../deploy/gcp/eureka-server.yaml) | Eureka deployment template |
| [deploy/gcp/api-gateway.yaml](../deploy/gcp/api-gateway.yaml) | Gateway deployment template |
| [deploy/gcp/ingress.yaml](../deploy/gcp/ingress.yaml) | Public API Ingress + managed cert |
| [deploy/gcp/cloudbuild.yaml](../deploy/gcp/cloudbuild.yaml) | CI/CD pipeline |

Copy and customize these templates for all eight services using the same pattern as `api-gateway.yaml`.

---

## Suggested timeline

| Week | Tasks |
|------|-------|
| **1** | GCP project, IAM, VPC, Cloud SQL, Secret Manager, Artifact Registry |
| **2** | GKE cluster, deploy Eureka + auth + gateway, verify login |
| **3** | Deploy remaining services, Ingress, DNS, TLS, frontend |
| **4** | SMTP, Cloud Build CI/CD, monitoring, alerts, staging environment |
| **5** | Security review, load test, Flyway migrations, go-live |

---

*Last updated: June 2026*
