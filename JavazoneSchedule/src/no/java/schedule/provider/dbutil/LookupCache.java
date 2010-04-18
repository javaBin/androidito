package no.java.schedule.provider.dbutil;

import android.content.ContentValues;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;
import android.util.Log;
import no.java.schedule.provider.SessionsContract;
import no.java.schedule.provider.SessionsProvider;

import java.util.Date;
import java.util.HashMap;

/**
 * Provide a lookup cache for {@link no.java.schedule.provider.SessionsContract.Tracks#_ID} and {@link no.java.schedule.provider.SessionsContract.Blocks#_ID},
 * which is useful for speeding up doing bulk inserts.
 */
public class LookupCache {

    private SQLiteStatement mTrackQuery;
    private SQLiteStatement mBlockQuery;
    private SQLiteStatement mTrackInsert;
    private SQLiteStatement mBlockInsert;

    private HashMap<Integer, Long> mTrackCache = new HashMap<Integer, Long>();
    private HashMap<Integer, Long> mBlockCache = new HashMap<Integer, Long>();

    public LookupCache(SQLiteDatabase db) {
        // Prepare lookup query and insert statements
        mTrackQuery = db.compileStatement("SELECT " + BaseColumns._ID + " FROM " + SessionsProvider.TABLE_TRACKS
                + " WHERE " + SessionsContract.TracksColumns.TRACK + "=?");
        mBlockQuery = db.compileStatement("SELECT " + BaseColumns._ID + " FROM " + SessionsProvider.TABLE_BLOCKS
                + " WHERE " + SessionsContract.BlocksColumns.TIME_START + "=? AND " + SessionsContract.BlocksColumns.TIME_END
                + "=?");

        mTrackInsert = db.compileStatement("INSERT INTO " + SessionsProvider.TABLE_TRACKS + " VALUES (NULL,?,10526880,1)");
        mBlockInsert = db.compileStatement("INSERT INTO " + SessionsProvider.TABLE_BLOCKS + " VALUES (NULL,?,?)");
    }

    /**
     * Fill a valid {@link no.java.schedule.provider.SessionsContract.Tracks#_ID} for the provided
     * {@link android.content.ContentValues}, querying or creating as needed.
     */
    public synchronized void fillTrackId(ContentValues incoming) {
        if (incoming.containsKey(SessionsContract.SessionsColumns.TRACK_ID)) return;
        String[] values = new String[] {
            incoming.getAsString(SessionsContract.TracksColumns.TRACK),
        };
        long trackId = getCachedId(mTrackQuery, mTrackInsert, values, mTrackCache);

        incoming.put(SessionsContract.SessionsColumns.TRACK_ID, trackId);
        incoming.remove(SessionsContract.TracksColumns.TRACK);
    }

    /**
     * Fill a valid {@link no.java.schedule.provider.SessionsContract.Blocks#_ID} for the provided
     * {@link android.content.ContentValues}, querying or creating as needed.
     */
    public synchronized void fillBlockId(ContentValues incoming) {
        if (incoming.containsKey(SessionsContract.SessionsColumns.BLOCK_ID)) return;

        final Long startTime = incoming.getAsLong(SessionsContract.BlocksColumns.TIME_START);
        final Long endTime = incoming.getAsLong(SessionsContract.BlocksColumns.TIME_END);

        Long[] values = new Long[]{
                startTime,
                endTime,
        };

        Log.d("javaBinSchedule", String.format("Looking up slot %s - %s", new Date(startTime), new Date(endTime)));
        long blockId = getCachedId(mBlockQuery, mBlockInsert, values, mBlockCache);

        incoming.put(SessionsContract.SessionsColumns.BLOCK_ID, blockId);
        incoming.remove(SessionsContract.BlocksColumns.TIME_START);
        incoming.remove(SessionsContract.BlocksColumns.TIME_END);
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
