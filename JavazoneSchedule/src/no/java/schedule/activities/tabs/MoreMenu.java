package no.java.schedule.activities.tabs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import no.java.schedule.R;
import no.java.schedule.activities.fullscreen.TwitterActivity;
import no.java.schedule.activities.fullscreen.VenueLocationMapActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: erlend
 * Date: Mar 19, 2010
 * Time: 10:37:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class MoreMenu extends ListActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getMenuItems());
        setListAdapter(adapter);

    }


    public List<String> getMenuItems() {

        List<String> l = new ArrayList();

        l.add("Twitter");
        l.add("Area Map");
        l.add("Venue Map");
        l.add("FAQ");
        l.add("Sponsors");
        l.add("About");


        return l;
    }



     @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);    //To change body of overridden methods use File | Settings | File Templates.

    switch (position) {
        case 0:
            openTwitterView();
            break;
        case 1:
            openGoogleMapView();
            break;
        case 2:
            Toast.makeText(getApplicationContext(), "Conference map not implemented yet.", Toast.LENGTH_SHORT).show();
            //AppUtil.showLevel(this, 1);
            break;
        case 3:
             Toast.makeText(getApplicationContext(), "FAQ not implemented yet.", Toast.LENGTH_SHORT).show();
           break;
        case 4:
            Toast.makeText(getApplicationContext(), "Sponsors not implemented yet.", Toast.LENGTH_SHORT).show();
            break;
        case 5:
            showDialog(R.id.dialog_about);
            break;

    }


    }

    protected void openTwitterView() {
        Intent intent = new Intent(this, TwitterActivity.class);
        startActivity(intent);
    }

     protected void openGoogleMapView() {
        Intent intent = new Intent(this, VenueLocationMapActivity.class);
        startActivity(intent);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case R.id.dialog_about:
                return buildAboutDialog();
            default:
                return null;
        }
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

}
