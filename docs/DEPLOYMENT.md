# ModResorts Deployment Guide (Azure AKS)

This document describes how to build, containerize, and deploy the ModResorts Java web application to Azure Kubernetes Service (AKS).

## 1. Prerequisites

- Azure subscription
- Azure CLI (az) installed and logged in
- kubectl installed and configured
- Docker installed and running
- Access to a container registry (Azure Container Registry or Docker Hub)
- Bash (Linux/macOS) or PowerShell/Command Prompt (Windows)

## 2. Project Overview

- Technology: Java EE / Servlet-based WAR application
- Build tool: Maven (pom.xml)
- Packaging: WAR (`modresorts-2.0.0.war`)
- Java version: 1.8
- Health endpoint: `GET /health` (implemented by `HealthCheckServlet`)
- Default container port: `9080` (exposed by Docker image and Kubernetes Service)

## 3. Local Development with Docker Compose

1. Ensure Docker is running.
2. From the project root, build and start the application:

   ```bash
   docker-compose up --build
   ```

3. Access the application:

   - Main app: http://localhost:9080/
   - Health check: http://localhost:9080/health

4. To stop the application:

   ```bash
   docker-compose down
   ```

## 4. Building and Pushing the Docker Image

Use the provided scripts to build and push the Docker image to your chosen registry.

### 4.1 Linux/macOS

```bash
chmod +x scripts/build-push.sh
scripts/build-push.sh
```

The script will prompt you for:

- Project name (used to derive the image name)
- Registry type:
  - `1` for Azure Container Registry (ACR)
  - `2` for Docker Hub
- Registry-specific details (ACR name or Docker Hub username/password)
- Image tag (defaults to `latest`)

The script will:

1. Sanitize the image name and tag.
2. Log in to the selected registry.
3. Build the Docker image using the `Dockerfile` in the project root.
4. Push the image to the registry.

### 4.2 Windows

```bat
scripts\build-push.bat
```

The Windows script provides the same functionality as the Bash script, using Windows-compatible commands and error handling.

## 5. Azure AKS Setup

If you do not already have an AKS cluster, create one using Azure CLI (example):

```bash
RESOURCE_GROUP=my-rg
CLUSTER_NAME=my-aks
LOCATION=eastus

az group create --name "$RESOURCE_GROUP" --location "$LOCATION"
az aks create --resource-group "$RESOURCE_GROUP" --name "$CLUSTER_NAME" --node-count 2 --enable-managed-identity --generate-ssh-keys
```

Configure kubectl to use the cluster:

```bash
az aks get-credentials --resource-group "$RESOURCE_GROUP" --name "$CLUSTER_NAME"
```

Verify connectivity:

```bash
kubectl cluster-info
```

## 6. Kubernetes Manifests

The `kubernetes/` directory contains the following manifests:

- `namespace.yaml` – Creates the `modresorts` namespace.
- `deployment.yaml` – Deploys the ModResorts container with 2 replicas.
- `service.yaml` – Exposes the application internally via a ClusterIP service on port 80 → 9080.
- `ingress.yaml` – Configures an Ingress resource for Azure Application Gateway.

Key configuration details:

- Container image: `{{IMAGE_URI}}` placeholder in `deployment.yaml` (replaced by deploy scripts).
- Container port: `9080`.
- Health probes: HTTP GET `/health` on port `9080` for both liveness and readiness.
- Resource requests/limits:
  - Requests: `cpu: 250m`, `memory: 512Mi`
  - Limits: `cpu: 500m`, `memory: 1Gi`
- Environment variables:
  - `TZ=UTC`
  - `JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:+UnlockExperimentalVMOptions -XX:MaxRAMPercentage=75.0`
  - `APP_ENV=prod`
  - `DATABASE_URL` (optional, placeholder)
  - `API_KEY` (optional, placeholder)

## 7. Deploying to Azure AKS

### 7.1 Linux/macOS

```bash
chmod +x scripts/deploy-image.sh
scripts/deploy-image.sh
```

The script will prompt for:

- Azure resource group name
- AKS cluster name
- Full Docker image URI (e.g., `myregistry.azurecr.io/modresorts:latest`)
- Optional `DATABASE_URL` and `API_KEY`

The script will:

1. Configure kubectl for the specified AKS cluster.
2. Verify cluster connectivity.
3. Replace placeholders in `kubernetes/deployment.yaml` with the provided values.
4. Apply `namespace.yaml`, `deployment.yaml`, `service.yaml`, and `ingress.yaml` in order.
5. Wait for the deployment rollout to complete.
6. Display the current pods, services, and ingress resources in the `modresorts` namespace.

### 7.2 Windows

```bat
scripts\deploy-image.bat
```

The Windows script performs the same steps using Azure CLI, kubectl, and PowerShell for placeholder replacement.

## 8. Accessing the Application on AKS

Once the ingress is configured and DNS is set up, the application will be accessible at:

- http://modresorts.example.com/

You may need to:

- Configure Azure Application Gateway Ingress Controller (AGIC) in your AKS cluster.
- Update the `host` field in `kubernetes/ingress.yaml` to match your real domain.
- Create a DNS record pointing your domain to the Application Gateway public IP.

## 9. Scaling and Management

### 9.1 Scaling the Deployment

To scale the number of replicas:

```bash
kubectl scale deployment/modresorts -n modresorts --replicas=3
```

### 9.2 Rolling Updates

Update the image tag in your registry and re-run the deployment script with the new `IMAGE_URI`, or manually patch the deployment:

```bash
kubectl set image deployment/modresorts modresorts=myregistry.azurecr.io/modresorts:newtag -n modresorts
```

Monitor rollout status:

```bash
kubectl rollout status deployment/modresorts -n modresorts
```

### 9.3 Rollbacks

To roll back to the previous ReplicaSet:

```bash
kubectl rollout undo deployment/modresorts -n modresorts
```

## 10. Configuration Management

Use environment variables for external dependencies such as databases, APIs, caches, etc. The sample manifests include `DATABASE_URL` and `API_KEY` placeholders.

For more complex configuration:

- Use Kubernetes ConfigMaps for non-sensitive configuration.
- Use Kubernetes Secrets for sensitive data (passwords, API keys).

Example: create a secret for `API_KEY` and reference it in the deployment.

```bash
kubectl create secret generic modresorts-secrets -n modresorts \
  --from-literal=API_KEY=your-api-key-here
```

Then update the deployment to use `valueFrom.secretKeyRef` instead of a plain `value`.

## 11. Security Considerations

- Run containers as a non-root user (the Dockerfile creates `appuser` with UID 1001).
- Limit container resources using requests and limits.
- Use HTTPS for ingress traffic (configure TLS certificates in Azure Application Gateway).
- Store secrets in Kubernetes Secrets or Azure Key Vault, not in source control.
- Regularly update base images and dependencies to include security patches.

## 12. Troubleshooting

- **Pods not starting**: Check pod logs and events:

  ```bash
  kubectl logs -l app=modresorts -n modresorts
  kubectl describe pods -l app=modresorts -n modresorts
  ```

- **Image pull errors**: Ensure the image URI is correct and the AKS cluster has access to the registry (for ACR, use `az aks update -n <cluster> -g <rg> --attach-acr <acrName>` if needed).

- **Ingress not working**: Verify ingress controller setup, DNS configuration, and Application Gateway health probes.

- **Health probe failures**: Confirm that `/health` endpoint is accessible and returns HTTP 200.

## 13. Java-Specific Notes

- The Dockerfile uses Java 8 (OpenJDK) as the runtime, matching the project configuration.
- JVM options are tuned for container environments via `JAVA_OPTS`.
- Adjust `-Xms`, `-Xmx`, and `MaxRAMPercentage` based on your workload and node sizes.

---

This deployment setup is intended as a solid starting point for running the ModResorts application on Azure AKS. Customize resource settings, environment variables, and ingress configuration to match your production requirements.
