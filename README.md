# amdb-receiver-service

[![许可证](https://img.shields.io/github/license/pingcap/tidb.svg)](https://github.com/pingcap/tidb/blob/master/LICENSE)
[![语言](https://img.shields.io/badge/Language-Java-blue.svg)](https://www.java.com/)
amdb-receiver-serivce是Takin的数据服务。 amdb 对外提供统一的数据写入和数据查询服务，其中包括应用节点信息收集和查询、链路梳理结果查询、入口服务查询、trace日志查询等。

[Takin详细介绍](https://docs.shulie.io/docs/opensource/opensource-1d2ckv049184j)

[Takin产品架构](https://docs.shulie.io/docs/opensource/opensource-1d4d0l6o0b6u9)

# 模块介绍

- amdb-common 工具包，定义了传输对象模型、枚举类、请求对象。
- amdb-app 此工程为springboot项目，提供业务能力支持。

# 快速开始

## 运行环境

JAVA JDK 1.8+

## 依赖中间件

- mysql 必须
- clickhouse 非必须
- zk 必须

## docker启动

参考takin部署 [地址](https://docs.shulie.io/docs/opensource/opensource-1d40ib39m90bu)

## 本地启动

### 获取项目源代码

```
git clone https://github.com/shulieTech/Takin-amdb.git
cd Takin-amdb
mvn clean install -DskipTests
```

### 修改配置

```
修改application.yml配置 

spring.datasource 相关ip、host、user、password
influx 相关ip、host、user、password
zookeeper.server 相关配置

datasource.traceAll=mysql 配置数据源，使项目不依赖clickhouse
```

### 打包运行

```
方式一
直接在编译器中运行AMDBAPIBootstrap

方式二
进入amdb工程根目录
mvn clean package -DskipTests
java -jar amdb-app/target/amdb-app-2.5.jar
```

# QA

* 1.Cannot resolve io.shulie.takin:simulator-internal-bootstrap-api:1.0.0

```
访问[link-agent](https://github.com/shulieTech/LinkAgent)仓库,对instrument-modules/bootstrap-inject/simulator-internal-bootstrap-api模块进行本地install
```

# 许可证

Takin amdb-receiver-service遵循 the Apache 2.0 许可证. 详见
the [LICENSE](https://github.com/shulieTech/Takin/blob/main/LICENSE) file for details.