FROM maven:3.8.1-openjdk-11 as build

COPY . /connector/

RUN cd /connector && mvn clean package

FROM quay.io/strimzi/kafka:0.24.0-kafka-2.7.0

USER root:root

RUN mkdir -p /opt/kafka/plugins

# Install Confluent Hub Client to easily install Kafka Connect connectors & converters
RUN curl -o /tmp/chc.tar.gz -f http://client.hub.confluent.io/confluent-hub-client-latest.tar.gz \
    && tar -xzvf /tmp/chc.tar.gz \
    && rm -f /tmp/chc.tar.gz

# Install AVRO converter (required if our connector has to deal with AVRO encoding)
RUN bin/confluent-hub install --no-prompt --worker-configs config/connect-distributed.properties --component-dir /opt/kafka/plugins confluentinc/kafka-connect-avro-converter:6.2.0

# Install our HTTP Sink Connector
COPY --from=build /connector/target/components/packages/asaintsever-http-sink-connector-*.zip /tmp/
RUN unzip -d /opt/kafka/plugins /tmp/asaintsever-http-sink-connector-*.zip \
    && rm -f /tmp/asaintsever-http-sink-connector-*.zip

USER 1001
