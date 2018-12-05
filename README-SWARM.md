> 本次会尝试将上一篇 [spring cloud 快速学习教程](https://www.jianshu.com/p/2e3953fce6c0)项目部署在Swarm集群中，如何搭建swarm集群请看[Docker Swarm集成搭建](https://www.jianshu.com/p/cd01033a21e6)
源码github分支**master-swarm** https://github.com/liangxiaobo/springbootcloud-all.git

## 1. swarm环境
基于[Docker Swarm集成搭建](https://www.jianshu.com/p/cd01033a21e6)的基础上，我的三台测试机

| IP  |角色 |
| ------ | ------ |
|172.16.10.85| manager|
|172.16.10.86| worker|
|172.16.10.87| worker|

我的自建Docker私库地址 172.16.10.192:5000

## 2. 优化springcloud项目
优化项目的配置文件注释掉显示真实IP，并给应用给加hostname，举例：
```
eureka:
  instance:
    leaseRenewalIntervalInSeconds: 10
    health-check-url-path: /actuator/health
#    prefer-ip-address: true
    hostname: service-user
```
注释的目的是让hostname和swarm service的name相同，这然在容器内部就可以使用http://hostname:port访问了，因为swarm内置的DNS使用servicename就可以访问

### 2.1 打包项目镜像
从github上下载分支 master-swarm
```bash
git clone -b master-swarm https://github.com/liangxiaobo/springbootcloud-all.git
```
如果你有自己的私库请将根目录的pom.xml中的 docker.image.prefix 改为自己的或公共库地址
如果不上传镜像的话，在创建service时会报找不到镜像的错误异常

打包docker镜像命令可以在项目根目录执行:
```bash
sh mvn-package-docker.sh
```
也可以手动在需要的项目下执行：
```bash
mvn package docker:build -Dmaven.test.skip=true
```
### 2.2 上传镜像到私有库中
查看打包出来的镜像：
```bash
[root@swarm-m service-user]# docker images
REPOSITORY                                    TAG                 IMAGE ID            CREATED             SIZE
172.16.10.192:5000/service-user               latest              1454fd81bb0c        42 minutes ago      692MB
172.16.10.192:5000/spring-boot-admin-server   latest              7b650dd6b8e9        12 hours ago        697MB
172.16.10.192:5000/swagger-doc                latest              03461f85d700        14 hours ago        689MB
172.16.10.192:5000/client-turbine-monitor     latest              a13d83c7ae8a        14 hours ago        692MB
172.16.10.192:5000/client-gateway-zuul        latest              379566b57536        14 hours ago        694MB
172.16.10.192:5000/client-order-ribbon        latest              e352da8519ec        14 hours ago        694MB
172.16.10.192:5000/client-feign               latest              5f8a2769bf61        14 hours ago        694MB
172.16.10.192:5000/service-order              latest              aacca65e148b        14 hours ago        692MB
172.16.10.192:5000/eureka-server              latest              dfc6e58fadf4        14 hours ago        691MB
```
上传镜像需要先登录到私库：
```bash 
docker login -u 用户名 -p 密码 172.16.10.192:5000<私库的IP:PORT>
```
其它节点要也登录到私库服务器

登出的话:
```bash
docker logout 172.16.10.192:5000<私库的IP:PORT>
```
执行上传命令:
```bash
docker push 172.16.10.192:5000/eureka-server
# docker push <IP:PORT/镜像名>
```
查看上传的镜像：
![WX20181017-113641@2x.png](https://upload-images.jianshu.io/upload_images/2151905-ff403fed36886e45.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/760)

##3. 创建swarm service
这里特别说明一下，如果是在私有库上拉镜像需要在 docker service create 上加 ```--with-registry-auth```，否则会报错，下会演示：
```bash
[root@swarm-m service-user]# docker service create  --name service-user3 --replicas 3 --network my-overlay-network --publish 8863:8763 -e "SPRING_PROFILES_ACTIVE=test" 172.16.10.192:5000/service-user
image 172.16.10.192:5000/service-user:latest could not be accessed on a registry to record
its digest. Each node will access 172.16.10.192:5000/service-user:latest independently,
possibly leading to different nodes running different
versions of the image.

0rslh6ebjm0xpf9dbw6z6ou1b
overall progress: 1 out of 3 tasks 
1/3: No such image: 172.16.10.192:5000/service-user:latest 
2/3: No such image: 172.16.10.192:5000/service-user:latest 
3/3: running   [==================================================>] 
^COperation continuing in background.
```
为会么要加 ```--with-registry-auth ``` 官网上有解释：
https://docs.docker.com/engine/reference/commandline/service_create/#create-a-service
![WX20181017-115328@2x.png](https://upload-images.jianshu.io/upload_images/2151905-29ce8c48adb78d60.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/760)

大概是说：这能将登录令牌从本地客户端传递到部署服务的swarm节点，从而是节点能登陆到私有注册表拉取镜像

###3.2. 执行docker service create
这是要创建服务的命令：
```--network my-overlay-network``` 是自定义网络
```bash
docker service create --name zipkin-service --replicas 2 --network my-overlay-network --publish 9411:9411  openzipkin/zipkin

docker service create --with-registry-auth --name eureka-server --replicas 2 --network my-overlay-network --publish 8761:8761 -e "SPRING_PROFILES_ACTIVE=test-peer1" 172.16.10.192:5000/eureka-server

docker service create --with-registry-auth --name service-user --replicas 2 --network my-overlay-network --publish 8763:8763 -e "SPRING_PROFILES_ACTIVE=test" 172.16.10.192:5000/service-user

docker service create --with-registry-auth --name service-order --replicas 2 --network my-overlay-network --publish 8764:8764 -e "SPRING_PROFILES_ACTIVE=test" 172.16.10.192:5000/service-order

docker service create --with-registry-auth --name spring-boot-admin-server --replicas 1 --network my-overlay-network --publish 8773:8773 -e "SPRING_PROFILES_ACTIVE=test" 172.16.10.192:5000/spring-boot-admin-server

docker service create --with-registry-auth --name client-order-ribbon --replicas 2 --network my-overlay-network --publish 8766:8766 -e "SPRING_PROFILES_ACTIVE=test" 172.16.10.192:5000/client-order-ribbon

docker service create --with-registry-auth --name client-feign --replicas 2 --network my-overlay-network --publish 8765:8765 -e "SPRING_PROFILES_ACTIVE=test" 172.16.10.192:5000/client-feign

docker service create --with-registry-auth --name client-gateway-zuul --replicas 2 --network my-overlay-network --publish 8771:8771 -e "SPRING_PROFILES_ACTIVE=test" 172.16.10.192:5000/client-gateway-zuul

docker service create --with-registry-auth --name client-turbine-monitor --replicas 2 --network my-overlay-network --publish 8767:8767 -e "SPRING_PROFILES_ACTIVE=test" 172.16.10.192:5000/client-turbine-monitor
```
服务创建完可以查看 
```bash
[root@swarm-m /]# docker service ls
ID                  NAME                       MODE                REPLICAS            IMAGE                                                PORTS
lbwpqv24hw9b        client-feign               replicated          2/2                 172.16.10.192:5000/client-feign:latest               *:8765->8765/tcp
vwtnddgl94ck        client-gateway-zuul        replicated          2/2                 172.16.10.192:5000/client-gateway-zuul:latest        *:8771->8771/tcp
14vmm45dtnl9        client-order-ribbon        replicated          2/2                 172.16.10.192:5000/client-order-ribbon:latest        *:8766->8766/tcp
y8twu0mclhia        client-turbine-monitor     replicated          2/2                 172.16.10.192:5000/client-turbine-monitor:latest     *:8767->8767/tcp
p0sy1vwrvq6f        eureka-server              replicated          2/2                 172.16.10.192:5000/eureka-server:latest              *:8761->8761/tcp
zqlpb42ipqhu        service-order              replicated          2/2                 172.16.10.192:5000/service-order:latest              *:8764->8764/tcp
kh89t4hpgr70        service-user               replicated          2/2                 172.16.10.192:5000/service-user:latest               *:8763->8763/tcp
0rslh6ebjm0x        service-user3              replicated          3/3                 172.16.10.192:5000/service-user:latest               *:8863->8763/tcp
25ji5lwx66cq        spring-boot-admin-server   replicated          1/1                 172.16.10.192:5000/spring-boot-admin-server:latest   *:8773->8773/tcp
iptkiejwkuyu        zipkin-service             replicated          2/2                 openzipkin/zipkin:latest                             *:9411->9411/tcp
```
访问 [http://172.16.10.85:8761/](http://172.16.10.85:8761/)
![WX20181017-142759@2x.png](https://upload-images.jianshu.io/upload_images/2151905-4d3213a5a5b197c8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/760)

访问 [http://172.16.10.85:8773](http://172.16.10.85:8773) 并用admin登录
![WX20181017-142956@2x.png](https://upload-images.jianshu.io/upload_images/2151905-c42d398fbd07348d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/760)

##4. 使用stack发布服务
编写docker-compose.yml
```xml
version: '3'

services:

  zipkin-service:
    image: openzipkin/zipkin:latest
    deploy:
      mode: replicated
      replicas: 2
      restart_policy:
        condition: on-failure
    ports:
      - "9411:9411"
    networks:
      my-overlay-network:
        aliases:
          - zipkin-service
    environment:
        - "SPRING_PROFILES_ACTIVE=test"

  eureka-server:
    image: 172.16.10.192:5000/eureka-server:latest
    deploy:
      mode: replicated
      replicas: 2
      restart_policy:
        condition: on-failure
#      placement:
#        constraints: [node.role == worker]
    ports:
      - "8761:8761"
    networks:
      my-overlay-network:
        aliases:
          - eureka-server
    environment:
        - "SPRING_PROFILES_ACTIVE=test-peer1"

  service-user:
    image: 172.16.10.192:5000/service-user:latest
    deploy:
      mode: replicated
      replicas: 2
      restart_policy:
        condition: on-failure
    ports:
      - "8763:8763"
    networks:
      my-overlay-network:
        aliases:
          - service-user
    environment:
        - "SPRING_PROFILES_ACTIVE=test"

  service-order:
    image: 172.16.10.192:5000/service-order:latest
    deploy:
      mode: replicated
      replicas: 2
      restart_policy:
        condition: on-failure
    ports:
      - "8764:8764"
    networks:
      my-overlay-network:
        aliases:
          - service-order
    environment:
        - "SPRING_PROFILES_ACTIVE=test"

  client-order-ribbon:
    image: 172.16.10.192:5000/client-order-ribbon:latest
    deploy:
      mode: replicated
      replicas: 2
      restart_policy:
        condition: on-failure
    ports:
      - "8766:8766"
    networks:
      my-overlay-network:
        aliases:
          - client-order-ribbon
    environment:
        - "SPRING_PROFILES_ACTIVE=test"

  client-feign:
    image: 172.16.10.192:5000/client-feign:latest
    deploy:
      mode: replicated
      replicas: 2
      restart_policy:
        condition: on-failure
    ports:
      - "8765:8765"
    networks:
      my-overlay-network:
        aliases:
          - client-feign
    environment:
        - "SPRING_PROFILES_ACTIVE=test"

  client-gateway-zuul:
    image: 172.16.10.192:5000/client-gateway-zuul:latest
    deploy:
      mode: replicated
      replicas: 2
      restart_policy:
        condition: on-failure
    ports:
      - "8771:8771"
    networks:
      my-overlay-network:
        aliases:
          - client-gateway-zuul
    environment:
        - "SPRING_PROFILES_ACTIVE=test"

  client-turbine-monitor:
    image: 172.16.10.192:5000/client-turbine-monitor:latest
    deploy:
      mode: replicated
      replicas: 2
      restart_policy:
        condition: on-failure
    ports:
      - "8767:8767"
    networks:
      my-overlay-network:
        aliases:
          - client-turbine-monitor
    environment:
        - "SPRING_PROFILES_ACTIVE=test"

  spring-boot-admin-server:
    image: 172.16.10.192:5000/spring-boot-admin-server:latest
    deploy:
      mode: replicated
      replicas: 1
      restart_policy:
        condition: on-failure
    ports:
      - "8773:8773"
    networks:
      my-overlay-network:
        aliases:
          - spring-boot-admin-server
    environment:
        - "SPRING_PROFILES_ACTIVE=test"



  visualizer:
    image: dockersamples/visualizer:stable
    ports:
      - "8080:8080"
    volumes:
      - "/var/run/docker.sock:/var/run/docker.sock"
    deploy:
      placement:
        constraints: [node.role == manager]
    networks:
      - my-overlay-network

networks:
  my-overlay-network:
    driver: overlay

```
配置中配置的自定义网络
```
networks:
  my-overlay-network:
    driver: overlay
```
还可以在网络中配置别名
```
networks:
      my-overlay-network:
        aliases:
          - spring-boot-admin-server
```

执行命令:
如果是私有库必须加上 --with-registry-auth 否则镜像下载不了
```bash
docker stack deploy -c docker-compose.yml --with-registry-auth app
```
公共库可以使用:
```bash
docker stack deploy -c docker-compose.yml  app
```
查看 
```bash
[root@swarm-m /]# docker stack ls
NAME                SERVICES            ORCHESTRATOR
app                 10                  Swarm
[root@swarm-m /]# docker service ls
ID                  NAME                           MODE                REPLICAS            IMAGE                                                PORTS
0f4hpqx7u5p3        app_client-feign               replicated          2/2                 172.16.10.192:5000/client-feign:latest               *:8765->8765/tcp
ju5rg4l5x0ir        app_client-gateway-zuul        replicated          2/2                 172.16.10.192:5000/client-gateway-zuul:latest        *:8771->8771/tcp
i1t6wbyd2si6        app_client-order-ribbon        replicated          2/2                 172.16.10.192:5000/client-order-ribbon:latest        *:8766->8766/tcp
rkld2rq7ntw5        app_client-turbine-monitor     replicated          2/2                 172.16.10.192:5000/client-turbine-monitor:latest     *:8767->8767/tcp
j1s5yy1lkw9f        app_eureka-server              replicated          2/2                 172.16.10.192:5000/eureka-server:latest              *:8761->8761/tcp
u38tk0j0ez4l        app_service-order              replicated          2/2                 172.16.10.192:5000/service-order:latest              *:8764->8764/tcp
sv6h294y9r60        app_service-user               replicated          2/2                 172.16.10.192:5000/service-user:latest               *:8763->8763/tcp
ybu18hzo4ra6        app_spring-boot-admin-server   replicated          1/1                 172.16.10.192:5000/spring-boot-admin-server:latest   *:8773->8773/tcp
2m48cqlcip6v        app_visualizer                 replicated          1/1                 dockersamples/visualizer:stable                      *:8080->8080/tcp
692kwr04oa5u        app_zipkin-service             replicated          2/2                 openzipkin/zipkin:latest                             *:9411->9411/tcp
```
![WX20181018-093328@2x.png](https://upload-images.jianshu.io/upload_images/2151905-cca5a48ab83490e7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/760)

![WX20181017-180634@2x.png](https://upload-images.jianshu.io/upload_images/2151905-e12fef072008d042.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/760)

![WX20181017-180615@2x.png](https://upload-images.jianshu.io/upload_images/2151905-73b49f2e9cf271cc.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/760)
