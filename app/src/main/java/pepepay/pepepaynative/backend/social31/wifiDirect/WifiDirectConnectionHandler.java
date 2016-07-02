package pepepay.pepepaynative.backend.social31.wifiDirect;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import pepepay.pepepaynative.backend.social31.ConnectionManager;
import pepepay.pepepaynative.backend.social31.handler.IDevice;
import pepepay.pepepaynative.backend.social31.handler.IDeviceConnectionHandler;
import pepepay.pepepaynative.utils.Function;


public class WifiDirectConnectionHandler implements IDeviceConnectionHandler<WifiDirectConnectionHandler, WifiDirectDevice>, Handler.Callback,
        WifiP2pManager.ConnectionInfoListener {

    public static final String TAG = "wifidirecthandler";

    // TXT RECORD properties
    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_INSTANCE = "_pepepaywallet";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";

    public static final int MESSAGE_READ = 0x400 + 1;
    public static final int MY_HANDLE = 0x400 + 2;
    static final int SERVER_PORT = 4545;
    private final IntentFilter intentFilter = new IntentFilter();
    private final Activity activity;
    private final ArrayList<WifiDirectDevice> availableServices = new ArrayList<>();
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private Handler handler = new Handler(this);
    private ConnectionManager connManager;
    private CommunicationManager comManager;
    private WifiDirectDevice connectedDevice;
    private boolean wifiEnabled = false;

    public WifiDirectConnectionHandler(Activity activity) {
        this.activity = activity;
    }

    public Handler getHandler() {
        return handler;
    }

    @Override
    public boolean canInit() {
        return wifiEnabled;
    }

    @Override
    public void preInit(final ConnectionManager manager) {
        connManager = manager;
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        final WifiManager wifiManager = ((WifiManager) activity.getSystemService(Context.WIFI_SERVICE));
        if (!wifiManager.isWifiEnabled()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    wifiManager.setWifiEnabled(true);
                }
            }).start();
        }

        wifiP2pManager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(activity, activity.getMainLooper(), null);

        disconnect();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(WifiDirectConnectionHandler.TAG, action);

                if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                    // Respond to wifi state changing
                    int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        wifiEnabled = true;
                        Log.i(TAG, "WifiP2P enabled");
                    } else {
                        wifiEnabled = false;
                        Log.i(TAG, "WifiP2P disabled");
                    }
                    Log.i(TAG, String.valueOf(state));
                } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                    //Updating peers with our Service
                    final Collection<WifiP2pDevice>[] deviceList = new Collection[1];
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        deviceList[0] = ((WifiP2pDeviceList) intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST)).getDeviceList();
                    } else {
                        wifiP2pManager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                            @Override
                            public void onPeersAvailable(WifiP2pDeviceList peers) {
                                deviceList[0] = peers.getDeviceList();
                            }
                        });
                    }
                    ArrayList<IDevice> gone = new ArrayList<IDevice>();
                    ArrayList<WifiDirectDevice> newDevices = new ArrayList<WifiDirectDevice>();
                    for (WifiDirectDevice device : availableServices) {
                        if (!deviceList[0].contains(device.getWifiP2pDevice())) {
                            availableServices.remove(device);
                            gone.add(device);
                        }
                    }
                    for (WifiP2pDevice newDevice : deviceList[0]) {
                        boolean isContained = false;
                        for (WifiDirectDevice device : availableServices) {
                            if (device.getWifiP2pDevice().equals(newDevice)) {
                                isContained = true;
                                break;
                            }
                        }
                        if (!isContained) {
                            if (newDevice.primaryDeviceType.startsWith("1")) {
                                //1 is device class "Computer" where tablets fall under
                                //10 is device class "Telephone" where smartphones fall under
                                //so i just check for prefix 1
                                newDevices.add(new WifiDirectDevice(newDevice));
                            }
                        }
                    }
                    availableServices.addAll(newDevices);
                    connManager.devicesChanged(newDevices, gone);
                } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                    // Respond to new connection or disconnections
                    if (wifiP2pManager == null) {
                        return;
                    }

                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                    if (networkInfo.isConnected()) {

                        // we are connected with the other device, request connection
                        // info to find group owner IP
                        Log.d(WifiDirectConnectionHandler.TAG,
                                "Connected to p2p network. Requesting network details");
                        wifiP2pManager.requestConnectionInfo(channel, WifiDirectConnectionHandler.this);
                    } else {
                        // It's a disconnect
                    }

                } else if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                    // Respond to wifi state changing
                    if (intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, 1) == 2) {
                        wifiEnabled = true;
                        Log.i(TAG, "WifiP2P enabled");
                    } else {
                        wifiEnabled = false;
                        Log.i(TAG, "WifiP2P disabled");
                    }
                }
            }
        };

        activity.registerReceiver(receiver, intentFilter);
    }

    @Override
    public void init(ConnectionManager manager) {
        startRegistrationAndDiscovery();
    }

    @Override
    public void onResume() {
        if (receiver != null) {
            activity.registerReceiver(receiver, intentFilter);
        }
    }

    @Override
    public void onPause() {
        activity.unregisterReceiver(receiver);
    }

    private void startRegistrationAndDiscovery() {
        Map<String, String> record = new HashMap<String, String>();
        record.put(TXTRECORD_PROP_AVAILABLE, "visible");

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
        wifiP2pManager.addLocalService(channel, service, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.i(TAG, "Added Local Service");
            }

            @Override
            public void onFailure(int error) {
                Log.i(TAG, "Failed to add a service");
            }
        });

        discoverService();

    }

    private void discoverService() {
        // After attaching listeners, create a service request and initiate
        // discovery.
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        wifiP2pManager.addServiceRequest(channel, serviceRequest,
                new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "Added service discovery request");
                    }

                    @Override
                    public void onFailure(int arg0) {
                        Log.i(TAG, "Failed adding service discovery request");
                    }
                });
        wifiP2pManager.discoverServices(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.i(TAG, "Service discovery initiated");
            }

            @Override
            public void onFailure(int arg0) {
                Log.i(TAG, "Service discovery failed");

            }
        });
    }

    private void connectP2p(final WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        if (serviceRequest != null)
            wifiP2pManager.removeServiceRequest(channel, serviceRequest,
                    new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFailure(int arg0) {

                        }
                    });

        wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.i(TAG, "Connecting to service");
            }

            @Override
            public void onFailure(int errorCode) {
                Log.i(TAG, "Failed connecting to service");
            }
        });
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_READ:
                //byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = (String) msg.obj;
                Log.d(TAG, readMessage);
                connManager.receive(readMessage, connectedDevice);
                break;
            case MY_HANDLE:
                Object obj = msg.obj;
                setCommunicationManager((CommunicationManager) obj);
        }
        return true;
    }


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {
        Thread handler;
        /*
         * The group owner accepts connections using a server socket and then spawns a
         * client socket for every client. This is handled by {@code
         * GroupOwnerSocketHandler}
         */

        if (p2pInfo.groupOwnerAddress == null) return;

        if (p2pInfo.isGroupOwner) {
            Log.d(TAG, "Connected as group owner");
            try {
                handler = new GroupOwnerSocketHandler(
                        this.getHandler(), this);
                handler.start();
            } catch (IOException e) {
                Log.d(TAG,
                        "Failed to create a server thread - " + e.getMessage());
            }
        } else {
            Log.d(TAG, "Connected as peer");
            handler = new ClientSocketHandler(
                    this.getHandler(),
                    p2pInfo.groupOwnerAddress, this);
            handler.start();
        }
        if (connectedDevice == null) {
            connectedDevice = new WifiDirectDevice(new WifiP2pDevice());
            connManager.incomingConnection(connectedDevice, this);
        }
    }


    public void setCommunicationManager(CommunicationManager obj) {
        comManager = obj;
    }

    @Override
    public void requestAvailableDevices(final Function<Void, ArrayList<WifiDirectDevice>> callback) {
        callback.eval(availableServices);
    }

    @Override
    public void connect(WifiDirectDevice target) {
        connectedDevice = target;
        connectP2p(target.getWifiP2pDevice());
    }

    @Override
    public void send(WifiDirectDevice target, String data) {
        if (comManager != null) {
            comManager.write(data);
        } else {
            Log.i(TAG, "comManager is null");
        }
    }

    @Override
    public boolean canSend() {
        return comManager != null;
    }

    @Override
    public Class<WifiDirectDevice> getIDeviceType() {
        return WifiDirectDevice.class;
    }

    public void disconnect() {
        if (wifiP2pManager != null && channel != null) {
            wifiP2pManager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && wifiP2pManager != null && channel != null
                            && group.isGroupOwner()) {
                        wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "removeGroup onSuccess -");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d(TAG, "removeGroup onFailure -" + reason);
                            }
                        });
                    }
                }
            });
        }
    }
}
