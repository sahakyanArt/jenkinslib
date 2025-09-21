def call(Map args) {
    def branch = args.branch
    def name = branch == 'main' ? 'nodemain' : 'nodedev'
    def port = branch == 'main' ? '3000' : '3001'
    def image = branch == 'main' ? 'nodemain:v1.0' : 'nodedev:v1.0'
    
    sh """
        docker ps -q --filter "name=${name}" | xargs -r docker stop
        docker ps -a -q --filter "name=${name}" | xargs -r docker rm
        docker run -d --name ${name} -p ${port}:3000 ${image}
    """
}
