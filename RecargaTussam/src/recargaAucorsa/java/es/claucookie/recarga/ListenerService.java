package es.claucookie.recarga;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.concurrent.TimeUnit;

/**
 * Created by claucookie on 02/01/15.
 */
public class ListenerService extends WearableListenerService {


    String senderId = "";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        senderId = messageEvent.getSourceNodeId();
        if (messageEvent.getPath().equals("/message_path")) {
            final String message = new String(messageEvent.getData());
            Log.v("myTag", "Message path received on hanheld is: " + messageEvent.getPath());
            Log.v("myTag", "Message received on handheld is: " + message);
            reply("/received_data");
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }

    private void reply(String message) {
        GoogleApiClient client = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Wearable.API)
                .build();
        client.blockingConnect(100, TimeUnit.MILLISECONDS);
        Wearable.MessageApi.sendMessage(client, senderId, message, "test".getBytes());
        client.disconnect();
    }
}
