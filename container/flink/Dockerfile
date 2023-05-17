FROM flink:1.17.0


# install python3 and pip3
RUN apt-get update -y && \
    apt-get install -y python3 python3-pip python3-dev && rm -rf /var/lib/apt/lists/*
RUN ln -s /usr/bin/python3 /usr/bin/python

RUN wget https://repo.maven.apache.org/maven2/org/apache/flink/flink-sql-connector-kafka/1.17.0/flink-sql-connector-kafka-1.17.0.jar && wget https://repo.maven.apache.org/maven2/org/apache/flink/flink-connector-jdbc/3.0.0-1.16/flink-connector-jdbc-3.0.0-1.16.jar && wget https://jdbc.postgresql.org/download/postgresql-42.6.0.jar

RUN echo "metrics.reporters: prom" >> "$FLINK_HOME/conf/flink-conf.yaml"; \
    echo "metrics.reporter.prom.factory.class: org.apache.flink.metrics.prometheus.PrometheusReporterFactory" >> "$FLINK_HOME/conf/flink-conf.yaml"

COPY requirements.txt /opt/flink/

# Install py dependencies
RUN pip install --no-cache-dir -r /opt/flink/requirements.txt