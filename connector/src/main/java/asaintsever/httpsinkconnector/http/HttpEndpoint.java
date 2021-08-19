package asaintsever.httpsinkconnector.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.kafka.connect.sink.SinkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import asaintsever.httpsinkconnector.event.formatter.IEventFormatter;
import asaintsever.httpsinkconnector.http.authentication.AuthException;
import asaintsever.httpsinkconnector.http.authentication.IAuthenticationProvider;


public class HttpEndpoint {
    
    private static final Logger log = LoggerFactory.getLogger(HttpEndpoint.class);

    private final String endpoint;
    private final long connectTimeout;
    private final long readTimeout;
    private final int batchSize;
    private final IAuthenticationProvider authenticationProvider;
    private final String contentType;
    private final IEventFormatter eventFormatter;
    private final List<Integer> validStatusCodes;
    
    private List<SinkRecord> batch = new ArrayList<>();
       
    
    public HttpEndpoint(
            String endpoint,
            long connectTimeout, long readTimeout,
            int batchSize,
            IAuthenticationProvider authenticationProvider,
            String contentType,
            IEventFormatter eventFormatter,
            List<Integer> validStatusCodes) {
        this.endpoint = endpoint;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.batchSize = batchSize;
        this.authenticationProvider = authenticationProvider;
        this.contentType = contentType;
        this.eventFormatter = eventFormatter;
        this.validStatusCodes = validStatusCodes;
    }
    
    
    public void write(Collection<SinkRecord> records) throws IOException, InterruptedException {
        for (SinkRecord record : records) {
            this.batch.add(record);
            
            // We got a complete batch: send it
            if (this.batch.size() >= this.batchSize) {
                this.sendBatch();
            }
        }
        
        // Send remaining records in one last batch (the records left if not enough to constitute a full batch)
        this.sendBatch();
    }
    
    private void sendBatch() throws IOException, InterruptedException {
        // Skip if batch list is empty
        if (this.batch.isEmpty())
            return;
        
        log.info("Sending batch of {} events to {}", this.batch.size(), this.endpoint);
        
        HttpResponse<String> resp;
        
        try {
            resp = this.invoke(this.eventFormatter.formatEventBatch(this.batch));
        } catch(AuthException authEx) {
            throw new HttpResponseException(this.endpoint, "Authentication error: " + authEx.getMessage());
        } finally {
            // Flush batch list
            this.batch.clear();
        }
        
        // Check status code
        if (resp != null) {
            for(int statusCode : this.validStatusCodes) {
                if (statusCode == resp.statusCode()) {
                    log.info("Response from HTTP endpoint {}: {} (status code: {})", this.endpoint, resp.body(), resp.statusCode());
                    return;
                }
            }
        }
        
        throw new HttpResponseException(this.endpoint, "Invalid response from HTTP endpoint", resp);
    }
    
    private HttpResponse<String> invoke(byte[] data) throws AuthException, IOException, InterruptedException {       
        HttpClient httpCli = HttpClient.newBuilder()
                //.version(Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(this.connectTimeout))
                .build();
        
        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(this.endpoint))
                .timeout(Duration.ofMillis(this.readTimeout))
                .header("Content-Type", this.contentType)
                .POST(BodyPublishers.ofString(new String(data)))
                ;
        
        // Add HTTP authentication header(s)
        if (this.authenticationProvider.addAuthentication() != null)
            reqBuilder.headers(this.authenticationProvider.addAuthentication());
        
        return httpCli.send(reqBuilder.build(), BodyHandlers.ofString());
    }
    
}
