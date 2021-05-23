package com.wl;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.List;

public class HttpClientUtil {

    protected CloseableHttpClient client = null;
    protected CloseableHttpResponse response = null;

    /**
     * GET请求
     * @param uri
     * @param pairList
     * @return
     * @throws Exception
     */
    public CloseableHttpResponse getExecute(String uri, List<NameValuePair> pairList) throws Exception {
        client = HttpClients.createDefault();
        HttpClientContext context = new HttpClientContext();

        URIBuilder uriBuilder = new URIBuilder(uri);
        uriBuilder.setParameters(pairList);
        HttpGet get = new HttpGet(uriBuilder.build());

        response = client.execute(get, context);

        return response;
    }

    public void close() {
        try {
            response.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

