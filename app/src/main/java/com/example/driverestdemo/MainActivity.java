package com.example.driverestdemo;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends BaseDriveActivity {

    private static final String APPLICATION_NAME = "Drive REST API Demo";
    private static final String FOLDER_NAME = "cakes"; //This is your google drive folder

    //Global instance of the HTTP transport
    private static HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

    // Global instance of the JSON factory
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private Drive mDrive;

    Button btnDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnDownload = findViewById(R.id.download);

        signIn();
        checkDirectory();

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //download images in Asynchronous Task
                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                    ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressDialog.setMessage("Downloading Images");
                        progressDialog.show();
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            listDriveFiles();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        progressDialog.dismiss();
                    }
                };
                task.execute();
            }
        });
    }

    @Override
    protected void onDriveClientReady(String displayName, String email, Uri avatar) {
        // Build a new authorized API client service.
        mDrive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    //checking if directory exists, else creates
    public void checkDirectory() {
        java.io.File folder = new java.io.File(Environment.getExternalStorageDirectory() +
                java.io.File.separator + "/Android/data/" + getPackageName());
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
    }


    //method to download images from Google Drive
    public void listDriveFiles() throws IOException {
        //Get all folders from drive
        FileList result = mDrive.files().list()
                .setQ("mimeType = 'application/vnd.google-apps.folder'")
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found..");
        } else {
            for (File file : files) {
                //get all files from folder named 'images'
                if (file.getName().equals(FOLDER_NAME)) {
                    FileList cakeImages = mDrive.files().list()
                            .setQ("'"+file.getId()+"' in parents")
                            .setFields("nextPageToken, files(id, name)")
                            .execute();
                    List<File> files1 = cakeImages.getFiles();
                    if (files1 == null || files.isEmpty()) {
                        System.out.println("No Files Found");
                    } else {
                        for(File file1 : files1) {
                            java.io.File imageFile;
                            FileOutputStream fileOutputStream = null;
                            try {
                                imageFile = new java.io.File(Environment.getExternalStorageDirectory()+"/Android/data/"
                                        +getPackageName()+java.io.File.separator+file1.getName());
                                imageFile.createNewFile();
                                fileOutputStream = new FileOutputStream(imageFile);

                                //writing file
                                String fileId = file1.getId();
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                mDrive.files().get(fileId).executeMediaAndDownloadTo(byteArrayOutputStream);
                                byte[] content = byteArrayOutputStream.toByteArray();
                                fileOutputStream.write(content);

                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                if (fileOutputStream != null) {
                                    fileOutputStream.close();
                                }
                            }
                        }
                    }
                }
            }
        }

    }


}
