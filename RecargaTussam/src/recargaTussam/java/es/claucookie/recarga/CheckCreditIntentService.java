package es.claucookie.recarga;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;
import java.util.Locale;

import es.claucookie.recarga.helpers.GeneralHelper;
import es.claucookie.recarga.helpers.PreferencesHelper;
import es.claucookie.recarga.model.dto.TussamCardDTO;
import es.claucookie.recarga.model.dto.TussamCardsDTO;

/**
 * Created by claucookie on 01/03/15.
 */
public class CheckCreditIntentService extends IntentService {

    public static final String TAG = CheckCreditIntentService.class.getName();

    TussamCardDTO favoriteCardDTO;
    float minCredit;

    /**
     * Global request queue for Volley
     */
    private RequestQueue mRequestQueue;

    public CheckCreditIntentService() {
        super("CheckCreditIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.hasExtra(Consts.ALARM_CREDIT_EXTRA)) {
            minCredit = intent.getIntExtra(Consts.ALARM_CREDIT_EXTRA, 0);
        }
        getFavoriteCard();
        requestUpdatedCardInfo();
    }

    private void getFavoriteCard() {
        TussamCardsDTO aucorsaCardsDTO = PreferencesHelper.getInstance().getCards(this);
        if (aucorsaCardsDTO != null && aucorsaCardsDTO.getCards() != null) {
            for (TussamCardDTO card : aucorsaCardsDTO.getCards()) {
                if (card.getIsCardFavorite() != null
                        && card.getIsCardFavorite()) {
                    favoriteCardDTO = card;
                }
            }
        }
        if (aucorsaCardsDTO != null
                && aucorsaCardsDTO.getCards() != null
                && aucorsaCardsDTO.getCards().size() > 0
                && favoriteCardDTO == null) {
            // If user didnt set a card as favourite, set the first one and load info.
            favoriteCardDTO = aucorsaCardsDTO.getCards().get(0);
            favoriteCardDTO.setIsCardFavorite(true);
        }
    }

    private void requestUpdatedCardInfo() {
        if (favoriteCardDTO != null) {

            if (favoriteCardDTO.getCardNumber() != null) {
                String cardNumber = GeneralHelper.trim(favoriteCardDTO.getCardNumber(), 0, favoriteCardDTO.getCardNumber().length());
                StringRequest req = new StringRequest(NetworkConsts.STATUS_URL + cardNumber, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseHtml(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // In case of error, return cached card
                        checkCardInfo();
                    }
                });

                // add the request object to the queue to be executed
                addToRequestQueue(req);
            }
        }
    }

    private void showNotification() {
        int notificationId = 001;
        // Build intent for notification content
        Intent viewIntent = new Intent(this, CheckCreditIntentService.class);
        //viewIntent.putExtra(EXTRA_EVENT_ID, eventId);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(this, 0, viewIntent, 0);

        // Create a WearableExtender to add functionality for wearables
        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender()
                        .setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.notif_bg));

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notif)
                        .setColor(getResources().getColor(R.color.color_primary))
                        .setContentTitle(getString(R.string.notification_credit_title))
                        .setContentText(String.format(Locale.US, getString(R.string.notification_credit_content), favoriteCardDTO.getCardCredit()))
                        .setContentIntent(viewPendingIntent)
                        .extend(wearableExtender)
                        .setDefaults(Notification.DEFAULT_ALL);

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void parseHtml(String response) {

        if (favoriteCardDTO != null) {
            Document responseDoc = Jsoup.parse(response);
            Element mainDiv = responseDoc.getElementById("cardStatus");
            if (mainDiv != null) {
                Elements cardInfo = mainDiv.select("span");
                // CardStatus
                if (cardInfo.size() > 1 && cardInfo.get(1) != null) {
                    String cardStatus = cardInfo.get(1).text().replaceFirst("^ *", "");
                    favoriteCardDTO.setCardStatus(cardStatus);
                }

                // CardType
                if (cardInfo.size() > 2 && cardInfo.get(2) != null) {
                    String cardType = cardInfo.get(2).text().replaceFirst("^ *", "");
                    favoriteCardDTO.setCardType(cardType);
                }

                // CardCredit
                if (cardInfo.size() > 3 && cardInfo.get(3) != null) {
                    favoriteCardDTO.setCardCredit(cardInfo.get(3).text());
                }

                // Last update date
                favoriteCardDTO.setLastDate((new Date()).getTime());

                // Save card
                PreferencesHelper.getInstance().saveCard(getApplicationContext(), favoriteCardDTO);
                checkCardInfo();
            }
        }

    }

    private void checkCardInfo() {
        if (favoriteCardDTO != null) {
            float cardCredit = Float.valueOf(favoriteCardDTO.getCardCredit().replace("€", ""));
            if (cardCredit <= minCredit) {
                if (BuildConfig.DEBUG) {
                    Log.v(TAG, "Credit under minimum !!!!");
                }
                showNotification();
            }

            if (BuildConfig.DEBUG) {
                Log.v(TAG, favoriteCardDTO.getCardCredit());
            }
        }
         
        
    }

    /**
     * @return The Volley Request queue, the queue will be created if it is null
     */
    public RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    /**
     * Adds the specified request to the global queue using the Default TAG.
     *
     * @param req
     */
    public <T> void addToRequestQueue(Request<T> req) {
        // set the default tag if tag is empty
        req.setTag(TAG);

        getRequestQueue().add(req);
    }
}
