package com.startdataengineering

import java.time.Instant
import java.util.Properties
import java.util.UUID.randomUUID
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import scala.util.Random

import com.startdataengineering.model.ServerLog

object ServerLogGenerator {

  private val random = new Random

  private val locationCountry: Array[String] = Array(
    "USA", "IN", "UK", "CA", "AU", "DE", "ES", "FR", "NL", "SG", "RU", "JP", "BR", "CN", "O")

  private val eventType: Array[String] = Array(
    "click", "purchase", "login", "log-out", "delete-account", "create-account", "update-settings", "other")

  def getServerLog(): ServerLog = {
    val eventId = randomUUID().toString
    val timestamp = Instant.now.getEpochSecond
    val currentCountry: String = locationCountry(random.nextInt(locationCountry.length))
    val currentEventType: String = eventType(random.nextInt(eventType.length))
    val accountId = random.nextInt(10000)

    ServerLog(eventId, accountId, currentEventType, currentCountry, timestamp)
  }

  def getProperties(): Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", "kafka:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props
  }

  def main(args: Array[String]): Unit = {
    val props = getProperties()
    val topic: String = "server-logs"

    val producer = new KafkaProducer[String, String](props)
    var i = 0

    while(i<100000) {
      val log: ServerLog = getServerLog()
      val record = new ProducerRecord[String, String](topic, log.eventId, log.toString)
      producer.send(record)
      i = i + 1
    }

    producer.close()
  }

}
