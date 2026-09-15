import request.PassengerRequest;

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

    private boolean isSubElevator; // 1:备用轿厢
    private boolean isDoubleMode;

    public ShadowElevator(Elevator elevator, boolean isDoubleMode) {
        // deepCopyForDouble: 修改到达目标层为转换层
        elevatorId = elevator.getElevatorId();
        waitingTable =
                (isDoubleMode) ? elevator.getWaitingTable().deepCopyForDouble(true) :
                        elevator.getWaitingTable().deepCopy();
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
            passengerRequestsInElevator.add(
                    (isDoubleMode) ? passengerRequest.deepCopyForDouble(true) :
                            passengerRequest.deepCopy()
            );
        }
        totalTime = (isDoubleMode) ? Arguments.PUNISHMENT_TIME : 0;
        departureTime = System.currentTimeMillis();
        isSubElevator = false;
        this.isDoubleMode = isDoubleMode;
    }

    public ShadowElevator(DoubleElevator doubleElevator) {
        elevatorId = doubleElevator.getElevatorId();
        waitingTable = doubleElevator.getWaitingTable().deepCopyForDouble(false);
        direction = doubleElevator.getDirection();
        currentFloor = doubleElevator.getCurrentFloor();
        weightHolder = doubleElevator.getWeightHolder().deepCopy();
        capacity = doubleElevator.getCapacity();
        openTime = doubleElevator.getOpenTime();
        closeTime = doubleElevator.getCloseTime();
        moveTime = doubleElevator.getMoveTime();
        passengerRequestsInElevator = new ArrayList<>();
        for (PassengerRequest passengerRequest :
                doubleElevator.getPassengerRequestsInElevator()) {
            passengerRequestsInElevator.add(
                    passengerRequest.deepCopyForDouble(false)
            );
        }
        // 双轿厢行程短，赋予惩罚时间平衡决策
        totalTime = Arguments.PUNISHMENT_TIME;
        departureTime = System.currentTimeMillis();
        isSubElevator = true;
        isDoubleMode = true;
    }

    public void addPassengerRequest(PassengerRequest passengerRequest) {
        if (isDoubleMode) {
            waitingTable.addPassengerRequest(passengerRequest.deepCopyForDouble(!isSubElevator));
        } else {
            waitingTable.addPassengerRequest(passengerRequest);
        }
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

    public long calcRunningTime(Elevator elevator, DoubleElevator doubleElevator) {
        strategy = new Strategy(waitingTable, elevatorId);
        long startTime = System.currentTimeMillis();
        /*if (isSubElevator) {
            Printer.printLogMessage("Running Time of Elevator " +
                    doubleElevator.getElevatorId() + " started.");
        } else {
            Printer.printLogMessage("Running Time of Elevator " +
                    elevator.getElevatorId() + " started. Current mode "
                    + elevator.getElevatorMode() + ".");
        }*/
        while (true) {
            ElevatorAction elevatorAction;
            if (isSubElevator) {
                elevatorAction = strategy.decideDoubleElevAction(currentFloor, direction,
                        passengerRequestsInElevator, weightHolder,
                        DoubleElevator.getGlobalWaitingTable(), false);
            } else {
                elevatorAction = strategy.decideAction(currentFloor, direction,
                                passengerRequestsInElevator, weightHolder,
                        elevator.getGlobalWaitingTable(), false, false,
                        isDoubleMode);
                // 已经包含了上下客的动作
            }
            /*Printer.printLogMessage("Action type: " + elevatorAction +
                    " Current Floor: " + FloorTranslator.translateFloorIntToStr(currentFloor));*/
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
                case TRANSFER:
                    /*if (isSubElevator) {
                        Printer.printLogMessage("Running Time of Elevator " +
                                doubleElevator.getElevatorId() + " finished.");
                    } else {
                        Printer.printLogMessage("Running Time of Elevator " +
                                elevator.getElevatorId() + " finished. Current mode "
                                + elevator.getElevatorMode() + ".");
                    }*/
                    return System.currentTimeMillis() + totalTime - startTime;
                default:
                    break;
            }
        }
    }
}
