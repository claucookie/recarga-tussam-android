package es.claucookie.recarga;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONException;

import java.util.concurrent.TimeUnit;

import es.claucookie.recarga.helpers.PreferencesHelper;
import es.claucookie.recarga.model.dao.TussamCardDAO;
import es.claucookie.recarga.model.dto.TussamCardDTO;
import es.claucookie.recarga.model.dto.TussamCardsDTO;

/**
 * Created by claucookie on 02/01/15.
 */
public class ListenerService extends WearableListenerService {


    String wearableId = "";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        wearableId = messageEvent.getSourceNodeId();

        if (messageEvent.getPath().equals(Consts.GET_FAVORITE_CARD_INFO_MESSAGE)) {
            final String message = new String(messageEvent.getData());
            if (BuildConfig.DEBUG) {
                Log.v("myTag", "Message path received on hanheld is: " + messageEvent.getPath());
                Log.v("myTag", "Message received on handheld is: " + message);
            }
            getFavoriteCard();
        } else {
            super.onMessageReceived(messageEvent);
        }
    }

    private void getFavoriteCard() {
        TussamCardsDTO aucorsaCardsDTO = PreferencesHelper.getInstance().getCards(this);
        TussamCardDTO favoriteCardDTO = null;
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
}
