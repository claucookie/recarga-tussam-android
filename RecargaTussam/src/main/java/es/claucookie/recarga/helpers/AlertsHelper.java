package es.claucookie.recarga.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;

public class AlertsHelper {

    /**
     * Helper Alert
     *
     * @param title
     * @param message
     * @param icon
     */
    public static AlertDialog.Builder alert(final Context ctx, final CharSequence title, final CharSequence message, int icon) {

        if (ctx != null) {
            return new AlertDialog.Builder(ctx)
                    .setIcon(icon)
                    .setTitle(title)
                    .setMessage(message);

        }else {
            return null;
        }
    }
}
