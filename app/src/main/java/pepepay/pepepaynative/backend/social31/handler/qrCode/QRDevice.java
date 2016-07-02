package pepepay.pepepaynative.backend.social31.handler.qrCode;

import pepepay.pepepaynative.backend.social31.handler.IDevice;

public class QRDevice implements IDevice<QRConnectionHandler> {
    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getName() {
        return "Scan QR-Code";
    }
}
