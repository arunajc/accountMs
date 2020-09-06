/*
    Helm install
 */
def helmInstall (release) {
    echo "Installing ${release}"

    script {
        sh "helm repo add helm ${HELM_REPO}; helm repo update"
        sh """
            helm upgrade --install ${release} helm_k8s
        """
        sh "sleep 5"
    }
}

/*
    Helm delete (if exists)
 */
def helmDelete (release) {
    echo "Deleting ${release} if deployed"

    script {
        sh "[ -z \"\$(helm ls --short ${release} 2>/dev/null)\" ] || helm delete --purge ${release}"
    }
}

pipeline {
    agent any
    parameters {
            string (name: 'APP_NAME', defaultValue: 'accountms', description: 'App name')
            string (name: 'GIT_BRANCH', defaultValue: 'master', description: 'Git branch to build')
            string(name: 'SONAR_HOST', defaultValue: 'http://localhost:9000', description: 'Sonar hostname')
             password(name: 'SONAR_TOKEN', defaultValue: '838532c7b6f7ecb9e94f794263d5dd7c814997ad', description: 'Sonar access token')
             password(name: 'SONAR_PROJECTID', defaultValue: 'com.mybank:accountMs', description: 'Sonar project Id')
             string (name: 'DOCKER_REG', defaultValue: 'registry.hub.docker.com/arunajc', description: 'Docker registry')
             string (name: 'DOCKER_TAG', defaultValue: 'dev', description: 'Docker tag')
        }
    tools {
        maven 'maven'
        jdk 'jdk'
		dockerTool 'docker'
    }

    environment {
            IMAGE_NAME = "${DOCKER_REG}/${APP_NAME}:${DOCKER_TAG}"
        }


    stages {
        stage ('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                '''
            }
        }

        stage ('Compile') {
            steps {
                sh 'mvn clean compile test-compile' 
            }
        }
		
		stage ('Unit Test') {
            steps {
                sh 'mvn test' 
            }
        }

        stage('Sonar Analysis') {
            environment {
                 SCANNER_HOME = tool 'SonarQubeScanner'
               }
          steps {
            withSonarQubeEnv('sonar') {
                sh '''$SCANNER_HOME/bin/sonar-scanner -Dsonar.projectKey=$SONAR_PROJECTID -Dsonar.language=java \
                -Dsonar.java.binaries=./target -Dsonar.java.codeCoveragePlugin=jacoco -Dsonar.coverage.jacoco.xmlReportPaths=/target/test-results/jacoco/jacoco.xml \
                 -Dsonar.exclusions=**/*.xml -Dsonar.coverage.exclusions=**/**/Application.*,**/**/config/*.*,**/**/constants/*.*,**/**/entity/*.*,**/**/exception/*.*,**/**/model/*.*'' \
                 -Dsonar.surefire.reportsPath=/target/test-results/surefire'''
            }
          }
        }
		
		stage ('Package') {
            steps {
                sh 'mvn package verify -Dmaven.test.skip=true -Dmaven.skip.tests' 
            }
        }
		
		stage ('Build Docker Image') {
			steps {
				echo 'Building the docker image'
				sh 'chmod 755 target/*.jar'
				sh 'docker build -f Dockerfile -t ${IMAGE_NAME} --no-cache .'
			}
        }
		
		stage ('Publish Docker Image') {
			steps {
				echo 'Publishing the docker image'
				sh 'docker push ${IMAGE_NAME}'
				echo 'Publishing docker image done'
			}
        }

         stage('Deploy to dev') {
                steps {
                    script {
                        branch = GIT_BRANCH.replaceAll('/', '-').replaceAll('\\*', '-')

                         ID = "${APP_NAME}-${DOCKER_TAG}-${branch}"

                        echo "Deploying application ${ID}"

                        // Remove release if exists
                        helmDelete (ID)

                        // Deploy with helm
                        echo "Deploying"
                        helmInstall(ID)
                    }
                }
            }
    }
}