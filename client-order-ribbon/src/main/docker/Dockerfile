FROM java
VOLUME /tmp
ADD client-order-ribbon-0.0.1-SNAPSHOT.jar client-order-ribbon.jar
#RUN bash -c 'touch /client-feign.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/client-order-ribbon.jar"]
EXPOSE 8766