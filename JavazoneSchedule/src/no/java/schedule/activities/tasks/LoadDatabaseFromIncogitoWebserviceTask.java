package no.java.schedule.activities.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Debug;
import android.util.Log;
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
public class LoadDatabaseFromIncogitoWebserviceTask extends AsyncTask<Void, Progress, Void> {

    // Todo implement a proper service root document
    private static final String INCOGITO09_EVENTS = "http://javazone.no/incogito09/rest/events/JavaZone%202009/";
    private static final String INCOGITO09_SESSIONS = "http://javazone.no/incogito09/rest/events/JavaZone%202009/sessions";
    private static final String INCOGITO09_SPEAKERS = "http://javazone.no/incogito09/rest/events/JavaZone%202009/speakers";
    private static final String INCOGITO09_SUGGEST = "http://javazone.no/incogito09/rest/events/JavaZone%202009/suggest";
    private MainActivity context;
    private android.app.ProgressDialog progressDialog;

    public LoadDatabaseFromIncogitoWebserviceTask(MainActivity context) {
        this.context = context;

    }

    @Override
    protected void onPreExecute() {
        progressDialog = context.buildLoadingDialog();
        progressDialog.show();

        Debug.startMethodTracing("JZSchedule-LoadDatabase-"+System.currentTimeMillis());

    }

    @Override
    protected Void doInBackground(Void... params) {

        publishProgress(new Progress("Starting parsing",0,0));

        try {
            loadTracks(this);
            loadSessions(this);
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

    private void loadTracks(LoadDatabaseFromIncogitoWebserviceTask task) throws IOException, JSONException {
        TrackParser trackParser = new TrackParser(context.getContentResolver(),task);
        trackParser.parseTracks(Uri.parse(INCOGITO09_EVENTS));
    }

    private void loadSessions(LoadDatabaseFromIncogitoWebserviceTask task) throws IOException, JSONException {
        SessionsParser sessionParser = new SessionsParser(context.getContentResolver(),task);
        sessionParser.parseSessions(Uri.parse(INCOGITO09_SESSIONS));
    }

    @Override
    protected void onPostExecute(Void result) {
        progressDialog.dismiss();
        // The insert notifications are disabled to avoid the refresh of the list adapters during importing.
        // Notify content observers that the import is complete.
        context.getContentResolver().notifyChange( SessionsContract.Sessions.CONTENT_URI, null);
        Debug.startMethodTracing("JZSchedule-LoadDatabase-"+System.currentTimeMillis());
    }

    @Override
    protected void onProgressUpdate(Progress... values) {
        Progress progress = values[0];
        progressDialog.setMessage(progress.getMessage());
        progressDialog.setProgress(progress.getMainProgress());
        progressDialog.setSecondaryProgress(progress.getSubProgress());
    }

    public void progress(String message, int progress, int subProgress) {

        // Use secondary progress as the main for now
        //publishProgress(new Progress(message,progress,subProgress));
        publishProgress(new Progress(message,subProgress,subProgress));

    }
}
