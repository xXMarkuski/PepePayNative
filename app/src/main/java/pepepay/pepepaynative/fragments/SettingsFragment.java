package pepepay.pepepaynative.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.ListPreference;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import java.util.ArrayList;

import pepepay.pepepaynative.R;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.utils.Options;

public class SettingsFragment extends PreferenceFragmentCompat {
    public SettingsFragment() {
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        ArrayList<String> ownWallets = Wallets.getOwnWalletIds();
        ListPreference listPreference = (ListPreference) getPreferenceManager().findPreference(Options.DEFAULT_WALLET);
        listPreference.setEntries(Wallets.getNamesFromID(ownWallets).toArray(new String[ownWallets.size()]));
        listPreference.setEntryValues(Wallets.getOwnWalletIds().toArray(new String[ownWallets.size()]));
    }
}
