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

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.*;

import static android.provider.BaseColumns._ID;
import static no.java.schedule.provider.SessionsContract.BlocksColumns.TIME_END;
import static no.java.schedule.provider.SessionsContract.BlocksColumns.TIME_START;
import static no.java.schedule.provider.SessionsContract.SessionsColumns.*;
import static no.java.schedule.provider.SessionsContract.TracksColumns.TRACK;

/**
 * The expandable sessions_menu adapter
 */
public class ExpandableSessionsAdapter extends BaseExpandableListAdapter {
    // Modes   //TODO - refactor to separate classes to get away with the modes
    public static final int MODE_SCHEDULE = 0;
    public static final int MODE_STARRED = 1;

    private static final String SCHEDULE_TIME_SORT_ORDER = SessionsContract.BlocksColumns.TIME_START+","+ SessionsContract.SessionsColumns.ROOM +" ASC";
    private static final String SCHEDULE_TRACK_SORT_ORDER = SessionsContract.Tracks.TRACK +", "+ SessionsContract.Sessions.TITLE +" ASC";
    private static final String SCHEDULE_SPEAKER_SORT_ORDER = SessionsContract.SessionsColumns.SPEAKER_NAMES +", "+ SessionsContract.Sessions.TITLE +" ASC";


    private final int mode;
    private final Context context;
    private ScheduleSorting sortOrder;
    private final List<Block> blocks;
    private final View.OnClickListener starListener;
    private final Uri uri;
    private final String selection;
    private final String[] selectionArgs;
    private final ExpandableAdapterListener listener;
    private ContentObserver contentObserver;
    private static final long FIFTEEN_MINUTES = 1000*60*15;

    private Long[] startTimes;
    private Long[] endTimes;
    private Block block; //TODO refactor
    private String lastTrack;//TODO refactor
    private String lastSpeaker;//TODO refactor


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
                                     String[] selectionArgs, ScheduleSorting sortOrder, int mode, ExpandableAdapterListener listener) {

        this.context = context;
        this.sortOrder = sortOrder;
        blocks = new ArrayList<Block>();
        this.uri = uri;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.mode = mode;
        this.listener = listener;

        createStartDates();
        buildItems();

        starListener = new View.OnClickListener() {

            public void onClick(View v) {
                Session sri = (Session)v.getTag();
                ContentValues values = new ContentValues();
                values.put(STARRED, sri.isStarred() ? 0 : 1);
                ExpandableSessionsAdapter.this.context.getContentResolver().update(sri.getUri(), values, null, null);
            }
        };

        contentObserver = new ContentObserver(new Handler()) {

            @Override
            public void onChange(boolean selfChange) {
                int beforeCount = blocks.size();
                buildItems();
                notifyDataSetChanged();
                if( blocks.size() != beforeCount) {
                    ExpandableSessionsAdapter.this.listener.onNewData();
                }
            }
        };

        this.context.getContentResolver().registerContentObserver(this.uri, true, contentObserver);

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
            if (!session.getType().equalsIgnoreCase("presentation")){
                titleText +=" (l.talk)";
            }
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
            view.findViewById(R.id.session_track).setVisibility(View.VISIBLE);
            view.findViewById(R.id.session_room).setVisibility(View.VISIBLE);
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

    public int getGroupCount()
    {
        return blocks.size();
    }

    public long getGroupId( int groupPosition)
    {
        return groupPosition;
    }

    public View getGroupView( int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        View rv = createScheduleGroupView(groupPosition, convertView);

        return rv;
    }

    private View createScheduleGroupView(int groupPosition, View convertView) {
        Block block = blocks.get(groupPosition);
        View rv;

        if( convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE);
            rv = vi.inflate( R.layout.exp_time_slot_separator_row_view, null);
        } else {
            rv = convertView;
        }

        ((TextView)rv.findViewById(R.id.text_sep)).setText( block.getHeading());

        if( block.hasSessions()) {
            ((TextView)rv.findViewById(R.id.text_count)).setText( "("+block.getCount()+" items)");
        } else {
            ((TextView)rv.findViewById(R.id.text_count)).setText( "");
        }
        return rv;
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

        if (mode == MODE_SCHEDULE) {
            buildAllItems(sortOrder);
        } else {
            buildStarredItems(sortOrder);
        }
    }

    /**
     * Build the items
     */
    private void buildAllItems(ScheduleSorting sorting) {
        blocks.clear();
        block=null;
        Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs, sortOrderSQLFor(sorting));
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    final Session session = createSession(cursor);
                    switch(sorting){
                        case SCHEDULE:
                            addToScheduleBlock(session);
                            break;
                        case TRACKS:
                            addToTrackBlock(session);
                            break;
                        case SPEAKERS:
                            addToSpeakerBlock(session);
                            break;

                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    //TODO Refactor these to be one
    private void addToSpeakerBlock(Session session) {
       if (lastSpeaker == null || !lastSpeaker.equals(session.getSpeakers()) || block==null) {
            block = new SpeakerBlock(context,session.getSpeakers());
            lastSpeaker = session.getSpeakers();
            blocks.add( block);
        }

        //TODO split speakers into their own blocks if more than one
        block.addSession(session);
    }

    private void addToTrackBlock(Session session) {


        if (lastTrack== null || !lastTrack.equals(session.getTrack()) || block==null) {
            block = new TrackBlock(context,session.getTrack());
            lastTrack = session.getTrack();
            blocks.add( block);
        }

        block.addSession(session);
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

    private Block addToScheduleBlock( Session session) {
        final long startTime = session.getStartTime();
        final long endTime = session.getEndTime();

        final long blockStart = findBlockStart(toMidnightDelta(startTime));
        final long blockEnd = findBlockEnd(toMidnightDelta(endTime));

        final long duration = endTime - startTime;

        if (lastBlockStartTime != blockStart || block==null) {
            block = new TimeBlock(context, startTime, endTime);
            lastBlockStartTime = blockStart;

            blocks.add( block);

        } else if (lastBlockStartTime > startTime){
            throw new AssertionError("Sorting of sessions is not in incremental order!");
        }
        block.addSession(session);

        return block;
    }


    long lastBlockStartTime = -1; //TODO - refactor



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

    private long toMidnightDelta(long startTime) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(startTime);

        Calendar midnight = new GregorianCalendar();
        midnight.setTimeInMillis(startTime);
        midnight.set(Calendar.HOUR_OF_DAY,0);
        midnight.set(Calendar.MINUTE,0);
        midnight.set(Calendar.MILLISECOND,0);


        return calendar.getTimeInMillis() - midnight.getTimeInMillis();

    }

    private long findBlockEnd(long time) {
        for (Long endTime : endTimes) {
            if (endTime >= time){
                return endTime;
            }
        }
       throw new IllegalStateException("error in slot time resolution");
    }

    private long findBlockStart(long time) {
        for (Long startTime : startTimes) {
            if (startTime <= time){
               return startTime;
            }
        }
        throw new IllegalStateException("error in slot time resolution");
    }

    /**
     * Build the items
     * @param sortOrder
     */
    private void buildStarredItems(ScheduleSorting sortOrder) {  //TODO refactor into "buildall"
        blocks.clear();
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
        if (bcursor != null) {
            if (bcursor.moveToFirst()) {
                int btsi = bcursor.getColumnIndexOrThrow(TIME_START);
                int btei = bcursor.getColumnIndexOrThrow(TIME_END);
                // Generate the items
                do {
                    long startTime = bcursor.getLong(btsi);
                    long endTime = bcursor.getLong(btei);
                    Block block = new TimeBlock(context, startTime, endTime);
                    blocks.add( block);

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
    }


    private void createStartDates() {

        // TODO replace this with data from the event feed

        List<Long> startTimes = new ArrayList<Long>();
        List<Long> endTimes = new ArrayList<Long>();

        final GregorianCalendar time = new GregorianCalendar(0, 0, 0, 9, 0); // 0900

        final GregorianCalendar midnight =  new GregorianCalendar(0, 0, 0, 0, 0);
        final long base = midnight.getTimeInMillis();

        // 09:00 - 10:00
        startTimes.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,60);
        endTimes.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,15);

        // 10:15 - 11:15
        startTimes.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,60);
        endTimes.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,30);  // First long break

        // 11:45 - 12:45
        startTimes.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,60);
        endTimes.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,15);

        // 13:00 - 14:00
        startTimes.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,60);
        endTimes.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,15);

        // 14:15 - 15:15
        startTimes.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,60);
        endTimes.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,30); // Second long break

        // 15:45 - 16:45
        startTimes.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,60);
        endTimes.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,15);

        // 17:00 - 18:00
        startTimes.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,60);
        endTimes.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,15);


        // 18:15 - 19:00
        startTimes.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,60);
        endTimes.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,15);


        this.startTimes = startTimes.toArray(new Long[startTimes.size()]);
        Arrays.sort(this.startTimes,Collections.reverseOrder());


        for (Long startTime : startTimes) {
            Log.d("startTime",new SimpleDateFormat("hh:mm").format(new Date(startTime)));
        }

        for (Long endTime : endTimes) {
            Log.d("endTime",new SimpleDateFormat("hh:mm").format(new Date(endTime)));

        }

        this.endTimes = endTimes.toArray(new Long[endTimes.size()]);
        Arrays.sort(this.endTimes);


    }


    public void setSorting(ScheduleSorting sorting) {
        this.sortOrder = sorting;
        buildItems();
    }
}
