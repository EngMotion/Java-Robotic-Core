package com.lucaf.robotic_core.dataInterfaces.tcp;

import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.dataInterfaces.impl.RoutableInterface;
import lombok.Getter;
import lombok.Setter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Base64;
import java.util.HashMap;

public class HttpJsonConnector extends RoutableInterface {

    static MediaType JSON = MediaType.get("application/json");
    static OkHttpClient client = new OkHttpClient.Builder()
            .callTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    HashMap<String, String> headers = new HashMap<>();

    String host = "http://localhost:8080";

    @Getter
    @Setter
    int masterID = 1;

    public HttpJsonConnector(String name, Logger logger) {
        super(name, logger);
    }

    public void setHost(String host, int port) {
        this.host = "http://" + host + ":" + port;
    }

    public void setSecureHost(String host, int port) {
        this.host = "https://" + host + ":" + port;
    }

    public void addBasicAuth(String username, String password) {
        String auth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        headers.put("Authorization", "Basic " + auth);
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    @Override
    public boolean isConnected() {
        try {
            InetAddress address = InetAddress.getByName(host.replace("http://", "").replace("https://", "").split(":")[0]);
            return address.isReachable(2000);
        } catch (IOException e) {
            return false;
        }
    }

    Request.Builder getBaseRequestBuilder(String address) {
        Request.Builder builder = new Request.Builder()
                .url(host + address);
        for (String key : headers.keySet()) {
            builder.addHeader(key, headers.get(key));
        }
        return builder;
    }

    @Override
    public byte[] get(String address) throws IOException {
        Request.Builder builder = getBaseRequestBuilder(address)
                .get();
        Request request = builder.build();
        try (okhttp3.Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logError("GET request to " + address + " failed with code " + response.code());
                return new byte[0];
            }
            return response.body().bytes();
        }
    }

    @Override
    public byte[] post(String address, byte[] data, MediaType mediaType) throws IOException {
        Request.Builder builder = getBaseRequestBuilder(address)
                .post(okhttp3.RequestBody.create(data, mediaType));
        Request request = builder.build();
        try (okhttp3.Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logError("POST request to " + address + " failed with code " + response.code());
                return new byte[0];
            }
            return response.body().bytes();
        }
    }

    @Override
    public byte[] put(String address, byte[] data, MediaType mediaType) throws IOException {
        Request.Builder builder = getBaseRequestBuilder(address)
                .put(okhttp3.RequestBody.create(data, mediaType));
        Request request = builder.build();
        try (okhttp3.Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logError("PUT request to " + address + " failed with code " + response.code());
                return new byte[0];
            }
            return response.body().bytes();
        }
    }

    @Override
    public byte[] delete(String address) throws IOException {
        Request.Builder builder = getBaseRequestBuilder(address)
                .delete();
        Request request = builder.build();
        try (okhttp3.Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logError("DELETE request to " + address + " failed with code " + response.code());
                return new byte[0];
            }
            return response.body().bytes();
        }
    }
}
