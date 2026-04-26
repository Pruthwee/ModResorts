# ModResorts - Cloud-Ready Application

## Overview
This application has been migrated to be cloud-ready for deployment on Google Cloud Platform (GCP). The application now follows cloud-native patterns and is ready for containerized deployment on GKE, Cloud Run, or App Engine.

## Cloud Readiness Improvements

### 1. Packaging Migration (WAR → JAR)
- **Changed from**: WAR packaging requiring external application server
- **Changed to**: Executable JAR with embedded Tomcat via Spring Boot
- **Benefit**: Simplified containerization, faster startup, smaller container images

### 2. File System Dependencies → Google Cloud Storage
- **Changed from**: Local file system operations with hard-coded paths
- **Changed to**: Google Cloud Storage (GCS) for persistent file storage
- **Files affected**: 
  - `AvailabilityCheckerServlet.java` - Export operations now use GCS
  - `IOUtils.java` - Resource loading from GCS or classpath
- **Benefit**: Data persists across container restarts and scaling events

### 3. Secrets Management → Google Secret Manager
- **Changed from**: Hard-coded API keys and credentials in code/config
- **Changed to**: Google Secret Manager for centralized secret management
- **Files affected**: `WeatherServlet.java`
- **Benefit**: Secure credential lifecycle management with audit logging

### 4. EJB 2.x → Spring Boot Services
- **Changed from**: EJB Singleton with container-managed resources
- **Changed to**: Spring Boot @Service with dependency injection
- **Files affected**: `ModResortsCustomerInformation.java`
- **Benefit**: Cloud-native, lightweight, container-friendly architecture

### 5. Connection Pooling with HikariCP
- **Changed from**: Direct JDBC connections without pooling
- **Changed to**: HikariCP connection pooling (Spring Boot default)
- **Configuration**: `application.properties`
- **Benefit**: Efficient resource usage, better performance, automatic connection management

### 6. Session Management → Redis (Memorystore)
- **Changed from**: WebSphere-specific session clustering
- **Changed to**: Spring Session with Redis backend (GCP Memorystore)
- **Files affected**: `LogoutServlet.java`, `UpperServlet.java`
- **Benefit**: Stateless application instances, horizontal scaling support

### 7. Time/Clock Dependencies → UTC Standardization
- **Changed from**: Server-local timezone and java.util.Timer
- **Changed to**: UTC timezone for all operations, Spring @Scheduled
- **Files affected**: 
  - `DateChecker.java`
  - `ReservationCheckerData.java`
  - `AvailabilityCheckerServlet.java`
  - `ScheduledTaskService.java`
- **Benefit**: Consistent behavior across distributed cloud instances

### 8. Resource Management → Try-with-Resources
- **Changed from**: Manual resource closing with potential leaks
- **Changed to**: Try-with-resources for automatic resource management
- **Files affected**: 
  - `AvailabilityCheckerServlet.java`
  - `WeatherServlet.java`
  - `ModResortsCustomerInformation.java`
- **Benefit**: No resource leaks in containerized environments with strict limits

## Environment Variables

The application uses environment variables for cloud configuration:

### Required for GCP Services
- `GCP_PROJECT_ID` - Your GCP project ID
- `GCS_BUCKET_NAME` - GCS bucket name for file storage (default: modresorts-bucket)

### Database (Cloud SQL)
- `DATABASE_URL` - JDBC connection string
- `DATABASE_USER` - Database username
- `DATABASE_PASSWORD` - Database password
- `DB_POOL_SIZE` - HikariCP max pool size (default: 10)
- `DB_MIN_IDLE` - HikariCP min idle connections (default: 2)

### Redis (Memorystore)
- `REDIS_HOST` - Redis host (default: localhost)
- `REDIS_PORT` - Redis port (default: 6379)
- `REDIS_PASSWORD` - Redis password (if required)

### Application
- `PORT` - Server port (default: 8080)
- `WEATHER_API_KEY` - Weather API key (fallback if Secret Manager unavailable)

## Secret Manager Configuration

Store sensitive credentials in Google Secret Manager:

1. **weather-api-key** - Weather Underground API key

## Building the Application

```bash
mvn clean package
```

This produces an executable JAR: `target/modresorts-2.0.0.jar`

## Running Locally

```bash
# Set required environment variables
export GCP_PROJECT_ID=your-project-id
export GCS_BUCKET_NAME=your-bucket-name
export DATABASE_URL=jdbc:postgresql://localhost:5432/modresorts
export DATABASE_USER=postgres
export DATABASE_PASSWORD=yourpassword
export REDIS_HOST=localhost
export REDIS_PORT=6379

# Run the application
java -jar target/modresorts-2.0.0.jar
```

## Deployment Options

### Google Kubernetes Engine (GKE)
1. Build container image
2. Push to Google Container Registry
3. Deploy using Kubernetes manifests
4. Configure Workload Identity for GCP service access

### Cloud Run
1. Build container image
2. Push to Google Container Registry
3. Deploy to Cloud Run
4. Configure service account for GCP service access

### App Engine Flexible Environment
1. Configure `app.yaml`
2. Deploy using `gcloud app deploy`

## Dependencies Added

- **Spring Boot Starter Web** - Embedded Tomcat, REST support
- **Spring Boot Starter Data JPA** - Database access with Hibernate
- **Spring Boot Starter Data Redis** - Redis integration
- **Spring Session Data Redis** - Distributed session management
- **Google Cloud Storage** - File storage service
- **Google Cloud Secret Manager** - Secrets management
- **Google Cloud Pub/Sub** - Async messaging (for future use)
- **HikariCP** - Connection pooling
- **PostgreSQL Driver** - Cloud SQL connectivity

## Architecture Changes

### Before (Legacy)
- WAR deployment on WebSphere
- Local file system for data storage
- Hard-coded credentials
- EJB 2.x for business logic
- WebSphere-specific session clustering
- Direct JDBC connections
- Server-local time dependencies

### After (Cloud-Native)
- Executable JAR with embedded Tomcat
- Google Cloud Storage for persistent data
- Google Secret Manager for credentials
- Spring Boot services with dependency injection
- Redis-backed distributed sessions
- HikariCP connection pooling
- UTC timezone standardization
- Try-with-resources for resource management

## Health Checks

The application exposes health check endpoints:

- `/actuator/health` - Overall application health
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

## Logging

Logging is configured for cloud environments:
- JSON-structured logs (compatible with Cloud Logging)
- UTC timestamps
- Appropriate log levels for production

## Next Steps

1. **Configure GCP Resources**:
   - Create GCS bucket
   - Set up Cloud SQL instance
   - Create Memorystore Redis instance
   - Configure Secret Manager secrets

2. **Set up CI/CD**:
   - Configure Cloud Build for automated builds
   - Set up deployment pipelines

3. **Monitoring**:
   - Configure Cloud Monitoring alerts
   - Set up Cloud Logging filters
   - Enable Cloud Trace for distributed tracing

4. **Security**:
   - Configure Workload Identity
   - Set up VPC Service Controls
   - Enable Cloud Armor for DDoS protection

## Support

For issues or questions, contact the development team.
