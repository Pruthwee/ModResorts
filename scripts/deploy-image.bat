@echo off
setlocal enabledelayedexpansion

echo -------------------------------------------------------
echo ModResorts EKS Deployment Script
echo -------------------------------------------------------

set /p AWS_REGION="Enter AWS Region [us-east-1]: "
if "!AWS_REGION!"=="" set AWS_REGION=us-east-1

set /p CLUSTER_NAME="Enter EKS Cluster Name: "
if "!CLUSTER_NAME!"=="" (
    echo Error: Cluster name is required.
    exit /b 1
)

set /p IMAGE_URI="Enter Docker Image URI (e.g., account.dkr.ecr.region.amazonaws.com/repo:tag): "
if "!IMAGE_URI!"=="" (
    echo Error: Image URI is required.
    exit /b 1
)

echo Configuring kubectl for EKS cluster...
aws eks update-kubeconfig --region !AWS_REGION! --name !CLUSTER_NAME!
if !ERRORLEVEL! neq 0 (
    echo Error: Failed to update kubeconfig
    exit /b 1
)

echo Verifying cluster connectivity...
kubectl cluster-info
if !ERRORLEVEL! neq 0 (
    echo Error: Could not connect to EKS cluster
    exit /b 1
)

echo Updating Kubernetes manifests...
:: Using PowerShell for sed-like replacement on Windows
powershell -Command "(Get-Content kubernetes/deployment.yaml) -replace '{{IMAGE_URI}}', '!IMAGE_URI!' | Set-Content kubernetes/deployment.yaml"

echo Applying manifests...
kubectl apply -f kubernetes/namespace.yaml
if !ERRORLEVEL! neq 0 (echo Namespace apply failed & exit /b 1)
kubectl apply -f kubernetes/deployment.yaml
if !ERRORLEVEL! neq 0 (echo Deployment apply failed & exit /b 1)
kubectl apply -f kubernetes/service.yaml
if !ERRORLEVEL! neq 0 (echo Service apply failed & exit /b 1)
kubectl apply -f kubernetes/ingress.yaml
if !ERRORLEVEL! neq 0 (echo Ingress apply failed & exit /b 1)

echo Waiting for deployment rollout...
kubectl rollout status deployment/modresorts -n modresorts
if !ERRORLEVEL! neq 0 (
    echo Deployment rollout failed
    exit /b 1
)

echo Verifying resources...
kubectl get pods,svc,ingress -n modresorts

echo -------------------------------------------------------
echo Deployment complete!
echo Application URL: http://modresorts.example.com
echo -------------------------------------------------------
