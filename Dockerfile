FROM swr.cn-east-3.myhuaweicloud.com/shulie-hangzhou/openjdk:8-jdk-alpine
WORKDIR /data/amdb
RUN cd / && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
COPY  amdb-app/target/amdb-app-*.jar  /data/amdb/amdb-app.jar
ENTRYPOINT ["java","-jar","amdb-app.jar"]