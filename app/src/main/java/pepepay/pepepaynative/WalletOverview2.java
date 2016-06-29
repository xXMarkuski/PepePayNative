package pepepay.pepepaynative;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
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

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import pepepay.pepepaynative.backend.social31.handler.IDeviceConnectionHandler;
import pepepay.pepepaynative.backend.social31.wifiDirect.WifiDirectConnectionHandler;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.backend.wallet2.transaction.Transaction;
import pepepay.pepepaynative.fragments.WalletCreateFragment;
import pepepay.pepepaynative.fragments.WalletInfoFragment;

public class WalletOverview2 extends AppCompatActivity implements Wallets.WalletsListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private Thread updateThread;
    private AssetManager assetManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assetManager = getResources().getAssets();

        if (updateThread == null) {
            new PepePay(Arrays.<IDeviceConnectionHandler>asList(new WifiDirectConnectionHandler(this))).create(new File(this.getFilesDir(), "godWallets"), new File(this.getFilesDir(), "wallets"), new File(this.getFilesDir(), "private"), new File(this.getFilesDir(), "names"), new File(this.getFilesDir(), "options"), this);

            updateThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.interrupted()) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        PepePay.CONNECTION_MANAGER.update();
                    }
                }
            });
            updateThread.start();

            Wallets.addWalletAddListener(this);
        }
        setContentView(R.layout.activity_wallet_overview2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        if (!PepePay.OPTIONS.get("agbs", false)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final Dialog[] greeting = new Dialog[]{null};
            greeting[0] = builder.setMessage(R.string.tosGreeting).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PepePay.OPTIONS.set("agbs", true);
                }
            }).setNeutralButton(R.string.showTos, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(WalletOverview2.this);
                    try {
                        InputStream stream = assetManager.open("agbs");
                        builder.setMessage(CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8))).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                greeting[0].show();
                            }
                        }).create().show();
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).create();
            greeting[0].show();
        }

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
    public void balanceChange(String walletID, Transaction newTransaction) {

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
        PepePay.CONNECTION_MANAGER.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PepePay.CONNECTION_MANAGER.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateThread != null) {
            updateThread.interrupt();
        }
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
            int size = Wallets.getOwnWallets().size();
            return size + 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.createWallet);
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
