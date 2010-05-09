package no.java.schedule.activities.adapters.listitems;

public class ListItem {


    public enum TYPE{DAY,BLOCK,SESSION,SESSION_AGGREGATE_HEADER,EMPTY_BLOCK}
    private final TYPE type;

   public ListItem(TYPE type) {
        this.type = type;
    }

    public TYPE getType() {
        return type;
    }
}
