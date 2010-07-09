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

package no.java.schedule.activities.adapters;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import no.java.schedule.R;
import no.java.schedule.activities.adapters.beans.*;
import no.java.schedule.activities.adapters.interfaces.ExpandableAdapterListener;
import no.java.schedule.provider.SessionsContract;
import no.java.schedule.provider.SessionsContract.Blocks;

import java.util.ArrayList;
import java.util.List;

import static android.provider.BaseColumns._ID;
import static no.java.schedule.provider.SessionsContract.BlocksColumns.TIME_END;
import static no.java.schedule.provider.SessionsContract.BlocksColumns.TIME_START;
import static no.java.schedule.provider.SessionsContract.SessionsColumns.*;
import static no.java.schedule.provider.SessionsContract.TracksColumns.TRACK;

/**
 * The expandable sessions_menu adapter
 */
public class ExpandableSessionsAdapter extends BaseExpandableListAdapter {

    private static final String SCHEDULE_TIME_SORT_ORDER = SessionsContract.BlocksColumns.TIME_START+","+ SessionsContract.SessionsColumns.ROOM +" ASC";
    private static final String SCHEDULE_TRACK_SORT_ORDER = SessionsContract.Tracks.TRACK +", "+ SessionsContract.Sessions.TITLE +" ASC";
    private static final String SCHEDULE_SPEAKER_SORT_ORDER = SessionsContract.SessionsColumns.SPEAKER_NAMES +", "+ SessionsContract.Sessions.TITLE +" ASC";


    private final SessionsAdapter.MODE mode;
    private final Context context;
    private ScheduleSorting sortOrder;
    private List<Block> blocks;
    private View.OnClickListener starListener;
    private final Uri uri;
    private final String selection;
    private final String[] selectionArgs;
    private final ExpandableAdapterListener listener;
    private ContentObserver contentObserver;


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
                                     String[] selectionArgs, ScheduleSorting sortOrder, SessionsAdapter.MODE mode, ExpandableAdapterListener listener) {

        this.context = context;
        this.sortOrder = sortOrder;
        blocks = new ArrayList<Block>();
        this.uri = uri;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.mode = mode;
        this.listener = listener;

        buildItems();

        registerListenersAndObservers();

    }

    private void registerListenersAndObservers() {
        starListener = new StarredSessionListener(context);
        contentObserver = new SessionListContentObserver();
        context.getContentResolver().registerContentObserver(uri, true, contentObserver);
    }


    public Object getChild(int groupPosition, int childPosition) {
        Block block = blocks.get(groupPosition);

        if( block.hasSessions()) {
            return block.getSession(childPosition);
        } else {
            return block;
        }
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public int getChildrenCount( int groupPosition) {
        if( groupPosition >= blocks.size()){
            return 0;
        }
        return blocks.get(groupPosition).getCount();
    }

    public View getChildView( int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        Block block = blocks.get(groupPosition);
        boolean sessionView = block.hasSessions();
        View view;

        if( convertView == null){
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate( R.layout.session_row, null);
        } else {
            view = convertView;
        }

        //TODO - split into separate classes rather than to rely on switches
        if( sessionView)
        {
            SessionDisplay session = block.getSession(childPosition);
            view.findViewById(R.id.session_color).setBackgroundColor(session.getColor());

            String titleText = session.getTitle();
            final TextView title = (TextView) view.findViewById(R.id.session_title);
            title.setText(titleText);

            CheckBox checkBox = ((CheckBox)view.findViewById(R.id.session_star));
            checkBox.setTag( session);
            checkBox.setOnClickListener(starListener);
            checkBox.setChecked(session.isStarred());
            checkBox.setVisibility(View.VISIBLE);

            // Find and hook up larger delegate view for toggling star
            View starDelegate = view.findViewById(R.id.star_delegate);
            Rect largeBounds = new Rect(0, 0, 1024, 1024);
            starDelegate.setTouchDelegate(new TouchDelegate(largeBounds, checkBox));

            ((TextView)view.findViewById(R.id.session_speakers)).setText(session.getSpeakers());
            TextView sessionTrack = ((TextView)view.findViewById(R.id.session_track));
            sessionTrack.setText(session.getTrack());
            sessionTrack.setTextColor( session.getColor());
            ((TextView)view.findViewById(R.id.session_room)).setText(session.getRoom());
            view.findViewById(R.id.session_room).setVisibility(View.VISIBLE);

            if (session.getType().equalsIgnoreCase("presentation")){
                view.findViewById(R.id.session_track).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.session_track).setVisibility(View.GONE);
            }
        }
        else
        {
            // The empty text view (already set)
            view.findViewById(R.id.session_color).setBackgroundColor( 0x00000000);
            ((TextView)view.findViewById(R.id.session_title)).setText( context.getString(R.string.starred_slot_empty_title));
            view.findViewById(R.id.session_star).setVisibility(View.GONE);

            ((TextView)view.findViewById(R.id.session_speakers)).setText( context.getString(R.string.starred_slot_empty_subtitle));
            view.findViewById(R.id.session_track).setVisibility(View.GONE);
            view.findViewById(R.id.session_room).setVisibility(View.GONE);
        }
        return view;
    }

    public Object getGroup( int groupPosition) {
        return blocks.get( groupPosition);
    }

    public int getGroupCount(){
        return blocks.size();
    }

    public long getGroupId( int groupPosition){
        return groupPosition;
    }

    public View getGroupView( int groupPosition, boolean isExpanded, View convertView, ViewGroup parent){

        Block block = blocks.get(groupPosition);
        View groupView;

        if( convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE);
            groupView = vi.inflate( R.layout.exp_time_slot_separator_row_view, null);
        } else {
            groupView = convertView;
        }

        final TextView title = (TextView) groupView.findViewById(R.id.text_sep);
        title.setText( block.getHeading());

        final TextView count = (TextView) groupView.findViewById(R.id.text_count);
        if( block.hasSessions()) {
            count.setText(context.getString(R.string.group_session_count, block.getCount()));
        } else {
            count.setText( "");
        }
        return groupView;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable( int groupPosition, int childPosition) {
        return true;
    }

    /**
     * Release the adapter
     */
    public void close() {
        blocks.clear();
        if (contentObserver != null) {
            context.getContentResolver().unregisterContentObserver(contentObserver);
            contentObserver = null;
        }
    }

    /**
     * Build the items
     */
    private void buildItems() {

        if (mode == SessionsAdapter.MODE.SCHEDULE) {
            buildAllItems(sortOrder);
        } else {
            buildStarredItems(sortOrder);
        }
    }

    private void buildAllItems(ScheduleSorting sorting) {
        List<Block> newList = new ArrayList<Block>();

        Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs, sortOrderSQLFor(sorting));
        if (cursor != null && cursor.moveToFirst()) {

            Block block=null;
            String lastBlockId = "";

            do {
                final Session session = createSession(cursor);

                final String blockId = blockIdFor(session, sorting);
                if (!lastBlockId.equals(blockId)){
                    block = createBlockFor(session,sorting);
                    lastBlockId = blockId !=null ? blockId : "";
                    newList.add( block);
                }

                block.addSession(session);

            } while (cursor.moveToNext());
        }
        cursor.close();

        blocks = newList;
    }

    private Block createBlockFor(Session session, ScheduleSorting sorting) {
        switch(sorting){
            default:
            case SCHEDULE:
                return new TimeBlock(context, session);
            case TRACKS:
                return new TrackBlock(context, session.getTrack());
            case SPEAKERS:
                return new SpeakerBlock(context, session.getSpeakers());
        }
    }

    private String blockIdFor(Session session, ScheduleSorting sorting) {
        switch(sorting){
            default:
            case SCHEDULE:
                long blockStart = ScheduleTimeUtil.findBlockStart(session.getStartTime());
                return String.valueOf(blockStart);

            case TRACKS:
                return session.getTrack();
            case SPEAKERS:
                return session.getSpeakers();

        }
    }

   
    private String sortOrderSQLFor(ScheduleSorting sorting) {
        switch(sorting){
            default:
            case SCHEDULE:
                return SCHEDULE_TIME_SORT_ORDER;

            case TRACKS:
                return SCHEDULE_TRACK_SORT_ORDER;

            case SPEAKERS:
                return SCHEDULE_SPEAKER_SORT_ORDER;

        }
    }





    private Session createSession(Cursor cursor) {


        final String title         = cursor.getString( cursor.getColumnIndexOrThrow( TITLE ));
        final String speakers      = cursor.getString( cursor.getColumnIndexOrThrow( SPEAKER_NAMES ));
        final String room          = cursor.getString(    cursor.getColumnIndexOrThrow( ROOM ));
        final String track      = cursor.getString( cursor.getColumnIndexOrThrow( TRACK ));
        final int color         = cursor.getInt(    cursor.getColumnIndexOrThrow(SessionsContract.TracksColumns.COLOR ));
        final boolean starred   = cursor.getInt(    cursor.getColumnIndexOrThrow( STARRED )) == 1;
        final long startTime    = cursor.getLong(   cursor.getColumnIndexOrThrow( TIME_START ));
        final long endTime      = cursor.getLong(   cursor.getColumnIndexOrThrow( TIME_END ));
        final int id            = cursor.getInt(    cursor.getColumnIndexOrThrow( _ID ));
        final String type       = cursor.getString( cursor.getColumnIndexOrThrow( TYPE));


        return new Session(context, id, startTime, endTime, title, speakers, room, track, color, starred, type);
    }

    




    /**
     * Build the items
     * @param sortOrder
     */
    private void buildStarredItems(ScheduleSorting sortOrder) {  //TODO refactor into "buildall"
        List<Block> newList = new ArrayList<Block>();

        List<Session> list = new ArrayList<Session>();
        Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs, sortOrderSQLFor(sortOrder));
        if (cursor != null) {
            if (cursor.moveToFirst()) {

                do {
                    list.add(createSession(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        Cursor bcursor = context.getContentResolver().query(Blocks.CONTENT_URI, null, null, null, null);
        TimeBlock lastBlock = null;
        if (bcursor != null) {
            if (bcursor.moveToFirst()) {
                int btsi = bcursor.getColumnIndexOrThrow(TIME_START);
                int btei = bcursor.getColumnIndexOrThrow(TIME_END);
                // Generate the items
                do {
                    long startTime = bcursor.getLong(btsi);
                    long endTime = bcursor.getLong(btei);
                    TimeBlock block = new TimeBlock(context, startTime, endTime);
                    
                    if (lastBlock!=null && block.getStartSlotTime() == lastBlock.getStartSlotTime()){
                        continue;
                    }

                    newList.add( block);
                    lastBlock = block;

                    while (list.size() > 0) {
                        Session si = list.get(0);
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
        blocks = newList;
    }


    public void setSorting(ScheduleSorting sorting) {
        this.sortOrder = sorting;
        buildItems();
    }

    private class SessionListContentObserver extends ContentObserver {
        public SessionListContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            int beforeCount = blocks.size();
            buildItems();
            notifyDataSetChanged();
            if( blocks.size() != beforeCount) {
                ExpandableSessionsAdapter.this.listener.onNewData();
            }
        }
    }
}
