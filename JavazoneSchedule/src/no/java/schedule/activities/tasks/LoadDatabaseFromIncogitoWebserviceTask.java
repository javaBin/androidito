package no.java.schedule.activities.tasks;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import no.java.schedule.R;
import no.java.schedule.activities.MainActivity;
import no.java.schedule.provider.SessionsContract;
import no.java.schedule.provider.parsers.SessionsParser;
import no.java.schedule.provider.parsers.SpeakerParser;
import no.java.schedule.provider.parsers.SuggestParser;
import no.java.schedule.provider.parsers.TrackParser;
import org.json.JSONException;

import java.io.IOException;

/**
 * Background task to parse local data, as provided by static JSON in
 * {@link android.content.res.AssetManager}. This task shows a progress dialog, and finishes
 * loading tabs when completed.
 */
public class LoadDatabaseFromIncogitoWebserviceTask extends AsyncTask<Void, Void, Void> {

    // Todo implement a proper service root document
    private static final String INCOGITO09_EVENTS = "http://javazone.no/incogito09/rest/events/JavaZone%202009/";
    private static final String INCOGITO09_SESSIONS = "http://javazone.no/incogito09/rest/events/JavaZone%202009/sessions";
    private static final String INCOGITO09_SPEAKERS = "http://javazone.no/incogito09/rest/events/JavaZone%202009/speakers";
    private static final String INCOGITO09_SUGGEST = "http://javazone.no/incogito09/rest/events/JavaZone%202009/suggest";
    private MainActivity context;

    public LoadDatabaseFromIncogitoWebserviceTask(MainActivity context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        context.showDialog(R.id.dialog_load);
    }

    @Override
    protected Void doInBackground(Void... params) {
        //Debug.startMethodTracing("JZSchedule-dataLoading");

        final Context context = this.context;
        final AssetManager assets = context.getAssets();

        try {

            loadTracks();
            loadSessions();
            //loadSuggest();
            //loadSpeakers();

        } catch (Exception ex) {
              Log.e(MainActivity.TAG, "Problem parsing schedules", ex);
        }

        //Debug.stopMethodTracing();
        return null;
    }

    private void loadSpeakers() throws IOException, JSONException {
        SpeakerParser speakerParser = new SpeakerParser(context.getContentResolver());
        speakerParser.parseSpeakers(Uri.parse(INCOGITO09_SPEAKERS));
    }

    private void loadSuggest() throws IOException, JSONException {
        SuggestParser suggestParser = new SuggestParser(context.getContentResolver());
        suggestParser.parseSuggest(Uri.parse(INCOGITO09_SUGGEST));
    }

    private void loadTracks() throws IOException, JSONException {
        TrackParser trackParser = new TrackParser(context.getContentResolver());
        trackParser.parseTracks(Uri.parse(INCOGITO09_EVENTS));
    }

    private void loadSessions() throws IOException, JSONException {
        SessionsParser sessionParser = new SessionsParser(context.getContentResolver());
        sessionParser.parseSessions(Uri.parse(INCOGITO09_SESSIONS));
    }

   @Override
    protected void onPostExecute(Void result) {
        context.dismissDialog(R.id.dialog_load);
        // The insert notifications are disabled to avoid the refresh of the list adapters during importing.
        // Notify content observers that the import is complete.
        context.getContentResolver().notifyChange( SessionsContract.Sessions.CONTENT_URI, null);
    }
}
