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
import no.java.schedule.provider.SessionsContract.*;
import no.java.schedule.provider.constants.SessionJsonKeys;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Parser that inserts a list of sesions into the {@link Sessions#CONTENT_URI}
 * provider, assuming a JSON format. Removes all existing data.
 */
public class SessionsParser extends AbstractScheduleParser {


    /**
     * Parse the given {@link InputStream} into {@link Sessions#CONTENT_URI}
     * assuming a JSON format. Removes all existing data.
     */
    public static void parseSessions(Context context, InputStream is) throws IOException, JSONException {
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(Sessions.CONTENT_URI, null, null);

        // Parse incoming JSON stream
        JSONArray sessions = parseJsonStream(is);
        ContentValues values = new ContentValues();
        
        // Walk across all sessions found
        int sessionCount = sessions.length();
        for (int i = 0; i < sessionCount; i++) {
            JSONObject session = sessions.getJSONObject(i);
            
            // Parse this session and insert
            values = parseSession(session, values);
            resolver.insert(Sessions.CONTENT_URI, values);
        }
	}

    private static StringBuilder sBuilder = new StringBuilder();
    
    /**
     * Parse a given session {@link JSONObject} into the given
     * {@link ContentValues} for insertion into {@link Sessions#CONTENT_URI}.
     */
    private static ContentValues parseSession(JSONObject session, ContentValues recycle) {
        recycle.clear();
        recycle.put(TracksColumns.TRACK, session.optString(SessionJsonKeys.TRACK, null));
        
        recycle.put(BlocksColumns.TIME_START, session.optLong(SessionJsonKeys.UNIXSTART, Long.MIN_VALUE));
        recycle.put(BlocksColumns.TIME_END, session.optLong(SessionJsonKeys.UNIXEND, Long.MIN_VALUE));
        
        final String sessiontitle = session.optString(SessionJsonKeys.SESSIONTITLE, null);
        final String sessionspeakers = session.optString(SessionJsonKeys.SESSIONSPEAKERS, null);
        final String sessionabstract = session.optString(SessionJsonKeys.SESSIONABSTRACT, null);
        final String tags = session.optString(SessionJsonKeys.TAGS, null);
        
        recycle.put(SessionsColumns.TITLE, sessiontitle);
        recycle.put(SessionsColumns.SPEAKER_NAMES, sessionspeakers);
        recycle.put(SessionsColumns.ABSTRACT, sessionabstract);
        recycle.put(SessionsColumns.ROOM, session.optString(SessionJsonKeys.ROOM, null));
        
        recycle.put(SessionsColumns.TYPE, session.optString(SessionJsonKeys.SESSIONTYPE, null));
        recycle.put(SessionsColumns.TAGS, tags);
        
        recycle.put(SessionsColumns.LINK, session.optString(SessionJsonKeys.FULLLINK, null));
        recycle.put(SessionsColumns.LINK_ALT, session.optString(SessionJsonKeys.MODERATORLINK, null));
        
        recycle.put(SessionsColumns.STARRED, 0);
        
        // Build search index string
        sBuilder.setLength(0);
        sBuilder.append(sessiontitle);
        sBuilder.append(" ");
        sBuilder.append(sessionspeakers);
        sBuilder.append(" ");
        sBuilder.append(sessionabstract);
        sBuilder.append(" ");
        sBuilder.append(tags);
        
        recycle.put(SearchColumns.INDEX_TEXT, sBuilder.toString());
        
        return recycle;
    }

}
