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

package no.java.schedule.activities.tabs;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import no.java.schedule.R;
import no.java.schedule.activities.fullscreen.SessionDetailsActivity;
import no.java.schedule.provider.SessionsContract.SearchColumns;
import no.java.schedule.provider.SessionsContract.Sessions;
import no.java.schedule.provider.SessionsContract.SessionsColumns;
import no.java.schedule.provider.SessionsContract.TracksColumns;
import no.java.schedule.provider.SessionsProvider;

/**
 * Activity to show search results as requested by {@link SearchManager}.
 */
public class SearchActivity extends ListActivity {
    // private static final String TAG = "SearchActivity";

    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.titled_session_view);
        onNewIntent(getIntent());

    }
    
    /** {@inheritDoc} */
    @Override
    public void onNewIntent(Intent intent) {
        // Pull search query string from extras
        final String query = intent.getStringExtra(SearchManager.QUERY);
        // Set the title
        ((TextView)findViewById(R.id.title_text)).setText(getString(R.string.title_search, query));
        //setTitle( getString(R.string.title_search, query));
        
        // Push off search query to backend provider
        Uri searchUri = Uri.withAppendedPath(Sessions.CONTENT_SEARCH_URI, Uri.encode(query));
        Cursor cursor = this.managedQuery(searchUri, Projections.PROJ_SESSIONS, null, null, null);

        // Prepare list adapter from cursor of results
        ListAdapter adapter = new SearchSessionsAdapter(this, cursor);
        this.setListAdapter(adapter);
        
    }

    /** {@inheritDoc} */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        // Start details activity for selected item
        Uri sessionUri = ContentUris.withAppendedId(Sessions.CONTENT_URI, id);
        Intent intent = new Intent(this, SessionDetailsActivity.class);
        intent.setData(sessionUri);
        startActivity(intent);
    }

    /**
     * List adapter that shows {@link Sessions#CONTENT_SEARCH_URI} search
     * results, including formatting {@link SearchColumns#SNIPPET} snippets in
     * bold where marked.
     */
    private static class SearchSessionsAdapter extends ResourceCursorAdapter {
        private static final int ROW_RESOURCE = R.layout.session_row_snippet;

        public SearchSessionsAdapter(Context context, Cursor c) {
            super(context, ROW_RESOURCE, c);
        }

        /**
         * Holder for an inflated view.
         */
        private static class ChildHolder {
            View color;
            TextView title;
            TextView snippet;
            long sessionId;
            long trackId;
            long blockId;
        }

        /** {@inheritDoc} */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View view = super.newView(context, cursor, parent);

            ChildHolder holder = new ChildHolder();
            holder.color = view.findViewById(R.id.session_color);
            holder.title = (TextView)view.findViewById(R.id.session_title);
            holder.snippet = (TextView)view.findViewById(R.id.snippet);

            view.setTag(holder);
            return view;
        }

        /** {@inheritDoc} */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ChildHolder holder = (ChildHolder)view.getTag();

            holder.color.setBackgroundColor(cursor.getInt(Projections.COL_COLOR) | 0xff000000);
            holder.title.setText(cursor.getString(Projections.COL_TITLE));

            // Format snippet text
            final String snippet = cursor.getString(Projections.COL_SNIPPET);
            Spannable styledSnippet = buildStyledSnippet(snippet);
            holder.snippet.setText(styledSnippet);

            holder.sessionId = cursor.getInt(Projections.COL_ID);
            holder.trackId = cursor.getInt(Projections.COL_TRACK_ID);
            holder.blockId = cursor.getInt(Projections.COL_BLOCK_ID);
        }

        private StyleSpan mBoldSpan = new StyleSpan(android.graphics.Typeface.BOLD);

        /**
         * Given a snippet string with matching segments surrounded by curly
         * braces, turn those areas into bold spans, removing the curly braces.
         */
        private Spannable buildStyledSnippet(String snippet) {
            SpannableStringBuilder builder = new SpannableStringBuilder(snippet);

            // Walk through string, inserting bold snippet spans
            int startIndex = -1, endIndex = -1, delta = 0;
            while ((startIndex = snippet.indexOf('{', endIndex)) != -1) {
                endIndex = snippet.indexOf('}', startIndex);

                // Remove braces from both sides
                builder.delete(startIndex - delta, startIndex - delta + 1);
                builder.delete(endIndex - delta - 1, endIndex - delta);

                // Insert bold style
                builder.setSpan(mBoldSpan, startIndex - delta, endIndex - delta - 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                delta += 2;
            }

            return builder;
        }
    }

    /**
     * Various projections and static columns for querying into
     * {@link SessionsProvider} tables.
     */
    public static interface Projections {
        public static final String[] PROJ_SESSIONS = new String[] {
                BaseColumns._ID, SessionsColumns.TRACK_ID, TracksColumns.COLOR,
                SessionsColumns.BLOCK_ID, SessionsColumns.TITLE, SessionsColumns.STARRED,
                SearchColumns.SNIPPET,
        };

        public static final int COL_ID = 0;
        public static final int COL_TRACK_ID = 1;
        public static final int COL_COLOR = 2;
        public static final int COL_BLOCK_ID = 3;
        public static final int COL_TITLE = 4;
        public static final int COL_STARRED = 5;
        public static final int COL_SNIPPET = 6;
    }

}
