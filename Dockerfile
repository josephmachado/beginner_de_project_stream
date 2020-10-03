FROM maven:3.6-jdk-8-slim AS builder

COPY ./ /opt/frauddetection
WORKDIR /opt/frauddetection
RUN mvn clean install

FROM flink:1.11.0-scala_2.11

WORKDIR /opt/flink/bin

COPY --from=builder /opt/frauddetection/target/frauddetection-*.jar /opt/frauddetection.jar