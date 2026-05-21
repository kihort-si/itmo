FROM postgres:18

RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    default-jre \
    dos2unix \
    && rm -rf /var/lib/apt/lists/*

ARG LIQUIBASE_VERSION=5.0.1
ARG POSTGRES_DRIVER_VERSION=42.6.0

RUN wget -q https://github.com/liquibase/liquibase/releases/download/v${LIQUIBASE_VERSION}/liquibase-${LIQUIBASE_VERSION}.tar.gz \
    && mkdir /opt/liquibase \
    && tar -xzf liquibase-${LIQUIBASE_VERSION}.tar.gz -C /opt/liquibase \
    && rm liquibase-${LIQUIBASE_VERSION}.tar.gz \
    && ln -s /opt/liquibase/liquibase /usr/local/bin/liquibase 

RUN cd /opt/liquibase/lib && wget -q https://jdbc.postgresql.org/download/postgresql-${POSTGRES_DRIVER_VERSION}.jar

RUN mkdir /opt/liquibase-projects

COPY init-databases.sh /docker-entrypoint-initdb.d/
RUN chmod +x /docker-entrypoint-initdb.d/init-databases.sh
RUN dos2unix /docker-entrypoint-initdb.d/init-databases.sh

COPY entry.sh /opt
RUN chmod +x /opt/entry.sh
RUN dos2unix /opt/entry.sh

CMD ["/opt/entry.sh"]