import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.MaintRequest;
import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;

import java.io.IOException;
import java.util.ArrayList;

public class InputThread extends Thread {
    private WaitingTable waitingTable;
    private ArrayList<Elevator> elevators;

    public InputThread(WaitingTable waitingTable, ArrayList<Elevator> elevators) {
        this.waitingTable = waitingTable;
        this.elevators = elevators;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                waitingTable.setEnded();
                Printer.printLogMessage("Input Thread Ended.");
                break;
            } else {
                if (request instanceof PersonRequest) {
                    PersonRequest personRequest = (PersonRequest) request;
                    waitingTable.addPassengerRequest(new PassengerRequest(personRequest));
                } else if (request instanceof MaintRequest) {
                    MaintRequest maintRequest = (MaintRequest) request;
                    MaintenanceRequest maintenanceRequest = new MaintenanceRequest(maintRequest);
                    Arguments.addMaintenanceCount();
                    Elevator target = elevators.get(maintenanceRequest.getElevatorId() - 1);
                    target.setUnderMaintenance(true);
                    target.setCurrentMaintenanceRequest(maintenanceRequest);
                    synchronized (target.getWaitingTable()) {
                        target.getWaitingTable().notifyAll();
                    }
                } else {
                    System.out.println("Invalid request type: " + request.getClass());
                    break;
                }
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
