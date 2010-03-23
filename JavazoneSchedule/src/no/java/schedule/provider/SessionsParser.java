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

package no.java.schedule.provider;

import no.java.schedule.provider.SessionsContract.BlocksColumns;
import no.java.schedule.provider.SessionsContract.SearchColumns;
import no.java.schedule.provider.SessionsContract.Sessions;
import no.java.schedule.provider.SessionsContract.SessionsColumns;
import no.java.schedule.provider.SessionsContract.Speakers;
import no.java.schedule.provider.SessionsContract.SpeakersColumns;
import no.java.schedule.provider.SessionsContract.Suggest;
import no.java.schedule.provider.SessionsContract.SuggestColumns;
import no.java.schedule.provider.SessionsContract.Tracks;
import no.java.schedule.provider.SessionsContract.TracksColumns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

/**
 * Parser that inserts a list of sesions into the {@link Sessions#CONTENT_URI}
 * provider, assuming a JSON format. Removes all existing data.
 */
public class SessionsParser {
    
    /**
     * Parse the given {@link InputStream} into a {@link JSONArray}.
     */
    private static JSONArray parseJsonStream(InputStream is) throws IOException, JSONException {
        byte[] buffer = new byte[is.available()];
        is.read(buffer);
        return new JSONArray(new String(buffer));
    }

    /**
     * Parse the given {@link InputStream} into {@link Tracks#CONTENT_URI}
     * assuming a JSON format. Removes all existing data.
     */
    public static void parseTracks(Context context, InputStream is) throws IOException, JSONException {
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(Tracks.CONTENT_URI, null, null);

        // Parse incoming JSON stream
        JSONArray tracks = parseJsonStream(is);
        ContentValues values = new ContentValues();
        
        // Walk across all sessions found
        int trackCount = tracks.length();
        for (int i = 0; i < trackCount; i++) {
            JSONObject track = tracks.getJSONObject(i);
            
            // Parse this session and insert
            values = parseTrack(track, values);
            resolver.insert(Tracks.CONTENT_URI, values);
        }
    }

    /**
     * Parse the given {@link InputStream} into {@link Suggest#CONTENT_URI}
     * assuming a JSON format. Removes all existing data.
     */
    public static void parseSuggest(Context context, InputStream is) throws IOException, JSONException {
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(Suggest.CONTENT_URI, null, null);

        // Parse incoming JSON stream
        JSONArray words = parseJsonStream(is);
        ContentValues values = new ContentValues();
        
        // Walk across all sessions found
        int wordCount = words.length();
        for (int i = 0; i < wordCount; i++) {
            String word = words.getString(i);
            
            values.clear();
            values.put(SuggestColumns.DISPLAY1, word);
            resolver.insert(Suggest.CONTENT_URI, values);
        }
    }
    
    /**
     * Parse the given {@link InputStream} into {@link Sessions#CONTENT_URI}
     * assuming a JSON format. Removes all existing data.
     */
    public static void parseSchedule(Context context, InputStream is) throws IOException, JSONException {
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
    
    /**
     * Parse the given {@link InputStream} into {@link Speakers#CONTENT_URI}
     * assuming a JSON format. Removes all existing data.
     */
    public static void parseSpeakers(Context context, InputStream is) throws IOException, JSONException {
    	ContentResolver resolver = context.getContentResolver();
    	resolver.delete(Speakers.CONTENT_URI, null, null);
    	
    	// Parse incoming JSON stream
    	JSONArray speakers = parseJsonStream(is);
    	ContentValues values = new ContentValues();
    	
    	// Walk across all speakers found
    	int speakersCount = speakers.length();
    	for (int i = 0; i < speakersCount; i++) {
    		JSONObject speaker = speakers.getJSONObject(i);
    		
    		// Parse this speaker and insert
    		values = parseSpeaker(speaker, values);
    		resolver.insert(Speakers.CONTENT_URI, values);
    	}
    }
    
    /**
     * Parse a given track {@link JSONObject} into the given
     * {@link ContentValues} for insertion into {@link Tracks#CONTENT_URI}.
     */
    private static ContentValues parseTrack(JSONObject track, ContentValues recycle) {
        recycle.clear();
        recycle.put(TracksColumns.TRACK, track.optString(TrackJsonKeys.TRACK, null));
        int color = Integer.parseInt(track.optString(TrackJsonKeys.COLOR, null), 16);
        recycle.put(TracksColumns.COLOR, color);
        recycle.put(TracksColumns.VISIBLE, 1);
        return recycle;
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
    
    /**
     * Parse a given speaker (@link JSONObject} into the given
     * {@link ContentValues} for insertion into {@link Speakers#CONTENT_URI}
     */
    private static ContentValues parseSpeaker(JSONObject speaker, ContentValues recycle) {
    	recycle.clear();
    	recycle.put(SpeakersColumns.SPEAKERNAME, speaker.optString(SpeakerJsonKeys.SPEAKERNAME, null));
    	recycle.put(SpeakersColumns.SPEAKERBIO, speaker.optString(SpeakerJsonKeys.SPEAKERBIO, null));
    	return recycle;
    }

    private static interface TrackJsonKeys {
        public static final String TRACK = "track";
        public static final String COLOR = "color";
    }

    private static interface SessionJsonKeys {
        public static final String TRACK = "track";
        
        public static final String SESSIONTITLE = "sessiontitle";
        public static final String SESSIONSPEAKERS = "sessionspeakers";
        public static final String SESSIONABSTRACT = "sessionabstract";
        public static final String ROOM = "room";
        
        public static final String SESSIONTYPE = "sessiontype";
        public static final String TAGS = "tags";

        public static final String FULLLINK = "fulllink";
        public static final String MODERATORLINK = "moderatorlink";

        public static final String UNIXSTART = "unixstart";
        public static final String UNIXEND = "unixend";

        public static final String DATETIME = "datetime";
    }
    
    private static interface SpeakerJsonKeys {
    	public static final String SPEAKERNAME = "speakerName";
    	public static final String SPEAKERBIO = "speakerbio";
    }
    
}
