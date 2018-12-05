# 请先拉取镜像
# docker pull openzipkin/zipkin

# 运行容器
# docker run -d -p 9411:9411 -e MYSQL_USER=root -e MYSQL_PASS=password -e MYSQL_HOST=192.168.0.8 -e STORAGE_TYPE=mysql openzipkin/zipkin
docker run -d -p 9411:9411 --name zipkin openzipkin/zipkin