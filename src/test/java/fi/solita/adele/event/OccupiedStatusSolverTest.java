package fi.solita.adele.event;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OccupiedStatusSolverTest {

    @Test
    public void should_solve_occupied_type_with_value_1_to_true() {
        assertTrue(OccupiedStatusSolver.isOccupied(EventType.occupied, 1.0));
    }

    @Test
    public void should_solve_occupied_type_with_value_more_1_to_true() {
        assertTrue(OccupiedStatusSolver.isOccupied(EventType.occupied, 1.1));
    }

    @Test
    public void should_solve_occupied_type_with_value_less_than_1_to_false() {
        assertFalse(OccupiedStatusSolver.isOccupied(EventType.occupied, 0.9));
    }
}
