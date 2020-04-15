package io.github.f6o.rescheck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;

public abstract class BaseDo {
	protected String id;
	protected List<String> headers = new ArrayList<>();
	protected String body;
	
	protected String hash;
	public String getHash() {
		if ( hash == null ) {
			setHash();
		}
		return hash;
	}
	abstract protected void setHash();
	
	protected static String calcHash(Object...objects) {
		String baseStr = Arrays.stream(objects).map(Object::toString).collect(Collectors.joining(";"));
		return DigestUtils.sha1Hex(baseStr);
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<String> getHeaders() {
		return headers;
	}
	public void addHeader(String headerLine) {
		headers.add(headerLine);
	}
	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
}
