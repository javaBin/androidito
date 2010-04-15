package no.java.schedule.activities.adapters.beans;

import android.content.Context;
import android.util.Log;
import no.java.schedule.R;
import no.java.schedule.util.StringUtils;

import java.util.Date;

import static java.lang.String.format;

public class TimeBlock extends Block {

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
    public TimeBlock(Context context, long startTime, long endTime) {
        this(context,startTime,endTime,0,0);
    }


    public TimeBlock(Context context, long startTime, long endTime, long startSlotTime, long endSlotTime){
        super("TimeSlot");
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

}
