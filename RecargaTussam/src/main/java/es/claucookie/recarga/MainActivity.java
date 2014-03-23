package es.claucookie.recarga;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
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
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import es.claucookie.recarga.helpers.SharedPrefsHelper;
import es.claucookie.recarga.helpers.SharedPrefsHelper_;

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
    ProgressBar progressBar;
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
    String cardName;

    @InstanceState
    String cardNumber;

    @InstanceState
    String cardStatus;

    @InstanceState
    String cardType;

    @InstanceState
    String cardCredit;

    @InstanceState
    LinkedHashMap<String, String> savedCards = new LinkedHashMap<String, String>();

    @InstanceState
    boolean isDetailView = false, isAddView = false, isEditView = false;

    @Pref
    SharedPrefsHelper_ prefsHelper;


    private ArrayAdapter<String> spinnerAdapter;


    /**
     * Global request queue for Volley
     */
    private RequestQueue mRequestQueue;

    @AfterViews
    void initViews() {
        initProgressBar();
        initSpinner();
        loadSavedCards();
        if (savedCards.size() > 0) {
            showDetailView();
        } else {
            showAddView();
        }
    }

    private void initProgressBar() {
        hideProgressBar();
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
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
                String key = (new ArrayList<String>(savedCards.keySet())).get(position);
                cardName = cardsSpinner.getSelectedItem().toString();
                cardNumber = key.trim();
                requestCardInfo(cardNumber);
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
        spinnerAdapter.addAll(savedCards.values());
        spinnerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
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
        if (!newCardNumber.equals("")) {
            newCardNumber = newCardNumber.replaceFirst("^0+(?!$)", "");
            savedCards.put(newCardNumber, newCardName);
            loadSavedCards();
            if (spinnerAdapter.getCount() > 0) {
                cardsSpinner.setSelection(spinnerAdapter.getCount() - 1);
            }
            showDetailView();
        }
    }

    @OptionsItem(R.id.save_card)
    void saveCardClicked() {
        String newCardNumber = cardEditNumberText.getText() != null ? cardEditNumberText.getText().toString().replace(" ", "") : "";
        String newCardName = cardEditNameText.getText() != null ? cardEditNameText.getText().toString() : "";
        if (!newCardNumber.equals("")) {
            newCardNumber = newCardNumber.replaceFirst("^0+(?!$)", "");
            savedCards.put(newCardNumber, newCardName);
            loadSavedCards();
            showDetailView();
        }
    }


    private void requestCardInfo(String cardNumber) {
        showProgressBar();
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

    private void parseHtml(String response) {

        boolean errorFound = false;
        Document responseDoc = Jsoup.parse(response);
        Element mainDiv = responseDoc.getElementById("cardStatus");
        if (mainDiv != null) {
            Elements cardInfo = mainDiv.select("span");
            // CardNumber
            if (cardInfo.size() > 0 && cardInfo.get(0) != null) {
                cardNumber = cardInfo.get(0).text();
            } else errorFound = true;

            // CardStatus
            if (cardInfo.size() > 1 && cardInfo.get(1) != null) {
                cardStatus = cardInfo.get(1).text();
            } else errorFound = true;

            // CardType
            if (cardInfo.size() > 2 && cardInfo.get(2) != null) {
                cardType = cardInfo.get(2).text();
            } else errorFound = true;

            // CardCredit
            if (cardInfo.size() > 3 && cardInfo.get(3) != null) {
                cardCredit = cardInfo.get(3).text();
            } else errorFound = true;

        } else {
            errorFound = true;
        }

        if (errorFound) {
            Toast.makeText(this, getString(R.string.parse_error), Toast.LENGTH_LONG).show();
        }

    }

    private void reloadData() {
        hideProgressBar();
        if (cardName != null) {
            cardNameText.setText(cardName);
            cardEditNameText.setText(cardName);
        }
        if (cardNumber != null) {
            cardNumberText.setText(cardNumber);
            cardEditNumberText.setText(cardNumber);
        }
        if (cardStatus != null) {
            cardStatusText.setText(cardStatus);
        }
        if (cardType != null) {
            cardTypeText.setText(cardType);
        }
        if (cardCredit != null) {
            cardCreditText.setText(cardCredit);
        }
    }

    private void clearData() {
        progressBar.setVisibility(View.GONE);
        cardNameText.setText(cardName);
        cardNumberText.setText(cardNumber);
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
        InputMethodManager imm =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
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
        cardEditNumberText.setText(cardNumber);
        cardEditNameText.setText(cardName);
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
