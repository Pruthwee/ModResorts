# ModResorts Deployment Guide

## Overview

ModResorts is a Java web application built with Spring Boot, running on Java 8 with an embedded Tomcat server. This guide covers local development with Docker Compose, building and pushing container images to AWS ECR, and deploying to AWS EKS.

## Prerequisites and System Requirements

### For Local Development with Docker Compose
- Docker Engine 20.10+ or Docker Desktop
- Docker Compose 2.0+
- Java 8 (for local builds without Docker)
- Maven 3.8+ (for local builds without Docker)

### For AWS EKS Deployment
- Docker Engine 20.10+
- AWS CLI 2.x
- kubectl 1.24+
- eksctl 0.140+
- IAM permissions for ECR and EKS

## Local Setup with Docker Compose

1. Set the required environment variables:
   ```bash
   export WEATHER_API_KEY="your-weather-api-key"
   export DB_HOST="your-db-host"
   export DB_PORT="5432"
   export DB_NAME="modresorts"
   export DB_USER="modresorts_user"
   export DB_PASSWORD="your-db-password"
   ```

2. Build and run locally:
   ```bash
   docker compose up --build
   ```

3. Access the application at `http://localhost:8080`

4. Check the health endpoint at `http://localhost:8080/health`

## Building and Pushing Container Images

### Using the Shell Script (Linux/macOS)

```bash
./scripts/build-push.sh
```

The script will:
1. Ask you to choose between AWS ECR or Docker Hub
2. For AWS ECR: ask for AWS region and account ID, log in to ECR, create the repository if needed
3. For Docker Hub: ask for your Docker Hub credentials
4. Ask for an image tag (press Enter for `latest`)
5. Build the Docker image using the Dockerfile
6. Push the image to the chosen registry

### Using the Batch Script (Windows)

```cmd
scripts\build-push.bat
```

Follow the same prompts as the shell script.

### Manual Build

```bash
# For AWS ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com
aws ecr create-repository --repository-name modresorts --region us-east-1
docker build -f Dockerfile -t <account-id>.dkr.ecr.us-east-1.amazonaws.com/modresorts:latest .
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/modresorts:latest
```

## AWS EKS Deployment

### EKS Cluster Setup

1. Create an EKS cluster (using eksctl):
   ```bash
   eksctl create cluster --name modresorts-cluster --region us-east-1 --nodegroup-name modresorts-nodes --node-type t3.medium --nodes 2 --nodes-max 4
   ```

2. Configure kubectl:
   ```bash
   aws eks update-kubeconfig --region us-east-1 --name modresorts-cluster
   ```

3. Verify cluster connectivity:
   ```bash
   kubectl cluster-info
   ```

### Deploying to EKS

1. Run the deployment script:
   ```bash
   ./scripts/deploy-image.sh
   ```

2. The script will ask for:
   - AWS region
   - EKS cluster name
   - Image URI (the full path with registry and tag)
   - Environment variable values (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD, WEATHER_API_KEY)

3. The script will:
   - Configure kubectl for your EKS cluster
   - Apply the namespace manifest
   - Create Kubernetes secrets for credentials
   - Replace placeholders in the deployment manifest
   - Apply deployment, service, and ingress manifests
   - Wait for the deployment rollout to complete
   - Show the deployment status

### Manual Deployment

```bash
# Create namespace
kubectl apply -f kubernetes/namespace.yaml

# Create secrets
kubectl create secret generic modresorts-secrets -n modresorts \
  --from-literal=db-password=$DB_PASSWORD \
  --from-literal=db-user=$DB_USER \
  --from-literal=weather-api-key=$WEATHER_API_KEY \
  --from-literal=db-host=$DB_HOST \
  --from-literal=db-port=$DB_PORT \
  --from-literal=db-name=$DB_NAME \
  --dry-run=client -o yaml | kubectl apply -f -

# Update deployment.yaml with your image URI
sed -i 's|{{IMAGE_URI}}|<account-id>.dkr.ecr.us-east-1.amazonaws.com/modresorts:latest|g' kubernetes/deployment.yaml
sed -i 's|{{DB_HOST}}|$DB_HOST|g' kubernetes/deployment.yaml
sed -i 's|{{DB_PORT}}|$DB_PORT|g' kubernetes/deployment.yaml
sed -i 's|{{DB_NAME}}|$DB_NAME|g' kubernetes/deployment.yaml

# Apply manifests
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml
kubectl apply -f kubernetes/ingress.yaml

# Wait for rollout
kubectl rollout status deployment/modresorts -n modresorts
```

### Kubernetes Manifests

The `kubernetes/` directory contains:

- **namespace.yaml**: Creates the `modresorts` namespace
- **deployment.yaml**: Defines the Deployment with 2 replicas, resource limits, liveness/readiness probes, and environment variables. Credentials are sourced from a Kubernetes Secret.
- **service.yaml**: Exposes the application via a ClusterIP service on port 80
- **ingress.yaml**: Configures an AWS ALB ingress for external access

### Accessing the Application

After deployment, the application is accessible at:
```
https://modresorts.example.com
```

To find the actual ingress URL:
```bash
kubectl get ingress -n modresorts
```

## Configuration Management

The application reads configuration from environment variables:

| Variable | Description |
|----------|-------------|
| `SPRING_PROFILES_ACTIVE` | Spring profile to use (default: `prod`) |
| `JAVA_OPTS` | JVM options for memory and performance tuning |
| `WEATHER_API_KEY` | API key for Weather Underground service (stored in Kubernetes Secret) |
| `DB_HOST` | Database host address |
| `DB_PORT` | Database port |
| `DB_NAME` | Database name |
| `DB_USER` | Database username (stored in Kubernetes Secret) |
| `DB_PASSWORD` | Database password (stored in Kubernetes Secret) |

## Security Considerations and Best Practices

1. **Secrets Management**: All credentials (DB_PASSWORD, DB_USER, WEATHER_API_KEY) are stored in Kubernetes Secrets, never in plain text in the deployment manifests.
2. **Non-root User**: The container runs as a non-root user (`modresorts`).
3. **Minimal Base Image**: The runtime uses `amazoncorretto:8` as a minimal base image.
4. **Resource Limits**: CPU and memory limits are set to prevent resource exhaustion.
5. **Health Checks**: Liveness and readiness probes are configured to ensure traffic is only routed to healthy pods.
6. **Image Scanning**: Scan container images for vulnerabilities before pushing to ECR.

## Java Notes

### JVM Settings

The container uses the following JVM settings:
- `-Xmx512m`: Maximum heap size of 512MB
- `-Xms256m`: Initial heap size of 256MB
- `-XX:+UseContainerSupport`: Enables container-aware memory management
- `-XX:MaxRAMPercentage=75.0`: Uses up to 75% of the container's memory limit

These settings are configured for the container's resource limits:
- Requests: CPU 250m, Memory 512Mi
- Limits: CPU 500m, Memory 1Gi

### Spring Profiles

The application uses Spring profiles for environment-specific configuration. The `SPRING_PROFILES_ACTIVE` environment variable controls which profile is active. The default profile set in the Dockerfile is `prod`.

### Health Endpoint

The application exposes a health check endpoint at `/health` that returns a JSON response with the application status. This endpoint is used by Kubernetes liveness and readiness probes:

```json
{"status": "UP", "application": "ModResorts"}
```

### Troubleshooting

#### Pod Failures
- Check pod logs: `kubectl logs <pod-name> -n modresorts`
- Describe pod for events: `kubectl describe pod <pod-name> -n modresorts`
- Check pod status: `kubectl get pods -n modresorts`

#### Service Issues
- Verify service endpoints: `kubectl get endpoints modresorts-service -n modresorts`
- Check service details: `kubectl describe service modresorts-service -n modresorts`

#### Ingress Problems
- Check ingress status: `kubectl get ingress -n modresorts`
- Describe ingress: `kubectl describe ingress modresorts-ingress -n modresorts`
- Ensure the AWS Load Balancer Controller is installed and running

### Scaling and Management

#### Horizontal Pod Autoscaler (HPA)

To enable auto-scaling based on CPU utilization:

```bash
kubectl autoscale deployment modresorts --cpu-percent=60 --min=2 --max=10 -n modresorts
```

#### Rolling Updates

To update the application with a new image:

```bash
# Update the image in the deployment
kubectl set image deployment/modresorts modresorts=<new-image-uri> -n modresorts

# Monitor the rollout
kubectl rollout status deployment/modresorts -n modresorts
```

#### Rollback

If a deployment fails, roll back to the previous version:

```bash
kubectl rollout undo deployment/modresorts -n modresorts
```

To roll back to a specific revision:

```bash
kubectl rollout history deployment/modresorts -n modresorts
kubectl rollout undo deployment/modresorts --to-revision=<revision> -n modresorts
```
