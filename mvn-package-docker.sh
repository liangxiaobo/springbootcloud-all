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
