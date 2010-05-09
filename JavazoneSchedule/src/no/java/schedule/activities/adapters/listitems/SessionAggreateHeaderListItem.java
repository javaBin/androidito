package no.java.schedule.activities.adapters.listitems;

public class SessionAggreateHeaderListItem extends ListItem {
    private CharSequence title;

    public SessionAggreateHeaderListItem(String title) {
        super(TYPE.SESSION_AGGREGATE_HEADER);
        this.title = title;
    }

    public CharSequence getTitle() {
        return title;
    }
}
