FROM clickhouse:25.8.22.28-jammy

RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    openjdk-17-jdk \
    dos2unix \
    maven \
    && rm -rf /var/lib/apt/lists/*

ARG LIQUIBASE_VERSION=5.0.1
ARG CLICKHOUSE_DRIVER_VERSION=0.9.8
#ARG SLF4J_VERSION=2.0.17

RUN wget -q https://github.com/liquibase/liquibase/releases/download/v${LIQUIBASE_VERSION}/liquibase-${LIQUIBASE_VERSION}.tar.gz \
    && mkdir /opt/liquibase \
    && tar -xzf liquibase-${LIQUIBASE_VERSION}.tar.gz -C /opt/liquibase \
    && rm liquibase-${LIQUIBASE_VERSION}.tar.gz \
    && ln -s /opt/liquibase/liquibase /usr/local/bin/liquibase 

# Я рот наоборот этих разрабов что собирают джарник на целой горе зависимостей

RUN mkdir -p /tmp/clickhouse-driver
COPY pom.xml /tmp/clickhouse-driver/
WORKDIR /tmp/clickhouse-driver

RUN mvn dependency:copy-dependencies -DoutputDirectory=/opt/liquibase/lib

RUN mkdir /opt/liquibase-projects

COPY entry.sh /opt
RUN chmod +x /opt/entry.sh
RUN dos2unix /opt/entry.sh

COPY liquibase-clickhouse-0.9.1.jar /opt/liquibase/lib/


CMD ["/opt/entry.sh"]