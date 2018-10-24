本项目有三个主要的分支: master-docker、master-zipkin、 master-swarm
在简书上都有详细说明 https://www.jianshu.com/u/7ef99661ebb0
简书上对本项目的三个文章: 
* [spring cloud 快速学习教程](https://www.jianshu.com/p/2e3953fce6c0)
* [Docker Swarm集成搭建](https://www.jianshu.com/p/cd01033a21e6)
* [Docker Swarm 搭建SpringCloud集群](https://www.jianshu.com/p/9426ad159b18)

想对看过的spring cloud 各个项目进行一个简单的搭建，并使用docker部署，目前包含的项目有Eureka、Ribbon、Feign、Hystrix、Hystrix Dashboard、Turbine聚合监控、Zuul、SpringBootAdmin等Spring Config 、OAuth2未集成进来，但是在我的主页里有单独的实例，后续会慢慢都集成进来。

github地址：https://github.com/liangxiaobo/springbootcloud-all 分支 master-docker


项目模块：
```bash
├── client-common-dependencys
├── client-feign
├── client-gateway-zuul
├── client-order-ribbon
├── client-turbine-monitor
├── docker-compose-base.yml
├── docker-compose.yml
├── docker-start.sh
├── docker-zipkin-shell.sh
├── eureka-server
├── mvn-package-docker.sh
├── pom.xml
├── service-common-dependencys
├── service-order
├── service-user
├── spring-boot-admin-server
├── springbootdemoall.iml
├── src
├── swagger-doc
└── var
```
| 项目 | 端口 |描述 |
| ------ | ------ | ------ |
| eureka-server | 8761 | 服务的注册与发现 |
| service-user  | 8763 | 服务提供者  |
| service-order  | 8764 | 服务提供者 |
| client-feign | 8765 | 负载均衡 Feign|
| client-order-ribbon | 8766 | 负载均衡 Ribbon|
| client-gateway-zuul | 8771 | 网关 Zuul|
| client-turbine-monitor | 8767 | Turbine聚合监控 |
| swagger-doc | 8772 | 生成在线Api |
| spring-boot-admin-server | 8773 | spring-boot-admin-server |

项目中看到两个项目 service-common-dependencys和client-common-dependencys是pom项目，用来测试统一管理引用依赖的项目（分别是service项目和client项目的父类）；
> 当父类pom用dependencies来管理依赖的话，那么子项目必须继承所有依赖
   当父类pom用dependencyManagement来管理依赖的话，那么子项目不是必须继承，而是有选择的引用依赖；我这里使用的是前者
 
eureka-server 项目 参照 [spring cloud 搭建集群Eureka Server](https://www.jianshu.com/p/3a8d637f3e07)
spring-boot-admin-server 参照 [SpringBoot Admin 2.0](https://www.jianshu.com/p/81ad086379b0)
client-roder-ribbon 参照 [使用RestTemplate和Ribbon来消费服务和Hystrix熔断功能](https://www.jianshu.com/p/0437b4160149)
service-user、service-order 就是简单的web项目

### Feign项目 client-feign
Feign是一个声明式的Web Service客户端，它使得编写Web Serivce客户端变得更加简单。我们只需要使用Feign来创建一个接口并用注解来配置它既可完成。它具备可插拔的注解支持，包括Feign注解和JAX-RS注解。Feign也支持可插拔的编码器和解码器。Spring Cloud为Feign增加了对Spring MVC注解的支持，还整合了Ribbon和Eureka来提供均衡负载的HTTP客户端实现。
依赖pom.xml
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```
pom中引用了父类 ,继承父类的所有依赖，其中有Hystrix、admin-client、eureka等
```xml
<parent>
        <groupId>com.spring.nahong.client.common.dependency</groupId>
        <artifactId>client-common-dependencys</artifactId>
        <version>${all.version}</version>
        <relativePath>../client-common-dependencys</relativePath>
    </parent>
```
看一下启动类
```java
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@EnableHystrixDashboard
public class ClientFeignApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientFeignApplication.class, args);
    }

    /**
     * springboot 版本如果是2.0则需要添加 ServletRegistrationBean
     * 因为springboot的默认路径不是 "/hystrix.stream"，
     * 只要在自己的项目里配置上下面的servlet就可以了
     * 第一次访问hystrix.stream 会出现 Unable to connect to Command Metric Stream
     */
    @Bean
    public ServletRegistrationBean getServlet() {
        HystrixMetricsStreamServlet streamServlet = new HystrixMetricsStreamServlet();
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(streamServlet);
        registrationBean.setLoadOnStartup(1);
        registrationBean.addUrlMappings("/hystrix.stream");
        registrationBean.setName("HystrixMetricsStreamServlet");
        return registrationBean;
    }
}
```
@EnableFeignClients 开启了feign @EnableHystrixDashboard开启了熔断hystrix的界面

定义Feign接口UserClientFeign
```java
@FeignClient(value = "service-user", configuration = FeignConfig.class, fallback = UserHystrix.class)
public interface UserClientFeign {
    @GetMapping("/user/say")
    String sayFromClient(@RequestParam("name") String name);
}
```
上面声明了指向service-user服务 UserHystrix是对应的熔断类
``` java
@Component
public class UserHystrix implements UserClientFeign {

    @Override
    public String sayFromClient(String name) {
        return "hi , sorry error!";
    }
}
```
定义一个服务UserService
```java
@Service
public class UserService {
    @Autowired
    UserClientFeign userClientFeign;

    public String say(String name) {
        return userClientFeign.sayFromClient(name);
    }
}
```
声明一个Feign的配置类FeignConfig
```java
@Configuration
public class FeignConfig {

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, SECONDS.toMillis(1), 5);
    }
}
```
创建一个UserController
``` java
@RestController
public class UserController {
    @Autowired
    UserService userService;

    @RequestMapping("/user/hi")
    public String say(@RequestParam("name") String name) {
        return userService.say(name);
    }
}
```
启用Feign的hystrix最重要的是在配置文件中添加
  ```xml
  feign:
    hystrix:
      enabled: true
  ```

可以启动 eureka-server service-user client-feign 访问http://localhost:8765/user/hi?name=liajngbo
如果测试负载均衡功能可以多运行一个service-user用不同的端口8863，然后在浏览器刷新 http://localhost:8765/user/hi?name=liajngbo 会发现有不同的结果
```
Hi, my name is liajngbo, port: 8763
Hi, my name is liajngbo, port: 8863
```

client-feign项目中已经配置了hystrix，现在访问http://localhost:8765/hystrix
![r1.png](https://upload-images.jianshu.io/upload_images/2151905-95b09f00da2c8c63.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/760)

在输入框中输入http://localhost:8765/hystrix.stream点击按扭
![r2.png](https://upload-images.jianshu.io/upload_images/2151905-3ecee3dc9e23ee77.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/760)

## Turbine项目 client-turbine-monitor
Turbine能够汇集监控信息，并将聚合后的信息提供给Hystrix Dashboard来集中展示和监控。
项目配置起来相当简单，只是2.0版本会有点小坑
pom.xml全部代码请参阅源码(源码包含了admin-client的依赖)这只是turbine的依赖：
```xml
 <dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-turbine</artifactId>
</dependency>

<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
</dependency>		
```
启动类：
```java
@SpringBootApplication
@EnableTurbine
@EnableHystrixDashboard
public class ClientTurbineMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientTurbineMonitorApplication.class, args);
	}
}
```
> @EnableTurbine 开启了turbine功能，@EnableHystrixDashboard开启了hystrix监控UI

一般的配置文件：
```xml
turbine:
  app-config: client-feign,client-order-ribbon,client-gateway-zuul # 指定了要监控的应用名字
  cluster-name-expression: new String("default") # 表示集群的名字为default
  combine-host-port: true # 表示同一主机上的服务通过host和port的组合来进行区分，默认情况下是使用host来区分，这样会使本地调试有问题
```
> 这里会有一个问题，turbine会去访问其它项目的/hystrix.stream路径，默认情况下turbine访问的是/actuator/hystrix.stream，这个路径访问不通，因为其它项目注册的路由地址是/hystrix.stream，所以要纠正turbine项目访问的默认地址，在配置文件中添加turbine.instanceUrlSuffix=hystrix.stream，management.context-path=/
配置文件：
```xml
management:
  context-path: /
turbine:
  app-config: client-feign,client-order-ribbon,client-gateway-zuul # 指定了要监控的应用名字
  cluster-name-expression: new String("default") # 表示集群的名字为default
  combine-host-port: true # 表示同一主机上的服务通过host和port的组合来进行区分，默认情况下是使用host来区分，这样会使本地调试有问题
  instanceUrlSuffix: hystrix.stream
```
测试一下turbine,必须启动多个项目 eureka-server、service-user、service-order、client-feign、client-order-ribbon、client-gateway-zuul、client-turbine-monitor
访问turbine之前要先访问一下其它项目，不然不会有数据显示turbine会处于loading中
访问 http://localhost:8767/turbine.stream  会有json格式的数据流
```javascript
{"rollingCountFallbackSuccess":0,"rollingCountFallbackFailure":0,"propertyValue_circuitBreakerRequestVolumeThreshold":20,"propertyValue_circuitBreakerForceOpen":false,"propertyValue_metricsRollingStatisticalWindowInMilliseconds":10000,"latencyTotal_mean":10,"rollingMaxConcurrentExecutionCount":0,"type":"HystrixCommand","rollingCountResponsesFromCache":0,"rollingCountBadRequests":0,"rollingCountTimeout":0,"propertyValue_executionIsolationStrategy":"THREAD","rollingCountFailure":0,"rollingCountExceptionsThrown":0,"rollingCountFallbackMissing":0,"threadPool":"service-user","latencyExecute_mean":10,"isCircuitBreakerOpen":false,"errorCount":0,"rollingCountSemaphoreRejected":0,"group":"service-user","latencyTotal":{"0":10,"99":10,"100":10,"25":10,"90":10,"50":10,"95":10,"99.5":10,"75":10},"requestCount":0,"rollingCountCollapsedRequests":0,"rollingCountShortCircuited":0,"propertyValue_circuitBreakerSleepWindowInMilliseconds":5000,"latencyExecute":{"0":10,"99":10,"100":10,"25":10,"90":10,"50":10,"95":10,"99.5":10,"75":10},"rollingCountEmit":0,"currentConcurrentExecutionCount":0,"propertyValue_executionIsolationSemaphoreMaxConcurrentRequests":10,"errorPercentage":0,"rollingCountThreadPoolRejected":0,"propertyValue_circuitBreakerEnabled":true,"propertyValue_executionIsolationThreadInterruptOnTimeout":true,"propertyValue_requestCacheEnabled":true,"rollingCountFallbackRejection":0,"propertyValue_requestLogEnabled":true,"rollingCountFallbackEmit":0,"rollingCountSuccess":0,"propertyValue_fallbackIsolationSemaphoreMaxConcurrentRequests":10,"propertyValue_circuitBreakerErrorThresholdPercentage":50,"propertyValue_circuitBreakerForceClosed":false,"name":"UserClientFeign#sayFromClient(String)","reportingHosts":1,"propertyValue_executionIsolationThreadPoolKeyOverride":"null","propertyValue_executionIsolationThreadTimeoutInMilliseconds":1000,"propertyValue_executionTimeoutInMilliseconds":1000}

data: {"currentCorePoolSize":10,"currentLargestPoolSize":2,"propertyValue_metricsRollingStatisticalWindowInMilliseconds":10000,"currentActiveCount":0,"currentMaximumPoolSize":10,"currentQueueSize":0,"type":"HystrixThreadPool","currentTaskCount":2,"currentCompletedTaskCount":2,"rollingMaxActiveThreads":0,"rollingCountCommandRejections":0,"name":"service-user","reportingHosts":1,"currentPoolSize":2,"propertyValue_queueSizeRejectionThreshold":5,"rollingCountThreadsExecuted":0}
```
访问hystrix UI http://localhost:8767/hystrix
![e1.png](https://upload-images.jianshu.io/upload_images/2151905-51a89633050f523e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/760)
![e2.png](https://upload-images.jianshu.io/upload_images/2151905-19de0bd813d1c9b4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/760)

## 网关Zuul client-gateway-zuul
项目依赖，完整的pom.xml中包含了其它的依赖
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-zuul</artifactId>
</dependency>
```
启动类：
```java
@SpringBootApplication
@EnableEurekaClient
@EnableZuulProxy
@EnableHystrixDashboard
public class ClientGatewayZuulApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientGatewayZuulApplication.class, args);
    }

    /**
     * springboot 版本如果是2.0则需要添加 ServletRegistrationBean
     * 因为springboot的默认路径不是 "/hystrix.stream"，
     * 只要在自己的项目里配置上下面的servlet就可以了
     * 第一次访问hystrix.stream 会出现 Unable to connect to Command Metric Stream
     *
     */
    @Bean
    public ServletRegistrationBean getServlet() {
        HystrixMetricsStreamServlet streamServlet = new HystrixMetricsStreamServlet();
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(streamServlet);
        registrationBean.setLoadOnStartup(1);
        registrationBean.addUrlMappings("/hystrix.stream");
        registrationBean.setName("HystrixMetricsStreamServlet");
        return registrationBean;
    }
}
```
> 开启网关zuul @EnableZuulProxy 
@EnableEurekaClient 开启eureka客户端
@EnableHystrixDashboard Hystrix Dashboard

主要配置:
```xml
zuul:
  routes:
    hiapi:
      path: /hiapi/**
      serviceId: service-user
    ribbonapi:
      path: /ribbonapi/**
      serviceId: client-order-ribbon
    feignapi:
      path: /feignapi/**
      serviceId: client-feign
  prefix: /v1
```
routes下面是路由，hiapi、ribbonapi、feignapi是自定义路由名称，下面path是路由规则，serviceId指定服务名称，prefix给路由加上前缀

> 注意：因为项目是我写好的demo，其中包含了hystrix功能，在/hystrix中Thread Pools会一直处于loading状态，这是由于Zuul默认会使用信号量来实现隔离，只有通过Hystrix配置把隔离机制改成为线程池的方式才能够得以展示，
SEMAPHORE - 它在调用线程上执行，并发请求受信号量计数的限制(Zuul默认此策略)
 THREAD - 它在一个单独的线程上执行，并发请求受到线程池中线程数的限制
配置文件中添加
```xml
thread-pool:
    use-separate-thread-pools: true
  ribbon-isolation-strategy: thread # 每个路由使用独立的线程池
```
Zuul中提供Filter的作用有哪些，我觉得分为如下几点：

* 网关是暴露在外面的，必须要进行权限控制
* 可以针对服务做控制，在路由的时候处理，比如服务降级
* 防止爬虫，利用Filter对请求进行过滤
* 流量控制，只允许最高的并发量，保护后端的服务
* 灰度发布，可以针对不用的用户进行路由来实现灰度

Filter种类
* pre：可以在请求被路由之前调用
* route：在路由请求时候被调用
* post：在route和error过滤器之后被调用
* error：处理请求时发生错误时被调用

zuul的Filter的生命周期，见下图：
![2685774-66bb8fa036d4256a.png](https://upload-images.jianshu.io/upload_images/2151905-2a82a21b56db7ff0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

创建一个Filter类MyFilter，filterType类型为"pre"：
``` java
@Component
public class MyFilter extends ZuulFilter {

    private static Logger logger = LoggerFactory.getLogger(MyFilter.class);

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE; // pre
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        Object accessToken = request.getParameter("token");

        if (accessToken == null) {
            logger.warn("token is empty");
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);

            try {
                ctx.getResponse().getWriter().write("token is empty");
            }catch (Exception e) {
                return null;
            }
        }

        logger.info("ok");
        return null;
    }
}

```
> shouldFilter是决定这个过滤器需不需要执行，返回false则不执行，这个也可以利用配置中心来做，达到动态的开启关闭效果，
filterOrder是表示过滤器执行的顺序，数字越小，优先级越高
run里面就是我们自己要执行的业务逻辑，这里我们验证了一下接口参数中有没有token

zuul自身是集成了hystrix的，所以它带有熔断功能
创建一个ServiceUserFallbackProvider类实现接口FallbackProvider，实现service-user的熔断功能：
```java
@Component
public class ServiceUserFallbackProvider implements FallbackProvider {

    static final Logger logger = LoggerFactory.getLogger(ServiceUserFallbackProvider.class);

    @Override
    public String getRoute() {
        // 表明是为哪个微服务提供回退，*表示为所有微服务提供回退，当前只为service-user提供
        return "service-user";
    }

    @Override
    public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
        logger.info("route: " + route);

        return new ClientHttpResponse() {
            @Override
            public HttpStatus getStatusCode() throws IOException {
                return HttpStatus.OK;
            }

            @Override
            public int getRawStatusCode() throws IOException {
                return 200;
            }

            @Override
            public String getStatusText() throws IOException {
                return "OK";
            }

            @Override
            public void close() {

            }

            @Override
            public InputStream getBody() throws IOException {
                return new ByteArrayInputStream("oooops! error,i'm the fallback.".getBytes());
            }

            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                return headers;
            }
        };
    }
}
```
启动eureka-server、service-user、service-order、client-feign、client-order-ribbon、client-gateway-zuul，访问http://localhost:8771/v1/hiapi/user/say?name=liang&token=123
正常结果显示：
```
Hi, my name is liang, port: 8763
```
去掉token访问：http://localhost:8771/v1/hiapi/user/say?name=liang
```
token is empty
```
测试熔断效果，关停service-user项目 访问: http://localhost:8771/v1/hiapi/user/say?name=liang&token=123
```
oooops! error,i'm the fallback.
```
## 使用docker部署项目
Docker是一个用于开发、交付和运行应用的开放平台，Docker被设计用于更快地交付应用。Docker可以将应用程序和基础设施层隔离，并且可以将基础设施当作程序一样进行管理。使用Docker，可以更快地打包代码、测试以及部署，并且可以减少从编写到部署运行代码的周期。
Docker将内核容器特性（LXC）、工作流和工具集成，以帮助管理和部署应用。

什么是Docker
核心是，Docker是一种在安全隔离的容器中运行近乎所有应用的方式，这种隔离性和安全性允许你在同一个主机上同时运行多个容器，而容器的这种轻量级特性，无需消耗运行hpervisor所需的额外负载，意味着你可以节省更多的硬件资源。

docker如何安装这里不讲，自行解决

这里演示在单台宿主机上的测试结果

因为项目是我提前写好的项目，可以看到项目中的pom文件中有：
```xml
<plugin>
    <groupId>com.spotify</groupId>
    <artifactId>docker-maven-plugin</artifactId>
    <version>${docker.plugin.version}</version>

    <configuration>
        <imageName>${docker.image.prefix}/${project.artifactId}</imageName>
        <dockerDirectory>src/main/docker</dockerDirectory>
        <resources>
            <resource>
                <targetPath>/</targetPath>
                <directory>${project.build.directory}</directory>
                <include>${project.build.finalName}.jar</include>
            </resource>
        </resources>
        <imageTags>
            <!--<imageTag>${project.version}</imageTag>-->
            <imageTag>latest</imageTag>
        </imageTags>
    </configuration>
</plugin>
```
> ${docker.image.prefix}是我在父类中定义的liangwang
imageName是镜像名称
dockerDirectory是Dockerfile路径
resources是资源的配置，jar所在的目录
imageTags是镜像的tag版本的意思，image:[tag]

Dockerfile
```bash
FROM java
VOLUME /tmp
ADD eureka-server-0.0.1-SNAPSHOT.jar app.jar
#RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}","/app.jar"]
EXPOSE 8761 # 开放的端口
```
> EXPOSE 是开放的端口 多个端口可以以空格隔开 "8761 8762 8763"
ENTRYPOINT 是容器启动后执行的命令
springboot打包的项目jar的运行命令 "java -jar app.jar" 如果启用不同的配置文件的话要 “java -jar app.jar --spring.profiles.active=test”,所以上面定义了一个变量来接收容器启动设置的参数**SPRING_PROFILES_ACTIVE** 容器启动时加 -e "SPRING_PROFILES_ACTIVE=test"

maven 打包 docker 命令
```bash
mvn clean package docker:build -Dmaven.test.skip=true
# -Dmaven.test.skip 跳过单元测试
```
容器启动：
```bash
docker run -d -e "SPRING_PROFILES_ACTIVE=test-peer1" --name eureka-server -p 8761:8761 -it liangwang/eureka-server
```
> 同一台宿主机可以使用容器名称来通信 比如 eureka-server容器使用link参数将其它链接到容器2
```bash
docker run -d -e "SPRING_PROFILES_ACTIVE=test" --name service-user-01 --link=eureka-server -p 8763:8763 -it liangwang/service-user
```
对于注册与发现服务，eureka-server和其它eureka-client中的 serviceUri.defaultZone=http://eureka-server:8761/eureka/
例举各别：
eureka-server的配置文件
```xml
spring:
  profiles: test-peer1
  application:
    name: eureka-server
server:
  port: 8761
eureka:
  instance:
    prefer-ip-address: true
    hostname: eureka-server
    instance-id: ${spring.cloud.client.ipAddress}:${server.port}
  client:
    service-url:
       defaultZone: http://eureka-server:8761/eureka/ # 注意客户端中也使用eureka-server
    register-with-eureka: false
    fetch-registry: false
```
下面可以按顺序打包项目：
项目根目录中有一个mvn-package-docker.sh
```bash
#!/bin/bash

echo "开始执行打包....[start]"

mvn clean
cd eureka-server
mvn package docker:build -Dmaven.test.skip=true

cd ../service-user
mvn package docker:build -Dmaven.test.skip=true

cd ../service-order
mvn package docker:build -Dmaven.test.skip=true

cd ../spring-boot-admin-server
mvn package docker:build -Dmaven.test.skip=true

cd ../client-feign
mvn package docker:build -Dmaven.test.skip=true

cd ../client-order-ribbon
mvn package docker:build -Dmaven.test.skip=true

cd ../client-gateway-zuul
mvn package docker:build -Dmaven.test.skip=true

cd ../client-turbine-monitor
mvn package docker:build -Dmaven.test.skip=true

cd ../swagger-doc
mvn package docker:build -Dmaven.test.skip=true

echo "执行打包结束.....[end]"
```
也可以手动打包各别你想打包的项目
打包完之后，执行命令 docker images
```bash
[root@swarm03 ~]# docker images
REPOSITORY                           TAG                 IMAGE ID            CREATED             SIZE
liangwang/swagger-doc                latest              9d477baa8695        2 days ago          689MB
liangwang/client-turbine-monitor     latest              bb26593b2683        2 days ago          692MB
liangwang/client-gateway-zuul        latest              a2148967a806        2 days ago          693MB
liangwang/client-order-ribbon        latest              71a24105d743        2 days ago          693MB
liangwang/client-feign               latest              6ae21d5dab6d        2 days ago          693MB
liangwang/spring-boot-admin-server   latest              6d9314544f13        2 days ago          697MB
liangwang/service-order              latest              48b8fd2e0de4        2 days ago          691MB
liangwang/service-user               latest              f33c40c5b79a        2 days ago          691MB
liangwang/eureka-server              latest              f5e714d47a6f        2 days ago          691MB
java                                 latest              d23bdf5b1b1b        20 months ago       643MB
```
除java之外都是打包生成的镜像
下面要运行容器来跑这些镜像，项目根目录有一个docker-start.sh脚本，里面是跑各个容器的命令，最好不要执行这个脚本，因为没有时间停顿，所以容器全部运行没有前后顺序，如果eureka-server没有跑起来，可能其它项目会报错，最好先手动一条一条执行，这里是演示测试所以没有用容器编排

```bash
# eureka-server docker 启动命令：
docker run -d -e "SPRING_PROFILES_ACTIVE=test-peer1" --name eureka-server -p 8761:8761 -it liangwang/eureka-server

# service-user-01 docker 启动命令：
docker run -d -e "SPRING_PROFILES_ACTIVE=test" --name service-user-01 --link=eureka-server -p 8763:8763 -it liangwang/service-user

# service-user-02 docker 启动命令：
docker run -d -e "SPRING_PROFILES_ACTIVE=test" --name service-user-02 --link=eureka-server -p 8863:8863 -it liangwang/service-user --server.port=8863


# service-order-01 docker 启动命令：
docker run  -d -e "SPRING_PROFILES_ACTIVE=test" --name service-order-01  --link=eureka-server -p 8764:8764 -it liangwang/service-order

# service-order-02 docker 启动命令：
docker run  -d -e "SPRING_PROFILES_ACTIVE=test" --name service-order-02  --link=eureka-server -p 8864:8864 -it liangwang/service-order --server.port=8864

# spring-boot-admin-server docker 启动命令：
docker run -d -e "SPRING_PROFILES_ACTIVE=test" --name spring-boot-admin-server  --link=eureka-server -p 8773:8773 -it liangwang/spring-boot-admin-server

# client-feign docker 启动命令：
docker run  -d -e "SPRING_PROFILES_ACTIVE=test" --name client-feign  --link=eureka-server -p 8765:8765 -it liangwang/client-feign

# client-order-ribbon docker 启动命令：
docker run -d -e "SPRING_PROFILES_ACTIVE=test" --name client-order-ribbon  --link=eureka-server -p 8766:8766 -it liangwang/client-order-ribbon

# client-gateway-zuul docker 启动命令：
docker run -d -e "SPRING_PROFILES_ACTIVE=test" --name client-gateway-zuul  --link=eureka-server -p 8771:8771 -it liangwang/client-gateway-zuul

# client-turbine-monitor docker 启动命令：
docker run -d -e "SPRING_PROFILES_ACTIVE=test" --name client-turbine-monitor  --link=eureka-server -p 8767:8767 -it liangwang/client-turbine-monitor

# swagger-doc docker 启动命令：
docker run  -d -e "SPRING_PROFILES_ACTIVE=test" --name swagger-doc  --link=eureka-server -p 8772:8772 -it liangwang/swagger-doc

```
没有问题的情况下，执行docker ps能看到正在运行的容器进程
```bash
[root@swarm03 ~]# docker ps
CONTAINER ID        IMAGE                                COMMAND                  CREATED             STATUS              PORTS                              NAMES
0c70c83fd5f2        liangwang/client-turbine-monitor     "java -Djava.secur..."   2 days ago          Up 2 days           0.0.0.0:8767->8767/tcp             client-turbine-monitor
1b78c139bded        liangwang/client-gateway-zuul        "java -Djava.secur..."   2 days ago          Up 2 days           0.0.0.0:8771->8771/tcp             client-gateway-zuul
1582be4ac882        liangwang/client-order-ribbon        "java -Djava.secur..."   2 days ago          Up 2 days           0.0.0.0:8766->8766/tcp             client-order-ribbon
d13d2fdf24ae        liangwang/client-feign               "java -Djava.secur..."   2 days ago          Up 2 days           0.0.0.0:8765->8765/tcp             client-feign
3f149a4873cf        liangwang/spring-boot-admin-server   "java -Djava.secur..."   2 days ago          Up 2 days           0.0.0.0:8773->8773/tcp             spring-boot-admin-server
399150b6fa6e        liangwang/service-order              "java -Djava.secur..."   2 days ago          Up 2 days           8764/tcp, 0.0.0.0:8864->8864/tcp   service-order-02
e7ffefe94d3e        liangwang/service-order              "java -Djava.secur..."   2 days ago          Up 2 days           0.0.0.0:8764->8764/tcp             service-order-01
9be1cabe9b5a        liangwang/service-user               "java -Djava.secur..."   2 days ago          Up 2 days           8763/tcp, 0.0.0.0:8863->8863/tcp   service-user-02
7b82a04ddbeb        liangwang/service-user               "java -Djava.secur..."   2 days ago          Up 2 days           0.0.0.0:8763->8763/tcp             service-user-01
ee0fdbee9f33        liangwang/eureka-server              "java -Djava.secur..."   2 days ago          Up 2 days           0.0.0.0:8761->8761/tcp             eureka-server
```
这时候访问 http://localhost:8761，我项目部署在测试服务器所以我的访问地址为 http://172.16.10.177:8761/
![e3.png](https://upload-images.jianshu.io/upload_images/2151905-e037a6bf57b52740.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/760)
能看到项目都已经注册进来了

看admin-server 访问http://localhost:8773，我的访问地址为 http://172.16.10.177:8773
![e5.png](https://upload-images.jianshu.io/upload_images/2151905-9e8f4c4b192582e5.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/760)

![e4.png](https://upload-images.jianshu.io/upload_images/2151905-82f84c824bd61d6c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/760)

## 改造项目增加zipkin链路追踪
使用镜像运行zipkin项目

请先拉取镜像
```bash
docker pull openzipkin/zipkin
```

```bash
# 运行容器
# docker run -d -p 9994:9411 -e MYSQL_USER=root -e MYSQL_PASS=password -e MYSQL_HOST=192.168.0.8 -e STORAGE_TYPE=mysql openzipkin/zipkin
docker run -d -p 9994:9411 --name zipkin openzipkin/zipkin
```

在需要追踪的项目中添加依赖
```xml
    <!-- 链路追踪 zipkin -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-zipkin</artifactId>
    </dependency>
```
在application.yml中添加
```xml
spring:
  zipkin:
    base-url: http://localhost:9994 # 如果zipkin服务在其它服务器，localhost应为对应的IP
  sleuth:
    sampler:
      probability: 1.0

```
sleuth.sampler.probability 是监控的百分比，默认的是0.1表示10%,这里给1.0表示全部监控
spring.zipkin.base-url：是zipkin-server的服务路径

访问 http://localhost:9994

![e7.png](https://upload-images.jianshu.io/upload_images/2151905-b44a5dbb51fa797a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/760)

![e6.png](https://upload-images.jianshu.io/upload_images/2151905-6ab6ef8452b23ea7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/760)

![e8.png](https://upload-images.jianshu.io/upload_images/2151905-cd7ba2c004812605.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/760)

后续我会集成OAuth2.0 JWT和Config配置中心
