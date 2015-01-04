package es.claucookie.recarga.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Base64;

import java.io.UnsupportedEncodingException;

import es.claucookie.recarga.R;
import es.claucookie.recarga.model.dto.TussamCardDTO;


/**
 * Created by claucookie on 04/03/14.
 */
public class GeneralHelper {

    public static String getDeviceId(Context context) {
        if (context != null) {
            return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        } else {
            return "";
        }
    }

    public static boolean isTelephonyEnabled(Activity activity) {
        TelephonyManager tm = (TelephonyManager) activity.getSystemService(Activity.TELEPHONY_SERVICE);
        return tm != null && tm.getSimState() == TelephonyManager.SIM_STATE_READY;
    }

    public static boolean hasPhoneAbility(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE)
            return false;

        return true;
    }

    public static void launchPhoneIntent(final Activity activity, final String telephone, String message, String accept, String cancel) {
        if (activity != null) {
            AlertDialog.Builder dialog = AlertsHelper.alert(activity, null, message, 0);
            if (dialog != null) {
                dialog.setPositiveButton(accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String phone = "tel:" + telephone;
                        Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                        if (hasPhoneAbility(activity)) {
                            //  phone
                            phoneIntent.setData(Uri.parse(phone));
                            if (activity != null) {
                                activity.startActivity(phoneIntent);
                            }
                        }
                    }
                });
                dialog.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        }
    }

    public static void launchEmailIntent(Activity activity, String[] emailRecipients, String subject, String content, int requestCode) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, emailRecipients);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, content);
        emailIntent.setType("message/rfc822");
        if (requestCode == 0) {
            activity.startActivity(emailIntent);
        } else {
            activity.startActivityForResult(emailIntent, requestCode);
        }
    }

    public static void launchExternalUrlWeb(final Activity activity, final String url, String message, String accept, String cancel) {
        if (activity != null) {
            AlertDialog.Builder dialog = AlertsHelper.alert(activity, null, message, 0);
            if (dialog != null) {
                dialog.setPositiveButton(accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (activity != null) {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            activity.startActivity(i);
                        }
                    }
                });
                dialog.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        }
    }

    /**
     * Other
     */

    public static String getEncStr(String base64) {
        String text = null;
        byte[] data = Base64.decode(base64, Base64.DEFAULT);
        try {
            text = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return text;
    }

    public static String getNumberOfTrips(Context context, TussamCardDTO card) {
        if (context != null) {
            Float price = Float.valueOf(context.getString(R.string.precio_normal));
            if (card.getCardType().toLowerCase().contains("estudiante")) {
                price = Float.valueOf(context.getString(R.string.precio_estudiante));
            } else if (card.getCardType().toLowerCase().contains("numerosa")) {
                price = Float.valueOf(context.getString(R.string.precio_familia_numerosa));
            } else if (card.getCardType().toLowerCase().contains("feria")) {
                price = Float.valueOf(context.getString(R.string.precio_feria));
            }
            Float numberOfTrips = Float.valueOf(card.getCardCredit().replace(" €", "")) / price;
            return String.format("%d", numberOfTrips.intValue());
        } else {
            return "";
        }

    }

}
