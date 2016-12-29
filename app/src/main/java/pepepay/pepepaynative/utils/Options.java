package pepepay.pepepaynative.utils;

import pepepay.pepepaynative.R;


public class Options {
    public static final String DEBUG_ENABLED = "pref_debug";
    public static final String USERNAME = "pref_username";
    public static final String THEME = "pref_theme";
    public static final String READ_AGB = "pref_read_agb";
    public static final String DEFAULT_WALLET = "pref_default_wallet";

    public static int getTheme(String pref){
        switch (pref){
            case "dark" : return R.style.DarkTheme_NoActionBar;
            default: return R.style.LightTheme_NoActionBar;
        }
    }
}
