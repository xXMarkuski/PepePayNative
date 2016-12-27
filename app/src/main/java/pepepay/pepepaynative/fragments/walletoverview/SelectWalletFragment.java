package pepepay.pepepaynative.fragments.walletoverview;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.R;
import pepepay.pepepaynative.backend.social31.connection.Connection;
import pepepay.pepepaynative.backend.social31.packages.Parcel;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.utils.function.Function;
import pepepay.pepepaynative.utils.types.LongUtils;
import pepepay.pepepaynative.utils.types.StringUtils;
import pepepay.pepepaynative.utils.ObjectManager;

public class SelectWalletFragment extends DialogFragment {
    private static final String CONNECTION = "connection";
    private static final String CALLBACK = "callback";

    private Function<Void, Wallet> callback;
    private Connection connection;

    public SelectWalletFragment() {
    }


    public static SelectWalletFragment newInstance(Connection connection, Function<Void, Wallet> callback) {
        SelectWalletFragment fragment = new SelectWalletFragment();
        Bundle args = new Bundle();
        args.putInt(CONNECTION, ObjectManager.add(connection));
        args.putInt(CALLBACK, ObjectManager.add(callback));
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(savedInstanceState != null){
            connection = ObjectManager.getAndRemove(savedInstanceState.getInt(CONNECTION));
            callback = ObjectManager.getAndRemove(savedInstanceState.getInt(CALLBACK));
        } else {
            connection = ObjectManager.getAndRemove(getArguments().getInt(CONNECTION));
            callback = ObjectManager.getAndRemove(getArguments().getInt(CALLBACK));
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final ArrayList<Wallet> wallets = new ArrayList<>();
        final ArrayAdapter<Wallet> adapter = new ArrayAdapter<Wallet>(this.getContext(), android.R.layout.select_dialog_singlechoice, wallets) {
            @Override
            public int getCount() {
                return wallets.size();
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setText(Wallets.getName(wallets.get(position)));
                return view;
            }
        };

        final int[] selectedItem = {0};

        builder.setTitle(R.string.selectWallet).setIcon(android.R.drawable.ic_menu_info_details).setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedItem[0] = which;
            }
        });


        final Parcel parcel = new Parcel(Connection.requestWalletIDs, Connection.REQ, LongUtils.nextLong(Long.MAX_VALUE));
        connection.send(parcel, new Function<Void, String>() {
            @Override
            public Void eval(String s) {
                Object loaded = PepePay.LOADER_MANAGER.load(s);
                if (loaded instanceof ArrayList) {
                    ArrayList<String> array = (ArrayList<String>) loaded;
                    for (String walletID : array) {
                        final Wallet wallet = Wallets.getWallet(walletID);
                        if (wallet == null) {
                            Parcel walletParcel = new Parcel(StringUtils.multiplex(Connection.getWalletNoTransaction, walletID), Connection.REQ, LongUtils.nextLong(Long.MAX_VALUE));
                            connection.send(walletParcel, new Function<Void, String>() {
                                @Override
                                public Void eval(String s) {
                                    Object o = PepePay.LOADER_MANAGER.load(s);
                                    if (o instanceof Wallet) {
                                        final Wallet wallet = (Wallet) o;
                                        Wallets.addWallet(wallet);
                                        PepePay.runOnUIThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Wallets.addWallet(wallet);
                                                handleWallet(wallet);
                                            }
                                        });
                                    }
                                    return null;
                                }
                            });
                        } else {
                            PepePay.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    handleWallet(wallet);
                                }
                            });
                        }
                    }
                }
                return null;
            }

            private void handleWallet(final Wallet wallet) {
                wallets.add(wallet);
                adapter.notifyDataSetChanged();
                if (!Wallets.hasName(wallet)) {
                    connection.send(Parcel.toParcel(StringUtils.multiplex(Connection.getName, wallet.getIdentifier()), Connection.REQ), new Function<Void, String>() {
                        @Override
                        public Void eval(String s) {
                            PepePay.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                            Wallets.addName(wallet, s);
                            return null;
                        }
                    });
                }
            }

        });

        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (adapter.getCount() == 0) {
                    callback.eval(null);
                } else {
                    callback.eval(adapter.getItem(selectedItem[0]));
                }
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder.create();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CONNECTION, ObjectManager.add(connection));
        outState.putInt(CALLBACK, ObjectManager.add(callback));
        super.onSaveInstanceState(outState);
    }
}
