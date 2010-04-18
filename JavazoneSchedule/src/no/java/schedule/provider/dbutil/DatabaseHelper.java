package no.java.schedule.provider.dbutil;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import no.java.schedule.provider.SessionsContract;
import no.java.schedule.provider.SessionsProvider;

/**
 * Helper to manage upgrading between versions of the database.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "sessions.db";

    private static final int DATABASE_VERSION = 5;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + SessionsProvider.TABLE_TRACKS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SessionsContract.TracksColumns.TRACK + " TEXT,"
                + SessionsContract.TracksColumns.COLOR + " INTEGER,"
                + SessionsContract.TracksColumns.VISIBLE + " INTEGER);");

        db.execSQL("CREATE TABLE " + SessionsProvider.TABLE_BLOCKS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SessionsContract.BlocksColumns.TIME_START + " INTEGER,"
                + SessionsContract.BlocksColumns.TIME_END + " INTEGER);");

        db.execSQL("CREATE TABLE " + SessionsProvider.TABLE_SESSIONS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SessionsContract.SessionsColumns.TRACK_ID + " INTEGER REFERENCES tracks(_id),"
                + SessionsContract.SessionsColumns.BLOCK_ID + " INTEGER REFERENCES blocks(_id),"
                + SessionsContract.SessionsColumns.TITLE + " TEXT,"
                + SessionsContract.SessionsColumns.SPEAKER_NAMES + " TEXT,"
                + SessionsContract.SessionsColumns.ABSTRACT + " TEXT,"
                + SessionsContract.SessionsColumns.ROOM + " INTEGER,"
                + SessionsContract.SessionsColumns.TYPE + " TEXT,"
                + SessionsContract.SessionsColumns.TAGS + " TEXT,"
                + SessionsContract.SessionsColumns.LINK + " TEXT,"
                + SessionsContract.SessionsColumns.LINK_ALT + " TEXT,"
                + SessionsContract.SessionsColumns.STARRED + " INTEGER);");

        db.execSQL("CREATE VIRTUAL TABLE " + SessionsProvider.TABLE_SEARCH + " USING FTS1("
                + SessionsContract.SearchColumns.INDEX_TEXT + ");");

        db.execSQL("CREATE TABLE " + SessionsProvider.TABLE_SUGGEST + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY,"
                + SessionsContract.SuggestColumns.DISPLAY1 + " TEXT);");

        // ??? speakers integration
        db.execSQL("CREATE TABLE " + SessionsProvider.TABLE_SPEAKERS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SessionsContract.SpeakersColumns.SPEAKERNAME + " TEXT,"
                + SessionsContract.SpeakersColumns.SPEAKERBIO + " TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int version = oldVersion;

        if (version < 5) {
            db.execSQL(SessionsContract.UpdateDatabaseSQL.V5_UPDATE_SESSIONS);
            db.execSQL(SessionsContract.UpdateDatabaseSQL.V5_UPDATE_SPEAKERS);
        }
    }
}
