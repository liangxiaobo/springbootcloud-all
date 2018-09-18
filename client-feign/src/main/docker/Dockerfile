FROM java
VOLUME /tmp
ADD client-feign-0.0.1-SNAPSHOT.jar client-feign.jar
#RUN bash -c 'touch /client-feign.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/client-feign.jar"]
EXPOSE 8765