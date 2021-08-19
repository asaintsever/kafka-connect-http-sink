package asaintsever.httpsinkconnector.utils;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpSinkConnectorInfo {
    private static final Logger log = LoggerFactory.getLogger(HttpSinkConnectorInfo.class);
    
    private static final String INFO_PATH = "/http-sink-connector.properties";
    private static final String VERSION;
    protected static final String DEFAULT_VALUE = "unknown";
    
    static {
        Properties props = new Properties();
        
        try (InputStream resourceStream = HttpSinkConnectorInfo.class.getResourceAsStream(INFO_PATH)) {
            props.load(resourceStream);
        } catch (Exception e) {
            log.warn("Error while loading resource file {}", INFO_PATH, e);
        }
        
        VERSION = props.getProperty("version", DEFAULT_VALUE).trim();
    }

    public static String getVersion() {
        return VERSION;
    }
}
