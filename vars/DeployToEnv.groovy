def call(String envName, String image, String port = null) {
  if (!port) {
    port = (envName == 'main') ? '3000' : '3001'
  }
  def container = (envName == 'main') ? 'app-main' : 'app-dev'
  echo "DeployToEnv -> ${envName} ${image} ${port}"
  sh """
    EXIST=$(docker ps -a --filter "name=^/${container}$" -q)
    if [ -n "$EXIST" ]; then docker rm -f $EXIST || true; fi
    docker run -d --name ${container} --expose 3000 -p ${port}:3000 ${image}
  """
}