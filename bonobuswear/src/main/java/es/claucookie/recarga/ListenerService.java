package es.claucookie.recarga;

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
                Log.v("myTag", "Message path received on watch is: " + messageEvent.getPath());
                Log.v("myTag", "Message received on watch is: " + new String(messageEvent.getData()));
            }
            Log.v("myTag", new String(messageEvent.getData()));
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
