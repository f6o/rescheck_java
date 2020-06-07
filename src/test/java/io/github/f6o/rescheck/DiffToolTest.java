package io.github.f6o.rescheck;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;
import com.fasterxml.jackson.databind.JsonNode;

public class DiffToolTest {
	
	private static String readFile(String filePath) throws FileNotFoundException, IOException {
		try ( BufferedReader br = new BufferedReader(new FileReader(filePath)) ) {
			StringBuffer sb = new StringBuffer();
			String line = null;
			while ( ( line = br.readLine()) != null ) {
				sb.append(line);
			}
			return sb.toString();
		}
	}
	
	@Test
	public void test() throws IOException {
		String json1 = readFile("./src/test/resources/user.json");
		String json2 = readFile("./src/test/resources/user2.json");
		JsonNode diffs = DiffTool.diffJson(json1, json2);
		for ( JsonNode diff : diffs ) {
			System.err.println(diff);
		}
	}
}