package es.claucookie.recarga.helpers;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import es.claucookie.recarga.R;
import es.claucookie.recarga.model.dao.TussamCardDAO;
import es.claucookie.recarga.model.dto.TussamCardDTO;

/**
 * Created by claucookie on 04/01/15.
 */
public class ParseHelper {

    public static final long ONE_MINUTE = 60 * 1000; // Millisecs
    public static final long ONE_HOUR = ONE_MINUTE * 60;
    public static final long ONE_DAY = ONE_HOUR * 24;

    public static TussamCardDTO parseData(String cardDataString) {
        TussamCardDTO favoriteCard = null;
        try {
            if (cardDataString != null && !cardDataString.isEmpty()) {
                favoriteCard = TussamCardDAO.getInstance().create(new JSONObject(cardDataString));
            }
        } catch (JSONException je) {
            Log.d("MainActivity", je.getMessage());
        }

        return favoriteCard;
    }

    public static String parseDate(Context context, Long date) {
        String dateString = "";
        if (context != null && date != null) {
            Date nowDate = new Date();
            long nowDateDifferenceLong = nowDate.getTime() - date;

            if (nowDateDifferenceLong < ONE_MINUTE) {
                // Less than 1 minute (now)
                dateString = context.getString(R.string.updated_now);

            } else if (nowDateDifferenceLong >= ONE_MINUTE && nowDateDifferenceLong < ONE_HOUR) {
                // More than 1 minute ( X minutes ago)
                dateString = String.format(Locale.US, "%d %s", nowDateDifferenceLong / ONE_MINUTE, context.getString(R.string.updated_minutes_ago)) ;

            } else if (nowDateDifferenceLong >= ONE_HOUR && nowDateDifferenceLong < ONE_DAY) {
                // More than 1 hour (X hours ago)
                dateString = String.format(Locale.US, "%d %s", nowDateDifferenceLong / ONE_HOUR, context.getString(R.string.updated_hours_ago)) ;

            } else {
                // More than 1 day (Date)
                dateString = new SimpleDateFormat(context.getString(R.string.updated_date_format)).format(new Date(date));

            }
        }
        return dateString;
    }
}
