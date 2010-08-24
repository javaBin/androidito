package no.java.schedule.provider.parsers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import no.java.schedule.activities.tasks.LoadDatabaseFromIncogitoWebserviceTask;
import no.java.schedule.provider.SessionsContract;
import no.java.schedule.provider.constants.SpeakerJsonKeys;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SpeakerParser extends AbstractScheduleParser {
    
    public SpeakerParser(ContentResolver contentResolver, LoadDatabaseFromIncogitoWebserviceTask task, SharedPreferences hashStore) {
        super(contentResolver, task, hashStore);
    }


    protected void parse(String feedData) throws JSONException {
        contentResolver.delete(SessionsContract.Speakers.CONTENT_URI, null, null);

        // Parse incoming JSON stream
        JSONArray speakers = new JSONArray(feedData);
        List<ContentValues> entries = new ArrayList<ContentValues>(speakers.length());

        for (int i = 0; i < speakers.length(); i++) {
            JSONObject speaker = speakers.getJSONObject(i);
            entries.add(parseSpeaker(speaker));
        }

        contentResolver.bulkInsert(SessionsContract.Speakers.CONTENT_URI, entries.toArray(new ContentValues[entries.size()]));
    }

    /**
     * Parse a given speaker (@link JSONObject} into the given
     * {@link android.content.ContentValues} for insertion into {@link no.java.schedule.provider.SessionsContract.Speakers#CONTENT_URI}
     */
    public static ContentValues parseSpeaker(JSONObject speaker) {
    	ContentValues values = new ContentValues();
    	values.put(SessionsContract.SpeakersColumns.SPEAKERNAME, speaker.optString(SpeakerJsonKeys.SPEAKERNAME, null));
    	values.put(SessionsContract.SpeakersColumns.SPEAKERBIO, speaker.optString(SpeakerJsonKeys.SPEAKERBIO, null));
    	return values;
    }

    @Override
    protected String downloadingMessage() {
        return "Fetching speakers.";
    }

    @Override
    protected String nochangesMessage() {
        return "No changes to speakers.";
    }
}
