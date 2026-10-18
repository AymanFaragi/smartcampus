package ma.ensa.smartcampus;

public class RoomService {

    public boolean canHost(int capacity, int students) {
        return students <= capacity;
    }

    public double occupancyRate(int capacity, int students) {
        return (students * 100.0) / capacity;
    }
}
