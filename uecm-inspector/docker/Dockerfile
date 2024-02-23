# 基础镜像
FROM openjdk:20-jdk-slim-buster

# 修改时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo "Asia/Shanghai" > /etc/timezone

# 切换软件源
COPY docker/etc/apt/source.mirrors.163.com.list /etc/apt/source.mirrors.163.com.list

# 安装命令行工具
RUN apt-get update && apt install -y dos2unix vim dnsutils iputils-ping net-tools

# 复制mib到容器
COPY docker/uecm-inspector/ /uecm-inspector/

# 复制command到容器
COPY docker/command/ /command/

# 转换为unix格式
RUN dos2unix /uecm-inspector/* /command/*

# 复制jar包到容器
COPY target/uecm-inspector-jar-with-dependencies.jar /app.jar

# 启用jvm容器支持
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=80.0 -XX:InitialRAMPercentage=75.0"

# 容器启动命令
ENTRYPOINT java ${JAVA_OPTS} -jar /app.jar
