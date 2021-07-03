package com.example.basicpdfviewer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends AppCompatActivity {

    String TAG = "testing1234";

    DownloadManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button b = findViewById(R.id.download_button);
        b.setOnClickListener(v -> {
            EditText e = findViewById(R.id.url_edit_text);
            String url = e.getText().toString();
            downloadPDF("https://www.adobe.com/support/products/enterprise/knowledgecenter/media/c4611_sample_explain.pdf");
        });
    }

    private void downloadPDF(String url) {
        Runnable r = () -> {

            if(!checkWriteExternalPermission()){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);            }

            try{
                File pdfFile = new File(Environment.getExternalStorageDirectory(), "AlcherTicket/download.pdf");
                pdfFile.getParentFile().mkdirs();
                pdfFile.createNewFile();
                Log.d(TAG, pdfFile.getAbsolutePath());
                FileDownloader.downloadFile(url, pdfFile);
                Log.d(TAG, "created file");
                startIntent(pdfFile);

            }catch (IOException e){
                e.printStackTrace();
            }

            /*manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse((url)));
            String title = "sample1.pdf";
            request.setMimeType("application/PDF");
            request.setTitle(title);
            request.setDescription("download file please wait.....");
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);

            Log.d(TAG, String.valueOf(manager.enqueue(request)));*/


        };
        new Thread(r).start();



    }

    private void startIntent(File file) {
        if (file.exists())
        {
            Intent intent=new Intent(Intent.ACTION_VIEW);
            //Uri uri = Uri.fromFile(file);

            Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", file);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try
            {
                startActivity(intent);
            }
            catch(ActivityNotFoundException e)
            {
                Log.d(TAG, "No Application available to view pdf");
            }
        }
        else{
            Log.d(TAG, "File not found");
        }
    }

    public static class FileDownloader {
        private static final int  MEGABYTE = 1024 * 1024;

        public static void downloadFile(String fileUrl, File directory){
            try {

                URL url = new URL(fileUrl);
                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(directory);
                int totalSize = urlConnection.getContentLength();

                byte[] buffer = new byte[MEGABYTE];
                int bufferLength = 0;
                while((bufferLength = inputStream.read(buffer))>0 ){
                    fileOutputStream.write(buffer, 0, bufferLength);
                }
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkWriteExternalPermission()
    {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = MainActivity.this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
}