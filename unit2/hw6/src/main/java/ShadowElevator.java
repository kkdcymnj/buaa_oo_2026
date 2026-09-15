import java.util.ArrayList;

public class ShadowElevator {
    private int elevatorId;
    private ArrayList<PassengerRequest> passengerRequestsInElevator;
    private WaitingTable waitingTable;  // 还在等待这一部电梯的乘客
    private int direction;
    private int currentFloor;
    private WeightHolder weightHolder;

    private int capacity;
    private int openTime;
    private int closeTime;
    private int moveTime;

    private Strategy strategy;

    private long totalTime;

    private volatile long departureTime;

    public ShadowElevator(Elevator elevator) {
        elevatorId = elevator.getElevatorId();
        waitingTable = elevator.getWaitingTable().deepCopy();
        direction = elevator.getDirection();
        currentFloor = elevator.getCurrentFloor();
        weightHolder = elevator.getWeightHolder().deepCopy();
        capacity = elevator.getCapacity();
        openTime = elevator.getOpenTime();
        closeTime = elevator.getCloseTime();
        moveTime = elevator.getMoveTime();
        passengerRequestsInElevator = new ArrayList<>();
        for (PassengerRequest passengerRequest :
                elevator.getPassengerRequestsInElevator()) {
            passengerRequestsInElevator.add(passengerRequest.deepCopy());
        }

        totalTime = 0;

        departureTime = System.currentTimeMillis();
    }

    public void addPassengerRequest(PassengerRequest passengerRequest) {
        waitingTable.addPassengerRequest(passengerRequest);
        if (waitingTable.getWaitingTable().size() == 1) {
            departureTime = System.currentTimeMillis();
        }
    }

    public void doorOperation() {
        totalTime += openTime + closeTime;
        strategy.needOpenDoor(
                currentFloor, direction, passengerRequestsInElevator,
                weightHolder, capacity, true
        );
        strategy.clearMessages();   // 不要打印进出信息
        departureTime = System.currentTimeMillis();
    }

    public void move() {
        long waitingTime = departureTime + moveTime - System.currentTimeMillis();
        // long waitingTime = moveTime;
        if (waitingTime < 0) {
            waitingTime = 0;
        }
        totalTime += waitingTime;
        currentFloor += direction;
        departureTime = System.currentTimeMillis();
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public long calcRunningTime(Elevator elevator) {
        strategy = new Strategy(waitingTable, elevatorId);
        long startTime = System.currentTimeMillis();
        while (true) {
            ElevatorAction elevatorAction =
                    strategy.decideAction(
                            currentFloor, direction,
                            passengerRequestsInElevator,
                            weightHolder, capacity,
                            false,
                            elevator.getGlobalWaitingTable()
                    );  // 已经包含了上下客的动作
            if (elevatorAction == ElevatorAction.TURN) {
                Printer.printLogMessage("ELEV " + elevatorId + ": total weight "
                        + weightHolder.getWeight());
            }
            switch (elevatorAction) {
                case OPEN:
                    doorOperation();
                    break;
                case MOVE:
                    move();
                    break;
                case TURN:
                    direction = -direction;
                    break;
                case WAIT:
                case END:
                    // Printer.printLogMessage("Shadow Elevator " + elevatorId + " Simulate End.");
                    return System.currentTimeMillis() + totalTime - startTime;
                default:
                    break;
            }
        }
    }
}
