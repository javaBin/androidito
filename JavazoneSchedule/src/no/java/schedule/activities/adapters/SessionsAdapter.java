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
import no.java.schedule.activities.adapters.bean.Session;
import no.java.schedule.provider.SessionsContract.Blocks;
import no.java.schedule.provider.SessionsContract.BlocksColumns;
import no.java.schedule.provider.SessionsContract.SessionsColumns;
import no.java.schedule.provider.SessionsContract.TracksColumns;
import no.java.schedule.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The sessions adapter
 */
public class SessionsAdapter extends BaseAdapter {
    public static final boolean DISPLAY_DAY = false; 

    // Modes
    public static final int MODE_SCHEDULE = 0;
    public static final int MODE_STARRED = 1;

    private final int m_mode;
    private final Context m_context;
    private final List<Item> m_items;
    private final View.OnClickListener m_starListener;
    private final Uri m_uri;
    private final String m_selection;
    private final String[] m_selectionArgs;
    private final String m_sortOrder;
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
    public SessionsAdapter(Context context, Uri uri, String selection, String[] selectionArgs,
            String sortOrder, int mode) {
        m_context = context;
        m_items = new ArrayList<Item>();
        m_uri = uri;
        m_selection = selection;
        m_selectionArgs = selectionArgs;
        m_sortOrder = sortOrder;
        m_mode = mode;
        // Log.d( "SessionsAdapter", "Selection: " + selection + ", args: " +
        // selectionArgs + ", sort: " + sortOrder);
        buildItems();
        m_starListener = new View.OnClickListener() {
            /*
             * (non-Javadoc)
             * @see android.view.View.OnClickListener#onClick(android.view.View)
             */
            public void onClick(View v) {
                Session sri = ((SessionItem)v.getTag()).getSessionItem();
                //Log.d( "SessionsAdapter.onClick", "Item: " + sri.getUri());
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
                //Log.d("SessionsAdapter.onChange", "Change!");
                buildItems();
                notifyDataSetChanged();
                //Log.d( "SessionsAdapter.onChange", "Done");
            }
        };
        m_context.getContentResolver().registerContentObserver(m_uri, true, m_contentObserver);
    }

    /**
     * Release the adapter
     */
    public void close() {
        m_items.clear();
        if (m_contentObserver != null) {
            m_context.getContentResolver().unregisterContentObserver(m_contentObserver);
            m_contentObserver = null;
        }
    }

    /**
     * @param position The position
     * @return The item
     */
    public Item getItemByPosition(int position) {
        return m_items.get(position);
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    public Object getItem(int position) {
        return position;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getItemId(int)
     */
    public long getItemId(int position) {
        return position;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
    public int getCount() {
        return m_items.size();
    }

    /*
     * (non-Javadoc)
     * @see android.widget.BaseAdapter#areAllItemsEnabled()
     */
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.BaseAdapter#isEnabled(int)
     */
    @Override
    public boolean isEnabled(int position) {
        switch (m_items.get(position).m_type) {
            case Item.TYPE_DAY:
            case Item.TYPE_BLOCK: {
                return false;
            }

            default: {
                return true;
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see android.widget.BaseAdapter#getViewTypeCount()
     */
    @Override
    public int getViewTypeCount() {
        return 4;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.BaseAdapter#getItemViewType(int)
     */
    @Override
    public int getItemViewType(int position) {
        Item item = m_items.get(position);
        switch (item.m_type) {
            case Item.TYPE_DAY:
                return 0;

            case Item.TYPE_BLOCK:
                return 1;

            case Item.TYPE_SESSION:
                return 2;

            case Item.TYPE_EMPTY_BLOCK:
                return 3;

            default:
                return 0;
        }
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getView(int, android.view.View,
     * android.view.ViewGroup)
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        View rv = null;
        Item item = m_items.get(position);
        // Log.d("getView", "Type: " + item.m_type);
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)m_context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            switch (item.m_type) {
                case Item.TYPE_DAY: {
                    rv = vi.inflate(R.layout.day_separator_row_view, null);
                    break;
                }

                case Item.TYPE_BLOCK: {
                    rv = vi.inflate(R.layout.time_slot_separator_row_view, null);
                    break;
                }

                case Item.TYPE_SESSION: {
                    rv = vi.inflate(R.layout.session_row, null);
                    break;
                }

                case Item.TYPE_EMPTY_BLOCK: {
                    rv = vi.inflate(R.layout.empty_time_slot_row_view, null);
                    break;
                }

                default: {
                    break;
                }
            }
        } else {
            rv = convertView;
        }

        switch (item.m_type) {
            case Item.TYPE_DAY: {
                DayItem dsi = (DayItem)item;
                ((TextView)rv.findViewById(R.id.text_sep)).setText(dsi.getDay());
                break;
            }

            case Item.TYPE_BLOCK: {
                BlockItem tsi = (BlockItem)item;
                ((TextView)rv.findViewById(R.id.text_sep)).setText(tsi.getTime());
                ((ImageView)rv.findViewById(R.id.image_sep))
                        .setImageResource(R.drawable.ic_dialog_time);
                break;
            }

            case Item.TYPE_SESSION: {
                Session sri = ((SessionItem)item).getSessionItem();
                rv.findViewById(R.id.session_color).setBackgroundColor(sri.getColor());
                ((TextView)rv.findViewById(R.id.session_title)).setText(sri.getTitle());
                CheckBox cb = ((CheckBox)rv.findViewById(R.id.session_star));
                cb.setTag(item);
                cb.setOnClickListener(m_starListener);
                cb.setChecked(sri.isStarred());

                ((TextView)rv.findViewById(R.id.session_speakers)).setText(sri.getSpeakers());
                TextView st = ((TextView)rv.findViewById(R.id.session_track));
                st.setText(sri.getTrack());
                st.setTextColor( sri.getColor());
                ((TextView)rv.findViewById(R.id.session_room)).setText(sri.getRoom());
                break;
            }

            case Item.TYPE_EMPTY_BLOCK: {
                break;
            }

            default: {
                break;
            }
        }

        return rv;
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
        m_items.clear();
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

                int day = 0;
                long lastBlockStartTime = -1;
                do {
                    long startTime = cursor.getLong(btsi);
                    long endTime = cursor.getLong(btei);
                    if( DISPLAY_DAY) {
                        Date di = new Date( startTime*1000);
                        if (di.getDay() != day) {
                            day = di.getDay();
                            m_items.add(new DayItem(m_context, startTime));
                        }
                    }
                    if (lastBlockStartTime != startTime) {
                        lastBlockStartTime = startTime;
                        m_items.add(new BlockItem(m_context, Item.TYPE_BLOCK, startTime, endTime));
                    }
                    m_items.add(new SessionItem(m_context, cursor.getInt(id), startTime, endTime,
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
        m_items.clear();
        List<SessionItem> list = new ArrayList<SessionItem>();
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
                    list.add(new SessionItem(m_context, cursor.getInt(id), cursor.getLong(btsi),
                            cursor.getLong(btei), cursor.getString(sti), cursor.getString(spni),
                            cursor.getInt(ri), cursor.getString(tri), cursor.getInt(ctri), cursor
                                    .getInt(ati) == 1));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        Cursor bcursor = m_context.getContentResolver().query(Blocks.CONTENT_URI, null, null, null,
                null);
        if (bcursor != null) {
            if (bcursor.moveToFirst()) {
                int btsi = bcursor.getColumnIndexOrThrow(BlocksColumns.TIME_START);
                int btei = bcursor.getColumnIndexOrThrow(BlocksColumns.TIME_END);
                // Generate the items
                int day = 0;
                do {
                    long startTime = bcursor.getLong(btsi);
                    long endTime = bcursor.getLong(btei);
                    if( DISPLAY_DAY) {
                        Date di = new Date( startTime*1000);
                        if (di.getDay() != day) {
                            day = di.getDay();
                            m_items.add(new DayItem(m_context, startTime));
                        }
                    }
                    m_items.add(new BlockItem(m_context, Item.TYPE_BLOCK, startTime, endTime));
                    boolean slotEmpty = true;
                    while (list.size() > 0) {
                        SessionItem si = list.get(0);
                        if (si.getSessionItem().getStartTime() == startTime
                                && si.getSessionItem().getEndTime() == endTime) {
                            m_items.add(list.remove(0));
                            slotEmpty = false;
                        } else {
                            break;
                        }
                    }
                    if (slotEmpty) {
                        m_items.add(new EmptyBlockItem(m_context, startTime, endTime));
                    }

                } while (bcursor.moveToNext());
            }
            bcursor.close();
        }
    }

    /**
     * List item
     */
    public static class Item {

        public static final int TYPE_DAY = 0;
        public static final int TYPE_BLOCK = 1;
        public static final int TYPE_SESSION = 2;
        public static final int TYPE_EMPTY_BLOCK = 3;
        private final int m_type;

        /**
         * Constructor
         * 
         * @param type The item type
         */
        protected Item(int type) {
            m_type = type;
        }

        /**
         * @return The type
         */
        public int getType() {
            return m_type;
        }
    }

    /**
     * The day separator
     */
    public static class DayItem extends Item {

        private final String m_day;

        /**
         * Constructor
         * 
         * @param context The context
         * @param startTime The startTime
         */
        public DayItem(Context context, long startTime) {
            super(Item.TYPE_DAY);
            m_day = StringUtils.getTimeAsString(context, StringUtils.MONTH_DAY, startTime);
        }

        /**
         * @return The day string
         */
        public String getDay() {
            return m_day;
        }
    }

    /**
     * The time block
     */
    public static class BlockItem extends Item {

        private final String m_time;
        private final long m_startTime;
        private final long m_endTime;

        /**
         * Constructor
         * 
         * @param context The context
         * @param type The type
         * @param startTime The start time
         * @param endTime The end time
         */
        public BlockItem(Context context, int type, long startTime, long endTime) {
            super(type);
            String endClause = StringUtils.getTimeAsString(context, StringUtils.HOUR_MIN_TIME, endTime);
            if( DISPLAY_DAY) {
                String startClause = StringUtils.getTimeAsString(context, StringUtils.HOUR_MIN_TIME, startTime);
                m_time = context.getString(R.string.block_time, startClause, endClause);
            }
            else {
                String startClause = StringUtils.getTimeAsString(context, StringUtils.DAY_HOUR_TIME, startTime);
                m_time = context.getString(R.string.block_time, startClause, endClause);
            }
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
    }

    /**
     * Empty time slot adapter
     */
    public static class EmptyBlockItem extends BlockItem {
        /**
         * Constructor
         * 
         * @param context The context
         * @param startTime The start time
         * @param endTime The end time
         */
        public EmptyBlockItem(Context context, long startTime, long endTime) {
            super(context, Item.TYPE_EMPTY_BLOCK, startTime, endTime);
        }
    }

    /**
     * The session item
     */
    public static class SessionItem extends Item {

        private final Session m_sri;

        /**
         * Constructor
         * 
         * @param context The context
         * @param id The id
         * @param startTime The start time
         * @param endTime The end time
         * @param title The session title
         * @param speakers The comma separated list of speakers
         * @param room The room number
         * @param track The track
         * @param color The track color
         * @param attend true if attending
         */
        public SessionItem(Context context, int id, long startTime, long endTime, String title,
                String speakers, int room, String track, int color, boolean attend) {
            super(TYPE_SESSION);
            m_sri = new Session(context, id, startTime, endTime, title, speakers, room,
                    track, color, attend);
        }

        /**
         * @return The session item
         */
        public Session getSessionItem() {
            return m_sri;
        }
    }
}
