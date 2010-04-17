package no.java.schedule.activities.tasks;

class Progress {
    private String message;
    private int mainProgress;
    private int subProgress;

    public Progress(String message, int mainProgress, int subProgress) {
        this.message = message;
        this.mainProgress = mainProgress;
        this.subProgress = subProgress;
    }



    public String getMessage() {
        return message;
    }

    public int getMainProgress() {
        return mainProgress;
    }

    public int getSubProgress() {
        return subProgress;
    }
}
