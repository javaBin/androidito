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

import android.content.*;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import no.java.schedule.provider.SessionsContract.*;
import no.java.schedule.provider.dbutil.DatabaseHelper;
import no.java.schedule.provider.dbutil.LookupCache;

import java.util.HashMap;

public class SessionsProvider extends ContentProvider {

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

    public static final HashMap<String, String> sSearchProjection = Projections.createSearchProjection();
    public static final HashMap<String, String> sSuggestProjection = Projections.createSuggestProjection();
    public static final HashMap<String, String> sSpeakersProjection = Projections.createSpeakerProjection();

    public static final String TABLE_TRACKS = "tracks";
    public static final String TABLE_BLOCKS = "blocks";
    public static final String TABLE_SESSIONS = "sessions";
    public static final String TABLE_SEARCH = "search";
    public static final String TABLE_SUGGEST = "suggest";
    public static final String TABLE_SPEAKERS = "speakers";


    /**
     * Matcher used to filter an incoming {@link Uri}.
     */
    private static final UriMatcher sUriMatcher =  new SessionUriMatcher();

    private static final String TABLE_SESSIONS_JOIN_TRACKS_BLOCKS = "sessions"
            + " LEFT OUTER JOIN tracks ON sessions.track_id=tracks._id"
            + " LEFT OUTER JOIN blocks ON sessions.block_id=blocks._id";

    private static final String TABLE_SEARCH_JOIN_SESSIONS_TRACKS_BLOCKS = "search "
            + "LEFT OUTER JOIN sessions ON search.rowid=sessions._id "
            + "LEFT OUTER JOIN tracks ON sessions.track_id=tracks._id "
            + "LEFT OUTER JOIN blocks ON sessions.block_id=blocks._id";

    private static final String DEFAULT_SORT_ORDER = BlocksColumns.TIME_START + " ASC, " + TracksColumns.TRACK + " ASC";

    private LookupCache mLookupCache;
    private SQLiteDatabase readDb;
    private SQLiteDatabase writeDb;
    private ContentResolver resolver;

    public SessionsProvider() {
        resolver = getContext().getContentResolver();
    }

    @Override
    public boolean onCreate() {
        try {
            DatabaseHelper mOpenHelper = new DatabaseHelper(getContext());
            readDb = mOpenHelper.getReadableDatabase();
            writeDb = mOpenHelper.getWritableDatabase();
            mLookupCache = new LookupCache(readDb);
        return true;
        } catch (SQLiteException e){
            Log.e("Androidito","Fatal error creating contentprovider",e);
            return false;
        }
    }

    @Override
    public Cursor query(Uri notificationUri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {


        switch (sUriMatcher.match(notificationUri)) {
            case TRACKS:
                return queryTrack(projection, selection, selectionArgs, notificationUri );

            case TRACKS_VISIBLE:
                return queryVisibleTracks(projection, selection, selectionArgs, notificationUri );

            case TRACKS_SESSIONS:
                return queryTrackSessions(notificationUri, projection, selection, selectionArgs, notificationUri );

            case BLOCKS:
                return queryBlocks(projection, selection, selectionArgs, notificationUri );

            case BLOCKS_SESSIONS:
                return queryBlockSessions(notificationUri, projection, selection, selectionArgs, notificationUri );

            case SESSIONS:
                return querySessions(projection, selection, selectionArgs, notificationUri );

            case SESSIONS_ID:
                return querySessionId(notificationUri, projection, selection, selectionArgs, notificationUri );

            case SESSIONS_SEARCH:
                return querySessionSearch(notificationUri, projection, selection, selectionArgs, notificationUri );

            case SUGGEST:
                return querySuggest(projection, selectionArgs, notificationUri );

            default:
                throw new IllegalArgumentException("Unknown URL " + notificationUri);
        }

    }

    private Cursor querySuggest(String[] projection, String[] selectionArgs, Uri notificationUri) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(TABLE_SUGGEST);
        qb.setProjectionMap(sSuggestProjection);
        qb.appendWhere(SuggestColumns.DISPLAY1 + " LIKE ");
        qb.appendWhereEscapeString(selectionArgs[0] + "%");

        Cursor c = qb.query(readDb, projection, null, null, null, null, SuggestColumns.DISPLAY1 + " ASC", "15");
        c.setNotificationUri(getContext().getContentResolver(), notificationUri);
        return c;
    }

    private Cursor querySessionSearch(Uri uri, String[] projection, String selection, String[] selectionArgs, Uri notificationUri) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String query = uri.getPathSegments().get(2);
        qb.setTables(TABLE_SEARCH_JOIN_SESSIONS_TRACKS_BLOCKS);
        qb.setProjectionMap(sSearchProjection);
        qb.appendWhere(SearchColumns.INDEX_TEXT + " MATCH ");
        qb.appendWhereEscapeString(query);

        Cursor c = qb.query(readDb, projection, selection, selectionArgs, null, null, DEFAULT_SORT_ORDER, null);
        c.setNotificationUri(getContext().getContentResolver(), notificationUri);
        return c;
    }

    private Cursor querySessionId(Uri uri, String[] projection, String selection, String[] selectionArgs, Uri notificationUri) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        long sessionId = ContentUris.parseId(uri);
        qb.setTables(TABLE_SESSIONS_JOIN_TRACKS_BLOCKS);
        qb.appendWhere("sessions._id=" + sessionId);
        Cursor c = qb.query(readDb, projection, selection, selectionArgs, null, null, DEFAULT_SORT_ORDER, null);
        c.setNotificationUri(getContext().getContentResolver(), notificationUri);
        return c;
    }

    private Cursor querySessions(String[] projection, String selection, String[] selectionArgs, Uri notificationUri) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_SESSIONS_JOIN_TRACKS_BLOCKS);
        qb.setProjectionMap(Projections.sSessionsProjection);
        Cursor c = qb.query(readDb, projection, selection, selectionArgs, null, null, DEFAULT_SORT_ORDER, null);
        c.setNotificationUri(getContext().getContentResolver(), notificationUri);
        return c;
    }

    private Cursor queryBlockSessions(Uri uri, String[] projection, String selection, String[] selectionArgs, Uri notificationUri) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_SESSIONS_JOIN_TRACKS_BLOCKS);
        qb.setProjectionMap(Projections.sSessionsProjection);
        qb.appendWhere("blocks._id=" + uri.getPathSegments().get(1));
        Cursor c = qb.query(readDb, projection, selection, selectionArgs, null, null, DEFAULT_SORT_ORDER, null);
        c.setNotificationUri(getContext().getContentResolver(), notificationUri);
        return c;
    }

    private Cursor queryBlocks(String[] projection, String selection, String[] selectionArgs, Uri notificationUri) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_BLOCKS);
        qb.setProjectionMap(Projections.sBlocksProjection);
        Cursor c = qb.query(readDb, projection, selection, selectionArgs, null, null, BlocksColumns.TIME_START + " ASC", null);
        c.setNotificationUri(getContext().getContentResolver(), Blocks.CONTENT_URI);
        return c;
    }

    private Cursor queryTrackSessions(Uri uri, String[] projection, String selection, String[] selectionArgs, Uri notificationUri) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_SESSIONS_JOIN_TRACKS_BLOCKS);
        qb.setProjectionMap(Projections.sSessionsProjection);
        qb.appendWhere("tracks._id=" + uri.getPathSegments().get(1));
        Cursor c = qb.query(readDb, projection, selection, selectionArgs, null, null, BlocksColumns.TIME_START + " ASC", null);
        c.setNotificationUri(getContext().getContentResolver(), notificationUri);
        return c;
    }

    private Cursor queryVisibleTracks(String[] projection, String selection, String[] selectionArgs, Uri notificationUri) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_TRACKS);
        qb.setProjectionMap(Projections.sTracksProjection);
        qb.appendWhere(TracksColumns.VISIBLE + "=1");
        Cursor c = qb.query(readDb, projection, selection, selectionArgs, null, null, TracksColumns.TRACK + " ASC", null);
        c.setNotificationUri(getContext().getContentResolver(), notificationUri);

        return c;
    }

    private Cursor queryTrack(String[] projection, String selection, String[] selectionArgs, Uri notificationUri) {
        String sortOrder;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_TRACKS);
        qb.setProjectionMap(Projections.sTracksProjection);
        sortOrder = TracksColumns.TRACK + " ASC";
        Cursor c = qb.query(readDb, projection, selection, selectionArgs, null, null, sortOrder, null);
        c.setNotificationUri(getContext().getContentResolver(), notificationUri);
        return c;
    }

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

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        switch (sUriMatcher.match(uri)) {
            case TRACKS:
                return insertTracks(values);

            case BLOCKS:
                return insertBlock(values);

            case SESSIONS:
                return insertSession(values);

            case SUGGEST:
                return insertSuggest(uri, values);

            case SPEAKERS:
                return insertSpeaker(uri, values);

            default:
                throw new SQLException("Failed to insert row into " + uri);
        }

    }

    private Uri insertSpeaker(Uri uri, ContentValues values) {
        writeDb.insert(TABLE_SPEAKERS, null, values);
        return uri;
    }

    private Uri insertSuggest(Uri uri, ContentValues values) {
        writeDb.insert(TABLE_SUGGEST, null, values);
        return uri;
    }

    private Uri insertSession(ContentValues values) {
        Uri uri;
        if (mLookupCache == null) {
            mLookupCache = new LookupCache(writeDb);
        }

        // Replace tracks and blocks with internal table references
        mLookupCache.fillTrackId(values);
        mLookupCache.fillBlockId(values);

        // Pull out search index before normal insert
        String indexText = values.getAsString(SearchColumns.INDEX_TEXT);
        values.remove(SearchColumns.INDEX_TEXT);

        long sessionId = writeDb.insert(TABLE_SESSIONS, SessionsColumns.TITLE, values);
        uri = ContentUris.withAppendedId(Sessions.CONTENT_URI, sessionId);

        // And now fill search index
        values.clear();
        values.put("rowid", sessionId);
        values.put(SearchColumns.INDEX_TEXT, indexText);

        writeDb.insert(TABLE_SEARCH, null, values);
        return uri;
    }

    private Uri insertBlock(ContentValues values) {
        long blockId = writeDb.insert(TABLE_BLOCKS, BlocksColumns.TIME_END, values);
        return ContentUris.withAppendedId(Blocks.CONTENT_URI, blockId);
    }

    private Uri insertTracks(ContentValues values) {
        long trackId = writeDb.insert(TABLE_TRACKS, TracksColumns.TRACK, values);
        return ContentUris.withAppendedId(Tracks.CONTENT_URI, trackId);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (sUriMatcher.match(uri)) {
            case TRACKS:
                return deleteTracks(uri, selection, selectionArgs);

            case BLOCKS:
                return deleteBlocks(uri, selection, selectionArgs);

            case SESSIONS:
                return deleteSessions(uri, selection, selectionArgs);

            case SESSIONS_ID:
                return deleteSessionsId(uri);

            case SUGGEST:
                return deleteSuggest(uri, selection, selectionArgs);

            case SPEAKERS:
                return deleteSpeakers(uri, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Unknown URL " + uri);

        }
    }

    private int deleteSpeakers(Uri uri, String selection, String[] selectionArgs) {
        int count;
        count = writeDb.delete(TABLE_SPEAKERS, selection, selectionArgs);
        resolver.notifyChange(uri, null, false);
        return count;
    }

    private int deleteSuggest(Uri uri, String selection, String[] selectionArgs) {
        int count;
        count = writeDb.delete(TABLE_SUGGEST, selection, selectionArgs);
        resolver.notifyChange(uri, null, false);
        return count;
    }

    private int deleteSessionsId(Uri uri) {
        int count;
        long sessionId = ContentUris.parseId(uri);
        count = writeDb.delete(TABLE_SESSIONS, BaseColumns._ID + "=" + sessionId, null);
        resolver.notifyChange(uri, null, false);
        return count;
    }

    private int deleteSessions(Uri uri, String selection, String[] selectionArgs) {
        int count;
        count = writeDb.delete(TABLE_SESSIONS, selection, selectionArgs);
        count += writeDb.delete(TABLE_SEARCH, selection, selectionArgs);
        resolver.notifyChange(uri, null, false);
        return count;
    }


    private int deleteBlocks(Uri uri, String selection, String[] selectionArgs) {
        int count;
        count = writeDb.delete(TABLE_BLOCKS, selection, selectionArgs);
        resolver.notifyChange(uri, null, false);
        return count;
    }

    private int deleteTracks(Uri uri, String selection, String[] selectionArgs) {
        int count;
        count = writeDb.delete(TABLE_TRACKS, selection, selectionArgs);
        resolver.notifyChange(uri, null, false);
        return count;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        switch (sUriMatcher.match(uri)) {

            case TRACKS_ID:
                return updateTrack(uri, values);
            case SESSIONS_ID:
                return updateSession(uri, values);
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);

        }

    }

    private int updateSession(Uri uri, ContentValues values) {
        long sessionId = ContentUris.parseId(uri);

        int count = writeDb.update(TABLE_SESSIONS, values, BaseColumns._ID + "=" + sessionId, null);

        getContext().getContentResolver().notifyChange(Sessions.CONTENT_URI, null, false);
        notifyTrackChange(values);
        notifyBlockChange(values);


        return count;
    }

    private void notifyBlockChange(ContentValues values) {
        // Pull out  block id if provided
        if (values.containsKey(SessionsColumns.BLOCK_ID)) {
            long blockId = values.getAsLong(SessionsColumns.BLOCK_ID);
            Uri blockUri = Uri.withAppendedPath(ContentUris.withAppendedId(Blocks.CONTENT_URI, blockId), Sessions.CONTENT_DIRECTORY);
            getContext().getContentResolver().notifyChange(blockUri, null, false);
        }
    }

    private void notifyTrackChange(ContentValues values) {
        // Pull out track id if provided
        if (values.containsKey(SessionsColumns.TRACK_ID) ) {
            long trackId = values.getAsLong(SessionsColumns.TRACK_ID);
            Uri trackUri = Uri.withAppendedPath( ContentUris.withAppendedId(Tracks.CONTENT_URI, trackId ), Sessions.CONTENT_DIRECTORY);
            getContext().getContentResolver().notifyChange(trackUri, null, false);
        }
    }

    private int updateTrack(Uri uri, ContentValues values) {
        int count;
        long trackId = ContentUris.parseId(uri);
        count = writeDb.update(TABLE_TRACKS, values, BaseColumns._ID + "=" + trackId, null);
        getContext().getContentResolver().notifyChange( Tracks.CONTENT_URI, null);
        return count;
    }


}
