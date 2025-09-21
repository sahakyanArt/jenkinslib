def call(Map params = [:]) {
  def image = params.image ?: 'example/image:latest'
  def envName = params.envName ?: 'main'
  def port = params.port ?: (envName == 'main' ? '3000' : '3001')
  def container = (envName == 'main') ? 'app-main' : 'app-dev'

  echo "SharedLib: DeployToMaster called with image=${image} env=${envName} container=${container} port=${port}"
  sh """
    EXIST=$(docker ps -a --filter "name=^/${container}$" -q)
    if [ -n "$EXIST" ]; then docker rm -f $EXIST || true; fi
    docker run -d --name ${container} --expose 3000 -p ${port}:3000 ${image}
  """
}