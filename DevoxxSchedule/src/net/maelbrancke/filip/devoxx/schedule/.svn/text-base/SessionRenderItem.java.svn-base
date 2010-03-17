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

import android.content.Context;
import android.net.Uri;

import net.maelbrancke.filip.devoxx.schedule.provider.SessionsContract.Sessions;
import net.maelbrancke.filip.devoxx.schedule.util.StringUtils;
import net.maelbrancke.filip.devoxx.schedule.R;

/**
 * The session item
 */
public class SessionRenderItem {
    private final int m_id;
    private final String m_time;
    private final long m_startTime;
    private final long m_endTime;
    private final String m_title;
    private final String m_speakers;
    private final String m_room;
    private final String m_track;
    private final int m_color;
    private boolean m_starred;

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
     * @param starred true if starred
     */
    public SessionRenderItem(Context context, int id, long startTime, long endTime,
            String title, String speakers, int room, String track, int color, boolean starred) {
        m_id = id;
        m_title = title;
        m_speakers = speakers;
        m_room = context.getString(R.string.sessions_room) + " " + room;
        m_startTime = startTime;
        m_endTime = endTime;
        String startClause = StringUtils.getTimeAsString(context, StringUtils.HOUR_MIN_TIME, startTime);
        String endClause = StringUtils.getTimeAsString(context, StringUtils.HOUR_MIN_TIME, endTime);
        m_time = context.getString(R.string.block_time, startClause, endClause);
        m_track = track;
        m_color = 0xff000000 | color;
        m_starred = starred;
    }

    /**
     * @return The id
     */
    public int getId()
    {
        return m_id;
    }
    /**
     * @return The title
     */
    public String getTitle() {
        return m_title;
    }

    /**
     * @return The speakers
     */
    public String getSpeakers() {
        return m_speakers;
    }

    /**
     * @return The room number
     */
    public String getRoom() {
        return m_room;
    }

    /**
     * @return The color
     */
    public int getColor() {
        return m_color;
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
    public long getStartTime(){
        return m_startTime;
    }

    /**
     * @return The end time
     */
    public long getEndTime(){
        return m_endTime;
    }

    /**
     * @return The track
     */
    public String getTrack() {
        return m_track;
    }

    /**
     * @return true if starred
     */
    public boolean isStarred() {
        return m_starred;
    }

    /**
     * @param starred true to star
     */
    public void setStarred(boolean starred) {
        m_starred = starred;
    }

    /**
     * @return The content URI
     */
    public Uri getUri() {
        return Uri.withAppendedPath(Sessions.CONTENT_URI, "" + m_id);
    }
}
