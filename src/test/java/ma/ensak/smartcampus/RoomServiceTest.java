package ma.ensak.smartcampus;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoomServiceTest {

    private final RoomService service = new RoomService();

    @Test
    void roomCanHostStudentsWithinCapacity() {
        assertTrue(service.canHost(40, 35));
    }

    @Test
    void occupancyRateIsComputedCorrectly() {
        assertEquals(50.0, service.occupancyRate(40, 20));
    }
}
