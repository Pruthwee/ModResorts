# ModResorts - Deployment Guide

## Overview

ModResorts is a Java 8 web application built with Maven and packaged as a Spring Boot JAR with an embedded Tomcat server. This guide covers building, pushing, and deploying the application container to AWS EKS (Elastic Kubernetes Service).

## Prerequisites

### System Requirements
- Java 8 (JDK)
- Maven 3.6+
- Docker
- AWS CLI
- kubectl
- eksctl (optional, for cluster creation)

### AWS Prerequisites
- AWS CLI configured with credentials (`aws configure`)
- IAM permissions for ECR, EKS, and EC2
- An EKS cluster (see [Cluster Setup](#eks-cluster-setup) below)

## Local Setup with Docker Compose

### Build and Run Locally

```bash
# Build the application
mvn clean package -DskipTests

# Run with Docker Compose
docker-compose up --build
```

The application will be available at `http://localhost:8080`.

### Environment Variables

The application reads the following environment variables:

| Variable | Description | Required |
|----------|-------------|----------|
| `SPRING_PROFILES_ACTIVE` | Spring profile to activate (default: prod) | No |
| `WEATHER_API_KEY` | API key for weather data (stored in Kubernetes Secret) | Yes |
| `JNDI_FACTORY_INITIAL` | JNDI factory class for directory service | No |
| `JNDI_PROVIDER_URL` | JNDI provider URL | No |
| `SERVER_DISPLAY_NAME` | Server display name (replaces WebSphere ServerName) | No |
| `SERVER_FULL_NAME` | Server full name (replaces WebSphere ServerName) | No |

## Building and Pushing the Container Image

### Using build-push.sh (Linux/macOS)

```bash
chmod +x scripts/build-push.sh
./scripts/build-push.sh
```

The script will:
1. Ask you to select AWS ECR or Docker Hub as the target platform
2. Prompt for the registry details (region, account ID for ECR; username, password for Docker Hub)
3. Prompt for an image tag (press Enter for `latest`)
4. Build the Docker image
5. Push the image to the selected registry

### Using build-push.bat (Windows)

```cmd
scripts\build-push.bat
```

Follow the same prompts as the shell script.

### Docker Build Command

```bash
docker build -f Dockerfile -t <registry>/<repo>:<tag> .
```

The build uses a multi-stage Dockerfile:
- **Build stage**: `maven:3.9.4-eclipse-temurin-8` builds the JAR with Maven
- **Runtime stage**: `eclipse-temurin:8-jre` runs the JAR with a non-root user

## AWS EKS Deployment

### EKS Cluster Setup

If you don't have an EKS cluster, create one using eksctl:

```bash
eksctl create cluster \
  --name <cluster-name> \
  --region <region> \
  --nodegroup-name standard-workers \
  --node-type t3.medium \
  --nodes 2 \
  --nodes-max 4
```

### kubectl Configuration

Configure kubectl to communicate with your EKS cluster:

```bash
aws eks update-kubeconfig --region <region> --name <cluster-name>
```

Verify the configuration:

```bash
kubectl cluster-info
```

### Deploying to EKS

Use the deploy-image script to deploy the application:

```bash
chmod +x scripts/deploy-image.sh
./scripts/deploy-image.sh
```

The script will:
1. Prompt for AWS region and EKS cluster name
2. Prompt for the full image URI (from build-push.sh)
3. Prompt for environment variable values (press Enter to skip)
4. Prompt for the WEATHER_API_KEY credential (hidden input)
5. Configure kubectl for the EKS cluster
6. Create the namespace
7. Create the Kubernetes Secret with credentials
8. Apply the deployment, service, and ingress manifests
9. Wait for the deployment to roll out
10. Display the deployment status and application URL

### Deployment Walkthrough

The Kubernetes manifests in `kubernetes/` define:

1. **namespace.yaml**: Creates a dedicated namespace for the application
2. **deployment.yaml**: Defines the Deployment with 2 replicas, resource limits, liveness/readiness probes, and environment variables
3. **service.yaml**: Exposes the application internally on port 80
4. **ingress.yaml**: Configures an AWS Load Balancer (ALB) ingress for external access

### Troubleshooting

#### Pod Failures
```bash
# Check pod status
kubectl get pods -n modresorts

# Check pod logs
kubectl logs <pod-name> -n modresorts

# Describe pod for events
kubectl describe pod <pod-name> -n modresorts
```

Common causes:
- Image pull failures: Verify the image URI and ECR authentication
- Missing secrets: Verify the Secret was created with all required keys
- Resource limits: Check if the pod is being OOMKilled

#### Service Issues
```bash
# Check service
kubectl get svc -n modresorts

# Test connectivity from within the cluster
kubectl exec -it <pod-name> -n modresorts -- curl http://modresorts-service:8080/actuator/health
```

#### Ingress Problems
```bash
# Check ingress
kubectl get ingress -n modresorts

# Check ALB
kubectl get ingress -n modresorts -o wide
```

Ensure the AWS Load Balancer Controller is installed in your EKS cluster:
```bash
eksctl utils associate-iam-oidc-provider --region <region> --cluster <cluster-name> --approve
```

### Scaling and Management

#### Horizontal Pod Autoscaler (HPA)

To enable auto-scaling based on CPU utilization:

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: modresorts-hpa
  namespace: modresorts
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: modresorts
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

Apply with:
```bash
kubectl apply -f hpa.yaml -n modresorts
```

#### Rolling Updates

```bash
# Update the image
kubectl set image deployment/modresorts modresorts=<new-image-uri> -n modresorts

# Monitor rollout
kubectl rollout status deployment/modresorts -n modresorts

# Rollback if needed
kubectl rollout undo deployment/modresorts -n modresorts
```

## Configuration Management

The application uses environment variables for configuration, following the 12-factor app methodology. No credentials are stored in the container image.

### Kubernetes Secrets

Credentials are stored in a Kubernetes Secret named `modresorts-secrets` in the `modresorts` namespace. The secret is created during deployment with:

```bash
kubectl create secret generic modresorts-secrets -n modresorts \
  --from-literal=weather-api-key=<value> \
  --dry-run=client -o yaml | kubectl apply -f -
```

### ConfigMaps

Non-sensitive configuration is passed through environment variables in the Deployment manifest. Use a Kubernetes ConfigMap for configuration that needs to be shared across multiple deployments.

## Security Considerations

- **Non-root user**: The container runs as a non-root user for security
- **Secrets management**: All credentials are stored in Kubernetes Secrets, never in the image
- **Health endpoint**: The application exposes `/actuator/health` for liveness and readiness probes
- **Resource limits**: CPU and memory limits are set to prevent resource exhaustion
- **Image scanning**: Scan the container image for vulnerabilities before pushing to ECR
- **Network policies**: Consider adding Kubernetes NetworkPolicy to restrict pod-to-pod traffic

## Java Notes

### JVM Settings

The container uses the following JVM settings:
- `-Xmx512m -Xms256m`: Heap size limits appropriate for the container's memory limits
- `-XX:+UseContainerSupport`: Enables container-aware JVM features
- `-XX:MaxRAMPercentage=75.0`: Uses 75% of the container's memory limit for the heap

These settings are tuned for the Kubernetes resource limits (512Mi requests, 1Gi limits).

### Spring Profiles

The application uses Spring profiles for environment-specific configuration. The `SPRING_PROFILES_ACTIVE=prod` environment variable activates the production profile. Profile-specific configuration can be provided via `application-prod.properties` or environment variables.

### Health Endpoints

Spring Boot Actuator provides the following health endpoints:
- `/actuator/health`: Overall health check
- `/actuator/health/liveness`: Liveness probe (whether the app should be restarted)
- `/actuator/health/readiness`: Readiness probe (whether the app is ready to serve traffic)

These endpoints are used by the Kubernetes liveness and readiness probes defined in the Deployment manifest.
