package no.java.schedule.activities.adapters.listitems;

public class ListItem {

    public static final int TYPE_DAY = 0;
    public static final int TYPE_BLOCK = 1;
    public static final int TYPE_SESSION = 2;
    public static final int TYPE_EMPTY_BLOCK = 3;
    private final int type;

    /**
     * Constructor
     *
     * @param type The item type
     */
    public ListItem(int type) {
        this.type = type;
    }

    /**
     * @return The type
     */
    public int getType() {
        return type;
    }
}
