# ModResorts – Deployment Guide (AWS EKS)

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Project Structure](#project-structure)
4. [Local Development with Docker Compose](#local-development-with-docker-compose)
5. [Build and Push Docker Image](#build-and-push-docker-image)
6. [AWS EKS Deployment](#aws-eks-deployment)
7. [Kubernetes Manifest Reference](#kubernetes-manifest-reference)
8. [Environment Variables](#environment-variables)
9. [Health Checks](#health-checks)
10. [Scaling and Management](#scaling-and-management)
11. [Troubleshooting](#troubleshooting)
12. [Security Considerations](#security-considerations)

---

## Overview

**ModResorts** is a Java EE web application (WAR) that provides resort booking, weather information, and availability checking. It is packaged as a WAR artifact built with Maven (Java 8 / Java EE 7) and deployed on a Java runtime container.

- **Application Port**: `9080`
- **Health Endpoint**: `GET /health`
- **Build Tool**: Maven 3.9.x
- **Java Version**: 8 (compiled with `maven.compiler.source=1.8`)
- **Package Type**: WAR (`modresorts-2.0.0.war`)
- **Runtime Base Image**: `amazoncorretto:8`
- **Target Platform**: AWS EKS (Kubernetes)

---

## Prerequisites

### Local Development
| Tool | Version | Purpose |
|------|---------|---------|
| Docker | 24.x+ | Build and run containers |
| Docker Compose | 2.x+ | Local multi-container orchestration |
| Java JDK | 8+ | Local compilation (optional) |
| Maven | 3.9.x | Local build (optional) |
| Node.js | 18.x | CSS/HTML minification build stage |

### AWS EKS Deployment
| Tool | Version | Purpose |
|------|---------|---------|
| AWS CLI | 2.x | AWS authentication and ECR access |
| kubectl | 1.28+ | Kubernetes cluster management |
| eksctl | 0.170+ | EKS cluster creation (optional) |

### AWS IAM Permissions Required
- `ecr:GetAuthorizationToken`
- `ecr:BatchCheckLayerAvailability`
- `ecr:GetDownloadUrlForLayer`
- `ecr:PutImage`
- `ecr:InitiateLayerUpload`
- `ecr:UploadLayerPart`
- `ecr:CompleteLayerUpload`
- `ecr:CreateRepository`
- `ecr:DescribeRepositories`
- `eks:DescribeCluster`
- `eks:UpdateKubeconfig`

---

## Project Structure

```
ModResorts/
├── Dockerfile                    # Multi-stage build (CSS/HTML minification + Maven + runtime)
├── docker-compose.yml            # Local development compose file
├── .dockerignore                 # Files excluded from Docker build context
├── pom.xml                       # Maven build descriptor (Java 8, WAR packaging)
├── package.json                  # Node.js tooling for CSS/HTML minification
├── postcss.config.js             # PostCSS configuration (PurgeCSS + cssnano)
├── src/
│   └── main/
│       ├── java/com/acme/modres/ # Java source files
│       └── resources/            # Application resources (ops.json, reservations.json)
├── WebContent/                   # Web assets (HTML, CSS, JS, JSP, images)
│   └── WEB-INF/web.xml           # Servlet configuration
├── kubernetes/
│   ├── namespace.yaml            # Kubernetes namespace
│   ├── deployment.yaml           # Kubernetes deployment (2 replicas)
│   ├── service.yaml              # ClusterIP service (port 80 → 9080)
│   └── ingress.yaml              # AWS ALB Ingress
├── scripts/
│   ├── build-push.sh             # Linux/macOS build and push script
│   ├── build-push.bat            # Windows build and push script
│   ├── deploy-image.sh           # Linux/macOS EKS deploy script
│   └── deploy-image.bat          # Windows EKS deploy script
└── docs/
    └── DEPLOYMENT.md             # This file
```

---

## Local Development with Docker Compose

### 1. Configure Environment Variables

Create a `.env` file in the project root:

```bash
# Weather API (optional – uses cached data if not set)
WEATHER_API_KEY=your_weather_api_key_here

# Redis / Amazon ElastiCache (for customer information state)
REDIS_HOST=localhost
REDIS_PORT=6379

# Service discovery (optional)
SERVICE_DISCOVERY_URL=
```

### 2. Start the Application

```bash
# Build and start
docker compose up --build

# Start in background
docker compose up --build -d

# View logs
docker compose logs -f modresorts

# Stop
docker compose down
```

### 3. Access the Application

| Endpoint | URL |
|----------|-----|
| Home page | http://localhost:9080/ |
| Health check | http://localhost:9080/health |
| Weather API | http://localhost:9080/weather?selectedCity=Paris |
| Availability | http://localhost:9080/availability |

---

## Build and Push Docker Image

### Linux / macOS

```bash
chmod +x scripts/build-push.sh
./scripts/build-push.sh
```

### Windows

```cmd
scripts\build-push.bat
```

The script will prompt you to:
1. Select registry type (AWS ECR or Docker Hub)
2. Enter registry credentials and details
3. Enter an image tag (defaults to `latest`)

The script automatically:
- Sanitizes the image name to lowercase with hyphens
- Creates the ECR repository if it does not exist (ECR only)
- Builds the Docker image from the project root
- Pushes the image to the selected registry

### Manual Build

```bash
# Build
docker build -t modresorts:latest .

# Tag for ECR
docker tag modresorts:latest 123456789012.dkr.ecr.us-east-1.amazonaws.com/modresorts:latest

# Push to ECR
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin 123456789012.dkr.ecr.us-east-1.amazonaws.com
docker push 123456789012.dkr.ecr.us-east-1.amazonaws.com/modresorts:latest
```

---

## AWS EKS Deployment

### Step 1: Configure AWS CLI

```bash
aws configure
# Enter: AWS Access Key ID, Secret Access Key, Region, Output format
```

### Step 2: Create or Connect to EKS Cluster

**Create a new cluster (if needed):**
```bash
eksctl create cluster \
  --name modresorts-cluster \
  --region us-east-1 \
  --nodegroup-name standard-workers \
  --node-type t3.medium \
  --nodes 2 \
  --nodes-min 1 \
  --nodes-max 4
```

**Connect to an existing cluster:**
```bash
aws eks update-kubeconfig --region us-east-1 --name modresorts-cluster
kubectl cluster-info
```

### Step 3: Install AWS Load Balancer Controller (for Ingress)

```bash
# Add the EKS chart repository
helm repo add eks https://aws.github.io/eks-charts
helm repo update

# Install the AWS Load Balancer Controller
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=modresorts-cluster \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller
```

> **Note**: The AWS Load Balancer Controller requires an IAM role with appropriate permissions. See the [AWS documentation](https://docs.aws.amazon.com/eks/latest/userguide/aws-load-balancer-controller.html) for setup details.

### Step 4: Deploy Using Script

**Linux / macOS:**
```bash
chmod +x scripts/deploy-image.sh
./scripts/deploy-image.sh
```

**Windows:**
```cmd
scripts\deploy-image.bat
```

The script will prompt for:
- AWS Region
- EKS Cluster Name
- Full Docker image URI (e.g., `123456789012.dkr.ecr.us-east-1.amazonaws.com/modresorts:latest`)
- Optional environment variables: `WEATHER_API_KEY`, `REDIS_HOST`, `REDIS_PORT`, `SERVICE_DISCOVERY_URL`

### Step 5: Manual Deployment

```bash
# Apply manifests in order
kubectl apply -f kubernetes/namespace.yaml
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml
kubectl apply -f kubernetes/ingress.yaml

# Wait for rollout
kubectl rollout status deployment/modresorts -n modresorts

# Verify
kubectl get pods,svc,ingress -n modresorts
```

---

## Kubernetes Manifest Reference

### namespace.yaml
Creates the `modresorts` namespace to isolate all application resources.

### deployment.yaml
- **Replicas**: 2 (for high availability)
- **Image**: Placeholder `{{IMAGE_URI}}` replaced at deploy time
- **Resources**:
  - Requests: `cpu: 250m`, `memory: 512Mi`
  - Limits: `cpu: 500m`, `memory: 1Gi`
- **Liveness Probe**: `GET /health` on port 9080 (initial delay: 60s, period: 30s)
- **Readiness Probe**: `GET /health` on port 9080 (initial delay: 30s, period: 15s)
- **JVM Options**: `-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`

### service.yaml
- **Type**: ClusterIP
- **Port mapping**: `80 → 9080`
- Routes traffic from the Ingress to the application pods

### ingress.yaml
- **Class**: AWS ALB (Application Load Balancer)
- **Scheme**: `internet-facing`
- **Target type**: `ip`
- **Health check path**: `/health`
- **Host**: `modresorts.example.com` (update to your actual domain)

**To update the ingress hostname:**
```bash
kubectl patch ingress modresorts-ingress -n modresorts \
  --type='json' \
  -p='[{"op": "replace", "path": "/spec/rules/0/host", "value": "your-domain.com"}]'
```

---

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `WEATHER_API_KEY` | No | (empty) | Weather Underground API key. If not set, cached weather data is used. |
| `REDIS_HOST` | Yes (for state) | `localhost` | Amazon ElastiCache (Redis) primary endpoint for customer information storage |
| `REDIS_PORT` | No | `6379` | Redis port |
| `SERVICE_DISCOVERY_URL` | No | (empty) | REST-based service discovery URL for container-native service lookup |
| `SERVER_DISPLAY_NAME` | No | `modresorts` | Server display name (replaces WebSphere-specific env var) |
| `SERVER_FULL_NAME` | No | `modresorts-instance` | Server full name |
| `JAVA_OPTS` | No | See Dockerfile | JVM options for memory management and container awareness |
| `TZ` | No | `UTC` | Timezone setting |

### Amazon ElastiCache (Redis) Setup

The application uses Redis (via Amazon ElastiCache) to store customer information across horizontally scaled pods. To configure:

```bash
# Create ElastiCache Redis cluster (example)
aws elasticache create-cache-cluster \
  --cache-cluster-id modresorts-redis \
  --cache-node-type cache.t3.micro \
  --engine redis \
  --num-cache-nodes 1 \
  --region us-east-1

# Get the endpoint
aws elasticache describe-cache-clusters \
  --cache-cluster-id modresorts-redis \
  --show-cache-node-info \
  --query 'CacheClusters[0].CacheNodes[0].Endpoint.Address'
```

Set `REDIS_HOST` to the ElastiCache endpoint in your deployment.

---

## Health Checks

The application exposes a dedicated health endpoint:

```
GET /health
```

**Response (HTTP 200):**
```json
{"status":"UP","application":"ModResorts"}
```

This endpoint is used by:
- Kubernetes **liveness probe** (restarts pod if unhealthy)
- Kubernetes **readiness probe** (removes pod from load balancer if not ready)
- AWS ALB health checks (via Ingress annotation)

---

## Scaling and Management

### Horizontal Scaling

```bash
# Scale to 4 replicas
kubectl scale deployment modresorts -n modresorts --replicas=4

# Check scaling status
kubectl get pods -n modresorts -w
```

### Horizontal Pod Autoscaler (HPA)

```bash
kubectl autoscale deployment modresorts \
  -n modresorts \
  --cpu-percent=70 \
  --min=2 \
  --max=10

kubectl get hpa -n modresorts
```

### Rolling Updates

```bash
# Update image
kubectl set image deployment/modresorts \
  modresorts=123456789012.dkr.ecr.us-east-1.amazonaws.com/modresorts:v2.0.1 \
  -n modresorts

# Monitor rollout
kubectl rollout status deployment/modresorts -n modresorts
```

### Rollback

```bash
# Rollback to previous version
kubectl rollout undo deployment/modresorts -n modresorts

# Rollback to specific revision
kubectl rollout history deployment/modresorts -n modresorts
kubectl rollout undo deployment/modresorts -n modresorts --to-revision=2
```

---

## Troubleshooting

### Pod Not Starting

```bash
# Check pod status
kubectl get pods -n modresorts

# Describe pod for events
kubectl describe pod <pod-name> -n modresorts

# View pod logs
kubectl logs <pod-name> -n modresorts
kubectl logs <pod-name> -n modresorts --previous  # previous container logs
```

### Common Issues

**ImagePullBackOff / ErrImagePull**
- Verify the image URI is correct
- Ensure the EKS node IAM role has ECR pull permissions (`AmazonEC2ContainerRegistryReadOnly`)
- Check ECR repository exists in the correct region

**CrashLoopBackOff**
- Check logs: `kubectl logs <pod-name> -n modresorts`
- Verify `REDIS_HOST` is reachable from the EKS cluster
- Ensure the WAR file is correctly packaged (check Maven build output)

**Liveness/Readiness Probe Failures**
- The `/health` endpoint must return HTTP 200
- Increase `initialDelaySeconds` if JVM startup is slow
- Check if the application port (9080) is correctly exposed

**Ingress Not Getting External IP**
- Verify AWS Load Balancer Controller is installed and running
- Check IAM permissions for the Load Balancer Controller service account
- Review ingress events: `kubectl describe ingress modresorts-ingress -n modresorts`

### Useful Commands

```bash
# Get all resources in namespace
kubectl get all -n modresorts

# View deployment events
kubectl describe deployment modresorts -n modresorts

# Execute shell in running pod
kubectl exec -it <pod-name> -n modresorts -- /bin/sh

# Port-forward for local testing
kubectl port-forward svc/modresorts-service 9080:80 -n modresorts
# Then access: http://localhost:9080/health

# View ingress details
kubectl describe ingress modresorts-ingress -n modresorts

# Check node resource usage
kubectl top nodes
kubectl top pods -n modresorts
```

---

## Security Considerations

1. **Non-root user**: The container runs as user `appuser` (UID 1001) to prevent privilege escalation.

2. **Secrets management**: Use Kubernetes Secrets or AWS Secrets Manager for sensitive values:
   ```bash
   kubectl create secret generic modresorts-secrets \
     --from-literal=WEATHER_API_KEY=your_key \
     -n modresorts
   ```
   Reference in deployment.yaml:
   ```yaml
   env:
     - name: WEATHER_API_KEY
       valueFrom:
         secretKeyRef:
           name: modresorts-secrets
           key: WEATHER_API_KEY
   ```

3. **Network policies**: Restrict pod-to-pod communication using Kubernetes NetworkPolicies.

4. **Image scanning**: Enable ECR image scanning to detect vulnerabilities:
   ```bash
   aws ecr put-image-scanning-configuration \
     --repository-name modresorts \
     --image-scanning-configuration scanOnPush=true \
     --region us-east-1
   ```

5. **RBAC**: Apply least-privilege IAM roles to EKS node groups and service accounts.

6. **TLS**: Configure HTTPS on the ALB Ingress by adding the certificate ARN annotation:
   ```yaml
   alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:us-east-1:123456789012:certificate/xxx
   alb.ingress.kubernetes.io/listen-ports: '[{"HTTPS": 443}]'
   ```

7. **Application security**: The `web.xml` security constraints are commented out for demo purposes. Enable them in production by uncommenting the `<security-constraint>` blocks.
