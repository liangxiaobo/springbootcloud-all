FROM java
VOLUME /tmp
ADD swagger-doc-0.0.1-SNAPSHOT.jar swagger-doc.jar
#RUN bash -c 'touch /swagger-doc.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/swagger-doc.jar"]
EXPOSE 8772