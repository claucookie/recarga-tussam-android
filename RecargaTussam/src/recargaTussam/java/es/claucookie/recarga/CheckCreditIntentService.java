package es.claucookie.recarga;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.androidannotations.annotations.EService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;

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

    /**
     * Global request queue for Volley
     */
    private RequestQueue mRequestQueue;

    public CheckCreditIntentService() {
        super("CheckCreditIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
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
            Log.v(TAG, favoriteCardDTO.getCardCredit());
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
