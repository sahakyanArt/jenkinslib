def call(Map config) {
    def image = config.image ?: error("image is required")
    def container = config.container ?: image
    def port = config.port ?: "3000"

    script {
        sh """
        docker ps -q --filter "name=${container}" | xargs -r docker stop
        docker ps -a -q --filter "name=${container}" | xargs -r docker rm
        docker pull ${image}
        docker run -d --name ${container} -p ${port}:3000 ${image}
        """
    }
}
