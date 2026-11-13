# ModResorts - Deployment Guide

This document describes how to build, deploy, and manage the ModResorts application on AWS EKS.

## Prerequisites

### System Requirements
- Java 8 (JDK)
- Maven 3.6+
- Docker 20.10+
- AWS CLI 2.x
- kubectl 1.21+
- eksctl (optional, for cluster creation)

### AWS EKS Prerequisites
- An AWS account with permissions to create EKS clusters, ECR repositories, and IAM roles
- IAM permissions: `eks:CreateCluster`, `eks:DescribeCluster`, `ecr:*`, `iam:PassRole`
- An existing EKS cluster or create one using `eksctl`

## Local Setup with Docker Compose

To run the application locally using Docker Compose:

```bash
# Set the required environment variable
export WEATHER_API_KEY="your-weather-api-key"

# Build and start the application
docker-compose up --build
```

The application will be available at `http://localhost:8080`.

### Environment Variables

| Variable | Description | Required | Default |
|---|---|---|---|
| `WEATHER_API_KEY` | API key for the Weather Underground service | Yes | - |
| `NAMING_PROVIDER_URL` | JNDI naming provider URL | No | `localhost:1099` |
| `NAMING_FACTORY_INITIAL` | JNDI initial context factory class | No | - |
| `DATASOURCE_JNDI` | JNDI name for the datasource | No | `java:comp/env/jdbc/ModResortsJndi` |

## Building and Pushing the Container Image

### Using build-push.sh (Linux/macOS)

```bash
chmod +x scripts/build-push.sh
./scripts/build-push.sh
```

The script will:
1. Ask you to choose a target platform (AWS ECR or Docker Hub)
2. Ask for the image tag
3. Ask for platform-specific details
4. Build the Docker image
5. Log in to the chosen registry
6. Push the image

### Using build-push.bat (Windows)

```cmd
scripts\build-push.bat
```

### AWS ECR Notes
- The script will create the ECR repository if it does not exist
- You need AWS CLI configured with appropriate permissions
- The script uses `aws ecr get-login-password` for authentication

## AWS EKS Deployment

### EKS Cluster Setup

If you do not have an EKS cluster, create one:

```bash
eksctl create cluster --name modresorts-cluster --region us-east-1 --nodegroup-name modresorts-nodes --node-type t3.medium --nodes 2 --nodes-max 4
```

### Configure kubectl

```bash
aws eks update-kubeconfig --region us-east-1 --name modresorts-cluster
```

### Deploy the Application

Use the deploy script:

```bash
chmod +x scripts/deploy-image.sh
./scripts/deploy-image.sh
```

The script will:
1. Ask for AWS region and EKS cluster name
2. Ask for the full image URI (from ECR)
3. Ask for environment variable values
4. Configure kubectl for the EKS cluster
5. Apply the namespace, secrets, deployment, service, and ingress
6. Wait for the rollout to complete

### Manual Deployment

You can also deploy manually:

```bash
# Apply namespace
kubectl apply -f kubernetes/namespace.yaml

# Create secrets for credentials
kubectl create secret generic modresorts-secrets -n modresorts \
  --from-literal=weather-api-key="your-weather-api-key" \
  --dry-run=client -o yaml | kubectl apply -f -

# Replace placeholders in deployment.yaml
sed -i.bak 's|{{IMAGE_URI}}|123456789.dkr.ecr.us-east-1.amazonaws.com/modresorts:latest|g' kubernetes/deployment.yaml
sed -i.bak 's|{{NAMING_PROVIDER_URL}}|your-naming-url|g' kubernetes/deployment.yaml
sed -i.bak 's|{{NAMING_FACTORY_INITIAL}}|your-naming-factory|g' kubernetes/deployment.yaml
sed -i.bak 's|{{DATASOURCE_JNDI}}|java:comp/env/jdbc/ModResortsJndi|g' kubernetes/deployment.yaml
rm -f kubernetes/deployment.yaml.bak

# Apply deployment, service, and ingress
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml
kubectl apply -f kubernetes/ingress.yaml

# Wait for rollout
kubectl rollout status deployment/modresorts -n modresorts
```

### Kubernetes Manifests

- **namespace.yaml**: Creates the `modresorts` namespace
- **deployment.yaml**: Defines the Deployment with 2 replicas, resource limits, liveness/readiness probes, and environment variables
- **service.yaml**: Exposes the application via a ClusterIP service on port 80
- **ingress.yaml**: Configures an AWS ALB ingress for external access

### Accessing the Application

After deployment, get the application URL:

```bash
kubectl get ingress modresorts-ingress -n modresorts
```

The application will be available at `http://modresorts.example.com`.

## Configuration Management

Configuration is managed through Kubernetes ConfigMaps and Secrets:

- **Secrets**: Credentials such as `WEATHER_API_KEY` are stored in a Kubernetes Secret named `modresorts-secrets`
- **ConfigMaps**: Non-sensitive configuration can be stored in ConfigMaps (not currently used)
- **Environment Variables**: The application reads configuration from environment variables

### Secret Management

The `WEATHER_API_KEY` is stored as a Kubernetes Secret and injected into the pod via `secretKeyRef`. Never hardcode credentials in the deployment manifest.

## Security Considerations

1. **Non-root user**: The container runs as a non-root user (`tomcat`)
2. **Secrets management**: Credentials are stored in Kubernetes Secrets, not in the image
3. **Minimal base image**: The runtime image uses `eclipse-temurin:8-jre` with only Tomcat and the application
4. **No package managers**: The runtime stage does not install any package managers
5. **Health checks**: The application exposes a `/health` endpoint for liveness and readiness probes
6. **Graceful shutdown**: Tomcat handles SIGTERM for graceful shutdown

## Troubleshooting

### Pod Failures
- Check pod logs: `kubectl logs <pod-name> -n modresorts`
- Check pod events: `kubectl describe pod <pod-name> -n modresorts`
- Common issues: missing secrets, insufficient resources, image pull errors

### Service Issues
- Verify the service is running: `kubectl get svc -n modresorts`
- Check endpoints: `kubectl get endpoints -n modresorts`

### Ingress Problems
- Verify the AWS Load Balancer Controller is installed
- Check ingress status: `kubectl get ingress -n modresorts`
- Verify DNS resolution: `nslookup modresorts.example.com`

### Rollback
```bash
kubectl rollout undo deployment/modresorts -n modresorts
```

## Scaling and Management

### Horizontal Pod Autoscaler (HPA)
To enable auto-scaling:

```bash
kubectl autoscale deployment modresorts --cpu-percent=60 --min=2 --max=10 -n modresorts
```

### Rolling Updates
To update the application image:

```bash
kubectl set image deployment/modresorts modresorts=<new-image-uri> -n modresorts
kubectl rollout status deployment/modresorts -n modresorts
```

### Manual Rollback
```bash
kubectl rollout history deployment/modresorts -n modresorts
kubectl rollout undo deployment/modresorts --to-revision=<revision> -n modresorts
```

## Java Notes

### JVM Settings
The container uses the following JVM settings via `JAVA_OPTS`:
- `-Xmx512m`: Maximum heap size of 512MB
- `-Xms256m`: Initial heap size of 256MB
- `-XX:+UseContainerSupport`: Enables container-aware JVM settings
- `-XX:MaxRAMPercentage=75.0`: Uses up to 75% of available container memory

These settings are tuned for the resource limits defined in the Kubernetes Deployment (requests: 512Mi memory, limits: 1Gi memory).

### Servlet Container
The application runs on Apache Tomcat 8.5, which is downloaded during the Docker build stage and copied into the runtime image. The WAR file is deployed as the ROOT application.

### Health Endpoint
The application exposes a `/health` endpoint that returns `{"status":"UP"}` with HTTP 200. This endpoint is used by Kubernetes liveness and readiness probes:
- **Liveness probe**: Checks `/health` on port 8080 after a 30-second initial delay, every 10 seconds
- **Readiness probe**: Checks `/health` on port 8080 after a 10-second initial delay, every 5 seconds

### Timezone and Encoding
- `TZ=UTC`: Sets the timezone to UTC
- `JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Duser.timezone=UTC`: Ensures consistent character encoding and timezone handling

### Java Version
The application targets Java 8 (`maven.compiler.source` and `maven.compiler.target` set to 1.8) and runs on `eclipse-temurin:8-jre`.
