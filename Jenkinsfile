// Jenkins must provide the following enviroment variables:
// CLONE_BASE: hostname + base path to the git repository (e.g: https://github.com/example)
// DEV_MAIL  : developer address to the mail for faild builds to (e.g: development@example.net)

// Jenkins must provide the following tools
// java-1.8.x : must reference an arbitrary jdk   1.8
pipeline {
    agent any

    stages {
        stage ("Cleanup") {
            steps {
                timestamps {
                    deleteDir()
                }
            }
        }
        stage ("Checkout") {
            steps {
                timestamps {
                    checkout(changelog: false, poll: false, scm: [$class: "GitSCM", branches: [[name: "*/master"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: "CloneOption", depth: 0, noTags: true, reference: "", shallow: true]], submoduleCfg: [], userRemoteConfigs: [[url: "${env.CLONE_BASE}/package-info-maven-plugin.git"]]])
                }
            }
        }
        stage ("Build") {
            steps {
                timestamps {
                    withMaven(jdk: "java-1.8.x") {
                        sh("./mvnw --update-snapshots -Dgpg.skip=true -Djarsigner.skip=true clean verify")
                    }
                }
            }
            post {
                always {
                    jacoco(execPattern: "**/jacoco.exec", exclusionPattern: "de/shadowhunt/maven/plugins/packageinfo/HelpMojo.class", inclusionPattern: "de/shadowhunt/maven/plugins/packageinfo/**/*")
                    junit(allowEmptyResults: true, keepLongStdio: true, testResults: "*/target/failsafe-reports/**/*.xml, */target/surefire-reports/**/*.xml")
                }
                failure {
                    mail(body: "See <${env.BUILD_URL}>", subject: "Jenkins build has failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}", to: "${env.DEV_MAIL}")
                }
            }
        }
    }
}
