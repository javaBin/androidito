/*
 * Copyright (C) 2009 Virgil Dobjanschi, Jeff Sharkey, Filip Maelbrancke
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

package net.maelbrancke.filip.devoxx.schedule;

import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.TimeZone;

import net.maelbrancke.filip.devoxx.schedule.provider.SessionsContract.BlocksColumns;
import net.maelbrancke.filip.devoxx.schedule.provider.SessionsContract.Sessions;
import net.maelbrancke.filip.devoxx.schedule.provider.SessionsContract.Tracks;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

/**
 * An activity which displays an expandable list
 */
public class SessionsExpandableListActivity extends ExpandableListActivity {
    //private static final String TAG = "SessionsExpandableListActivity";

    public static final String EXTRA_CHILD_MODE = "childmode";
    public static final String EXTRA_SELECTION = "selection";
    public static final String EXTRA_SELECTION_ARGS = "selection_args";

    public static final String STATE_EXPANDED = "expanded";

    public static final int CHILD_MODE_ALL = 0x0;
    public static final int CHILD_MODE_STARRED = 0x1;
    public static final int CHILD_MODE_VISIBLE_TRACKS = 0x2;
    public static final int CHILD_MODE_PICK = 0x3;

    private ExpandableSessionsAdapter m_adapter;
    private String m_prefsKey;

    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expandable_list);

        final Intent intent = getIntent();
        int childMode = intent.getIntExtra(EXTRA_CHILD_MODE, CHILD_MODE_ALL);

        Uri uri = Sessions.CONTENT_URI;
        String selection = null;
        String[] selectionArgs = null;
        int mode = SessionsAdapter.MODE_SCHEDULE;
        switch (childMode) {
            case CHILD_MODE_STARRED: {
                selection = Sessions.STARRED + "=?";
                selectionArgs = new String[] {"1"};
                mode = SessionsAdapter.MODE_STARRED;
                break;
            }

            case CHILD_MODE_VISIBLE_TRACKS: {
                selection = Tracks.VISIBLE + "=?";
                selectionArgs = new String[] {"1"};
                break;
            }

            case CHILD_MODE_PICK: {
                selection = intent.getStringExtra(EXTRA_SELECTION);
                selectionArgs = intent.getStringArrayExtra(EXTRA_SELECTION_ARGS);
                break;
            }

            default: {
                break;
            }
        }
        m_prefsKey = ""+mode;

        m_adapter = new ExpandableSessionsAdapter(this, uri, selection, selectionArgs, null, mode, new ExpandableSessionsAdapter.ExpandableAdapterListener(){
            /** {@inheritDoc} */
            public void onNewData() {
                ExpandableListView elv = getExpandableListView();
                int size = m_adapter.getGroupCount();
                for( int i=0; i<size; i++) {
                    elv.expandGroup(i);
                }
            }
        });
        ExpandableListView elv = getExpandableListView();
        elv.setAdapter(m_adapter);
        elv.setChildDivider( getResources().getDrawable( android.R.drawable.divider_horizontal_bright));
        // Restore the expanded groups
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String expanded = prefs.getString( STATE_EXPANDED + m_prefsKey, null);
        if( expanded != null) {
            //Log.d( "onCreate", "Loaded: " + expanded);
            StringTokenizer tok = new StringTokenizer( expanded, ",");
            while( tok.hasMoreTokens()) {
                 String t = tok.nextToken();
                 int index = Integer.parseInt( t);
                 if( index < m_adapter.getGroupCount()) {
                     elv.expandGroup( index);
                }
            }
        }
        
        // iterate through the groups, and set the group with the current time as selected (if possible)
        int objectToSelect = 0;
        int childCount = 0;
        long currentMillis = System.currentTimeMillis();
        // add one hour for GMT+1
        currentMillis += 1000 * 60 * 60;
        long currentUnixTime = currentMillis/1000L;
        long lastSessionEndTime = 0L;
        for (int i = 0; i < m_adapter.getGroupCount(); i++) {
        	Object obj = m_adapter.getGroup(i);
        	if ( obj instanceof ExpandableSessionsAdapter.Block ) {
        		elv.expandGroup(i);
        		ExpandableSessionsAdapter.Block bl = (ExpandableSessionsAdapter.Block) obj;
        		if ( (currentUnixTime > bl.getStartTime()) && (currentUnixTime < bl.getEndTime()) ) {
        			objectToSelect = childCount;
        		}
        		// also check if we're in a timeslot between sessions -> jump to this block
        		if ( (lastSessionEndTime != 0L) && (currentUnixTime > lastSessionEndTime) && (currentUnixTime < bl.getStartTime()) ) {
        			objectToSelect = childCount;
        		}
        		lastSessionEndTime = bl.getEndTime();
        	}
        	
        	childCount += m_adapter.getChildrenCount(i)+1;
        }
        elv.setSelection(objectToSelect);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m_adapter != null) {
            // Save the expanded state
            ExpandableListView elv = getExpandableListView();
            int length = m_adapter.getGroupCount();
            String expanded = "";
            for( int i=0; i < length; i++) {
                if( elv.isGroupExpanded( i)) {
                    if( expanded.length() > 0) {
                        expanded += ",";
                    }
                    expanded += i;
                }
            }
            //Log.d( "onDestroy", "Saved: " + expanded);
            SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
            prefs.putString( STATE_EXPANDED + m_prefsKey, expanded);
            prefs.commit();

            getExpandableListView().setAdapter((ExpandableListAdapter)null);
            m_adapter.close();
        }
    }

    /** {@inheritDoc} */
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Object obj = m_adapter.getChild(groupPosition, childPosition);
        if( obj instanceof SessionRenderItem)
        {
            SessionRenderItem si = (SessionRenderItem)obj;
            // Start details activity for selected item
            Intent intent = new Intent( this, SessionDetailsActivity.class);
            intent.setAction( Intent.ACTION_VIEW);
            intent.setData( si.getUri()); startActivityForResult( intent, 0);
        }
        else if( obj instanceof ExpandableSessionsAdapter.Block)
        {
            ExpandableSessionsAdapter.Block eti = (ExpandableSessionsAdapter.Block)obj;
            Intent intent = new Intent().setClass( this, SessionsListActivity.class);
            intent.setAction( Intent.ACTION_PICK);
            intent.putExtra(SessionsListActivity.EXTRA_CHILD_MODE, SessionsListActivity.CHILD_MODE_PICK);
            intent.putExtra( EXTRA_SELECTION, "(" + BlocksColumns.TIME_START + "=?) AND (" + BlocksColumns.TIME_END + "=?)");
            intent.putExtra( EXTRA_SELECTION_ARGS, new String[] { "" + eti.getStartTime(), "" + eti.getEndTime() });
            startActivityForResult( intent, 1);
        }
        return true;
    }

    public void collapseAll() {
        ExpandableListView elv = getExpandableListView();
        int size = m_adapter.getGroupCount();
        for( int i=0; i<size; i++) {
            elv.collapseGroup(i);
        }
    }

    public void expandAll() {
        ExpandableListView elv = getExpandableListView();
        int size = m_adapter.getGroupCount();
        for( int i=0; i<size; i++) {
            elv.expandGroup(i);
        }
    }

}
