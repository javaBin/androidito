package no.java.schedule;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import no.java.schedule.provider.SessionsContract;

public class TracksExpandableListActivity extends ListActivity {
    private Cursor cursor;

    @Override
     protected void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);


        cursor = getContentResolver().query(SessionsContract.Tracks.CONTENT_URI,null,null,null,null);

        startManagingCursor(cursor);

        ListAdapter adapter = new SimpleCursorAdapter(
                 this, // Context.
                 R.layout.track_row,
                 cursor,
                 new String[] {SessionsContract.TracksColumns.TRACK},
                 new int[]{R.id.track_title});                                 

         // Bind to our new adapter.
         setListAdapter(adapter);
     }
}
