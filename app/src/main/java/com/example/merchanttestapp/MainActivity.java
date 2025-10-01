package com.example.merchanttestapp;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.merchanttestapp.managers.SettingsManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import company.tap.google.pay.open.DataConfiguration;
import company.tap.google.pay.open.GooglePayButton;
import company.tap.google.pay.open.SDKDelegate;
import company.tap.google.pay.open.enums.AllowedMethods;
import company.tap.google.pay.open.enums.GooglePayButtonType;
import company.tap.google.pay.open.enums.SDKMode;
import company.tap.gosellapi.GoSellSDK;

import company.tap.gosellapi.internal.api.callbacks.GoSellError;
import company.tap.gosellapi.internal.api.models.Authorize;
import company.tap.gosellapi.internal.api.models.Charge;
import company.tap.gosellapi.internal.api.models.PhoneNumber;
import company.tap.gosellapi.internal.api.models.SaveCard;
import company.tap.gosellapi.internal.api.models.SavedCard;
import company.tap.gosellapi.internal.api.models.Token;
import company.tap.gosellapi.open.buttons.PayButtonView;
import company.tap.gosellapi.open.controllers.SDKSession;
import company.tap.gosellapi.open.controllers.ThemeObject;
import company.tap.gosellapi.open.delegate.SessionDelegate;
import company.tap.gosellapi.open.enums.AppearanceMode;
import company.tap.gosellapi.open.enums.CardType;
import company.tap.gosellapi.open.enums.GPayWalletMode;
import company.tap.gosellapi.open.enums.OperationMode;
import company.tap.gosellapi.open.enums.TransactionMode;
import company.tap.gosellapi.open.models.CardsList;
import company.tap.gosellapi.open.models.Customer;
import company.tap.gosellapi.open.models.PaymentItem;
import company.tap.gosellapi.open.models.Receipt;
import company.tap.gosellapi.open.models.TapCurrency;
import company.tap.gosellapi.open.models.Tax;
import company.tap.gosellapi.open.models.TopUp;
import company.tap.gosellapi.open.models.TopUpApplication;
import company.tap.gosellapi.open.models.TopupPost;
import company.tap.gosellapi.open.viewmodel.CustomerViewModel;
import company.tap.tapbenefitpay.open.BenefitPayDataConfiguration;
import company.tap.tapbenefitpay.open.TapBenefitPayStatusDelegate;
import company.tap.tapbenefitpay.open.web_wrapper.BeneiftPayConfiguration;
import company.tap.tapcardformkit.open.TapCardStatusDelegate;
import company.tap.tapcardformkit.open.web_wrapper.TapCardConfiguration;
import company.tap.tapcheckout_android.CheckoutConfiguration;
import company.tap.tapcheckout_android.TapCheckoutStatusDelegate;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, SessionDelegate, SDKDelegate , TapBenefitPayStatusDelegate, TapCheckoutStatusDelegate {
    Button ButtonStartSDK;
    private final int SDK_REQUEST_CODE = 1001;
    private SDKSession sdkSession;
    private PayButtonView payButtonView;
    private SettingsManager settingsManager;
    private ProgressDialog progress;
    LinearLayout layoutButton;


    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    private static ArrayList<SavedCard> data;
    static View.OnClickListener myOnClickListener;
    private static ArrayList<Integer> removedItems;
    Button paybutn;
    DataConfiguration dataConfiguration = DataConfiguration.INSTANCE;
    GooglePayButton googlePayButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.merchanttestapp.R.layout.activity_main);

        settingsManager = SettingsManager.getInstance();
        settingsManager.setPref(this);
        ButtonStartSDK = findViewById(com.example.merchanttestapp.R.id.button_startSDK);

        googlePayButton = findViewById(R.id.googlePayView); // paybutn = findViewById(com.example.merchanttestapp.R.id.button_startSDK);
        googlePayButton.setGooglePayButtonType(GooglePayButtonType.NORMAL_GOOGLE_PAY);

        ButtonStartSDK.setOnClickListener(this);

        googlePayButton.buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataConfiguration.getGooglePayToken(MainActivity.this,googlePayButton);
            }
        });
        layoutButton = findViewById(com.example.merchanttestapp.R.id.layoutButton);
        // start tap goSellSDK auto
        startSDK();
        initializeSDK();
        configureSDKData();
        configureSdk();
        getDataFromHashMap();
        configureCheckoutSdk(null);
    }

    private void initializeSDK() {
        if (settingsManager != null) {
            String keyTestName = settingsManager.getString("key_test_name", "sk_test_kovrMB0mupFJXfNZWx6Etg5y");
            String packageName = settingsManager.getString("key_package_name", "company.tap.goSellSDKExample");

            if (keyTestName != null && packageName != null) {
                dataConfiguration.initSDK(MainActivity.this, keyTestName, packageName);
            }
        }
    }



    private void configureSDKData() {
        // Pass your activity as a session delegate to listen to SDK internal payment process
        dataConfiguration.addSDKDelegate(this); // Required

        // Set SDK Environment Mode (assuming an enum value)
        dataConfiguration.setEnvironmentMode(SDKMode.ENVIRONMENT_TEST); // Required SDK MODE

        // Set Gateway ID
        dataConfiguration.setGatewayId("tappayments"); // Required GATEWAY ID

        // Set Gateway Merchant ID
        dataConfiguration.setGatewayMerchantID("1124340"); // Required GATEWAY Merchant ID

        // Set Amount
        dataConfiguration.setAmount(new BigDecimal("23.7")); // Required Amount

        // Set Allowed Card Auth Methods
        List<String> allowedAuthMethods = Arrays.asList("PAN_ONLY", "CRYPTOGRAM_3DS"); // Example values
        // For a single allowed method
        dataConfiguration.setAllowedCardAuthMethods(AllowedMethods.ALL); // or PAN_ONLY, or CRYPTOGRAM_3DS
        // Required Auth Methods

        // Set Currency Code
        dataConfiguration.setTransactionCurrency("USD"); // Required Currency

        // Set Country Code
        dataConfiguration.setCountryCode("US"); // Required Country

        // Set Allowed Card Networks
        List<String> allowedNetworks = Arrays.asList("AMEX", "MASTERCARD", "VISA"); // Example networks
        dataConfiguration.setAllowedCardNetworks(allowedNetworks); // Required Payment Networks
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (settingsManager == null) {
            settingsManager = SettingsManager.getInstance();
            settingsManager.setPref(this);
        }
    }

    /**
     * Integrating SDK.
     */
    private void startSDK() {
        /**
         * Required step.
         * Configure SDK with your Secret API key and App Bundle name registered with tap company.
         */
        configureApp();

        /**
         * Optional step
         * Here you can configure your app theme (Look and Feel).
         */
        configureSDKThemeObject();

        /**
         * Required step.
         * Configure SDK Session with all required data.
         */
        configureSDKSession();

        /**
         * Required step.
         * Choose between different SDK modes
         */
        configureSDKMode();

        /**
         * If you included Tap Pay Button then configure it first, if not then ignore this step.
         */
        initPayButton();

    }

    /**
     * Required step.
     * Configure SDK with your Secret API key and App Bundle name registered with tap company.
     */

    private void configureApp() {
        GoSellSDK.init(this, "sk_test_kovrMB0mupFJXfNZWx6Etg5y", "company.tap.goSellSDKExample");   // to be replaced by merchant
       // GoSellSDK.setLocale("ar");  // to be replaced by merchant

    }

    /**
     * Configure SDK Theme
     */
    private void configureSDKThemeObject() {

        ThemeObject.getInstance()
                .setAppearanceMode(AppearanceMode.WINDOWED_MODE)
                .setSdkLanguage("ar")

                .setHeaderFont(Typeface.createFromAsset(getAssets(), "fonts/roboto_light.ttf"))
                .setHeaderTextColor(getResources().getColor(company.tap.gosellapi.R.color.black1))
                .setHeaderTextSize(17)
                .setHeaderBackgroundColor(getResources().getColor(company.tap.gosellapi.R.color.french_gray_new))


                .setCardInputFont(Typeface.createFromAsset(getAssets(), "fonts/roboto_light.ttf"))
                .setCardInputTextColor(getResources().getColor(company.tap.gosellapi.R.color.black))
                .setCardInputInvalidTextColor(getResources().getColor(company.tap.gosellapi.R.color.red))
                .setCardInputPlaceholderTextColor(getResources().getColor(company.tap.gosellapi.R.color.gray))


                .setSaveCardSwitchOffThumbTint(getResources().getColor(company.tap.gosellapi.R.color.french_gray_new))
                .setSaveCardSwitchOnThumbTint(getResources().getColor(company.tap.gosellapi.R.color.vibrant_green))
                .setSaveCardSwitchOffTrackTint(getResources().getColor(company.tap.gosellapi.R.color.french_gray))
                .setSaveCardSwitchOnTrackTint(getResources().getColor(company.tap.gosellapi.R.color.vibrant_green_pressed))

                .setScanIconDrawable(getResources().getDrawable(company.tap.gosellapi.R.drawable.btn_card_scanner_normal))
           //     .setCardScannerIconVisible(true) // **Optional**

                .setPayButtonResourceId(company.tap.gosellapi.R.drawable.btn_pay_selector)  //btn_pay_merchant_selector
                .setPayButtonFont(Typeface.createFromAsset(getAssets(), "fonts/roboto_light.ttf"))

                .setPayButtonDisabledTitleColor(getResources().getColor(company.tap.gosellapi.R.color.white))
                .setPayButtonEnabledTitleColor(getResources().getColor(company.tap.gosellapi.R.color.white))
                .setPayButtonTextSize(14)
                .setPayButtonLoaderVisible(true)
                .setPayButtonSecurityIconVisible(true)

                .setPayButtonText(getResources().getString(company.tap.gosellapi.R.string.pay)) // **Optional**



                // setup dialog textcolor and textsize
                .setDialogTextColor(getResources().getColor(company.tap.gosellapi.R.color.black1))     // **Optional**
                .setDialogTextSize(17)                // **Optional**

        ;

    }


    /**
     * Configure SDK Session
     */
    private void configureSDKSession() {

        // Instantiate SDK Session
        if (sdkSession == null) sdkSession = new SDKSession();   //** Required **

        // pass your activity as a session delegate to listen to SDK internal payment process follow
        sdkSession.addSessionDelegate(this);    //** Required **

        // initiate PaymentDataSource
        sdkSession.instantiatePaymentDataSource();    //** Required **

        // set transaction currency associated to your account
        sdkSession.setTransactionCurrency(new TapCurrency("KWD"));    //** Required **

        // Using static CustomerBuilder method available inside TAP Customer Class you can populate TAP Customer object and pass it to SDK
        sdkSession.setCustomer(getCustomer());    //** Required **

        // Set Total Amount. The Total amount will be recalculated according to provided Taxes and Shipping
        sdkSession.setAmount(new BigDecimal(10));  //** Required **

        // Set Payment Items array list
        sdkSession.setPaymentItems(new ArrayList<>());// ** Optional ** you can pass empty array list
        //  sdkSession.setPaymentItems(settingsManager.getPaymentItems());// ** Optional ** you can pass empty array list


        sdkSession.setPaymentType("ALL");   //** Merchant can pass paymentType

        // Set Taxes array list
        sdkSession.setTaxes(new ArrayList<>());// ** Optional ** you can pass empty array list

        // Set Shipping array list
        sdkSession.setShipping(new ArrayList<>());// ** Optional ** you can pass empty array list

        // Post URL
        sdkSession.setPostURL(""); // ** Optional **

        // Payment Description
        sdkSession.setPaymentDescription(""); //** Optional **

        // Payment Extra Info
        sdkSession.setPaymentMetadata(new HashMap<>());// ** Optional ** you can pass empty array hash map

        // Payment Reference
        sdkSession.setPaymentReference(null); // ** Optional ** you can pass null

        // Payment Statement Descriptor
        sdkSession.setPaymentStatementDescriptor(""); // ** Optional **

        // Enable or Disable Saving Card
        sdkSession.isUserAllowedToSaveCard(true); //  ** Required ** you can pass boolean

        // Enable or Disable 3DSecure
        sdkSession.isRequires3DSecure(true);

        //Set Receipt Settings [SMS - Email ]
        sdkSession.setReceiptSettings(new Receipt(false, false)); // ** Optional ** you can pass Receipt object or null

        // Set Authorize Action
        sdkSession.setAuthorizeAction(null); // ** Optional ** you can pass AuthorizeAction object or null

        sdkSession.setDestination(null); // ** Optional ** you can pass Destinations object or null

        sdkSession.setMerchantID(null); // ** Optional ** you can pass merchant id or null

        sdkSession.setCardType(CardType.ALL); // ** Optional ** you can pass which cardType[CREDIT/DEBIT] you want.By default it loads all available cards for Merchant.

        sdkSession.setOperationMode(OperationMode.SAND_BOX);

      //  sdkSession.setGooglePayWalletMode(GPayWalletMode.ENVIRONMENT_TEST);//** Required ** For setting GooglePAY Environment

        // sdkSession.setTopUp(getTopUp()); // ** Optional ** you can pass TopUp object for Merchant.

        // sdkSession.setDefaultCardHolderName("TEST TAP"); // ** Optional ** you can pass default CardHolderName of the user .So you don't need to type it.
        // sdkSession.isUserAllowedToEnableCardHolderName(false); // ** Optional ** you can enable/ disable  default CardHolderName .

    }


    /**
     * Configure SDK Theme
     */
    private void configureSDKMode() {

        /**
         * You have to choose only one Mode of the following modes:
         * Note:-
         *      - In case of using PayButton, then don't call sdkSession.start(this); because the SDK will start when user clicks the tap pay button.
         */
        //////////////////////////////////////////////////////    SDK with UI //////////////////////
        /**
         * 1- Start using  SDK features through SDK main activity (With Tap CARD FORM)
         */
        startSDKWithUI();

    }


    /**
     * Start using  SDK features through SDK main activity
     */
    private void startSDKWithUI() {
        if (sdkSession != null) {
            TransactionMode trx_mode = (settingsManager != null) ? settingsManager.getTransactionsMode("key_sdk_transaction_mode") : TransactionMode.PURCHASE;
            // set transaction mode [TransactionMode.PURCHASE - TransactionMode.AUTHORIZE_CAPTURE - TransactionMode.SAVE_CARD - TransactionMode.TOKENIZE_CARD ]
            sdkSession.setTransactionMode(TransactionMode.TOKENIZE_CARD);    //** Required **
            // if you are not using tap button then start SDK using the following call
           // sdkSession.start(this);
        }
    }






    /**
     * Include pay button in merchant page
     */
    private void initPayButton() {

        payButtonView = findViewById(R.id.payButtonId);

        if (ThemeObject.getInstance().getPayButtonFont() != null)
            payButtonView.setupFontTypeFace(ThemeObject.getInstance().getPayButtonFont());
        if (ThemeObject.getInstance().getPayButtonEnabledTitleColor() != 0 && ThemeObject.getInstance().getPayButtonDisabledTitleColor() != 0)
            payButtonView.setupTextColor(ThemeObject.getInstance().getPayButtonEnabledTitleColor(),
                    ThemeObject.getInstance().getPayButtonDisabledTitleColor());
//
        if (ThemeObject.getInstance().getPayButtonTextSize() != 0)

            payButtonView.getPayButton().setTextSize(ThemeObject.getInstance().getPayButtonTextSize());
//
        if(ThemeObject.getInstance().isPayButtSecurityIconVisible())

            payButtonView.getSecurityIconView().setVisibility(ThemeObject.getInstance().isPayButtSecurityIconVisible() ? View.VISIBLE : View.INVISIBLE);
        if(ThemeObject.getInstance().getPayButtonResourceId() !=0)
        payButtonView.setBackgroundSelector(ThemeObject.getInstance().getPayButtonResourceId());

        if (sdkSession != null) {
            TransactionMode trx_mode = sdkSession.getTransactionMode();
            if (trx_mode != null) {

                if (TransactionMode.SAVE_CARD == trx_mode ) {
                    payButtonView.getPayButton().setText(getString(company.tap.gosellapi.R.string.save_card));
                } else if (TransactionMode.TOKENIZE_CARD == trx_mode ) {
                    payButtonView.getPayButton().setText(getString(company.tap.gosellapi.R.string.tokenize));
                } else {
                    payButtonView.getPayButton().setText(getString(company.tap.gosellapi.R.string.pay));
                }
            } else {
                configureSDKMode();
            }
            sdkSession.setButtonView(payButtonView, this, SDK_REQUEST_CODE);
        }


    }


    //    //////////////////////////////////////////////////////  List Saved Cards  ////////////////////////

    /**
     * retrieve list of saved cards from the backend.
     */
    private void listSavedCards() {
        if (sdkSession != null)
            sdkSession.listAllCards("cus_s4H13120191115x0R12606480", this);
    }

//    //////////////////////////////////////////////////////  Overridden section : Session Delegation ////////////////////////

    @Override
    public void paymentSucceed(@NonNull Charge charge) {

        System.out.println("Payment Succeeded : charge status : " + charge.getStatus());
        System.out.println("Payment Succeeded : description : " + charge.getDescription());
        System.out.println("Payment Succeeded : message : " + charge.getResponse().getMessage());
        System.out.println("##############################################################################");
        if (charge.getCard() != null) {
            System.out.println("Payment Succeeded : first six : " + charge.getCard().getFirstSix());
            System.out.println("Payment Succeeded : last four: " + charge.getCard().getLast4());
            System.out.println("Payment Succeeded : card object : " + charge.getCard().getObject());
          //  System.out.println("Payment Succeeded : exp month : " + charge.getCard().getExpiry().getMonth());
           // System.out.println("Payment Succeeded : exp year : " + charge.getCard().getExpiry().getYear());
        }

        System.out.println("##############################################################################");
        if (charge.getTopup() != null) {
            System.out.println("Payment Succeeded : topupWalletId : " + charge.getTopup().getWalletId());
            System.out.println("Payment Succeeded : Id : " + charge.getTopup().getId());
            System.out.println("Payment Succeeded : TopUpApp : " + charge.getTopup().getApplication().getAmount());
        }


        System.out.println("##############################################################################");
        if (charge.getAcquirer() != null) {
            System.out.println("Payment Succeeded : acquirer id : " + charge.getAcquirer().getId());
            System.out.println("Payment Succeeded : acquirer response code : " + charge.getAcquirer().getResponse().getCode());
            System.out.println("Payment Succeeded : acquirer response message: " + charge.getAcquirer().getResponse().getMessage());
        }
        System.out.println("##############################################################################");
        if (charge.getSource() != null) {
            System.out.println("Payment Succeeded : source id: " + charge.getSource().getId());
            System.out.println("Payment Succeeded : source channel: " + charge.getSource().getChannel());
            System.out.println("Payment Succeeded : source object: " + charge.getSource().getObject());
            System.out.println("Payment Succeeded : source payment method: " + charge.getSource().getPaymentMethodStringValue());
            System.out.println("Payment Succeeded : source payment type: " + charge.getSource().getPaymentType());
            System.out.println("Payment Succeeded : source type: " + charge.getSource().getType());
        }

        System.out.println("##############################################################################");
        if (charge.getExpiry() != null) {
            System.out.println("Payment Succeeded : expiry type :" + charge.getExpiry().getType());
            System.out.println("Payment Succeeded : expiry period :" + charge.getExpiry().getPeriod());
        }

        saveCustomerRefInSession(charge);
        configureSDKSession();
        showDialog(charge.getId(), charge.getResponse().getMessage(), company.tap.gosellapi.R.drawable.ic_checkmark_normal);
    }

    @Override
    public void paymentFailed(@Nullable Charge charge) {
        System.out.println("Payment Failed : " + charge.getStatus());
        System.out.println("Payment Failed : " + charge.getDescription());
        System.out.println("Payment Failed : " + charge.getResponse().getMessage());


        showDialog(charge.getId(), charge.getResponse().getMessage(), company.tap.gosellapi.R.drawable.icon_failed);
    }

    @Override
    public void authorizationSucceed(@NonNull Authorize authorize) {
        System.out.println("Authorize Succeeded : " + authorize.getStatus());
        System.out.println("Authorize Succeeded : " + authorize.getResponse().getMessage());

        if (authorize.getCard() != null) {
            System.out.println("Payment Authorized Succeeded : first six : " + authorize.getCard().getFirstSix());
            System.out.println("Payment Authorized Succeeded : last four: " + authorize.getCard().getLast4());
            System.out.println("Payment Authorized Succeeded : card object : " + authorize.getCard().getObject());
        }

        System.out.println("##############################################################################");
        if (authorize.getAcquirer() != null) {
            System.out.println("Payment Authorized Succeeded : acquirer id : " + authorize.getAcquirer().getId());
            System.out.println("Payment Authorized Succeeded : acquirer response code : " + authorize.getAcquirer().getResponse().getCode());
            System.out.println("Payment Authorized Succeeded : acquirer response message: " + authorize.getAcquirer().getResponse().getMessage());
        }
        System.out.println("##############################################################################");
        if (authorize.getSource() != null) {
            System.out.println("Payment Authorized Succeeded : source id: " + authorize.getSource().getId());
            System.out.println("Payment Authorized Succeeded : source channel: " + authorize.getSource().getChannel());
            System.out.println("Payment Authorized Succeeded : source object: " + authorize.getSource().getObject());
            System.out.println("Payment Authorized Succeeded : source payment method: " + authorize.getSource().getPaymentMethodStringValue());
            System.out.println("Payment Authorized Succeeded : source payment type: " + authorize.getSource().getPaymentType());
            System.out.println("Payment Authorized Succeeded : source type: " + authorize.getSource().getType());
        }

        System.out.println("##############################################################################");
        if (authorize.getExpiry() != null) {
            System.out.println("Payment Authorized Succeeded : expiry type :" + authorize.getExpiry().getType());
            System.out.println("Payment Authorized Succeeded : expiry period :" + authorize.getExpiry().getPeriod());
        }


        saveCustomerRefInSession(authorize);
        configureSDKSession();
        showDialog(authorize.getId(), authorize.getResponse().getMessage(), company.tap.gosellapi.R.drawable.ic_checkmark_normal);
    }

    @Override
    public void authorizationFailed(Authorize authorize) {
        System.out.println("Authorize Failed : " + authorize.getStatus());
        System.out.println("Authorize Failed : " + authorize.getDescription());
        System.out.println("Authorize Failed : " + authorize.getResponse().getMessage());
        showDialog(authorize.getId(), authorize.getResponse().getMessage(), company.tap.gosellapi.R.drawable.icon_failed);
    }


    @Override
    public void cardSaved(@NonNull Charge charge) {
        // Cast charge object to SaveCard first to get all the Card info.
        if (charge instanceof SaveCard) {
            System.out.println("Card Saved Succeeded : first six digits : " + ((SaveCard) charge).getCard().getFirstSix() + "  last four :" + ((SaveCard) charge).getCard().getLast4());
        }
        System.out.println("Card Saved Succeeded : " + charge.getStatus());
        System.out.println("Card Saved Succeeded : " + charge.getDescription());
        System.out.println("Card Saved Succeeded : " + charge.getResponse().getMessage());
        System.out.println("Card Saved Succeeded : " + ((SaveCard) charge).getCardIssuer().getName());
        System.out.println("Card Saved Succeeded : " + ((SaveCard) charge).getCardIssuer().getId());
        System.out.println("Card Saved Succeeded : " + ((SaveCard) charge).getCardIssuer().getCountry());
        saveCustomerRefInSession(charge);
        showDialog(charge.getId(), charge.getStatus().toString(), company.tap.gosellapi.R.drawable.ic_checkmark_normal);
    }

    @Override
    public void cardSavingFailed(@NonNull Charge charge) {
        System.out.println("Card Saved Failed : " + charge.getStatus());
        System.out.println("Card Saved Failed : " + charge.getDescription());
        System.out.println("Card Saved Failed : " + charge.getResponse().getMessage());
        showDialog(charge.getId(), charge.getStatus().toString(), company.tap.gosellapi.R.drawable.icon_failed);
    }

    @Override
    public void cardTokenizedSuccessfully(@NonNull Token token) {
        System.out.println("Card Tokenized Succeeded : ");
        System.out.println("Token card : " + token.getCard().getFirstSix() + " **** " + token.getCard().getLastFour());
        System.out.println("Token card : " + token.getCard().getFingerprint() + " **** " + token.getCard().getFunding());
        System.out.println("Token card : " + token.getCard().getId() + " ****** " + token.getCard().getName());
        System.out.println("Token card : " + token.getCard().getAddress() + " ****** " + token.getCard().getObject());
        System.out.println("Token card : " + token.getCard().getExpirationMonth() + " ****** " + token.getCard().getExpirationYear());

        showDialog(token.getId(), "Token", company.tap.gosellapi.R.drawable.ic_checkmark_normal);
    }

    @Override
    public void savedCardsList(@NonNull CardsList cardsList) {
        System.out.println(" Card List Response Code : " + cardsList.getResponseCode());
        System.out.println(" Card List Top 10 : " + cardsList.getCards().size());
        System.out.println(" Card List Has More : " + cardsList.isHas_more());

        showSavedCardsDialog(cardsList);
    }


    @Override
    public void sdkError(@Nullable GoSellError goSellError) {
        if (progress != null)
            progress.dismiss();
        if (goSellError != null) {
            System.out.println("SDK Process Error : " + goSellError.getErrorBody());
            System.out.println("SDK Process Error : " + goSellError.getErrorMessage());
            System.out.println("SDK Process Error : " + goSellError.getErrorCode());
            showDialog(goSellError.getErrorCode() + "", goSellError.getErrorMessage(), company.tap.gosellapi.R.drawable.icon_failed);
        }
    }


    @Override
    public void sessionIsStarting() {
        System.out.println(" Session Is Starting.....");
    }

    @Override
    public void sessionHasStarted() {
        System.out.println(" Session Has Started .......");
    }


    @Override
    public void sessionCancelled() {
        Log.d("MainActivity", "Session Cancelled.........");
    }

    @Override
    public void sessionFailedToStart() {
        Log.d("MainActivity", "Session Failed to start.........");
    }


    @Override
    public void invalidCardDetails() {
        System.out.println(" Card details are invalid....");
    }

    @Override
    public void backendUnknownError(String message) {
        System.out.println("Backend Un-Known error.... : " + message);
    }

    @Override
    public void invalidTransactionMode() {
        System.out.println(" invalidTransactionMode  ......");
    }

    @Override
    public void invalidCustomerID() {
        if (progress != null)
            progress.dismiss();
        System.out.println("Invalid Customer ID .......");

    }

    @Override
    public void userEnabledSaveCardOption(boolean saveCardEnabled) {
        System.out.println("userEnabledSaveCardOption :  " + saveCardEnabled);
    }

    @Override
    public void cardTokenizedSuccessfully(@NonNull Token token, boolean saveCardEnabled) {
        System.out.println("Card Tokenized Succeeded : ");
        System.out.println("saveCardEnabled  : "+saveCardEnabled);
        System.out.println("Token card : " + token.getCard().getFirstSix() + " **** " + token.getCard().getLastFour());
        System.out.println("Token card : " + token.getCard().getFingerprint() + " **** " + token.getCard().getFunding());
        System.out.println("Token card : " + token.getCard().getId() + " ****** " + token.getCard().getName());
        System.out.println("Token card : " + token.getCard().getAddress() + " ****** " + token.getCard().getObject());
        System.out.println("Token card : " + token.getCard().getExpirationMonth() + " ****** " + token.getCard().getExpirationYear());

        showDialog(token.getId(), "Token", company.tap.gosellapi.R.drawable.ic_checkmark_normal);
    }

    @Override
    public void asyncPaymentStarted(@NonNull Charge charge) {
        System.out.println("asyncPaymentStarted Succeeded : charge status : " + charge.getStatus());
        System.out.println("asyncPaymentStarted Succeeded : description : " + charge.getDescription());
        System.out.println("asyncPaymentStarted Succeeded : message : " + charge.getResponse().getMessage());
        System.out.println("##############################################################################");
        if (charge.getCard() != null) {
            System.out.println("Payment Succeeded : first six : " + charge.getCard().getFirstSix());
            System.out.println("Payment Succeeded : last four: " + charge.getCard().getLast4());
            System.out.println("Payment Succeeded : card object : " + charge.getCard().getObject());
            //  System.out.println("Payment Succeeded : exp month : " + charge.getCard().getExpiry().getMonth());
            // System.out.println("Payment Succeeded : exp year : " + charge.getCard().getExpiry().getYear());
        }
    }

    @Override
    public void paymentInitiated(@Nullable Charge charge) {

    }

    @Override
    public void googlePayFailed(String error) {

    }


/////////////////////////////////////////////////////////  needed only for demo ////////////////////


    public void showSavedCardsDialog(CardsList cardsList) {
        if (progress != null)
            progress.dismiss();

        if (cardsList != null && cardsList.getCards() != null && cardsList.getCards().size() == 0) {
            Toast.makeText(this, "There is no card saved for this customer", Toast.LENGTH_LONG).show();
            return;
        }

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
//        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        data = new ArrayList<SavedCard>();

        removedItems = new ArrayList<Integer>();

        adapter = new CustomAdapter(cardsList.getCards());
        recyclerView.setAdapter(adapter);


    }


    private Customer getCustomer() { // test customer id cus_Kh1b4220191939i1KP2506448
       // cus_s4H13120191115x0R12606480

        Customer customer = (settingsManager != null) ? settingsManager.getCustomer() : null;

        PhoneNumber phoneNumber = customer != null ? customer.getPhone() : new PhoneNumber("965", "69045932");

        return new Customer.CustomerBuilder("").email("abc@abc.com").firstName("firstname")
                .lastName("lastname").metadata("").phone(new PhoneNumber(phoneNumber.getCountryCode(), phoneNumber.getNumber()))
                .middleName("middlename").build();


    }

    private void showDialog(String chargeID, String msg, int icon) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        PopupWindow popupWindow;
        try {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {

                View layout = inflater.inflate(company.tap.gosellapi.R.layout.charge_status_layout, findViewById(
                        company.tap.gosellapi.R.id.popup_element));

                popupWindow = new PopupWindow(layout, width, 250, true);

                ImageView status_icon = layout.findViewById(company.tap.gosellapi.R.id.status_icon);
                TextView statusText = layout.findViewById(company.tap.gosellapi.R.id.status_text);
                TextView chargeText = layout.findViewById(company.tap.gosellapi.R.id.charge_id_txt);
                status_icon.setImageResource(icon);
//                status_icon.setVisibility(View.INVISIBLE);
                chargeText.setText(chargeID);
                statusText.setText((msg != null && msg.length() > 30) ? msg.substring(0, 29) : msg);


                popupWindow.showAtLocation(layout, Gravity.TOP, 0, 50);
                popupWindow.getContentView().startAnimation(AnimationUtils.loadAnimation(this, company.tap.gosellapi.R.anim.popup_show));

                setupTimer(popupWindow);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTimer(PopupWindow popupWindow) {
        // Hide after some seconds
        final Handler handler = new Handler();
        final Runnable runnable = () -> {
            if (popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
        };

        popupWindow.setOnDismissListener(() -> handler.removeCallbacks(runnable));

        handler.postDelayed(runnable, 4000);
    }

    private void saveCustomerRefInSession(Charge charge) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Gson gson = new Gson();

        String response = preferences.getString("customer", "");


        ArrayList<CustomerViewModel> customersList = gson.fromJson(response,
                new TypeToken<List<CustomerViewModel>>() {
                }.getType());

        if (customersList != null) {
            customersList.clear();
            customersList.add(new CustomerViewModel(
                    charge.getCustomer().getIdentifier(),
                    charge.getCustomer().getFirstName(),
                    charge.getCustomer().getMiddleName(),
                    charge.getCustomer().getLastName(),
                    charge.getCustomer().getEmail(),
                    charge.getCustomer().getPhone().getCountryCode(),
                    charge.getCustomer().getPhone().getNumber()));

            String data = gson.toJson(customersList);

            writeCustomersToPreferences(data, preferences);
        }
    }


    private void writeCustomersToPreferences(String data, SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("customer", data);
        editor.commit();
    }


    public void getCustomerSavedCardsList(View view) {
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.show();
        listSavedCards();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.button_startSDK){
           // payButtonView.setVisibility(View.VISIBLE);
            ButtonStartSDK.setVisibility(View.GONE);
            layoutButton.setVisibility(View.VISIBLE);
            startSDK();
        }

    }

    public void sesssioncancel(View view) {
       // sdkSession.cancelSession(this);
    }

    @Override
    public void onFailed(@NonNull String s) {

    }

    @Override
    public void onGooglePayToken(@NonNull String s) {
        showDialog("onGooglePayToken",s, company.tap.gosellapi.R.drawable.ic_checkmark_normal);

    }

    @Override
    public void onTapToken(@NonNull company.tap.google.pay.internal.api.responses.Token token) {
        showDialog("onGooglePayToken",token.getId(), company.tap.gosellapi.R.drawable.ic_checkmark_normal);

    }

    @Override
    public void onBenefitPaySuccess(@NonNull String s) {

    }

    @Override
    public void onBenefitPayReady() {

    }

    @Override
    public void onBenefitPayClick() {

    }

    @Override
    public void onBenefitPayOrderCreated(@NonNull String s) {

    }

    @Override
    public void onBenefitPayChargeCreated(@NonNull String s) {

    }

    @Override
    public void onBenefitPayError(@NonNull String s) {

    }

    @Override
    public void onBenefitPayCancel() {

    }

    @Override
    public void onCheckoutSuccess(@NonNull String s) {

    }

    @Override
    public void onCheckoutReady() {

    }

    @Override
    public void onCheckoutClick() {

    }

    @Override
    public void onCheckoutOrderCreated(@NonNull String s) {

    }

    @Override
    public void onCheckoutChargeCreated(@NonNull String s) {

    }

    @Override
    public void onCheckoutError(@NonNull String s) {

    }

    @Override
    public void onCheckoutcancel() {

    }


    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

        private ArrayList<SavedCard> dataSet;

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView textViewName;
            TextView textViewVersion;
            TextView textViewexp;
            ImageView imageViewIcon;

            public MyViewHolder(View itemView) {
                super(itemView);
                this.textViewName = (TextView) itemView.findViewById(R.id.textViewName);
                this.textViewVersion = (TextView) itemView.findViewById(R.id.textViewVersion);
                this.textViewexp = (TextView) itemView.findViewById(R.id.textViewexp);
                this.imageViewIcon = (ImageView) itemView.findViewById(R.id.imageView);
            }
        }

        public CustomAdapter(ArrayList<SavedCard> data) {
            this.dataSet = data;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cards_layout, parent, false);

            // view.setOnClickListener(MainActivity.myOnClickListener);

            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

            TextView textViewName = holder.textViewName;
            TextView textViewVersion = holder.textViewVersion;
            TextView textViewexp = holder.textViewexp;
            ImageView imageView = holder.imageViewIcon;

            textViewName.setText(dataSet.get(listPosition).getFirstSix() + " ***** " + dataSet.get(listPosition).getLastFour());
            textViewVersion.setText((dataSet.get(listPosition)).getExp_month() + " / " + (dataSet.get(listPosition)).getExp_year());
            imageView.setImageResource(R.drawable.cards1);
        }

        @Override
        public int getItemCount() {
            return dataSet.size();
        }
    }
    //Set topup object
    private TopUp getTopUp() {
        TopUp topUp = new TopUp(
                null,
                "wal_xXTwK5211326gmgS16SV53834",
                null,
                null,
                BigDecimal.valueOf(20),
                "kwd",
                null,null,null,new TopUpApplication((BigDecimal.valueOf(30)),"kwd"),null,new TopupPost("wwww.google.com"),null);
        return topUp;


    }
    private void configureSdk() {

        // ======================
        // Operator
        // ======================
        HashMap<String, Object> operator = new HashMap<>();
        operator.put("publicKey", "pk_test_YhUjg9PNT8oDlKJ1aE2fMRz7");   // hardcoded
        operator.put("hashString", ""); // hardcoded
        operator.put("scopeKey", "charge");  // hardcoded

        Log.e("orderData", "publicKey=pk_test_123456 \nhash=hash_test_123456");

        // ======================
        // Metadata
        // ======================
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("id", "");

        // ======================
        // Order
        // ======================
        String orderId = "ORD_001";
        String orderDescription = "Sample order description";
        String orderAmount = "1";
        String orderReference = "REF_123456";
        String selectedCurrency = "BHD";

        HashMap<String, Object> order = new HashMap<>();
        order.put("id", orderId);
        order.put("amount", orderAmount);
        order.put("currency", selectedCurrency);
        order.put("description", orderDescription);
        order.put("reference", orderReference);
        order.put("metadata", metadata);

        Log.e("orderData", "id=" + orderId +
                "\ndesc=" + orderDescription +
                "\namount=" + orderAmount +
                "\nref=" + orderReference +
                "\ncurrency=" + selectedCurrency);

        // ======================
        // Merchant
        // ======================
        HashMap<String, Object> merchant = new HashMap<>();
        merchant.put("id", "");

        // ======================
        // Invoice
        // ======================
        HashMap<String, Object> invoice = new HashMap<>();
        invoice.put("id", "");

        // ======================
        // Phone
        // ======================
        HashMap<String, Object> phone = new HashMap<>();
        phone.put("countryCode", "965");
        phone.put("number", "6617090");

        // ======================
        // Contact
        // ======================
        HashMap<String, Object> contact = new HashMap<>();
        contact.put("email", "email@emailc.com");
        contact.put("phone", phone);

        // ======================
        // Interface
        // ======================
        String selectedLanguage = "en";
        String selectedCardEdge = "circular";
        String paymentMethod = "benefitpay";

        Log.e("interfaceData", "language=" + selectedLanguage + " cardedge=" + selectedCardEdge);

        HashMap<String, Object> interfacee = new HashMap<>();
        interfacee.put("locale", selectedLanguage);
        interfacee.put("edges", selectedCardEdge);
        interfacee.put("theme", "dynamic");

        // ======================
        // Post
        // ======================
        HashMap<String, Object> post = new HashMap<>();
        post.put("url", "");

        // ======================
        // Transaction
        // ======================
        HashMap<String, Object> transaction = new HashMap<>();
        transaction.put("amount", orderAmount);
        transaction.put("currency", selectedCurrency);
        transaction.put("autoDismiss", false); // hardcoded false

        Log.e("transaction", "amount=" + orderAmount + " currency=" + selectedCurrency);

        // ======================
        // Reference
        // ======================
        HashMap<String, Object> reference = new HashMap<>();
        reference.put("transaction", orderReference);
        reference.put("order", orderDescription);

        // ======================
        // Name
        // ======================
        HashMap<String, Object> name = new HashMap<>();
        name.put("lang", selectedLanguage);
        name.put("first", "TAP");
        name.put("middle", "middle");
        name.put("last", "PAYMENTS");

        // ======================
        // Customer
        // ======================
        HashMap<String, Object> customer = new HashMap<>();
        customer.put("id", "");
        customer.put("contact", contact);
        customer.put("names", java.util.Collections.singletonList(name));

        // ======================
        // Configuration
        // ======================
        LinkedHashMap<String, Object> configuration = new LinkedHashMap<>();
        configuration.put("paymentMethod", paymentMethod);
        configuration.put("merchant", merchant);
        configuration.put("scope", "scope_test_123456");
        configuration.put("redirect", "tapredirectionwebsdk://");
        configuration.put("customer", customer);
        configuration.put("interface", interfacee);
        configuration.put("reference", reference);
        configuration.put("metadata", "");
        configuration.put("post", post);
        configuration.put("transaction", transaction);
        configuration.put("operator", operator);

        // ======================
        // Call SDK Configurator
        // ======================
        BeneiftPayConfiguration.Companion.configureWithTapBenfitPayDictionaryConfiguration(
                this,
                findViewById(R.id.benfit_pay),
                configuration,
                this
        );
    }

    private void getDataFromHashMap() {

        // Hardcoded values
        String selectedLanguage = "en";
        String selectedCurrency = "KWD";
        String selectedTheme = "light";
        String custId = "";
        String cardNameKey = "John Doe";
        String cardNumber = "4111111111111111";
        String cardExpiry = "12/30";
        String selectedCardEdge = "rounded";
        boolean showCardBrands = true;
        boolean showHideScanner = true;
        boolean showHideNFC = true;
        String amount = "1";
        ArrayList<String> cardBrands = new ArrayList<>(Arrays.asList("Visa", "MasterCard"));
        ArrayList<String> cardFundSources = new ArrayList<>(Arrays.asList("Credit", "Debit"));
        String scopeType = "charge";
        boolean powerdBy = true;
        boolean showLoadingState = true;
        String sandboxKey = "pk_test_YhUjg9PNT8oDlKJ1aE2fMRz7";
        String merchantIdKey = "1124340";
        String selectedCardDirection = "horizontal";
        String ordrId = "order_001";
        String orderDescription = "Test Order";
        String transactionRefrence = "txn_001";
        String postUrl = "https://example.com/post";
        String invoiceId = "invoice_001";
        String purpose = "purchase";
        boolean saveCard = true;
        boolean autoSaveCard = true;
        String redirectURL = "https://example.com/redirect";
        String selectedColorStyle = "blue";
        boolean cardHolder = true;
        boolean cvv = true;

        // Operator
        HashMap<String, Object> operator = new HashMap<>();
        operator.put("publicKey", sandboxKey);

        // Scope
        HashMap<String, Object> scope = new HashMap<>();
        scope.put("scope", scopeType);

        // Merchant
        HashMap<String, Object> merchant = new HashMap<>();
        merchant.put("id", merchantIdKey);

        // Invoice
        HashMap<String, Object> invoice = new HashMap<>();
        invoice.put("id", invoiceId);

        // Post
        HashMap<String, Object> post = new HashMap<>();
        post.put("url", postUrl);

        // Redirect
        HashMap<String, Object> redirect = new HashMap<>();
        redirect.put("url", redirectURL);

        // Metadata
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("id", "");

        // Contract
        HashMap<String, Object> contract = new HashMap<>();
        contract.put("id", "");

        // PaymentAgreement
        HashMap<String, Object> paymentAgreement = new HashMap<>();
        paymentAgreement.put("id", "");
        paymentAgreement.put("contract", contract);

        // Transaction
        HashMap<String, Object> transaction = new HashMap<>();
        transaction.put("paymentAgreement", paymentAgreement);
        transaction.put("metadata", metadata);

        // Phone
        HashMap<String, Object> phone = new HashMap<>();
        phone.put("countryCode", "+20");
        phone.put("number", "011");

        // Contact
        HashMap<String, Object> contact = new HashMap<>();
        contact.put("email", "test@gmail.com");
        contact.put("phone", phone);

        // Name
        HashMap<String, Object> name = new HashMap<>();
        name.put("lang", selectedLanguage);
        name.put("first", "first");
        name.put("middle", "middle");
        name.put("last", "last");

        // Customer
        HashMap<String, Object> customer = new HashMap<>();
        customer.put("nameOnCard", cardNameKey);
        customer.put("editable", cardHolder);
        customer.put("contact", contact);
        customer.put("name", Collections.singletonList(name));

        // Acceptance
        HashMap<String, Object> acceptance = new HashMap<>();
        acceptance.put("supportedSchemes", cardBrands);
        acceptance.put("supportedFundSource", cardFundSources);
        acceptance.put("supportedPaymentAuthentications", Collections.singletonList("3DS"));

        // Field visibility
        HashMap<String, Object> card = new HashMap<>();
        card.put("cvv", cvv);
        card.put("cardHolder", cardHolder);
        HashMap<String, Object> fieldVisibility = new HashMap<>();
        fieldVisibility.put("card", card);

        // Customer cards
        HashMap<String, Object> customerCards = new HashMap<>();
        customerCards.put("saveCard", saveCard);
        customerCards.put("autoSaveCard", autoSaveCard);

        // Alternative card input
        HashMap<String, Object> alternativeCardInput = new HashMap<>();
        alternativeCardInput.put("cardScanner", showHideScanner);
        alternativeCardInput.put("cardNFC", showHideNFC);

        // Features
        HashMap<String, Object> features = new HashMap<>();
        features.put("acceptanceBadge", showCardBrands);
        features.put("customerCards", customerCards);
        features.put("alternativeCardInputs", alternativeCardInput);

        // Order
        HashMap<String, Object> order = new HashMap<>();
        order.put("id", ordrId);
        order.put("amount", amount);
        order.put("currency", selectedCurrency);
        order.put("description", orderDescription);
        order.put("reference", transactionRefrence);

        // Interface
        HashMap<String, Object> interfacee = new HashMap<>();
        interfacee.put("locale", selectedLanguage);
        interfacee.put("theme", selectedTheme);
        interfacee.put("edges", selectedCardEdge);
        interfacee.put("cardDirection", selectedCardDirection);
        interfacee.put("powered", powerdBy);
        interfacee.put("colorStyle", selectedColorStyle);
        interfacee.put("loader", showLoadingState);

        // Final configuration
        LinkedHashMap<String, Object> configuration = new LinkedHashMap<>();
        configuration.put("operator", operator);
        configuration.put("scope", scopeType);
        configuration.put("order", order);
        configuration.put("customer", customer);
        configuration.put("purpose", purpose);
        configuration.put("transaction", transaction);
        configuration.put("invoice", invoice);
        configuration.put("merchant", merchant);
        configuration.put("features", features);
        configuration.put("acceptance", acceptance);
        configuration.put("fieldVisibility", fieldVisibility);
        configuration.put("interface", interfacee);
        configuration.put("redirect", redirect);
        configuration.put("post", post);

        System.out.println("configuration here: " + configuration);

        // Call TapCardConfiguration (example)
        TapCardConfiguration.Companion.configureWithTapCardDictionaryConfiguration(
                this,
                findViewById(R.id.tapCardForm),
                configuration,
                new TapCardStatusDelegate() {
                    @Override
                    public void onHeightChange(@NonNull String s) {

                    }

                    @Override
                    public void onCardFocus() {

                    }

                    @Override
                    public void onCardSuccess(String data) {
                        Toast.makeText(MainActivity.this, "onSuccess " + data, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCardReady() {
                       // findViewById(R.id.tokenizeBtn).setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onBindIdentification(String data) { }

                    @Override
                    public void onValidInput(String isValid) { }

                    @Override
                    public void onChangeSaveCard(boolean enabled) { }

                    @Override
                    public void onCardError(String error) {
                        Toast.makeText(MainActivity.this, "onError " + error, Toast.LENGTH_SHORT).show();
                    }
                },
                cardNumber,
                cardExpiry

        );
    }

    public void configureCheckoutSdk(String intentId) {
        try {
            // Hardcoded public key
            String publicKey = "pk_test_ohzQrUWRnTkCLD1cqMeudyjX";

            // Intent object
            HashMap<String, Object> intentObj = new HashMap<>();
            if (intentId != null) {
                intentObj.put("intent", intentId);
            }

            // Main configuration
            LinkedHashMap<String, Object> configuration = new LinkedHashMap<>();
            configuration.put("open", true);
            configuration.put("hashString", "");
            configuration.put("checkoutMode", "page");
            configuration.put("language", "en");
            configuration.put("themeMode", "dark");

            // Supported payment methods
            JSONArray jsonArrayPaymentMethod = new JSONArray();
            jsonArrayPaymentMethod.put("CARD");
           // jsonArrayPaymentMethod.put("WEB");
           // jsonArrayPaymentMethod.put("APPLE_PAY");
            configuration.put("supportedPaymentMethods", "ALL");

            configuration.put("paymentType", "ALL");
            configuration.put("selectedCurrency", "KWD");
            configuration.put("supportedCurrencies", "ALL");

            // Gateway
            JSONObject gateway = new JSONObject();
            gateway.put("publicKey", "pk_test_gznOhsfdL0QMV8AW7tSN2wKP");
            gateway.put("merchantId", ""); // Hardcoded empty
            configuration.put("gateway", gateway);

            // Customer
            JSONObject customer = new JSONObject();
            customer.put("firstName", "First Android");
            customer.put("lastName", "Test");
            customer.put("email", "example@gmail.com");

            JSONObject phone = new JSONObject();
            phone.put("countryCode", "965");
            phone.put("number", "55567890");
            customer.put("phone", phone);

            configuration.put("customer", customer);

            // Transaction
            JSONObject transaction = new JSONObject();
            transaction.put("mode", "charge");

            JSONObject charge = new JSONObject();
            charge.put("saveCard", true);

            JSONObject auto = new JSONObject();
            auto.put("type", "VOID");
            auto.put("time", 100);
            charge.put("auto", auto);

            JSONObject redirect = new JSONObject();
            redirect.put("url", "https://demo.staging.tap.company/v2/sdk/checkout");
            charge.put("redirect", redirect);

            charge.put("threeDSecure", true);

            JSONObject subscription = new JSONObject();
            subscription.put("type", "SCHEDULED");
            subscription.put("amount_variability", "FIXED");
            subscription.put("txn_count", 0);
            charge.put("subscription", subscription);

            JSONObject airline = new JSONObject();
            JSONObject reference = new JSONObject();
            reference.put("booking", "");
            airline.put("reference", reference);
            charge.put("airline", airline);

            transaction.put("charge", charge);
            configuration.put("transaction", transaction);

            // Amount
            configuration.put("amount", "1");

            // Order
            JSONObject order = new JSONObject();
            order.put("id", "");
            order.put("currency", "KWD");
            order.put("amount", "1");

            JSONArray items = new JSONArray();
            JSONObject item = new JSONObject();
            item.put("amount", "1");
            item.put("currency", "KWD");
            item.put("name", "Item Title 1");
            item.put("quantity", 1);
            item.put("description", "item description 1");
            items.put(item);

            order.put("items", items);
            configuration.put("order", order);

            // Card options
            JSONObject cardOptions = new JSONObject();
            cardOptions.put("showBrands", true);
            cardOptions.put("showLoadingState", false);
            cardOptions.put("collectHolderName", true);
            cardOptions.put("preLoadCardName", "");
            cardOptions.put("cardNameEditable", true);
            cardOptions.put("cardFundingSource", "all");
            cardOptions.put("saveCardOption", "all");
            cardOptions.put("forceLtr", false);

            JSONObject alternativeCardInputs = new JSONObject();
            alternativeCardInputs.put("cardScanner", true);
            alternativeCardInputs.put("cardNFC", true);
            cardOptions.put("alternativeCardInputs", alternativeCardInputs);

            configuration.put("cardOptions", cardOptions);
            configuration.put("isApplePayAvailableOnClient", true);

            // Call SDK if no intentId
            if (intentId == null) {
                CheckoutConfiguration.Companion.configureWithTapCheckoutDictionary(
                        this,
                        publicKey,
                        findViewById(R.id.checkout_pay),
                        configuration,
                        this
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
