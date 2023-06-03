package common.model;

public enum JobType {
    CHECK_AND_MARK(0),
    INSERT(1),
    DELETE(2);

    private final int value;
    private JobType(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}

