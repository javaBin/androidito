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
    
    public static class UpdateDatabaseSQL {
    	public static final String lineSeparator = System.getProperty("line.separator");
    	public static final String V5_UPDATE_SESSIONS = "UPDATE sessions SET title= \"Do you really get class loaders?\", speaker_names=\"Jevgeni Kabanov\", abstract =\"Class loaders are at the core of the Java language. Java EE containers, OSGi, NetBeans modules, Tapestry 5, Grails and many others use class loaders heavily. Yet when something goes wrong, would you know how to solve it?" + lineSeparator + lineSeparator + "In this session we'll take a tour of the Java class loading mechanism, both from JVM and developer point of view. We'll see how different delegation systems are built, how synchronization works, what is the difference between finding classes and resources, what wrong assumptions has been made and are now supported." + lineSeparator + lineSeparator + "Next we will look at typical problems that you get with class loading and how to solve them. ClassNoDefError, IncompatibleClassChangeError, LinkageError and many others are symptoms of specific things going wrong that you can usually find and fix. For each problem we'll go through a hands on demo with a corresponding solution." + lineSeparator + lineSeparator + "Finally we'll take a look at the complicated class loading mechanisms like the ones used in OSGi and Tapestry 5. We'll look in detail at the benefits they have and problems they might cause.\", tags=\"Java SE\" WHERE _id = 77";
    	public static final String V5_UPDATE_SPEAKERS = "INSERT INTO speakers(speakerName, speakerbio) VALUES (\"Jevgeni Kabanov\",\"Jevgeni Kabanov is the founder and CTO of ZeroTurnaround, a development tools company that focuses on productivity. Before that he worked as the R&D director of Webmedia, Ltd, the largest custom software development company in the Baltics." + lineSeparator + "As part of the effort to reduce development time tunraround he wrote the prototype of the ZeroTurnaround flagship product, JavaRebel, a class reloading JVM plugin." + lineSeparator + "Jevgeni has been speaking at international conferences for several years, including JavaPolis/Devoxx, JavaZone, JAOO and so on. He also has an active research interest, publishing several papers on topics ranging from category theoretical notions to typesafe Java DSLs." + lineSeparator + "Besides the commercial products made for ZeroTurnaround, Jevgeni is a co-founder of two open-source projects � Aranea and Squill. [Aranea http://www.araneaframework.org] is a web development and integration platform based on strong object-oriented principles. Squill is a typesafe internal DSL for constructing and executing SQL queries." + lineSeparator + lineSeparator + "Jevgeni's personal blog can be found at http://dow.ngra.de.\");";
    }
}
