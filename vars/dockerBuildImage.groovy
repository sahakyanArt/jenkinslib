def call(Map args) {
    def branch = args.branch
    def imageName = branch == 'main' ? 'nodemain:v1.0' : 'nodedev:v1.0'
    sh "docker build -t ${imageName} ."
}
