FROM java
VOLUME /tmp
ADD eureka-server-0.0.1-SNAPSHOT.jar app.jar
#RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
EXPOSE 8761


# 打包命令 mvn clean package docker:build
# 运行容器命令 docker run --name eureka-server -p 8761:8761 -t liangwang/eureka-server:[tag]