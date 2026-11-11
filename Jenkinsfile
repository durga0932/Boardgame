
pipeline {
    agent any

    tools {
        jdk 'jdk21'
        maven 'maven3'
    }

    environment {
        DOCKER_IMAGE = 'durga0932/boardgame'
        IMAGE_TAG = "${BUILD_NUMBER}"
        K8S_NAMESPACE = 'boardgame'
    }

    stages {

        stage('Git Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Check Java and Maven') {
            steps {
                sh '''
                    java -version
                    mvn -version
                '''
            }
        }

        stage('Clean and Compile') {
            steps {
                sh 'mvn -B clean compile'
            }
        }

        stage('Unit Testing') {
            steps {
                sh 'mvn -B test'
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Code Coverage') {
            steps {
                sh 'mvn -B verify'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn -B sonar:sonar \
                        -Dsonar.projectKey=Boardgame \
                        -Dsonar.projectName=Boardgame
                    '''
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    docker build \
                    -t ${DOCKER_IMAGE}:${IMAGE_TAG} \
                    -t ${DOCKER_IMAGE}:latest .
                '''
            }
        }

        stage('Docker Image Scan') {
            steps {
                sh '''
                    trivy image \
                    --exit-code 0 \
                    --severity HIGH,CRITICAL \
                    ${DOCKER_IMAGE}:${IMAGE_TAG}
                '''
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {
                    sh '''
                        echo "$DOCKER_PASSWORD" | docker login \
                        -u "$DOCKER_USER" --password-stdin

                        docker push ${DOCKER_IMAGE}:${IMAGE_TAG}
                        docker push ${DOCKER_IMAGE}:latest

                        docker logout
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                withCredentials([
                    file(
                        credentialsId: 'kubeconfig-boardgame',
                        variable: 'KUBECONFIG_FILE'
                    )
                ]) {
                    sh '''
                        export KUBECONFIG="$KUBECONFIG_FILE"

                        kubectl apply -f deployment-service.yaml

                        kubectl set image \
                        deployment/boardgame-deployment \
                        boardgame=${DOCKER_IMAGE}:${IMAGE_TAG} \
                        -n ${K8S_NAMESPACE}

                        kubectl rollout status \
                        deployment/boardgame-deployment \
                        -n ${K8S_NAMESPACE} \
                        --timeout=180s
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'Boardgame CI/CD pipeline completed successfully!'
        }

        failure {
            echo 'Pipeline failed. Please check Jenkins console output.'
        }

        always {
            echo 'Pipeline execution completed.'
        }
    }
}
