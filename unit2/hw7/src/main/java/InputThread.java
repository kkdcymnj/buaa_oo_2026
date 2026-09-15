import com.oocourse.elevator3.ElevatorInput;
import com.oocourse.elevator3.MaintRequest;
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.RecycleRequest;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.UpdateRequest;
import request.ElevRecycleRequest;
import request.ElevUpdateRequest;
import request.MaintenanceRequest;
import request.PassengerRequest;
import tool.Printer;

import java.io.IOException;
import java.util.ArrayList;

public class InputThread extends Thread {
    private WaitingTable waitingTable;
    private ArrayList<Elevator> elevators;
    private ArrayList<DoubleElevator> doubleElevators;

    public InputThread(WaitingTable waitingTable, ArrayList<Elevator> elevators,
                       ArrayList<DoubleElevator> doubleElevators) {
        this.waitingTable = waitingTable;
        this.elevators = elevators;
        this.doubleElevators = doubleElevators;
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
                } else if (request instanceof UpdateRequest) {
                    UpdateRequest updateRequest = (UpdateRequest) request;
                    ElevUpdateRequest elevUpdateRequest = new ElevUpdateRequest(updateRequest);
                    Arguments.addUpdateCount();
                    Elevator target = elevators.get(elevUpdateRequest.getElevatorId() - 1);
                    target.setUnderUpdate(true);
                    target.setCurrentElevUpdateRequest(elevUpdateRequest);
                    synchronized (target.getWaitingTable()) {
                        target.getWaitingTable().notifyAll();
                    }
                } else if (request instanceof RecycleRequest) {
                    RecycleRequest recycleRequest = (RecycleRequest) request;
                    ElevRecycleRequest elevRecycleRequest = new ElevRecycleRequest(recycleRequest);
                    Arguments.addRecycleCount();
                    DoubleElevator target = doubleElevators.get(
                            elevRecycleRequest.getElevatorId() - 7);
                    target.setUnderRecycle(true);
                    target.setCurrentElevRecycleRequest(elevRecycleRequest);
                    synchronized (target.getWaitingTable()) {
                        target.getWaitingTable().notifyAll();
                    }
                } else {
                    Printer.printLogMessage("Invalid request type: " + request.getClass());
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
