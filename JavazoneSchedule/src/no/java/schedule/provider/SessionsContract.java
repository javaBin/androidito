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
    
    public static interface BlocksColumns {
        /** Exact start time of session as UTC timestamp */
        public static final String TIME_START = "timestart";
        /** Exact end time of session as UTC timestamp */
        public static final String TIME_END = "timeend";
        
    }

    public static interface TracksColumns {
        /** Name of this track */
        public static final String TRACK = "track";
        /** Color of this track */
        public static final String COLOR = "color";
        /** Flag if this track is visible to user */
        public static final String VISIBLE = "visible";
        
    }
    
    public static interface SessionsColumns {
        /** {@link Tracks#_ID} this session belongs to */
        public static final String TRACK_ID = "track_id";
        /** {@link Blocks#_ID} this session belongs to */
        public static final String BLOCK_ID = "block_id";
        
        /** Title of this session */
        public static final String TITLE = "title";
        /** List of all speakers for this session */
        public static final String SPEAKER_NAMES = "speaker_names";
        /** Body text of session abstract */
        public static final String ABSTRACT = "abstract";
        /** Name of room this session takes place in */
        public static final String ROOM = "room";
        
        /** Type of session, such as its technical level */
        public static final String TYPE = "type";
        /** Tags or keywords associated with this session */
        public static final String TAGS = "tags";

        /** Link to online details about this session */
        public static final String LINK = "link";
        /** Link to alternate details, such as a Moderator page */
        public static final String LINK_ALT = "linkalt";

        /** Flag if this session has been starred by user */
        public static final String STARRED = "starred";
        
    }
    
    public static interface SpeakersColumns {
    	/** Name of this speaker */
    	public static final String SPEAKERNAME = "speakername";
    	/** Bio of this speaker */
    	public static final String SPEAKERBIO = "speakerbio";
    }
    
    public static interface SearchColumns {
        /** Text to be used when building search index */
        public static final String INDEX_TEXT = "indextext";
        /** Snippet text that was matched by a search */
        public static final String SNIPPET = "snippet";
        /** {@link SessionColumns#_ID} that this search entry belongs to */
        public static final String SESSION_ID = "session_id";
        
    }
    
    public static interface SuggestColumns {
        /** Suggestion text to present user */
        public static final String DISPLAY1 = "display1";
        
    }
    
    
    public static class Blocks implements BaseColumns, BlocksColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/blocks/");
        
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/net.maelbrancke.filip.devoxx.block";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/net.maelbrancke.filip.devoxx.block";
        
    }

    public static class Tracks implements BaseColumns, TracksColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/tracks/");
        public static final Uri CONTENT_VISIBLE_URI = Uri.withAppendedPath(CONTENT_URI, "visible");
        
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/net.maelbrancke.filip.devoxx.track";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/net.maelbrancke.filip.devoxx.track";
        
    }
    
    public static class Sessions implements BaseColumns, SessionsColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/sessions/");
        public static final Uri CONTENT_SEARCH_URI = Uri.parse("content://" + AUTHORITY + "/sessions/search/");

        /** The directory twig for session sub-tables */
        public static final String CONTENT_DIRECTORY = "sessions";
        
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/net.maelbrancke.filip.devoxx.session";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/net.maelbrancke.filip.devoxx.session";
        
    }
    
    
    public static class Suggest implements BaseColumns, SuggestColumns {
        /** Search keyword suggestions */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/search_suggest_query/");
        
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/suggestion";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/suggestion";
        
    }
    
    public static class Speakers implements BaseColumns, SpeakersColumns {
    	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/speakers/");
    	
    	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/net.maelbrancke.filip.devoxx.speaker";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/net.maelbrancke.filip.devoxx.speaker";
    }
    
    public static class UpdateDatabaseSQL {
    	public static final String lineSeparator = System.getProperty("line.separator");
    	
    	public static final String V5_UPDATE_SESSIONS = "UPDATE sessions SET title= \"Do you really get class loaders?\", speaker_names=\"Jevgeni Kabanov\", abstract =\"Class loaders are at the core of the Java language. Java EE containers, OSGi, NetBeans modules, Tapestry 5, Grails and many others use class loaders heavily. Yet when something goes wrong, would you know how to solve it?" + lineSeparator + lineSeparator + "In this session we'll take a tour of the Java class loading mechanism, both from JVM and developer point of view. We'll see how different delegation systems are built, how synchronization works, what is the difference between finding classes and resources, what wrong assumptions has been made and are now supported." + lineSeparator + lineSeparator + "Next we will look at typical problems that you get with class loading and how to solve them. ClassNoDefError, IncompatibleClassChangeError, LinkageError and many others are symptoms of specific things going wrong that you can usually find and fix. For each problem we'll go through a hands on demo with a corresponding solution." + lineSeparator + lineSeparator + "Finally we'll take a look at the complicated class loading mechanisms like the ones used in OSGi and Tapestry 5. We'll look in detail at the benefits they have and problems they might cause.\", tags=\"Java SE\" WHERE _id = 77";
    	
    	public static final String V5_UPDATE_SPEAKERS = "INSERT INTO speakers(speakerName, speakerbio) VALUES (\"Jevgeni Kabanov\",\"Jevgeni Kabanov is the founder and CTO of ZeroTurnaround, a development tools company that focuses on productivity. Before that he worked as the R&D director of Webmedia, Ltd, the largest custom software development company in the Baltics." + lineSeparator + "As part of the effort to reduce development time tunraround he wrote the prototype of the ZeroTurnaround flagship product, JavaRebel, a class reloading JVM plugin." + lineSeparator + "Jevgeni has been speaking at international conferences for several years, including JavaPolis/Devoxx, JavaZone, JAOO and so on. He also has an active research interest, publishing several papers on topics ranging from category theoretical notions to typesafe Java DSLs." + lineSeparator + "Besides the commercial products made for ZeroTurnaround, Jevgeni is a co-founder of two open-source projects � Aranea and Squill. [Aranea http://www.araneaframework.org] is a web development and integration platform based on strong object-oriented principles. Squill is a typesafe internal DSL for constructing and executing SQL queries." + lineSeparator + lineSeparator + "Jevgeni's personal blog can be found at http://dow.ngra.de.\");";
    }
}
