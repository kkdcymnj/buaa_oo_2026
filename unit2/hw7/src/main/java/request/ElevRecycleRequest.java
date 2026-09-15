package request;

import com.oocourse.elevator3.RecycleRequest;

public class ElevRecycleRequest {
    private int elevatorId;

    public ElevRecycleRequest(RecycleRequest recycleRequest) {
        this.elevatorId = recycleRequest.getElevatorId();
    }

    public int getElevatorId() {
        return elevatorId;
    }
}
