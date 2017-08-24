package com.gt22.pbbot.utils;


import com.github.davidmoten.rx2.util.Pair;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JavaHttpRequestBuilder {
	private final URL url;
	private final List<Pair<String, String>> postParams = new ArrayList<>();
	public JavaHttpRequestBuilder(String url) throws MalformedURLException {
		this.url = new URL(url);
		if(!this.url.getProtocol().startsWith("http")) {
			throw new MalformedURLException("Url must be http or https");
		}
	}

	public JavaHttpRequestBuilder addPostParam(String name, String val) {
		postParams.add(new Pair<>(name, val));
		return this;
	}

	public HttpURLConnection build() throws IOException {
		HttpURLConnection ret = (HttpURLConnection) url.openConnection();

		if(!postParams.isEmpty()) {
			byte[] postData = postParams.stream().map(p -> p.left() + '=' + p.right()).reduce((s1, s2) -> s1 + '&' + s2).map(s -> s.getBytes(StandardCharsets.UTF_8)).orElse(new byte[]{});
			ret.setDoOutput(true);
			ret.setRequestMethod("POST");
			ret.setRequestProperty("Content-Encoding", "UTF-8");
			ret.setRequestProperty("charset", "utf-8");
			ret.setRequestProperty("Content-Length", Integer.toString(postData.length));
			ret.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			ret.setUseCaches(false);
			try(OutputStream w = ret.getOutputStream()) {
				w.write(postData);
			}
		}
		return ret;
	}

}
