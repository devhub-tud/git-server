package nl.tudelft.ewi.git.web.jackson;

import java.io.IOException;

import javax.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.git.web.models.GroupModel;
import nl.tudelft.ewi.git.web.models.IdentifiableModel;
import nl.tudelft.ewi.git.web.models.UserModel;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Slf4j
@Provider
@SuppressWarnings("serial")
public class MappingModule extends SimpleModule {

	@Override
	public void setupModule(SetupContext context) {
		SimpleDeserializers deserializers = new SimpleDeserializers();
		addDeserializerForIdentifiableModel(deserializers);
		context.addDeserializers(deserializers);
	}

	private void addDeserializerForIdentifiableModel(SimpleDeserializers deserializers) {
		log.debug("Registering deserializer for: {}", IdentifiableModel.class);
		deserializers.addDeserializer(IdentifiableModel.class, new JsonDeserializer<IdentifiableModel>() {
			@Override
			public IdentifiableModel deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
				ObjectMapper mapper = (ObjectMapper) jp.getCodec();
				ObjectNode root = mapper.readTree(jp);

				JsonNode type = root.get("name");
				String name = type.textValue();
				
				JsonParser traverse = root.traverse();
				traverse.setCodec(mapper);

				if (name.startsWith("@")) {
					return mapper.readValue(traverse, GroupModel.class);
				}
				else {
					return mapper.readValue(traverse, UserModel.class);
				}
			}
		});
	}
	
}
