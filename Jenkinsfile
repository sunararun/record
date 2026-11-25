pipeline {
    agent any

    environment {
        REPO_URL    = 'https://bitbucket.org/awseb1584/recorded.git'
        BRANCH      = 'main'                    // change if needed
        PACKAGE_DIR = 'package'
        ZIP_NAME    = 'recorded.zip'
    }

   /* tools {
        maven 'Maven3'    // name configured in Manage Jenkins > Tools
        jdk   'JDK17'     // name configured in Manage Jenkins > Tools
    }*/

    stages {

        stage('Checkout') {
            steps {
                git branch: "${BRANCH}",
                        url: "${REPO_URL}",
                        credentialsId: 'BitBucketLink'   // Username/App password credential

                script {
                    // deployment/packaging.groovy must be in the repo (checked out above)
                    packaging = load 'deployment/packaging.groovy'
                }
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Prepare Package') {
            steps {
                script {
                    packaging.preparePackage()
                }
            }
        }

        stage('Create Zip') {
            steps {
                script {
                    packaging.createZip()
                }
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'recorded.zip', fingerprint: true
        }
        failure {
            echo 'Build failed'
        }
    }
}
