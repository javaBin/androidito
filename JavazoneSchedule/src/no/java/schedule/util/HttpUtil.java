package no.java.schedule.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;

public class HttpUtil {

public static InputStream GET( final String url) throws IOException {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("Accept","application/json");
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            return entity.getContent();
        }

}
