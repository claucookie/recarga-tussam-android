package es.claucookie.recarga.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import com.android.volley.Network;
import com.mobivery.android.widgets.ExText;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.jsoup.helper.StringUtil;

import java.util.ArrayList;

import es.claucookie.recarga.CheckCreditBroadcastReceiver;
import es.claucookie.recarga.Consts;
import es.claucookie.recarga.NetworkConsts;
import es.claucookie.recarga.R;
import es.claucookie.recarga.helpers.PreferencesHelper;
import es.claucookie.recarga.iabutil.IabHelper;
import es.claucookie.recarga.iabutil.IabResult;
import es.claucookie.recarga.iabutil.Inventory;
import es.claucookie.recarga.iabutil.Purchase;
import es.claucookie.recarga.logic.InAppBillingLogic;
import es.claucookie.recarga.model.dto.TussamCardsDTO;

/**
 * Created by claucookie on 15/02/15.
 */
@EActivity(R.layout.activity_settings)
public class SettingsActivity extends ActionBarActivity {

    private static final String TAG = SettingsActivity.class.getName();

    @ViewById
    Button inappButton;
    @ViewById
    SwitchCompat alarmSwitch;
    @ViewById
    RelativeLayout creditLayout;
    @ViewById
    ExText creditValueText;

    private boolean publiRemovePurchased = false;

    @AfterViews
    void initViews() {
        initActionBar();
        checkStoredPreferences();
        initCreditLayout();
    }

    private void initActionBar() {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void initCreditLayout() {
        alarmSwitch.setHighlightColor(getResources().getColor(R.color.color_palette_2));
        showCreditLayout(alarmSwitch.isChecked());
    }
    
    @Override
    public void onBackPressed() {
        checkAlarmSwitch();
        super.onBackPressed();
    }

    @OptionsItem(android.R.id.home)
    void homeSelected() {
        checkAlarmSwitch();
        finish();
    }
    
    private void checkAlarmSwitch() {
        PreferencesHelper.getInstance().activateCreditAlarm(this, alarmSwitch.isChecked());
        if (PreferencesHelper.getInstance().creditAlarmActivated(this)) {
            scheduleAlarm();
        } else {
            cancelAlarm();
        }
        // Save min credit value
        String creditString = creditValueText.getText().toString();
        int credit = Integer.valueOf(
                !StringUtil.isBlank(creditString) ? creditString : "0"
        );
        PreferencesHelper.getInstance().setCreditAlarmValue(this, credit);
    }

    @Click
    void inappButtonClicked() {
        if (InAppBillingLogic.getInstance().getIabHelper() != null) {
            startPurchaseFlowDialog();
        } else {
            InAppBillingLogic.getInstance().setupInappBilling(this, inappSetupFinishedListener);
        }
    }

    @CheckedChange(R.id.alarm_switch)
    void alarmSwitchChanged(CompoundButton switchCompat) {
        showCreditLayout(switchCompat.isChecked());
    }

    private void showCreditLayout(boolean checked) {
        if (checked) {
            creditLayout.setVisibility(View.VISIBLE);
        } else {
            creditLayout.setVisibility(View.GONE);
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
        if (PreferencesHelper.getInstance().creditAlarmActivated(this)) {
            alarmSwitch.setChecked(true);
        } else {
            alarmSwitch.setChecked(false);
        }
        creditValueText.setText(PreferencesHelper.getInstance().getCreditAlarmValue(this));
    }

    private void updateStoredPreferences() {
        // Save Purchase state into preferences
        PreferencesHelper.getInstance().setInappPurchased(this, publiRemovePurchased);
    }

    /**
     * Alarm methods
     */

    @Background
    public void scheduleAlarm() {
        TussamCardsDTO aucorsaCardsDTO = PreferencesHelper.getInstance().getCards(this);

        if (aucorsaCardsDTO != null) {
            // Construct an intent that will execute the AlarmReceiver
            Intent intent = new Intent(getApplicationContext(), CheckCreditBroadcastReceiver.class);
            String creditString = creditValueText.getText().toString();
            int credit = Integer.valueOf(
                    !StringUtil.isBlank(creditString) ? creditString : "0"
            );
            intent.putExtra(Consts.ALARM_CREDIT_EXTRA, credit);
            // Create a PendingIntent to be triggered when the alarm goes off
            final PendingIntent pIntent = PendingIntent.getBroadcast(this, CheckCreditBroadcastReceiver.REQUEST_CODE,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            // Setup periodic alarm every 5 seconds
            long firstMillis = System.currentTimeMillis(); // first run of alarm is immediate
            int intervalMillis = 60000; //5sec //1000 * 60 * 60 * 24; // 24 hours in miliseconds
            AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, intervalMillis, pIntent);
        }
    }

    @Background
    public void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), CheckCreditBroadcastReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, CheckCreditBroadcastReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
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

    private void checkInappStatusAndShowInfo(boolean purchased) {
        // Is remove app inapp purchased?
        publiRemovePurchased = purchased;
        if (!purchased) {
            publiRemovePurchased = InAppBillingLogic.getInstance().checkPurchasedItem(this, NetworkConsts.REMOVE_PUB_INAPP_CODE);
        }
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
            checkInappStatusAndShowInfo(false);
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
            if (result.getResponse() == IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED) {
                publiRemovePurchased = true;
            }
            checkInappStatusAndShowInfo(publiRemovePurchased);
        }
    };

    @OnActivityResult(NetworkConsts.IN_APP_PURCHASE_REQUEST_CODE)
    void onResult(int resultCode, Intent data) {
        publiRemovePurchased = resultCode == Activity.RESULT_OK;
        checkInappStatusAndShowInfo(publiRemovePurchased);
    }


}
