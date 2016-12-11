package pepepay.pepepaynative.backend.social31.handler.wifiSalut;

import com.peak.salut.SalutDevice;

import pepepay.pepepaynative.backend.social31.handler.IDevice;


public class WifiSalutDevice implements IDevice<SalutConnectionHandler> {

    private final SalutDevice salutDevice;

    WifiSalutDevice(SalutDevice salutDevice) {
        this.salutDevice = salutDevice;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String getName() {
        return salutDevice.readableName;
    }

    public SalutDevice getSalutDevice() {
        return salutDevice;
    }
}
