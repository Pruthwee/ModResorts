# MOD-Resorts Deployment Guide

## Overview

MOD-Resorts is a Java 8 web application packaged as a WAR file, built with Maven.
It runs on an embedded Apache Tomcat 8.5 servlet container inside a Docker container
based on `amazoncorretto:8`.

## Prerequisites

### System Requirements

- Docker 20.10 or later
- Maven 3.6 or later (for local builds)
- Java 8 (JRE 1.8+) for running the application
- AWS CLI 2 (for ECR and EKS)
- kubectl 1.24+ (for EKS deployment)
- eksctl (optional, for cluster creation)

### AWS Prerequisites

- AWS account with permissions for ECR, EKS, and IAM
- IAM user/role with `AmazonEC2ContainerRegistryFullAccess` and `AmazonEKSClusterPolicy`
- EKS cluster already created (see EKS Cluster Setup below)

## Local Setup with Docker Compose

1. Ensure Docker is running.

2. Set required environment variables:

   ```bash
   export WEATHER_API_KEY=your_weather_api_key
   ```

3. Build and start the application:

   ```bash
   docker-compose up --build
   ```

4. Access the application at `http://localhost:8080`

5. Access the health check endpoint at `http://localhost:8080/health`

## Building and Pushing the Container Image

### Using the Shell Script (Linux/macOS)

1. Ensure you have the AWS CLI installed and configured:

   ```bash
   aws configure
   ```

2. Run the build and push script:

   ```bash
   ./scripts/build-push.sh
   ```

3. Follow the prompts:
   - Select target platform (1 for AWS ECR, 2 for Docker Hub)
   - Enter AWS region (for ECR)
   - Enter ECR registry URL
   - Enter ECR repository name
   - Enter image tag (or press Enter for `latest`)

### Using the Batch Script (Windows)

1. Open Command Prompt or PowerShell as Administrator.

2. Run the build and push script:

   ```cmd
   scripts\build-push.bat
   ```

3. Follow the prompts.

### Manual Build

```bash
# Build
docker build -f Dockerfile -t <registry>/<repo>:<tag> .

# Push (after logging in)
docker push <registry>/<repo>:<tag>
```

## Deploying to AWS EKS

### EKS Cluster Setup

If you don't have an EKS cluster yet, create one:

```bash
# Create EKS cluster
eksctl create cluster \
  --name modresorts-cluster \
  --region us-east-1 \
  --nodegroup-name modresorts-nodes \
  --node-type t3.medium \
  --nodes 2 \
  --nodes-max 4 \
  --managed
```

### kubectl Configuration

Configure kubectl to point to your EKS cluster:

```bash
aws eks update-kubeconfig --region us-east-1 --name modresorts-cluster
```

Verify connectivity:

```bash
kubectl cluster-info
```

### Deployment Walkthrough

The deployment consists of four Kubernetes manifests:

#### 1. Namespace (`kubernetes/namespace.yaml`)

Creates a dedicated namespace `modresorts` for the application, isolating it from
other applications in the cluster.

#### 2. Deployment (`kubernetes/deployment.yaml`)

Creates a Deployment with 2 replicas of the MOD-Resorts container. Key configuration:

- **Image**: `{{IMAGE_URI}}` - replaced at deploy time with the full ECR image path
- **Port**: 8080 (Tomcat default)
- **Environment Variables**:
  - `JAVA_OPTS` - JVM settings for container memory limits
  - `WEATHER_API_KEY` - API key for weather data (from Secret)
- **Resources**:
  - Requests: 250m CPU, 512Mi memory
  - Limits: 500m CPU, 1Gi memory
- **Liveness Probe**: HTTP GET to `/health` on port 8080
- **Readiness Probe**: HTTP GET to `/health` on port 8080

#### 3. Service (`kubernetes/service.yaml`)

Creates a ClusterIP Service that routes traffic to the pods on port 80.

#### 4. Ingress (`kubernetes/ingress.yaml`)

Creates an AWS ALB Ingress that exposes the service externally at
`modresorts.example.com` on port 80.

### Deploy Steps

1. Ensure you have built and pushed the image to ECR.

2. Run the deploy script:

   ```bash
   ./scripts/deploy-image.sh
   ```

3. Follow the prompts:
   - Enter AWS region
   - Enter EKS cluster name
   - Enter full image URI
   - Enter environment variable values

4. Verify the deployment:

   ```bash
   kubectl get pods,svc,ingress -n modresorts
   ```

### Manual Deployment

```bash
# Configure kubectl
aws eks update-kubeconfig --region us-east-1 --name modresorts-cluster

# Apply namespace
kubectl apply -f kubernetes/namespace.yaml

# Create secrets
kubectl create secret generic modresorts-secrets -n modresorts \
  --from-literal=WEATHER_API_KEY=your_api_key \
  --dry-run=client -o yaml | kubectl apply -f -

# Replace placeholders
sed -i.bak 's|{{IMAGE_URI}}|123456789.dkr.ecr.us-east-1.amazonaws.com/modresorts:latest|g' kubernetes/deployment.yaml
sed -i.bak 's|{{WEATHER_API_KEY}}|your_api_key|g' kubernetes/deployment.yaml
rm -f kubernetes/deployment.yaml.bak

# Apply manifests
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml
kubectl apply -f kubernetes/ingress.yaml

# Wait for rollout
kubectl rollout status deployment/modresorts -n modresorts
```

## Configuration Management

The application reads configuration from environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `WEATHER_API_KEY` | API key for weather data service | None (falls back to default data) |
| `JAVA_OPTS` | JVM options | `-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0` |
| `NAMING_PROVIDER_URL` | JNDI provider URL | `file:///` |
| `CATALINA_HOME` | Tomcat installation directory | `/opt/tomcat` |

### Secrets Management

Sensitive values (like `WEATHER_API_KEY`) should be stored in Kubernetes Secrets,
not in plain text. The deploy script creates a Secret named `modresorts-secrets`
in the `modresorts` namespace.

## Security Considerations

1. **Non-root user**: The container runs as a non-root `tomcat` user.
2. **Secrets**: Credentials are stored in Kubernetes Secrets, never in plain text.
3. **Minimal Tomcat**: Unused Tomcat webapps (examples, docs, manager, host-manager)
   are removed from the image.
4. **No package manager**: The runtime image does not include a package manager.
5. **Health checks**: The application exposes a `/health` endpoint for liveness
   and readiness probes.

## Troubleshooting

### Pod Failures

```bash
# Check pod logs
kubectl logs -n modresorts <pod-name>

# Describe pod for events
kubectl describe pod -n modresorts <pod-name>

# Check if pod is crash-looping
kubectl get pods -n modresorts
```

### Service Issues

```bash
# Check service endpoints
kubectl get endpoints -n modresorts

# Test connectivity from within a pod
kubectl exec -it -n modresorts <pod-name> -- curl http://modresorts-service:80/health
```

### Ingress Problems

```bash
# Check ingress status
kubectl get ingress -n modresorts

# Check ingress controller logs
kubectl logs -n kube-system <alb-ingress-controller-pod>
```

### Rollback

```bash
kubectl rollout undo deployment/modresorts -n modresorts
```

## Scaling and Management

### Horizontal Pod Autoscaler

```bash
kubectl autoscale deployment/modresorts -n modresorts \
  --cpu-percent=60 \
  --min=2 \
  --max=10
```

### Rolling Updates

```bash
# Update the image
kubectl set image deployment/modresorts -n modresorts \
  modresorts=<new-image-uri>

# Monitor rollout
kubectl rollout status deployment/modresorts -n modresorts
```

## Java Notes

### JVM Settings

The container uses the following JVM settings:

- `-Xmx512m` - Maximum heap size of 512MB (within the 1Gi memory limit)
- `-Xms256m` - Initial heap size of 256MB
- `-XX:+UseContainerSupport` - Enables container-aware memory limits (Java 8u191+)
- `-XX:MaxRAMPercentage=75.0` - Uses 75% of container memory for heap

These settings are tuned for the Kubernetes resource limits (512Mi request, 1Gi limit).
The JVM will use up to 768MB of the 1Gi limit for heap, leaving room for non-heap memory.

### Health Endpoint

The application exposes a `/health` endpoint that returns `{"status":"UP"}` with HTTP 200.
This endpoint is used for Kubernetes liveness and readiness probes.

### Servlet Container

The application runs on Apache Tomcat 8.5.99, which is compatible with Java 8 and
the `javax.servlet` API. The WAR file is deployed as `ROOT.war` so it serves on the
root context path.
