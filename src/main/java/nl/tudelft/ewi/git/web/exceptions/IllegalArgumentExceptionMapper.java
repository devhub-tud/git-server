package nl.tudelft.ewi.git.web.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;

/**
 * This class handles {@link IllegalArgumentException}s thrown during the RESTEasy resource handling phase, and returns
 * an appropiate HTTP response to the requester.
 * 
 * @author michael
 */
@Slf4j
@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

	@Override
	public Response toResponse(IllegalArgumentException exception) {
		log.warn(exception.getMessage(), exception);
		return Response.status(Status.CONFLICT).type(MediaType.APPLICATION_JSON).entity(exception.getMessage()).build();
	}

}
