package nl.tudelft.ewi.git.client;

import java.net.URLEncoder;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import lombok.SneakyThrows;

class Backend {

	static interface Request<T> {
		T perform(Client client);
	}

	private final String host;

	Backend(String host) {
		this.host = host;
	}
	
	String getHost() {
		return host;
	}
	
	@SneakyThrows
	String encode(String value) {
		return URLEncoder.encode(value, "UTF-8");
	}
	
	String createUrl(String path) {
		StringBuilder url = new StringBuilder();
		if (host.endsWith("/")) {
			url.append(host.substring(0, host.length() - 1));
		}
		else {
			url.append(host);
		}
		url.append("/");
		if (path.startsWith("/")) {
			url.append(path.substring(1));
		}
		else {
			url.append(path);
		}
		return url.toString();
	}
	
	<T> T perform(Request<T> action) {
		Client client = ClientBuilder.newClient();
		try {
			return action.perform(client);
		}
		catch (ClientErrorException e) {
			throw e;
		}
		finally {
			client.close();
		}
	}

}
