pipeline {
    agent any

    environment {
        REPO_URL   = 'https://bitbucket.org/awseb1584/recorded.git'
        BRANCH     = 'main'                    // change if needed
        PACKAGE_DIR = 'package'
        ZIP_NAME   = 'recorded.zip'
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
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Prepare Package') {
            steps {
                bat '''
                @echo off
                if exist %PACKAGE_DIR% rmdir /s /q %PACKAGE_DIR%
                if exist %ZIP_NAME% del /q %ZIP_NAME%

                mkdir %PACKAGE_DIR%
                mkdir %PACKAGE_DIR%\\target

                if exist .ebextensions (
                    xcopy ".ebextensions" "%PACKAGE_DIR%\\.ebextensions" /E /I /Y /H
                ) else (
                    echo WARNING: .ebextensions folder not found
                )

                if exist Procfile (
                    copy /Y Procfile "%PACKAGE_DIR%\\Procfile"
                ) else (
                    echo WARNING: Procfile not found
                )

                copy /Y target\\*.jar "%PACKAGE_DIR%\\target\\"
                if errorlevel 1 (
                    echo ERROR: No jar found in target folder
                    exit /b 1
                )

                echo ===== Package contents =====
                dir /s /b %PACKAGE_DIR%
                '''
            }
        }

        stage('Create Zip') {
            steps {
                bat '''
                @echo off
                powershell -NoProfile -ExecutionPolicy Bypass -Command "Add-Type -AssemblyName System.IO.Compression.FileSystem; $src=(Resolve-Path '%PACKAGE_DIR%').Path; $dst=Join-Path (Get-Location).Path '%ZIP_NAME%'; $z=[IO.Compression.ZipFile]::Open($dst,'Create'); Get-ChildItem $src -Recurse -File -Force | ForEach-Object { $rel=$_.FullName.Substring($src.Length+1).Replace([string][char]92,'/'); [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($z,$_.FullName,$rel) | Out-Null; Write-Host ('Added: ' + $rel) }; $z.Dispose()"
                if errorlevel 1 exit /b 1
                dir %ZIP_NAME%
                '''
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