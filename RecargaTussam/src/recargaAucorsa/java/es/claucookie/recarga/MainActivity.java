package es.claucookie.recarga;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.mobivery.android.helpers.TagFormat;
import com.mobivery.android.widgets.ExLabel;
import com.mobivery.android.widgets.ExText;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import es.claucookie.recarga.helpers.GeneralHelper;
import es.claucookie.recarga.helpers.PreferencesHelper;
import es.claucookie.recarga.model.dto.TussamCardDTO;
import es.claucookie.recarga.model.dto.TussamCardsDTO;

@EActivity(R.layout.activity_main2)
public class MainActivity extends ActionBarActivity {
    public static final String STATUS_URL = "http://recargas.aucorsa.es/checkcard.php?nCard=";
    public static final String CREDIT_URL = "http://recargas.aucorsa.es/checkcard.php?nCard=";
    public static final String VALIDATE_URL = "https://recargas.tussam.es/TPW/Common/validateHWSNumberAjax.do?idNewCard=";
    public static final long ONE_MINUTE = 60 * 1000; // Millisecs
    public static final long ONE_HOUR = ONE_MINUTE * 60;
    public static final long ONE_DAY = ONE_HOUR * 24;

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
    CheckBox favoriteCardCb;
    @ViewById
    ExLabel cardLastUpdateText;
    @ViewById
    Button addCardButton;
    @ViewById
    ImageView minicard;
    @ViewById
    LinearLayout timeView;

    @InstanceState
    TussamCardsDTO aucorsaCardsDTO = new TussamCardsDTO();

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
        hideData();
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
            if (aucorsaCardsDTO != null &&
                    aucorsaCardsDTO.getCards() != null &&
                    aucorsaCardsDTO.getCards().size() > 0) {
                showDetailView();
            } else {
                if (aucorsaCardsDTO != null && aucorsaCardsDTO.getCards() != null && aucorsaCardsDTO.getCards().size() > 0) {
                    showAddView();
                } else {
                    finish();
                }
            }
        } else {
            finish();
        }
    }

    private void loadFirstView() {
        if (aucorsaCardsDTO != null
                && aucorsaCardsDTO.getCards() != null
                && aucorsaCardsDTO.getCards().size() > 0) {
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
                if (aucorsaCardsDTO != null && aucorsaCardsDTO.getCards() != null
                        && aucorsaCardsDTO.getCards().size() > 0) {
                    selectedCardDTO = aucorsaCardsDTO.getCards().get(position);
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
        aucorsaCardsDTO = PreferencesHelper.getInstance().getCards(this);
        if (aucorsaCardsDTO != null && aucorsaCardsDTO.getCards() != null) {
            for (TussamCardDTO card : aucorsaCardsDTO.getCards()) {
                spinnerAdapter.add(card.getCardName());
                if (selectedCardDTO != null
                        && selectedCardDTO.isEmpty()
                        && card.getIsCardFavorite() != null
                        && card.getIsCardFavorite()) {
                    selectedCardDTO = card;
                }
            }
        }

        if (selectedCardDTO != null && aucorsaCardsDTO != null && aucorsaCardsDTO.getCards() != null) {
            int newCardIndex = aucorsaCardsDTO.getCards().indexOf(selectedCardDTO);
            cardsSpinner.setSelection(newCardIndex);
        }
        spinnerAdapter.notifyDataSetChanged();
    }


    @Click(R.id.discard_card_image)
    void deleteCardClicked() {

        if (selectedCardDTO != null) {
            PreferencesHelper.getInstance().deleteCards(this);
            aucorsaCardsDTO.getCards().remove(selectedCardDTO);
            selectedCardDTO = null;
            PreferencesHelper.getInstance().saveCards(this, aucorsaCardsDTO);
            loadSavedCards();
            loadFirstView();
        }
    }

    @Click(R.id.edit_card_image)
    void editCardClicked() {

        showEditView();
    }

    @Click
    void addCardButtonClicked() {

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
            //newCardNumber = newCardNumber.replaceFirst("^0+(?!$)", "");
            if (selectedCardDTO != null && !newCardNumber.equals("")) {
                selectedCardDTO.setCardName(newCardName);
                selectedCardDTO.setCardNumber(newCardNumber);
                aucorsaCardsDTO.getCards().set(cardsSpinner.getSelectedItemPosition(), selectedCardDTO);
                PreferencesHelper.getInstance().saveCards(this, aucorsaCardsDTO);
                loadSavedCards();
                showDetailView();
                reloadData();
                requestCardInfo();
            }
        }
    }

    @Click(R.id.remove_card_image)
    void cancelClicked() {
        if (aucorsaCardsDTO != null &&
                aucorsaCardsDTO.getCards() != null &&
                aucorsaCardsDTO.getCards().size() > 0) {
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
    void rechargeCardClicked() {
        if (selectedCardDTO != null) {
            String externalUrl = CREDIT_URL + selectedCardDTO.getCardNumber();
            GeneralHelper.launchExternalUrlWeb(this, externalUrl, getString(R.string.recharge_card_text), getString(R.string.alert_yes), getString(R.string.alert_no));
        }
    }

    @Click
    void favoriteCardCbClicked() {
        toggleFavoriteCardIndicator(favoriteCardCb.isChecked());
    }

    @Background
    void toggleFavoriteCardIndicator(boolean isChecked) {
        if (selectedCardDTO != null && aucorsaCardsDTO != null && aucorsaCardsDTO.getCards() != null) {
            selectedCardDTO.setIsCardFavorite(isChecked);
            int cardsSize = aucorsaCardsDTO.getCards().size();
            for (int i = 0; i < cardsSize; i++) {
                aucorsaCardsDTO.getCards().get(i).setIsCardFavorite(false);
                if (isChecked && selectedCardDTO == aucorsaCardsDTO.getCards().get(i)) {
                    aucorsaCardsDTO.getCards().get(i).setIsCardFavorite(true);
                }
            }
            PreferencesHelper.getInstance().saveCards(this, aucorsaCardsDTO);
        }
    }


    private void requestCardInfo() {

        if (selectedCardDTO != null) {
            hideData();
            preloadData();
            if (selectedCardDTO.getCardNumber() != null) {
                progressView.setVisibility(View.VISIBLE);
                timeView.setVisibility(View.INVISIBLE);
                String cardNumber = trim(selectedCardDTO.getCardNumber(), 0, selectedCardDTO.getCardNumber().length());
                parseHtml(cardNumber);
                /*
                StringRequest req = new StringRequest(STATUS_URL + cardNumber, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseHtml(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        reloadData();
                        String errorString = getString(R.string.parse_error);
                        if (error.networkResponse != null &&
                                error.networkResponse.statusCode == 500 &&
                                selectedCardDTO != null &&
                                selectedCardDTO.getCardCredit() == null) {

                            // Server error
                            cardTypeText.setText(getString(R.string.wrong_card_number_error));
                        } else {
                            // Network error
                            errorString = getString(R.string.network_error);
                        }
                        Toast.makeText(MainActivity.this, errorString, Toast.LENGTH_LONG).show();
                        showDetailView();
                    }
                });


                // add the request object to the queue to be executed
                addToRequestQueue(req);
                */
            }
        }

    }

    private void hideData() {
        cardsSpinner.setVisibility(View.VISIBLE);
        cardsData.setVisibility(View.INVISIBLE);
        cardsEditData.setVisibility(View.INVISIBLE);
        cardCreditText.setVisibility(View.INVISIBLE);
        cardNumberText.setVisibility(View.INVISIBLE);
        // remove soft keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(cardNumberText.getWindowToken(), 0);
    }

    private void saveNewCard(TussamCardDTO card) {

        selectedCardDTO = card;
        if (aucorsaCardsDTO != null && aucorsaCardsDTO.getCards() != null) {
            aucorsaCardsDTO.getCards().add(card);
        } else {
            aucorsaCardsDTO = new TussamCardsDTO();
            aucorsaCardsDTO.setCards(new ArrayList<TussamCardDTO>());
            aucorsaCardsDTO.getCards().add(card);
        }
        PreferencesHelper.getInstance().saveCards(this, aucorsaCardsDTO);
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

    @Background
    public void parseHtml(String response) {

        boolean errorFound = false;
        if (selectedCardDTO == null) {
            selectedCardDTO = new TussamCardDTO();
        }
        try {
            Document document = Jsoup.connect(STATUS_URL+response)
                    .userAgent(getEncStr("TW96aWxsYS81LjAgKGNvbXBhdGlibGU7IEdvb2dsZWJvdC8yLjE7ICtodHRwOi8vd3d3Lmdvb2dsZS5jb20vYm90Lmh0bWwp"))
                    .get();

            Element mainDiv = document.getElementById("global");
            if (mainDiv != null) {
                //Element cardDiv = mainDiv.getElementsByClass("card").get(0);
                if (!mainDiv.select("span.spanSaldo").isEmpty()) {
                    String credit = mainDiv.select("span.spanSaldo").text();
                    selectedCardDTO.setCardCredit(credit);
                    selectedCardDTO.setLastDate((new Date()).getTime());
                } else {
                    selectedCardDTO.setCardType(mainDiv.select("span#spanMsg").text());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save retrieved card
        if (cardsSpinner != null &&
                aucorsaCardsDTO != null &&
                aucorsaCardsDTO.getCards() != null &&
                aucorsaCardsDTO.getCards().size() > cardsSpinner.getSelectedItemPosition()) {

            aucorsaCardsDTO.getCards().set(cardsSpinner.getSelectedItemPosition(), selectedCardDTO);
            PreferencesHelper.getInstance().saveCards(this, aucorsaCardsDTO);
        }

        reloadData();
        showDetailView();

    }

    @UiThread
    public void preloadData() {
        // Load data stored in preferences like favorite card flag
        if (selectedCardDTO != null) {
            favoriteCardCb.setChecked(selectedCardDTO.getIsCardFavorite() != null ? selectedCardDTO.getIsCardFavorite() : false);
        }
        reloadData();
    }

    @UiThread
    public void reloadData() {
        if (selectedCardDTO != null) {
            cardNameText.setText(selectedCardDTO.getCardName() != null ? selectedCardDTO.getCardName() : "");
            cardEditNameText.setText(selectedCardDTO.getCardName() != null ? selectedCardDTO.getCardName() : "");
            cardEditNumberText.setText(getFormattedNumber(selectedCardDTO.getCardNumber() != null ? selectedCardDTO.getCardNumber() : ""));
            cardStatusText.setText(selectedCardDTO.getCardStatus() != null ? selectedCardDTO.getCardStatus() : "");
            cardTypeText.setText(selectedCardDTO.getCardType() != null && selectedCardDTO.getCardType().length() > 0 ? selectedCardDTO.getCardType().substring(1, selectedCardDTO.getCardType().length()) : "");
            cardCreditText.setText(selectedCardDTO.getCardCredit() != null ? selectedCardDTO.getCardCredit() : "");
            cardNumberText.setText(getFormattedNumber(selectedCardDTO.getCardNumber() != null ? selectedCardDTO.getCardNumber() : ""));

            if (selectedCardDTO.getLastDate() != null) {
                Date nowDate = new Date();
                long nowDateDifferenceLong = nowDate.getTime() - selectedCardDTO.getLastDate();
                String dateString = "";

                if (nowDateDifferenceLong < ONE_MINUTE) {
                    // Less than 1 minute (now)
                    dateString = getString(R.string.updated_now);

                } else if (nowDateDifferenceLong >= ONE_MINUTE && nowDateDifferenceLong < ONE_HOUR) {
                    // More than 1 minute ( X minutes ago)
                    dateString = TagFormat.from(getString(R.string.updated_minutes_ago))
                            .with("minutes", String.valueOf(nowDateDifferenceLong / ONE_MINUTE))
                            .format();

                } else if (nowDateDifferenceLong >= ONE_HOUR && nowDateDifferenceLong < ONE_DAY) {
                    // More than 1 hour (X hours ago)
                    dateString = TagFormat.from(getString(R.string.updated_hours_ago))
                            .with("hour", String.valueOf(nowDateDifferenceLong / ONE_HOUR))
                            .format();

                } else {
                    // More than 1 day (Date)
                    dateString = new SimpleDateFormat(getString(R.string.updated_date_format)).format(new Date(selectedCardDTO.getLastDate()));

                }

                cardLastUpdateText.setText(TagFormat.from(getString(R.string.last_date_update))
                        .with("date", dateString)
                        .format());

            } else {
                cardLastUpdateText.setText("");
            }
        }
    }

    private String getFormattedNumber(String number) {
        return number;
    }

    private void clearData() {
        cardNameText.setText("");
        cardNumberText.setText("");
        cardStatusText.setText("");
        cardTypeText.setText("");
        cardCreditText.setText("");
    }

    @UiThread
    public void showDetailView() {
        favoriteCardCb.setVisibility(View.VISIBLE);
        progressView.setVisibility(View.GONE);
        timeView.setVisibility(View.VISIBLE);
        isDetailView = true;
        isEditView = false;
        isAddView = false;
        cardsSpinner.setVisibility(View.VISIBLE);
        cardsData.setVisibility(View.VISIBLE);
        cardsEditData.setVisibility(View.GONE);
        cardActions.setVisibility(View.VISIBLE);
        cardEditActions.setVisibility(View.GONE);
        cardNewActions.setVisibility(View.GONE);
        addCardButton.setVisibility(View.VISIBLE);
        // remove soft keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(cardNumberText.getWindowToken(), 0);
        minicard.setVisibility(View.VISIBLE);
        cardCreditText.setVisibility(View.VISIBLE);
        cardLastUpdateText.setVisibility(View.VISIBLE);
        cardNumberText.setVisibility(View.VISIBLE);
    }

    private void showAddView() {
        favoriteCardCb.setVisibility(View.GONE);
        cancelPendingRequests(TAG);
        progressView.setVisibility(View.GONE);
        timeView.setVisibility(View.GONE);
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
        minicard.setVisibility(View.GONE);
        cardCreditText.setVisibility(View.GONE);
        addCardButton.setVisibility(View.GONE);
        cardLastUpdateText.setVisibility(View.INVISIBLE);
        cardNumberText.setVisibility(View.GONE);
    }

    private void showEditView() {
        favoriteCardCb.setVisibility(View.VISIBLE);
        cancelPendingRequests(TAG);
        isDetailView = false;
        isEditView = true;
        isAddView = false;
        if (selectedCardDTO != null) {
            cardEditNumberText.setText(getFormattedNumber(selectedCardDTO.getCardNumber()));
            cardEditNameText.setText(selectedCardDTO.getCardName());
        }
        cardsSpinner.setVisibility(View.GONE);
        cardsData.setVisibility(View.GONE);
        cardsEditData.setVisibility(View.VISIBLE);
        cardActions.setVisibility(View.GONE);
        cardEditActions.setVisibility(View.VISIBLE);
        cardNewActions.setVisibility(View.GONE);
        minicard.setVisibility(View.GONE);
        cardCreditText.setVisibility(View.GONE);
        addCardButton.setVisibility(View.GONE);
        cardLastUpdateText.setVisibility(View.INVISIBLE);
        timeView.setVisibility(View.GONE);
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

    /**
     * Other
     */

    public static String getEncStr(String base64) {
        String text = null;
        byte[] data = Base64.decode(base64, Base64.DEFAULT);
        try {
            text = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return text;
    }

    private class GetCard extends AsyncTask<String, Void, Void> {
        private String tarjeta = null;
        private String saldo = null;
        private boolean error = false;
        private boolean registrar = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            tarjeta = params[0];
            String url = getEncStr(STATUS_URL);
            Log.d(TAG, url);
            /*
            try {
                tarjeta = params[0];

                Document document = Jsoup.connect(getEncStr(STATUS_URL)+tarjeta)
                        .userAgent(getEncStr(getString(R.string.url_ua)))
                        .get();
                if(document.select("span.spanSaldo").isEmpty()) {
                    error = true;
                    if(!document.select("span#spanMsg").isEmpty())
                        errorTXT = document.select("span#spanMsg").text();
                    if(!document.select("div.divFormRegistration").isEmpty()) {
                        registrar = true;
                    }
                } else {
                    saldo = document.select("span.spanSaldo").text();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            */
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!error) {
                if (saldo != null) {
                    //Intent intent = new Intent(getApplicationContext(), InfoActivity.class);
                    //intent.putExtra("SALDO", saldo);
                    //intent.putExtra("NUMTARJETA", tarjeta);
                    //startActivity(intent);
                } else {
                    //showMessage(getString(R.string.error), getString(R.string.error_saldo));
                }
            } else {

            }
        }
    }
}
