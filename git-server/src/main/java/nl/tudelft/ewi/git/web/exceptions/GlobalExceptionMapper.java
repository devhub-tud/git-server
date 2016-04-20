package nl.tudelft.ewi.git.web.exceptions;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * This class handles {@link Exception}s thrown during the RESTEasy resource handling phase, and returns an
 * appropiate HTTP response to the requester.
 * 
 * @author Jan-Willem
 */
@Slf4j
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

	@Override
	public Response toResponse(Exception exception) {
		log.warn(exception.getMessage(), exception);
		return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(exception.getMessage())
			.build();
	}

}
