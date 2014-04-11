package es.claucookie.recarga.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;


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

    public static boolean hasPhoneAbility(Context context)
    {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if(telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE)
            return false;

        return true;
    }

    public static void launchPhoneIntent(final Activity activity, final String telephone , String message, String accept, String cancel) {
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

}
