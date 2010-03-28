package no.java.schedule.provider.parsers;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class AbstractScheduleParser {
    protected static String readString(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }

        return stringBuilder.toString();
    }

    /**
     * Parse the given {@link java.io.InputStream} into a {@link org.json.JSONArray}.
     */
    protected static JSONArray parseJsonStream(InputStream is) throws IOException, JSONException {
        return new JSONArray(readString(is));
    }
}
