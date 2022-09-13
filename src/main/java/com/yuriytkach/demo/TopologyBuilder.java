package com.yuriytkach.demo;

import java.time.Duration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;

import com.yuriytkach.demo.model.RoomAverageTemperature;
import com.yuriytkach.demo.model.RoomData;
import com.yuriytkach.demo.model.RoomTemperature;
import com.yuriytkach.demo.model.SensorData;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;

@ApplicationScoped
public class TopologyBuilder {

  private final ObjectMapperSerde<RoomData> roomDataSerde = new ObjectMapperSerde<>(RoomData.class);
  private final ObjectMapperSerde<SensorData> sensorDataSerde = new ObjectMapperSerde<>(SensorData.class);
  private final ObjectMapperSerde<RoomTemperature>  roomTempSerde = new ObjectMapperSerde<>(RoomTemperature.class);
  private final ObjectMapperSerde<RoomAverageTemperature>  roomAverageTempSerde = new ObjectMapperSerde<>(RoomAverageTemperature.class);
  private final ObjectMapperSerde<CountAndSum> countAndSumSerde = new ObjectMapperSerde<>(CountAndSum.class);

  @Produces
  public Topology buildTopology() {
    final StreamsBuilder builder = new StreamsBuilder();

    final KTable<String, RoomData> roomTable = builder.stream("room", Consumed.with(Serdes.String(), roomDataSerde))
      .selectKey((oldKey, data) -> data.sensorId())
      .toTable(
        Materialized.with(Serdes.String(), roomDataSerde)
      );

    final KStream<String, RoomTemperature> roomTempStream = builder.stream(
        "temperature",
        Consumed.with(Serdes.String(), sensorDataSerde)
      )
      .filter((key, data) -> data.value() > -50 && data.value() < 50)
      .join(
        roomTable,
        (temp, room) -> new RoomTemperature(room.name(), temp.value())
      );

    roomTempStream
      .selectKey((oldKey, data) -> data.name())
      .groupByKey(Grouped.with(Serdes.String(), roomTempSerde))
      .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(30)))
      .aggregate(
        CountAndSum::empty,
        ((key, value, aggregate) -> aggregate.addTemperature(value)),
        Materialized.with(Serdes.String(), countAndSumSerde)
      )
      .mapValues(CountAndSum::countAverage)
      .mapValues((windowKey, data) ->  new RoomAverageTemperature(windowKey.key(), data, windowKey.window().startTime()))
      .toStream((windowKey, data) -> windowKey.key())
      .to("tmp", Produced.with(Serdes.String(), roomAverageTempSerde));


    return builder.build();
  }

  public record CountAndSum(
    int sum,
    int count
  ) {

    public static CountAndSum empty() {
      return new CountAndSum(0, 0);
    }

    public CountAndSum addTemperature(final RoomTemperature value) {
      return new CountAndSum(this.sum + value.value(), this.count + 1);
    }

    public int countAverage() {
      if (this.count == 0) {
        return 0;
      }
      return this.sum / this.count;
    }
  }

}
