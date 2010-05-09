/*
 * Copyright (C) 2009 Virgil Dobjanschi, Jeff Sharkey
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.java.schedule.activities.tabs;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import no.java.schedule.R;
import no.java.schedule.activities.adapters.SessionsAdapter;
import no.java.schedule.activities.adapters.listitems.ListItem;
import no.java.schedule.activities.adapters.listitems.SessionListItem;
import no.java.schedule.activities.fullscreen.SessionDetailsActivity;
import no.java.schedule.provider.SessionsContract.Sessions;

import static no.java.schedule.activities.tabs.SessionsListActivity.EXTRA_SELECTION;
import static no.java.schedule.activities.tabs.SessionsListActivity.EXTRA_SELECTION_ARGS;

public class SessionsAggregateListActivity extends ListActivity implements OnItemClickListener{

    private SessionsAdapter mSessionAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        final Intent intent = getIntent();

        Uri uri = Sessions.CONTENT_URI;
        String selection = intent.getStringExtra(EXTRA_SELECTION);
        String[] selectionArgs = intent.getStringArrayExtra(EXTRA_SELECTION_ARGS);

        mSessionAdapter = new SessionsAdapter( this, uri, selection, selectionArgs, null,SessionsAdapter.MODE.SESSION_AGGREGATE_VIEW);

        getListView().setAdapter(mSessionAdapter);
        getListView().setOnItemClickListener( this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSessionAdapter != null) {
            getListView().setAdapter(null);
            mSessionAdapter.close();
        }
    }

    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
    {
        ListItem listItem = mSessionAdapter.getItemByPosition(position);
        switch (listItem.getType()){

            case SESSION:
                startDetailActivityFor(listItem);
                return;

            default:
                return;
        }
    }


    private void startDetailActivityFor(ListItem listItem) {
        SessionListItem si = (SessionListItem) listItem;
        // Start details activity for selected listItem
        Intent intent = new Intent( this, SessionDetailsActivity.class);
        intent.setData( si.getSessionItem().getUri());
        startActivityForResult( intent, 0);
    }
}