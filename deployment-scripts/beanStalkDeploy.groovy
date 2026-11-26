// Elastic Beanstalk deployment helpers (Windows agent, uses bat)

def findLatestZip(Map cfg) {
    withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: cfg.credentialsId]]) {
        def key = bat(returnStdout: true, script: """
            @echo off
            aws s3api list-objects-v2 --region ${cfg.region} --bucket ${cfg.bucket} --prefix "${cfg.prefix}/" --query "sort_by(Contents[?ends_with(Key, '.zip')], &LastModified)[-1].Key" --output text
        """).trim()

        if (!key || key == 'None') {
            error "No .zip file found in s3://${cfg.bucket}/${cfg.prefix}/"
        }
        env.LATEST_KEY    = key
        env.VERSION_LABEL = "build-${env.BUILD_NUMBER}-" + new Date().format('yyyyMMddHHmmss')
        echo "Latest file : ${env.LATEST_KEY}"
        echo "Version     : ${env.VERSION_LABEL}"
    }
}

def createVersion(Map cfg) {
    withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: cfg.credentialsId]]) {
        bat """
            aws elasticbeanstalk create-application-version ^
              --region ${cfg.region} ^
              --application-name ${cfg.appName} ^
              --version-label ${env.VERSION_LABEL} ^
              --source-bundle S3Bucket=${cfg.bucket},S3Key=${env.LATEST_KEY} ^
              --description "Jenkins build ${env.BUILD_NUMBER}"
        """
    }
}

def deploy(Map cfg) {
    withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: cfg.credentialsId]]) {
        bat """
            aws elasticbeanstalk update-environment ^
              --region ${cfg.region} ^
              --application-name ${cfg.appName} ^
              --environment-name ${cfg.envName} ^
              --version-label ${env.VERSION_LABEL}
        """
    }
}

def waitForDeploy(Map cfg) {
    withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: cfg.credentialsId]]) {
        bat """
            aws elasticbeanstalk wait environment-updated ^
              --region ${cfg.region} ^
              --application-name ${cfg.appName} ^
              --environment-names ${cfg.envName}
            aws elasticbeanstalk describe-environments ^
              --region ${cfg.region} ^
              --environment-names ${cfg.envName} ^
              --query "Environments[0].[Status,Health,VersionLabel]" --output text
        """
    }
}

return this