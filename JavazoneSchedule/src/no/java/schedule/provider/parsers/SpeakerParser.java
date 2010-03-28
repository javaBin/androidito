package no.java.schedule.provider.parsers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import no.java.schedule.provider.SessionsContract;
import no.java.schedule.provider.constants.SpeakerJsonKeys;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class SpeakerParser extends AbstractScheduleParser {
    
    /**
     * Parse the given {@link java.io.InputStream} into {@link no.java.schedule.provider.SessionsContract.Speakers#CONTENT_URI}
     * assuming a JSON format. Removes all existing data.
     */
    public static void parseSpeakers(Context context, InputStream is) throws IOException, JSONException {
    	ContentResolver resolver = context.getContentResolver();
    	resolver.delete(SessionsContract.Speakers.CONTENT_URI, null, null);

    	// Parse incoming JSON stream
    	JSONArray speakers = AbstractScheduleParser.parseJsonStream(is);
    	ContentValues values = new ContentValues();

    	// Walk across all speakers found
    	int speakersCount = speakers.length();
    	for (int i = 0; i < speakersCount; i++) {
    		JSONObject speaker = speakers.getJSONObject(i);

    		// Parse this speaker and insert
    		values = parseSpeaker(speaker, values);
    		resolver.insert(SessionsContract.Speakers.CONTENT_URI, values);
    	}
    }

    /**
     * Parse a given speaker (@link JSONObject} into the given
     * {@link android.content.ContentValues} for insertion into {@link no.java.schedule.provider.SessionsContract.Speakers#CONTENT_URI}
     */
    public static ContentValues parseSpeaker(JSONObject speaker, ContentValues recycle) {
    	recycle.clear();
    	recycle.put(SessionsContract.SpeakersColumns.SPEAKERNAME, speaker.optString(SpeakerJsonKeys.SPEAKERNAME, null));
    	recycle.put(SessionsContract.SpeakersColumns.SPEAKERBIO, speaker.optString(SpeakerJsonKeys.SPEAKERBIO, null));
    	return recycle;
    }
}
