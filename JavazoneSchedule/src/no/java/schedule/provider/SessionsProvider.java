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

import android.app.SearchManager;
import android.content.*;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.*;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import no.java.schedule.provider.SessionsContract.*;
import static no.java.schedule.provider.SessionsContract.AUTHORITY;

import java.util.Date;
import java.util.HashMap;

/**
 * {@link ContentProvider} that stores session details.
 */
public class SessionsProvider extends ContentProvider {
    //private static final String TAG = "SessionsProvider";
    
    private static final String TABLE_TRACKS = "tracks";
    private static final String TABLE_BLOCKS = "blocks";
    private static final String TABLE_SESSIONS = "sessions";
    private static final String TABLE_SEARCH = "search";
    private static final String TABLE_SUGGEST = "suggest";
    private static final String TABLE_SPEAKERS = "speakers";

    private static final String TABLE_SESSIONS_JOIN_TRACKS_BLOCKS = "sessions" 
            + " LEFT OUTER JOIN tracks ON sessions.track_id=tracks._id"
            + " LEFT OUTER JOIN blocks ON sessions.block_id=blocks._id";

    private static final String TABLE_SEARCH_JOIN_SESSIONS_TRACKS_BLOCKS = "search "
        + "LEFT OUTER JOIN sessions ON search.rowid=sessions._id "
        + "LEFT OUTER JOIN tracks ON sessions.track_id=tracks._id "
        + "LEFT OUTER JOIN blocks ON sessions.block_id=blocks._id";
    
    private static final String SNIPPET_SQL = "snippet(" + TABLE_SEARCH
            + ", '{', '}', '\u2026') AS " + SearchColumns.SNIPPET;
    
    private DatabaseHelper mOpenHelper;
    private LookupCache mLookupCache;
    
    /** {@inheritDoc} */
    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return (mOpenHelper.getReadableDatabase() != null);
    }

    /**
     * Provide a lookup cache for {@link Tracks#_ID} and {@link Blocks#_ID},
     * which is useful for speeding up doing bulk inserts.
     */
    private static class LookupCache {
        
        private SQLiteStatement mTrackQuery;
        private SQLiteStatement mBlockQuery;
        private SQLiteStatement mTrackInsert;
        private SQLiteStatement mBlockInsert;
        
        private HashMap<Integer, Long> mTrackCache = new HashMap<Integer, Long>();
        private HashMap<Integer, Long> mBlockCache = new HashMap<Integer, Long>();

        public LookupCache(SQLiteDatabase db) {
            // Prepare lookup query and insert statements
            mTrackQuery = db.compileStatement("SELECT " + BaseColumns._ID + " FROM " + TABLE_TRACKS
                    + " WHERE " + TracksColumns.TRACK + "=?");
            mBlockQuery = db.compileStatement("SELECT " + BaseColumns._ID + " FROM " + TABLE_BLOCKS
                    + " WHERE " + BlocksColumns.TIME_START + "=? AND " + BlocksColumns.TIME_END
                    + "=?");
            
            mTrackInsert = db.compileStatement("INSERT INTO " + TABLE_TRACKS + " VALUES (NULL,?,10526880,1)");
            mBlockInsert = db.compileStatement("INSERT INTO " + TABLE_BLOCKS + " VALUES (NULL,?,?)");
        }
        
        /**
         * Fill a valid {@link Tracks#_ID} for the provided
         * {@link ContentValues}, querying or creating as needed.
         */
        public synchronized void fillTrackId(ContentValues incoming) {
            if (incoming.containsKey(SessionsColumns.TRACK_ID)) return;
            String[] values = new String[] {
                incoming.getAsString(TracksColumns.TRACK),
            };
            long trackId = getCachedId(mTrackQuery, mTrackInsert, values, mTrackCache);
            
            incoming.put(SessionsColumns.TRACK_ID, trackId);
            incoming.remove(TracksColumns.TRACK);
        }
        
        /**
         * Fill a valid {@link Blocks#_ID} for the provided
         * {@link ContentValues}, querying or creating as needed.
         */
        public synchronized void fillBlockId(ContentValues incoming) {
            if (incoming.containsKey(SessionsColumns.BLOCK_ID)) return;

            final Long startTime = incoming.getAsLong(BlocksColumns.TIME_START);
            final Long endTime = incoming.getAsLong(BlocksColumns.TIME_END);

            Long[] values = new Long[]{
                    startTime,
                    endTime,
            };

            Log.d("javaBinSchedule", String.format("Looking up slot %s - %s", new Date(startTime), new Date(endTime)));
            long blockId = getCachedId(mBlockQuery, mBlockInsert, values, mBlockCache);
            
            incoming.put(SessionsColumns.BLOCK_ID, blockId);
            incoming.remove(BlocksColumns.TIME_START);
            incoming.remove(BlocksColumns.TIME_END);
        }

        /**
         * Attempt and in-memory cache lookup of the given value, using the
         * provided {@link SQLiteStatements} to query and create one if needed.
         */
        private synchronized long getCachedId(SQLiteStatement query, SQLiteStatement insert,
                Object[] values, HashMap<Integer, Long> cache) {
            // Try and in-memory cache lookup

            //TODO - Bug here does not fetch the track, but inserts on each lookup
            final int hashCode = values.hashCode();
            if (cache.containsKey(hashCode)) {
                return cache.get(hashCode);
            }

            long id = -1;
            try {
                // Try searching database for mapping
                for (int i = 0; i < values.length; i++){
                    DatabaseUtils.bindObjectToProgram(query, i + 1, values[i]);
                }

                id = query.simpleQueryForLong();
                Log.d("javaBinSchedule","Found block:"+id);

            } catch (SQLiteDoneException e) {
                // Nothing found, so try inserting new mapping
                for (int i = 0; i < values.length; i++){
                    DatabaseUtils.bindObjectToProgram(insert, i + 1, values[i]);
                }
                id = insert.executeInsert();
                Log.d("javaBinSchedule","Created new block:"+id);
            }

            if (id != -1) {
                // Cache and return the new answer
                //cache.put(hashCode, id);
                return id;
            } else {
                throw new IllegalStateException("Couldn't find or create internal mapping");
            }
        }
    }

    /**
     * Helper to manage upgrading between versions of the forecast database.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "sessions.db";

        private static final int DATABASE_VERSION = 5;
        
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_TRACKS + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + TracksColumns.TRACK + " TEXT,"
                    + TracksColumns.COLOR + " INTEGER,"
                    + TracksColumns.VISIBLE + " INTEGER);");
            
            db.execSQL("CREATE TABLE " + TABLE_BLOCKS + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + BlocksColumns.TIME_START + " INTEGER,"
                    + BlocksColumns.TIME_END + " INTEGER);");

            db.execSQL("CREATE TABLE " + TABLE_SESSIONS + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + SessionsColumns.TRACK_ID + " INTEGER REFERENCES tracks(_id),"
                    + SessionsColumns.BLOCK_ID + " INTEGER REFERENCES blocks(_id),"
                    + SessionsColumns.TITLE + " TEXT,"
                    + SessionsColumns.SPEAKER_NAMES + " TEXT,"
                    + SessionsColumns.ABSTRACT + " TEXT,"
                    + SessionsColumns.ROOM + " INTEGER,"
                    + SessionsColumns.TYPE + " TEXT,"
                    + SessionsColumns.TAGS + " TEXT,"
                    + SessionsColumns.LINK + " TEXT,"
                    + SessionsColumns.LINK_ALT + " TEXT,"
                    + SessionsColumns.STARRED + " INTEGER);");

            db.execSQL("CREATE VIRTUAL TABLE " + TABLE_SEARCH + " USING FTS1("
                    + SearchColumns.INDEX_TEXT + ");");
            
            db.execSQL("CREATE TABLE " + TABLE_SUGGEST + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY,"
                    + SuggestColumns.DISPLAY1 + " TEXT);");
            
            // ��� speakers integration
            db.execSQL("CREATE TABLE " + TABLE_SPEAKERS + " ("
            		+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            		+ SpeakersColumns.SPEAKERNAME + " TEXT,"
            		+ SpeakersColumns.SPEAKERBIO + " TEXT);");
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            int version = oldVersion;

            if (version < 5) {
            	db.execSQL(UpdateDatabaseSQL.V5_UPDATE_SESSIONS);
            	db.execSQL(UpdateDatabaseSQL.V5_UPDATE_SPEAKERS);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        
        //Log.d(TAG, "query() on " + uri);
        
        String limit = null;
        Uri notificationUri = uri;
        switch (sUriMatcher.match(uri)) {
            case TRACKS: {
                qb.setTables(TABLE_TRACKS);
                qb.setProjectionMap(sTracksProjection);
                sortOrder = TracksColumns.TRACK + " ASC";
                break;
            }
            case TRACKS_VISIBLE: {
                qb.setTables(TABLE_TRACKS);
                qb.setProjectionMap(sTracksProjection);
                qb.appendWhere(TracksColumns.VISIBLE + "=1");
                sortOrder = TracksColumns.TRACK + " ASC";
                break;
            }
            case TRACKS_SESSIONS: {
                qb.setTables(TABLE_SESSIONS_JOIN_TRACKS_BLOCKS);
                qb.setProjectionMap(sSessionsProjection);
                qb.appendWhere("tracks._id=" + uri.getPathSegments().get(1));
                //sortOrder = TracksColumns.TRACK + " ASC, " + BlocksColumns.TIME_START + " ASC";
                sortOrder = BlocksColumns.TIME_START + " ASC";
                break;
            }
            case BLOCKS: {
                qb.setTables(TABLE_BLOCKS);
                qb.setProjectionMap(sBlocksProjection);
                sortOrder = BlocksColumns.TIME_START + " ASC";
                notificationUri = Blocks.CONTENT_URI;
                break;
            }
            case BLOCKS_SESSIONS: {
                qb.setTables(TABLE_SESSIONS_JOIN_TRACKS_BLOCKS);
                qb.setProjectionMap(sSessionsProjection);
                qb.appendWhere("blocks._id=" + uri.getPathSegments().get(1));
                sortOrder = BlocksColumns.TIME_START + " ASC, " + TracksColumns.TRACK + " ASC";
                break;
            }
            case SESSIONS: {
                qb.setTables(TABLE_SESSIONS_JOIN_TRACKS_BLOCKS);
                qb.setProjectionMap(sSessionsProjection);
                break;
            }
            case SESSIONS_ID: {
                long sessionId = ContentUris.parseId(uri);
                qb.setTables(TABLE_SESSIONS_JOIN_TRACKS_BLOCKS);
                qb.appendWhere("sessions._id=" + sessionId);
                break;
            }
            case SESSIONS_SEARCH: {
                String query = uri.getPathSegments().get(2);
                qb.setTables(TABLE_SEARCH_JOIN_SESSIONS_TRACKS_BLOCKS);
                qb.setProjectionMap(sSearchProjection);
                qb.appendWhere(SearchColumns.INDEX_TEXT + " MATCH ");
                qb.appendWhereEscapeString(query);
                break;
            }
            case SUGGEST: {
                //final String query = selectionArgs[0];
                
                qb.setTables(TABLE_SUGGEST);
                qb.setProjectionMap(sSuggestProjection);
                qb.appendWhere(SuggestColumns.DISPLAY1 + " LIKE ");
                qb.appendWhereEscapeString(selectionArgs[0] + "%");
                sortOrder = SuggestColumns.DISPLAY1 + " ASC";
                limit = "15";
                
                selection = null;
                selectionArgs = null;
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown URL " + uri);
            }
        }

        // If no sort order is specified use the default
        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = BlocksColumns.TIME_START + " ASC, " + TracksColumns.TRACK + " ASC";
        }

        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder, limit);
        c.setNotificationUri(getContext().getContentResolver(), notificationUri);
        return c;
    }

    /** {@inheritDoc} */
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case TRACKS:
                return Tracks.CONTENT_TYPE;
            case BLOCKS:
                return Blocks.CONTENT_TYPE;
            case TRACKS_SESSIONS:
            case BLOCKS_SESSIONS:
            case SESSIONS:
                return Sessions.CONTENT_TYPE;
            case SESSIONS_ID:
                return Sessions.CONTENT_ITEM_TYPE;
            case SPEAKERS:
            	return Speakers.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        
        switch (sUriMatcher.match(uri)) {
            case TRACKS: {
                long trackId = db.insert(TABLE_TRACKS, TracksColumns.TRACK, values);
                uri = ContentUris.withAppendedId(Tracks.CONTENT_URI, trackId);
                break;
            }
            case BLOCKS: {
                long blockId = db.insert(TABLE_BLOCKS, BlocksColumns.TIME_END, values);
                uri = ContentUris.withAppendedId(Blocks.CONTENT_URI, blockId);
                break;
            }
            case SESSIONS: {
                if (mLookupCache == null) {
                    mLookupCache = new LookupCache(db);
                }
                
                // Replace tracks and blocks with internal table references
                mLookupCache.fillTrackId(values);
                mLookupCache.fillBlockId(values);
                
                // Pull out search index before normal insert
                String indexText = values.getAsString(SearchColumns.INDEX_TEXT);
                values.remove(SearchColumns.INDEX_TEXT);
                
                long sessionId = db.insert(TABLE_SESSIONS, SessionsColumns.TITLE, values);
                uri = ContentUris.withAppendedId(Sessions.CONTENT_URI, sessionId);

                // And now fill search index
                values.clear();
                values.put("rowid", sessionId);
                values.put(SearchColumns.INDEX_TEXT, indexText);
                
                db.insert(TABLE_SEARCH, null, values);
                break;
            }
            case SUGGEST: {
                db.insert(TABLE_SUGGEST, null, values);
                break;
            }
            case SPEAKERS: {
            	db.insert(TABLE_SPEAKERS, null, values);
            	break;
            }
            default: {
                throw new SQLException("Failed to insert row into " + uri);
            }
        }
        return uri;
    }

    /** {@inheritDoc} */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        
        int count;
        switch (sUriMatcher.match(uri)) {
            case TRACKS: {
                count = db.delete(TABLE_TRACKS, selection, selectionArgs);
                break;
            }
            case BLOCKS: {
                count = db.delete(TABLE_BLOCKS, selection, selectionArgs);
                break;
            }
            case SESSIONS: {
                count = db.delete(TABLE_SESSIONS, selection, selectionArgs);
                count += db.delete(TABLE_SEARCH, selection, selectionArgs);
                break;
            }
            case SESSIONS_ID: {
                long sessionId = ContentUris.parseId(uri);
                count = db.delete(TABLE_SESSIONS, BaseColumns._ID + "=" + sessionId, null);
                break;
            }
            case SUGGEST: {
                count = db.delete(TABLE_SUGGEST, selection, selectionArgs);
                break;
            }
            case SPEAKERS: {
            	count = db.delete(TABLE_SPEAKERS, selection, selectionArgs);
            	break;
            }
            default: {
                throw new IllegalArgumentException("Unknown URL " + uri);
            }
        }

        getContext().getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /** {@inheritDoc} */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        
        //Log.d(TAG, "update() on " + uri + " with " + values.toString());
        
        int count;
        Uri notifyUri = uri;
        switch (sUriMatcher.match(uri)) {
            case TRACKS_ID: {
                long trackId = ContentUris.parseId(uri);
                count = db.update(TABLE_TRACKS, values, BaseColumns._ID + "=" + trackId, null);

                getContext().getContentResolver().notifyChange( Sessions.CONTENT_URI, null);
                break;
            }
            case SESSIONS_ID: {
                long sessionId = ContentUris.parseId(uri);
                count = db.update(TABLE_SESSIONS, values, BaseColumns._ID + "=" + sessionId, null);
                
                // Pull out track and block id, if provided
                if (values.containsKey(SessionsColumns.TRACK_ID)
                        && values.containsKey(SessionsColumns.BLOCK_ID)) {
                    long trackId = values.getAsLong(SessionsColumns.TRACK_ID);
                    long blockId = values.getAsLong(SessionsColumns.BLOCK_ID);

                    final ContentResolver resolver = getContext().getContentResolver();
                    Uri trackUri = Uri.withAppendedPath(ContentUris.withAppendedId(Tracks.CONTENT_URI,
                            trackId), Sessions.CONTENT_DIRECTORY);
                    Uri blockUri = Uri.withAppendedPath(ContentUris.withAppendedId(Blocks.CONTENT_URI,
                            blockId), Sessions.CONTENT_DIRECTORY);
                    
                    resolver.notifyChange(trackUri, null, false);
                    resolver.notifyChange(blockUri, null, false);
                }
                
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown URL " + uri);
            }
        }

        getContext().getContentResolver().notifyChange(notifyUri, null, false);
        return count;
    }


    /**
     * Matcher used to filter an incoming {@link Uri}. 
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final HashMap<String, String> sTracksProjection;
    public static final HashMap<String, String> sBlocksProjection;
    public static final HashMap<String, String> sSessionsProjection;
    public static final HashMap<String, String> sSearchProjection;
    public static final HashMap<String, String> sSuggestProjection;
    public static final HashMap<String, String> sSpeakersProjection;
    
    private static final int TRACKS = 101;
    private static final int TRACKS_ID = 102;
    private static final int TRACKS_VISIBLE = 103;
    private static final int TRACKS_SESSIONS = 104;
    
    private static final int BLOCKS = 201;
    private static final int BLOCKS_SESSIONS = 203;

    private static final int SESSIONS = 301;
    private static final int SESSIONS_ID = 302;
    private static final int SESSIONS_SEARCH = 303;
    
    private static final int SUGGEST = 401;

    private static final int SPEAKERS = 501;
    private static final int SPEAKERS_SEARCH = 502;

    static {
        sUriMatcher.addURI(AUTHORITY, "tracks", TRACKS);
        sUriMatcher.addURI(AUTHORITY, "tracks/#", TRACKS_ID);
        sUriMatcher.addURI(AUTHORITY, "tracks/visible", TRACKS_VISIBLE);
        sUriMatcher.addURI(AUTHORITY, "tracks/#/sessions", TRACKS_SESSIONS);
        
        sUriMatcher.addURI(AUTHORITY, "blocks", BLOCKS);
        sUriMatcher.addURI(AUTHORITY, "blocks/#/sessions", BLOCKS_SESSIONS);
        
        sUriMatcher.addURI(AUTHORITY, "sessions", SESSIONS);
        sUriMatcher.addURI(AUTHORITY, "sessions/#", SESSIONS_ID);
        sUriMatcher.addURI(AUTHORITY, "sessions/search/*", SESSIONS_SEARCH);

        sUriMatcher.addURI(AUTHORITY, "search_suggest_query", SUGGEST);
        
        sUriMatcher.addURI(AUTHORITY, "speakers", SPEAKERS);
        sUriMatcher.addURI(AUTHORITY, "speakers/search/*", SPEAKERS_SEARCH);
        
        // Projection for tracks
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(BaseColumns._ID, BaseColumns._ID);
        map.put(TracksColumns.TRACK, TracksColumns.TRACK);
        map.put(TracksColumns.COLOR, TracksColumns.COLOR);
        map.put(TracksColumns.VISIBLE, TracksColumns.VISIBLE);
        sTracksProjection = map;
        
        // Projection for blocks
        map = new HashMap<String, String>();
        map.put(BaseColumns._ID, BaseColumns._ID);
        map.put(BlocksColumns.TIME_START, BlocksColumns.TIME_START);
        map.put(BlocksColumns.TIME_END, BlocksColumns.TIME_END);
        sBlocksProjection = map;

        // Projection for sessions
        map = new HashMap<String, String>();
        map.putAll(sTracksProjection);
        map.putAll(sBlocksProjection);
        map.put(BaseColumns._ID, "sessions._id as _id");
        map.put(SessionsColumns.TRACK_ID, SessionsColumns.TRACK_ID);
        map.put(SessionsColumns.BLOCK_ID, SessionsColumns.BLOCK_ID);
        map.put(SessionsColumns.TITLE, SessionsColumns.TITLE);
        map.put(SessionsColumns.SPEAKER_NAMES, SessionsColumns.SPEAKER_NAMES);
        map.put(SessionsColumns.ABSTRACT, SessionsColumns.ABSTRACT);
        map.put(SessionsColumns.ROOM, SessionsColumns.ROOM);
        map.put(SessionsColumns.TYPE, SessionsColumns.TYPE);
        map.put(SessionsColumns.TAGS, SessionsColumns.TAGS);
        map.put(SessionsColumns.LINK, SessionsColumns.LINK);
        map.put(SessionsColumns.LINK_ALT, SessionsColumns.LINK_ALT);
        map.put(SessionsColumns.STARRED, SessionsColumns.STARRED);
        map.put(TracksColumns.COLOR, TracksColumns.COLOR);
        sSessionsProjection = map;
        
        // Projection for searches
        map = new HashMap<String, String>();
        map.putAll(sSessionsProjection);
        map.put(SearchColumns.SNIPPET, SNIPPET_SQL);
        sSearchProjection = map;
        
        // Projection for suggestions
        map = new HashMap<String, String>();
        map.put(BaseColumns._ID, BaseColumns._ID);
        map.put(SearchManager.SUGGEST_COLUMN_TEXT_1, SuggestColumns.DISPLAY1 + " AS "
                + SearchManager.SUGGEST_COLUMN_TEXT_1);
        map.put(SearchManager.SUGGEST_COLUMN_QUERY, SuggestColumns.DISPLAY1 + " AS "
                + SearchManager.SUGGEST_COLUMN_QUERY);
        sSuggestProjection = map;
        
        // Projection for speakers
        map = new HashMap<String, String>();
        map.put(BaseColumns._ID, BaseColumns._ID);
        map.put(SpeakersColumns.SPEAKERNAME, SpeakersColumns.SPEAKERNAME);
        map.put(SpeakersColumns.SPEAKERBIO, SpeakersColumns.SPEAKERBIO);
        sSpeakersProjection = map;
        
    }
}
