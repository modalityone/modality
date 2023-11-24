package one.modality.event.frontoffice.activities.booking.views;

/**
 * @author Bruno Salmon
 */
final class MapPoint {

    private static final double TILE_SIZE = 256;  // Standard tile size for Google Maps
    private final double latitude;
    private final double longitude;
    private final double x;
    private final double y;

    public MapPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        // Mercator's projection (at zoom level 0):
        x =   TILE_SIZE * longitude / 360;
        y = - TILE_SIZE * Math.log(Math.tan(Math.PI / 4 + Math.toRadians(latitude) / 2)) / Math.PI / 2;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

}
