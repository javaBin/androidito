package no.java.schedule.provider.parsers;

import android.content.ContentResolver;
import android.net.Uri;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static no.java.schedule.util.HttpUtil.GET;

public abstract class AbstractScheduleParser {
    
    protected ContentResolver contentResolver;


    public AbstractScheduleParser(ContentResolver contentResolver){
       this.contentResolver = contentResolver;
    }
    public String readURI(Uri uri) throws IOException, JSONException {
        InputStream inputStream = GET(uri);
        final String feedData = readString(inputStream);
        inputStream.close();
        return feedData;
  	}



    protected String readString(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }

        return stringBuilder.toString();
    }

}
