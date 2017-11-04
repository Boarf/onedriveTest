package com.example.leyom.ondrvtest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.AuthenticationResult;
import com.microsoft.identity.client.MsalClientException;
import com.microsoft.identity.client.MsalException;
import com.microsoft.identity.client.MsalServiceException;
import com.microsoft.identity.client.MsalUiRequiredException;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.User;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import static android.content.ContentValues.TAG;

/**
 * Created by Leyom on 26/10/2017.
 */

public class OneDriveManagement extends AppCompatActivity{

    // entry point to use the library
    private PublicClientApplication mClientApp;
    // result that allowe to get information about the token
    private AuthenticationResult mAuthResult;

    final static String CLIENT_ID = "138823c8-1e4b-490b-a243-86ffbb99b96f";
    final static String SCOPES [] = {"https://graph.microsoft.com/Files.ReadWrite.All","https://graph.microsoft.com/Files.Read.All"};
    final static String MSGRAPH_URL = "https://graph.microsoft.com/v1.0";
    public static final String APP_FOLDER_PATH = "me/drive/special/approot";
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mClientApp.handleInteractiveRequestRedirect(requestCode,resultCode,data);
    }

    void init() {
        if (!isAvailable("com.android.chrome"))
        {
            Log.d(TAG, "init: Chrome not installed");
            finish();
            return;
        }
        mClientApp = new PublicClientApplication(
                this.getApplicationContext(),
                CLIENT_ID);
        List<User> users = null;

        try {
            users = mClientApp.getUsers();

            // check that there is one user that has already got a tokent
            if (users != null && users.size() == 1) {
                Log.d(TAG, "init: silent");
                mClientApp.acquireTokenSilentAsync(SCOPES,
                        users.get(0),
                        getAuthSilentCallback(this));
            }else {
                // no user
                // A user must connect interactively through a browser
                Log.d(TAG, "init: interactive");
                mClientApp.acquireToken(this,SCOPES, getAuthInteractiveCallback());
            }
        }catch(MsalClientException e){
            Log.d(TAG, "init: MSAL Exceptions Generated while getting users: " + e.toString());
        }catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "init: User at this position does not exists " + e.toString());
        }


    }


    private boolean isAvailable  ( String PackageName) {
        final PackageManager packageManager = getPackageManager();
        if (packageManager == null) {
            return false;
        }


        try {
            packageManager.getPackageInfo(PackageName, PackageManager.GET_ACTIVITIES);
            return true;

        } catch (final PackageManager.NameNotFoundException e) {
            return false;
        }


    }

    // This method permits to get the token without asking the user
    // It is used after the token was retrieved once interactively
    private AuthenticationCallback getAuthSilentCallback (final Activity activity) {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                // Successfully got a token
                Log.d(TAG, "onSuccess: getAuthSilentCallback Successfullt authenticated");

                //Store the authResult if we want some information about the token
                mAuthResult = authenticationResult;



            }

            @Override
            public void onError(MsalException exception) {
                //Failed to acquire a token
                Log.d(TAG, "onError: getAuthSilentCallback Authentication failed: " + exception.toString());

                if(exception instanceof MsalClientException) {
                    // The exception is inside MSAL.
                    // There is more info inside MsalError.java
                    Log.d(TAG, "onError: getAuthSilentCallbackMsalClientException");
                }else if (exception instanceof MsalServiceException) {
                    //Exception when communicating with STS
                    // It is likely a configuration issue
                    Log.d(TAG, "onError: getAuthSilentCallback MsalServiceException");
                }else if (exception instanceof MsalUiRequiredException) {
                    // The tokens expired or there is no session
                    // we must retry with the interactive acquisition
                    mClientApp.acquireToken(activity,SCOPES, getAuthInteractiveCallback());
                }

            }

            @Override
            public void onCancel() {
                // The use cancelled the authentication
                Log.d(TAG, "onCancel: getAuthSilentCallbackUser cancelled login");

            }
        };
    }

    // This method permits to get the token by using a browser for the user to connect and grant authorization
    private AuthenticationCallback getAuthInteractiveCallback () {

        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                // We successfully got a token
                Log.d(TAG, "onSuccess: getAuthInteractiveCallback Successfully authenticated");
                Log.d(TAG, "onSuccess: getAuthInteractiveCallback ID token: " + authenticationResult.getIdToken());

                //Store the result oif the authentication if we want some inforamtion about the token
                mAuthResult = authenticationResult;
            }

            @Override
            public void onError(MsalException exception) {
                //Failed to acquire a token
                Log.d(TAG, "onError: getAuthInteractiveCallback Authentication failed: " + exception.toString());

                if(exception instanceof MsalClientException) {
                    // The exception is inside MSAL.
                    // There is more info inside MsalError.java
                    Log.d(TAG, "onError: getAuthInteractiveCallback MsalClientException");
                }else if (exception instanceof MsalServiceException) {
                    //Exception when communicating with STS
                    // It is likely a configuration issue
                    Log.d(TAG, "onError: MsalClientException MsalServiceException");
                }

            }

            @Override
            public void onCancel() {
                // The use cancelled the authentication
                Log.d(TAG, "onCancel: User cancelled login");
            }
        };

    }


    public boolean upload(String fileName, InputStream stream,long size)  {

        boolean result = false;
        // First, construct the URI that is used to create the URL
        Uri fileUri = Uri.parse(MSGRAPH_URL).buildUpon()
                .appendEncodedPath(APP_FOLDER_PATH + ":")
                .appendEncodedPath(fileName +":")
                .appendPath("content")
                .build();

        try {
            URL fileUrl = new URL(fileUri.toString());
            // Second, create an HTTPURLConnection with openConnection
            // to set the operation and the headers and other parameters
            HttpsURLConnection urlConnection = (HttpsURLConnection) fileUrl.openConnection();
            urlConnection.setRequestMethod("PUT");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Authorization","Bearer " + mAuthResult.getAccessToken());
            urlConnection.setRequestProperty("Content-type","text/plain");
            //To avoid internal buffering when streaming, meaning all the data are in memory
            urlConnection.setFixedLengthStreamingMode(size);
            //Third, connect to the ressource pointed by the URL
            urlConnection.connect();

            //Fourth, operate

            // Get the stream to write associated to the connection
            OutputStream outputStream = urlConnection.getOutputStream();
            // Read the input stream (file here)
            // we can read one byte at a time since the stream is actually buffered
            int b;
            while((b = stream.read()) != -1){
                //write/redirect read bytes to the connection
                outputStream.write(b);
            }
            //outputStream.flush();
            outputStream.close();




            stream.close();

            // we have to get the response otherwise the file is not uploaded
            // It can be also getContent or getResponseMessage
            result = urlConnection.getResponseCode() == 200;


            urlConnection.disconnect();

        } catch (MalformedURLException e) {
            Log.d(TAG, "upload: URL problem");
            e.printStackTrace();

        } catch(IOException e) {
            Log.d(TAG, "upload: URL IO exception");
            e.printStackTrace();
        }

        return result;


    }

    public String download(String fileName) {

        // First, construct the URI that is used to create the URL
        Uri fileUri = Uri.parse(MSGRAPH_URL).buildUpon()
                .appendEncodedPath(APP_FOLDER_PATH + ":")
                .appendEncodedPath(fileName +":")
                .appendPath("content")
                .build();

        try {
            URL fileUrl = new URL(fileUri.toString());
            // Second, create an HTTPURLConnection with openConnection
            // to set the operation and the headers and other parameters
            HttpsURLConnection urlConnection = (HttpsURLConnection) fileUrl.openConnection();
            urlConnection.setRequestMethod("GET");

            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Authorization","Bearer " + mAuthResult.getAccessToken());
            urlConnection.setRequestProperty("Content-type","text/plain");

            //Third, connect to the ressource pointed by the URL
            urlConnection.connect();

            //Fourth, operate

            // Get the stream to read from associated to the connection
            InputStream inputStream = urlConnection.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            // Read the input stream (file here)
            Scanner scanner = new Scanner(bufferedInputStream);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            String response = null;
            if (hasInput) {
                response = scanner.next();
            }
            scanner.close();
            bufferedInputStream.close();
            inputStream.close();
            urlConnection.disconnect();
            return response;

        } catch (MalformedURLException e) {
            Log.d(TAG, "upload: URL problem");
            e.printStackTrace();
        } catch(IOException e) {
            Log.d(TAG, "upload: URL IO exception");
            e.printStackTrace();
        }

        return null;

    }

    // The call to this method make MSAL to forget about the current user
    // The next request to get access token will rbe interactive
    // It is relevant when a user has several email accounts for example
    // Or when there are several users
    public void signOut(){
        List<User> users =null;

        try {
            users = mClientApp.getUsers();
            if(users != null && users.size() == 1){
                mClientApp.remove(users.get(0));

            }else {
                Log.d(TAG, "signOut: problem with users");
            }
        }catch(MsalClientException e) {
            Log.d(TAG, "signOut: MSAL Exception Genenrated while getting users: " + e.toString());
        } catch(IndexOutOfBoundsException e) {
            Log.d(TAG, "signOut: User at this position does not exist: " + e.toString());
        }
    }


}
