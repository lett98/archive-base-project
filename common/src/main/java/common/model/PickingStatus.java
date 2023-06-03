package common.model;

public enum PickingStatus {
    PENDING(0),
    PROCESSING(1),
    FAILURE(2);
    private final int value;
    private PickingStatus(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public static PickingStatus fromInt(int value) {
        for (PickingStatus status : PickingStatus.values()) {
            if (status.value() == value) {
                return status;
            }
        }
        return null;
    }
}

