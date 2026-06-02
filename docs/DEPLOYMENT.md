# ModResorts Deployment Guide (Azure AKS)

## 1. Overview

This document describes how to build, containerize, and deploy the ModResorts Java EE application to Azure Kubernetes Service (AKS).

The project produces a WAR file intended for deployment to a Java EE application server. The provided Dockerfile builds the WAR artifact; you can either use the image as an artifact carrier or adapt it to include a servlet container.

## 2. Prerequisites

- Azure subscription
- Azure CLI installed and logged in (`az login`)
- kubectl installed and configured
- Docker installed and running
- Bash (Linux/macOS) or PowerShell/Command Prompt (Windows)

## 3. Local Development with Docker

1. From the project root, build and run using Docker Compose:

   ```bash
   docker-compose up --build
   ```

2. The container will be built using the provided Dockerfile. The image contains the `modresorts` WAR at `/app/app.war`.

> Note: The sample Dockerfile does not embed an application server. In a real deployment, you would typically deploy the WAR to a servlet container such as Tomcat, Jetty, or WebSphere Liberty.

## 4. Building and Pushing the Image

### Linux/macOS

1. Make the script executable:

   ```bash
   chmod +x scripts/build-push.sh
   ```

2. Run the script:

   ```bash
   ./scripts/build-push.sh
   ```

3. Follow the prompts to:
   - Enter project name
   - Select registry type (Azure ACR or Docker Hub)
   - Provide registry credentials
   - Provide image tag

The script will build the Docker image using the project Dockerfile and push it to the selected registry.

### Windows

1. Run the batch script from Command Prompt:

   ```bat
   scripts\build-push.bat
   ```

2. Follow the prompts similar to the Linux/macOS script.

## 5. Azure AKS Setup

1. Create a resource group (if not already created):

   ```bash
   az group create --name myResourceGroup --location eastus
   ```

2. Create an AKS cluster:

   ```bash
   az aks create \
     --resource-group myResourceGroup \
     --name myAksCluster \
     --node-count 2 \
     --enable-addons monitoring \
     --generate-ssh-keys
   ```

3. Get cluster credentials:

   ```bash
   az aks get-credentials --resource-group myResourceGroup --name myAksCluster
   ```

4. Verify connectivity:

   ```bash
   kubectl cluster-info
   ```

## 6. Kubernetes Manifests

The `kubernetes` directory contains:

- `namespace.yaml` – Namespace definition (`modresorts`)
- `deployment.yaml` – Deployment for the application
- `service.yaml` – ClusterIP service exposing the application on port 80
- `ingress.yaml` – Ingress resource configured for Azure Application Gateway

The deployment uses:

- 2 replicas
- Container port 8080
- Resource requests/limits suitable for a small Java application
- TCP-based liveness and readiness probes on port 8080

## 7. Deploying to AKS

### Linux/macOS

1. Ensure you have built and pushed the image to a registry accessible by AKS.

2. Run the deployment script:

   ```bash
   chmod +x scripts/deploy-image.sh
   ./scripts/deploy-image.sh
   ```

3. Provide:
   - Azure resource group
   - AKS cluster name
   - Full image URI (e.g., `myregistry.azurecr.io/modresorts:latest`)

4. The script will:
   - Configure kubectl for the cluster
   - Update `deployment.yaml` with the image URI
   - Apply namespace, deployment, service, and ingress manifests
   - Wait for the deployment rollout
   - Show current resources in the `modresorts` namespace

### Windows

1. Run the batch script:

   ```bat
   scripts\deploy-image.bat
   ```

2. Provide the same information as for the Linux/macOS script.

## 8. Accessing the Application

The sample ingress is configured with host `modresorts.example.com`. To use it:

1. Configure Azure Application Gateway Ingress Controller (AGIC) for your AKS cluster following Azure documentation.
2. Create a DNS record pointing `modresorts.example.com` to the public IP of your Application Gateway.
3. Access the application via:

   ```
   http://modresorts.example.com/
   ```

## 9. Scaling and Management

- Scale the deployment:

  ```bash
  kubectl scale deployment/modresorts -n modresorts --replicas=3
  ```

- View rollout history:

  ```bash
  kubectl rollout history deployment/modresorts -n modresorts
  ```

- Roll back to a previous revision:

  ```bash
  kubectl rollout undo deployment/modresorts -n modresorts
  ```

## 10. Troubleshooting

- Check pod logs:

  ```bash
  kubectl logs -l app=modresorts -n modresorts
  ```

- Describe pods and services:

  ```bash
  kubectl describe pods -l app=modresorts -n modresorts
  kubectl describe service modresorts-service -n modresorts
  ```

- Verify ingress:

  ```bash
  kubectl describe ingress modresorts-ingress -n modresorts
  ```

## 11. Security Considerations

- Use private container registries (e.g., Azure ACR) and restrict access.
- Configure Kubernetes RBAC to limit access to the `modresorts` namespace.
- Use network policies to control traffic between pods.
- Store secrets (e.g., database credentials) in Kubernetes Secrets, not in images or manifests.

## 12. Notes on Java Configuration

- The application is built for Java 8 and packaged as a WAR.
- JVM options are configured via the `JAVA_OPTS` environment variable.
- Timezone is set to UTC by default.

You can customize resource limits, environment variables, and ingress hostnames in the Kubernetes manifests to match your environment.
