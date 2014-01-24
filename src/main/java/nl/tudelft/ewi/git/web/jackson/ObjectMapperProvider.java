package nl.tudelft.ewi.git.web.jackson;

import javax.inject.Inject;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

	private final ObjectMapper mapper;

	@Inject
	ObjectMapperProvider(ObjectMapper mapper) {
		this.mapper = mapper;
		mapper.registerModule(new MappingModule());
	}

	@Override
	public ObjectMapper getContext(Class<?> type) {
		return mapper;
	}

}
