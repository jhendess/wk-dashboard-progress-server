package org.xlrnet.wk.dashboardprogressserver.config;

import com.j256.simplecsv.processor.CsvProcessor;
import org.springframework.stereotype.Component;
import org.xlrnet.wk.dashboardprogressserver.api.entity.HistoricEntry;

import javax.annotation.PostConstruct;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Simple message body writer which is capable of producing text/csv output for history entry lists in JAX RS.
 */
@Provider
@Component
@Produces("text/csv")
public class CsvMessageBodyWriter implements MessageBodyWriter<List<HistoricEntry>> {

    /** The Jackson CSV mapper. */
    private CsvProcessor<HistoricEntry> csvProcessor;

    @PostConstruct
    public void init() {
        csvProcessor = new CsvProcessor<HistoricEntry>(HistoricEntry.class);
        csvProcessor.initialize();
    }

    @Override
    public boolean isWriteable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return true; // Type checking is done by the jax rs runtime
    }

    @Override
    public long getSize(List<HistoricEntry> objects, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(List<HistoricEntry> objects, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        if (objects != null && objects.size() > 0) {
            try (Writer writer = new OutputStreamWriter(entityStream)) {
                csvProcessor.writeAll(writer, objects, true);
            }
        }
    }
}