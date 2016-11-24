public class WayPoint {
    private WayX wayX;
    private WayY wayY;

    public WayPoint(WayX wayX, WayY wayY) {
        this.wayX = wayX;
        this.wayY = wayY;
    }

    public WayX getWayX() {
        return wayX;
    }

    public WayY getWayY() {
        return wayY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WayPoint wayPoint = (WayPoint) o;

        if (wayX != wayPoint.wayX) return false;
        return wayY == wayPoint.wayY;
    }

    @Override
    public int hashCode() {
        int result = wayX != null ? wayX.hashCode() : 0;
        result = 31 * result + (wayY != null ? wayY.hashCode() : 0);
        return result;
    }
}
