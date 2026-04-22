# ============================================================
# Dockerfile — WildFly + blps-lab WAR
# Контекст сборки: корень проекта (docker-compose build context)
# ============================================================
FROM quay.io/wildfly/wildfly:latest

# --- Директория для XML-хранилища пользователей -------------
USER root
RUN mkdir -p /opt/blps/security && \
    chown -R jboss:jboss /opt/blps
USER jboss

# --- Конфигурация WildFly через CLI -------------------------
COPY --chown=jboss:jboss docker/wildfly/configure.cli /tmp/configure.cli
RUN $JBOSS_HOME/bin/jboss-cli.sh --file=/tmp/configure.cli && \
    rm /tmp/configure.cli

# --- Деплой WAR ---------------------------------------------
# WAR должен быть собран до сборки образа: ./gradlew bootWar
COPY --chown=jboss:jboss build/libs/app-0.0.1-SNAPSHOT.war \
    $JBOSS_HOME/standalone/deployments/blps-lab.war

EXPOSE 8080 9990

# Запуск с standalone-full.xml (включает JMS/Artemis)
CMD ["/opt/jboss/wildfly/bin/standalone.sh", \
     "-b",           "0.0.0.0", \
     "-bmanagement", "0.0.0.0", \
     "--server-config=standalone-full.xml"]
