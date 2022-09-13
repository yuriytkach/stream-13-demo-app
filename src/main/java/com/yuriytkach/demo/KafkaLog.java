package com.yuriytkach.demo;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.yuriytkach.demo.model.RoomAverageTemperature;
import com.yuriytkach.demo.model.RoomData;
import com.yuriytkach.demo.model.SensorData;

import io.smallrye.reactive.messaging.kafka.Record;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class KafkaLog {

  @Incoming("temp-in")
  public void logSensor(final Record<String, SensorData> record) {
    log.info("Received: {}={}", record.key(), record.value());
  }

  @Incoming("room-in")
  public void logRoom(final Record<String, RoomData> record) {
    log.info("Received: {}={}", record.key(), record.value());
  }

  @Incoming("tmp-in")
  public void logTmp(final Record<String, RoomAverageTemperature> record) {
    log.info("Received: {}={}", record.key(), record.value());
  }

}
