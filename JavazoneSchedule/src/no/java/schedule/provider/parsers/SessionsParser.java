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
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import no.java.schedule.provider.SessionsContract.*;
import no.java.schedule.provider.constants.SessionJsonKeys;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static no.java.schedule.util.HttpUtil.GET;

/**
 * Parser that inserts a list of sesions into the {@link Sessions#CONTENT_URI}
 * provider, assuming a JSON format. Removes all existing data.
 */
public class SessionsParser extends AbstractScheduleParser {


    /**
     * Parse the given {@link InputStream} into {@link Sessions#CONTENT_URI}
     * assuming a JSON format. Removes all existing data.
     */
    public static void parseSessions(Context context, String url) throws IOException, JSONException {

        InputStream inputStream = GET(url);

        ContentResolver resolver = context.getContentResolver();
        resolver.delete(Sessions.CONTENT_URI, null, null);

        // Parse incoming JSON stream
        JSONObject conference = new JSONObject(readString(inputStream));
        JSONArray sessions = conference.getJSONArray("sessions");

        ContentValues values = new ContentValues();
        
        // Walk across all sessions found
        int sessionCount = sessions.length();
        for (int i = 0; i < sessionCount; i++) {
            JSONObject session = sessions.getJSONObject(i);
            // Parse this session and insert
            values = parseSession(session, values);
            Uri result = resolver.insert(Sessions.CONTENT_URI, values);
        }

        inputStream.close();
	}

    private static StringBuilder sBuilder = new StringBuilder();
    
    /**
     * Parse a given session {@link JSONObject} into the given
     * {@link ContentValues} for insertion into {@link Sessions#CONTENT_URI}.
     */
    private static ContentValues parseSession(JSONObject session, ContentValues contentValues) throws JSONException {

        contentValues.clear();
        contentValues.put(TracksColumns.TRACK, session.optString(SessionJsonKeys.TRACK, null));

        contentValues.put(SessionsColumns.TITLE, session.optString(SessionJsonKeys.SESSIONTITLE, null));
        contentValues.put(SessionsColumns.ABSTRACT, session.optString(SessionJsonKeys.SESSIONABSTRACT, null));
        contentValues.put(SessionsColumns.ROOM, session.optString(SessionJsonKeys.ROOM, null));
        
        contentValues.put(SessionsColumns.TYPE, session.optString(SessionJsonKeys.SESSIONTYPE, null));

        contentValues.put(SessionsColumns.LINK, session.optString(SessionJsonKeys.FULLLINK, null));
        //contentValues.put(SessionsColumns.LINK_ALT, session.optString(SessionJsonKeys.MODERATORLINK, null));
        
        contentValues.put(SessionsColumns.STARRED, 0);


        JSONObject start = session.getJSONObject(SessionJsonKeys.START);
        JSONObject end = session.getJSONObject(SessionJsonKeys.END);
        contentValues.put(BlocksColumns.TIME_START, parseJSONDateToLong(start));
        contentValues.put(BlocksColumns.TIME_END, parseJSONDateToLong(end));


        JSONArray speakers = session.getJSONArray(SessionJsonKeys.SESSIONSPEAKERS);
        JSONArray labels = session.getJSONArray(SessionJsonKeys.TAGS);  //TODO labels

        contentValues.put(SessionsColumns.TAGS,"tags  not implemented ..");
        contentValues.put(SessionsColumns.SPEAKER_NAMES, "speakers not implemented");


        //contentValues.put(BlocksColumns.TIME_START, session.optLong(SessionJsonKeys.UNIXSTART, Long.MIN_VALUE));
             //contentValues.put(BlocksColumns.TIME_END, session.optLong(SessionJsonKeys.UNIXEND, Long.MIN_VALUE));

        // Build search index string
        sBuilder.setLength(0);
        sBuilder.append(SessionsColumns.TITLE);
        sBuilder.append(" ");
        sBuilder.append(SessionsColumns.SPEAKER_NAMES);
        sBuilder.append(" ");
        sBuilder.append(SessionsColumns.ABSTRACT);
        sBuilder.append(" ");
        sBuilder.append(SessionsColumns.TAGS);
        
        contentValues.put(SearchColumns.INDEX_TEXT, sBuilder.toString());
        
        return contentValues;
    }

    private static long parseJSONDateToLong(JSONObject jsonObject) throws JSONException {
        int day = jsonObject.getInt("day");
        int month = jsonObject.getInt("month");
        int hour = jsonObject.getInt("hour");
        int minute = jsonObject.getInt("minute");
        int second = jsonObject.getInt("second");
        int year = jsonObject.getInt("eonAndYear");

        Calendar calendar = new GregorianCalendar(year,month-1,day,hour,minute,0);  // Stupid 0 based month needs the -1
        Log.d("JavaZoneSchedule",String.format("Input %s.%s.%s %s:%s:%s",day,month,year,hour,minute,second));
        Log.d("JavaZoneSchedule","Date: "+calendar.getTime());

        return calendar.getTime().getTime();


    }

}
