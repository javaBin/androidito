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

package no.java.schedule.activities.fullscreen;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import no.java.schedule.R;
import no.java.schedule.activities.adapters.StarredSessionListener;
import no.java.schedule.activities.adapters.beans.Session;
import no.java.schedule.provider.SessionsContract.BlocksColumns;
import no.java.schedule.provider.SessionsContract.SessionsColumns;
import no.java.schedule.provider.SessionsContract.TracksColumns;
import no.java.schedule.util.AppUtil;
import no.java.schedule.util.StringUtils;

/**
 * Session details activity
 */
public class SessionDetailsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_details_view);
        // Set the title
        ((TextView)findViewById(R.id.title_text)).setText(R.string.session_details_title);

        Uri uri = getIntent().getData();
        Cursor cursor = getContentResolver().query( uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {             //TODO refactor this into buildAll - its the same code
                int sti = cursor.getColumnIndexOrThrow(SessionsColumns.TITLE);
                int spni = cursor.getColumnIndexOrThrow(SessionsColumns.SPEAKER_NAMES);
                int ri = cursor.getColumnIndexOrThrow(SessionsColumns.ROOM);
                int tri = cursor.getColumnIndexOrThrow(TracksColumns.TRACK);
                int ctri = cursor.getColumnIndexOrThrow(TracksColumns.COLOR);
                int ai = cursor.getColumnIndexOrThrow(SessionsColumns.ABSTRACT);
                int ati = cursor.getColumnIndexOrThrow(SessionsColumns.STARRED);
                int btsi = cursor.getColumnIndexOrThrow(BlocksColumns.TIME_START);
                int btei = cursor.getColumnIndexOrThrow(BlocksColumns.TIME_END);
                int mi = cursor.getColumnIndexOrThrow(SessionsColumns.WEB_LINK_ALT);
                int typeIndex = cursor.getColumnIndexOrThrow(SessionsColumns.TYPE);

                ((TextView)findViewById(R.id.session_abstract)).setText(Html.fromHtml(cursor.getString(ai)));
                final Session session = new Session(this,
                        Integer.parseInt(uri.getLastPathSegment()), 
                                cursor.getLong(btsi), cursor.getLong(btei), 
                                cursor.getString(sti), cursor.getString(spni), cursor.getString(ri), cursor
                                .getString(tri), cursor.getInt(ctri),
                        cursor.getInt(ati) == 1,cursor.getString(typeIndex));
                ((TextView)findViewById(R.id.session_title)).setText(session.getTitle());
                CompoundButton sessionStar = (CompoundButton)findViewById(R.id.session_star);
                sessionStar.setChecked(session.isStarred());
                sessionStar.setTag(session);
                sessionStar.setOnClickListener(new StarredSessionListener(this));
                
                ((TextView)findViewById(R.id.session_speakers)).setText(session.getSpeakers());
                TextView sessionTrack = ((TextView)findViewById(R.id.session_track));
                sessionTrack.setText(session.getTrack());
                sessionTrack.setTextColor( session.getColor());
                String startClause = StringUtils.getTimeAsString( this, StringUtils.DAY_HOUR_TIME, session.getStartTime());
                String endClause = StringUtils.getTimeAsString( this, StringUtils.HOUR_MIN_TIME, session.getEndTime());
                String time = getString(R.string.block_time, startClause, endClause);
                ((TextView)findViewById(R.id.session_time)).setText( time);
                Button roomBtn = ((Button)findViewById(R.id.session_room));
                roomBtn.setText(session.getRoom());
                roomBtn.setOnClickListener(new View.OnClickListener() {
                   //TODO Create room map for Oslo Spektrum
                    public void onClick(View v) {
                    	// room 1 and 2 are on the floor level, the others on the first level
                    	String roomNumber = session.getRoom();
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
