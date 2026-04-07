void call(Map config) {
    sh "docker compose -p ${config.project_name} build"
}
