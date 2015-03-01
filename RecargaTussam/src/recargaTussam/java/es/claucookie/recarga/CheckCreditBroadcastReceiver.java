package es.claucookie.recarga;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by claucookie on 01/03/15.
 */
public class CheckCreditBroadcastReceiver extends BroadcastReceiver {
    
    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "es.claucookie.recarga.alarm";

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, CheckCreditIntentService.class);
        i.putExtra("foo", "bar");
        context.startService(i);
    }
}
