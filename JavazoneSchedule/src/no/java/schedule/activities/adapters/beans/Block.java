package no.java.schedule.activities.adapters.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * A time block
 */
public class Block {

    public Block(String heading){
        this.heading = heading;
    }

    private final List<Session> m_sessions = new ArrayList<Session>();
    protected CharSequence heading;


    /**
     * Get the session from position
     *
     * @param position The position
     * @return The session
     */
    public Session getSession(int position) {
        return m_sessions.get(position);
    }

    /**
     * Add a new session to this block
     *
     * @param si The session item
     */
    public void addSession( Session si)
    {
        m_sessions.add(si);
    }

    /**
     * @return The count of sessions_menu or 1 if the count is zero (for the empty item)
     */
    public int getCount()
    {
        int size = m_sessions.size();
        if( size > 0)
        {
            return size;
        }
        else
        {
            return 1; // The empty view
        }
    }

    /**
     * @return The count of sessions_menu
     */
    public boolean hasSessions()
    {
        return (m_sessions.size() > 0);
    }


    public CharSequence getHeading() {
        return heading;
    }
}
