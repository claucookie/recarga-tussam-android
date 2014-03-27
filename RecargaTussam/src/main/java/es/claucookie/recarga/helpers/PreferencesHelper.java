package es.claucookie.recarga.helpers;

import android.content.Context;
import android.util.Log;

import com.mobivery.android.helpers.ObfuscatedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import es.claucookie.recarga.model.dao.TussamCardsDAO;
import es.claucookie.recarga.model.dto.TussamCardsDTO;

/**
 * Created by mvy17 on 17/02/14.
 */
public class PreferencesHelper {

    public static final String FAVORITES_PREFERENCES = "FAVORITES_PREFERENCES";
    private static final String SALT = "´gwek,v'b op'&%DFTRHFGFTv,tyjhjk`Ç'`¡u8495ubsrw";
    public static final String CARDS = "cards";

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


}
