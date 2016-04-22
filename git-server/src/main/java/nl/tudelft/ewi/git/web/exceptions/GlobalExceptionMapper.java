package nl.tudelft.ewi.git.web.exceptions;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
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

	@Context
	private HttpServletRequest request;

	@Override
	public Response toResponse(Exception exception) {
		log.warn(
			String.format(
				"Resource resulted in an exception for method %s at %s, failed with: %s",
				request.getMethod(),
				request.getRequestURL(),
				exception.getMessage()
			),
			exception
		);

		return Response.status(Status.INTERNAL_SERVER_ERROR)
			.type(MediaType.TEXT_PLAIN)
			.header("Requested-URL", request.getRequestURL())
			.entity(exception.getMessage())
			.build();
	}

}
