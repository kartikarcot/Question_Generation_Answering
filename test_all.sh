SHARE=$(pwd)
IMAGE=$1
NAME=$(sudo docker run -d -v ${SHARE}:/host/Users -it ${IMAGE} /bin/bash)
echo '****************'
for f in 1 2 3 4 5 6 7 8 9; do
	echo $f
	sudo docker exec -it $NAME ./ask /host/Users/data/set1/a$f.txt 20
done
sudo docker stop $NAME >/dev/null
