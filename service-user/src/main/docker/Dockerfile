FROM java
VOLUME /tmp
ADD service-user-0.0.1-SNAPSHOT.jar service-user.jar
#RUN bash -c 'touch /service-user.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}","/service-user.jar"]
EXPOSE 8763