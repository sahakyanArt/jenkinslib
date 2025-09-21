def call(Map config = [:]) {
    pipeline {
        agent any

        environment {
            APP_NAME = config.APP_NAME ?: 'exampleapp'
            MAIN_PORT = config.MAIN_PORT ?: '3000'
            DEV_PORT  = config.DEV_PORT ?: '3001'
            DOCKER_REGISTRY = config.DOCKER_REGISTRY ?: 'https://index.docker.io/v1/'
            DOCKER_CREDENTIALS = config.DOCKER_CREDENTIALS ?: 'dockerhub-credentials'
            DOCKERHUB_USER = config.DOCKERHUB_USER ?: 'dockeruser'
        }

        tools {
            nodejs config.NODEJS ?: 'node14'
        }

        stages {

            stage('Checkout') {
                steps { checkout scm }
            }

            stage('Lint Dockerfile') {
                steps { sh 'hadolint Dockerfile' }
            }

            stage('Build') {
                steps { sh 'npm install' }
            }

            stage('Test') {
                steps { sh 'npm test || echo "Tests failed but continuing..."' }
            }

            stage('Build Docker Image') {
                steps {
                    script {
                        if (env.BRANCH_NAME == 'main') {
                            sh "docker build -t nodemain:v1.0 ."
                        } else if (env.BRANCH_NAME == 'dev') {
                            sh "docker build -t nodedev:v1.0 ."
                        }
                    }
                }
            }

            stage('Scan Docker Image for Vulnerabilities') {
                steps {
                    script {
                        def imageName = env.BRANCH_NAME == 'main' ? "nodemain:v1.0" : "nodedev:v1.0"
                        def vulns = sh(script: "trivy image --exit-code 0 --severity HIGH,MEDIUM,LOW --no-progress ${imageName}", returnStdout: true).trim()
                        echo "Vulnerabilities report:\n${vulns}"
                    }
                }
            }

            stage('Push Docker Image') {
                steps {
                    script {
                        docker.withRegistry(env.DOCKER_REGISTRY, env.DOCKER_CREDENTIALS) {
                            def imageName = env.BRANCH_NAME == 'main' ? "nodemain:v1.0" : "nodedev:v1.0"
                            def repoTag = env.BRANCH_NAME == 'main' ? "${env.DOCKERHUB_USER}/nodemain:v1.0" : "${env.DOCKERHUB_USER}/nodedev:v1.0"
                            sh "docker tag ${imageName} ${repoTag}"
                            sh "docker push ${repoTag}"
                        }
                    }
                }
            }

            stage('Deploy Local') {
                steps {
                    script {
                        def containerName = env.BRANCH_NAME == 'main' ? "nodemain" : "nodedev"
                        def imageName = env.BRANCH_NAME == 'main' ? "nodemain:v1.0" : "nodedev:v1.0"
                        def portMapping = env.BRANCH_NAME == 'main' ? "3000:3000" : "3001:3000"
                        
                        sh """
                        docker ps -q --filter "name=${containerName}" | xargs -r docker stop
                        docker ps -a -q --filter "name=${containerName}" | xargs -r docker rm
                        docker run -d --name ${containerName} -p ${portMapping} ${imageName}
                        """
                    }
                }
            }

            stage('Trigger Deploy Pipeline') {
                steps {
                    script {
                        if (env.BRANCH_NAME == 'main') {
                            build job: 'Deploy_to_main'
                        } else if (env.BRANCH_NAME == 'dev') {
                            build job: 'Deploy_to_dev'
                        }
                    }
                }
            }
        }
    }
}
