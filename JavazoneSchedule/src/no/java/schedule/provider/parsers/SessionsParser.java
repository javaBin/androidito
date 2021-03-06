/*
 * Copyright (C) 2009 Virgil Dobjanschi, Jeff Sharkey, Filip Maelbrancke
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.java.schedule.provider.parsers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import no.java.schedule.activities.tasks.LoadDatabaseFromIncogitoWebserviceTask;
import no.java.schedule.provider.SessionsContract;
import no.java.schedule.provider.SessionsContract.*;
import no.java.schedule.provider.constants.SessionJsonKeys;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Parser that inserts a list of sesions into the {@link Sessions#CONTENT_URI}
 * provider, assuming a JSON format. Removes all existing data.
 */
public class SessionsParser extends AbstractScheduleParser {


    private GregorianCalendar calendar = new GregorianCalendar();

    public SessionsParser(ContentResolver contentResolver, LoadDatabaseFromIncogitoWebserviceTask task, SharedPreferences hashStore) {
        super(contentResolver, task, hashStore);
    }


    @Override
    protected String downloadingMessage() {
       return "Fetching sessions.";
    }

    @Override
    protected String nochangesMessage() {
        return "No changes to sessions.";
    }

    protected void parse(String feedData) throws JSONException {
        //contentResolver.delete(Sessions.CONTENT_URI, null, null);
        contentResolver.delete(Blocks.CONTENT_URI,null,null);

        JSONObject conference = new JSONObject(feedData);
        JSONArray sessions = conference.getJSONArray("sessions");

        task.progress("Parsing sessions");

        List<ContentValues> entries = new ArrayList<ContentValues>(sessions.length());

        for (int i = 0; i < sessions.length(); i++) {
            JSONObject session = sessions.getJSONObject(i);
            entries.add(parseSession(session));
        }

        contentResolver.bulkInsert(Sessions.CONTENT_URI,entries.toArray(new ContentValues[0]));
    }


    /**
     * Parse a given session {@link org.json.JSONObject} into the given
     * {@link android.content.ContentValues} for insertion into {@link no.java.schedule.provider.SessionsContract.Sessions#CONTENT_URI}.
     */
    private ContentValues parseSession(JSONObject session) throws JSONException {

        ContentValues contentValues = new ContentValues();


        contentValues.put(SessionsColumns.TITLE, session.optString(SessionJsonKeys.SESSIONTITLE, null));
        contentValues.put(SessionsColumns.ABSTRACT, session.optString(SessionJsonKeys.SESSIONABSTRACT, null));
        contentValues.put(SessionsColumns.ROOM, session.optString(SessionJsonKeys.ROOM, null));
        
        contentValues.put(SessionsColumns.TYPE,session.optString(SessionJsonKeys.SESSIONTYPE, SessionsContract.TYPE_PRESENTATION));

        contentValues.put(SessionsColumns.WEB_LINK, session.optString(SessionJsonKeys.FULLLINK, null));
        //contentValues.put(SessionsColumns.LINK_ALT, session.optString(SessionJsonKeys.MODERATORLINK, null));
        
        contentValues.put(SessionsColumns.STARRED, 0);

        JSONObject start = session.getJSONObject(SessionJsonKeys.START);
        JSONObject end = session.getJSONObject(SessionJsonKeys.END);
        contentValues.put(BlocksColumns.TIME_START, parseJSONDateToLong(start));
        contentValues.put(BlocksColumns.TIME_END, parseJSONDateToLong(end));

        final JSONArray trackJson = session.getJSONArray("labels");
        if (trackJson.length()>0){
            contentValues.put(TracksColumns.TRACK, trackJson.getJSONObject(0).optString("displayName","Error..."));
        }

        //TODO level indication

        String speakernames = "";

        JSONArray speakers = session.getJSONArray(SessionJsonKeys.SESSIONSPEAKERS);
        for (int i = 0; i < speakers.length(); i++){
            JSONObject speaker = speakers.getJSONObject(i);
            speakernames+= (i>0 ? ", " : "")+ speaker.get(SessionJsonKeys.SPEAKER_NAME);
        }

        contentValues.put(SessionsColumns.SPEAKER_NAMES, speakernames); 


        JSONArray labels = session.getJSONArray(SessionJsonKeys.TAGS);  //TODO labels
        contentValues.put(SessionsColumns.TAGS,"tags  not implemented ..");


        // Build search index string
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(contentValues.get(SessionsColumns.TITLE));
        stringBuilder.append(" ");
        stringBuilder.append(contentValues.get(SessionsColumns.SPEAKER_NAMES));
        stringBuilder.append(" ");
        stringBuilder.append(contentValues.get(SessionsColumns.ABSTRACT));
        stringBuilder.append(" ");
        stringBuilder.append(contentValues.get(SessionsColumns.TAGS));

        contentValues.put(SearchColumns.INDEX_TEXT, stringBuilder.toString());
        
        return contentValues;
    }

    private long parseJSONDateToLong(JSONObject jsonObject) throws JSONException {
        int day = jsonObject.getInt("day");
        int month = jsonObject.getInt("month");
        int hour = jsonObject.getInt("hour");
        int minute = jsonObject.getInt("minute");
        int second = jsonObject.getInt("second");
        int year = jsonObject.getInt("eonAndYear");

        calendar = new GregorianCalendar(year,month-1,day,hour,minute,0);  // Stupid 0 based month needs the -1
       /*
        calendar.set(GregorianCalendar.YEAR,year);
        calendar.set(GregorianCalendar.MONTH,month-1);
        calendar.set(GregorianCalendar.DAY_OF_MONTH,day);
        calendar.set(GregorianCalendar.HOUR_OF_DAY,hour);
        calendar.set(GregorianCalendar.MINUTE,minute);
        calendar.set(GregorianCalendar.SECOND,0);
         */
        
        //Log.d("JavaZoneSchedule",String.format("Input %s.%s.%s %s:%s:%s",day,month,year,hour,minute,second));
        //Log.d("JavaZoneSchedule","Date: "+calendar.getTime());

        return calendar.getTimeInMillis();


    }

}
