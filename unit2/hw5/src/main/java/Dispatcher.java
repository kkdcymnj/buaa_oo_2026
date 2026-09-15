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
        int result = passengerRequest.getElevatorId() - 1;
        Printer.printReceive(passengerRequest.getPassengerId(), result + 1);
        dispatchTime = System.currentTimeMillis();
        return result;
    }

    @Override
    public void run() {
        while (true) {
            if (waitingTable.isEmpty() && waitingTable.isEnded()) {
                for (WaitingTable waitingTablesAtFloor : dispatchedTables) {
                    waitingTablesAtFloor.setEnded();
                }
                return;
            }
            // 从等候队列中取出一个请求进行分配
            PassengerRequest passengerRequest = waitingTable.removeAndDispatchRequest();
            if (passengerRequest == null) {
                continue;
            }
            int result = getDispatchTarget(passengerRequest);
            WaitingTable targetTable = dispatchedTables.get(result);
            if (targetTable.isEmpty()) {
                elevators.get(result).setDepartureTime(dispatchTime);
            }
            targetTable.addPassengerRequest(passengerRequest);
        }
    }
}
