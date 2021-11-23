# Kafka Connect HTTP Sink Connector

The HTTP Sink Connector is a sample implementation of a Kafka Connect connector. This is a `sink` connector, reading events from kafka topics to send them to some HTTP endpoint.

Using connector's configuration, you can set the list of Kafka topics to read from and the target HTTP endpoint (only one supported in this implementation). You can also provide your own event formatters (see default [PassthroughStringEventFormatter](src/main/java/asaintsever/httpsinkconnector/event/formatter/PassthroughStringEventFormatter.java) as an example) and HTTP authentication providers (see default [NoAuthenticationProvider](src/main/java/asaintsever/httpsinkconnector/http/authentication/NoAuthenticationProvider.java) and [ConfigAuthenticationProvider](src/main/java/asaintsever/httpsinkconnector/http/authentication/ConfigAuthenticationProvider.java) as examples).

This connector will batch events before sending them in order to reduce the number of calls and not overwhelm the HTTP endpoint.

Refer to [Configuration](#configuration) section for the full list of properties and default values.

## Configuration

Here is the complete list of properties you can set on the connector. You can find some examples of running configurations here:

- [httpsink-connector-raw.yaml](deploy/k8s/httpsink-connector-raw.yaml)
- [httpsink-connector-json.yaml](deploy/k8s/httpsink-connector-json.yaml)

> **Important note:** Kafka Connect defines global configuration properties for connectors (e.g.: `topics`, `key.converter`, `value.converter`, `transforms.*`, `errors.*`). Those properties are ***not*** defined below (we stick with properties of our connector only).
>
> Refer to official Kafka/Confluent documentation for the global properties:
>
> - <https://docs.confluent.io/platform/current/installation/configuration/connect/index.html>
> - <https://docs.confluent.io/platform/current/installation/configuration/connect/sink-connect-configs.html>

| Property | Default value | Description |
|---|---|---|
| `event.batch.maxsize` | 10 | The maximum number of events to consume and consider before invoking the HTTP endpoint |
| `event.formatter.class` | asaintsever.httpsinkconnector.event.formatter.PassthroughStringEventFormatter | The name of the class to format the event. This class should implement the interface IEventFormatter |
| `event.formatter.param.*` | | Event formatter's properties |
| `http.endpoint` | | The URI where the connector should try to send the data |
| `http.timeout.connect.ms` | 60000 | HTTP connect timeout in ms when connecting to HTTP endpoint |
| `http.timeout.read.ms` | 60000 | HTTP read timeout in ms when reading response from HTTP endpoint |
| `http.request.content.type` | "application/json" | The value of Content-Type header for HTTP request |
| `http.request.authentication.provider.class` | asaintsever.httpsinkconnector.http.authentication.NoAuthenticationProvider | The name of the class to perform HTTP authentication. This class should implement the interface IAuthenticationProvider |
| `http.request.authentication.provider.param.*` | | HTTP authentication provider's properties |
| `http.response.valid.status.codes` | 200,201,202,204 | A list with the HTTP response status codes indicating success |
| `http.request.retry.exp.backoff.base.interval.ms` | 5000 | The exponential backoff retry base interval in ms for a errored request |
| `http.request.retry.exp.backoff.multiplier` | 2.5 | The exponential backoff retry multiplier for a errored request |
| `http.request.retry.maxattempts` | 5 | Max number of retries for a errored request |

## Build

### Connector archive

Packages the connector into an archive file for use on Confluent Hub or to easily install it as a Connect plugin in custom Docker images.

The archive will be a ZIP file generated in the `target/components/packages` directory.

> *Pre-requisites: Java 11 JDK and Maven are required*

```sh
make connector-archive
```

### Connector image

Generates a ready to use image of the connector for your Kafka Connect cluster.

> *Pre-requisites: Docker or Podman installed*

```sh
make connector-image
```

## Deploy

> *Pre-requisites: kubectl, helm (v3), Kubernetes cluster available*
>
> For e.g., to create a local test k8s cluster, using Minikube (refer to Minikube doc for drivers and detailed instructions):
>
> ```sh
> minikube -p test-cluster start --cpus 4 --memory 4g --driver <your driver: kvm2, podman ...>
> ```

We'll leverage [Strimzi](https://strimzi.io/) to quickly and easily install a Kafka cluster and Kafka Connect runtime on Kubernetes:

```sh
# Deploy Strimzi Operator via Strimzi Helm Chart
helm repo add strimzi https://strimzi.io/charts/

helm install strimzi-kafka-operator strimzi/strimzi-kafka-operator --version 0.24.0 -f deploy/k8s/kafka/strimzi-operator-values.yaml --debug

# Now, using Strimzi CRDs, create Kafka cluster and test topics
kubectl apply -f deploy/k8s/kafka/cluster.yaml
kubectl apply -f deploy/k8s/kafka/topics.yaml
```

> For testing purpose, let's deploy [kcat](https://github.com/edenhill/kcat) utility (formerly known as `kafkacat`):
>
> ```sh
> kubectl run -i --tty --attach=false kcat --command --image=edenhill/kcat:1.7.0 -- cat
> ```

Check cluster status:

```sh
TOOLSPOD="kubectl exec -i local-kafka-0 -- sh -c"
KCATPOD="kubectl exec -i kcat -- sh -c"

# Get Kafka broker metadata
${KCATPOD} "kcat -b local-kafka-brokers:9092 -L"

# Get Kafka broker config
${TOOLSPOD} "bin/kafka-configs.sh --describe --all --bootstrap-server local-kafka-brokers:9092 --broker 0"
```

Let's deploy a test HTTP endpoint:

```sh
kubectl apply -f deploy/k8s/http_endpoint/echo-service.yaml
```

Last, create a Kafka Connect cluster and deploy 2 instances of our HTTP Sink connector to test different configurations (one instance dealing with plain text events, another with JSON events):

```sh
kubectl apply -f deploy/k8s/httpsink-kafkaconnect.yaml
kubectl apply -f deploy/k8s/httpsink-connector-raw.yaml
kubectl apply -f deploy/k8s/httpsink-connector-json.yaml
```

Check Kafka Connect HTTP Sink connector:

```sh
${TOOLSPOD} "curl -s -X GET http://httpsinkconnector-connect-api:8083/connector-plugins" | jq .

${TOOLSPOD} "curl -s -X GET http://httpsinkconnector-connect-api:8083/connectors/http-sink-raw" | jq .
${TOOLSPOD} "curl -s -X GET http://httpsinkconnector-connect-api:8083/connectors/http-sink-raw/status" | jq .

${TOOLSPOD} "curl -s -X GET http://httpsinkconnector-connect-api:8083/connectors/http-sink-json" | jq .
${TOOLSPOD} "curl -s -X GET http://httpsinkconnector-connect-api:8083/connectors/http-sink-json/status" | jq .
```

## Test

1) Post single text & JSON events:

    ```sh
    # Post single events
    # (do not forget the 'jq . -c' part for the JSON sample as we must provide a one line string otherwise kcat will interpret each line of the file as a new value ...)
    cat samples/sample.txt | ${KCATPOD} "kcat -b local-kafka-brokers:9092 -P -t raw-events"
    cat samples/sample.json | jq . -c | ${KCATPOD} "kcat -b local-kafka-brokers:9092 -P -t json-events-1"
    ```

    Look at the HTTP Sink connector's log (you should see outputs from our test HTTP endpoint):

    ```sh
    kubectl logs <HTTP Sink connector pod>
    ```

2) Post batch of text & JSON events:

    ```sh
    # Post batches of events
    # (each event must be on one line in the batch file)
    cat samples/sample.txt.batch | ${KCATPOD} "kcat -b local-kafka-brokers:9092 -P -t raw-events"
    cat samples/sample.json.batch | ${KCATPOD} "kcat -b local-kafka-brokers:9092 -P -t json-events-1"
    ```

    Look at the HTTP Sink connector's log (you should see outputs from our test HTTP endpoint with events sent in batch):

    ```sh
    kubectl logs <HTTP Sink connector pod>
    ```

To inspect content of Kafka topics and errors from HTTP Sink connector (posted in dlq-httpsinkconnector topic):

```sh
${KCATPOD} "kcat -b local-kafka-brokers:9092 -C -t raw-events -q -f '%T %k %s %p %o\n' -e"
${KCATPOD} "kcat -b local-kafka-brokers:9092 -C -t json-events-1 -q -f '%T %k %s %p %o\n' -e"

# For HTTP Sink connector errors
${KCATPOD} "kcat -b local-kafka-brokers:9092 -C -t dlq-httpsinkconnector -q -f '%T %h %k %s %p %o\n' -e"
```
