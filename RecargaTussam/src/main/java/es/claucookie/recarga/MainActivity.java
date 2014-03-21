package es.claucookie.recarga;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
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

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

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


    @InstanceState
    String cardNumber;

    @InstanceState
    String cardStatus;

    @InstanceState
    String cardType;

    @InstanceState
    String cardCredit;

    @InstanceState
    ArrayList<String> savedCards = new ArrayList<String>();
    @ViewById
    ProgressBar progressBar;
    @ViewById
    ExLabel cardNameText;

    private ArrayAdapter<String> spinnerAdapter;


    /**
     * Global request queue for Volley
     */
    private RequestQueue mRequestQueue;

    @AfterViews
    void initViews() {
        initSpinner();
        loadSavedCards();
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
                requestCardInfo((String) parent.getItemAtPosition(position));
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
        savedCards.add("31161031261");
        savedCards.add("31161031260");
        spinnerAdapter.clear();
        spinnerAdapter.addAll(savedCards);
        spinnerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

    }

    private void requestCardInfo(String cardNumber) {
        progressBar.setVisibility(View.VISIBLE);
        StringRequest req = new StringRequest(URL + cardNumber, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseHtml(response);
                reloadData();
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
        progressBar.setVisibility(View.GONE);
        if (cardNumber != null) {
            cardNumberText.setText(cardNumber);
        }
        if (cardStatus != null) {
            cardStatusText.setText("  " + cardStatus);
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
        cardNumberText.setText(getString(R.string.card_number));
        cardStatusText.setText(getString(R.string.card_status));
        cardTypeText.setText(getString(R.string.card_type));
        cardCreditText.setText(getString(R.string.card_credit));
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
