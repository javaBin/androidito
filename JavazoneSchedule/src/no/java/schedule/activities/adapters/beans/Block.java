package no.java.schedule.activities.adapters.beans;

import java.util.ArrayList;
import java.util.List;

public class Block {

    public Block(String heading){
        this.heading = heading;
    }

    protected final List<SessionDisplay> sessions = new ArrayList<SessionDisplay>();
    protected CharSequence heading;


    public SessionDisplay getSession(int position) {
        return sessions.get(position);
    }

    public void addSession( SessionDisplay display) {
        sessions.add(display);
    }

    public int getCount()
    {
        int size = sessions.size();
        if( size > 0) {
            return size;
        } else {
            return 1; // The empty view
        }
    }

    public boolean hasSessions()
    {
        return (sessions.size() > 0);
    }


    public CharSequence getHeading() {
        return heading;
    }
}
