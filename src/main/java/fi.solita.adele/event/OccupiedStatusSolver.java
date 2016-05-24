package fi.solita.adele.event;

public class OccupiedStatusSolver {

    public static boolean isOccupied(final EventType eventType, final double value) {
        if(eventType == EventType.occupied) {
            return value >= 1.0;
        }
        return false;
        //throw new UnsolvableEventOccupiedStatus(eventType, value);
    }
}
