package es.claucookie.recarga.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import es.claucookie.recarga.NetworkConsts;
import es.claucookie.recarga.R;
import es.claucookie.recarga.helpers.PreferencesHelper;
import es.claucookie.recarga.iabutil.IabHelper;
import es.claucookie.recarga.iabutil.IabResult;
import es.claucookie.recarga.iabutil.Inventory;
import es.claucookie.recarga.iabutil.Purchase;
import es.claucookie.recarga.logic.InAppBillingLogic;

/**
 * Created by claucookie on 15/02/15.
 */
@EActivity(R.layout.activity_settings)
public class SettingsActivity extends ActionBarActivity {

    private static final String TAG = SettingsActivity.class.getName();

    @ViewById
    Button inappButton;

    private boolean publiRemovePurchased = false;

    @AfterViews
    void initViews() {
        initActionBar();
        checkStoredPreferences();
    }

    private void initActionBar() {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @OptionsItem(android.R.id.home)
    void homeSelected() {
        finish();
    }

    @Click
    void inappButtonClicked() {
        if (InAppBillingLogic.getInstance().getIabHelper() != null) {
            startPurchaseFlowDialog();
        } else {
            InAppBillingLogic.getInstance().setupInappBilling(this, inappSetupFinishedListener);
        }

    }

    /**
     * Preferences 
     */
    private void checkStoredPreferences() {
        publiRemovePurchased = PreferencesHelper.getInstance().inappPurchased(this);
        updatePubliLayout();
        if (!publiRemovePurchased) {
            setupInappBilling();
        }
    }

    private void updateStoredPreferences() {
        // Save Purchase state into preferences
        PreferencesHelper.getInstance().setInappPurchased(this, publiRemovePurchased);
    }

    /**
     * Inapp billing methods
     */

    private void startPurchaseFlowDialog() {
        String payload = InAppBillingLogic.getInstance().getPayloadByMainAccount(this);
        String inAppCode = NetworkConsts.REMOVE_PUB_INAPP_CODE;
        InAppBillingLogic.getInstance().getIabHelper().flagEndAsync();
        InAppBillingLogic.getInstance().getIabHelper().launchPurchaseFlow(this, inAppCode, NetworkConsts.IN_APP_PURCHASE_REQUEST_CODE,
                purchaseFinishedListener, payload);

    }

    private void checkInappStatusAndShowInfo() {
        // Is remove app inapp purchased?
        publiRemovePurchased = InAppBillingLogic.getInstance().checkPurchasedItem(this, NetworkConsts.REMOVE_PUB_INAPP_CODE);
        updatePubliLayout();
        updateStoredPreferences();
    }

    private void updatePubliLayout() {
        if (publiRemovePurchased) {
            // show purchased view
            inappButton.setVisibility(View.GONE);
        } else {
            updatePriceLayout();
            // show purchase view
            inappButton.setVisibility(View.VISIBLE);
        }

    }

    private void updatePriceLayout() {
        String price = InAppBillingLogic.getInstance().getPriceForProduct(NetworkConsts.REMOVE_PUB_INAPP_CODE);
        inappButton.setText(price);
    }

    private void setupInappBilling() {
        // Creating sku array to request their details
        ArrayList<String> moreSkus = new ArrayList<String>();
        moreSkus.add(NetworkConsts.REMOVE_PUB_INAPP_CODE);
        InAppBillingLogic.getInstance().setSkuDetailsList(moreSkus);

        if (InAppBillingLogic.getInstance().getIabHelper() != null) {
            InAppBillingLogic.getInstance().queryInventory(gotInventoryListener);
        } else {
            InAppBillingLogic.getInstance().setupInappBilling(this, gotInventoryListener);
        }

    }


    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener gotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            InAppBillingLogic.getInstance().setInventory(inventory);
            checkInappStatusAndShowInfo();
        }
    };

    // Callback for when a setup is finished
    IabHelper.OnIabSetupFinishedListener inappSetupFinishedListener = new IabHelper.OnIabSetupFinishedListener() {

        @Override
        public void onIabSetupFinished(IabResult result) {
            if (result.isSuccess()) {
                Log.d(TAG, "Inapp setup finished and purchase flow is starting now.");
                startPurchaseFlowDialog();
            } else if (result.isFailure()) {
                Log.d(TAG, result.getMessage());
            }
        }
    };

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener purchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            publiRemovePurchased = result.isSuccess();
            checkInappStatusAndShowInfo();
        }
    };


}
