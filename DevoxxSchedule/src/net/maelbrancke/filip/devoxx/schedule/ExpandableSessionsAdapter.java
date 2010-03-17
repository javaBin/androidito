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

package net.maelbrancke.filip.devoxx.schedule;

import java.util.ArrayList;
import java.util.List;

import net.maelbrancke.filip.devoxx.schedule.provider.SessionsContract.Blocks;
import net.maelbrancke.filip.devoxx.schedule.provider.SessionsContract.BlocksColumns;
import net.maelbrancke.filip.devoxx.schedule.provider.SessionsContract.SessionsColumns;
import net.maelbrancke.filip.devoxx.schedule.provider.SessionsContract.TracksColumns;
import net.maelbrancke.filip.devoxx.schedule.util.StringUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * The expandable sessions adapter
 */
public class ExpandableSessionsAdapter extends BaseExpandableListAdapter {
    // Modes
    public static final int MODE_SCHEDULE = 0;
    public static final int MODE_STARRED = 1;

    private final int m_mode;
    private final Context m_context;
    private final List<Block> m_blocks;
    private final View.OnClickListener m_starListener;
    private final Uri m_uri;
    private final String m_selection;
    private final String[] m_selectionArgs;
    private final String m_sortOrder;
    private final ExpandableAdapterListener m_listener;
    private ContentObserver m_contentObserver;

    /**
     * Constructor
     * 
     * @param context The context
     * @param uri The URI
     * @param selection The selection
     * @param selectionArgs The selection arguments
     * @param sortOrder The sortOrder
     * @param mode The mode (MODE_ALL, MODE_STARRED)
     */
    public ExpandableSessionsAdapter(Context context, Uri uri, String selection,
            String[] selectionArgs, String sortOrder, int mode, ExpandableAdapterListener listener) {
        m_context = context;
        m_blocks = new ArrayList<Block>();
        m_uri = uri;
        m_selection = selection;
        m_selectionArgs = selectionArgs;
        m_sortOrder = sortOrder;
        m_mode = mode;
        m_listener = listener;
        // Log.d( "SessionsAdapter", "Selection: " + selection + ", args: " +
        // selectionArgs + ", sort: " + sortOrder);
        buildItems();
        m_starListener = new View.OnClickListener() {
            /*
             * (non-Javadoc)
             * @see android.view.View.OnClickListener#onClick(android.view.View)
             */
            public void onClick(View v) {
                SessionRenderItem sri = (SessionRenderItem)v.getTag();
                // Log.d( "SessionsAdapter.onClick", "Item: " + sri.getUri());
                // Update the content provider
                ContentValues values = new ContentValues();
                values.put(SessionsColumns.STARRED, sri.isStarred() ? 0 : 1);
                m_context.getContentResolver().update(sri.getUri(), values, null, null);
            }
        };

        m_contentObserver = new ContentObserver(new Handler()) {
            /*
             * (non-Javadoc)
             * @see android.database.ContentObserver#onChange(boolean)
             */
            @Override
            public void onChange(boolean selfChange) {
                int beforeCount = m_blocks.size();
                //Log.d("ExpandableSessionsAdapter.onChange", "Change!");
                buildItems();
                notifyDataSetChanged();
                //Log.d("ExpandableSessionsAdapter.onChange", "Done");
                if( m_blocks.size() != beforeCount) {
                    // Expand all items
                    m_listener.onNewData();
                }
            }
        };
        m_context.getContentResolver().registerContentObserver(m_uri, true, m_contentObserver);
    }

    /** {@inheritDoc} */
    public Object getChild(int groupPosition, int childPosition) {
        Block block = m_blocks.get(groupPosition);
        if( block.hasSessions())
        {
            return block.getSession(childPosition);
        }
        else
        {
            return block;
        }
    }

    /** {@inheritDoc} */
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    /** {@inheritDoc} */
    public int getChildrenCount( int groupPosition) {
        if( groupPosition >= m_blocks.size()){
            return 0;
        }
        return m_blocks.get(groupPosition).getCount();
    }

    /*
     * (non-Javadoc)
     * @see android.widget.ExpandableListAdapter#getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)
     */
    public View getChildView( int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        Block block = m_blocks.get(groupPosition);
        boolean sessionView = block.hasSessions();
        View rv;
        if( convertView == null)
        {
            LayoutInflater vi = (LayoutInflater)m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rv = vi.inflate( R.layout.session_row, null);
        }
        else
        {
            rv = convertView;
        }
        
        if( sessionView)
        {
            //Log.d( "getChildView", "Group pos: " + groupPosition + ", child pos: " + childPosition + ", size: " + block.m_sessions.size());
            SessionRenderItem sri = block.getSession(childPosition);
            //Log.d( "getChildView", "Session: " + sri);
            rv.findViewById(R.id.session_color).setBackgroundColor(sri.getColor());
            ((TextView)rv.findViewById(R.id.session_title)).setText(sri.getTitle());
            
            CheckBox cb = ((CheckBox)rv.findViewById(R.id.session_star));
            cb.setTag( sri);
            cb.setOnClickListener(m_starListener);
            cb.setChecked(sri.isStarred());
            cb.setVisibility(View.VISIBLE);
            
            // Find and hook up larger delegate view for toggling star
            View starDelegate = rv.findViewById(R.id.star_delegate);
            Rect largeBounds = new Rect(0, 0, 1024, 1024);
            starDelegate.setTouchDelegate(new TouchDelegate(largeBounds, cb));

            ((TextView)rv.findViewById(R.id.session_speakers)).setText(sri.getSpeakers());
            TextView st = ((TextView)rv.findViewById(R.id.session_track));
            st.setText(sri.getTrack());
            st.setTextColor( sri.getColor());
            ((TextView)rv.findViewById(R.id.session_room)).setText(sri.getRoom());
            rv.findViewById(R.id.session_track).setVisibility(View.VISIBLE);
            rv.findViewById(R.id.session_room).setVisibility(View.VISIBLE);
        }
        else
        {
            // The empty text view (already set)
            rv.findViewById(R.id.session_color).setBackgroundColor( 0x00000000);
            ((TextView)rv.findViewById(R.id.session_title)).setText( m_context.getString(R.string.starred_slot_empty_title));
            rv.findViewById(R.id.session_star).setVisibility(View.GONE);

            ((TextView)rv.findViewById(R.id.session_speakers)).setText( m_context.getString(R.string.starred_slot_empty_subtitle));
            rv.findViewById(R.id.session_track).setVisibility(View.GONE);
            rv.findViewById(R.id.session_room).setVisibility(View.GONE);
        }
        return rv;
    }

    /** {@inheritDoc} */
    public Object getGroup( int groupPosition) {
        return m_blocks.get( groupPosition);
    }

    /** {@inheritDoc} */
    public int getGroupCount()
    {
        //Log.d( "getGroupCount", "=============== Group count: " + m_groups.size());
        return m_blocks.size();
    }

    /** {@inheritDoc} */
    public long getGroupId( int groupPosition)
    {
        return groupPosition;
    }

    /** {@inheritDoc} */
    public View getGroupView( int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        Block block = m_blocks.get(groupPosition);
        View rv;
        if( convertView == null)
        {
            LayoutInflater vi = (LayoutInflater)m_context.getSystemService( Context.LAYOUT_INFLATER_SERVICE);
            rv = vi.inflate( R.layout.exp_time_slot_separator_row_view, null);
        }
        else
        {
            rv = convertView;
        }
        ((TextView)rv.findViewById(R.id.text_sep)).setText( block.getTime());
        if( block.hasSessions())
        {
            ((TextView)rv.findViewById(R.id.text_count)).setText( "("+block.getCount()+" items)");
//            rv.findViewById(R.id.text_count_background).setVisibility(View.VISIBLE);
        }
        else
        {
            
            ((TextView)rv.findViewById(R.id.text_count)).setText( "");
//            rv.findViewById(R.id.text_count_background).setVisibility(View.GONE);
        }

        return rv;
    }
    
    /** {@inheritDoc} */
    public boolean hasStableIds()
    {
        return true;
    }

    /** {@inheritDoc} */
    public boolean isChildSelectable( int groupPosition, int childPosition)
    {
        return true;
    }

    /**
     * Release the adapter
     */
    public void close() {
        m_blocks.clear();
        if (m_contentObserver != null) {
            m_context.getContentResolver().unregisterContentObserver(m_contentObserver);
            m_contentObserver = null;
        }
    }

    /**
     * Build the items
     */
    private void buildItems() {
        // Log.d( "buildItems", "Build: " + m_uri);
        if (m_mode == MODE_SCHEDULE) {
            buildAllItems();
        } else {
            buildStarredItems();
        }
    }

    /**
     * Build the items
     */
    private void buildAllItems() {
        m_blocks.clear();
        Cursor cursor = m_context.getContentResolver().query(m_uri, null, m_selection,
                m_selectionArgs, m_sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getColumnIndexOrThrow(BaseColumns._ID);
                int sti = cursor.getColumnIndexOrThrow(SessionsColumns.TITLE);
                int spni = cursor.getColumnIndexOrThrow(SessionsColumns.SPEAKER_NAMES);
                int ri = cursor.getColumnIndexOrThrow(SessionsColumns.ROOM);
                int tri = cursor.getColumnIndexOrThrow(TracksColumns.TRACK);
                int ctri = cursor.getColumnIndexOrThrow(TracksColumns.COLOR);
                int ati = cursor.getColumnIndexOrThrow(SessionsColumns.STARRED);
                int btsi = cursor.getColumnIndexOrThrow(BlocksColumns.TIME_START);
                int btei = cursor.getColumnIndexOrThrow(BlocksColumns.TIME_END);

                long lastBlockStartTime = -1;
                Block block = null;
                do {
                    long startTime = cursor.getLong(btsi);
                    long endTime = cursor.getLong(btei);

                    if (lastBlockStartTime != startTime) {
                        lastBlockStartTime = startTime;
                        block = new Block(m_context, startTime, endTime);
                        m_blocks.add( block);
                    }
                    block.addSession(new SessionRenderItem(m_context, cursor.getInt(id), startTime, endTime,
                            cursor.getString(sti), cursor.getString(spni), cursor.getInt(ri),
                            cursor.getString(tri), cursor.getInt(ctri), cursor.getInt(ati) == 1));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    /**
     * Build the items
     */
    private void buildStarredItems() {
        m_blocks.clear();
        List<SessionRenderItem> list = new ArrayList<SessionRenderItem>();
        Cursor cursor = m_context.getContentResolver().query(m_uri, null, m_selection,
                m_selectionArgs, m_sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getColumnIndexOrThrow(BaseColumns._ID);
                int sti = cursor.getColumnIndexOrThrow(SessionsColumns.TITLE);
                int spni = cursor.getColumnIndexOrThrow(SessionsColumns.SPEAKER_NAMES);
                int ri = cursor.getColumnIndexOrThrow(SessionsColumns.ROOM);
                int tri = cursor.getColumnIndexOrThrow(TracksColumns.TRACK);
                int ctri = cursor.getColumnIndexOrThrow(TracksColumns.COLOR);
                int ati = cursor.getColumnIndexOrThrow(SessionsColumns.STARRED);
                int btsi = cursor.getColumnIndexOrThrow(BlocksColumns.TIME_START);
                int btei = cursor.getColumnIndexOrThrow(BlocksColumns.TIME_END);
                do {
                    list.add(new SessionRenderItem(m_context, cursor.getInt(id), cursor.getLong(btsi),
                            cursor.getLong(btei), cursor.getString(sti), cursor.getString(spni),
                            cursor.getInt(ri), cursor.getString(tri), cursor.getInt(ctri), 
                            cursor.getInt(ati) == 1));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        Cursor bcursor = m_context.getContentResolver().query(Blocks.CONTENT_URI, null, null, null, null);
        if (bcursor != null) {
            if (bcursor.moveToFirst()) {
                int btsi = bcursor.getColumnIndexOrThrow(BlocksColumns.TIME_START);
                int btei = bcursor.getColumnIndexOrThrow(BlocksColumns.TIME_END);
                // Generate the items
                do {
                    long startTime = bcursor.getLong(btsi);
                    long endTime = bcursor.getLong(btei);
                    Block block = new Block(m_context, startTime, endTime);
                    m_blocks.add( block);

                    while (list.size() > 0) {
                        SessionRenderItem si = list.get(0);
                        if (si.getStartTime() == startTime) {
                            block.addSession(list.remove(0));
                        } else {
                            break;
                        }
                    }

                } while (bcursor.moveToNext());
            }
            bcursor.close();
        }
    }

    /**
     * A time block
     */
    protected static class Block {
        private final List<SessionRenderItem> m_sessions = new ArrayList<SessionRenderItem>();
        private final String m_time;
        private final long m_startTime;
        private final long m_endTime;

        /**
         * Constructor
         * 
         * @param context The context
         * @param startTime The start time
         * @param endTime The end time
         */
        public Block(Context context, long startTime, long endTime) {

            String startClause = StringUtils.getTimeAsString( context, StringUtils.DAY_HOUR_TIME, startTime);
            String endClause = StringUtils.getTimeAsString( context, StringUtils.HOUR_MIN_TIME, endTime);
            m_time = context.getString(R.string.block_time, startClause, endClause);
            m_startTime = startTime;
            m_endTime = endTime;

        }

        /**
         * @return The time string
         */
        public String getTime() {
            return m_time;
        }

        /**
         * @return The start time
         */
        public long getStartTime() {
            return m_startTime;
        }

        /**
         * @return The end time
         */
        public long getEndTime() {
            return m_endTime;
        }

        /**
         * Get the session from position
         * 
         * @param position The position
         * @return The session
         */
        private SessionRenderItem getSession(int position) {
            return m_sessions.get(position);
        }
        
        /**
         * Add a new session to this block
         * 
         * @param si The session item
         */
        private void addSession( SessionRenderItem si)
        {
            m_sessions.add(si);
        }
        
        /**
         * @return The count of sessions or 1 if the count is zero (for the empty item)
         */
        private int getCount()
        {
            int size = m_sessions.size();
            if( size > 0)
            {
                return size;
            }
            else
            {
                return 1; // The empty view
            }
        }

        /**
         * @return The count of sessions
         */
        private boolean hasSessions()
        {
            return (m_sessions.size() > 0);
        }
    }
 
    /**
     * The adapter listener  
     */
    public interface ExpandableAdapterListener {
        /**
         * New data notification
         */
        public void onNewData();
    }
}
