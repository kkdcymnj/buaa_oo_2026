import com.oocourse.elevator2.MaintRequest;

public class MaintenanceRequest {
    private int elevatorId;
    private int maintainerId;
    private int targetFloor;

    public MaintenanceRequest(int elevatorId, int maintainerId, int targetFloor) {
        this.elevatorId = elevatorId;
        this.maintainerId = maintainerId;
        this.targetFloor = targetFloor;
    }

    public MaintenanceRequest(MaintRequest maintRequest) {
        this.elevatorId = maintRequest.getElevatorId();
        this.maintainerId = maintRequest.getWorkerId();
        this.targetFloor =
                FloorTranslator.translateFloorStrToInt(
                        maintRequest.getToFloor()
                );
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public int getMaintainerId() {
        return maintainerId;
    }

    public int getTargetFloor() {
        return targetFloor;
    }
}
