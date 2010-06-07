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

package no.java.schedule.activities;

import android.app.*;
import android.content.*;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.*;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;
import no.java.schedule.R;
import no.java.schedule.activities.adapters.ScheduleSorting;
import no.java.schedule.activities.tabs.MoreMenu;
import no.java.schedule.activities.tabs.SessionsExpandableListActivity;
import no.java.schedule.activities.tasks.LoadDatabaseFromIncogitoWebserviceTask;
import no.java.schedule.provider.SessionsContract.Blocks;
import no.java.schedule.provider.SessionsContract.Tracks;
import no.java.schedule.provider.SessionsContract.TracksColumns;
import no.java.schedule.provider.SessionsProvider;
import no.java.schedule.util.AppUtil;

import static no.java.schedule.activities.tabs.SessionsExpandableListActivity.EXTRA_CHILD_MODE;
import static no.java.schedule.activities.tabs.SessionsListActivity.CHILD_MODE.STARRED;
import static no.java.schedule.activities.tabs.SessionsListActivity.CHILD_MODE.VISIBLE_TRACKS;
import static no.java.schedule.provider.SessionsContract.Tracks.CONTENT_URI;

/**
 * The main activity
 */
public class MainActivity extends TabActivity {
    public static final String TAG = "MainActivity";

    private TabHost mTabHost;
    private Resources mResources;

    private static final String TAG_SCHEDULE = "schedule";
    private static final String TAG_STARRED = "starred";
    private static final String TAG_TWITTER = "twitter";
    private static final String TAG_TRACKS = "tracks";
    private static final String TAG_OTHER = "other";

    private static final String PREF_STICKY_TAB = "stickyTab";
    private boolean expanded = true; //TODO - this is global to all tabs, ie wont be in sync with option menu

    private static final String EXTRA_SORTING = "no.java.schedule.extra.sorting";
    private Dialog loadingDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.main_activity);

        mTabHost = getTabHost();
        mResources = getResources();

        if (!localDataNeedsRefresh()) {
            new LoadDatabaseFromIncogitoWebserviceTask(this).execute();
        }

        // Add various tabs
        addScheduleTab(ScheduleSorting.SCHEDULE);
        addStarredTab();
        addOtherTab();

        // Restore last saved sticky tab
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int currentTab = prefs.getInt(PREF_STICKY_TAB, 0);
        mTabHost.setCurrentTab(currentTab);

    }

    /**
     * Check if we have valid data in our local {@link SessionsProvider}
     * database, which means we can show UI right away.
     *
     * @return true if data must be loaded
     */
    protected boolean localDataNeedsRefresh() {
        Cursor cursor = managedQuery(CONTENT_URI, new String[] {BaseColumns._ID}, null, null, null);
        return cursor !=null && (cursor.getCount() > 0) ;
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
    private void addScheduleTab(ScheduleSorting sorting) {
        Intent intent = new Intent(this, SessionsExpandableListActivity.class);
        intent.setData(Blocks.CONTENT_URI);
        intent.putExtra(EXTRA_SORTING, sorting);
        intent.putExtra(EXTRA_CHILD_MODE, VISIBLE_TRACKS);

        TabSpec spec = mTabHost.newTabSpec(TAG_SCHEDULE);
        spec.setIndicator(
                mResources.getString(R.string.schedule),
                mResources.getDrawable(R.drawable.ic_tab_schedule)
        );
        spec.setContent(intent);

        mTabHost.addTab(spec);
    }

    /**
     * Add tab for starred sessions_menu.
     */
    private void addStarredTab() {
        Intent intent = new Intent(this, SessionsExpandableListActivity.class);
        intent.setData(Blocks.CONTENT_URI);
        intent.putExtra(EXTRA_CHILD_MODE,STARRED);

        TabSpec spec = mTabHost.newTabSpec(TAG_STARRED);
        spec.setIndicator(mResources.getString(R.string.starred), mResources
                .getDrawable(R.drawable.ic_tab_starred));
        spec.setContent(intent);

        mTabHost.addTab(spec);
    }





    private void addOtherTab() {
        Intent intent = new Intent(this, MoreMenu.class);
        intent.setData(Blocks.CONTENT_URI);

        TabSpec spec = mTabHost.newTabSpec(TAG_OTHER);
        spec.setIndicator(mResources.getString(R.string.more), mResources.getDrawable(R.drawable.ic_menu_more));
        spec.setContent(intent);

        mTabHost.addTab(spec);
    }



    /** {@inheritDoc} */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.sessions_menu, menu);

        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //TODO handle 
        super.onConfigurationChanged(newConfig);
    }

    /** {@inheritDoc} */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // Show or hide tracks menu option depending on tab
        //boolean showTracks = (mTabHost.getCurrentTabTag() == TAG_SCHEDULE);
        //menu.findItem(R.id.menu_tracks).setVisible(showTracks);

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                this.onSearchRequested();
                return true;
            case R.id.menu_schedule_view:
                showDialog(R.id.dialog_schedule_view);
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
            case R.id.menu_expand_or_collapse:
                toggleExpandAndCollapse(item);
                return true;
            case R.id.menu_refresh:
                new LoadDatabaseFromIncogitoWebserviceTask(this).execute();
        }
        return false;
    }

    private void toggleExpandAndCollapse(MenuItem item) {
        if (expanded){
            collapseAll();
            item.setTitle(getString(R.string.expand));
        } else {
            expandAll();
            item.setTitle(R.string.collapse);
        }
    }

    protected void collapseAll() {
        //int currentTab = mTabHost.getCurrentTab();
        Activity a = getCurrentActivity();
        if (a instanceof SessionsExpandableListActivity) {
            SessionsExpandableListActivity sela = (SessionsExpandableListActivity) a;

            sela.collapseAll();
            Log.i("collapseAll()", "collapsed!");
        }

        expanded = false;
    }

    protected void expandAll() {
        Activity a = getCurrentActivity();
        if (a instanceof SessionsExpandableListActivity) {
            SessionsExpandableListActivity sela = (SessionsExpandableListActivity) a;
            sela.expandAll();
            Log.i("expandAll()", "expanded!");
        }

        expanded = true;
    }



    /** {@inheritDoc} */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case R.id.dialog_load:
                loadingDialog = buildLoadingDialog();
                return loadingDialog;
            //case R.id.dialog_tracks:
            //    return buildTracksDialog();
            case R.id.dialog_schedule_view:
                return buildChooseScheduleView();
            case R.id.dialog_about:
                return buildAboutDialog();
            default:
                return null;
        }
    }

    private Dialog buildChooseScheduleView() {

        final CharSequence[] items = {"Schedule", "Tracks", "Speakers"};


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("View sort");
        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Toast.makeText(MainActivity.this, items[item], Toast.LENGTH_SHORT).show();

                switch(item){
                    case 0: // Schedule
                        sendChangeSortingIntent(ScheduleSorting.SCHEDULE);
                        break;
                    case 1: // Tracks
                        sendChangeSortingIntent(ScheduleSorting.TRACKS);
                        break;
                    case 2: // Speakers
                        sendChangeSortingIntent(ScheduleSorting.SPEAKERS);
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "Error: Unknown sort selected", Toast.LENGTH_SHORT).show();
                }

                dialog.cancel();
            }


        });

        return builder.create();
    }

    private void sendChangeSortingIntent(final ScheduleSorting pScheduleSorting) {

        if (getCurrentActivity() instanceof ScheduleSortingConfigurable){
            ((ScheduleSortingConfigurable)getCurrentActivity()).setSorting(pScheduleSorting);
            expandAll();
        }
    }

    /**
     * Build dialog to show when loading data.
     * @return
     */
    public ProgressDialog buildLoadingDialog() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMessage(getText(R.string.dialog_loading));
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        return dialog;
    }

    /**
     * Build dialog to pick the visible tracks.
     */
    private Dialog buildTracksDialog() {
        final ContentResolver resolver = getContentResolver();
        final ContentValues values = new ContentValues();

        final Cursor cursor = managedQuery(CONTENT_URI, null, null, null, null);
        cursor.setNotificationUri(resolver, CONTENT_URI);

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
                        Uri trackUri = ContentUris.withAppendedId(CONTENT_URI, trackId);

                        // Update visible state of this track
                        values.clear();
                        values.put(TracksColumns.VISIBLE, isChecked ? 1 : 0);
                        resolver.update(trackUri, values, null, null);

                        cursor.requery();

                    }
                });

        return builder.create();
    }

    private Dialog buildAboutDialog() {

        String versionName = findVersion();

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

    private String findVersion() {
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
            return "unknown";
        }
    }


}
