package nl.tudelft.ewi.git.web.exceptions;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
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

	@Context
	private HttpServletRequest request;

	@Override
	public Response toResponse(IllegalArgumentException exception) {
		log.info(
			String.format(
				"Bad request for method %s at %s, failed with: %s",
				request.getMethod(),
				request.getRequestURL(),
				exception.getMessage()
			),
			exception
		);

		return Response.status(Status.BAD_REQUEST)
			.type(MediaType.TEXT_PLAIN)
			.header("Requested-URL", request.getRequestURL())
			.entity(exception.getMessage())
			.build();
	}

}
