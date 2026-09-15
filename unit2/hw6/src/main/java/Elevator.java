import java.util.ArrayList;
import java.util.Iterator;

public class Elevator extends Thread {
    private volatile boolean isUnderMaintenance;
    private MaintenanceRequest currentMaintenanceRequest;

    // 未被分配电梯的乘客列表，所有电梯要共享这同一张表
    private static WaitingTable globalWaitingTable;

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
    private int moveTimeUnderTest;

    private Strategy strategy;

    private volatile long departureTime;

    public Elevator(int elevatorId, WaitingTable waitingTable, WaitingTable globalWaitingTable) {
        Elevator.globalWaitingTable = globalWaitingTable;

        this.elevatorId = elevatorId;
        this.waitingTable = waitingTable;
        this.passengerRequestsInElevator = new ArrayList<>();
        this.direction = 1;
        this.currentFloor = FloorTranslator.translateFloorStrToInt("F1");

        this.weightHolder = new WeightHolder();

        this.capacity = Arguments.CAPACITY;
        this.openTime = Arguments.OPEN_TIME;
        this.closeTime = Arguments.CLOSE_TIME;
        this.moveTime = Arguments.MOVE_TIME;
        this.moveTimeUnderTest = Arguments.MOVE_TIME_UNDER_TEST;

        this.strategy = new Strategy(waitingTable, elevatorId);

        this.departureTime = System.currentTimeMillis();
    }

    public void doorOperation() {
        Printer.printOpen(currentFloor, elevatorId);
        long startTime = System.currentTimeMillis();
        strategy.printAllInAndOutMessages();
        doorOpenWait(startTime);
        strategy.needOpenDoor(
                currentFloor, direction, passengerRequestsInElevator,
                weightHolder, capacity, true
        );
        strategy.printAllInAndOutMessages();
        Printer.printClose(currentFloor, elevatorId);
        departureTime = System.currentTimeMillis();
    }

    public void setDepartureTime(long departureTime) {
        this.departureTime = departureTime;
    }

    public void move() {
        moveWait(departureTime, moveTime);
        currentFloor += direction;
        Printer.printArrive(currentFloor, elevatorId);
        departureTime = System.currentTimeMillis();
    }

    public boolean insidePassengerDestCheck(int targetFloor) {
        for (PassengerRequest passengerRequest : passengerRequestsInElevator) {
            if (passengerRequest.getToFloor() == targetFloor) {
                return true;
            }
        }
        return false;
    }

    public void managePassengersBeforeMaint() {
        int floorF1 = FloorTranslator.translateFloorStrToInt("F1");
        // 下客
        Iterator<PassengerRequest> iterator = passengerRequestsInElevator.iterator();
        while (iterator.hasNext()) {
            PassengerRequest passengerRequest = iterator.next();
            if (passengerRequest.getToFloor() == floorF1 ||
                    passengerRequest.getToFloor() != currentFloor) {
                continue;
            }

            boolean isDestination = (passengerRequest.getToFloor() == currentFloor);
            Printer.printOut(
                    passengerRequest.getPassengerId(), currentFloor,
                    elevatorId, isDestination
            );
            weightHolder.subtractWeight(passengerRequest.getWeight());

            iterator.remove();
        }
        // 上客
        ArrayList<Integer> inPassengers = new ArrayList<>();
        synchronized (waitingTable) {
            for (PassengerRequest passengerRequest : waitingTable.getWaitingTable().values()) {
                if (passengerRequest.getFromFloor() == currentFloor &&
                        passengerRequest.getToFloor() == floorF1 &&
                        weightHolder.getWeight() + passengerRequest.getWeight() <= capacity) {
                    Printer.printIn(
                            passengerRequest.getPassengerId(), currentFloor, elevatorId
                    );
                    passengerRequestsInElevator.add(passengerRequest);
                    inPassengers.add(passengerRequest.getPassengerId());
                    weightHolder.addWeight(passengerRequest.getWeight());
                }
            }
            waitingTable.removePassengerRequestSeries(inPassengers);
        }
    }

    public void clearInsidePassengers() {
        if (!passengerRequestsInElevator.isEmpty()) {
            for (PassengerRequest passengerRequest : passengerRequestsInElevator) {
                boolean isDestination = (passengerRequest.getToFloor() == currentFloor);
                Printer.printOut(
                        passengerRequest.getPassengerId(), currentFloor,
                        elevatorId, isDestination
                );
                weightHolder.subtractWeight(passengerRequest.getWeight());
                if (!isDestination) {
                    synchronized (waitingTable) {
                        waitingTable.addPassengerRequest(
                                new PassengerRequest(
                                        currentFloor, passengerRequest.getToFloor(),
                                        passengerRequest.getPassengerId(),
                                        passengerRequest.getWeight()
                                )
                        );
                    }

                }
            }
        }
        passengerRequestsInElevator.clear();
    }

    public void prepareForMaint() {
        boolean doorOpen = false;
        long startTime = 0L;
        int floorF1 = FloorTranslator.translateFloorStrToInt("F1");

        // 如果有本层要下电梯的，当前楼层要开门；如果存在要去F1的乘客请求，也在当前楼层开门
        if (insidePassengerDestCheck(currentFloor) || waitingTable.waitingDestCheck(floorF1)) {
            doorOpen = true;
            Printer.printOpen(currentFloor, elevatorId);
            startTime = System.currentTimeMillis();
            managePassengersBeforeMaint();
        }

        if (currentFloor != floorF1) {
            if (doorOpen) {
                doorOpenWait(startTime);
                Printer.printClose(currentFloor, elevatorId);
            }
            moveToTargetFloor(floorF1, moveTime);
            Printer.printOpen(currentFloor, elevatorId);
        } else {
            if (!doorOpen) {
                Printer.printOpen(currentFloor, elevatorId);
            }
        }
    }

    public void moveToTargetFloor(int endFloor, long moveTime) {
        if (currentFloor == endFloor) {
            return;
        }

        departureTime = System.currentTimeMillis();

        if (currentFloor < endFloor) {
            direction = 1;
        } else {
            direction = -1;
        }

        while (currentFloor != endFloor) {
            currentFloor += direction;
            moveWait(departureTime, moveTime);
            Printer.printArrive(currentFloor, elevatorId);
            departureTime = System.currentTimeMillis();
        }
    }

    public void maintain() {
        // maintain开始时电梯必须在F1
        assert currentFloor == FloorTranslator.translateFloorStrToInt("F1");
        long startTime = System.currentTimeMillis();
        // 维修工进入电梯前必须清客
        clearInsidePassengers();
        // 维修工进入电梯
        Printer.printIn(
                currentMaintenanceRequest.getMaintainerId(),
                currentFloor, elevatorId
        );
        doorOpenWait(startTime);
        Printer.printClose(currentFloor, elevatorId);
        Printer.printMaintMessage1(elevatorId);
        startTime = System.currentTimeMillis();
        // 当前还在等待这部电梯的人，需要乘坐其他电梯。只有maint1输出后才可以进行这个操作。
        synchronized (waitingTable) {
            for (PassengerRequest passengerRequest : waitingTable.getWaitingTable().values()) {
                globalWaitingTable.addPassengerRequest(passengerRequest.deepCopy());
            }
            waitingTable.clearWaitingTable();
        }
        long waitingTime = Math.max(startTime + 1000 - System.currentTimeMillis(), 0);
        try {
            Thread.sleep(waitingTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Printer.printMaintMessage2(elevatorId);
        // runningToTargetFloor中会更新departureTime
        testRun(currentMaintenanceRequest.getTargetFloor());
        // 在F1必须开关门一次让维修工下去
        Printer.printOpen(currentFloor, elevatorId);
        startTime = System.currentTimeMillis();
        Printer.printOut(
                currentMaintenanceRequest.getMaintainerId(),
                currentFloor, elevatorId, true
        );
        doorOpenWait(startTime);
        Printer.printClose(currentFloor, elevatorId);
    }

    public void testRun(int targetFloor) {
        moveToTargetFloor(targetFloor, moveTimeUnderTest);
        moveToTargetFloor(
                FloorTranslator.translateFloorStrToInt("F1"),
                moveTimeUnderTest
        );
    }

    private void doorOpenWait(long startTime) {
        long waitingTime = startTime + openTime + closeTime - System.currentTimeMillis();
        if (waitingTime < 0) {
            waitingTime = 0;
        }
        try {
            Thread.sleep(waitingTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void moveWait(long startTime, long moveTime) {
        long waitingTime = startTime + moveTime - System.currentTimeMillis();
        if (waitingTime < 0) {
            waitingTime = 0;
        }
        try {
            Thread.sleep(waitingTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (true) {
            // 修改前可能产生死锁的原因：Strategy先进行了判定，此时isUnderMaintenance为否，
            // 且waitingTable中没有乘客，电梯进入了wait状态。
            ElevatorAction elevatorAction =
                    strategy.decideAction(currentFloor, direction,
                            passengerRequestsInElevator,
                            weightHolder, capacity,
                            isUnderMaintenance, globalWaitingTable);
            switch (elevatorAction) {
                case MAINTAIN:
                    prepareForMaint();
                    maintain();
                    // 退出维护状态
                    Printer.printMaintEndMessage(elevatorId);
                    Arguments.subMaintenanceCount();
                    setUnderMaintenance(false);
                    currentMaintenanceRequest = null;
                    departureTime = System.currentTimeMillis();
                    break;
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
                    try {
                        synchronized (waitingTable) {
                            waitingTable.wait();
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case END:
                    Printer.printLogMessage("Elevator " + elevatorId + " Thread Ended.");
                    return;
                default:
                    break;
            }
        }
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public ArrayList<PassengerRequest> getPassengerRequestsInElevator() {
        return passengerRequestsInElevator;
    }

    public WaitingTable getWaitingTable() {
        return waitingTable;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getDirection() {
        return direction;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public int getOpenTime() {
        return openTime;
    }

    public int getCloseTime() {
        return closeTime;
    }

    public int getMoveTime() {
        return moveTime;
    }

    public WeightHolder getWeightHolder() {
        return weightHolder;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public boolean isUnderMaintenance() {
        return isUnderMaintenance;
    }

    public void setCurrentMaintenanceRequest(
            MaintenanceRequest currentMaintenanceRequest) {
        this.currentMaintenanceRequest = currentMaintenanceRequest;
    }

    public void setUnderMaintenance(boolean underMaintenance) {
        isUnderMaintenance = underMaintenance;
    }

    public WaitingTable getGlobalWaitingTable() {
        return globalWaitingTable;
    }
}
