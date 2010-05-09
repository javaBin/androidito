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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static no.java.schedule.activities.adapters.SessionsAdapter.MODE.SCHEDULE;
import static no.java.schedule.activities.adapters.listitems.ListItem.TYPE.BLOCK;

/**
 * The sessions_menu adapter
 */
public class SessionsAdapter extends BaseAdapter {
    public static final boolean DISPLAY_DAY = false; 

    public enum MODE{SCHEDULE,STARRED};

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

        this.context.getContentResolver().registerContentObserver(this.uri, true, contentObserver);
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
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        switch (listItems.get(position).getType()) {
            case DAY:
            case BLOCK:
                return false;
            default:
                return true;
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

                case EMPTY_BLOCK: {
                    view = inflater.inflate(R.layout.empty_time_slot_row_view, null);
                    break;
                }

                default: {
                    break;
                }
            }
        }

        final TextView textView = (TextView) view.findViewById(R.id.text_sep);
        final ImageView imageView = (ImageView) view.findViewById(R.id.image_sep);
        final View sessionColorCode = view.findViewById(R.id.session_color);
        final TextView sessionTitle = (TextView) view.findViewById(R.id.session_title);
        final CheckBox starred = ((CheckBox)view.findViewById(R.id.session_star));
        final TextView speakers = (TextView) view.findViewById(R.id.session_speakers);
        final TextView tracks = ((TextView)view.findViewById(R.id.session_track));
        final TextView room = (TextView) view.findViewById(R.id.session_room);

        switch (listItem.getType()) {
            case DAY:
                DayListItem dsi = (DayListItem) listItem;
                textView.setText(dsi.getDay());
                break;

            case BLOCK:
                BlockListItem tsi = (BlockListItem) listItem;
                textView.setText(tsi.getTime());
                imageView.setImageResource(R.drawable.ic_dialog_time);
                break;

            case SESSION:
                Session session = ((SessionListItem) listItem).getSessionItem();
                sessionColorCode.setBackgroundColor(session.getColor());
                sessionTitle.setText(session.getTitle());
                starred.setTag(listItem);
                starred.setOnClickListener(startListener);
                starred.setChecked(session.isStarred());

                speakers.setText(session.getSpeakers());
                tracks.setText(session.getTrack());
                tracks.setTextColor( session.getColor());
                room.setText(session.getRoom());
                break;

            case EMPTY_BLOCK:
                break;

            default:
                break;

        }

        return view;
    }

    private void buildItems() {

        if (mode == SCHEDULE) {
            buildAllItems();
        } else {
            buildStarredItems();
        }
    }

    private void buildAllItems() {
        listItems.clear();
        Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs, sortOrder);

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
                int typeIndex = cursor.getColumnIndexOrThrow(SessionsColumns.TYPE);

                int day = 0;
                long lastBlockStartTime = -1;

                do {
                    long startTime = cursor.getLong(btsi);
                    long endTime = cursor.getLong(btei);
                    if( DISPLAY_DAY) {
                        Date di = new Date( startTime*1000);
                        if (di.getDay() != day) {
                            day = di.getDay();
                            listItems.add(new DayListItem(context, startTime));
                        }
                    }
                    if (lastBlockStartTime != startTime) {
                        lastBlockStartTime = startTime;
                        listItems.add(new BlockListItem(context, BLOCK, startTime, endTime));
                    }
                    listItems.add(new SessionListItem(new Session(context, cursor.getInt(id), startTime, endTime,
                            cursor.getString(sti), cursor.getString(spni), cursor.getString(ri),
                            cursor.getString(tri), cursor.getInt(ctri), cursor.getInt(ati) == 1,cursor.getString(typeIndex))));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    private void buildStarredItems() {    //TODO refactor this to reuse code from  buildAllItems - code is identical
        listItems.clear();
        List<SessionListItem> list = new ArrayList<SessionListItem>();
        populateWithSessions(list);

        Cursor blockCursor = context.getContentResolver().query(Blocks.CONTENT_URI, null, null, null, null);

        if (blockCursor != null) {
            if (blockCursor.moveToFirst()) {
                int btsi = blockCursor.getColumnIndexOrThrow(BlocksColumns.TIME_START);
                int btei = blockCursor.getColumnIndexOrThrow(BlocksColumns.TIME_END);
                // Generate the listItems
                int day = 0;
                do {
                    long startTime = blockCursor.getLong(btsi);
                    long endTime = blockCursor.getLong(btei);
                    if( DISPLAY_DAY) {
                        Date di = new Date( startTime*1000);
                        if (di.getDay() != day) {
                            day = di.getDay();
                            listItems.add(new DayListItem(context, startTime));
                        }
                    }

                    listItems.add(new BlockListItem(context, BLOCK, startTime, endTime));

                    boolean foundSession = false;

                    while (list.size() > 0 && !foundSession) {
                        final Session sessionItem = list.get(0).getSessionItem();

                        if (sessionItem.getStartTime() == startTime && sessionItem.getEndTime() == endTime) {
                            listItems.add(list.remove(0));
                            foundSession = true;
                        }
                    }

                    if (!foundSession) {
                        listItems.add(new EmptyBlockListItem(context, startTime, endTime));
                    }

                } while (blockCursor.moveToNext());
            }
            blockCursor.close();
        }
    }

    private void populateWithSessions(List<SessionListItem> list) {
        Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs, sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(new SessionListItem(sessionFrom(cursor)));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    private Session sessionFrom(Cursor cursor) {
        int id = cursor.getColumnIndexOrThrow(BaseColumns._ID);
        int sti = cursor.getColumnIndexOrThrow(SessionsColumns.TITLE);
        int spni = cursor.getColumnIndexOrThrow(SessionsColumns.SPEAKER_NAMES);
        int ri = cursor.getColumnIndexOrThrow(SessionsColumns.ROOM);
        int tri = cursor.getColumnIndexOrThrow(TracksColumns.TRACK);
        int ctri = cursor.getColumnIndexOrThrow(TracksColumns.COLOR);
        int ati = cursor.getColumnIndexOrThrow(SessionsColumns.STARRED);
        int btsi = cursor.getColumnIndexOrThrow(BlocksColumns.TIME_START);
        int btei = cursor.getColumnIndexOrThrow(BlocksColumns.TIME_END);
        int typeIndex = cursor.getColumnIndexOrThrow(SessionsColumns.TYPE);

        final Session session = new Session(context, cursor.getInt(id), cursor.getLong(btsi),
                cursor.getLong(btei), cursor.getString(sti), cursor.getString(spni),
                cursor.getString(ri), cursor.getString(tri), cursor.getInt(ctri), cursor
                        .getInt(ati) == 1, cursor.getString(typeIndex));
        return session;
    }

}
