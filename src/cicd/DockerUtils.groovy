package cicd

class DockerUtils implements Serializable {
    def script
    DockerUtils(script) { this.script = script }

    def lint() {
        script.sh '''
            docker run --rm -v $PWD:/workspace -w /workspace hadolint/hadolint hadolint --no-fail Dockerfile
        '''
    }

    def buildImage(branch) {
        def imageName = branch == 'main' ? 'nodemain:v1.0' : 'nodedev:v1.0'
        script.sh "docker build -t ${imageName} ."
    }

    def scanImage(branch) {
        def imageName = branch == 'main' ? 'nodemain:v1.0' : 'nodedev:v1.0'
        script.sh """
            docker run --rm -e TRIVY_TIMEOUT=15m \
                -v /var/run/docker.sock:/var/run/docker.sock \
                -v /var/lib/trivy:/root/.cache/trivy \
                aquasec/trivy:latest image --scanners vuln \
                --exit-code 0 --severity HIGH,MEDIUM,LOW --no-progress ${imageName}
        """
    }

    def deploy(branch) {
        def name = branch == 'main' ? 'nodemain' : 'nodedev'
        def port = branch == 'main' ? '3000' : '3001'
        def image = branch == 'main' ? 'nodemain:v1.0' : 'nodedev:v1.0'
        script.sh """
            docker ps -q --filter "name=${name}" | xargs -r docker stop
            docker ps -a -q --filter "name=${name}" | xargs -r docker rm
            docker run -d --name ${name} -p ${port}:3000 ${image}
        """
    }
}
