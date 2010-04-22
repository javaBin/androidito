package no.java.schedule.provider.parsers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import no.java.schedule.provider.SessionsContract;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SuggestParser extends AbstractScheduleParser {
    public SuggestParser(ContentResolver contentResolver) {
        super(contentResolver);
    }

    public void parseSuggest(Uri uri) throws IOException, JSONException {
        parseSuggest(readURI(uri));
    }

    private void parseSuggest(String feedData) throws JSONException {

        //TODO - really needs to be redone, this can be inserted as part of the session/speaker parsing as far as I can see
        contentResolver.delete(SessionsContract.SearchKeywordSuggest.CONTENT_URI, null, null);

        // Parse incoming JSON stream
        JSONArray words = new JSONArray(feedData);

        List<ContentValues> entries = new ArrayList<ContentValues>();

        int wordCount = words.length();
        for (int i = 0; i < wordCount; i++) {

            String word = words.getString(i);
            ContentValues values = new ContentValues();
            values.put(SessionsContract.SuggestColumns.DISPLAY, word);
            contentResolver.insert(SessionsContract.SearchKeywordSuggest.CONTENT_URI, values);
        }
    }
}
