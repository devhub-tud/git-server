package nl.tudelft.ewi.git.web.security;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.interception.PostMatchContainerRequestContext;

import com.google.common.base.Strings;

@Singleton
@javax.ws.rs.ext.Provider
public class SecurityFilter implements ContainerRequestFilter {

	private final Provider<RequestScope> requestScopeProvider;

	@Inject
	public SecurityFilter(Provider<RequestScope> requestScopeProvider) {
		this.requestScopeProvider = requestScopeProvider;
	}

	@Override
	public void filter(ContainerRequestContext ctx) throws IOException {
		if (!(ctx instanceof PostMatchContainerRequestContext)) {
			return;
		}

		PostMatchContainerRequestContext context = (PostMatchContainerRequestContext) ctx;
		ResourceMethodInvoker resourceMethod = context.getResourceMethod();
		Method method = resourceMethod.getMethod();
		Class<?> resource = method.getDeclaringClass();
		checkRequireAuthentication(ctx, method, resource);
	}

	private void checkRequireAuthentication(ContainerRequestContext ctx, Method method, Class<?> resource) {
		RequireAuthentication annotation = getAnnotation(method, resource, RequireAuthentication.class);
		if (annotation != null) {
			RequestScope requestScope = requestScopeProvider.get();
			if (Strings.isNullOrEmpty(requestScope.getClientId())) {
				ctx.abortWith(Response.status(Status.UNAUTHORIZED)
						.entity("You need to be authenticated to use this resource!")
						.build());
			}
		}
	}
	
	private <T extends Annotation> T getAnnotation(Method method, Class<?> resource, Class<T> annotationType) {
		T annotation = method.getAnnotation(annotationType);
		if (annotation == null) {
			annotation = resource.getAnnotation(annotationType);
		}
		return annotation;
	}

}
