package com.example.leyom.ondrvtest;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.R.id.message;
import static android.content.ContentValues.TAG;
import static android.provider.Telephony.Mms.Part.FILENAME;

/**
 * Created by Leyom on 26/10/2017.
 */

public class FileContent {

    public FileContent(Context context, String fileName, String message) {
        try {
            DataOutputStream dos = new DataOutputStream(
                    new BufferedOutputStream(
                            context.openFileOutput(fileName, Context.MODE_PRIVATE)
                    )
            );


            dos.write(message.getBytes());
            dos.close();


        } catch (FileNotFoundException e) {
            Log.d(TAG, "FileContent: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "FileContent: " + e.getMessage());
        }


    }

    public boolean setFileContent(Context context, String fileName, String message) {

        try {
            DataOutputStream dos = new DataOutputStream(
                    new BufferedOutputStream(
                            context.openFileOutput(fileName, Context.MODE_PRIVATE)
                    )
            );
            dos.write(message.getBytes());
            dos.close();
            return true;
        } catch (FileNotFoundException e) {
            Log.d(TAG, "setFileContent: " + e.getMessage());
            return false;
        } catch (IOException e) {
            Log.d(TAG, "setFileContent: " + e.getMessage());
            return false;
        }

    }

    public DataInputStream getFileContent(Context context, String fileName) {
        DataInputStream stream;
        try {
            stream = new DataInputStream(
                    new BufferedInputStream(
                            context.openFileInput(fileName)
                    )
            );
            return stream;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

    public long getFileContentSize(Context context, String filename) {

        return context.getFileStreamPath(filename).length();
    }

}
