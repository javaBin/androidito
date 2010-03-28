package no.java.schedule.provider.parsers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import no.java.schedule.provider.SessionsContract;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;

public class SuggestParser extends AbstractScheduleParser {
    /**
     * Parse the given {@link java.io.InputStream} into {@link no.java.schedule.provider.SessionsContract.Suggest#CONTENT_URI}
     * assuming a JSON format. Removes all existing data.
     */


    public static void parseSuggest(Context context, InputStream is) throws IOException, JSONException {
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(SessionsContract.Suggest.CONTENT_URI, null, null);

        // Parse incoming JSON stream
        JSONArray words = parseJsonStream(is);
        ContentValues values = new ContentValues();

        // Walk across all sessions found
        int wordCount = words.length();
        for (int i = 0; i < wordCount; i++) {
            String word = words.getString(i);

            values.clear();
            values.put(SessionsContract.SuggestColumns.DISPLAY1, word);
            resolver.insert(SessionsContract.Suggest.CONTENT_URI, values);
        }
    }
}
