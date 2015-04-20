package nl.tudelft.ewi.git.web.providers;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.eclipse.jgit.lib.ObjectLoader;
import org.jboss.resteasy.plugins.providers.InputStreamProvider;

/**
 * The {@code ObjectLoaderBodyWriter} is a {@link MessageBodyWriter} which
 * allows sending {@link ObjectLoader} and make the servlet container aware of
 * the object size, which can be used to determine the {@code Content-Length} or
 * decide whether or not to use chunked encoding.
 * 
 * Responses for the type {@link ObjectLoader} can be fetched using, for
 * example, an {@link InputStreamProvider}.
 * 
 * @author Jan-Willem Gmelig Meyling
 * @see InputStreamProvider
 */
@Provider
@Produces(MediaType.WILDCARD)
@Consumes(MediaType.WILDCARD)
public class ObjectLoaderBodyWriter implements MessageBodyWriter<ObjectLoader> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return ObjectLoader.class.isAssignableFrom(type);
	}

	@Override
	public long getSize(ObjectLoader t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return t.getSize();
	}

	@Override
	public void writeTo(ObjectLoader t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
		/*
		 * Set the content length since it is known
		 * - http://stackoverflow.com/a/2419423
		 * - https://issues.apache.org/jira/browse/CXF-5349
		 */
		httpHeaders.add("Content-Length", t.getSize());
		t.copyTo(entityStream);
	}

}
