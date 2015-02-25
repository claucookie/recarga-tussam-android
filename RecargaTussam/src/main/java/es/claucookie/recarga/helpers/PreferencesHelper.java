package es.claucookie.recarga.helpers;

import android.content.Context;
import android.util.Log;

import com.mobivery.android.helpers.ObfuscatedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import es.claucookie.recarga.model.dao.TussamCardsDAO;
import es.claucookie.recarga.model.dto.TussamCardDTO;
import es.claucookie.recarga.model.dto.TussamCardsDTO;

/**
 * Created by mvy17 on 17/02/14.
 */
public class PreferencesHelper {

    public static final String FAVORITES_PREFERENCES = "FAVORITES_PREFERENCES";
    private static final String SALT = "´gwek,v'b op'&%DFTRHFGFTv,tyjhjk`Ç'`¡u8495ubsrw";
    public static final String CARDS = "cards";
    public static final String PUBLI_INAPP = "publi_inapp";

    private ObfuscatedPreferences memoryPreferences;

    private ObfuscatedPreferences preferences(Context context) {
        if (memoryPreferences == null) {
            memoryPreferences = new ObfuscatedPreferences(context, FAVORITES_PREFERENCES, SALT, true);
        }
        return memoryPreferences;
    }

    private PreferencesHelper() {

    }

    private static class FavoritesLogicHolder {
        public static final PreferencesHelper instance = new PreferencesHelper();
    }

    public static PreferencesHelper getInstance() {
        return FavoritesLogicHolder.instance;
    }

    public void saveCards(Context context, TussamCardsDTO cards) {
        try {
            preferences(context).put(CARDS, TussamCardsDAO.getInstance().serialize(cards).toString());
        } catch (JSONException je) {

        }
    }

    public void saveCard(Context context, TussamCardDTO card) {
        TussamCardsDTO cards = getCards(context);
        for (TussamCardDTO currentCard : cards.getCards()) {
            if (currentCard.getCardNumber().equals(card.getCardNumber())) {
                currentCard.setCardCredit(card.getCardCredit());
                currentCard.setCardStatus(card.getCardStatus());
                currentCard.setCardName(card.getCardName());
                currentCard.setCardType(card.getCardType());
                currentCard.setIsCardFavorite(card.getIsCardFavorite());
                currentCard.setLastDate(card.getLastDate());
            }
        }
        try {
            preferences(context).put(CARDS, TussamCardsDAO.getInstance().serialize(cards).toString());
        } catch (JSONException je) {

        }
    }

    public TussamCardsDTO getCards(Context context) {
        TussamCardsDTO tussamCards = null;
        try {
            String cardsString = preferences(context).getString(CARDS);
            if (cardsString != null) {
                tussamCards = TussamCardsDAO.getInstance().create(new JSONObject(cardsString));
            }
        } catch (JSONException je) {
            Log.d("PreferencesHelper", je.getMessage());

        }

        return tussamCards;
    }

    public void deleteCards(Context context) {
        preferences(context).removeValue(CARDS);
    }

    public boolean inappPurchased(Context context) {
        return context != null && "true".equals(preferences(context).getString(PUBLI_INAPP));
    }

    public void setInappPurchased(Context context, boolean value) {

        if (context != null) {
            if (value) {
                preferences(context).put(PUBLI_INAPP, "true");
            } else {
                preferences(context).put(PUBLI_INAPP, "false");
            }
        }
    }


}
