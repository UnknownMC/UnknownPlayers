package com.mojang.api.profiles;

import com.google.gson.Gson;
import com.mojang.api.http.BasicHttpClient;
import com.mojang.api.http.HttpBody;
import com.mojang.api.http.HttpClient;
import com.mojang.api.http.HttpHeader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HttpProfileRepository implements ProfileRepository {

    private static final int MAX_QUERIES_PER_REQUEST = 100;
    private static Gson gson = new Gson();
    private HttpClient client;
    private final String agent;

    public HttpProfileRepository(String agent) {
        this(agent, BasicHttpClient.getInstance());
    }

    public HttpProfileRepository(String agent, HttpClient client) {
        this.agent = agent;
        this.client = client;
    }

    @Override
    public Profile[] findProfilesByCriteria(String... names) {
    	List<Profile> profs = new ArrayList<Profile>();
        try {
            List<HttpHeader> headers = new ArrayList<HttpHeader>();
            headers.add(new HttpHeader("Content-Type", "application/json"));
            int count = names.length;
            int start = 0;
            int i = 0;
            while (start < count) {
            	int end = MAX_QUERIES_PER_REQUEST * (i+1);
            	if (end > count) {
            		end = count;
            	}
            	String[] batch = Arrays.copyOfRange(names, start, end);
            	HttpBody body = getHttpBody(batch);
            	Profile[] result = post(getProfilesUrl(), body, headers);
            	profs.addAll(Arrays.asList(result));
            	start = end;
            	i++;
            }
        } catch (Exception e) {
            // TODO: logging and allowing consumer to react?
        }
        return profs.toArray(new Profile[profs.size()]);
    }

    private URL getProfilesUrl() throws MalformedURLException {
    	return new URL("https://api.mojang.com/profiles/" + agent);
    }
    
    private Profile[] post(URL url, HttpBody body, List<HttpHeader> headers) throws IOException {
    	String response = client.post(url, body, headers);
    	return gson.fromJson(response, Profile[].class);
    }
    
    private static HttpBody getHttpBody(String... batch) {
    	return new HttpBody(gson.toJson(batch));
    }

}
