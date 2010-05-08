package no.java.schedule.activities.adapters.beans;

import android.net.Uri;

import java.util.ArrayList;

public class SessionAggregate  implements SessionDisplay {

    private ArrayList<SessionDisplay> containedSession = new ArrayList<SessionDisplay>();
    private String title;
    private long startSlotTime;
    private long endSlotTime;
    private SessionDisplay base;

    public SessionAggregate(String title, long startSlotTime, long endSlotTime, SessionDisplay base){
        this.title = title;
        this.startSlotTime = startSlotTime;
        this.endSlotTime = endSlotTime;
        this.base = base;
    }


    public void addSession(SessionDisplay session) {
        containedSession.add(session);

    }

    public int getId() {
        return base.getId();
    }

    public String getTitle() {
        return title;
    }

    public String getSpeakers() {
        return "Various speakers";
    }

    public String getRoom() {
        return base.getRoom();
    }

    public int getColor() {
        return 0;
    }

    public String getTime() {
        return base.getTime();
    }

    public long getStartTime() {
        return base.getStartTime();
    }

    public long getEndTime() {
        return base.getEndTime();
    }

    public String getTrack() {
        return base.getTrack();
    }

    public boolean isStarred() {
        return base.isStarred();
    }

    public Uri getUri() {
        //TODO override if more than one session - so it points to the lightning talk view
        return base.getUri();
    }

    public String getType() {
        return "Various";
    }

    public long getStartSlotTime() {
        return startSlotTime;
    }

    public long getEndSlotTime() {
        return endSlotTime;
    }
}
