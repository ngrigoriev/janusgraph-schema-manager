package com.newforma.titan.schema.types;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class DoctagListTypeDeserializer extends StdDeserializer<DoctagListType> {

    private static final long serialVersionUID = -8548101953133470051L;

    public DoctagListTypeDeserializer() {
		super(DoctagListType.class);
	}

	public DoctagListTypeDeserializer(Class<DoctagListType> t) {
		super(t);
	}

	@Override
	public DoctagListType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		final ObjectCodec oc = p.getCodec();
		final String val = oc.readValue(p, String.class);

	    return new DoctagListType(new LinkedHashSet<>(Arrays.asList(StringUtils.stripAll(StringUtils.split(val, ',')))));
	}
}
