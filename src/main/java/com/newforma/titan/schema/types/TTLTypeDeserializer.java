package com.newforma.titan.schema.types;

import java.io.IOException;
import java.time.Duration;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class TTLTypeDeserializer extends StdDeserializer<TTLType> {

	private static final long serialVersionUID = -339902057204450460L;

	public TTLTypeDeserializer() {
		super(TTLType.class);
	}

	public TTLTypeDeserializer(Class<TTLType> t) {
		super(t);
	}

	@Override
	public TTLType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		final ObjectCodec oc = p.getCodec();
		final String val = oc.readValue(p, String.class);

		try {
			return new TTLType(Duration.parse(val));
		} catch (DateTimeParseException e) {
			throw new JsonParseException(p, "Failed to parse ISO-8601 duration value \"" + val + "\"");
		}
	}
}
