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

import net.maelbrancke.filip.devoxx.schedule.provider.SessionsContract.BlocksColumns;
import net.maelbrancke.filip.devoxx.schedule.provider.SessionsContract.SessionsColumns;
import net.maelbrancke.filip.devoxx.schedule.provider.SessionsContract.TracksColumns;
import net.maelbrancke.filip.devoxx.schedule.util.AppUtil;
import net.maelbrancke.filip.devoxx.schedule.util.StringUtils;
import net.maelbrancke.filip.devoxx.schedule.R;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

/**
 * Session details activity
 */
public class SessionDetailsActivity extends Activity {
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_details_view);
        // Set the title
        ((TextView)findViewById(R.id.title_text)).setText(R.string.session_details_title);

        Uri uri = getIntent().getData();
        Cursor cursor = getContentResolver().query( uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int sti = cursor.getColumnIndexOrThrow(SessionsColumns.TITLE);
                int spni = cursor.getColumnIndexOrThrow(SessionsColumns.SPEAKER_NAMES);
                int ri = cursor.getColumnIndexOrThrow(SessionsColumns.ROOM);
                int tri = cursor.getColumnIndexOrThrow(TracksColumns.TRACK);
                int ctri = cursor.getColumnIndexOrThrow(TracksColumns.COLOR);
                int ai = cursor.getColumnIndexOrThrow(SessionsColumns.ABSTRACT);
                int ati = cursor.getColumnIndexOrThrow(SessionsColumns.STARRED);
                int btsi = cursor.getColumnIndexOrThrow(BlocksColumns.TIME_START);
                int btei = cursor.getColumnIndexOrThrow(BlocksColumns.TIME_END);
                int mi = cursor.getColumnIndexOrThrow(SessionsColumns.LINK_ALT);

                ((TextView)findViewById(R.id.session_abstract)).setText(cursor.getString(ai));
                final SessionRenderItem sessionItem = new SessionRenderItem(this,
                        Integer.parseInt(uri.getLastPathSegment()), 
                                cursor.getLong(btsi), cursor.getLong(btei), 
                                cursor.getString(sti), cursor.getString(spni), cursor.getInt(ri), cursor
                                .getString(tri), cursor.getInt(ctri),
                        cursor.getInt(ati) == 1);
                ((TextView)findViewById(R.id.session_title)).setText(sessionItem.getTitle());
                CompoundButton cb = (CompoundButton)findViewById(R.id.session_star);
                cb.setChecked(sessionItem.isStarred());
                cb.setOnClickListener(new View.OnClickListener() {
                    /*
                     * @see android.view.View.OnClickListener#onClick(android.view.View)
                     */
                    public void onClick(View v) {
                        // Update the item
                        sessionItem.setStarred(!sessionItem.isStarred());

                        // Update the content provider
                        ContentValues values = new ContentValues();
                        values.put(SessionsColumns.STARRED, sessionItem.isStarred() ? 1 : 0);
                        getContentResolver().update(sessionItem.getUri(), values, null, null);
                    }
                });
                ((TextView)findViewById(R.id.session_speakers)).setText(sessionItem.getSpeakers());
                TextView st = ((TextView)findViewById(R.id.session_track));
                st.setText(sessionItem.getTrack());
                st.setTextColor( sessionItem.getColor());
                String startClause = StringUtils.getTimeAsString( this, StringUtils.DAY_HOUR_TIME, sessionItem.getStartTime());
                String endClause = StringUtils.getTimeAsString( this, StringUtils.HOUR_MIN_TIME, sessionItem.getEndTime());
                String time = getString(R.string.block_time, startClause, endClause);
                ((TextView)findViewById(R.id.session_time)).setText( time);
                Button roomBtn = ((Button)findViewById(R.id.session_room));
                roomBtn.setText(sessionItem.getRoom());
                roomBtn.setOnClickListener(new View.OnClickListener() {
                    /*
                     * @see android.view.View.OnClickListener#onClick(android.view.View)
                     */
                    public void onClick(View v) {
                    	// room 1 and 2 are on the floor level, the others on the first level
                    	String roomNumber = sessionItem.getRoom();
                    	if ((roomNumber.equalsIgnoreCase("room 1")) || (roomNumber.equalsIgnoreCase("room 2"))) {
                    		AppUtil.showLevel(SessionDetailsActivity.this, 1);
                    	} else {
                    		AppUtil.showLevel(SessionDetailsActivity.this, 2);
                    	}
                        
                    }
                });
                if( cursor.getString( mi) != null)
                {
                    ((TextView)findViewById(R.id.session_moderation)).setText( getString( R.string.session_details_moderation, cursor.getString( mi)));
                }
                else
                {
                    findViewById(R.id.session_moderation_layout).setVisibility(View.GONE);
                }

                cursor.close();
            } else {
                cursor.close();
                finish();
                return;
            }
        } else {
            finish();
            return;
        }
    }
}
