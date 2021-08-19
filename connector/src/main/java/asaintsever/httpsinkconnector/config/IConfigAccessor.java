package asaintsever.httpsinkconnector.config;

import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;

public interface IConfigAccessor {

    ConfigDef configDef();

    void configure(Map<String, ?> configs);
}
