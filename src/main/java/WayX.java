public enum WayX {
    LEFT(-1), NONE(0), RIGHT(1);

    private int diff;

    WayX(int diff) {
        this.diff = diff;
    }

    public int getDiff() {
        return diff;
    }

    public static WayX fromValue(int value) {
        switch (value) {
            case -1:
                return WayX.LEFT;
            case 0:
                return WayX.NONE;
            case 1:
                return WayX.RIGHT;
        }

        throw new RuntimeException("invalid value " + value);
    }
}
