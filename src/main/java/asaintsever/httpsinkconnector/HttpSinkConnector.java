package asaintsever.httpsinkconnector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import asaintsever.httpsinkconnector.config.HttpSinkConnectorConfig;
import asaintsever.httpsinkconnector.utils.HttpSinkConnectorInfo;

public class HttpSinkConnector extends SinkConnector {
    
    private static final Logger log = LoggerFactory.getLogger(HttpSinkConnector.class);
    private Map<String, String> connectorConfig = new HashMap<>();

    @Override
    public String version() {
        return HttpSinkConnectorInfo.getVersion();
    }

    @Override
    public void start(Map<String, String> props) {
        log.info("Starting CCS Event Publisher HTTP Sink Connector");
        if (!connectorConfig.isEmpty()) {
            this.context.raiseError(new RuntimeException("Connector has not been stopped"));
            return;
        }
        
        connectorConfig.putAll(props);
    }

    @Override
    public Class<? extends Task> taskClass() {
        return HttpSinkTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        List<Map<String, String>> taskConfigs = new ArrayList<>(maxTasks);
        
        for (int task = 0; task < maxTasks; task++) {
            taskConfigs.add(new HashMap<>(connectorConfig));
        }
        
        return taskConfigs;
    }

    @Override
    public void stop() {
        log.info("Stopping CCS Event Publisher HTTP Sink Connector");
        connectorConfig.clear();
    }

    @Override
    public ConfigDef config() {
        return HttpSinkConnectorConfig.configDef(Collections.emptyMap());
    }

}
