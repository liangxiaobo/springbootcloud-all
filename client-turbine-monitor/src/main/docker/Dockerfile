FROM java
VOLUME /tmp
ADD client-turbine-monitor-0.0.1-SNAPSHOT.jar client-turbine-monitor.jar
#RUN bash -c 'touch /client-gateway-zuul.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/client-turbine-monitor.jar"]
EXPOSE 8767