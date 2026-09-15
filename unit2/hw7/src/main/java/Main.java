import com.oocourse.elevator3.TimableOutput;
import tool.FloorTranslator;

import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();
        WaitingTable waitingTable = new WaitingTable();
        ArrayList<WaitingTable> dispatchedTables =
                new ArrayList<>(Arrays.asList(new WaitingTable[12]));
        ArrayList<Elevator> elevators = new ArrayList<>();
        ArrayList<DoubleElevator> doubleElevators = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            WaitingTable dispatchedTable = new WaitingTable();
            WaitingTable dispatchedTableDouble = new WaitingTable();
            dispatchedTables.set(i, dispatchedTable);
            dispatchedTables.set(i + 6, dispatchedTableDouble);
            TransferFloorLock transferFloorLock = new TransferFloorLock();
            Elevator elevator =
                    new Elevator(i + 1, dispatchedTable, waitingTable,
                    transferFloorLock);
            DoubleElevator doubleElevator =
                    new DoubleElevator(i + 7, dispatchedTableDouble, waitingTable,
                            FloorTranslator.translateFloorStrToInt("F1"),
                            transferFloorLock, elevator);
            elevator.setSubElevator(doubleElevator);
            elevators.add(elevator);
            doubleElevators.add(doubleElevator);
            elevator.start();
        }
        Dispatcher dispatcher =
                new Dispatcher(waitingTable, dispatchedTables, elevators, doubleElevators);
        dispatcher.start();
        InputThread inputThread = new InputThread(waitingTable, elevators, doubleElevators);
        inputThread.start();
    }
}
