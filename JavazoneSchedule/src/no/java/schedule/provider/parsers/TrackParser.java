package no.java.schedule.provider.parsers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import no.java.schedule.provider.SessionsContract;
import no.java.schedule.provider.constants.TrackJsonKeys;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class TrackParser extends AbstractScheduleParser {

    public TrackParser(ContentResolver contentResolver) {
        super(contentResolver);
    }


    private void parseTrack(String feedData) throws JSONException {
        contentResolver.delete(SessionsContract.Tracks.CONTENT_URI, null, null);

        // Parse incoming JSON stream


        JSONObject conference = new JSONObject(feedData);
        JSONArray tracks = conference.getJSONArray("labels");

        ContentValues values = new ContentValues();

        // Walk across all sessions found
        int trackCount = tracks.length();
        for (int i = 0; i < trackCount; i++) {
            JSONObject track = tracks.getJSONObject(i);

            // Parse this session and insert
            values = parseTrack(track, values);
            contentResolver.insert(SessionsContract.Tracks.CONTENT_URI, values);
        }
    }

    /**
     * Parse a given track {@link org.json.JSONObject} into the given
     * {@link android.content.ContentValues} for insertion into {@link no.java.schedule.provider.SessionsContract.Tracks#CONTENT_URI}.
     */
    public ContentValues parseTrack(JSONObject track, ContentValues contentValues) {
        contentValues.clear();
        contentValues.put(SessionsContract.TracksColumns.TRACK, track.optString(TrackJsonKeys.DISPLAYNAME, null));
        //int color = Integer.parseInt(track.optString(TrackJsonKeys.COLOR, null), 16);
        //contentValues.put(TracksColumns.COLOR, color);
        contentValues.put(SessionsContract.TracksColumns.VISIBLE, 1);
        return contentValues;
    }

    public void parseTracks(Uri uri) throws JSONException, IOException {
        parseTrack(readURI(uri));;
    }
}
