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

package no.java.schedule;

import no.java.schedule.provider.SessionsParser;
import no.java.schedule.provider.SessionsProvider;
import no.java.schedule.provider.SessionsContract.Blocks;
import no.java.schedule.provider.SessionsContract.Sessions;
import no.java.schedule.provider.SessionsContract.Tracks;
import no.java.schedule.provider.SessionsContract.TracksColumns;
import no.java.schedule.util.AppUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

import java.io.InputStream;

/**
 * The main activity
 */
public class MainActivity extends TabActivity {
    private static final String TAG = "MainActivity";

    private TabHost mTabHost;
    private Resources mResources;

    private static final String TAG_SCHEDULE = "schedule";
    private static final String TAG_STARRED = "starred";
    private static final String TAG_TWITTER = "twitter";
    private static final String TAG_OTHER = "other";

    private static final String PREF_STICKY_TAB = "stickyTab";

    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_host_view);

        mTabHost = getTabHost();
        mResources = getResources();

        if (!hasLocalData()) {
            new LocalParseTask().execute();
        }

        // Add various tabs
        addScheduleTab();
        addStarredTab();
        addTracksTab();
        addSpeakersTab();
        //addTwitterTab();
        addOtherTab();

        // Restore last saved sticky tab
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int currentTab = prefs.getInt(PREF_STICKY_TAB, 0);
        mTabHost.setCurrentTab(currentTab);
    }

    /**
     * Check if we have valid data in our local {@link SessionsProvider}
     * database, which means we can show UI right away.
     */
    protected boolean hasLocalData() {
        Cursor cursor = managedQuery(Tracks.CONTENT_URI, new String[] {BaseColumns._ID}, null, null, null);
        return (cursor.getCount() > 0);
    }

    /** {@inheritDoc} */
    @Override
    protected void onPause() {
        super.onPause();

        // When closing activity, save current tab as sticky
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        int currentTab = mTabHost.getCurrentTab();
        editor.putInt(PREF_STICKY_TAB, currentTab);
        editor.commit();
    }

    /**
     * Add tab for full session schedule.
     */
    private void addScheduleTab() {
        Intent intent = new Intent(this, SessionsExpandableListActivity.class);
        intent.setData(Blocks.CONTENT_URI);
        intent.putExtra(SessionsExpandableListActivity.EXTRA_CHILD_MODE,
                SessionsExpandableListActivity.CHILD_MODE_VISIBLE_TRACKS);

        TabSpec spec = mTabHost.newTabSpec(TAG_SCHEDULE);
        spec.setIndicator(mResources.getString(R.string.schedule), mResources
                .getDrawable(R.drawable.ic_tab_schedule));
        spec.setContent(intent);

        mTabHost.addTab(spec);
    }

    /**
     * Add tab for starred sessions.
     */
    private void addStarredTab() {
        Intent intent = new Intent(this, SessionsExpandableListActivity.class);
        intent.setData(Blocks.CONTENT_URI);
        intent.putExtra(SessionsExpandableListActivity.EXTRA_CHILD_MODE,
                SessionsExpandableListActivity.CHILD_MODE_STARRED);

        TabSpec spec = mTabHost.newTabSpec(TAG_STARRED);
        spec.setIndicator(mResources.getString(R.string.starred), mResources
                .getDrawable(R.drawable.ic_tab_starred));
        spec.setContent(intent);

        mTabHost.addTab(spec);
    }

    /**
     * Add tab for starred sessions.
     */
    private void addTracksTab() {
        Intent intent = new Intent(this, SessionsExpandableListActivity.class);
        intent.setData(Blocks.CONTENT_URI);
        //intent.putExtra(SessionsExpandableListActivity.EXTRA_CHILD_MODE,
        //        SessionsExpandableListActivity.CHILD_MODE_STARRED);

        TabSpec spec = mTabHost.newTabSpec(TAG_TWITTER);
        spec.setIndicator(mResources.getString(R.string.tracks), mResources
                .getDrawable(R.drawable.ic_menu_archive));
        spec.setContent(intent);

        mTabHost.addTab(spec);
    }

    private void addSpeakersTab() {
        Intent intent = new Intent(this, SessionsExpandableListActivity.class);
        intent.setData(Blocks.CONTENT_URI);
        //intent.putExtra(SessionsExpandableListActivity.EXTRA_CHILD_MODE,
        //        SessionsExpandableListActivity.CHILD_MODE_STARRED);

        TabSpec spec = mTabHost.newTabSpec(TAG_TWITTER);
        spec.setIndicator(mResources.getString(R.string.speakers), mResources
                .getDrawable(R.drawable.ic_menu_cc));
        spec.setContent(intent);

        mTabHost.addTab(spec);
    }

     private void addOtherTab() {
        Intent intent = new Intent(this, MoreMenu.class);
        intent.setData(Blocks.CONTENT_URI);
        //intent.putExtra(SessionsExpandableListActivity.EXTRA_CHILD_MODE,
        //        SessionsExpandableListActivity.CHILD_MODE_VISIBLE_TRACKS);

        TabSpec spec = mTabHost.newTabSpec(TAG_OTHER);
        spec.setIndicator(mResources.getString(R.string.more), mResources
                .getDrawable(R.drawable.ic_menu_more));
        spec.setContent(intent);

        mTabHost.addTab(spec);
    }

    

    /** {@inheritDoc} */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.sessions, menu);

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // Show or hide tracks menu option depending on tab
        boolean showTracks = (mTabHost.getCurrentTabTag() == TAG_SCHEDULE);
        menu.findItem(R.id.menu_tracks).setVisible(showTracks);

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                this.onSearchRequested();
                return true;
            case R.id.menu_tracks:
                showDialog(R.id.dialog_tracks);
                return true;
            case R.id.menu_level_1:
                AppUtil.showLevel(this, 1);
                return true;
            case R.id.menu_level_1b:
            	AppUtil.showLevel(this, 1);
            	return true;
            case R.id.menu_level_2:
                AppUtil.showLevel(this, 2);
                return true;
            case R.id.menu_about:
                showDialog(R.id.dialog_about);
                return true;
            case R.id.menu_collapse:
                collapseAll();
                return true;
             case R.id.menu_expand:
                collapseAll();
                return true;
//            case R.id.map:
//                openMap();
//                return true;
        }
        return false;
    }

    protected void collapseAll() {
        int currentTab = mTabHost.getCurrentTab();



        View view = mTabHost.getCurrentTabView();
        Log.i("INFO: ", view.getClass().getName());

        
    }



    /** {@inheritDoc} */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case R.id.dialog_load:
                return buildLoadingDialog();
            case R.id.dialog_tracks:
                return buildTracksDialog();
            case R.id.dialog_about:
                return buildAboutDialog();
            default:
                return null;
        }
    }

    /**
     * Build dialog to show when loading data.
     */
    private Dialog buildLoadingDialog() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(getText(R.string.dialog_loading));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        return dialog;
    }

    /**
     * Build dialog to pick the visible tracks.
     */
    private Dialog buildTracksDialog() {
        final ContentResolver resolver = getContentResolver();
        final ContentValues values = new ContentValues();

        final Cursor cursor = managedQuery(Tracks.CONTENT_URI, null, null, null, null);
        cursor.setNotificationUri(resolver, Tracks.CONTENT_URI);

        final int COL_ID = cursor.getColumnIndex(Tracks._ID);

        // Wrap this dialog in a specific theme so that list items have correct
        // text color, otherwise they inherit from our white theme.
        final Context dialogContext = new ContextThemeWrapper(this, android.R.style.Theme_Black);

        AlertDialog.Builder builder = new AlertDialog.Builder(dialogContext);
        builder.setInverseBackgroundForced(true);
        builder.setTitle(R.string.menu_tracks);
        builder.setPositiveButton(getString(android.R.string.ok), null);
        builder.setNegativeButton(getString(android.R.string.cancel), null);

        builder.setMultiChoiceItems(cursor, TracksColumns.VISIBLE, TracksColumns.TRACK,
                new OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        // Build Uri for this specific track
                        cursor.moveToPosition(which);
                        long trackId = cursor.getLong(COL_ID);
                        Uri trackUri = ContentUris.withAppendedId(Tracks.CONTENT_URI, trackId);

                        // Update visible state of this track
                        values.clear();
                        values.put(TracksColumns.VISIBLE, isChecked ? 1 : 0);
                        resolver.update(trackUri, values, null, null);

                        cursor.requery();

                    }
                });

        return builder.create();
    }

    /**
     * Build about dialog.
     */
    private Dialog buildAboutDialog() {

        String versionName = null;
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }

        View view = getLayoutInflater().inflate(R.layout.about, null, false);

        TextView version = (TextView)view.findViewById(R.id.version);
        version.setText(getString(R.string.about_version, versionName));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setView(view);
        builder.setPositiveButton(getString(android.R.string.ok), null);
        builder.setCancelable(true);

        return builder.create();
    }

    /**
     * Background task to parse local data, as provided by static JSON in
     * {@link AssetManager}. This task shows a progress dialog, and finishes
     * loading tabs when completed.
     */
    private class LocalParseTask extends AsyncTask<Void, Void, Void> {
        /** {@inheritDoc} */
        @Override
        protected void onPreExecute() {
            showDialog(R.id.dialog_load);
        }

        /** {@inheritDoc} */
        @Override
        protected Void doInBackground(Void... params) {
            final Context context = MainActivity.this;
            final AssetManager assets = context.getAssets();

            InputStream is;
            try {
                is = assets.open("tracks.json");
                SessionsParser.parseTracks(context, is);
                is.close();

                is = assets.open("sessions.json");
                SessionsParser.parseSchedule(context, is);
                is.close();
                
                is = assets.open("suggest.json");
                SessionsParser.parseSuggest(context, is);
                is.close();
                
                is = assets.open("speakers.json");
                SessionsParser.parseSpeakers(context, is);
                is.close();
            } catch (Exception ex) {
                Log.e(TAG, "Problem parsing schedules", ex);
            }

            return null;
        }

        /** {@inheritDoc} */
        @Override
        protected void onPostExecute(Void result) {
            dismissDialog(R.id.dialog_load);
            // The insert notifications are disabled to avoid the refresh of the list adapters during importing.
            // Notify content observers that the import is complete.
            getContentResolver().notifyChange( Sessions.CONTENT_URI, null);
        }
    }
}
