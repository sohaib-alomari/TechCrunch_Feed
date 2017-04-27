package com.example.xyzreader.data;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.xyzreader.remote.RemoteEndpointUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UpdaterService extends IntentService {
    ArrayList<ContentProviderOperation> cpo;
    Uri dirUri;
    Time time;

    private static final String TAG = "UpdaterService";

    public static final String BROADCAST_ACTION_STATE_CHANGE
            = "com.example.xyzreader.intent.action.STATE_CHANGE";
    public static final String EXTRA_REFRESHING
            = "com.example.xyzreader.intent.extra.REFRESHING";

    public UpdaterService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
         time = new Time();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
            Log.w(TAG, "Not online, not refreshing.");
            return;
        }

        sendStickyBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, true));

        // Don't even inspect the intent, we only do one thing, and that's fetch content.
        cpo = new ArrayList<ContentProviderOperation>();

        dirUri = ItemsContract.Items.buildDirUri();

        // Delete all items
        cpo.add(ContentProviderOperation.newDelete(dirUri).build());




            String URL_DATA = "https://public-api.wordpress.com/rest/v1.1/sites/techcrunch.com/posts/?pretty=true";

            StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.GET, URL_DATA, new com.android.volley.Response.Listener<String>() {
                @Override
                public void onResponse(String s) {

                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        JSONArray array = jsonObject.getJSONArray("posts");

                        if (array == null) {
                            throw new JSONException("Invalid parsed item array");
                        }

                        for (int i = 0; i < array.length(); i++) {
                            ContentValues values = new ContentValues();
                            JSONObject object = array.getJSONObject(i);
                            JSONObject n=object.getJSONObject("author");
                            JSONObject categories=object.getJSONObject("categories");




                            values.put(ItemsContract.Items.Slug, object.getString("excerpt"));
                            values.put(ItemsContract.Items.AUTHOR, n.getString("name"));
                            values.put(ItemsContract.Items.TITLE, object.getString("title"));
                            values.put(ItemsContract.Items.BODY, object.getString("content"));
                            values.put(ItemsContract.Items.THUMB_URL, object.getString("featured_image"));
                            values.put(ItemsContract.Items.PHOTO_URL, object.getString("featured_image"));
                            values.put(ItemsContract.Items.ASPECT_RATIO, findCategory(categories.toString()));
                            time.parse3339(object.getString("date"));
                            values.put(ItemsContract.Items.PUBLISHED_DATE, time.toMillis(false));
                            cpo.add(ContentProviderOperation.newInsert(dirUri).withValues(values).build());
                        }
                        try {
                            getContentResolver().applyBatch(ItemsContract.CONTENT_AUTHORITY, cpo);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (OperationApplicationException e) {
                            e.printStackTrace();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {


                }
            });

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);


            sendStickyBroadcast(new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, false));




    }

    public String findCategory(String s)
    {

        int index=s.indexOf(":");
       String finaal = s.substring(2,index-1);

        return finaal;

    }

}
