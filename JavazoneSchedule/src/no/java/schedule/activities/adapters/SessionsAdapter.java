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
import android.net.Uri;
import android.os.Handler;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import no.java.schedule.R;
import no.java.schedule.activities.adapters.beans.Session;
import no.java.schedule.activities.adapters.listitems.*;
import no.java.schedule.provider.SessionsContract.Blocks;
import no.java.schedule.provider.SessionsContract.BlocksColumns;
import no.java.schedule.provider.SessionsContract.SessionsColumns;
import no.java.schedule.provider.SessionsContract.TracksColumns;
import no.java.schedule.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static no.java.schedule.activities.adapters.listitems.ListItem.TYPE.BLOCK;

/**
 * The sessions_menu adapter
 */
public class SessionsAdapter extends BaseAdapter {
    private static final boolean NO_BLOCK_HEADERS = false;
    public static final boolean DISPLAY_DAY = NO_BLOCK_HEADERS;
    private static final boolean CREATE_BLOCK_HEADERS = true;

    public enum MODE{SCHEDULE,STARRED,SESSION_AGGREGATE_VIEW};

    private enum BLOCK_HEADERS{YES,NO}
    private final MODE mode;
    private final Context context;
    private final List<ListItem> listItems;
    private final View.OnClickListener startListener;
    private final Uri uri;
    private final String selection;
    private final String[] selectionArgs;
    private final String sortOrder;
    private ContentObserver contentObserver;



    public SessionsAdapter(Context context, Uri uri, String selection, String[] selectionArgs, String sortOrder, MODE mode) {
        this.context = context;
        listItems = new ArrayList<ListItem>();
        this.uri = uri;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.sortOrder = sortOrder;
        this.mode = mode;

        buildItems();

        startListener = new View.OnClickListener() {

            public void onClick(View view) {
                Session session = ((SessionListItem)view.getTag()).getSessionItem();

                // Update the content provider
                ContentValues values = new ContentValues();
                values.put(SessionsColumns.STARRED, session.isStarred() ? 0 : 1);
                SessionsAdapter.this.context.getContentResolver().update(session.getUri(), values, null, null);
            }
        };

        contentObserver = new ContentObserver(new Handler()) {

            @Override
            public void onChange(boolean selfChange) {
                buildItems();
                notifyDataSetChanged();
            }
        };

        this.context.getContentResolver().registerContentObserver(this.uri, CREATE_BLOCK_HEADERS, contentObserver);
    }

    /**
     * Release the adapter
     */
    public void close() {
        listItems.clear();
        if (contentObserver != null) {
            context.getContentResolver().unregisterContentObserver(contentObserver);
            contentObserver = null;
        }
    }

    public ListItem getItemByPosition(int position) {
        return listItems.get(position);
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public int getCount() {
        return listItems.size();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return NO_BLOCK_HEADERS;
    }

    @Override
    public boolean isEnabled(int position) {
        switch (listItems.get(position).getType()) {
            case DAY:
            case BLOCK:
                return NO_BLOCK_HEADERS;
            default:
                return CREATE_BLOCK_HEADERS;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getItemViewType(int position) {
        ListItem listItem = listItems.get(position);
        return listItem.getType().ordinal();
    }

    public View getView(int position, View view, ViewGroup parent) {

        ListItem listItem = listItems.get(position);

        if (view == null) {
            view = inflateView(view, listItem);
        }

        switch (listItem.getType()) {
            case DAY:
                setDayValues(view,listItem);
                break;
            case BLOCK:
                setBlockValues(view,listItem);
                break;
            case SESSION:
                setSessionValues(view,listItem);
                break;
            case SESSION_AGGREGATE_HEADER:
                setSessionAggregateHeaderValues(view,listItem);
            case EMPTY_BLOCK:

            default:
                break;
        }

        return view;
    }

    private void setSessionAggregateHeaderValues(View view, ListItem listItem) {
        SessionAggreateHeaderListItem headerListItem = (SessionAggreateHeaderListItem) listItem;
        ((TextView)  view.findViewById(R.id.text_sep)).setText(headerListItem.getTitle());
        ((ImageView) view.findViewById(R.id.image_sep)).setImageResource(R.drawable.ic_menu_agenda);
    }

    private void setSessionValues(View view, ListItem listItem) {
        Session session = ((SessionListItem) listItem).getSessionItem();

        view.findViewById(R.id.session_color).setBackgroundColor(session.getColor());

        ((TextView) view.findViewById(R.id.session_title)).setText(session.getTitle());

        ((CheckBox) view.findViewById(R.id.session_star)).setTag(listItem);
        ((CheckBox) view.findViewById(R.id.session_star)).setOnClickListener(startListener);
        ((CheckBox) view.findViewById(R.id.session_star)).setChecked(session.isStarred());

        ((TextView) view.findViewById(R.id.session_speakers)).setText(session.getSpeakers());

        ((TextView) view.findViewById(R.id.session_track)).setText(session.getTrack());
        ((TextView) view.findViewById(R.id.session_track)).setTextColor( session.getColor());

        ((TextView) view.findViewById(R.id.session_room)).setText(session.getRoom());
    }

    private void setBlockValues(View view, ListItem listItem) {
        BlockListItem tsi = (BlockListItem) listItem;
        ((TextView)  view.findViewById(R.id.text_sep)).setText(tsi.getTime());
        ((ImageView) view.findViewById(R.id.image_sep)).setImageResource(R.drawable.ic_dialog_time);
    }

    private void setDayValues(View view, ListItem listItem) {
        ((TextView) view.findViewById(R.id.text_sep)).setText(((DayListItem) listItem).getDay());
    }

    private View inflateView(View view, ListItem listItem) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        switch (listItem.getType()) {
            case DAY: {
                view = inflater.inflate(R.layout.day_separator_row_view, null);
                break;
            }

            case BLOCK: {
                view = inflater.inflate(R.layout.time_slot_separator_row_view, null);
                break;
            }

            case SESSION: {
                view = inflater.inflate(R.layout.session_row, null);
                break;
            }

            case SESSION_AGGREGATE_HEADER: {
                view = inflater.inflate(R.layout.session_aggregate_header, null);
                break;
            }



            case EMPTY_BLOCK: {
                view = inflater.inflate(R.layout.empty_time_slot_row_view, null);
                break;
            }

            default: {
                break;
            }
        }
        return view;
    }

    private void buildItems() {

         listItems.clear();

        switch (mode){
            case STARRED:
                listItems.addAll(buildStarredItems());
                return;
            case SESSION_AGGREGATE_VIEW:
                listItems.addAll(buildAllItems(BLOCK_HEADERS.NO));
                final Session sessionItem = ((SessionListItem) listItems.get(0)).getSessionItem();
                final String timeAsString = StringUtils.getTimeAsString(
                        context,
                        new SimpleDateFormat("hh:mm"),
                        sessionItem.getStartTime());

                String heading = String.format("Lightning talks at %s in room %s", timeAsString, sessionItem.getRoom());

                listItems.add(0,new SessionAggreateHeaderListItem(heading));
                return;
            case SCHEDULE:
            default:
                listItems.addAll(buildAllItems(BLOCK_HEADERS.YES));
                return;
        }
    }




    private List<ListItem> buildAllItems(BLOCK_HEADERS createBlockHeaders) {

        List<ListItem> newListOfItems = new ArrayList<ListItem>();

        Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs, sortOrder);

        if (cursor != null) {
            if (cursor.moveToFirst()) {

                long lastBlockStartTime = -1;

                do {

                    if(createBlockHeaders == BLOCK_HEADERS.YES){
                        long startTime = cursor.getLong(cursor.getColumnIndexOrThrow(BlocksColumns.TIME_START));
                        createBlockHeaderIfNeeded(cursor,lastBlockStartTime,newListOfItems);
                        lastBlockStartTime  = startTime;
                    }

                    newListOfItems.add(createSessionListItem(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }


       return newListOfItems;
    }

    private void addDayHeaderIfNeccesary() {
        /*if( DISPLAY_DAY) {
            Date di = new Date( startTime*1000);
            if (di.getDay() != day) {
                day = di.getDay();
                newListOfItems.add(new DayListItem(context, startTime));
            }
        } */
    }

    private void createBlockHeaderIfNeeded(Cursor cursor, long lastBlockStartTime, List<ListItem> newListOfItems) {

        if (lastBlockStartTime != cursor.getLong(cursor.getColumnIndexOrThrow(BlocksColumns.TIME_START))) {
            newListOfItems.add(new BlockListItem(
                    context,
                    BLOCK,
                    cursor.getLong(cursor.getColumnIndexOrThrow(BlocksColumns.TIME_START)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(BlocksColumns.TIME_END)))
            );
        }
    }

    private SessionListItem createSessionListItem(Cursor cursor) {

        return new SessionListItem(
                new Session(
                        context,
                        cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(BlocksColumns.TIME_START)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(BlocksColumns.TIME_END)),
                        cursor.getString(cursor.getColumnIndexOrThrow(SessionsColumns.TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(SessionsColumns.SPEAKER_NAMES)),
                        cursor.getString(cursor.getColumnIndexOrThrow(SessionsColumns.ROOM)),
                        cursor.getString(cursor.getColumnIndexOrThrow(TracksColumns.TRACK)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(TracksColumns.COLOR)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(SessionsColumns.STARRED)) == 1,
                        cursor.getString(cursor.getColumnIndexOrThrow(SessionsColumns.TYPE))
                )
        );
    }

    private List<ListItem> buildStarredItems() {    //TODO refactor this to reuse code from  buildAllItems - code is identical
        List<ListItem> newListOfItems = new ArrayList<ListItem>();

        List<SessionListItem> list = new ArrayList<SessionListItem>();
        populateWithSessions(list);

        Cursor blockCursor = context.getContentResolver().query(Blocks.CONTENT_URI, null, null, null, null);

        if (blockCursor != null) {
            if (blockCursor.moveToFirst()) {

                int day = 0;

                do {
                    long startTime = blockCursor.getLong(blockCursor.getColumnIndexOrThrow(BlocksColumns.TIME_START));
                    long endTime = blockCursor.getLong(blockCursor.getColumnIndexOrThrow(BlocksColumns.TIME_END));

                    //addDayHeaderIfNeccesary();

                    newListOfItems.add(new BlockListItem(context, BLOCK, startTime, endTime));

                    boolean foundSession = NO_BLOCK_HEADERS;

                    while (list.size() > 0 && !foundSession) {
                        final Session sessionItem = list.get(0).getSessionItem();

                        if (sessionItem.getStartTime() == startTime && sessionItem.getEndTime() == endTime) {
                            newListOfItems.add(list.remove(0));
                            foundSession = CREATE_BLOCK_HEADERS;
                        }
                    }

                    if (!foundSession) {
                        newListOfItems.add(new EmptyBlockListItem(context, startTime, endTime));
                    }

                } while (blockCursor.moveToNext());
            }
            blockCursor.close();
        }

        return newListOfItems;
    }

    private void populateWithSessions(List<SessionListItem> list) {
        Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs, sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(createSessionListItem(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }


}
