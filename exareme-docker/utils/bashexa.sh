id=`docker ps | grep exaremelocal | awk -F" " '{print $1}'`
docker exec  -i -t $id  /bin/bash
