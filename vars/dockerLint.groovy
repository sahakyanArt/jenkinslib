def call() {
    sh '''
        docker run --rm \
            -v $PWD:/workspace \
            -w /workspace \
            hadolint/hadolint \
            hadolint --no-fail Dockerfile
    '''
}
