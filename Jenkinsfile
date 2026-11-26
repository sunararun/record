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

    parameters {
        string(name: 'EB_APP_NAME', defaultValue: '', description: 'Elastic Beanstalk application name')
        string(name: 'EB_ENV_NAME', defaultValue: '', description: 'Elastic Beanstalk environment name')
    }

   /* tools {
        maven 'Maven3'    // name configured in Manage Jenkins > Tools
        jdk   'JDK17'     // name configured in Manage Jenkins > Tools
    }*/

    stages {

        stage('Validate Parameters') {
            steps {
                script {
                    if (!params.EB_APP_NAME?.trim() || !params.EB_ENV_NAME?.trim()) {
                        error 'EB_APP_NAME and EB_ENV_NAME parameters are required.'
                    }
                }
            }
        }

        stage('Checkout') {
            steps {
                git branch: "${BRANCH}",
                        url: "${REPO_URL}",
                        credentialsId: 'BitBucketLink'   // Username/App password credential

                script {
                    // helper scripts live in the repo's deployment-scripts/ folder (checked out above)
                    packaging = load 'deployment-scripts/packaging.groovy'
                    s3Upload  = load 'deployment-scripts/s3Upload.groovy'
                    ebDeploy  = load 'deployment-scripts/ebDeploy.groovy'
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

        stage('Find Latest ZIP in S3') {
            steps {
                script {
                    ebDeploy.findLatestZip(
                        credentialsId: env.AWS_CREDS_ID,
                        bucket       : env.S3_BUCKET,
                        region       : env.AWS_REGION,
                        prefix       : env.S3_PREFIX
                    )
                }
            }
        }

        stage('Create EB Application Version') {
            steps {
                script {
                    ebDeploy.createVersion(
                        credentialsId: env.AWS_CREDS_ID,
                        bucket       : env.S3_BUCKET,
                        region       : env.AWS_REGION,
                        appName      : params.EB_APP_NAME
                    )
                }
            }
        }

        stage('Deploy to Elastic Beanstalk') {
            steps {
                script {
                    ebDeploy.deploy(
                        credentialsId: env.AWS_CREDS_ID,
                        region       : env.AWS_REGION,
                        appName      : params.EB_APP_NAME,
                        envName      : params.EB_ENV_NAME
                    )
                }
            }
        }

        stage('Wait for Deployment') {
            steps {
                script {
                    ebDeploy.waitForDeploy(
                        credentialsId: env.AWS_CREDS_ID,
                        region       : env.AWS_REGION,
                        appName      : params.EB_APP_NAME,
                        envName      : params.EB_ENV_NAME
                    )
                }
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: "${ZIP_NAME}", fingerprint: true
            echo "Deployed ${env.LATEST_KEY} to ${params.EB_ENV_NAME}"
        }
        failure {
            echo 'Build failed'
        }
    }
}