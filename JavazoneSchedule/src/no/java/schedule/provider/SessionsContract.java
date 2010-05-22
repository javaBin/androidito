/*
 * Copyright (C) 2009 Virgil Dobjanschi, Jeff Sharkey
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

import android.net.Uri;
import android.provider.BaseColumns;

public class SessionsContract {
    
    public static final String AUTHORITY = "no.java.schedule";
    public static final String TYPE_PRESENTATION = "Presentation";

    public static interface BlocksColumns {

        public static final String TIME_START = "timestart";
        public static final String TIME_END = "timeend";
    }

    public static interface TracksColumns {

        public static final String TRACK = "track";
        public static final String COLOR = "color";
        public static final String VISIBLE = "visible";
    }
    
    public static interface SessionsColumns {

        public static final String TRACK_ID = "track_id";
        public static final String BLOCK_ID = "block_id";
        public static final String TITLE = "title";
        public static final String SPEAKER_NAMES = "speaker_names";
        public static final String ABSTRACT = "abstract";
        public static final String ROOM = "room";
        public static final String TYPE = "type";
        public static final String TAGS = "tags";
        public static final String WEB_LINK = "link";
        public static final String WEB_LINK_ALT = "linkalt";
        public static final String STARRED = "starred";
    }
    
    public static interface SpeakersColumns {

    	public static final String SPEAKERNAME = "speakername";
    	public static final String SPEAKERBIO = "speakerbio";
    }
    
    public static interface SearchColumns {

        public static final String INDEX_TEXT = "indextext";
        public static final String SNIPPET = "snippet";
        public static final String SESSION_ID = "session_id";
    }
    
    public static interface SuggestColumns {

        public static final String DISPLAY = "display1";

    }
    
    
    public static class Blocks implements BaseColumns, BlocksColumns {

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/blocks/");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/no.java.schedule.block";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/no.java.schedule.block";
    }

    public static class Tracks implements BaseColumns, TracksColumns {

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/tracks/");
        public static final Uri CONTENT_VISIBLE_URI = Uri.withAppendedPath(CONTENT_URI, "visible");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/no.java.schedule.track";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/no.java.schedule.track";
    }
    
    public static class Sessions implements BaseColumns, SessionsColumns {

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/sessions/");
        public static final Uri CONTENT_SEARCH_URI = Uri.parse("content://" + AUTHORITY + "/sessions/search/");
        public static final String CONTENT_DIRECTORY = "sessions";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/no.java.schedule.session";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/no.java.schedule.session";
    }
    
    
    public static class SearchKeywordSuggest implements BaseColumns, SuggestColumns {

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/search_suggest_query/");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/suggestion";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/suggestion";
        
    }
    
    public static class Speakers implements BaseColumns, SpeakersColumns {
    	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/speakers/");
    	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/no.java.schedule.speaker";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/no.java.schedule.speaker";
    }
    
   
}
