package no.java.schedule.activities.adapters.listitems;

import android.content.Context;

/**
 * Empty time slot adapter
 */
public class EmptyBlockListItem extends BlockListItem {
    /**
     * Constructor
     *
     * @param context The context
     * @param startTime The start time
     * @param endTime The end time
     */
    public EmptyBlockListItem(Context context, long startTime, long endTime) {
        super(context, ListItem.TYPE.EMPTY_BLOCK, startTime, endTime);
    }
}
