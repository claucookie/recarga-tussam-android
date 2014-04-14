package es.claucookie.recarga;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.mobivery.android.widgets.ExLabel;
import com.mobivery.android.widgets.ExText;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import es.claucookie.recarga.helpers.AlertsHelper;
import es.claucookie.recarga.helpers.GeneralHelper;
import es.claucookie.recarga.helpers.PreferencesHelper;
import es.claucookie.recarga.model.dto.TussamCardDTO;
import es.claucookie.recarga.model.dto.TussamCardsDTO;

@OptionsMenu(R.menu.menu_add_edit)
@EActivity(R.layout.activity_main2)
public class MainActivity extends Activity {
    public static final String STATUS_URL = "http://recargas.tussam.es/TPW/Common/cardStatus.do?swNumber=";
    public static final String CREDIT_URL = "https://recargas.tussam.es/TPW/Common/viewProductSelection.do?idNewCard=";
    public static final String VALIDATE_URL = "https://recargas.tussam.es/TPW/Common/validateHWSNumberAjax.do?idNewCard=";

    /**
     * Log or request TAG
     */
    public static final String TAG = "VolleyPatterns";

    @ViewById
    Spinner cardsSpinner;
    @ViewById
    ExLabel cardTypeText;
    @ViewById
    ExLabel cardNameText;
    @ViewById
    ExLabel cardNumberText;
    @ViewById
    ExLabel cardStatusText;
    @ViewById
    ExLabel cardCreditText;
    @ViewById
    RelativeLayout cardsData;
    @ViewById
    ExText cardEditNameText;
    @ViewById
    ExText cardEditNumberText;
    @ViewById
    RelativeLayout cardsEditData;
    @ViewById
    ImageView editCardImage;
    @ViewById
    ImageView refreshCardImage;
    @ViewById
    ImageView rechargeCardImage;
    @ViewById
    LinearLayout cardActions;
    @ViewById
    ImageView discardCardImage;
    @ViewById
    ImageView saveCardImage;
    @ViewById
    LinearLayout cardEditActions;
    @ViewById
    ImageView doneCardImage;
    @ViewById
    ImageView removeCardImage;
    @ViewById
    LinearLayout cardNewActions;
    @ViewById
    LinearLayout tussamInfo;
    @ViewById
    LinearLayout progressView;
    @ViewById
    LinearLayout newCardHelpView;
    @ViewById
    CheckBox favoriteCardCb;

    @InstanceState
    TussamCardsDTO tussamCardsDTO = new TussamCardsDTO();

    @InstanceState
    TussamCardDTO selectedCardDTO = new TussamCardDTO();

    @InstanceState
    boolean isDetailView = false, isAddView = false, isEditView = false;

    private ArrayAdapter<String> spinnerAdapter;

    /**
     * Global request queue for Volley
     */
    private RequestQueue mRequestQueue;

    @AfterViews
    void initViews() {
        initSpinner();
        loadSavedCards();
        loadFirstView();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

    }

    @Override
    public void onBackPressed() {
        if (isAddView || isEditView) {
            if (tussamCardsDTO != null &&
                    tussamCardsDTO.getCards() != null &&
                    tussamCardsDTO.getCards().size() > 0) {
                showDetailView();
            } else {
                showAddView();
            }
        } else {
            finish();
        }
    }

    private void loadFirstView() {
        if (tussamCardsDTO != null
                && tussamCardsDTO.getCards() != null
                && tussamCardsDTO.getCards().size() > 0) {
            showDetailView();
        } else {
            showAddView();
        }
    }

    private void initSpinner() {
        if (spinnerAdapter == null) {
            spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        }
        // Specify the layout to use when the list of choices appears
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        cardsSpinner.setAdapter(spinnerAdapter);
        cardsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (tussamCardsDTO != null && tussamCardsDTO.getCards() != null
                        && tussamCardsDTO.getCards().size() > 0) {
                    selectedCardDTO = tussamCardsDTO.getCards().get(position);
                    requestCardInfo();

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void loadSavedCards() {
        // Get saved cards from preferences
        spinnerAdapter.clear();
        tussamCardsDTO = PreferencesHelper.getInstance().getCards(this);
        if (tussamCardsDTO != null && tussamCardsDTO.getCards() != null) {
            for (TussamCardDTO card : tussamCardsDTO.getCards()) {
                spinnerAdapter.add(card.getCardName());
                if (selectedCardDTO != null
                        && selectedCardDTO.isEmpty()
                        && card.getIsCardFavorite() != null
                        && card.getIsCardFavorite()) {
                    selectedCardDTO = card;
                }
            }
        }
        spinnerAdapter.notifyDataSetChanged();
        if (selectedCardDTO != null && tussamCardsDTO != null && tussamCardsDTO.getCards() != null) {
            int newCardIndex = tussamCardsDTO.getCards().indexOf(selectedCardDTO);
            cardsSpinner.setSelection(newCardIndex);
        }
    }


    @Click(R.id.discard_card_image)
    void deleteCardClicked() {

        if (selectedCardDTO != null) {
            PreferencesHelper.getInstance().deleteCards(this);
            tussamCardsDTO.getCards().remove(selectedCardDTO);
            selectedCardDTO = null;
            PreferencesHelper.getInstance().saveCards(this, tussamCardsDTO);
            loadSavedCards();
            loadFirstView();
        }
    }

    @Click(R.id.edit_card_image)
    void editCardClicked() {

        showEditView();
    }

    @OptionsItem(R.id.add_card)
    void addCardClicked() {

        showAddView();
    }

    @Click(R.id.done_card_image)
    void createCardClicked() {

        String newCardNumber = cardEditNumberText.getText() != null ? cardEditNumberText.getText().toString().replace(" ", "") : "";
        String newCardName = cardEditNameText.getText() != null ? cardEditNameText.getText().toString() : "";
        newCardNumber = newCardNumber.replaceFirst("^0+(?!$)", "");
        if (!newCardNumber.equals("")) {
            TussamCardDTO newCard = new TussamCardDTO();
            newCard.setCardName(newCardName);
            newCard.setCardNumber(newCardNumber);
            saveNewCard(newCard);
            selectedCardDTO = newCard;
            loadSavedCards();
            showDetailView();
            reloadData();
        }
    }


    @Click(R.id.save_card_image)
    void saveCardClicked() {
        if (cardEditNumberText != null && cardEditNameText != null) {
            String newCardNumber = cardEditNumberText.getText() != null ? cardEditNumberText.getText().toString().replace(" ", "") : "";
            String newCardName = cardEditNameText.getText() != null ? cardEditNameText.getText().toString() : "";
            newCardNumber = newCardNumber.replaceFirst("^0+(?!$)", "");
            if (selectedCardDTO != null && !newCardNumber.equals("")) {
                selectedCardDTO.setCardName(newCardName);
                selectedCardDTO.setCardNumber(newCardNumber);
                tussamCardsDTO.getCards().set(cardsSpinner.getSelectedItemPosition(), selectedCardDTO);
                PreferencesHelper.getInstance().saveCards(this, tussamCardsDTO);
                loadSavedCards();
                showDetailView();
                reloadData();
                requestCardInfo();
            }
        }
    }

    @Click(R.id.remove_card_image)
    void cancelClicked() {
        if (tussamCardsDTO != null &&
                tussamCardsDTO.getCards() != null &&
                tussamCardsDTO.getCards().size() > 0) {
            showDetailView();
        }
    }

    @Click(R.id.refresh_card_image)
    void refreshClicked() {
        if (selectedCardDTO != null) {
            requestCardInfo();
        }
    }

    @Click(R.id.recharge_card_image)
    void rechardCardClicked() {
        if (selectedCardDTO != null) {
            String externalUrl = CREDIT_URL+selectedCardDTO.getCardNumber();
            GeneralHelper.launchExternalUrlWeb(this, externalUrl, getString(R.string.recharge_card_text), getString(R.string.alert_yes), getString(R.string.alert_no));
        }
    }

    @Click
    void favoriteCardCbClicked() {
        toggleFavoriteCardIndicator(favoriteCardCb.isChecked());
    }

    @Background
    void toggleFavoriteCardIndicator(boolean isChecked) {
        if (selectedCardDTO != null && tussamCardsDTO != null && tussamCardsDTO.getCards() != null) {
            selectedCardDTO.setIsCardFavorite(isChecked);
            int cardsSize = tussamCardsDTO.getCards().size();
            for( int i=0; i<cardsSize; i++) {
                tussamCardsDTO.getCards().get(i).setIsCardFavorite(false);
                if (isChecked && selectedCardDTO == tussamCardsDTO.getCards().get(i)) {
                    tussamCardsDTO.getCards().get(i).setIsCardFavorite(true);
                }
            }
            PreferencesHelper.getInstance().saveCards(this, tussamCardsDTO);
        }
    }


    private void requestCardInfo() {
        if (selectedCardDTO != null) {
            hideData();
            preloadData();
            if (selectedCardDTO.getCardNumber() != null) {
                progressView.setVisibility(View.VISIBLE);
                String cardNumber = trim(selectedCardDTO.getCardNumber(), 0, selectedCardDTO.getCardNumber().length());
                StringRequest req = new StringRequest(STATUS_URL + cardNumber, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseHtml(response);
                        reloadData();
                        showDetailView();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        reloadData();
                        if (error.networkResponse != null &&
                                error.networkResponse.statusCode == 500 &&
                                selectedCardDTO != null &&
                                selectedCardDTO.getCardCredit() == null) {

                            cardTypeText.setText(getString(R.string.wrong_card_number_error));
                        }
                        Toast.makeText(MainActivity.this, getString(R.string.parse_error), Toast.LENGTH_LONG).show();
                        showDetailView();
                    }
                });

                // add the request object to the queue to be executed
                addToRequestQueue(req);
            }
        }
    }

    private void hideData() {
        cardsSpinner.setVisibility(View.VISIBLE);
        cardsData.setVisibility(View.INVISIBLE);
        cardsEditData.setVisibility(View.INVISIBLE);
        // remove soft keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(cardNumberText.getWindowToken(), 0);
    }

    private void saveNewCard(TussamCardDTO card) {

        selectedCardDTO = card;
        if (tussamCardsDTO != null && tussamCardsDTO.getCards() != null) {
            tussamCardsDTO.getCards().add(card);
        } else {
            tussamCardsDTO = new TussamCardsDTO();
            tussamCardsDTO.setCards(new ArrayList<TussamCardDTO>());
            tussamCardsDTO.getCards().add(card);
        }
        PreferencesHelper.getInstance().saveCards(this, tussamCardsDTO);
    }

    public String trim(CharSequence s, int start, int end) {
        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }

        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }

        return s.subSequence(start, end).toString();
    }

    private void parseHtml(String response) {

        boolean errorFound = false;
        if (selectedCardDTO == null) {
            selectedCardDTO = new TussamCardDTO();
        }
        Document responseDoc = Jsoup.parse(response);
        Element mainDiv = responseDoc.getElementById("cardStatus");
        if (mainDiv != null) {
            Elements cardInfo = mainDiv.select("span");
            // CardStatus
            if (cardInfo.size() > 1 && cardInfo.get(1) != null) {
                selectedCardDTO.setCardStatus(cardInfo.get(1).text());
            } else errorFound = true;

            // CardType
            if (cardInfo.size() > 2 && cardInfo.get(2) != null) {
                selectedCardDTO.setCardType(cardInfo.get(2).text());
            } else errorFound = true;

            // CardCredit
            if (cardInfo.size() > 3 && cardInfo.get(3) != null) {
                selectedCardDTO.setCardCredit(cardInfo.get(3).text());
            } else errorFound = true;

        } else {
            errorFound = true;
        }

        if (errorFound) {
            Toast.makeText(this, getString(R.string.parse_error), Toast.LENGTH_LONG).show();
        }

    }

    private void preloadData() {
        // Load data stored in preferences like favorite card flag
        if (selectedCardDTO != null) {
            favoriteCardCb.setChecked(selectedCardDTO.getIsCardFavorite() != null ? selectedCardDTO.getIsCardFavorite() : false);
        }
    }

    private void reloadData() {
        if (selectedCardDTO != null) {
            cardNameText.setText(selectedCardDTO.getCardName() != null ? selectedCardDTO.getCardName() : "");
            cardEditNameText.setText(selectedCardDTO.getCardName() != null ? selectedCardDTO.getCardName() : "");
            cardNumberText.setText(selectedCardDTO.getCardNumber() != null ? selectedCardDTO.getCardNumber() : "");
            cardEditNumberText.setText(selectedCardDTO.getCardNumber() != null ? selectedCardDTO.getCardNumber() : "");
            cardStatusText.setText(selectedCardDTO.getCardStatus() != null ? selectedCardDTO.getCardStatus() : "");
            cardTypeText.setText(selectedCardDTO.getCardType() != null ? selectedCardDTO.getCardType() : "");
            cardCreditText.setText(selectedCardDTO.getCardCredit() != null ? selectedCardDTO.getCardCredit() : "");
        }
    }

    private void clearData() {
        cardNameText.setText("");
        cardNumberText.setText("");
        cardStatusText.setText("");
        cardTypeText.setText("");
        cardCreditText.setText("");
    }

    private void showDetailView() {
        favoriteCardCb.setVisibility(View.VISIBLE);
        newCardHelpView.setVisibility(View.GONE);
        progressView.setVisibility(View.GONE);
        isDetailView = true;
        isEditView = false;
        isAddView = false;
        cardsSpinner.setVisibility(View.VISIBLE);
        cardsData.setVisibility(View.VISIBLE);
        cardsEditData.setVisibility(View.GONE);
        cardActions.setVisibility(View.VISIBLE);
        cardEditActions.setVisibility(View.GONE);
        cardNewActions.setVisibility(View.GONE);
        // remove soft keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(cardNumberText.getWindowToken(), 0);
    }

    private void showAddView() {
        favoriteCardCb.setVisibility(View.GONE);
        newCardHelpView.setVisibility(View.VISIBLE);
        cancelPendingRequests(TAG);
        progressView.setVisibility(View.GONE);
        isDetailView = false;
        isEditView = false;
        isAddView = true;
        cardEditNumberText.setText("");
        cardEditNameText.setText("");
        cardsSpinner.setVisibility(View.GONE);
        cardsData.setVisibility(View.GONE);
        cardsEditData.setVisibility(View.VISIBLE);
        cardActions.setVisibility(View.GONE);
        cardEditActions.setVisibility(View.GONE);
        cardNewActions.setVisibility(View.VISIBLE);
    }

    private void showEditView() {
        favoriteCardCb.setVisibility(View.VISIBLE);
        newCardHelpView.setVisibility(View.GONE);
        cancelPendingRequests(TAG);
        isDetailView = false;
        isEditView = true;
        isAddView = false;
        if (selectedCardDTO != null) {
            cardEditNumberText.setText(selectedCardDTO.getCardNumber());
            cardEditNameText.setText(selectedCardDTO.getCardName());
        }
        cardsSpinner.setVisibility(View.VISIBLE);
        cardsData.setVisibility(View.GONE);
        cardsEditData.setVisibility(View.VISIBLE);
        cardActions.setVisibility(View.GONE);
        cardEditActions.setVisibility(View.VISIBLE);
        cardNewActions.setVisibility(View.GONE);
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
     * Adds the specified request to the global queue, if tag is specified
     * then it is used else Default TAG is used.
     *
     * @param req
     * @param tag
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        getRequestQueue().add(req);
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

    /**
     * Cancels all pending requests by the specified TAG, it is important
     * to specify a TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param tag
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
