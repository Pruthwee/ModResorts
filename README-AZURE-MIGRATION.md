# ModResorts - Azure Cloud-Native Migration

## Overview
This application has been migrated from a traditional WAR-based Java EE application to a cloud-native Spring Boot application optimized for Azure deployment.

## Key Changes

### 1. Packaging Migration (Blockers 18-19)
- **From**: WAR packaging requiring external application server
- **To**: Executable JAR with embedded Tomcat
- **Benefit**: Simplified deployment, smaller container images, cloud-native compatibility

### 2. File System Operations (Blockers 1-4, 6)
- **From**: Local file system operations with hard-coded paths
- **To**: Azure Blob Storage with Azure SDK
- **Implementation**: `AzureBlobStorageService` for all file operations
- **Configuration**: Set `AZURE_STORAGE_ACCOUNT_NAME` or `AZURE_STORAGE_ACCOUNT_URL`

### 3. Secrets Management (Blocker 7)
- **From**: Hard-coded API keys in source code
- **To**: Azure Key Vault with Managed Identity
- **Implementation**: `SecretClient` bean for secure secret retrieval
- **Configuration**: Set `AZURE_KEYVAULT_URL` or `AZURE_KEYVAULT_NAME`

### 4. EJB Migration (Blockers 8-9)
- **From**: EJB 2.x with heavy container dependencies
- **To**: Spring Boot services with dependency injection
- **Implementation**: `@Service` annotation on `ModResortsCustomerInformation`
- **Benefit**: Lightweight, cloud-native, no application server required

### 5. Timer/Scheduling (Blockers 10-14)
- **From**: `java.util.Timer` with local scheduling
- **To**: Azure Service Bus scheduled messages
- **Implementation**: `AzureServiceBusSchedulerService` for distributed scheduling
- **Configuration**: Set `AZURE_SERVICEBUS_NAMESPACE`

### 6. Session Management (Blockers 15-17)
- **From**: WebSphere-specific session clustering
- **To**: Spring Session with Azure Cache for Redis
- **Implementation**: `@EnableRedisHttpSession` annotation
- **Configuration**: Set `AZURE_REDIS_HOST`, `AZURE_REDIS_PORT`, `AZURE_REDIS_PASSWORD`

### 7. Resource Management (Blocker 5)
- **From**: Manual resource cleanup with potential leaks
- **To**: Try-with-resources for automatic resource management
- **Implementation**: All database connections, streams, and file handles use try-with-resources

## Azure Services Required

### Core Services
1. **Azure App Service** or **Azure Container Apps** - Application hosting
2. **Azure Blob Storage** - File storage
3. **Azure Key Vault** - Secrets management
4. **Azure Cache for Redis** - Distributed session storage

### Optional Services
5. **Azure Service Bus** - Scheduled task execution
6. **Azure Database for PostgreSQL** - Database (if using database features)

## Environment Variables

### Required
```bash
# Azure Storage
AZURE_STORAGE_ACCOUNT_NAME=<your-storage-account>
# OR
AZURE_STORAGE_ACCOUNT_URL=https://<account>.blob.core.windows.net

# Azure Key Vault
AZURE_KEYVAULT_NAME=<your-keyvault>
# OR
AZURE_KEYVAULT_URL=https://<keyvault>.vault.azure.net

# Azure Redis Cache
AZURE_REDIS_HOST=<your-redis>.redis.cache.windows.net
AZURE_REDIS_PORT=6380
AZURE_REDIS_PASSWORD=<your-redis-key>
AZURE_REDIS_SSL=true
```

### Optional
```bash
# Azure Service Bus (for scheduled tasks)
AZURE_SERVICEBUS_NAMESPACE=<your-servicebus>.servicebus.windows.net

# Database (if using database features)
DATABASE_URL=jdbc:postgresql://<server>.postgres.database.azure.com:5432/modresorts
DATABASE_USERNAME=<username>
DATABASE_PASSWORD=<password>

# Application Port
PORT=8080
```

## Managed Identity Configuration

This application uses **Azure Managed Identity** for authentication to Azure services. Ensure your App Service or Container App has:

1. **System-assigned Managed Identity** enabled
2. **Role assignments**:
   - Storage Blob Data Contributor (for Blob Storage)
   - Key Vault Secrets User (for Key Vault)
   - Azure Service Bus Data Sender (for Service Bus)

## Building the Application

```bash
mvn clean package
```

This produces an executable JAR: `target/modresorts-2.0.0.jar`

## Running Locally

```bash
# Set required environment variables
export AZURE_STORAGE_ACCOUNT_NAME=<your-storage>
export AZURE_KEYVAULT_NAME=<your-keyvault>
export AZURE_REDIS_HOST=<your-redis>.redis.cache.windows.net
export AZURE_REDIS_PORT=6380
export AZURE_REDIS_PASSWORD=<your-key>

# Run the application
java -jar target/modresorts-2.0.0.jar
```

## Deploying to Azure

### Azure App Service
```bash
az webapp create --resource-group <rg> --plan <plan> --name <app-name> --runtime "JAVA:8-jre8"
az webapp deploy --resource-group <rg> --name <app-name> --src-path target/modresorts-2.0.0.jar
```

### Azure Container Apps
```bash
# Build container image
docker build -t modresorts:2.0.0 .

# Push to Azure Container Registry
az acr build --registry <acr-name> --image modresorts:2.0.0 .

# Deploy to Container Apps
az containerapp create \
  --name modresorts \
  --resource-group <rg> \
  --environment <env> \
  --image <acr-name>.azurecr.io/modresorts:2.0.0 \
  --target-port 8080 \
  --ingress external
```

## Health Checks

The application exposes health check endpoints:
- `/actuator/health` - Overall health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

## Migration Notes

### Breaking Changes
1. **Session Storage**: Sessions are now stored in Redis. Existing sessions will be lost during migration.
2. **File Paths**: All file operations now use Azure Blob Storage. Migrate existing files to Blob Storage.
3. **API Keys**: Move all API keys to Azure Key Vault before deployment.

### Backward Compatibility
- Servlet endpoints remain unchanged
- Business logic preserved
- API contracts maintained

## Troubleshooting

### Common Issues

1. **Blob Storage Connection Failed**
   - Verify Managed Identity has Storage Blob Data Contributor role
   - Check `AZURE_STORAGE_ACCOUNT_NAME` environment variable

2. **Key Vault Access Denied**
   - Verify Managed Identity has Key Vault Secrets User role
   - Check `AZURE_KEYVAULT_NAME` environment variable

3. **Redis Connection Failed**
   - Verify Redis credentials and SSL settings
   - Check firewall rules allow App Service IP

4. **Service Bus Not Working**
   - Verify Managed Identity has Azure Service Bus Data Sender role
   - Check queue exists in Service Bus namespace

## Support

For issues or questions, contact the development team.
