package pepepay.pepepaynative.fragments.walletoverview;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pepepay.pepepaynative.R;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.backend.wallet2.transaction.Transaction;

public class WalletOverview extends Fragment implements Wallets.WalletsListener {

    private WalletOverview.SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;

    public WalletOverview() {
        // Required empty public constructor
    }

    public static WalletOverview newInstance() {
        Bundle args = new Bundle();

        WalletOverview fragment = new WalletOverview();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wallet_overview, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Wallets.addWalletAddListener(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        mSectionsPagerAdapter = new WalletOverview.SectionsPagerAdapter(getActivity().getSupportFragmentManager());

        mViewPager = (ViewPager) getActivity().findViewById(R.id.wallet_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) getActivity().findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        ((ViewGroup) tabLayout.getParent()).removeView(tabLayout);

        AppBarLayout toolbar = (AppBarLayout) getActivity().findViewById(R.id.appbar);
        toolbar.addView(tabLayout);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(tabLayout != null){
            if(tabLayout.getParent() != null){
                ((ViewGroup) tabLayout.getParent()).removeView(tabLayout);
            }
        }
    }

    @Override
    public void privateWalletAdded(Wallet wallet) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSectionsPagerAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void privateWalletGeneratingBegin() {

    }

    @Override
    public void nameChange(String walletID, String newName) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshTabTitles();
            }
        });
    }

    @Override
    public void balanceChange(String walletID, Transaction newTransaction) {

    }

    @Override
    public void walletDeleted(Wallet wallet) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSectionsPagerAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Wallets.removeWalletAddListener(this);
    }

    private void refreshTabTitles() {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setText(mSectionsPagerAdapter.getPageTitle(i));
            }
        }
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return WalletInfoFragment.newInstance(Wallets.getOwnWallet(position));
        }

        @Override
        public int getCount() {
            return Wallets.getOwnWalletsCount();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return Wallets.getName(Wallets.getOwnWalletID(position));
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }
}
