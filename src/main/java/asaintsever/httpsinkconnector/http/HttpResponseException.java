package asaintsever.httpsinkconnector.http;

import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponseException extends RuntimeException {

    private static final long serialVersionUID = -1406594721325855805L;
    private static final Logger log = LoggerFactory.getLogger(HttpResponseException.class);
    
    private final String endpoint;
    private final HttpResponse<String> resp;

    
    public HttpResponseException(String endpoint, String msg) {
        this(endpoint, msg, null);
    }
    
    public HttpResponseException(String endpoint, String msg, HttpResponse<String> resp) {
        super(msg);
        
        this.endpoint = endpoint;
        this.resp = resp;
        
        if(resp != null)
            log.error("Error trying to access HTTP endpoint {}: {} (body: {}) (status code: {})", getEndpoint(), msg, getBody(), getStatus());
        else
            log.error("Error trying to access HTTP endpoint {}: {}", getEndpoint(), msg);
    }
    
    public String getEndpoint() {
        return this.endpoint;
    }
    
    public String getBody() {
        return (this.resp != null ? this.resp.body() : null);
    }
    
    public Integer getStatus() {
        return (this.resp != null ? this.resp.statusCode() : null);
    }
    
}
