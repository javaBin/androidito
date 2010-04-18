package no.java.schedule.provider;

import android.app.SearchManager;
import android.provider.BaseColumns;

import java.util.HashMap;

public class Projections {
    public static final HashMap<String, String> sTracksProjection = createTracksProjection();
    public static final HashMap<String, String> sBlocksProjection = createBlocksProjection();
    public static final HashMap<String, String> sSessionsProjection = createSessionsProjection();
    static final String SNIPPET_SQL = "snippet(" + SessionsProvider.TABLE_SEARCH
            + ", '{', '}', '\u2026') AS " + SessionsContract.SearchColumns.SNIPPET;

    static HashMap<String, String> createSpeakerProjection() {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(BaseColumns._ID, BaseColumns._ID);
                map.put(SessionsContract.SpeakersColumns.SPEAKERNAME, SessionsContract.SpeakersColumns.SPEAKERNAME);
                map.put(SessionsContract.SpeakersColumns.SPEAKERBIO, SessionsContract.SpeakersColumns.SPEAKERBIO);
                return map;
            }

    static HashMap<String, String> createSuggestProjection() {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(BaseColumns._ID, BaseColumns._ID);
                map.put(SearchManager.SUGGEST_COLUMN_TEXT_1, SessionsContract.SuggestColumns.DISPLAY1 + " AS "
                        + SearchManager.SUGGEST_COLUMN_TEXT_1);
                map.put(SearchManager.SUGGEST_COLUMN_QUERY, SessionsContract.SuggestColumns.DISPLAY1 + " AS "
                        + SearchManager.SUGGEST_COLUMN_QUERY);
                return map;
            }

    static HashMap<String, String> createSearchProjection() {
                // Projection for searches
                HashMap<String, String> map = new HashMap<String, String>();
                map.putAll(sSessionsProjection);
                map.put(SessionsContract.SearchColumns.SNIPPET, SNIPPET_SQL);
                return map;
            }

    static HashMap<String, String> createSessionsProjection() {
                HashMap<String, String> map = new HashMap<String, String>();
                map.putAll(sTracksProjection);
                map.putAll(sBlocksProjection);
                map.put(BaseColumns._ID, "sessions._id as _id");
                map.put(SessionsContract.SessionsColumns.TRACK_ID, SessionsContract.SessionsColumns.TRACK_ID);
                map.put(SessionsContract.SessionsColumns.BLOCK_ID, SessionsContract.SessionsColumns.BLOCK_ID);
                map.put(SessionsContract.SessionsColumns.TITLE, SessionsContract.SessionsColumns.TITLE);
                map.put(SessionsContract.SessionsColumns.SPEAKER_NAMES, SessionsContract.SessionsColumns.SPEAKER_NAMES);
                map.put(SessionsContract.SessionsColumns.ABSTRACT, SessionsContract.SessionsColumns.ABSTRACT);
                map.put(SessionsContract.SessionsColumns.ROOM, SessionsContract.SessionsColumns.ROOM);
                map.put(SessionsContract.SessionsColumns.TYPE, SessionsContract.SessionsColumns.TYPE);
                map.put(SessionsContract.SessionsColumns.TAGS, SessionsContract.SessionsColumns.TAGS);
                map.put(SessionsContract.SessionsColumns.LINK, SessionsContract.SessionsColumns.LINK);
                map.put(SessionsContract.SessionsColumns.LINK_ALT, SessionsContract.SessionsColumns.LINK_ALT);
                map.put(SessionsContract.SessionsColumns.STARRED, SessionsContract.SessionsColumns.STARRED);
                map.put(SessionsContract.TracksColumns.COLOR, SessionsContract.TracksColumns.COLOR);

                return map;
            }

    static HashMap<String, String> createBlocksProjection() {

                // Projection for blocks
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(BaseColumns._ID, BaseColumns._ID);
                map.put(SessionsContract.BlocksColumns.TIME_START, SessionsContract.BlocksColumns.TIME_START);
                map.put(SessionsContract.BlocksColumns.TIME_END, SessionsContract.BlocksColumns.TIME_END);
                return map;
            }

    static HashMap<String, String> createTracksProjection() {
        // Projection for tracks
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(BaseColumns._ID, BaseColumns._ID);
                map.put(SessionsContract.TracksColumns.TRACK, SessionsContract.TracksColumns.TRACK);
                map.put(SessionsContract.TracksColumns.COLOR, SessionsContract.TracksColumns.COLOR);
                map.put(SessionsContract.TracksColumns.VISIBLE, SessionsContract.TracksColumns.VISIBLE);
                return map;
            }
}
