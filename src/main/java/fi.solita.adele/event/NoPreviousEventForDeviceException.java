package fi.solita.adele.event;

public class NoPreviousEventForDeviceException extends RuntimeException {

    public NoPreviousEventForDeviceException(int deviceId) {
        super("No previous event for device " + deviceId);
    }
}
