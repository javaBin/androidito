package no.java.schedule.activities.adapters.listitems;

import android.content.Context;

import static no.java.schedule.util.StringUtils.MONTH_DAY;
import static no.java.schedule.util.StringUtils.getTimeAsString;

/**
 * The day separator
 */
public class DayListItem extends ListItem {

    private final String day;

    /**
     * Constructor
     *
     * @param context The context
     * @param startTime The startTime
     */
    public DayListItem(Context context, long startTime) {
        super(ListItem.TYPE_DAY);
        day = getTimeAsString(context, MONTH_DAY, startTime);
    }

    /**
     * @return The day string
     */
    public String getDay() {
        return day;
    }
}
