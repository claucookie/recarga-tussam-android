package es.claucookie.recarga;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EService;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import es.claucookie.recarga.helpers.GeneralHelper;
import es.claucookie.recarga.helpers.PreferencesHelper;
import es.claucookie.recarga.model.dao.TussamCardDAO;
import es.claucookie.recarga.model.dto.TussamCardDTO;
import es.claucookie.recarga.model.dto.TussamCardsDTO;

/**
 * Created by claucookie on 02/01/15.
 */
@EService
public class ListenerService extends WearableListenerService {


    /**
     * Log or request TAG
     */
    public static final String TAG = "HandheldListenerService";

    String wearableId = "";
    TussamCardDTO favoriteCardDTO;
    GoogleApiClient client;

    /**
     * Global request queue for Volley
     */
    private RequestQueue mRequestQueue;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        wearableId = messageEvent.getSourceNodeId();

        if (messageEvent.getPath().equals(Consts.GET_FAVORITE_CARD_INFO_MESSAGE)) {
            final String message = new String(messageEvent.getData());
            if (BuildConfig.DEBUG) {
                Log.v(TAG, "Message path received on hanheld is: " + messageEvent.getPath());
                Log.v(TAG, "Message received on handheld is: " + message);
            }
            getFavoriteCard();
            requestUpdatedCardInfo();
        } else {
            super.onMessageReceived(messageEvent);
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
                        sendCachedCard();
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
                sendCardUpdated(favoriteCardDTO);
            }
        }

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
        } else {
            sendCachedCard();
        }
            
    }

    private void sendCachedCard() {
        if (favoriteCardDTO != null) {
            sendCard(favoriteCardDTO);
        } else {
            sendNoCardsMessage();
        }
    }

    /**
     *
     * Google api client methods
     *
     */

    private void initGoogleApiClient() {
        if (client == null) {
            client = new GoogleApiClient.Builder(getApplicationContext())
                    .addApi(Wearable.API)
                    .build();
        }
    }

    private boolean connectGoogleApiClient() {
        ConnectionResult connectionResult =
                client.blockingConnect(30, TimeUnit.SECONDS);

        if (!connectionResult.isSuccess()) {
            Log.e(TAG, "Failed to connect to GoogleApiClient.");
            return false;
        }
        return true;
    }

    @Background
    void sendError(String errorMessage) {
        initGoogleApiClient();
        if (connectGoogleApiClient()) {
            Wearable.MessageApi.sendMessage(client, wearableId, Consts.GET_FAVORITE_CARD_INFO_ERROR, errorMessage.getBytes());
        }
    }

    @Background
    void sendCard(TussamCardDTO favoriteCard) {
        initGoogleApiClient();
        if (connectGoogleApiClient()) {
            try {
                Wearable.MessageApi.sendMessage(client, wearableId, Consts.GET_FAVORITE_CARD_INFO_MESSAGE, TussamCardDAO.getInstance().serialize(favoriteCard).toString().getBytes());
            } catch (JSONException e) {
                e.printStackTrace();
                sendError("Error parsing card");
            }
        }
    }

    @Background
    void sendNoCardsMessage() {
        TussamCardDTO errorCard = new TussamCardDTO();
        errorCard.setCardName(getString(R.string.no_card));
        errorCard.setCardStatus(getString(R.string.no_card_detail));
        errorCard.setIsCardFavorite(true);
        errorCard.setCardType("");
        errorCard.setCardNumber("");
        errorCard.setLastDate((new Date()).getTime());

        initGoogleApiClient();
        if (connectGoogleApiClient()) {
            try {
                Wearable.MessageApi.sendMessage(client, wearableId, Consts.GET_FAVORITE_CARD_INFO_UPDATED_MESSAGE, TussamCardDAO.getInstance().serialize(errorCard).toString().getBytes());
            } catch (JSONException e) {
                e.printStackTrace();
                sendError("Error parsing card");
            }
        }

    }

    @Background
    void sendCardUpdated(TussamCardDTO favoriteCard) {
        initGoogleApiClient();
        if (connectGoogleApiClient()) {
            try {
                Wearable.MessageApi.sendMessage(client, wearableId, Consts.GET_FAVORITE_CARD_INFO_UPDATED_MESSAGE, TussamCardDAO.getInstance().serialize(favoriteCard).toString().getBytes());
            } catch (JSONException e) {
                e.printStackTrace();
                sendError("Error parsing card");
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
