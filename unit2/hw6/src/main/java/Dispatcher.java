import java.util.ArrayList;

public class Dispatcher extends Thread {
    private WaitingTable waitingTable;
    private ArrayList<WaitingTable> dispatchedTables;
    private long dispatchTime;
    private ArrayList<Elevator> elevators;

    public Dispatcher(WaitingTable waitingTable,
                      ArrayList<WaitingTable> dispatchedTables,
                      ArrayList<Elevator> elevators) {
        this.waitingTable = waitingTable;
        this.dispatchedTables = dispatchedTables;
        this.dispatchTime = System.currentTimeMillis();
        this.elevators = elevators;
    }

    public int getDispatchTarget(PassengerRequest passengerRequest) {
        ArrayList<Elevator> workingElevators = new ArrayList<>();
        ArrayList<ShadowElevator> shadowElevators = new ArrayList<>();
        for (Elevator elevator : elevators) {
            if (!elevator.isUnderMaintenance()) {
                workingElevators.add(elevator);
            }
        }
        int workingNum = workingElevators.size();
        if (workingNum < 2 && elevators.size() > 1) {
            return -1;
        }
        ArrayList<Long> runningTimes = new ArrayList<>();
        for (int i = 0; i < workingNum * workingNum; i++) {
            shadowElevators.add(new ShadowElevator(workingElevators.get(i % workingNum)));
        }
        Printer.printLogMessage("Dispatch Calculation Begin.");
        for (int i = 0; i < workingNum; i++) {
            shadowElevators.get(i * workingNum + i).addPassengerRequest(passengerRequest);
            runningTimes.add(0L);
            long runningTime = 0L;
            for (int j = 0; j < workingNum; j++) {
                runningTime =
                        Math.max(runningTime,
                                shadowElevators.get(i * workingNum + j).
                                calcRunningTime(workingElevators.get(j)));
                Printer.printLogMessage("Dispatch Calculation of " + j + "th shadow elevator end." +
                        " The new request is in " + i + "th shadow elevator.");
            }
            runningTimes.set(i, runningTime);
        }
        Printer.printLogMessage("Dispatch Calculation End.");
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

    @Override
    public void run() {
        while (true) {
            if (waitingTable.isEmpty() && waitingTable.isEnded() &&
                Arguments.getMaintenanceCount() == 0) {
                for (WaitingTable waitingTablesAtFloor : dispatchedTables) {
                    waitingTablesAtFloor.setEnded();
                }
                Printer.printLogMessage("Dispatcher Thread Ended.");
                return;
            }
            // 从等候队列中取出一个请求进行分配
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
                elevators.get(result - 1).setDepartureTime(dispatchTime);
            }
            targetTable.addPassengerRequest(passengerRequest);
        }
    }
}
