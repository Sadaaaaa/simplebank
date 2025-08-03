pipeline {
    agent any
    
    environment {
        DOCKER_REGISTRY = 'your-registry.com'
        KUBERNETES_NAMESPACE = 'simplebank'
        HELM_CHART_PATH = 'helm-charts'
        VERSION = "${env.BUILD_NUMBER}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Validate Helm Charts') {
            steps {
                script {
                    sh '''
                        echo "Validating Helm charts..."
                        helm lint ${HELM_CHART_PATH}
                        
                        # Validate each subchart
                        for chart in ${HELM_CHART_PATH}/charts/*; do
                            if [ -d "$chart" ]; then
                                echo "Validating chart: $(basename $chart)"
                                helm lint $chart
                            fi
                        done
                    '''
                }
            }
        }
        
        stage('Build and Test') {
            parallel {
                stage('Build Auth Server') {
                    steps {
                        script {
                            buildMicroservice('auth-server', '9000')
                        }
                    }
                }
                
                stage('Build Accounts') {
                    steps {
                        script {
                            buildMicroservice('accounts', '8082')
                        }
                    }
                }
                
                stage('Build Cash') {
                    steps {
                        script {
                            buildMicroservice('cash', '8083')
                        }
                    }
                }
                
                stage('Build Transfer') {
                    steps {
                        script {
                            buildMicroservice('transfer', '8084')
                        }
                    }
                }
                
                stage('Build Exchange') {
                    steps {
                        script {
                            buildMicroservice('exchange', '8086')
                        }
                    }
                }
                
                stage('Build Exchange Generator') {
                    steps {
                        script {
                            buildMicroservice('exchange-generator', '8085')
                        }
                    }
                }
                
                stage('Build Blocker') {
                    steps {
                        script {
                            buildMicroservice('blocker', '8087')
                        }
                    }
                }
                
                stage('Build Notifications') {
                    steps {
                        script {
                            buildMicroservice('notifications', '8088')
                        }
                    }
                }
                
                stage('Build Gateway') {
                    steps {
                        script {
                            buildMicroservice('gateway', '8081')
                        }
                    }
                }
                
                stage('Build Front UI') {
                    steps {
                        script {
                            buildMicroservice('front-ui', '8080')
                        }
                    }
                }
            }
        }
        
        stage('Integration Tests') {
            steps {
                script {
                    sh '''
                        echo "Running integration tests..."
                        # Здесь можно добавить интеграционные тесты
                        # например, тестирование API endpoints
                    '''
                }
            }
        }
        
        stage('Build Docker Images') {
            parallel {
                stage('Build Auth Server Image') {
                    steps {
                        script {
                            buildDockerImage('auth-server')
                        }
                    }
                }
                
                stage('Build Accounts Image') {
                    steps {
                        script {
                            buildDockerImage('accounts')
                        }
                    }
                }
                
                stage('Build Cash Image') {
                    steps {
                        script {
                            buildDockerImage('cash')
                        }
                    }
                }
                
                stage('Build Transfer Image') {
                    steps {
                        script {
                            buildDockerImage('transfer')
                        }
                    }
                }
                
                stage('Build Exchange Image') {
                    steps {
                        script {
                            buildDockerImage('exchange')
                        }
                    }
                }
                
                stage('Build Exchange Generator Image') {
                    steps {
                        script {
                            buildDockerImage('exchange-generator')
                        }
                    }
                }
                
                stage('Build Blocker Image') {
                    steps {
                        script {
                            buildDockerImage('blocker')
                        }
                    }
                }
                
                stage('Build Notifications Image') {
                    steps {
                        script {
                            buildDockerImage('notifications')
                        }
                    }
                }
                
                stage('Build Gateway Image') {
                    steps {
                        script {
                            buildDockerImage('gateway')
                        }
                    }
                }
                
                stage('Build Front UI Image') {
                    steps {
                        script {
                            buildDockerImage('front-ui')
                        }
                    }
                }
            }
        }
        
        stage('Push Docker Images') {
            steps {
                script {
                    sh '''
                        echo "Pushing Docker images to registry..."
                        docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD $DOCKER_REGISTRY
                        
                        for service in auth-server accounts cash transfer exchange exchange-generator blocker notifications gateway front-ui; do
                            docker tag simplebank/$service:$VERSION $DOCKER_REGISTRY/simplebank/$service:$VERSION
                            docker push $DOCKER_REGISTRY/simplebank/$service:$VERSION
                            docker tag simplebank/$service:$VERSION $DOCKER_REGISTRY/simplebank/$service:latest
                            docker push $DOCKER_REGISTRY/simplebank/$service:latest
                        done
                    '''
                }
            }
        }
        
        stage('Deploy to Test Environment') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    deployToEnvironment('test')
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                script {
                    deployToEnvironment('production')
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}

def buildMicroservice(serviceName, port) {
    echo "Building ${serviceName} microservice..."
    
    dir(serviceName) {
        sh '''
            echo "Cleaning previous build..."
            mvn clean
            
            echo "Compiling..."
            mvn compile
            
            echo "Running tests..."
            mvn test
            
            echo "Building package..."
            mvn package -DskipTests
        '''
    }
}

def buildDockerImage(serviceName) {
    echo "Building Docker image for ${serviceName}..."
    
    sh """
        docker build -t simplebank/${serviceName}:${VERSION} -f ${serviceName}/Dockerfile .
        docker tag simplebank/${serviceName}:${VERSION} simplebank/${serviceName}:latest
    """
}

def deployToEnvironment(environment) {
    echo "Deploying to ${environment} environment..."
    
    script {
        // Обновляем версии образов в values.yaml
        sh """
            sed -i 's/tag: ".*"/tag: "${VERSION}"/g' ${HELM_CHART_PATH}/values.yaml
            
            # Обновляем registry в values.yaml
            sed -i 's|repository: simplebank/|repository: ${DOCKER_REGISTRY}/simplebank/|g' ${HELM_CHART_PATH}/values.yaml
        """
        
        // Развертываем в Kubernetes
        sh """
            # Добавляем репозиторий Helm если нужно
            helm repo add bitnami https://charts.bitnami.com/bitnami
            
            # Обновляем зависимости
            helm dependency update ${HELM_CHART_PATH}
            
            # Устанавливаем/обновляем релиз
            helm upgrade --install simplebank-${environment} ${HELM_CHART_PATH} \
                --namespace ${KUBERNETES_NAMESPACE}-${environment} \
                --create-namespace \
                --set global.imageRegistry=${DOCKER_REGISTRY} \
                --wait --timeout=10m
        """
        
        // Проверяем статус развертывания
        sh """
            kubectl get pods -n ${KUBERNETES_NAMESPACE}-${environment}
            kubectl get services -n ${KUBERNETES_NAMESPACE}-${environment}
            kubectl get ingress -n ${KUBERNETES_NAMESPACE}-${environment}
        """
    }
} 