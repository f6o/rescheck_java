package io.github.f6o.rescheck;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;

public class DiffTool {
	
	public static JsonNode diffJson(String a, String b) throws JsonMappingException, JsonProcessingException {
		JsonNode tree1 = parseJson(a);
		JsonNode tree2 = parseJson(b);
		JsonNode patches = JsonDiff.asJson(tree1, tree2);
		return patches;
	}
	
	public static JsonNode parseJson(String json) throws JsonMappingException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode n = mapper.readTree(json);
		return n;
	}
	
}