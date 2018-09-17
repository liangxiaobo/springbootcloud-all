FROM java
VOLUME /tmp
ADD service-user-0.0.1-SNAPSHOT.jar app2.jar
#RUN bash -c 'touch /app2.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app2.jar"]
EXPOSE 8763