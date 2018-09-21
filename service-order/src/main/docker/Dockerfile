FROM java
VOLUME /tmp
ADD service-order-0.0.1-SNAPSHOT.jar service-order.jar
#RUN bash -c 'touch /service-order.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}","/service-order.jar"]
EXPOSE 8764