# GCP Kubernetes Starter Manifests

Templates for deploying SMS on **GKE**. Full step-by-step guide: [docs/GCP_DEPLOYMENT.md](../../docs/GCP_DEPLOYMENT.md).

## Before you apply

1. Replace placeholders:
   - `PROJECT_ID` — your GCP project ID
   - `REGION` — e.g. `us-central1`
   - `yourdomain.com` — your real domain
   - Cloud SQL connection details in `configmap.yaml`
2. Create secrets in **Secret Manager** (see GCP guide Phase 4).
3. Build and push images to Artifact Registry (see GCP guide Phase 5).
4. Install **External Secrets Operator** or mount secrets manually before deploying app services.

## Apply order

```bash
export PROJECT_ID=your-gcp-project
export REGION=us-central1
gcloud container clusters get-credentials sms-gke --region=$REGION

kubectl apply -f namespace.yaml
kubectl apply -f configmap.yaml
kubectl apply -f eureka-server.yaml
# Apply auth, course, notification, student, teacher, admin (copy api-gateway pattern)
kubectl apply -f api-gateway.yaml
kubectl apply -f ingress.yaml
```

## Cloud SQL Auth Proxy sidecar

For services that need a database, add this sidecar to the pod spec:

```yaml
- name: cloud-sql-proxy
  image: gcr.io/cloud-sql-connectors/cloud-sql-proxy:2.14.3
  args:
    - "--structured-logs"
    - "--port=5432"
    - "PROJECT_ID:REGION:sms-postgres"
  securityContext:
    runAsNonRoot: true
```

Set `DB_HOST=127.0.0.1` in the app container when using the sidecar.

## CI/CD

```bash
gcloud builds submit --config=cloudbuild.yaml .
```

Customize `cloudbuild.yaml` substitutions for your project, region, and cluster name.
