package no.java.schedule.provider.parsers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.net.Uri;
import no.java.schedule.activities.tasks.LoadDatabaseFromIncogitoWebserviceTask;
import no.java.schedule.provider.SessionsContract;
import no.java.schedule.provider.constants.TrackJsonKeys;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrackParser extends AbstractScheduleParser {
    private int[] colors;
    private int nextColorIndex =0;

    public TrackParser(ContentResolver contentResolver, LoadDatabaseFromIncogitoWebserviceTask task, SharedPreferences hashStore) {
        super(contentResolver, task, hashStore);
    }


    protected void parse(String feedData) throws JSONException {
        task.progress("Deleting old tracks from database");

        contentResolver.delete(SessionsContract.Tracks.CONTENT_URI, null, null);

        // Parse incoming JSON stream


        JSONObject conference = new JSONObject(feedData);
        JSONArray tracks = conference.getJSONArray("labels");

        task.progress("Parsing tracks");

        List<ContentValues> entries = new ArrayList <ContentValues>(tracks.length());

        createColorCodes(tracks.length());

        for (int i = 0; i < tracks.length(); i++) {
            JSONObject track = tracks.getJSONObject(i);
            entries.add(parse(track));
        }

        contentResolver.bulkInsert(SessionsContract.Tracks.CONTENT_URI, entries.toArray(new ContentValues[entries.size()]));
    }

    void createColorCodes(int trackCount) {

        if (trackCount>0){
            colors = new int[trackCount];
            float angle = 0;
            float increment = 360/trackCount;
            int startColor = Color.BLUE;


            for (int i = 0; i < colors.length; i++) {
                colors[i] = rotateColor(startColor,angle);
                angle+=increment;
            }
        }

    }

    /**
     * Parse a given track {@link org.json.JSONObject} into the given
     * {@link android.content.ContentValues} for insertion into {@link no.java.schedule.provider.SessionsContract.Tracks#CONTENT_URI}.
     */
    public ContentValues parse(JSONObject track) {
        ContentValues contentValues = new ContentValues();
        final String title = track.optString(TrackJsonKeys.DISPLAYNAME, null);
        contentValues.put(SessionsContract.TracksColumns.TRACK, title);

        //int color = Integer.parseInt(track.optString(TrackJsonKeys.COLOR, null), 16);
        int color = findColorFromTitle(title);
        contentValues.put(SessionsContract.TracksColumns.COLOR, color);
        contentValues.put(SessionsContract.TracksColumns.VISIBLE, 1);
        return contentValues;
    }

    private int findColorFromTitle(String title) {
        return colors[nextColorIndex++];
    }


    private int rotateColor(int color, float deg) {
        // Based on the com.example.android.apis.graphics.ColorPickerDialog of android api demos

        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        ColorMatrix cm = new ColorMatrix();
        ColorMatrix tmp = new ColorMatrix();

        cm.setRGB2YUV();
        tmp.setRotate(0, deg);
        cm.postConcat(tmp);
        tmp.setYUV2RGB();
        cm.postConcat(tmp);

        final float[] a = cm.getArray();

        int ir = Math.round(a[0] * r +  a[1] * g +  a[2] * b);
        int ig = Math.round(a[5] * r +  a[6] * g +  a[7] * b);
        int ib = Math.round(a[10] * r + a[11] * g + a[12] * b);

        return Color.argb(Color.alpha(color), pinToByte(ir),pinToByte(ig), pinToByte(ib));
    }

    private int pinToByte(int n) {
        if (n < 0) {
            n = 0;
        } else if (n > 255) {
            n = 255;
        }
        return n;
    }

    public void parseTracks(Uri uri) throws JSONException, IOException {
        task.progress("Downloading tracks feed");

        parse(readURI(uri));
    }

    @Override
    protected String downloadingMessage() {
        return "Fetching tracks.";
    }

    @Override
    protected String nochangesMessage() {
        return "No changes to tracks.";
    }
}
