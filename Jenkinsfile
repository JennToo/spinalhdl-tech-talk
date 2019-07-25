#!groovy
// TODO: Replace with a general pipeline script

@Library(['adtran-pipeline-library@v1.17.0']) _

pipeline {
    agent { label 'common_builder' }

    parameters {
        booleanParam(
                name: 'RELEASE_PACKAGE',
                defaultValue: false,
                description: 'Enables publishing package to Artifactory when true')
    }

    options {
        buildDiscarder(logRotator(numToKeepStr:'100'))
        timestamps()
    }

    environment {
        TWINE_USERNAME = "${ARTIFACTORY_USR}"
        TWINE_PASSWORD = "${ARTIFACTORY_PSW}"
        TWINE_REPOSITORY_URL = "https://artifactory.adtran.com/artifactory/api/pypi/pypi"
    }

    stages {
        stage('Test') {
            steps {
                withVenv("python3") {
                    pysh "pip install porg"
                    pysh "porg check"
                    pysh "porg test"
                    pysh "porg doc"
                }
            }

            post {
                always {
                    junit ".porg/test_results.xml"
                }
            }
        }

        stage('Publish') {
            when {
                allOf {
                    branch 'master'
                    expression {
                        params.RELEASE_PACKAGE == true
                    }
                }
            }

            steps {
                script {
                    milestone(0)
                    withVenv("python3") {
                        pysh "pip install porg"
                        def package_version = pysh(returnStdout: true, script: "porg get-version").trim()
                        sshagent(['github.adtran.com-SSH']) {
                            sh("git tag -a v${package_version} -m 'Jenkins auto release'")
                            sh("git push origin v${package_version}")

                            sh "bash .porg/docs/publish.sh"
                        }
                        pysh "porg publish"
                    }
                }
            }
        }
    }

    post { always { deleteDir() } }
}
