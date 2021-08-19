package asaintsever.httpsinkconnector.http.authentication;

import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;

public class NoAuthenticationProvider implements IAuthenticationProvider {
    
    @Override
    public ConfigDef configDef() {
        return new ConfigDef();
    }
    
    @Override
    public void configure(Map<String, ?> configs) {
    }
    
    @Override
    public String[] addAuthentication() throws AuthException {
        return null;
    }

}
