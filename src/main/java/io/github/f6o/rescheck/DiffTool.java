package io.github.f6o.rescheck;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;

public class DiffTool {
	
	public static JsonNode diffJson(String a, String b) throws JsonMappingException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode tree1 = mapper.readTree(a);
		JsonNode tree2 = mapper.readTree(b);
		JsonNode patches = JsonDiff.asJson(tree1, tree2);
		return patches;
	}
	
}
	