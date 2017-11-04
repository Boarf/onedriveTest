package com.example.leyom.ondrvtest;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.leyom.ondrvtest.OneDriveManagement;
import com.example.leyom.ondrvtest.R;

import org.w3c.dom.Text;

import static android.R.attr.id;
import static android.R.id.message;
import static android.util.Log.w;

public class MainActivity extends OneDriveManagement {

    FileContent mFileContent;
    ProgressBar pb_wait;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create local file with a string content
        mFileContent = new FileContent(this, "onedrive.txt", "C'est un essai de chargement d'un fichier et de téléchargement de fichier et voilà");
        // Create application folder and get access token
        init();

        Button bn_download = (Button) findViewById(R.id.button_download);
        Button bn_upload = (Button) findViewById(R.id.button_upload);
        Button bn_signout = (Button) findViewById(R.id.button_signout);
        pb_wait = (ProgressBar) findViewById(R.id.progressBar);
        pb_wait.setVisibility(View.GONE);

        TextView tv_download = (TextView) findViewById(R.id.edit_to_upload);

        bn_upload.setOnClickListener(new FileContentUpload("onedrive.txt"));
        bn_download.setOnClickListener(new FileContentDownload("onedrive.txt"));
        bn_signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });



    }


    private class FileContentDownload implements View.OnClickListener {
        String mFileName;

        public FileContentDownload(String fileName) {
            mFileName = fileName;
        }

        @Override
        public void onClick(View v) {


            new AsyncTask<Void, Void, String>() {
                @Override
                protected void onPreExecute() {
                    pb_wait.setVisibility(View.VISIBLE);
                }

                @Override
                protected String doInBackground(Void... params) {
                    String content;
                    content = download(mFileName);
                    return content;
                }

                @Override
                protected void onPostExecute(String content) {

                    pb_wait.setVisibility(View.GONE);
                    if (content != null) {
                        TextView editText = (TextView) findViewById(R.id.text_downloaded);
                        editText.setText(content);
                        Toast.makeText(getApplicationContext(), "Download done", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Download problem", Toast.LENGTH_LONG).show();
                    }


                }
            }.execute();
        }


    }


private class FileContentUpload implements View.OnClickListener {
    String mFileName;

    public FileContentUpload(String fileName) {
        mFileName = fileName;
    }

    @Override
    public void onClick(View v) {
        EditText ed_downloadMessage = (EditText) findViewById(R.id.edit_to_upload);
        String message = ed_downloadMessage.getText().toString();


        if (mFileContent.setFileContent(getApplicationContext(), "onedrive.txt", message)) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected void onPreExecute() {
                    pb_wait.setVisibility(View.VISIBLE);
                }

                @Override
                protected Void doInBackground(Void... params) {
                    Context context = getApplicationContext();
                    upload(mFileName,
                            mFileContent.getFileContent(context, mFileName),
                            mFileContent.getFileContentSize(context, mFileName));
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    pb_wait.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Upload done", Toast.LENGTH_LONG);
                }
            }.execute();

        }
    }
}
}
