package no.java.schedule.provider.parsers;

import android.content.ContentResolver;
import android.net.Uri;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public abstract class AbstractScheduleParser {
    
    protected ContentResolver contentResolver;


    public AbstractScheduleParser(ContentResolver contentResolver){
       this.contentResolver = contentResolver;
    }

    public String readURI(Uri uri) throws IOException, JSONException {

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri.toString());
        httpGet.addHeader("Accept","application/json");
        HttpResponse response = httpClient.execute(httpGet);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        response.getEntity().writeTo(os);
        return os.toString();
  	}





}
