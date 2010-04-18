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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import no.java.schedule.provider.SessionsContract.*;
import no.java.schedule.provider.dbutil.DatabaseHelper;
import no.java.schedule.provider.dbutil.LookupCache;

import java.util.HashMap;

/**
 * {@link ContentProvider} that stores session details.
 */
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

    private DatabaseHelper mOpenHelper;
    private LookupCache mLookupCache;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return (mOpenHelper.getReadableDatabase() != null);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String limit = null;
        Uri notificationUri = uri;
        switch (sUriMatcher.match(uri)) {
            case TRACKS: {
                qb.setTables(TABLE_TRACKS);
                qb.setProjectionMap(Projections.sTracksProjection);
                sortOrder = TracksColumns.TRACK + " ASC";
                break;
            }
            case TRACKS_VISIBLE: {
                qb.setTables(TABLE_TRACKS);
                qb.setProjectionMap(Projections.sTracksProjection);
                qb.appendWhere(TracksColumns.VISIBLE + "=1");
                sortOrder = TracksColumns.TRACK + " ASC";
                break;
            }
            case TRACKS_SESSIONS: {
                qb.setTables(TABLE_SESSIONS_JOIN_TRACKS_BLOCKS);
                qb.setProjectionMap(Projections.sSessionsProjection);
                qb.appendWhere("tracks._id=" + uri.getPathSegments().get(1));
                //sortOrder = TracksColumns.TRACK + " ASC, " + BlocksColumns.TIME_START + " ASC";
                sortOrder = BlocksColumns.TIME_START + " ASC";
                break;
            }
            case BLOCKS: {
                qb.setTables(TABLE_BLOCKS);
                qb.setProjectionMap(Projections.sBlocksProjection);
                sortOrder = BlocksColumns.TIME_START + " ASC";
                notificationUri = Blocks.CONTENT_URI;
                break;
            }
            case BLOCKS_SESSIONS: {
                qb.setTables(TABLE_SESSIONS_JOIN_TRACKS_BLOCKS);
                qb.setProjectionMap(Projections.sSessionsProjection);
                qb.appendWhere("blocks._id=" + uri.getPathSegments().get(1));
                sortOrder = BlocksColumns.TIME_START + " ASC, " + TracksColumns.TRACK + " ASC";
                break;
            }
            case SESSIONS: {
                qb.setTables(TABLE_SESSIONS_JOIN_TRACKS_BLOCKS);
                qb.setProjectionMap(Projections.sSessionsProjection);
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

        switch (sUriMatcher.match(uri)) {

            case TRACKS_ID:
                return updateTrack(uri, values, db);
            case SESSIONS_ID:
                return updateSession(uri, values, db);
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);

        }

    }

    private int updateSession(Uri uri, ContentValues values, SQLiteDatabase db) {
       long sessionId = ContentUris.parseId(uri);

        int count = db.update(TABLE_SESSIONS, values, BaseColumns._ID + "=" + sessionId, null);

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

    private int updateTrack(Uri uri, ContentValues values, SQLiteDatabase db) {
        int count;
        long trackId = ContentUris.parseId(uri);
        count = db.update(TABLE_TRACKS, values, BaseColumns._ID + "=" + trackId, null);
        getContext().getContentResolver().notifyChange( Tracks.CONTENT_URI, null);
        return count;
    }


}
