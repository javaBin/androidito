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

/**
 * Provide a lookup cache for {@link no.java.schedule.provider.SessionsContract.Tracks#_ID} and {@link no.java.schedule.provider.SessionsContract.Blocks#_ID},
 * which is useful for speeding up doing bulk inserts.
 */
public class LookupCache {

    private SQLiteStatement mTrackQuery;
    private SQLiteStatement mBlockQuery;
    private SQLiteStatement mTrackInsert;
    private SQLiteStatement mBlockInsert;

    private static final String TRACK_QUERY = ""+
            " SELECT " + BaseColumns._ID +
            " FROM   " + SessionsProvider.TABLE_TRACKS +
            " WHERE  " + SessionsContract.TracksColumns.TRACK + "=?";

    private static final String BLOCK_QUERY = ""+
            " SELECT " + BaseColumns._ID +
            " FROM   " + SessionsProvider.TABLE_BLOCKS+
            " WHERE  " + SessionsContract.BlocksColumns.TIME_START + "=?" +
            " AND    " + SessionsContract.BlocksColumns.TIME_END + "=?";

    private static final String TRACK_INSERT = ""+
            " INSERT INTO " + SessionsProvider.TABLE_TRACKS +
            " VALUES (NULL,?,10526880,1)";

    private static final String BLOCK_INSERT = "" +
            " INSERT INTO " + SessionsProvider.TABLE_BLOCKS +
            " VALUES (NULL,?,?)";

    public LookupCache(SQLiteDatabase db) {
        // Prepare lookup query and insert statements
        mTrackQuery = db.compileStatement(TRACK_QUERY);
        mBlockQuery = db.compileStatement(BLOCK_QUERY);

        mTrackInsert = db.compileStatement(TRACK_INSERT);
        mBlockInsert = db.compileStatement(BLOCK_INSERT);
    }

    /**
     * Fill a valid {@link no.java.schedule.provider.SessionsContract.Tracks#_ID} for the provided
     * {@link android.content.ContentValues}, querying or creating as needed.
     */
    public synchronized void fillTrackId(ContentValues incoming) {
        if (!incoming.containsKey(SessionsContract.SessionsColumns.TRACK_ID)){

            String[] values = new String[] { incoming.getAsString(SessionsContract.TracksColumns.TRACK)};
            long trackId = geRecordId(mTrackQuery, mTrackInsert, values);

            incoming.put(SessionsContract.SessionsColumns.TRACK_ID, trackId);
            incoming.remove(SessionsContract.TracksColumns.TRACK);
        }
    }

    /**
     * Fill a valid {@link no.java.schedule.provider.SessionsContract.Blocks#_ID} for the provided
     * {@link android.content.ContentValues}, querying or creating as needed.
     */
    public synchronized void fillBlockId(ContentValues incoming) {
        if (!incoming.containsKey(SessionsContract.SessionsColumns.BLOCK_ID)) {

            final Long startTime = incoming.getAsLong(SessionsContract.BlocksColumns.TIME_START);
            final Long endTime = incoming.getAsLong(SessionsContract.BlocksColumns.TIME_END);

            Long[] values = new Long[]{ startTime, endTime };

            Log.d("javaBinSchedule", String.format("Looking up slot %s - %s", new Date(startTime), new Date(endTime)));

            long blockId = geRecordId(mBlockQuery, mBlockInsert, values);

            incoming.put(SessionsContract.SessionsColumns.BLOCK_ID, blockId);
            incoming.remove(SessionsContract.BlocksColumns.TIME_START);
            incoming.remove(SessionsContract.BlocksColumns.TIME_END);
        }
    }

    private synchronized long geRecordId(SQLiteStatement query, SQLiteStatement insert, Object[] values) {


        try {
            // Try searching database for mapping
            bindValues(query, values);

            return query.simpleQueryForLong();

        } catch (SQLiteDoneException e) {
            // Nothing found, so try inserting new mapping
            bindValues(insert, values);
            final long result = insert.executeInsert();

            if (result != -1){
                return result;
            } else {
                throw new IllegalStateException("Couldn't find or create internal mapping");
            }
        }

    }

    private void bindValues(SQLiteStatement query, Object[] values) {
        for (int i = 0; i < values.length; i++){
            DatabaseUtils.bindObjectToProgram(query, i + 1, values[i]);
        }
    }
}
