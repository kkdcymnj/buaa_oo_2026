import request.PassengerRequest;
import tool.FloorTranslator;
import tool.Printer;

import java.util.ArrayList;

public class Dispatcher extends Thread {
    private WaitingTable waitingTable;
    private ArrayList<WaitingTable> dispatchedTables;
    private long dispatchTime;
    private ArrayList<Elevator> elevators;
    private ArrayList<DoubleElevator> doubleElevators;

    public Dispatcher(WaitingTable waitingTable,
                      ArrayList<WaitingTable> dispatchedTables,
                      ArrayList<Elevator> elevators,
                      ArrayList<DoubleElevator> doubleElevators) {
        this.waitingTable = waitingTable;
        this.dispatchedTables = dispatchedTables;
        this.dispatchTime = System.currentTimeMillis();
        this.elevators = elevators;
        this.doubleElevators = doubleElevators;
    }

    public boolean chooseUpperDeck(PassengerRequest passengerRequest) {
        int transferFloor = FloorTranslator.translateFloorStrToInt("F2");
        int toFloor = passengerRequest.getToFloor();
        int fromFloor = passengerRequest.getFromFloor();
        // 返回值1：上层轿厢；0：下层轿厢
        if (fromFloor > transferFloor) {
            return true;
        } else if (fromFloor < transferFloor) {
            return false;
        } else {
            return toFloor > transferFloor;
        }
    }

    private int createShadowMatrix(PassengerRequest passengerRequest,
                                   ArrayList<Elevator> workingElevators,
                                   ArrayList<ShadowElevator> shadowElevators) {
        boolean upperDeck = false;
        boolean flag;
        for (Elevator elevator : elevators) {
            if (elevator.getElevatorMode() == ElevatorMode.SINGLE) {
                flag = !elevator.isUnderUpdate() && !elevator.isUnderMaintenance();
            } else {
                upperDeck = chooseUpperDeck(passengerRequest);
                if (upperDeck) {
                    flag = true;
                } else {
                    DoubleElevator subElevator = elevator.getSubElevator();
                    flag = !subElevator.isUnderRecycle() && !subElevator.isElevEnded() &&
                            subElevator.isAlive();
                }
            }
            if (flag) {
                workingElevators.add(elevator);
            }
        }
        int workingNum = workingElevators.size();
        // Printer.printLogMessage("Current working num: " + workingNum);
        for (int i = 0; i < workingNum * workingNum; i++) {
            Elevator elevator = workingElevators.get(i % workingNum);
            if (elevator.getElevatorMode() == ElevatorMode.SINGLE) {
                shadowElevators.add(
                        new ShadowElevator(elevator, false));
            } else {
                if (upperDeck) {
                    shadowElevators.add(
                            new ShadowElevator(elevator, true));
                } else {
                    shadowElevators.add(
                            new ShadowElevator(doubleElevators.get(elevator.getElevatorId() - 1))
                    );
                }
            }
        }
        return workingNum;
    }

    public int getDispatchTarget(PassengerRequest passengerRequest) {
        // 改为局部变量，每次调度创建新的列表
        ArrayList<Elevator> workingElevators = new ArrayList<>();
        ArrayList<ShadowElevator> shadowElevators = new ArrayList<>();

        int workingNum = createShadowMatrix(passengerRequest, workingElevators, shadowElevators);
        if (workingNum == -1) {
            return -1;
        }

        ArrayList<Long> runningTimes = new ArrayList<>();
        //Printer.printLogMessage("Dispatch Calculation Begin.");
        for (int i = 0; i < workingNum; i++) {
            shadowElevators.get(i * workingNum + i).addPassengerRequest(passengerRequest);
            runningTimes.add(0L);
            long runningTime = 0L;
            for (int j = 0; j < workingNum; j++) {
                runningTime = Math.max(
                        runningTime,
                        shadowElevators.get(i * workingNum + j).calcRunningTime(
                                workingElevators.get(j),
                                doubleElevators.get(workingElevators.get(j).getElevatorId() - 1)
                        )
                );
            }
            runningTimes.set(i, runningTime);
        }
        //Printer.printLogMessage("Dispatch Calculation End.");

        long minTime = Long.MAX_VALUE;
        int result = 0;
        for (int i = 0; i < workingNum; i++) {
            if (runningTimes.get(i) < minTime) {
                minTime = runningTimes.get(i);
                result = shadowElevators.get(i).getElevatorId();
            }
        }
        Printer.printReceive(passengerRequest.getPassengerId(), result);
        dispatchTime = System.currentTimeMillis();
        return result;
    }

    private void waitForDispatchCondition() {
        // 等待 waitingTable 不为空的条件
        synchronized (waitingTable) {
            while (waitingTable.isEmpty() && !waitingTable.isEnded()) {
                try {
                    Printer.printLogMessage("Waiting for passengers...");
                    waitingTable.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        // 等待电梯充足的条件
        synchronized (Arguments.dispatchLock) {
            while (Arguments.notEnoughElevator()) {
                try {
                    Printer.printLogMessage("Waiting for enough elevators...");
                    Arguments.dispatchLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            waitForDispatchCondition();

            if (waitingTable.isEmpty() && waitingTable.isEnded() &&
                    Arguments.noSpecialState()) {
                for (WaitingTable dispatchedTable : dispatchedTables) {
                    dispatchedTable.setEnded();
                }
                Printer.printLogMessage("Dispatcher Thread Ended.");
                return;
            }
            Printer.printLogMessage("Dispatch enable.");

            PassengerRequest passengerRequest = waitingTable.removeRequestForDispatch();
            if (passengerRequest == null) {
                continue;
            }
            int result = getDispatchTarget(passengerRequest);
            // 当前没有可用电梯，乘客仍然进入waitingTable
            if (result < 1) {
                waitingTable.addPassengerRequest(passengerRequest);
                continue;
            }
            // 让乘客去对应电梯等待
            WaitingTable targetTable = dispatchedTables.get(result - 1);
            boolean shouldSetTime;
            shouldSetTime = targetTable.isEmpty();
            if (shouldSetTime) {
                if (result <= 6) {
                    elevators.get(result - 1).setDepartureTime(dispatchTime);
                } else {
                    doubleElevators.get(result - 7).setDepartureTime(dispatchTime);
                }
            }
            targetTable.addPassengerRequest(passengerRequest);
        }
    }
}