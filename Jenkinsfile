pipeline {
    agent any

    environment {
        REPO_URL     = 'https://bitbucket.org/awseb1584/recorded.git'
        BRANCH       = 'main'                    // change if needed
        PACKAGE_DIR  = 'package'
        ZIP_NAME     = 'recorded.zip'

        // S3 upload settings
        AWS_CREDS_ID = 'aws config'              // Jenkins credential ID (AWS Credentials)
        S3_BUCKET    = 'elasticbeanstalk-ap-south-1-501591550267'
        AWS_REGION   = 'ap-south-1'
        S3_PREFIX    = 'recorded'                // folder inside the bucket
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
                    // helper scripts live in the repo's deployment-scripts/ folder (checked out above)
                    packaging = load 'deployment-scripts/packaging.groovy'
                    s3Upload  = load 'deployment-scripts/s3Upload.groovy'
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

        stage('Upload to S3') {
            steps {
                script {
                    s3Upload.uploadZip(
                        credentialsId: env.AWS_CREDS_ID,
                        bucket       : env.S3_BUCKET,
                        region       : env.AWS_REGION,
                        zipName      : env.ZIP_NAME,
                        prefix       : env.S3_PREFIX
                    )
                }
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: "${ZIP_NAME}", fingerprint: true
        }
        failure {
            echo 'Build failed'
        }
    }
}