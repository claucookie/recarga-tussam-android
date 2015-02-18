package es.claucookie.recarga.logic;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import es.claucookie.recarga.BuildConfig;
import es.claucookie.recarga.Consts;
import es.claucookie.recarga.NetworkConsts;
import es.claucookie.recarga.iabutil.IabHelper;
import es.claucookie.recarga.iabutil.IabResult;
import es.claucookie.recarga.iabutil.Inventory;
import es.claucookie.recarga.iabutil.Purchase;

/**
 * Created by claucookie on 26/08/14.
 */
public class InAppBillingLogic {

    private static final String TAG = InAppBillingLogic.class.getSimpleName();

    private IabHelper iabHelper;

    public IabHelper getIabHelper() {
        return iabHelper;
    }

    public void setIabHelper(IabHelper iabHelper) {
        this.iabHelper = iabHelper;
    }


    private ArrayList<String> skuDetailsList = new ArrayList<String>();

    public ArrayList<String> getSkuDetailsList() {
        return skuDetailsList;
    }

    public void setSkuDetailsList(ArrayList<String> skuDetailsList) {
        this.skuDetailsList = skuDetailsList;
    }

    private Inventory inventory;

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void consumePurchase(String sku) {
        if (iabHelper != null && inventory != null) {
            iabHelper.consumeAsync(inventory.getPurchase(sku), null);
        }
    }


    public String getPriceForProduct(String inAppCode) {
        String price = "";
        if (inventory != null && inventory.hasDetails(inAppCode)) {
            price = inventory.getSkuDetails(inAppCode).getPrice();
        }

        return price;
    }

    public boolean isThisItemAnInapp(String inAppCode) {
        if (inventory != null && inventory.hasDetails(inAppCode)) {
            return true;
        } else {
            return false;
        }
    }


    private static class InAppBillingHolder {
        public static final InAppBillingLogic instance = new InAppBillingLogic();
    }

    public static InAppBillingLogic getInstance() {
        return InAppBillingHolder.instance;
    }

    public void setupInappBilling(Context context, final IabHelper.QueryInventoryFinishedListener gotInventoryListener) {
        String base64EncodedPublicKey = NetworkConsts.GOOGLE_PLAY_PUBLIC_RSA_KEY;

        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        if (iabHelper == null) {
            iabHelper = new IabHelper(context, base64EncodedPublicKey);
        }

        // enable debug logging (for a production application, you should set this to false).
        if (BuildConfig.DEBUG) {
            iabHelper.enableDebugLogging(false);
        }

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting Setup");
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.d(TAG, "Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (iabHelper == null) return;

                // IAB is fully set up.
                Log.d(TAG, "Setup successful");
                queryInventory(gotInventoryListener);
            }
        });
    }

    public void setupInappBilling(Context context, final IabHelper.OnIabSetupFinishedListener setupFinishedListener) {
        String base64EncodedPublicKey = NetworkConsts.GOOGLE_PLAY_PUBLIC_RSA_KEY;

        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        if (iabHelper == null) {
            iabHelper = new IabHelper(context, base64EncodedPublicKey);
        }

        // enable debug logging (for a production application, you should set this to false).
        if (BuildConfig.DEBUG) {
            iabHelper.enableDebugLogging(false);
        }

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting Setup");
        try {
            iabHelper.startSetup(setupFinishedListener);
        } catch (IllegalStateException ex) {
            Log.d(TAG, "IABHelper is already setup.");
        }
    }

    public void queryInventory(final IabHelper.QueryInventoryFinishedListener gotInventoryListener) {
        // IAB is fully set up. Now, let's get an inventory of stuff we own.
        Log.d(TAG, "Setup successful. Querying inventory.");
        if (iabHelper != null) {
            iabHelper.flagEndAsync();
            iabHelper.queryInventoryAsync(true, skuDetailsList, gotInventoryListener);
        }

    }

    public String getPayloadByMainAccount(Context context) {

        if (context != null) {
            AccountManager accountManager = AccountManager.get(context);
            Account[] accounts = accountManager.getAccountsByType("com.google");
            String payLoad = "";
            if (accounts.length > 0) {
                payLoad = accounts[0].name.toLowerCase();
            }

            return payLoad;
        } else {
            return "";
        }
    }

    private boolean verifyDeveloperPayload(Context context, Purchase p) {

        if (context != null) {
            String developerPayload = p.getDeveloperPayload();

            if (developerPayload.equals(getPayloadByMainAccount(context))) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    public boolean checkPurchasedItemAndResult(Context context, IabResult result, Inventory inventory, String inAppCode) {
        // Have we been disposed of in the meantime? If so, quit.
        if (iabHelper == null) return false;

        // Is it a failure?
        if (result.isFailure()) {
            Log.d(TAG, "Failed to query inventory: " + result);
            return false;
        }

        Log.d(TAG, "Query inventory was successful.");
        this.inventory = inventory;
        /*
        Check for items we own. Notice that for each purchase, we check
        the developer payload to see if it's correct! See
        verifyDeveloperPayload().
         */
        if (inAppCode != null) {
            Purchase itemPurchase = inventory.getPurchase(inAppCode);

            // Do we have the item purchased?
            boolean isPurchased = (itemPurchase != null && verifyDeveloperPayload(context, itemPurchase));
            Log.d(TAG, "User has purchased this item " + (isPurchased ? "YES" : "NO"));

            return isPurchased;
        } else {
            return false;
        }
    }

    public boolean checkPurchasedItem(Context context, String inAppCode) {
        if (inAppCode != null && context != null && inventory != null) {
            Purchase itemPurchase = inventory.getPurchase(inAppCode);

            // Do we have the item purchased?
            boolean isPurchased = (itemPurchase != null && verifyDeveloperPayload(context, itemPurchase));
            Log.d(TAG, "User has purchased this item " + (isPurchased ? "YES" : "NO"));

            return isPurchased;
        } else {
            return false;
        }
    }

    public boolean checkPurchase(Context context, IabResult result, Purchase purchase, String inAppCode) {
        // if we were disposed of in the meantime, quit.
        if (iabHelper == null || inAppCode == null) return false;

        if (result.isFailure()) {
            Log.d(TAG, "Error purchasing: " + result);
            return false;
        }
        if (!verifyDeveloperPayload(context, purchase)) {
            Log.d(TAG, "Error purchasing. Authenticity verification failed.");
            return false;
        }

        Log.d(TAG, "Purchase successful.");

        boolean isPurchased = false;


        if (purchase.getSku().equals(inAppCode)) {
            // Item purchased
            Log.d(TAG, "item purchased. Congratulating user.");
            isPurchased = true;
        }

        return isPurchased;
    }

    /**
     * This method unbounds helper to google play billing services.
     */
    public void disposeHelper() {
        Log.d(TAG, "Destroying helper.");
        if (iabHelper != null) {
            iabHelper.dispose();
            iabHelper = null;
        }
        if (inventory != null) {
            inventory = null;
        }
    }
}
