import com.oocourse.elevator2.PersonRequest;

public class PassengerRequest {
    private int fromFloor;
    private int toFloor;
    private int passengerId;
    private int weight;

    public PassengerRequest(int fromFloor, int toFloor, int passengerId, int weight) {
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;
        this.passengerId = passengerId;
        this.weight = weight;
    }

    public PassengerRequest(PersonRequest personRequest) {
        this.fromFloor = FloorTranslator.translateFloorStrToInt(personRequest.getFromFloor());
        this.toFloor = FloorTranslator.translateFloorStrToInt(personRequest.getToFloor());
        this.passengerId = personRequest.getPersonId();
        this.weight = personRequest.getWeight();
    }

    public int getFromFloor() {
        return fromFloor;
    }

    public int getToFloor() {
        return toFloor;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public int getWeight() {
        return weight;
    }

    public int getDirection() {
        if (fromFloor > toFloor) {
            return -1;
        } else {
            return 1;
        }
    }

    public PassengerRequest deepCopy() {
        return new PassengerRequest(
                this.getFromFloor(),
                this.getToFloor(),
                this.getPassengerId(),
                this.getWeight()
        );
    }
}
