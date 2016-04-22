package nl.tudelft.ewi.git.web.exceptions;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
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
@Slf4j
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

	@Context
	private HttpServletRequest request;

	@Override
	public Response toResponse(NotFoundException exception) {
		log.warn(
			String.format(
				"Resource was not found for method %s at %s, failed with: %s",
				request.getMethod(),
				request.getRequestURL(),
				exception.getMessage()
			),
			exception
		);

		return Response.status(Status.NOT_FOUND)
			.type(MediaType.TEXT_PLAIN)
			.entity(exception.getMessage())
			.build();
	}

}
