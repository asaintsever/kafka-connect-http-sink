package asaintsever.httpsinkconnector.http.authentication;

import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Width;

public class ConfigAuthenticationProviderConfig extends AbstractConfig {
    
    public static final String AUTH_GROUP ="ConfigAuthentication";
    
    public static final String HEADER_NAME_CONFIG = "header.name";
    public static final String HEADER_NAME_DISPLAYNAME = "HTTP Header name";
    public static final String HEADER_NAME_DOC = "Header name for HTTP endpoint authentication";

    public static final String HEADER_VALUE_CONFIG = "header.value";
    public static final String HEADER_VALUE_DISPLAYNAME = "HTTP Header value";
    public static final String HEADER_VALUE_DOC = "Header value for HTTP endpoint authentication";
    
    public static ConfigDef configDef() {
        return new ConfigDef()
            .define(HEADER_NAME_CONFIG, Type.STRING, ConfigDef.NO_DEFAULT_VALUE, Importance.HIGH, HEADER_NAME_DOC, AUTH_GROUP, 0, Width.MEDIUM, HEADER_NAME_DISPLAYNAME)
            .define(HEADER_VALUE_CONFIG, Type.STRING, ConfigDef.NO_DEFAULT_VALUE, Importance.HIGH, HEADER_VALUE_DOC, AUTH_GROUP, 1, Width.LONG, HEADER_VALUE_DISPLAYNAME)
            ;
    }
    
    
    public ConfigAuthenticationProviderConfig(Map<?, ?> originals) {
        super(configDef(), originals);
    }
    
    
    public String getHeaderName() {
        return getString(HEADER_NAME_CONFIG);
    }

    public String getHeaderValue() {
        return getString(HEADER_VALUE_CONFIG);
    }
}
