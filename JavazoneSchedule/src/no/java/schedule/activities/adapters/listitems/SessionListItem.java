package no.java.schedule.activities.adapters.listitems;

import no.java.schedule.activities.adapters.bean.Session;

/**
 * The session item
 */
public class SessionListItem extends ListItem {

    private final Session session;

    public SessionListItem(Session session) {
        super(TYPE_SESSION);
        this.session = session;

    }

    /**
     * @return The session item
     */
    public Session getSessionItem() {
        return session;
    }
}
