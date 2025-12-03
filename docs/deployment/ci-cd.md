# InTicky - CI/CD Pipeline

## Célközönség

Ez a dokumentum a CI/CD pipeline konfigurációját mutatja be. DevOps mérnökök számára készült.

## CI/CD Stack

**Választott:**
- **Jenkins** - CI/CD szerver
- **Azure Container Registry** - Container image registry
- **Azure App Service** - Deployment target

## Jenkins Setup

### Jenkins Szerver Konfiguráció

**Szükséges plugins:**
- Git Plugin
- Docker Pipeline Plugin
- Azure Plugin
- Maven Plugin
- NodeJS Plugin

**Credentials beállítása:**
- GitHub/GitLab access token
- Azure Service Principal
- Azure Container Registry credentials
- Docker Hub credentials (ha szükséges)

### Jenkinsfile Struktúra

**Root Jenkinsfile (monorepo):**
```groovy
pipeline {
    agent any
    
    environment {
        REGISTRY = 'inticky.azurecr.io'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        AZURE_RESOURCE_GROUP = 'inticky-rg'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm run build'
                }
            }
        }
        
        stage('Build Backend') {
            steps {
                dir('backend') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }
        
        stage('Run Tests') {
            parallel {
                stage('Frontend Tests') {
                    steps {
                        dir('frontend') {
                            sh 'npm test -- --run'
                        }
                    }
                }
                stage('Backend Tests') {
                    steps {
                        dir('backend') {
                            sh 'mvn test'
                        }
                    }
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                script {
                    // Backend services
                    def services = [
                        'api-gateway',
                        'auth-service',
                        'ticket-service',
                        'project-service',
                        'user-service',
                        'notification-service',
                        'file-service',
                        'ai-agent-service'
                    ]
                    
                    services.each { service ->
                        sh """
                            docker build -t ${REGISTRY}/${service}:${IMAGE_TAG} \
                                -t ${REGISTRY}/${service}:latest \
                                ./backend/${service}
                        """
                    }
                    
                    // Frontend
                    sh """
                        docker build -t ${REGISTRY}/frontend:${IMAGE_TAG} \
                            -t ${REGISTRY}/frontend:latest \
                            ./frontend
                    """
                }
            }
        }
        
        stage('Push to ACR') {
            steps {
                withCredentials([azureServicePrincipal(credentialsId: 'azure-credentials')]) {
                    sh 'az acr login --name inticky'
                    
                    script {
                        def services = [
                    'api-gateway', 'auth-service', 'ticket-service',
                    'project-service', 'user-service', 'notification-service',
                    'file-service', 'ai-agent-service', 'frontend'
                        ]
                        
                        services.each { service ->
                            sh "docker push ${REGISTRY}/${service}:${IMAGE_TAG}"
                            sh "docker push ${REGISTRY}/${service}:latest"
                        }
                    }
                }
            }
        }
        
        stage('Deploy to Staging') {
            steps {
                script {
                    def services = [
                    'api-gateway', 'auth-service', 'ticket-service',
                    'project-service', 'user-service', 'notification-service',
                    'file-service', 'ai-agent-service'
                    ]
                    
                    services.each { service ->
                        sh """
                            az webapp config container set \
                                --name inticky-${service}-staging \
                                --resource-group ${AZURE_RESOURCE_GROUP} \
                                --docker-custom-image-name ${REGISTRY}/${service}:${IMAGE_TAG}
                        """
                    }
                    
                    // Frontend
                    sh """
                        az staticwebapp deploy \
                            --name inticky-frontend-staging \
                            --resource-group ${AZURE_RESOURCE_GROUP} \
                            --app-location frontend/dist
                    """
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                input message: 'Deploy to production?', ok: 'Deploy'
                
                script {
                    def services = [
                    'api-gateway', 'auth-service', 'ticket-service',
                    'project-service', 'user-service', 'notification-service',
                    'file-service', 'ai-agent-service'
                    ]
                    
                    services.each { service ->
                        sh """
                            az webapp config container set \
                                --name inticky-${service}-prod \
                                --resource-group ${AZURE_RESOURCE_GROUP} \
                                --docker-custom-image-name ${REGISTRY}/${service}:${IMAGE_TAG}
                        """
                    }
                    
                    // Frontend
                    sh """
                        az staticwebapp deploy \
                            --name inticky-frontend-prod \
                            --resource-group ${AZURE_RESOURCE_GROUP} \
                            --app-location frontend/dist
                    """
                }
            }
        }
    }
    
    post {
        success {
            echo 'Pipeline succeeded!'
        }
        failure {
            echo 'Pipeline failed!'
            // Notification küldése
        }
        always {
            cleanWs()
        }
    }
}
```

## Pipeline Stages Részletezése

### 1. Checkout

**Cél:** Repository klónozása.

**Konfiguráció:**
- Branch: `develop` (staging), `main` (production)
- Credentials: GitHub/GitLab token

### 2. Build Frontend

**Cél:** React alkalmazás build-elése.

**Lépések:**
1. Dependencies telepítése (`npm ci`)
2. TypeScript compilation
3. Production build (`npm run build`)
4. Output: `frontend/dist/`

### 3. Build Backend

**Cél:** Java service-ek build-elése.

**Lépések:**
1. Maven dependencies (`mvn clean install`)
2. Compilation
3. Package creation (JAR fájlok)
4. Output: `backend/*/target/*.jar`

### 4. Run Tests

**Cél:** Unit és integration tesztek futtatása.

**Frontend:**
- Vitest unit tesztek
- Component tesztek

**Backend:**
- JUnit 5 unit tesztek
- REST Assured integration tesztek

**Ha tesztek sikertelenek:** Pipeline leáll, deployment nem történik.

### 5. Docker Build

**Cél:** Container image-ek készítése.

**Backend service-ek:**
- Minden service-hez külön Dockerfile
- Multi-stage build (optimalizált méret)
- Image tagging: `:latest` és `:BUILD_NUMBER`

**Frontend:**
- Nginx-alapú image
- Static file serving

### 6. Push to ACR

**Cél:** Image-ek feltöltése Azure Container Registry-be.

**Lépések:**
1. Azure login
2. ACR login
3. Image push (minden service)
4. Image tagging

### 7. Deploy to Staging

**Cél:** Staging környezetbe deployment.

**Backend:**
- Azure App Service container update
- Rolling deployment

**Frontend:**
- Azure Static Web Apps deployment

### 8. Deploy to Production

**Cél:** Production környezetbe deployment.

**Biztonsági megfontolások:**
- Manual approval szükséges
- Csak `main` branch-ről
- Rollback lehetőség

## Dockerfile Példák

### Backend Service Dockerfile

```dockerfile
# backend/ticket-service/Dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*-runner.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Megjegyzés:** Az `ai-agent-service` ugyanezt a Dockerfile struktúrát használja, de tartalmazza az AgentInSec AI Library dependency-t.

### Frontend Dockerfile

```dockerfile
# frontend/Dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## Environment Konfigurációk

### Staging Environment

**Azure App Service:**
- App Service Plan: `inticky-staging-plan`
- App Service nevek: `inticky-{service}-staging`
- Environment változók: `application-staging.yml`

### Production Environment

**Azure App Service:**
- App Service Plan: `inticky-prod-plan`
- App Service nevek: `inticky-{service}-prod`
- Environment változók: `application-prod.yml`

## További Információk

- [Azure Setup](./azure-setup.md)
- [Environments](./environments.md)
- [Monitoring](./monitoring.md)
- [Jenkins dokumentáció](https://www.jenkins.io/doc/)

