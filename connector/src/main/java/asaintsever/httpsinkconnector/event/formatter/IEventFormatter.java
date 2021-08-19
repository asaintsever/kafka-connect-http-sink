package asaintsever.httpsinkconnector.event.formatter;

import java.io.IOException;
import java.util.List;

import org.apache.kafka.connect.sink.SinkRecord;
import asaintsever.httpsinkconnector.config.IConfigAccessor;

public interface IEventFormatter extends IConfigAccessor {
   
    byte[] formatEventBatch(List<SinkRecord> batch) throws IOException;
}
