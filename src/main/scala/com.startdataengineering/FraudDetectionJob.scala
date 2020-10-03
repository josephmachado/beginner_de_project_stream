package com.startdataengineering

import java.util.Properties
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}

object FraudDetectionJob {

  @throws[Exception]
  def main(args: Array[String]): Unit = {

    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "kafka:9092")

    val myConsumer = new FlinkKafkaConsumer[String](
      "server-logs", new SimpleStringSchema(), properties)
    myConsumer.setStartFromEarliest()

    val events = env
      .addSource(myConsumer)
      .name("incoming-events")

    val alerts: DataStream[String] = events
      .keyBy(event => event.split(",")(1))
      .process(new FraudDetection)
      .name("fraud-detector")

    val myProducer = new FlinkKafkaProducer[String](
      "alerts", new SimpleStringSchema(), properties)

    alerts
      .addSink(myProducer)
      .name("send-alerts")

    events
      .addSink(new ServerLogSink)
      .name("event-log")

    env.execute("Fraud Detection")

  }

}
