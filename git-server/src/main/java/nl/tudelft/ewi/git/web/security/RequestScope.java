package nl.tudelft.ewi.git.web.security;

import org.jboss.resteasy.plugins.guice.RequestScoped;

@RequestScoped
public class RequestScope {

	public String getClientId() {
		return "test-client";
	}
	
	public String getUser() {
		return "mdejong2";
	}

}
