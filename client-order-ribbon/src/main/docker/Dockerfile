FROM java
VOLUME /tmp
ADD client-order-ribbon-0.0.1-SNAPSHOT.jar client-order-ribbon.jar
#RUN bash -c 'touch /client-feign.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}","/client-order-ribbon.jar"]
EXPOSE 8766