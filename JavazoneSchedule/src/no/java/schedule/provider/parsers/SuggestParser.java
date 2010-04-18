package no.java.schedule.provider.parsers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import no.java.schedule.provider.SessionsContract;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

public class SuggestParser extends AbstractScheduleParser {
    public SuggestParser(ContentResolver contentResolver) {
        super(contentResolver);
    }

    public void parseSuggest(Uri uri) throws IOException, JSONException {
        parseSuggest(readURI(uri));
    }

    private void parseSuggest(String feedData) throws JSONException {
        contentResolver.delete(SessionsContract.SearchKeywordSuggest.CONTENT_URI, null, null);

        // Parse incoming JSON stream
        JSONArray words = new JSONArray(feedData);
        ContentValues values = new ContentValues();

        // Walk across all sessions found
        int wordCount = words.length();
        for (int i = 0; i < wordCount; i++) {
            String word = words.getString(i);

            values.clear();
            values.put(SessionsContract.SuggestColumns.DISPLAY, word);
            contentResolver.insert(SessionsContract.SearchKeywordSuggest.CONTENT_URI, values);
        }
    }
}
