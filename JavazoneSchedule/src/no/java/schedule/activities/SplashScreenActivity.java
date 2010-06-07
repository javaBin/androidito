package no.java.schedule.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import no.java.schedule.R;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class SplashScreenActivity extends Activity {
    private static final int START_MAIN_ACTIVITY = 0;
    final String CRASH_REPORT_FOLDER = "/sdcard/androidito/stactraces/";


    private static CustomExceptionHandler exceptionHandler;
    private static final String PREFS_NAME = "AndroiditoPreferences";
    private static final String IS_FIRST_RUN = "isFirstRun";
    private static final int CRASHREPORT_UPLOAD_DIALOG = 0;
    private static final int FIRST_RUN_DIALOG = 1;

    {
        try {
            exceptionHandler = new CustomExceptionHandler(
                    CRASH_REPORT_FOLDER, "http://lokling.com/androidito/feedback/error/");
            Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
        } catch (URISyntaxException e) {
            Log.e("Androidito","Error registring excheption handler"+e.toString());
        } catch (MalformedURLException e) {
            Log.e("Androidito","Error registring excheption handler"+e.toString());
        }
    }


    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.splash);

        new DelayedAcitivtyStartHandler().sendEmptyMessageDelayed(0,1000);
    }

    private boolean canSendErrorReport() {
        return exceptionHandler.hasUnsentErrorReports() && !exceptionHandler.isUploadInProgress();
    }

    private boolean isFirstRun() {
        return getSharedPreferences(PREFS_NAME, 0).getBoolean(IS_FIRST_RUN, true);
    }

    private void resetFirstRunFlag() {
        getSharedPreferences(PREFS_NAME,0).edit().putBoolean(IS_FIRST_RUN,true).commit();
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id){
            case FIRST_RUN_DIALOG:
                return createFirstRunInforDialog();
            case CRASHREPORT_UPLOAD_DIALOG:
                return createCrashReportUploadRequest();
            default:
                return null;
        }
    }

    private AlertDialog createFirstRunInforDialog() {
        return new AlertDialog.Builder(this).setTitle("Preview")
                .setMessage("This is a first preview, using the JavaZone 2009 Schedule.  ")
                .setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                        settings.edit().putBoolean(IS_FIRST_RUN,false).commit();
                         startMainActivity();
                    }
                }).create();
    }

    private void startMainActivityAfterDelay(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            //Ignored
        }

        startMainActivity();
    }

    private AlertDialog createCrashReportUploadRequest() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Report crash")
                .setMessage("A crash report found, do you want to submit this to the developer?")
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            dialogInterface.dismiss();
                            exceptionHandler.report(SplashScreenActivity.this);
                            startMainActivity();
                        } catch (IOException e) {
                            Log.e("Androidito","Error uploading errors",e);
                        }
                    }
                })
                .setNeutralButton("Ask me later",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        startMainActivity();
                    }
                })
                .setNegativeButton("No, delete logs",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        exceptionHandler.ignoreLogs();
                        startMainActivity();
                    }
                })
                .create();
        return dialog;
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }



    private class DelayedAcitivtyStartHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
         if (isFirstRun()){
            showDialog(FIRST_RUN_DIALOG);
        } else if (canSendErrorReport()){
            showDialog(CRASHREPORT_UPLOAD_DIALOG);
        } else {
            startMainActivity();
        }

        }
    }
}