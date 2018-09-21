FROM java
VOLUME /tmp
ADD client-gateway-zuul-0.0.1-SNAPSHOT.jar client-gateway-zuul.jar
#RUN bash -c 'touch /client-gateway-zuul.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}","/client-gateway-zuul.jar"]
EXPOSE 8771