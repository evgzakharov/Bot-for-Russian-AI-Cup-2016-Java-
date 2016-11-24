public enum WayY {
    TOP(-1), NONE(0), DOWN(1);

    private int diff;

    WayY(int diff) {
        this.diff = diff;
    }

    public int getDiff() {
        return diff;
    }

    public static WayY fromValue(int value) {
        switch (value) {
            case -1:
                return WayY.TOP;
            case 0:
                return WayY.NONE;
            case 1:
                return WayY.DOWN;
        }

        throw new RuntimeException("invalid value " + value);
    }
}
