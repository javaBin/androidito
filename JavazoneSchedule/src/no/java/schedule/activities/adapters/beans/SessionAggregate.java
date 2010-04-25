package no.java.schedule.activities.adapters.beans;

import android.net.Uri;

import java.util.ArrayList;

public class SessionAggregate  implements SessionDisplay, Comparable {
    private ArrayList<SessionDisplay> containedSession = new ArrayList<SessionDisplay>();
    private SessionDisplay sessionToDisplay;
    private String aggregateTitle;

    public SessionAggregate(SessionDisplay sessionToDisplay){
        this.sessionToDisplay = sessionToDisplay;

    }


    public void addSession(SessionDisplay session) {
        containedSession.add(session);
        if (aggregateTitle==null){
            aggregateTitle = "Lightning talks: "+session.getTitle();
        } else {
            aggregateTitle += ", "+session.getTitle();
        }
    }

    public int getId() {
        return sessionToDisplay.getId();
    }

    public String getTitle() {

        if (containedSession.size()>1){
            return aggregateTitle;
        } else {
            return sessionToDisplay.getTitle();
        }
    }

    public String getSpeakers() {
        return sessionToDisplay.getSpeakers();
    }

    public String getRoom() {
        return sessionToDisplay.getRoom();
    }

    public int getColor() {
        return sessionToDisplay.getColor();
    }

    public String getTime() {
        return sessionToDisplay.getTime();
    }

    public long getStartTime() {
        return sessionToDisplay.getStartTime();
    }

    public long getEndTime() {
        return sessionToDisplay.getEndTime();
    }

    public String getTrack() {
        return sessionToDisplay.getTrack();
    }

    public boolean isStarred() {
        return sessionToDisplay.isStarred();
    }

    public Uri getUri() {
        //TODO override if more than one session - so it points to the lightning talk view
        return sessionToDisplay.getUri();
    }

    public String getType() {
        return sessionToDisplay.getType();
    }

    public int compareTo(Object o) {
        return -((SessionAggregate)o).getRoom().compareTo(getRoom());
    }
}
