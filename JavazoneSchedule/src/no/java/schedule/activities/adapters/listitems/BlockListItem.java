package no.java.schedule.activities.adapters.listitems;

import android.content.Context;
import no.java.schedule.R;

import static no.java.schedule.util.StringUtils.FULL_TIME;
import static no.java.schedule.util.StringUtils.getTimeAsString;

/**
 * The time block
 */
public class BlockListItem extends ListItem {

    private final String time;
    private final long startTime;
    private final long endTime;

    /**
     * Constructor
     *
     * @param context The context
     * @param type The type
     * @param startTime The start time
     * @param endTime The end time
     */
    public BlockListItem(Context context, TYPE type, long startTime, long endTime) {
        super(type);
        String endClause = getTimeAsString(context, FULL_TIME, endTime);
        String startClause = getTimeAsString(context, FULL_TIME, startTime);

        time = context.getString(R.string.block_time, startClause, endClause);

        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getTime() {
        return time;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
