package es.claucookie.recarga;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import es.claucookie.recarga.helpers.ParseHelper;
import es.claucookie.recarga.model.dto.TussamCardDTO;

public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private TextView cardName;
    private TextView cardCredit;
    private TextView cardTrips;
    private TextView cardUpdate;
    private ProgressBar loadingView;
    private LinearLayout cardInfoView;
    GoogleApiClient googleClient;
    TussamCardDTO favoriteCard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        // Register the local broadcast receiver, defined in step 3.
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);


        // Build a new GoogleApiClient
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void setupViews() {
        showLoadingView();
    }

    private void showInfoView() {
        loadingView.setVisibility(View.GONE);
        cardInfoView.setVisibility(View.VISIBLE);
    }

    private void showInfoAndLoadingView() {
        loadingView.setVisibility(View.VISIBLE);
        cardInfoView.setVisibility(View.VISIBLE);
    }

    private void showLoadingView() {
        loadingView.setVisibility(View.VISIBLE);
        cardInfoView.setVisibility(View.GONE);
    }

    private void initView() {
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                cardName = (TextView) stub.findViewById(R.id.card_name);
                cardCredit = (TextView) stub.findViewById(R.id.card_credit);
                cardTrips = (TextView) stub.findViewById(R.id.card_trips);
                cardUpdate = (TextView) stub.findViewById(R.id.card_update);
                loadingView = (ProgressBar) stub.findViewById(R.id.loading_view);
                cardInfoView = (LinearLayout) stub.findViewById(R.id.card_info);
                setupViews();
            }
        });
    }

    private void loadData() {

        if (favoriteCard != null) {
            cardName.setText(favoriteCard.getCardName());
            cardCredit.setText(favoriteCard.getCardCredit());
            cardTrips.setText(favoriteCard.getCardStatus());
            cardUpdate.setText(ParseHelper.parseDate(this, favoriteCard.getLastDate()));
        }
    }

    // Connect to the data layer when the Activity starts
    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();
    }

    /**
     * Google api methods
     */

    // Send a message when the data layer connection is successful.
    @Override
    public void onConnected(Bundle connectionHint) {
        String message = "Connected to handheld !!!";
        // Send Message to Handheld to request Card info
        new SendToDataLayerThread(Consts.GET_FAVORITE_CARD_INFO_MESSAGE, message).start();
    }

    // Disconnect from the data layer when the Activity stops
    @Override
    protected void onStop() {
        if (null != googleClient && googleClient.isConnected()) {
            googleClient.disconnect();
        }
        super.onStop();
    }

    // Placeholders for required connection callbacks
    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    class SendToDataLayerThread extends Thread {
        String path;
        String message;

        // Constructor to send a message to the data layer
        SendToDataLayerThread(String p, String msg) {
            path = p;
            message = msg;
        }

        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
            for (Node node : nodes.getNodes()) {
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(), path, message.getBytes()).await();
                if (result.getStatus().isSuccess()) {
                    if (BuildConfig.DEBUG) {
                        Log.v("myTag", "Message: {" + message + "} sent to: " + node.getDisplayName());
                    }
                } else {
                    // Log an error
                    if (BuildConfig.DEBUG) {
                        Log.v("myTag", "ERROR: failed to send Message");
                    }
                }
            }
        }
    }

    /**
     * Broadcast receivers
     */

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra(Consts.CARD_DATA)) {
                String cardDataString = intent.getStringExtra(Consts.CARD_DATA);
                // Display message in UI
                favoriteCard = ParseHelper.parseData(cardDataString);
                loadData();

                if (intent.hasExtra(Consts.CARD_DATA_REQUEST_FINISHED)) {
                    if (!intent.getBooleanExtra(Consts.CARD_DATA_REQUEST_FINISHED, false)) {
                        showInfoAndLoadingView();
                    } else {
                        showInfoView();
                    }
                } else {
                    showInfoView();
                }
            }
        }
    }

}
