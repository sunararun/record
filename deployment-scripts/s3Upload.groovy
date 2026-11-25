// deployment/s3Upload.groovy
// Loaded from the Jenkinsfile via: def s3Upload = load 'deployment/s3Upload.groovy'
//
// Usage:
//   s3Upload.uploadZip(
//       credentialsId: 'aws config',
//       bucket       : 'my-bucket',
//       region       : 'ap-south-1',
//       zipName      : 'recorded.zip',
//       prefix       : 'recorded'
//   )
//
// Requires on the Jenkins agent:
//   - AWS CLI installed and on PATH
//   - "AWS Credentials" plugin (AmazonWebServicesCredentialsBinding)
//   - a Jenkins credential (kind: AWS Credentials) with s3:PutObject on the bucket

def uploadZip(Map cfg) {
    // ---- validate inputs ----
    ['credentialsId', 'bucket', 'region', 'zipName', 'prefix'].each { key ->
        if (!cfg[key]) {
            error "s3Upload.uploadZip: missing required parameter '${key}'"
        }
    }

    // ---- upload ----
    withCredentials([[
                             $class           : 'AmazonWebServicesCredentialsBinding',
                             credentialsId    : cfg.credentialsId,
                             accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                             secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
                     ]]) {
        withEnv([
                "S3_BUCKET=${cfg.bucket}",
                "AWS_DEFAULT_REGION=${cfg.region}",
                "S3_ZIP_NAME=${cfg.zipName}",
                "S3_PREFIX=${cfg.prefix}"
        ]) {
            bat '''
            @echo off
            set ZIP_PATH=%WORKSPACE%\\%S3_ZIP_NAME%
            set S3_URI=s3://%S3_BUCKET%/%S3_PREFIX%/%BUILD_NUMBER%/%S3_ZIP_NAME%

            if not exist "%ZIP_PATH%" (
                echo ERROR: %S3_ZIP_NAME% not found in %WORKSPACE%
                exit /b 1
            )

            echo Uploading %ZIP_PATH% to %S3_URI%
            aws s3 cp "%ZIP_PATH%" "%S3_URI%" --region %AWS_DEFAULT_REGION%
            if errorlevel 1 (
                echo ERROR: S3 upload failed
                exit /b 1
            )

            echo Upload complete: %S3_URI%
            '''
        }
    }
}

// Required so `load` returns an object whose methods can be called
return this