package no.java.schedule.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import no.java.schedule.R;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler, DialogInterface.OnCancelListener {
    private File storageFolder;
    private String webservice;
    private static final String ANROIDITO_STACKTRACE_FILE_PREFIX = "anroiditoStacktrace_";
    private static final String FILE_ENDING = ".txt";
    private FilenameFilter unsentReportFileFilter = new UnsentReportFilenameFilter();
    private boolean uploading = false;

    public CustomExceptionHandler(final String filePath, final String webService) throws URISyntaxException, MalformedURLException {
        storageFolder = new File(filePath);
        webservice = webService;

    }

    public void uncaughtException(final Thread t, final Throwable e) {
        try {
            Log.e("Androidito","Uncaught exception(logging report)",e);
            writeExceptionToFile(t,e);
        } catch (IOException e1) {
            Log.e("Androidito","Error in exception handler",e1);

        } finally {
            System.exit(1);
        }

    }

    public void ignoreLogs(){
        File[] files = storageFolder.listFiles(unsentReportFileFilter);
        for (File file : files) {
            markIgnored(file);
        }
    }

    private void writeExceptionToFile(Thread t, Throwable e) throws IOException {
        if (!storageFolder.exists()){
            storageFolder.mkdirs();
        }

        FileWriter errorReport = new FileWriter(
                new File(storageFolder, ANROIDITO_STACKTRACE_FILE_PREFIX +System.currentTimeMillis()+e.hashCode()+ FILE_ENDING));

        errorReport.write(e.toString());



    }

    public void report(Context context) throws IOException {
        uploading = true;

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Notification notification = new Notification(R.drawable.favicon, "Uploading error report... ", System.currentTimeMillis());
        notification.setLatestEventInfo(context, "Androidito", "Uploading error report... ", contentIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(Notications.NOTIFY_FILE_UPLOAD.ordinal(),notification);

        new Thread(new Runnable(){

            public void run() {

                if (storageFolder.exists()){


                    File[] reports = storageFolder.listFiles(unsentReportFileFilter);

                    for (File report : reports) {
                        try {
                            if (upload(report)){
                                markUploaded(report);
                            }
                        } catch (IOException e) {
                            Log.e("Androidito","Error uploading error report"+e);
                        }
                    }
                 uploading=false;
                }


            }
        }).start();



        notification.setLatestEventInfo(context, "Androidito", "Done uploading error report", contentIntent);
        notificationManager.notify(Notications.NOTIFY_FILE_UPLOAD.ordinal(),notification);

    }

    private void markUploaded(File report) {
        report.renameTo(new File(report.getParent(),"uploaded_"+report.getName()));
    }

    private void markIgnored(File report) {
        report.renameTo(new File(report.getParent(),"uploaded_"+report.getName()));
    }


    private boolean upload(File report) throws IOException {



        HttpPost httpost = new HttpPost(webservice);

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("IDToken1", "username"));
        nvps.add(new BasicNameValuePair("IDToken2", "password"));

        httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));



        httpost.setHeader("User-Agent", "Androidito");
        httpost.setHeader("Accept", "text/plain");
        httpost.setHeader("Content-Type", "application/x-www-form-urlencoded");

        httpost.setEntity(new StringEntity(readFile(report),"UTF-8"));

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(httpost,new BasicHttpContext());

        httpclient.getConnectionManager().shutdown();
        return response.getStatusLine().getStatusCode() == 200;
    }

    private String readFile(File report) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(report));
        String result = "";
        String line;
        while((line = reader.readLine())!=null){
            result+=line+"\n";
        }

        return result;
    }

    public void onCancel(DialogInterface dialogInterface) {

    }

    public boolean hasUnsentErrorReports() {
        return storageFolder.listFiles(unsentReportFileFilter).length > 0;
    }

    public boolean isUploadInProgress() {
        return uploading;
    }

    private static class UnsentReportFilenameFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            return name.startsWith(ANROIDITO_STACKTRACE_FILE_PREFIX);
        }
    }
}
