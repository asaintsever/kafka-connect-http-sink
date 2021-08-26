package asaintsever.httpsinkconnector.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Validator;
import org.apache.kafka.common.config.ConfigDef.Width;
import org.apache.kafka.common.config.ConfigException;
import asaintsever.httpsinkconnector.event.formatter.PassthroughStringEventFormatter;
import asaintsever.httpsinkconnector.event.formatter.IEventFormatter;
import asaintsever.httpsinkconnector.http.authentication.IAuthenticationProvider;
import asaintsever.httpsinkconnector.http.authentication.NoAuthenticationProvider;
import asaintsever.httpsinkconnector.utils.ClassLoaderHelper;

public class HttpSinkConnectorConfig extends AbstractConfig {
    
    private static final String EVENT_GROUP = "Event";
    private static final String HTTP_GROUP = "HTTP";
    private static final String RETRIES_GROUP = "Retries";

    // Event settings
    public static final String EVENT_MAX_BATCH_SIZE_CONFIG = "event.batch.maxsize";
    private static final int EVENT_MAX_BATCH_SIZE_DEFAULT = 10;
    public static final String EVENT_MAX_BATCH_SIZE_DISPLAYNAME = "Event Max Batch Size";
    public static final String EVENT_MAX_BATCH_SIZE_DOC = "The maximum number of events to consume and consider before invoking the HTTP endpoint";
    
    public static final String EVENT_FORMATTER_CLASS_CONFIG = "event.formatter.class";
    private static final String EVENT_FORMATTER_CLASS_DEFAULT = PassthroughStringEventFormatter.class.getCanonicalName();
    public static final String EVENT_FORMATTER_CLASS_DISPLAYNAME = "Event Formatter Class";
    public static final String EVENT_FORMATTER_CLASS_DOC = "The name of the class to format the event. This class should implement the interface IEventFormatter";
    public static final String EVENT_FORMATTER_CLASS_PARAM_PREFIX = "event.formatter.param.";
    public static final String EVENT_FORMATTER_CLASS_GROUP_PREFIX = EVENT_GROUP + ".Formatting.";
    
    // HTTP settings
    public static final String HTTP_ENDPOINT_CONFIG = "http.endpoint";
    public static final String HTTP_ENDPOINT_DISPLAYNAME = "HTTP Endpoint";
    public static final String HTTP_ENDPOINT_DOC = "The URI where the connector should try to send the data";
    
    public static final String HTTP_CONNECT_TIMEOUT_CONFIG = "http.timeout.connect.ms";
    private static final long HTTP_CONNECT_TIMEOUT_DEFAULT = 60000;
    public static final String HTTP_CONNECT_TIMEOUT_DISPLAYNAME = "HTTP Connect Timeout (ms)";
    public static final String HTTP_CONNECT_TIMEOUT_DOC = "HTTP connect timeout in ms when connecting to HTTP endpoint";
    
    public static final String HTTP_READ_TIMEOUT_CONFIG = "http.timeout.read.ms";
    private static final long HTTP_READ_TIMEOUT_DEFAULT = 60000;
    public static final String HTTP_READ_TIMEOUT_DISPLAYNAME = "HTTP Read Timeout (ms)";
    public static final String HTTP_READ_TIMEOUT_DOC = "HTTP read timeout in ms when reading response from HTTP endpoint";

    public static final String HTTP_REQ_CONTENT_TYPE_CONFIG = "http.request.content.type";
    private static final String HTTP_REQ_CONTENT_TYPE_DEFAULT = "application/json";
    public static final String HTTP_REQ_CONTENT_TYPE_DISPLAYNAME = "HTTP Request Content Type";
    public static final String HTTP_REQ_CONTENT_TYPE_DOC = "The value of Content-Type header for HTTP request";
    
    public static final String HTTP_REQ_AUTHENTICATION_PROVIDER_CLASS_CONFIG = "http.request.authentication.provider.class";
    private static final String HTTP_REQ_AUTHENTICATION_PROVIDER_CLASS_DEFAULT = NoAuthenticationProvider.class.getCanonicalName();
    public static final String HTTP_REQ_AUTHENTICATION_PROVIDER_CLASS_DISPLAYNAME = "HTTP Request Authentication Provider Class";
    public static final String HTTP_REQ_AUTHENTICATION_PROVIDER_CLASS_DOC = "The name of the class to perform HTTP authentication. This class should implement the interface IAuthenticationProvider";
    public static final String HTTP_REQ_AUTHENTICATION_PROVIDER_CLASS_PARAM_PREFIX = "http.request.authentication.provider.param.";
    public static final String HTTP_REQ_AUTHENTICATION_PROVIDER_CLASS_GROUP_PREFIX = HTTP_GROUP + ".Authentication.";
    
    public static final String HTTP_RESP_VALID_STATUS_CODES_CONFIG = "http.response.valid.status.codes";
    private static final List<String> HTTP_RESP_VALID_STATUS_CODES_DEFAULT = Arrays.asList("200", "201", "202", "204");
    public static final String HTTP_RESP_VALID_STATUS_CODES_DISPLAYNAME = "Valid HTTP Response Status Codes";
    public static final String HTTP_RESP_VALID_STATUS_CODES_DOC = "A list with the HTTP response status codes indicating success";

    // Retries settings
    public static final String HTTP_REQ_RETRY_EXPBACKOFF_BASE_INTERVAL_MS_CONFIG = "http.request.retry.exp.backoff.base.interval.ms";
    private static final long HTTP_REQ_RETRY_EXPBACKOFF_BASE_INTERVAL_MS_DEFAULT = 5000;
    public static final String HTTP_REQ_RETRY_EXPBACKOFF_BASE_INTERVAL_MS_DISPLAYNAME = "HTTP Request Exponential Backoff Retry Base Interval (ms)";
    public static final String HTTP_REQ_RETRY_EXPBACKOFF_BASE_INTERVAL_MS_DOC = "The exponential backoff retry base interval in ms for a errored request";
    
    public static final String HTTP_REQ_RETRY_EXPBACKOFF_MULTIPLIER_CONFIG = "http.request.retry.exp.backoff.multiplier";
    private static final double HTTP_REQ_RETRY_EXPBACKOFF_MULTIPLIER_DEFAULT = 2.5;
    public static final String HTTP_REQ_RETRY_EXPBACKOFF_MULTIPLIER_DISPLAYNAME = "HTTP Request Exponential Backoff Retry Multiplier";
    public static final String HTTP_REQ_RETRY_EXPBACKOFF_MULTIPLIER_DOC = "The exponential backoff retry multiplier for a errored request";
    
    public static final String HTTP_REQ_RETRY_MAX_ATTEMPTS_CONFIG = "http.request.retry.maxattempts";
    private static final int HTTP_REQ_RETRY_MAX_ATTEMPTS_DEFAULT = 5;
    public static final String HTTP_REQ_RETRY_MAX_ATTEMPTS_DISPLAYNAME = "HTTP Request Retry Max Attempts";
    public static final String HTTP_REQ_RETRY_MAX_ATTEMPTS_DOC = "Max number of retries for a errored request";
        
    
    public static ConfigDef configDef(Map<?, ?> originals) {
        ConfigDef connectorConfigDef = new ConfigDef()
                // Event group
                .define(EVENT_MAX_BATCH_SIZE_CONFIG, Type.INT, EVENT_MAX_BATCH_SIZE_DEFAULT, Importance.MEDIUM, EVENT_MAX_BATCH_SIZE_DOC, EVENT_GROUP, 0, Width.SHORT, EVENT_MAX_BATCH_SIZE_DISPLAYNAME)
                .define(EVENT_FORMATTER_CLASS_CONFIG, Type.STRING, EVENT_FORMATTER_CLASS_DEFAULT, new ClassValidator(IEventFormatter.class), Importance.MEDIUM, EVENT_FORMATTER_CLASS_DOC, EVENT_GROUP, 1, Width.MEDIUM, EVENT_FORMATTER_CLASS_DISPLAYNAME)
                // HTTP group
                .define(HTTP_ENDPOINT_CONFIG, Type.STRING, Importance.HIGH, HTTP_ENDPOINT_DOC, HTTP_GROUP, 0, Width.LONG, HTTP_ENDPOINT_DISPLAYNAME)
                .define(HTTP_CONNECT_TIMEOUT_CONFIG, Type.LONG, HTTP_CONNECT_TIMEOUT_DEFAULT, Importance.MEDIUM, HTTP_CONNECT_TIMEOUT_DOC, HTTP_GROUP, 1, Width.SHORT, HTTP_CONNECT_TIMEOUT_DISPLAYNAME)
                .define(HTTP_READ_TIMEOUT_CONFIG, Type.LONG, HTTP_READ_TIMEOUT_DEFAULT, Importance.MEDIUM, HTTP_READ_TIMEOUT_DOC, HTTP_GROUP, 2, Width.SHORT, HTTP_READ_TIMEOUT_DISPLAYNAME)
                .define(HTTP_REQ_CONTENT_TYPE_CONFIG, Type.STRING, HTTP_REQ_CONTENT_TYPE_DEFAULT, Importance.MEDIUM, HTTP_REQ_CONTENT_TYPE_DOC, HTTP_GROUP, 3, Width.MEDIUM, HTTP_REQ_CONTENT_TYPE_DISPLAYNAME)
                .define(HTTP_REQ_AUTHENTICATION_PROVIDER_CLASS_CONFIG, Type.STRING, HTTP_REQ_AUTHENTICATION_PROVIDER_CLASS_DEFAULT, new ClassValidator(IAuthenticationProvider.class), Importance.MEDIUM, HTTP_REQ_AUTHENTICATION_PROVIDER_CLASS_DOC, HTTP_GROUP, 4, Width.MEDIUM, HTTP_REQ_AUTHENTICATION_PROVIDER_CLASS_DISPLAYNAME)
                .define(HTTP_RESP_VALID_STATUS_CODES_CONFIG, Type.LIST, HTTP_RESP_VALID_STATUS_CODES_DEFAULT, Importance.HIGH, HTTP_RESP_VALID_STATUS_CODES_DOC, HTTP_GROUP, 5, Width.SHORT, HTTP_RESP_VALID_STATUS_CODES_DISPLAYNAME)
                // Retries group
                .define(HTTP_REQ_RETRY_EXPBACKOFF_BASE_INTERVAL_MS_CONFIG, Type.LONG, HTTP_REQ_RETRY_EXPBACKOFF_BASE_INTERVAL_MS_DEFAULT, Importance.MEDIUM, HTTP_REQ_RETRY_EXPBACKOFF_BASE_INTERVAL_MS_DOC, RETRIES_GROUP, 0, Width.SHORT, HTTP_REQ_RETRY_EXPBACKOFF_BASE_INTERVAL_MS_DISPLAYNAME)
                .define(HTTP_REQ_RETRY_EXPBACKOFF_MULTIPLIER_CONFIG, Type.DOUBLE, HTTP_REQ_RETRY_EXPBACKOFF_MULTIPLIER_DEFAULT, Importance.MEDIUM, HTTP_REQ_RETRY_EXPBACKOFF_MULTIPLIER_DOC, RETRIES_GROUP, 1, Width.SHORT, HTTP_REQ_RETRY_EXPBACKOFF_MULTIPLIER_DISPLAYNAME)
                .define(HTTP_REQ_RETRY_MAX_ATTEMPTS_CONFIG, Type.INT, HTTP_REQ_RETRY_MAX_ATTEMPTS_DEFAULT, Importance.MEDIUM, HTTP_REQ_RETRY_MAX_ATTEMPTS_DOC, RETRIES_GROUP, 2, Width.SHORT, HTTP_REQ_RETRY_MAX_ATTEMPTS_DISPLAYNAME)
                ;
        
        // Add config defined for EVENT_FORMATTER_CLASS_CONFIG
        embedConfigDef(connectorConfigDef, originals,
                EVENT_FORMATTER_CLASS_CONFIG, EVENT_FORMATTER_CLASS_DEFAULT,
                EVENT_FORMATTER_CLASS_PARAM_PREFIX, EVENT_FORMATTER_CLASS_GROUP_PREFIX);

        // Add config defined for HTTP_REQ_AUTHENTICATION_PROVIDER_CLASS_CONFIG
        embedConfigDef(connectorConfigDef, originals,
                HTTP_REQ_AUTHENTICATION_PROVIDER_CLASS_CONFIG, HTTP_REQ_AUTHENTICATION_PROVIDER_CLASS_DEFAULT,
                HTTP_REQ_AUTHENTICATION_PROVIDER_CLASS_PARAM_PREFIX, HTTP_REQ_AUTHENTICATION_PROVIDER_CLASS_GROUP_PREFIX);
        
        return connectorConfigDef;
    }
    
    private static ConfigDef embedConfigDef(ConfigDef connectorConfigDef, Map<?, ?> originals, 
                                        String configName, String configDefault, 
                                        String configPrefix, String groupPrefix) {
        Object configClassObj = ConfigDef.parseType(configName, originals.get(configName), Type.STRING);
        String configClassName = (configClassObj instanceof String) ? (String) configClassObj : configDefault;
        
        IConfigAccessor cfgAccess = ClassLoaderHelper.INSTANCE.newInstance(IConfigAccessor.class, configClassName);
        connectorConfigDef.embed(configPrefix, groupPrefix, 0, cfgAccess.configDef());
        return connectorConfigDef;
    }
    
    
    public HttpSinkConnectorConfig(Map<?, ?> originals) {
        super(configDef(originals), originals);
    }
    
    
    public Integer getEventMaxBatchSize() {
        return getInt(EVENT_MAX_BATCH_SIZE_CONFIG);
    }
    
    public IEventFormatter getEventFormatter() {
        return ClassLoaderHelper.INSTANCE.newInstance(IEventFormatter.class, getString(EVENT_FORMATTER_CLASS_CONFIG));
    }
    
    public String getHttpEndpoint() {
        return getString(HTTP_ENDPOINT_CONFIG);
    }
    
    public Long getHttpConnectTimeout() {
        return getLong(HTTP_CONNECT_TIMEOUT_CONFIG);
    }
    
    public Long getHttpReadTimeout() {
        return getLong(HTTP_READ_TIMEOUT_CONFIG);
    }

    public String getHttpReqContentType() {
        return getString(HTTP_REQ_CONTENT_TYPE_CONFIG);
    }
    
    public IAuthenticationProvider getHttpReqAuthProvider() {
        return ClassLoaderHelper.INSTANCE.newInstance(IAuthenticationProvider.class, getString(HTTP_REQ_AUTHENTICATION_PROVIDER_CLASS_CONFIG));
    }
    
    public List<Integer> getHttpRespValidStatusCodes() {
        return getList(HTTP_RESP_VALID_STATUS_CODES_CONFIG).stream().map(Integer::parseInt).collect(Collectors.toList());
    }
    
    public Long getHttpReqRetryExpBackoffBaseIntervalMs() {
        return getLong(HTTP_REQ_RETRY_EXPBACKOFF_BASE_INTERVAL_MS_CONFIG);
    }
    
    public Double getHttpReqRetryExpBackoffMultiplier() {
        return getDouble(HTTP_REQ_RETRY_EXPBACKOFF_MULTIPLIER_CONFIG);
    }
    
    public Integer getHttpReqRetryMaxAttempts() {
        return getInt(HTTP_REQ_RETRY_MAX_ATTEMPTS_CONFIG);
    }
    

    static class ClassValidator implements Validator {
        
        final Class<?> targetClass;
        
        ClassValidator(Class<?> targetClass) {
            if (targetClass == null) {
                throw new IllegalArgumentException("TargetClass cannot be null");
            }
            
            this.targetClass = targetClass;
        }

        @Override
        public void ensureValid(String name, Object value) {
            if (value == null) {
                throw new ConfigException(name, value, "Value can not be null");
            }
            
            if (value instanceof String) {
                try {
                    Class<?> clazz = ClassLoaderHelper.INSTANCE.classForName((String) value);
                    
                    if (!targetClass.isAssignableFrom(clazz)) {
                        throw new ConfigException(name, value, "Class does not implement or extend " + targetClass.getName());
                    }
                } catch (ClassNotFoundException cnfe) {
                    throw new ConfigException(name, value, "Could not find class");
                }
            } else {
                throw new ConfigException(name, value, "Value must be of type String, got " + value.getClass().getName());
            }
        }
    }
}
