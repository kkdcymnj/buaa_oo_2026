import request.ElevRecycleRequest;
import request.PassengerRequest;
import tool.FloorTranslator;
import tool.Printer;

import java.util.ArrayList;
import java.util.Iterator;

public class DoubleElevator extends Thread {
    private boolean isUpperDeck;

    private volatile boolean isUnderRecycle;
    private ElevRecycleRequest currentElevRecycleRequest;

    // 未被分配电梯的乘客列表，所有电梯要共享这同一张表
    private static WaitingTable globalWaitingTable;

    private int elevatorId;
    private ArrayList<PassengerRequest> passengerRequestsInElevator;
    private WaitingTable waitingTable;  // 还在等待这一部电梯的乘客
    private int direction;
    private int currentFloor;
    private int transferFloor;  // 换乘层设定
    private WeightHolder weightHolder;
    private int capacity;
    private int openTime;
    private int closeTime;
    private int moveTime;
    private Strategy strategy;
    private volatile long departureTime;

    private TransferFloorLock transferFloorLock;
    private boolean isElevEnded;
    private boolean isRecycleBegin;

    private Elevator mainElevator;

    public DoubleElevator(int elevatorId, WaitingTable waitingTable,
                          WaitingTable globalWaitingTable,
                          int currentFloor, TransferFloorLock transferFloorLock,
                          Elevator mainElevator) {
        DoubleElevator.globalWaitingTable = globalWaitingTable;

        this.isUnderRecycle = false;
        this.isElevEnded = false;
        this.elevatorId = elevatorId;
        this.waitingTable = waitingTable;
        this.passengerRequestsInElevator = new ArrayList<>();
        this.direction = 1;
        this.currentFloor = currentFloor;
        this.weightHolder = new WeightHolder();

        this.capacity = Arguments.CAPACITY;
        this.openTime = Arguments.OPEN_TIME;
        this.closeTime = Arguments.CLOSE_TIME;
        this.moveTime = Arguments.MOVE_TIME;

        this.strategy = new Strategy(waitingTable, elevatorId);

        this.departureTime = System.currentTimeMillis();

        this.transferFloorLock = transferFloorLock;
        this.transferFloor = FloorTranslator.translateFloorStrToInt("F2");

        this.isUpperDeck = (elevatorId <= 6);
        this.isRecycleBegin = false;

        this.mainElevator = mainElevator;
    }

    public void timelyWait(long waitTime) {
        try {
            Thread.sleep(Math.max(waitTime, 0));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void moveWait(long departureTime, long moveTime) {
        timelyWait(departureTime + moveTime - System.currentTimeMillis());
    }

    public void doorOpenWait(long departureTime, long openTime, long closeTime) {
        timelyWait(departureTime + openTime + closeTime - System.currentTimeMillis());
    }

    public void doorOperation() {
        Printer.printOpen(currentFloor, elevatorId);
        long startTime = System.currentTimeMillis();
        strategy.printAllInAndOutMessages();
        doorOpenWait(startTime, openTime, closeTime);
        strategy.needOpenDoor(
                currentFloor, direction, passengerRequestsInElevator,
                weightHolder, capacity, true
        );
        strategy.printAllInAndOutMessages();
        Printer.printClose(currentFloor, elevatorId);
        departureTime = System.currentTimeMillis();
    }

    public void move() {
        moveWait(departureTime, moveTime);
        if (currentFloor + direction == transferFloor) {
            transferFloorLock.getTransferFloorLock();
        }
        currentFloor += direction;
        Printer.printArrive(currentFloor, elevatorId);
        if (currentFloor - direction == transferFloor) {
            transferFloorLock.releaseTransferFloorLock();
        }
        departureTime = System.currentTimeMillis();
    }

    public void leaveTransferFloor() {
        moveWait(departureTime, moveTime);
        if (isUpperDeck) {
            direction = 1;
            currentFloor++;
        } else {
            direction = -1;
            currentFloor--;
        }
        Printer.printArrive(currentFloor, elevatorId);
        transferFloorLock.releaseTransferFloorLock();
        departureTime = System.currentTimeMillis();
    }

    public void elevWait() {
        if (currentFloor == transferFloor) {
            leaveTransferFloor();
        }
        synchronized (waitingTable) {
            while (waitingTable.isEmpty() && !isElevEnded && !isUnderRecycle) {
                try {
                    waitingTable.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void elevEnd() {
        if (currentFloor == transferFloor) {
            leaveTransferFloor();
        }
        Printer.printLogMessage("Elevator " + elevatorId + " Thread Ended.");
    }

    public void setElevEnded(boolean elevEnded) {
        this.isElevEnded = elevEnded;
    }

    public boolean insidePassengerDestCheck(int targetFloor) {
        for (PassengerRequest passengerRequest : passengerRequestsInElevator) {
            if (passengerRequest.getToFloor() == targetFloor) {
                return true;
            }
        }
        return false;
    }

    public void removeOutWayPassengers(int currentFloor, int targetFloor) {
        Iterator<PassengerRequest> iterator = passengerRequestsInElevator.iterator();
        while (iterator.hasNext()) {
            PassengerRequest passengerRequest = iterator.next();
            boolean isDestination = (passengerRequest.getToFloor() == currentFloor);
            if (passengerRequest.getToFloor() == targetFloor || !isDestination) {
                continue;
            }

            Printer.printOut(
                    passengerRequest.getPassengerId(), currentFloor,
                    elevatorId, true
            );
            weightHolder.subtractWeight(passengerRequest.getWeight());

            iterator.remove();
        }
    }

    public void takeOnWayPassengers(int currentFloor, int targetFloor) {
        ArrayList<Integer> inPassengers = new ArrayList<>();
        synchronized (waitingTable) {
            for (PassengerRequest passengerRequest : waitingTable.getWaitingTable().values()) {
                if (passengerRequest.getToFloor() != targetFloor ||
                        passengerRequest.getFromFloor() != currentFloor) {
                    continue;
                }
                int weight = passengerRequest.getWeight();
                int passengerId = passengerRequest.getPassengerId();
                if (weightHolder.getWeight() + weight <= capacity) {
                    Printer.printIn(passengerId, currentFloor, elevatorId);
                    passengerRequestsInElevator.add(passengerRequest);
                    inPassengers.add(passengerId);
                    weightHolder.addWeight(weight);
                }
            }
            waitingTable.removePassengerRequestSeries(inPassengers);
        }
    }

    public void moveToTargetFloor(int endFloor, long moveTime) {
        if (currentFloor == endFloor) {
            return;
        }
        departureTime = System.currentTimeMillis();
        direction = (endFloor > currentFloor) ? 1 : -1;
        while (currentFloor != endFloor) {
            move();
            departureTime = System.currentTimeMillis();
        }
    }

    public void prepareForRecycle() {
        boolean doorOpen = false;
        long startTime = 0L;
        int targetFloor = FloorTranslator.translateFloorStrToInt("F1");

        // 如果有本层要下电梯的，当前楼层要开门；如果存在要去F1的乘客请求，也在当前楼层开门
        if (insidePassengerDestCheck(currentFloor) || waitingTable.waitingDestCheck(targetFloor)) {
            doorOpen = true;
            Printer.printOpen(currentFloor, elevatorId);
            startTime = System.currentTimeMillis();
            managePassengersBeforeRecycle();
        }

        if (currentFloor != targetFloor) {
            if (doorOpen) {
                doorOpenWait(startTime, openTime, closeTime);
                Printer.printClose(currentFloor, elevatorId);
            }
            moveToTargetFloor(targetFloor, moveTime);
            if (!passengerRequestsInElevator.isEmpty()) {
                Printer.printOpen(currentFloor, elevatorId);
            }
        } else {
            if (!doorOpen && !passengerRequestsInElevator.isEmpty()) {
                Printer.printOpen(currentFloor, elevatorId);
            }
        }
    }

    public void clearInsidePassengers() {
        if (passengerRequestsInElevator.isEmpty()) {
            return;
        }
        for (PassengerRequest passengerRequest : passengerRequestsInElevator) {
            boolean isDestination = (passengerRequest.getToFloor() == currentFloor);
            Printer.printOut(
                    passengerRequest.getPassengerId(), currentFloor,
                    elevatorId, isDestination
            );
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
        passengerRequestsInElevator.clear();
        weightHolder.setWeight(0);
    }

    public void managePassengersBeforeRecycle() {
        int floorF1 = FloorTranslator.translateFloorStrToInt("F1");
        removeOutWayPassengers(currentFloor, floorF1);
        takeOnWayPassengers(currentFloor, floorF1);
    }

    public void transferPassengers() {
        Printer.printOpen(currentFloor, elevatorId);
        long startTime = System.currentTimeMillis();
        Iterator<PassengerRequest> iterator = passengerRequestsInElevator.iterator();
        while (iterator.hasNext()) {
            PassengerRequest passengerRequest = iterator.next();
            int toFloor = passengerRequest.getToFloor();
            if (toFloor > transferFloor && isUpperDeck) {
                continue;
            } else if (toFloor < transferFloor && !isUpperDeck) {
                continue;
            }
            boolean isDestination = (passengerRequest.getToFloor() == transferFloor);
            Printer.printOut(passengerRequest.getPassengerId(), transferFloor,
                    elevatorId, isDestination);
            if (!isDestination) {
                globalWaitingTable.addPassengerRequest(
                        new PassengerRequest(transferFloor, passengerRequest.getToFloor(),
                                passengerRequest.getPassengerId(), passengerRequest.getWeight())
                );
                //Arguments.subTransferCount();
            }
            weightHolder.subtractWeight(passengerRequest.getWeight());
            iterator.remove();
        }
        doorOpenWait(startTime, openTime, closeTime);
        Printer.printClose(currentFloor, elevatorId);
        departureTime = System.currentTimeMillis();
    }

    public void recycle() {
        assert currentFloor == FloorTranslator.translateFloorStrToInt("F1");
        long startTime = System.currentTimeMillis();
        // 第一阶段：清客+开始回收
        if (!passengerRequestsInElevator.isEmpty()) {
            clearInsidePassengers();
            doorOpenWait(startTime, openTime, closeTime);
            Printer.printClose(currentFloor, elevatorId);
        }
        Printer.printRecycleBeginMessage(elevatorId);
        isRecycleBegin = true;
        startTime = System.currentTimeMillis();
        // 第二阶段：回收结束
        timelyWait(1000 + startTime - System.currentTimeMillis());
        Printer.printRecycleEndMessage(elevatorId);
        // 此时电梯一定把transferLock交出了
        isElevEnded = true;
    }

    @Override
    public void run() {
        while (true) {
            ElevatorAction elevatorAction =
                    strategy.decideDoubleElevAction(
                            currentFloor, direction, passengerRequestsInElevator,
                            weightHolder, globalWaitingTable,
                            isUnderRecycle
                    );
            switch (elevatorAction) {
                case RECYCLE:
                    prepareForRecycle();
                    recycle();
                    departureTime = System.currentTimeMillis();
                    mainElevator.setElevatorMode(ElevatorMode.SINGLE);
                    synchronized (waitingTable) {
                        for (PassengerRequest passengerRequest :
                                waitingTable.getWaitingTable().values()) {
                            globalWaitingTable.addPassengerRequest(passengerRequest.deepCopy());
                        }
                        waitingTable.clearWaitingTable();
                    }
                    Arguments.subRecycleCount();
                    setUnderRecycle(false);
                    elevEnd();
                    return;
                case TRANSFER:
                    transferPassengers();
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
                    elevWait();
                    break;
                case END:
                    elevEnd();
                    return;
                default:
                    break;
            }
        }
    }

    public void setUnderRecycle(boolean underRecycle) {
        this.isUnderRecycle = underRecycle;
    }

    public boolean isUnderRecycle() {
        return isUnderRecycle;
    }

    public boolean isUpperDeck() {
        return isUpperDeck;
    }

    public void setIsElevEnded(boolean isElevEnded) {
        this.isElevEnded = isElevEnded;
    }

    public void setCurrentElevRecycleRequest(ElevRecycleRequest elevRecycleRequest) {
        this.currentElevRecycleRequest = elevRecycleRequest;
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

    public void setDepartureTime(long departureTime) {
        this.departureTime = departureTime;
    }

    public boolean isElevEnded() {
        return isElevEnded;
    }

    public boolean isRecycleBegin() {
        return isRecycleBegin;
    }

    public static WaitingTable getGlobalWaitingTable() {
        return globalWaitingTable;
    }
}
