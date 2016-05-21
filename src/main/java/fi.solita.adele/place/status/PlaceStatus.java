package fi.solita.adele.place.status;

public class PlaceStatus {
    private long place_id;
    private double latitude;
    private double longitude;
    private boolean occupied;

    public long getPlace_id() {
        return place_id;
    }

    public PlaceStatus setPlace_id(long place_id) {
        this.place_id = place_id;
        return this;
    }

    public double getLatitude() {
        return latitude;
    }

    public PlaceStatus setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public PlaceStatus setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public PlaceStatus setOccupied(boolean occupied) {
        this.occupied = occupied;
        return this;
    }
}
