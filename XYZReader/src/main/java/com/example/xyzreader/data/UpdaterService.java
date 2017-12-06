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
import android.util.ArrayMap;

import com.example.xyzreader.remote.RemoteEndpointUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import timber.log.Timber;

public class UpdaterService extends IntentService {

    public static final String BROADCAST_ACTION_STATE_CHANGE
            = "com.example.xyzreader.intent.action.STATE_CHANGE";
    public static final String EXTRA_REFRESHING
            = "com.example.xyzreader.intent.extra.REFRESHING";

    private static final ArrayMap<String, String> FIELD_MAP;
    static {
        FIELD_MAP = new ArrayMap<>();
        FIELD_MAP.put(ItemsContract.Items.SERVER_ID, "id" );
        FIELD_MAP.put(ItemsContract.Items.AUTHOR, "author" );
        FIELD_MAP.put(ItemsContract.Items.TITLE, "title" );
        FIELD_MAP.put(ItemsContract.Items.BODY, "body" );
        FIELD_MAP.put(ItemsContract.Items.THUMB_URL, "thumb" );
        FIELD_MAP.put(ItemsContract.Items.PHOTO_URL, "photo" );
        FIELD_MAP.put(ItemsContract.Items.ASPECT_RATIO, "aspect_ratio" );
        FIELD_MAP.put(ItemsContract.Items.PUBLISHED_DATE, "published_date");
    };

    public UpdaterService() {
        super(UpdaterService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean proceed = (cm != null);
        if (proceed) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni == null || !ni.isConnected()) {
                Timber.w("Not online, not refreshing.");
                proceed = false;
            }
        } else {
            Timber.w("No connectivity service");
        }
        if (!proceed) {
            return;
        }

        postEvent(true);
//        sendStickyBroadcast(
//                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, true));

        // Don't even inspect the intent, we only do one thing, and that's fetch content.
        ArrayList<ContentProviderOperation> cpo = new ArrayList<>();

        Uri dirUri = ItemsContract.Items.buildDirUri();

        // Delete all items
        cpo.add(ContentProviderOperation.newDelete(dirUri).build());

        try {
            JSONArray array = RemoteEndpointUtil.fetchJsonArray();
            if (array == null) {
                throw new JSONException("Invalid parsed item array" );
            }

            for (int i = 0; i < array.length(); i++) {
                ContentValues values = new ContentValues();
                JSONObject object = array.getJSONObject(i);

                for (String key : FIELD_MAP.keySet()) {
                    String value = object.getString(FIELD_MAP.get(key));
                    switch (key) {
                        case ItemsContract.Items.BODY:
                            value = value.replaceAll("(\r\n|\n)", "<br/>");
                            break;
                        default:
                            break;
                    }
                    values.put(key, value);
                }

                cpo.add(ContentProviderOperation.newInsert(dirUri).withValues(values).build());
            }

            getContentResolver().applyBatch(ItemsContract.CONTENT_AUTHORITY, cpo);

        } catch (JSONException | RemoteException | OperationApplicationException e) {
            Timber.e(e, "Error updating content.");
        } finally {
            postEvent(false);
        }

//        sendStickyBroadcast(
//                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, false));
    }


    private void postEvent(boolean isRefreshing) {
        EventBus.getDefault().post(new UpdateServiceEvent(isRefreshing));
    }

    public static class UpdateServiceEvent {
        private boolean mIsRefreshing;

        public UpdateServiceEvent(boolean mIsRefreshing) {
            this.mIsRefreshing = mIsRefreshing;
        }

        public boolean isIsRefreshing() {
            return mIsRefreshing;
        }
    }

}
