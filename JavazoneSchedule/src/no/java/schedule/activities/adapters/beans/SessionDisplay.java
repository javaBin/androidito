package no.java.schedule.activities.adapters.beans;

import android.net.Uri;

public interface SessionDisplay {
    int getId();

    String getTitle();

    String getSpeakers();

    String getRoom();

    int getColor();

    String getTime();

    long getStartTime();

    long getEndTime();

    String getTrack();

    boolean isStarred();

    Uri getUri();

    String getType();
}
