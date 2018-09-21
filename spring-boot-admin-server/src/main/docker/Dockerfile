FROM java
VOLUME /tmp
ADD spring-boot-admin-server-0.0.1-SNAPSHOT.jar spring-boot-admin-server.jar
#RUN bash -c 'touch /spring-boot-admin-server.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}","/spring-boot-admin-server.jar"]
EXPOSE 8773