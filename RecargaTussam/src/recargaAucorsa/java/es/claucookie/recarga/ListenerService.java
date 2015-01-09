package es.claucookie.recarga;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.mobivery.android.helpers.TagFormat;

import org.androidannotations.annotations.sharedpreferences.Pref;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import es.claucookie.recarga.helpers.GeneralHelper;
import es.claucookie.recarga.helpers.PreferencesHelper;
import es.claucookie.recarga.model.dao.TussamCardDAO;
import es.claucookie.recarga.model.dto.TussamCardDTO;
import es.claucookie.recarga.model.dto.TussamCardsDTO;

/**
 * Created by claucookie on 02/01/15.
 */
public class ListenerService extends WearableListenerService {


    String wearableId = "";
    TussamCardDTO favoriteCardDTO;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        wearableId = messageEvent.getSourceNodeId();

        if (messageEvent.getPath().equals(Consts.GET_FAVORITE_CARD_INFO_MESSAGE)) {
            final String message = new String(messageEvent.getData());
            if (BuildConfig.DEBUG) {
                Log.v("ListenerService", "Message path received on hanheld is: " + messageEvent.getPath());
                Log.v("ListenerService", "Message received on handheld is: " + message);
            }
            getFavoriteCard();
            requestUpdatedCardInfo();
        } else {
            super.onMessageReceived(messageEvent);
        }
    }

    private void requestUpdatedCardInfo() {

        if (favoriteCardDTO != null) {
            try {
                Document document = Jsoup.connect(String.format(Locale.US, NetworkConsts.STATUS_URL, favoriteCardDTO.getCardNumber()))
                        .get();

                Element mainDiv = document.getElementById("global");
                if (mainDiv != null) {
                    if (!mainDiv.select("span.spanSaldo").isEmpty()) {
                        String credit = mainDiv.select("span.spanSaldo").text();
                        favoriteCardDTO.setCardCredit(credit);
                        favoriteCardDTO.setLastDate((new Date()).getTime());
                        favoriteCardDTO.setCardType(mainDiv.select("p#titleName").text());
                        String tripsLeft = TagFormat.from(getString(R.string.trips_left))
                                .with("trip", GeneralHelper.getNumberOfTrips(getApplicationContext(), favoriteCardDTO))
                                .format();
                        favoriteCardDTO.setCardStatus(tripsLeft);
                    } else {
                        favoriteCardDTO.setCardType(mainDiv.select("span#spanMsg").text());
                        favoriteCardDTO.setCardCredit("");
                        favoriteCardDTO.setLastDate((new Date()).getTime());
                        favoriteCardDTO.setCardStatus(getString(R.string.register_text));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            PreferencesHelper.getInstance().saveCard(getApplicationContext(), favoriteCardDTO);
            sendCardUpdated(favoriteCardDTO);
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
        if (favoriteCardDTO != null) {
            sendCard(favoriteCardDTO);
        } else if (aucorsaCardsDTO != null &&
                aucorsaCardsDTO.getCards() != null &&
                aucorsaCardsDTO.getCards().size() > 0){
            // If user didnt set a card as favourite, set the first one and load info.
            favoriteCardDTO = aucorsaCardsDTO.getCards().get(0);
            favoriteCardDTO.setIsCardFavorite(true);
            sendCard(favoriteCardDTO);
        } else {
            sendError("Card not found");
        }
    }

    private void sendError(String errorMessage) {
        GoogleApiClient client = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Wearable.API)
                .build();
        client.blockingConnect(100, TimeUnit.MILLISECONDS);
        Wearable.MessageApi.sendMessage(client, wearableId, Consts.GET_FAVORITE_CARD_INFO_ERROR, errorMessage.getBytes());
        client.disconnect();

    }

    private void sendCard(TussamCardDTO favoriteCard) {
        GoogleApiClient client = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Wearable.API)
                .build();
        client.blockingConnect(100, TimeUnit.MILLISECONDS);
        try {
            Wearable.MessageApi.sendMessage(client, wearableId, Consts.GET_FAVORITE_CARD_INFO_MESSAGE, TussamCardDAO.getInstance().serialize(favoriteCard).toString().getBytes());
        } catch (JSONException e) {
            e.printStackTrace();
            sendError("Error parsing card");
        }
        client.disconnect();
    }

    private void sendCardUpdated(TussamCardDTO favoriteCard) {
        GoogleApiClient client = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Wearable.API)
                .build();
        client.blockingConnect(100, TimeUnit.MILLISECONDS);
        try {
            Wearable.MessageApi.sendMessage(client, wearableId, Consts.GET_FAVORITE_CARD_INFO_UPDATED_MESSAGE, TussamCardDAO.getInstance().serialize(favoriteCard).toString().getBytes());
        } catch (JSONException e) {
            e.printStackTrace();
            sendError("Error parsing card");
        }
        client.disconnect();
    }
}
