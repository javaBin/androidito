package no.java.schedule.provider.parsers;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.net.Uri;
import no.java.schedule.activities.tasks.LoadDatabaseFromIncogitoWebserviceTask;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public abstract class AbstractScheduleParser {

    protected abstract String downloadingMessage();
    protected abstract String nochangesMessage();

    
    protected ContentResolver contentResolver;
    private SharedPreferences hashStore;
    protected LoadDatabaseFromIncogitoWebserviceTask task;


    public void parse(Uri uri) throws JSONException, IOException {
        task.progress(downloadingMessage());
        final String content = readURI(uri);
        //if (content.hashCode()!=lastChecksum(uri)){
            parse(content);
        //} else {
        //    task.progress(nochangesMessage());
        //}
    }

    protected abstract void parse(String content) throws JSONException;

    public AbstractScheduleParser(ContentResolver contentResolver,LoadDatabaseFromIncogitoWebserviceTask task,SharedPreferences hashStore){
       this.contentResolver = contentResolver;
       this.hashStore = hashStore;
       this.task = task;
    }

    protected String readURI(Uri uri) throws IOException, JSONException {

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri.toString());
        httpGet.addHeader("Accept","application/json");
        HttpResponse response = httpClient.execute(httpGet);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        response.getEntity().writeTo(os);
        final String content = os.toString();
        storeHash(uri,content.hashCode());
        return content;
  	}

    protected void storeHash(Uri uri, int checksum) {
      SharedPreferences.Editor editor = hashStore.edit();
      editor.putString(uri.toString(), Integer.toString(checksum));
      editor.commit();
    }

    protected int lastChecksum(Uri uri){
        final String storedHash = hashStore.getString(uri.toString(),"0");
        return Integer.parseInt(storedHash);
    }


}
