package no.java.schedule.provider;

import android.content.UriMatcher;

import static no.java.schedule.provider.SessionsContract.AUTHORITY;
import static no.java.schedule.provider.SessionsProvider.e.*;

class SessionUriMatcher extends UriMatcher {

    public SessionUriMatcher() {
        super(NO_MATCH);


    this.addURI(AUTHORITY, "tracks",                TRACKS.ordinal());
    this.addURI(AUTHORITY, "tracks/#",              TRACKS_ID.ordinal());
    this.addURI(AUTHORITY, "tracks/visible",        TRACKS_VISIBLE.ordinal());
    this.addURI(AUTHORITY, "tracks/#/sessions",     TRACKS_SESSIONS.ordinal());

    this.addURI(AUTHORITY, "blocks",                BLOCKS.ordinal());
    this.addURI(AUTHORITY, "blocks/#/sessions",     BLOCKS_SESSIONS.ordinal());

    this.addURI(AUTHORITY, "sessions",              SESSIONS.ordinal());
    this.addURI(AUTHORITY, "sessions/#",            SESSIONS_ID.ordinal());
    this.addURI(AUTHORITY, "sessions/search/*",     SESSIONS_SEARCH.ordinal());

    this.addURI(AUTHORITY, "search_suggest_query",  SUGGEST.ordinal());

    this.addURI(AUTHORITY, "speakers",              SPEAKERS.ordinal());
    this.addURI(AUTHORITY, "speakers/search/*",     SPEAKERS_SEARCH.ordinal());

    }

}
