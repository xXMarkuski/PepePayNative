package pepepay.pepepaynative;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.HandlerThread;
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

import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.backend.wallet2.transaction.Transaction;
import pepepay.pepepaynative.fragments.SettingsFragment;
import pepepay.pepepaynative.fragments.WalletCreateFragment;
import pepepay.pepepaynative.fragments.walletoverview.WalletInfoFragment;

public class WalletOverview2 extends AppCompatActivity implements Wallets.WalletsListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private HandlerThread connThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.LightTheme_NoActionBar);
        super.onCreate(savedInstanceState);

        /*Fabric.with(this, new Crashlytics());

        WifiDirectConnectionHandler wifiDirectConnectionHandler = new WifiDirectConnectionHandler(this);
        PepePay.create(Arrays.<IDeviceConnectionHandler>asList(/*wifiDirectConnectionHandler, new QRConnectionHandler(wifiDirectConnectionHandler, this), new SalutConnectionHandler(this)),
                new File(this.getFilesDir(), "godWallets"),
                new File(this.getFilesDir(), "wallets"),
                new File(this.getFilesDir(), "private"),
                new File(this.getFilesDir(), "names"),
                new File(this.getFilesDir(), "options"),
                new File(this.getFilesDir(), "errols"), this);

        connThread = new HandlerThread("pepepay.connection");
        connThread.start();
        final Handler h = new Handler(connThread.getLooper());

        final Runnable[] connup = new Runnable[1];
        connup[0] = new Runnable() {
            @Override
            public void run() {
                PepePay.CONNECTION_MANAGER.update();
                h.postDelayed((connup[0]), 100);
            }
        };

        h.post(connup[0]);
        h.post(connup[0]);*/


        //Wallets.addWalletAddListener(this);

        setContentView(R.layout.activity_wallet_overview2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        /*if (!PepePay.OPTIONS.get(Options.STANDARD_FORM_CONTRACT, false)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final Dialog[] greeting = new Dialog[]{null};
            greeting[0] = builder.setMessage(R.string.tosGreeting).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PepePay.OPTIONS.set(Options.STANDARD_FORM_CONTRACT, true);

                    PepePay.OPTIONS.set(Options.USER_DEFINED_DEVICE_NAME, "");
                }
            }).setNeutralButton(R.string.showTos, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(WalletOverview2.this);
                    builder.setMessage(FileUtils.readAsset(Options.STANDARD_FORM_CONTRACT)).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            greeting[0].show();
                        }
                    }).setCancelable(false).create().show();
                }
            }).setCancelable(false).create();
            greeting[0].show();
        }*/

        if (savedInstanceState != null) {

        } else {
            if (mSectionsPagerAdapter.getCount() > 1) {
                mViewPager.setCurrentItem(1);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wallet_overview2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final Dialog[] greeting = new Dialog[]{null};
            greeting[0] = builder.setMessage(R.string.aboutText).setPositiveButton(R.string.confirm, null).setNeutralButton(R.string.showTos, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(WalletOverview2.this);
                    /*builder.setMessage(FileUtils.readAsset(Options.STANDARD_FORM_CONTRACT)).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            greeting[0].show();
                        }
                    }).create().show();*/
                }
            }).create();
            greeting[0].show();
            return true;
        } /*else if (id == R.id.action_createQR) {
            startActivity(QRCreatorActivity.class);
        }*/ else if(id == R.id.action_settings){
            getSupportFragmentManager().beginTransaction().replace(android.R.id.content, SettingsFragment.newInstance()).addToBackStack(null).commit();
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
        //Wallets.saveAll();
        //PepePay.ERROL.saveErrols(PepePay.errolFile);
        PepePay.CONNECTION_MANAGER.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PepePay.CONNECTION_MANAGER.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Wallets.removeWalletAddListener(this);
        connThread.quit();
    }

    private void refreshTabTitles() {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setText(mSectionsPagerAdapter.getPageTitle(i));
            }
        }
    }

    public void startActivity(Class<? extends Activity> clazz) {
        Intent myIntent = new Intent(this, clazz);
        this.startActivity(myIntent);
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return WalletCreateFragment.newInstance();
            } else {
                return WalletInfoFragment.newInstance(Wallets.getOwnWallet(position - 1));
            }
        }

        @Override
        public int getCount() {
            int size = Wallets.getOwnWalletsCount();
            return size + 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.createWallet);
            } else {
                return Wallets.getName(Wallets.getOwnWalletID(position - 1));
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }

}
