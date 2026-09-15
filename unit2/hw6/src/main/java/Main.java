import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();
        WaitingTable waitingTable = new WaitingTable();
        ArrayList<WaitingTable> dispatchedTables = new ArrayList<>();
        ArrayList<Elevator> elevators = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            WaitingTable dispatchedTable = new WaitingTable();
            dispatchedTables.add(dispatchedTable);
            Elevator elevator = new Elevator(i + 1, dispatchedTable, waitingTable);
            elevators.add(elevator);
            elevator.start();
        }
        Dispatcher dispatcher = new Dispatcher(waitingTable, dispatchedTables, elevators);
        dispatcher.start();
        InputThread inputThread = new InputThread(waitingTable, elevators);
        inputThread.start();
    }
}
