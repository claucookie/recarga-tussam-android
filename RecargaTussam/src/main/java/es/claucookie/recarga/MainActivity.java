package es.claucookie.recarga;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
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

import es.claucookie.recarga.helpers.PreferencesHelper;
import es.claucookie.recarga.model.dto.TussamCardDTO;
import es.claucookie.recarga.model.dto.TussamCardsDTO;

@OptionsMenu(R.menu.menu_add_edit)
@EActivity(R.layout.activity_main)
public class MainActivity extends Activity {
    public static final String URL = "http://recargas.tussam.es/TPW/Common/cardStatus.do?swNumber=";

    /**
     * Log or request TAG
     */
    public static final String TAG = "VolleyPatterns";

    @ViewById
    TextView cardNumberText;
    @ViewById
    TextView cardStatusText;
    @ViewById
    TextView cardTypeText;
    @ViewById
    TextView cardCreditText;
    @ViewById
    Spinner cardsSpinner;
    @ViewById
    ExLabel cardNameText;
    @ViewById
    ExText cardEditNameText;
    @ViewById
    ExText cardEditNumberText;
    @ViewById
    RelativeLayout cardsEditData;
    @ViewById
    RelativeLayout cardsData;
    @ViewById
    LinearLayout tussamInfo;

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
        loadDetailView();
    }

    private void loadDetailView() {
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
                    if (selectedCardDTO != null) {
                        requestCardInfo(selectedCardDTO.getCardNumber());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        cardsSpinner.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Delete item

                return false;
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
            }
        }
        spinnerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isDetailView) {
            menu.clear();
            getMenuInflater().inflate(R.menu.menu_add_edit, menu);
        } else if (isEditView) {
            menu.clear();
            getMenuInflater().inflate(R.menu.menu_save_delete, menu);
        } else if (isAddView) {
            menu.clear();
            getMenuInflater().inflate(R.menu.menu_done, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }


    @OptionsItem(R.id.edit_card)
    void editCardClicked() {

        showEditView();
        invalidateOptionsMenu();
    }

    @OptionsItem(R.id.add_card)
    void addCardClicked() {

        showAddView();
        invalidateOptionsMenu();
    }

    @OptionsItem(R.id.create_card)
    void createCardClicked() {

        String newCardNumber = cardEditNumberText.getText() != null ? cardEditNumberText.getText().toString().replace(" ", "") : "";
        String newCardName = cardEditNameText.getText() != null ? cardEditNameText.getText().toString() : "";
        newCardNumber = newCardNumber.replaceFirst("^0+(?!$)", "");
        if (!newCardNumber.equals("")) {
            TussamCardDTO newCard = new TussamCardDTO();
            newCard.setCardName(newCardName);
            newCard.setCardNumber(newCardNumber);
            saveNewCard(newCard);
            loadSavedCards();
            showDetailView();
            reloadData();
        }
    }


    @OptionsItem(R.id.save_card)
    void saveCardClicked() {
        String newCardNumber = cardEditNumberText.getText() != null ? cardEditNumberText.getText().toString().replace(" ", "") : "";
        String newCardName = cardEditNameText.getText() != null ? cardEditNameText.getText().toString() : "";
        newCardNumber = newCardNumber.replaceFirst("^0+(?!$)", "");
        if (!newCardNumber.equals("")) {
            selectedCardDTO.setCardName(newCardName);
            selectedCardDTO.setCardNumber(newCardNumber);
            tussamCardsDTO.getCards().set(cardsSpinner.getSelectedItemPosition(), selectedCardDTO);
            PreferencesHelper.getInstance().saveCards(this, tussamCardsDTO);
            loadSavedCards();
            showDetailView();
            reloadData();
        }
    }

    @OptionsItem(R.id.discard_card)
    void cancelClicked() {
        showDetailView();
    }

    @OptionsItem(R.id.refresh_card)
    void refreshClicked() {
        if (selectedCardDTO != null) {
            requestCardInfo(selectedCardDTO.getCardNumber());
        }
    }

    private void requestCardInfo(String cardNumber) {
        hideData();
        if (cardNumber != null) {
            cardNumber = trim(cardNumber, 0, cardNumber.length());
            StringRequest req = new StringRequest(URL + cardNumber, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    parseHtml(response);
                    reloadData();
                    showDetailView();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, getString(R.string.parse_error), Toast.LENGTH_LONG).show();
                    clearData();
                }
            });

            // add the request object to the queue to be executed
            addToRequestQueue(req);
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

    private void reloadData() {
        if (selectedCardDTO != null) {
            cardNameText.setText(selectedCardDTO.getCardName());
            cardEditNameText.setText(selectedCardDTO.getCardName());
            cardNumberText.setText(selectedCardDTO.getCardNumber());
            cardEditNumberText.setText(selectedCardDTO.getCardNumber());
            cardStatusText.setText(selectedCardDTO.getCardStatus());
            cardTypeText.setText(selectedCardDTO.getCardType());
            cardCreditText.setText(selectedCardDTO.getCardCredit());
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
        isDetailView = true;
        isEditView = false;
        isAddView = false;
        cardsSpinner.setVisibility(View.VISIBLE);
        cardsData.setVisibility(View.VISIBLE);
        cardsEditData.setVisibility(View.GONE);
        invalidateOptionsMenu();
        // remove soft keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(cardNumberText.getWindowToken(), 0);
    }

    private void showAddView() {
        isDetailView = false;
        isEditView = false;
        isAddView = true;
        cardEditNumberText.setText("");
        cardEditNameText.setText("");
        cardsSpinner.setVisibility(View.GONE);
        cardsData.setVisibility(View.GONE);
        cardsEditData.setVisibility(View.VISIBLE);
        invalidateOptionsMenu();
    }

    private void showEditView() {
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
        invalidateOptionsMenu();
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
