package com.zaiddabaeen.trialize;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Manage your trial Android applications in real-time.
 * Created by Zaid Daba'een on 4/26/15.
 */
public class Trialize {

    private static final String API = "http://trialize.zaiddabaeen.tk/api/isValid";

    private static Context mContext;
    private static Handler mHandler;

    /** Sends a requests to check if the application's trial is still valid.
     *
     * @param context The application's context
     * @param trialListener A listener for responses of onGoing, ended, and error.
     */
    public static void isValid(final Context context, final TrialListener trialListener){

        mContext = context;
        mHandler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {

                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(API);

                try {
                    // Add your data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("package_name", context.getPackageName()));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    ResponseHandler<String> responseHandler=new BasicResponseHandler();
                    String sResponse = httpclient.execute(httppost, responseHandler);

                    JSONObject response = new JSONObject(sResponse);

                    if (response.getString("status").equals("1")) {
                        final int days_left = Integer.valueOf(response.getString("left"));
                        if(trialListener != null){
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    trialListener.onGoing(days_left);
                                }
                            });
                        }
                        return;
                    } else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (trialListener != null) trialListener.ended();
                            }
                        });
                    }

                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (trialListener != null) trialListener.error();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (trialListener != null) trialListener.error();
                        }
                    });
                } catch (Exception e){
                    e.printStackTrace();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (trialListener != null) trialListener.error();
                        }
                    });
                }

            }
        }).start();

    }

    /**
     * An interface to deliver the responses over
     */
    public interface TrialListener{

        public void onGoing(int days_left);

        public void ended();

        public void error();

    }


}
