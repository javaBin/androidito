package no.java.schedule.activities.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.CheckBox;
import no.java.schedule.activities.adapters.beans.SessionDisplay;

import static no.java.schedule.provider.SessionsContract.SessionsColumns.STARRED;

public class StarredSessionListener implements View.OnClickListener {
    private Context context;

    public StarredSessionListener(Context context){

        this.context = context;
    }


    public void onClick(final View view) {
        final CheckBox checkBox = (CheckBox) view;
        
        AsyncTask updateStar = new ToggleStarAsyncTask(view, checkBox);

        updateStar.execute();

    }


    private class ToggleStarAsyncTask extends AsyncTask<Object,Object,Object> {
        private final View view;
        private final CheckBox checkBox;

        public ToggleStarAsyncTask(View view, CheckBox checkBox) {
            this.view = view;
            this.checkBox = checkBox;
        }

        @Override
               protected Object doInBackground(Object... objects) {
            final SessionDisplay session = (SessionDisplay) view.getTag();
            // Update the content provider
            ContentValues values = new ContentValues();
            boolean starred = checkBox.isChecked();
            values.put(STARRED, starred ? 1 : 0 );
            context.getContentResolver().update(session.getUri(), values, null, null);
            return null;
        }

    }
}
