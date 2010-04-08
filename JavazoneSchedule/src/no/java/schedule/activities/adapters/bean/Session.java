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

package no.java.schedule.activities.adapters.bean;

import android.content.Context;
import android.net.Uri;
import no.java.schedule.R;
import no.java.schedule.provider.SessionsContract.Sessions;
import no.java.schedule.util.StringUtils;

/**
 * The session item
 */
public class Session {
    private final int id;
    private final String time;
    private final long startTime;
    private final long endTime;
    private final String title;
    private final String speakers;
    private final String type;
    private final String room;
    private final String track;
    private final int color;
    private boolean starred;

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
    public Session(Context context, int id, long startTime, long endTime,
            String title, String speakers, String room, String track, int color, boolean starred, String type) {
        this.id = id;
        this.title = title;
        this.speakers = speakers;
        this.type = type;
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
        String startClause = StringUtils.getTimeAsString(context, StringUtils.HOUR_MIN_TIME, startTime);
        String endClause = StringUtils.getTimeAsString(context, StringUtils.HOUR_MIN_TIME, endTime);
        this.time = context.getString(R.string.block_time, startClause, endClause);
        this.track = track;
        this.color = 0xff000000 | color;
        this.starred = starred;

    }

    /**
     * @return The id
     */
    public int getId()
    {
        return id;
    }
    /**
     * @return The title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return The speakers
     */
    public String getSpeakers() {
        return speakers;
    }

    /**
     * @return The room number
     */
    public String getRoom() {
        return room;
    }

    /**
     * @return The color
     */
    public int getColor() {
        return color;
    }

    /**
     * @return The time string
     */
    public String getTime() {
        return time;
    }

    /**
     * @return The start time
     */
    public long getStartTime(){
        return startTime;
    }

    /**
     * @return The end time
     */
    public long getEndTime(){
        return endTime;
    }

    /**
     * @return The track
     */
    public String getTrack() {
        return track;
    }

    /**
     * @return true if starred
     */
    public boolean isStarred() {
        return starred;
    }

    /**
     * @param starred true to star
     */
    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    /**
     * @return The content URI
     */
    public Uri getUri() {
        return Uri.withAppendedPath(Sessions.CONTENT_URI, "" + id);
    }

    public String getType() {
        return type;
    }
}
