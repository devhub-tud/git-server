package nl.tudelft.ewi.git.web.exceptions;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * This class handles {@link NotFoundException}s thrown during the RESTEasy resource handling phase, and returns an
 * appropiate HTTP response to the requester.
 * 
 * @author michael
 */
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

	@Override
	public Response toResponse(NotFoundException exception) {
		return Response.status(Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).entity(exception.getMessage())
			.build();
	}

}
