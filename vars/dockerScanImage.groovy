def call(Map args) {
    def branch = args.branch
    def imageName = branch == 'main' ? 'nodemain:v1.0' : 'nodedev:v1.0'
    sh """
        docker run --rm \
            -e TRIVY_TIMEOUT=15m \
            -v /var/run/docker.sock:/var/run/docker.sock \
            -v /var/lib/trivy:/root/.cache/trivy \
            aquasec/trivy:latest image --scanners vuln \
            --exit-code 0 --severity HIGH,MEDIUM,LOW --no-progress ${imageName}
    """
}
