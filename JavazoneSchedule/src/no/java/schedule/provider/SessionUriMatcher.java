package no.java.schedule.provider;

import android.content.UriMatcher;

class SessionUriMatcher extends UriMatcher {

    public SessionUriMatcher() {
        super(NO_MATCH);


    this.addURI(SessionsContract.AUTHORITY, "tracks", SessionsProvider.TRACKS);
    this.addURI(SessionsContract.AUTHORITY, "tracks/#", SessionsProvider.TRACKS_ID);
    this.addURI(SessionsContract.AUTHORITY, "tracks/visible", SessionsProvider.TRACKS_VISIBLE);
    this.addURI(SessionsContract.AUTHORITY, "tracks/#/sessions", SessionsProvider.TRACKS_SESSIONS);

    this.addURI(SessionsContract.AUTHORITY, "blocks", SessionsProvider.BLOCKS);
    this.addURI(SessionsContract.AUTHORITY, "blocks/#/sessions", SessionsProvider.BLOCKS_SESSIONS);

    this.addURI(SessionsContract.AUTHORITY, "sessions", SessionsProvider.SESSIONS);
    this.addURI(SessionsContract.AUTHORITY, "sessions/#", SessionsProvider.SESSIONS_ID);
    this.addURI(SessionsContract.AUTHORITY, "sessions/search/*", SessionsProvider.SESSIONS_SEARCH);

    this.addURI(SessionsContract.AUTHORITY, "search_suggest_query", SessionsProvider.SUGGEST);

    this.addURI(SessionsContract.AUTHORITY, "speakers", SessionsProvider.SPEAKERS);
    this.addURI(SessionsContract.AUTHORITY, "speakers/search/*", SessionsProvider.SPEAKERS_SEARCH);

    }

}
