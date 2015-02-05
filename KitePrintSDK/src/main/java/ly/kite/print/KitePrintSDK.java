package ly.kite.print;

import android.content.Context;

import java.util.Currency;
import java.util.Locale;

/**
 * Created by deonbotha on 29/01/2014.
 */
public class KitePrintSDK {

    static final String KITE_SHARED_PREFERENCES = "ly.kite.shared_preferences";

    private static final String PAYPAL_CLIENT_ID_SANDBOX = "Aa5nsBDntBpozWQykoxQXoHFOqs551hTNt0B8LQXTudoh8bD0nT1F735c_Fh";
    private static final String PAYPAL_RECIPIENT_SANDBOX = "hello-facilitator@psilov.eu";
    private static final String PAYPAL_CLIENT_ID_LIVE = "AT2JfBAmXD-CHGJnUb05ik4J-GrCi4XxjY9_grfCFjreYaLrNswj8uzhuWyj";
    private static final String PAYPAL_RECIPIENT_LIVE = "deon@oceanlabs.co";

    public static enum Environment {
        LIVE("https://api.kite.ly"),
        TEST("https://api.kite.ly"),
        STAGING("http://staging.api.kite.ly"); /* private environment intended only for Ocean Labs use, hands off :) */

        private final String apiEndpoint;

        private Environment(String apiEndpoint) {
            this.apiEndpoint = apiEndpoint;
        }

        public String getPrintAPIEndpoint() {
            return apiEndpoint;
        }
    }

    private static String apiKey;
    private static Environment environment;

    public static String getUserCurrencyCode() {
        return userCurrencyCode;
    }
    public static String userCurrencyCode;

    private static Context applicationContext;

    public static void initialize(String apiKey, Environment env, Context context) {
        KitePrintSDK.apiKey = apiKey;
        KitePrintSDK.environment = env;
        Locale defaultLocale = Locale.getDefault();
        Currency a = Currency.getInstance(defaultLocale);
        KitePrintSDK.userCurrencyCode = a.getCurrencyCode();
        Template.sync(context.getApplicationContext());
        applicationContext = context.getApplicationContext();
    }

    static Context getApplicationContext() {
        return applicationContext;
    }

    public static String getAPIKey() {
        return apiKey;
    }

    public static Environment getEnvironment() {
        return environment;
    }

}
