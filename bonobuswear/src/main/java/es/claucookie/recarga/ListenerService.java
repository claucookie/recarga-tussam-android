package es.claucookie.recarga;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by claucookie on 02/01/15.
 */
public class ListenerService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals(Consts.GET_FAVORITE_CARD_INFO_MESSAGE)) {
            if (BuildConfig.DEBUG) {
                Log.v("ListenerService", "Message path received on watch is: " + messageEvent.getPath());
                Log.v("ListenerService", "Message received on watch is: " + new String(messageEvent.getData()));
                Log.v("ListenerService", new String(messageEvent.getData()));
            }

            // Broadcast message to wearable activity for display
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra(Consts.CARD_DATA, new String(messageEvent.getData()));
            messageIntent.putExtra(Consts.CARD_DATA_REQUEST_FINISHED, false);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);

        } else if (messageEvent.getPath().equals(Consts.GET_FAVORITE_CARD_INFO_UPDATED_MESSAGE)) {
            if (BuildConfig.DEBUG) {
                Log.v("ListenerService", "Message path received on watch is: " + messageEvent.getPath());
                Log.v("ListenerService", "Message received on watch is: " + new String(messageEvent.getData()));
                Log.v("ListenerService", new String(messageEvent.getData()));
            }

            // Broadcast message to wearable activity for display
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra(Consts.CARD_DATA, new String(messageEvent.getData()));
            messageIntent.putExtra(Consts.CARD_DATA_REQUEST_FINISHED, true);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);

        } else if (messageEvent.getPath().equals(Consts.GET_FAVORITE_CARD_INFO_ERROR)) {
            if (BuildConfig.DEBUG) {
                Log.v("myTag", "Message path received on watch is: " + messageEvent.getPath());
                Log.v("myTag", "Message received on watch is: " + new String(messageEvent.getData()));
            }
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }
}
