package pepepay.pepepaynative.backend.social31.wifiDirect;


import android.net.wifi.p2p.WifiP2pDevice;

import pepepay.pepepaynative.backend.social31.handler.IDevice;


public class WifiDirectDevice implements IDevice<WifiDirectConnectionHandler> {

    private WifiP2pDevice wifiP2pDevice;

    public WifiDirectDevice(WifiP2pDevice wifiP2pDevice) {
        this.wifiP2pDevice = wifiP2pDevice;
    }

    public WifiP2pDevice getWifiP2pDevice() {
        return wifiP2pDevice;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String getName() {
        return wifiP2pDevice.deviceName;
    }
}
