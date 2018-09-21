#!/bin/bash
# 用shell脚本启动docker容器

echo "容器创建开始"

# eureka-server docker 启动命令：
docker run -d -e "SPRING_PROFILES_ACTIVE=test-peer1" --name eureka-server -p 8761:8761 -it liangwang/eureka-server

# service-user-01 docker 启动命令：
docker run -d -e "SPRING_PROFILES_ACTIVE=test" --name service-user-01 --link=eureka-server -p 8763:8763 -it liangwang/service-user

# service-user-02 docker 启动命令：
docker run -d -e "SPRING_PROFILES_ACTIVE=test" --name service-user-02 --link=eureka-server -p 8863:8863 -it liangwang/service-user --server.port=8863


# service-order-01 docker 启动命令：
docker run  -d -e "SPRING_PROFILES_ACTIVE=test" --name service-order-01  --link=eureka-server -p 8764:8764 -it liangwang/service-order

# service-order-02 docker 启动命令：
docker run  -d -e "SPRING_PROFILES_ACTIVE=test" --name service-order-02  --link=eureka-server -p 8864:8864 -it liangwang/service-order --server.port=8864

# spring-boot-admin-server docker 启动命令：
docker run -d -e "SPRING_PROFILES_ACTIVE=test" --name spring-boot-admin-server  --link=eureka-server -p 8773:8773 -it liangwang/spring-boot-admin-server

# client-feign docker 启动命令：
docker run  -d -e "SPRING_PROFILES_ACTIVE=test" --name client-feign  --link=eureka-server -p 8765:8765 -it liangwang/client-feign

# client-order-ribbon docker 启动命令：
docker run -d -e "SPRING_PROFILES_ACTIVE=test" --name client-order-ribbon  --link=eureka-server -p 8766:8766 -it liangwang/client-order-ribbon

# client-gateway-zuul docker 启动命令：
docker run -d -e "SPRING_PROFILES_ACTIVE=test" --name client-gateway-zuul  --link=eureka-server -p 8771:8771 -it liangwang/client-gateway-zuul

# client-turbine-monitor docker 启动命令：
docker run -d -e "SPRING_PROFILES_ACTIVE=test" --name client-turbine-monitor  --link=eureka-server -p 8767:8767 -it liangwang/client-turbine-monitor

# swagger-doc docker 启动命令：
docker run  -d -e "SPRING_PROFILES_ACTIVE=test" --name swagger-doc  --link=eureka-server -p 8772:8772 -it liangwang/swagger-doc

echo "容器创建完成"