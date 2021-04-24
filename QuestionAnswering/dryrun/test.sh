SHARE=$(pwd)
IMAGE=$1
NAME=$(sudo docker run -d --network host -v ${SHARE}:/host/Users -it ${IMAGE} /bin/bash)
echo '****************'
sudo docker exec -it $NAME ./answer /host/Users/article2.txt /host/Users/questions2.txt
echo '****************'
sudo docker stop $NAME >/dev/null
