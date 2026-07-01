# Deployment Guide for ModResorts on AWS EKS

## Overview
This guide provides instructions for containerizing and deploying the ModResorts Java application to AWS Elastic Kubernetes Service (EKS).

## Prerequisites
- Java 8 JDK
- Maven 3.8+
- Docker
- AWS CLI configured with appropriate permissions
- kubectl installed
- AWS EKS Cluster

## Local Development Setup
To run the application locally using Docker Compose:
1. Navigate to the project root.
2. Run `docker-compose up --build`.
3. Access the application at `http://localhost:8080`.

## Build and Push Instructions
### Linux/macOS
1. Make the script executable: `chmod +x scripts/build-push.sh`
2. Run the script: `./scripts/build-push.sh`
3. Follow the prompts to select your registry (AWS ECR or Docker Hub) and provide credentials.

### Windows
1. Run the batch script: `scripts\\build-push.bat`
2. Follow the prompts to select your registry and provide credentials.

## AWS EKS Deployment
### Prerequisites
- Ensure your AWS CLI is configured to the correct account.
- Ensure you have `kubectl` installed and configured.

### Deployment Steps
1. Run the deployment script:
   - Linux/macOS: `chmod +x scripts/deploy-image.sh && ./scripts/deploy-image.sh`
   - Windows: `scripts\\deploy-image.bat`
2. Provide the AWS region, EKS cluster name, and the full Docker image URI generated in the build step.
3. The script will automatically:
   - Update your kubeconfig.
   - Replace placeholders in Kubernetes manifests.
   - Apply the namespace, deployment, service, and ingress.
   - Wait for the rollout to complete.

## Kubernetes Manifests Description
- `namespace.yaml`: Creates a dedicated namespace `modresorts` for the application.
- `deployment.yaml`: Defines the application pods, resource limits (CPU: 500m, Memory: 1Gi), and health probes.
- `service.yaml`: Exposes the application internally within the cluster on port 80.
- `ingress.yaml`: Configures an AWS Application Load Balancer (ALB) to route external traffic to the service.

## Troubleshooting
- **Pod Failures**: Check logs using `kubectl logs -l app=modresorts -n modresorts`.
- **Service Issues**: Verify service selector matches deployment labels using `kubectl describe svc modresorts-service -n modresorts`.
- **Ingress Problems**: Check ALB controller logs and ensure the ingress class is set to `alb`.

## Configuration Management
Environment variables are used for configuration. You can add more variables to `kubernetes/deployment.yaml` or modify the `deploy-image` scripts to prompt for them.

## Security Considerations
- The application runs as a non-root user (`appuser`) inside the container.
- Resource limits are enforced to prevent noisy neighbor issues.
- Use AWS IAM roles for service accounts (IRSA) for any AWS resource access.
