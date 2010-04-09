package no.java.schedule.activities.adapters.beans;

import android.content.Context;
import android.util.Log;
import no.java.schedule.R;
import no.java.schedule.activities.adapters.bean.Session;
import no.java.schedule.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;

/**
 * A time block
 */
public class Block {
    private final List<Session> m_sessions = new ArrayList<Session>();
    private final String timeString;
    private final long startTime;
    private final long endTime;
    private long startSlotTime;
    private long endSlotTime;
    private boolean lightningTalk;

    /**
     * Constructor
     *
     * @param context The context
     * @param startTime The start time
     * @param endTime The end time
     */
    public Block(Context context, long startTime, long endTime) {

        this(context,startTime,endTime,0,0);

    }


    public Block(Context context, long startTime, long endTime, long startSlotTime, long endSlotTime){
        this.startTime = startTime;
        this.endTime = endTime;

        this.startSlotTime = startSlotTime;
        this.endSlotTime = endSlotTime;


        if (startTime!=0){
            lightningTalk = true;
        }

        Log.d(getClass().getSimpleName(), format("Creating new block: %s - %s", new Date(startTime), new Date(endTime)));
        String startClause = StringUtils.getTimeAsString( context, StringUtils.DAY_HOUR_TIME, startTime);
        String endClause = StringUtils.getTimeAsString( context, StringUtils.HOUR_MIN_TIME, endTime);

        timeString = context.getString(R.string.block_time, startClause, endClause);



    }

    /**
     * @return The time string
     */
    public String getTime() {
        return timeString;
    }

    /**
     * @return The start time
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @return The end time
     */
    public long getEndTime() {
        return endTime;
    }

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
}
