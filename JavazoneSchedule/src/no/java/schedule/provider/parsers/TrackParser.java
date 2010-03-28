package no.java.schedule.provider.parsers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import no.java.schedule.provider.SessionsContract;
import no.java.schedule.provider.constants.TrackJsonKeys;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class TrackParser extends AbstractScheduleParser {
    /**
     * Parse the given {@link java.io.InputStream} into {@link no.java.schedule.provider.SessionsContract.Tracks#CONTENT_URI}
     * assuming a JSON format. Removes all existing data.
     */
    public static void parseTracks(Context context, InputStream is) throws IOException, JSONException {
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(SessionsContract.Tracks.CONTENT_URI, null, null);

        // Parse incoming JSON stream


        JSONObject conference = new JSONObject(readString(is));
        JSONArray tracks = conference.getJSONArray("labels");

        ContentValues values = new ContentValues();

        // Walk across all sessions found
        int trackCount = tracks.length();
        for (int i = 0; i < trackCount; i++) {
            JSONObject track = tracks.getJSONObject(i);

            // Parse this session and insert
            values = parseTrack(track, values);
            resolver.insert(SessionsContract.Tracks.CONTENT_URI, values);
        }
    }

    /**
     * Parse a given track {@link org.json.JSONObject} into the given
     * {@link android.content.ContentValues} for insertion into {@link no.java.schedule.provider.SessionsContract.Tracks#CONTENT_URI}.
     */
    public static ContentValues parseTrack(JSONObject track, ContentValues contentValues) {
        contentValues.clear();
        contentValues.put(SessionsContract.TracksColumns.TRACK, track.optString(TrackJsonKeys.DISPLAYNAME, null));
        //int color = Integer.parseInt(track.optString(TrackJsonKeys.COLOR, null), 16);
        //contentValues.put(TracksColumns.COLOR, color);
        contentValues.put(SessionsContract.TracksColumns.VISIBLE, 1);
        return contentValues;
    }
}
