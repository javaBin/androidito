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

package no.java.schedule.activities.adapters.beans;

import android.content.Context;
import android.net.Uri;
import no.java.schedule.R;
import no.java.schedule.provider.SessionsContract.Sessions;
import no.java.schedule.util.StringUtils;

/**
 * The session item
 */
public class Session implements SessionDisplay {
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

    public int getId()
    {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSpeakers() {
        return speakers;
    }

    public String getRoom() {
        return room;
    }

    public int getColor() {
        return color;
    }

    public String getTime() {
        return time;
    }

    public long getStartTime(){
        return startTime;
    }

    public long getEndTime(){
        return endTime;
    }

    public String getTrack() {
        return track;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public Uri getUri() {
        return Uri.withAppendedPath(Sessions.CONTENT_URI, "" + id);
    }

    public String getType() {
        return type;
    }
}
