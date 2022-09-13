package com.yuriytkach.demo;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import com.yuriytkach.demo.model.RoomData;
import com.yuriytkach.demo.model.SensorData;

import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/")
public class RestResource {

  @Inject
  @Channel("temp-out")
  Emitter<SensorData> sensorDataEmitter;

  @Inject
  @Channel("room-out")
  Emitter<RoomData> roomDataEmitter;

  @POST
  @Path("temp")
  @Consumes(MediaType.APPLICATION_JSON)
  public String injectTemperature(final SensorData data) {
    log.info("Inject: {}", data);

    final var metadata = OutgoingKafkaRecordMetadata.builder()
      .withKey(data.id())
      .build();

    final var message = Message.of(data, Metadata.of(metadata));

    sensorDataEmitter.send(message);

    return "Injected: " + data;
  }

  @POST
  @Path("room")
  @Consumes(MediaType.APPLICATION_JSON)
  public String injectRoom(final RoomData data) {
    log.info("Inject: {}", data);

    final var metadata = OutgoingKafkaRecordMetadata.builder()
      .withKey(data.name())
      .build();
    final var message = Message.of(data, Metadata.of(metadata));

    roomDataEmitter.send(message);

    return "Injected: " + data;
  }

}
