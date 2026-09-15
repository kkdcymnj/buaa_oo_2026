package request;

import com.oocourse.elevator3.UpdateRequest;

public class ElevUpdateRequest {
    private int elevatorId;

    public ElevUpdateRequest(UpdateRequest updateRequest) {
        this.elevatorId = updateRequest.getElevatorId();
    }

    public int getElevatorId() {
        return elevatorId;
    }
}
