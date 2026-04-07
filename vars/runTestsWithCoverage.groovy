void call(Map config) {
    withCredentials([
            file(
                    credentialsId: config.project_env,
                    variable: 'credvar')
    ]) {
        sh """
               rm -f .env
               cp \${credvar} .env
               docker compose -p ${config.project_name} --profile testing build tester
               docker compose up tester
               CONTAINER_ID=\$(docker compose ps -qa tester)
               echo "Exit status was \$(docker inspect \${CONTAINER_ID} --format='{{.State.ExitCode}}')"
               docker cp \${CONTAINER_ID}:/usr/src/app/.coverage .coverage
               docker cp \${CONTAINER_ID}:/usr/src/app/coverage.xml coverage.xml
               docker cp \${CONTAINER_ID}:/usr/src/app/htmlcov htmlcov
           """
    }
}
