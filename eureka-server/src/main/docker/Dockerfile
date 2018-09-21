FROM java
VOLUME /tmp
ADD eureka-server-0.0.1-SNAPSHOT.jar eureka-server.jar
#RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}","/eureka-server.jar"]
EXPOSE 8761


# 打包命令 mvn clean package docker:build
# 运行容器命令 docker run --name eureka-server -p 8761:8761 -t liangwang/eureka-server:[tag]
# docker run --name eureka-server-01 -p 8761:8761 -t liangwang/eureka-server:0.0.1-SNAPSHOT --spring.profiles.active=test-peer1