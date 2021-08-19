package asaintsever.httpsinkconnector.http.authentication;

import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;

public class ConfigAuthenticationProvider implements IAuthenticationProvider {

    private String headerName;
    private String headerValue;

    @Override
    public ConfigDef configDef() {
        return ConfigAuthenticationProviderConfig.configDef();
    }
    
    @Override
    public void configure(Map<String, ?> configs) {
        ConfigAuthenticationProviderConfig config = new ConfigAuthenticationProviderConfig(configs);
        this.headerName = config.getHeaderName();
        this.headerValue = config.getHeaderValue();
    }
    
    @Override
    public String[] addAuthentication() throws AuthException {
        return new String[] {this.headerName, this.headerValue};
    }

}
