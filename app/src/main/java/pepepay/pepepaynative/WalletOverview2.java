package pepepay.pepepaynative;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.util.Arrays;

import pepepay.pepepaynative.backend.social31.handler.IDeviceConnectionHandler;
import pepepay.pepepaynative.backend.social31.wifiDirect.WifiDirectConnectionHandler;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.fragments.WalletCreateFragment;
import pepepay.pepepaynative.fragments.WalletInfoFragment;

public class WalletOverview2 extends AppCompatActivity implements WalletCreateFragment.OnFragmentInteractionListener, WalletInfoFragment.OnFragmentInteractionListener, Wallets.WalletsListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new PepePay(Arrays.<IDeviceConnectionHandler>asList(new WifiDirectConnectionHandler(this))).create(new File(this.getFilesDir(), "godWallets"), new File(this.getFilesDir(), "wallets"), new File(this.getFilesDir(), "private"), new File(this.getFilesDir(), "names"), new File(this.getFilesDir(), "options"));

        setContentView(R.layout.activity_wallet_overview2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Wallets.addWalletAddListener(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wallet_overview2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void privateWalletAdded(Wallet wallet) {
        runOnUiThread(new Runnable() {
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

    }

    @Override
    public void balanceChange(String walletID, float newBalance) {

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                return WalletCreateFragment.newInstance();
            } else {
                return WalletInfoFragment.newInstance(position - 1);
            }
        }

        @Override
        public int getCount() {
            return Wallets.getOwnWallets().size() + 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Create Wallet";
            } else {
                return Wallets.getName(Wallets.getOwnWallets().get(position - 1));
            }
        }
    }
}
