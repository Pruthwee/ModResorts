# ModResorts Cloud Deployment Guide

## Overview
ModResorts has been modernized for cloud deployment on AWS. The application is now packaged as an executable JAR with embedded Tomcat and uses cloud-native services for storage, secrets, and session management.

## Cloud Readiness Improvements

### 1. Packaging (JAR instead of WAR)
- **Changed from**: WAR file requiring external application server
- **Changed to**: Executable JAR with embedded Tomcat
- **Benefit**: Direct deployment to AWS ECS, EKS, or Fargate without application server

### 2. File Storage (Amazon S3)
- **Changed from**: Local file system writes (ephemeral in containers)
- **Changed to**: Amazon S3 for durable storage
- **Configuration**: Set `S3_BUCKET_NAME` environment variable
- **Files affected**: `AvailabilityCheckerServlet.java`, `IOUtils.java`

### 3. Secrets Management (AWS Secrets Manager)
- **Changed from**: Hardcoded API keys and environment variables
- **Changed to**: AWS Secrets Manager with automatic rotation
- **Configuration**: Create secret named `weather-api-key` in AWS Secrets Manager
- **Files affected**: `WeatherServlet.java`

### 4. Session Management (Amazon ElastiCache Redis)
- **Changed from**: WebSphere/JBoss cluster state replication
- **Changed to**: Spring Session with Redis (ElastiCache)
- **Configuration**: Set `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` environment variables
- **Files affected**: `LogoutServlet.java`, `UpperServlet.java`

### 5. Database Connection Pooling (HikariCP)
- **Changed from**: EJB 2.x with direct JDBC connections
- **Changed to**: Spring Data JPA with HikariCP connection pooling
- **Configuration**: Set `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` environment variables
- **Files affected**: `ModResortsCustomerInformation.java`

### 6. Date/Time Handling (java.time API)
- **Changed from**: java.util.Date (timezone-dependent)
- **Changed to**: java.time.LocalDate (timezone-independent, UTC-based)
- **Benefit**: Consistent date handling across distributed cloud environments
- **Files affected**: `AvailabilityCheckerServlet.java`, `DateChecker.java`, `ReservationCheckerData.java`

## Required Environment Variables

### AWS Configuration
```bash
AWS_REGION=us-east-1                    # AWS region for all services
S3_BUCKET_NAME=modresorts-data          # S3 bucket for file storage
```

### Database Configuration (AWS RDS)
```bash
DATABASE_URL=jdbc:postgresql://your-rds-endpoint:5432/modresorts
DATABASE_USERNAME=modresorts_user
DATABASE_PASSWORD=<retrieve-from-secrets-manager>
DB_POOL_SIZE=10                         # HikariCP max pool size
DB_POOL_MIN_IDLE=2                      # HikariCP min idle connections
```

### Redis Configuration (Amazon ElastiCache)
```bash
REDIS_HOST=your-elasticache-endpoint.cache.amazonaws.com
REDIS_PORT=6379
REDIS_PASSWORD=<retrieve-from-secrets-manager>
REDIS_SSL=true                          # Enable SSL for ElastiCache
```

### Application Configuration
```bash
PORT=8080                               # Application port
```

## AWS Services Required

### 1. Amazon S3
- Create S3 bucket for file storage
- Configure IAM role with S3 read/write permissions
- Bucket policy should allow application to PutObject and GetObject

### 2. AWS Secrets Manager
- Create secret: `weather-api-key` (string value)
- Create secret: `database-password` (optional, can use environment variable)
- Create secret: `redis-password` (optional, can use environment variable)
- Configure IAM role with SecretsManager:GetSecretValue permission

### 3. Amazon RDS (PostgreSQL)
- Create PostgreSQL database instance
- Configure security group to allow connections from application
- Create database schema and tables
- Enable automated backups

### 4. Amazon ElastiCache (Redis)
- Create Redis cluster (single node or cluster mode)
- Configure security group to allow connections from application
- Enable encryption in transit (SSL/TLS)
- Enable encryption at rest

### 5. IAM Role
Create IAM role with following policies:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::modresorts-data",
        "arn:aws:s3:::modresorts-data/*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": [
        "arn:aws:secretsmanager:*:*:secret:weather-api-key-*",
        "arn:aws:secretsmanager:*:*:secret:database-password-*",
        "arn:aws:secretsmanager:*:*:secret:redis-password-*"
      ]
    }
  ]
}
```

## Building the Application

```bash
# Build executable JAR
mvn clean package

# The output will be: target/modresorts-2.0.0.jar
```

## Running Locally

```bash
# Set environment variables
export AWS_REGION=us-east-1
export S3_BUCKET_NAME=modresorts-data
export DATABASE_URL=jdbc:postgresql://localhost:5432/modresorts
export DATABASE_USERNAME=modresorts
export DATABASE_PASSWORD=changeme
export REDIS_HOST=localhost
export REDIS_PORT=6379

# Run the application
java -jar target/modresorts-2.0.0.jar
```

## Deployment Options

### Option 1: AWS ECS (Fargate)
1. Build Docker image (separate workflow)
2. Push to Amazon ECR
3. Create ECS task definition with environment variables
4. Attach IAM role to task
5. Deploy to ECS cluster

### Option 2: AWS EKS (Kubernetes)
1. Build Docker image (separate workflow)
2. Push to Amazon ECR
3. Create Kubernetes deployment with ConfigMap and Secrets
4. Use IAM Roles for Service Accounts (IRSA)
5. Deploy to EKS cluster

### Option 3: AWS Elastic Beanstalk
1. Package as executable JAR
2. Create Elastic Beanstalk application
3. Configure environment variables
4. Attach IAM instance profile
5. Deploy JAR file

## Health Checks

The application exposes Spring Boot Actuator endpoints:

- **Health**: `http://localhost:8080/actuator/health`
- **Info**: `http://localhost:8080/actuator/info`
- **Metrics**: `http://localhost:8080/actuator/metrics`

Configure your load balancer to use `/actuator/health` for health checks.

## Monitoring and Logging

### CloudWatch Logs
- Application logs are written to stdout/stderr
- Configure log driver to send logs to CloudWatch Logs
- Use structured logging for better searchability

### CloudWatch Metrics
- Spring Boot Actuator metrics can be exported to CloudWatch
- Monitor JVM metrics, HTTP request metrics, database connection pool metrics

### X-Ray Tracing
- Add AWS X-Ray SDK for distributed tracing
- Trace requests across S3, RDS, ElastiCache, and Secrets Manager

## Security Best Practices

1. **Never hardcode credentials** - Use AWS Secrets Manager or environment variables
2. **Use IAM roles** - Attach IAM roles to ECS tasks or EC2 instances instead of access keys
3. **Enable encryption** - Use SSL/TLS for Redis, RDS, and S3
4. **Network isolation** - Deploy in private subnets with security groups
5. **Least privilege** - Grant only necessary IAM permissions
6. **Rotate secrets** - Enable automatic rotation in Secrets Manager

## Troubleshooting

### Application won't start
- Check environment variables are set correctly
- Verify IAM role has necessary permissions
- Check CloudWatch Logs for startup errors

### Cannot connect to RDS
- Verify security group allows inbound traffic from application
- Check DATABASE_URL is correct
- Verify database credentials

### Cannot connect to ElastiCache
- Verify security group allows inbound traffic from application
- Check REDIS_HOST and REDIS_PORT are correct
- Verify Redis password if authentication is enabled

### S3 operations fail
- Verify IAM role has S3 permissions
- Check S3_BUCKET_NAME is correct
- Verify bucket exists and is in the same region

### Secrets Manager operations fail
- Verify IAM role has SecretsManager:GetSecretValue permission
- Check secret name is correct
- Verify secret exists in the same region

## Migration Checklist

- [x] Convert WAR to executable JAR
- [x] Replace local file writes with S3
- [x] Replace hardcoded secrets with Secrets Manager
- [x] Replace WebSphere session management with Redis
- [x] Replace EJB with Spring Data JPA
- [x] Replace java.util.Date with java.time API
- [x] Add HikariCP connection pooling
- [x] Configure Spring Boot Actuator for health checks
- [x] Externalize all configuration to environment variables
- [ ] Create Docker image (separate workflow)
- [ ] Create Kubernetes manifests (separate workflow)
- [ ] Set up CI/CD pipeline (separate workflow)
- [ ] Configure CloudWatch monitoring
- [ ] Set up AWS X-Ray tracing
- [ ] Perform load testing
- [ ] Document runbooks for operations team
