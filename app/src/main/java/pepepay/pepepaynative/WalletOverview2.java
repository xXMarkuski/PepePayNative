package pepepay.pepepaynative;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.Arrays;

import pepepay.pepepaynative.backend.social31.handler.IDeviceConnectionHandler;
import pepepay.pepepaynative.backend.social31.wifiDirect.WifiDirectConnectionHandler;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.fragments.WalletCreateFragment;
import pepepay.pepepaynative.fragments.WalletInfoFragment;

public class WalletOverview2 extends AppCompatActivity implements Wallets.WalletsListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new PepePay(Arrays.<IDeviceConnectionHandler>asList(new WifiDirectConnectionHandler(this))).create(new File(this.getFilesDir(), "godWallets"), new File(this.getFilesDir(), "wallets"), new File(this.getFilesDir(), "private"), new File(this.getFilesDir(), "names"), new File(this.getFilesDir(), "options"), this);

        setContentView(R.layout.activity_wallet_overview2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        Wallets.addWalletAddListener(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    PepePay.CONNECTION_MANAGER.update();
                }
            }
        }).start();

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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshTabTitles();
            }
        });
    }

    @Override
    public void balanceChange(String walletID, float newBalance) {

    }

    @Override
    public void walletDeleted(Wallet wallet) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSectionsPagerAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wallets.saveAll();
        PepePay.OPTIONS.save(PepePay.optionsFile);
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

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }
}
