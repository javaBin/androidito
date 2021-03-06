package no.java.schedule.activities.adapters.beans;

import android.content.Context;
import no.java.schedule.R;
import no.java.schedule.activities.adapters.ScheduleTimeUtil;
import no.java.schedule.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class TimeBlock extends Block {

    private final long startTime;
    private final long endTime;
    private long startSlotTime;
    private long endSlotTime;
    private boolean lightningTalk;
    private HashMap<String,SessionDisplay> roomAggregatedSessions = new HashMap<String,SessionDisplay>();



    public TimeBlock(Context context, Session session){
        this(context,session.getStartTime(),session.getEndTime());

    }

    public TimeBlock(Context context, long startTime, long endTime){
        super("TimeSlot");

        this.startTime = startTime;
        this.endTime = endTime;

        this.startSlotTime = ScheduleTimeUtil.findBlockStart(startTime);
        this.endSlotTime = ScheduleTimeUtil.findBlockEnd(endTime);


        if (startTime!=0){
            lightningTalk = true;
        }

        //Log.d(getClass().getSimpleName(), format("Creating new block: %s - %s", new Date(startTime), new Date(endTime)));
        String startClause = StringUtils.getTimeAsString( context, StringUtils.DAY_HOUR_TIME, startSlotTime);
        String endClause = StringUtils.getTimeAsString( context, StringUtils.HOUR_MIN_TIME, endSlotTime);

        heading = context.getString(R.string.block_time, startClause, endClause);

    }


    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getStartSlotTime() {
        return startSlotTime;
    }

    public long getEndSlotTime() {
        return endSlotTime;
    }

    @Override
    public void addSession(SessionDisplay session) {
        super.addSession(session);
        final String key = session.getRoom();

        final SessionDisplay currentSessionAtRoom = roomAggregatedSessions.get(session.getRoom());
        if (currentSessionAtRoom == null){
            roomAggregatedSessions.put(key,session);
        } else if (roomAggregatedSessions.get(key) instanceof SessionAggregate){
            ((SessionAggregate)currentSessionAtRoom).addSession(session);
        } else {
            final SessionAggregate sessionAggregate = new SessionAggregate("Lightning Talks",startSlotTime,endSlotTime,currentSessionAtRoom);
            sessionAggregate.addSession(currentSessionAtRoom);
            sessionAggregate.addSession(session);
            roomAggregatedSessions.put(key, sessionAggregate);
        }


        sessions.clear();
        final ArrayList arrayList = new ArrayList<SessionDisplay>(roomAggregatedSessions.values());
        Collections.sort(arrayList, new Comparator<SessionDisplay>(){

            public int compare(SessionDisplay o1, SessionDisplay o2) {
                return -o1.getRoom().compareTo(o2.getRoom());
            }
        });
        sessions.addAll(arrayList);


    }


}
