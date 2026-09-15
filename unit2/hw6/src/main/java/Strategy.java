import java.util.ArrayList;
import java.util.Iterator;

public class Strategy {
    private WaitingTable waitingTable;
    private int elevatorId;
    private ArrayList<String> outMessages = new ArrayList<>();
    private ArrayList<String> inMessages = new ArrayList<>();

    public Strategy(WaitingTable waitingTable, int elevatorId) {
        this.waitingTable = waitingTable;
        this.elevatorId = elevatorId;
    }

    public void printAllInAndOutMessages() {
        for (String message : outMessages) {
            Printer.outPrint(message);
        }
        for (String message : inMessages) {
            Printer.outPrint(message);
        }
        clearMessages();
    }

    public void clearMessages() {
        outMessages.clear();
        inMessages.clear();
    }

    public boolean needTurn(int currentFloor, int direction) {
        // 1: 上行，-1: 下行
        synchronized (waitingTable) {
            for (PassengerRequest passengerRequest :
                    waitingTable.getWaitingTable().values()) {
                if (passengerRequest.getFromFloor() > currentFloor && direction > 0) {
                    return false;
                }
                if (passengerRequest.getFromFloor() < currentFloor && direction < 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean needOpenDoor(int currentFloor, int direction,
                                ArrayList<PassengerRequest> requestInElevator,
                                WeightHolder weightHolder, int capacity,
                                boolean inOnly) {
        Iterator<PassengerRequest> iterator = requestInElevator.iterator();
        boolean hasOut = false;
        boolean hasIn = false;

        if (!inOnly) {
            while (iterator.hasNext()) {
                PassengerRequest passengerRequest = iterator.next();
                if (passengerRequest.getToFloor() == currentFloor) {
                    outMessages.add(
                            Printer.makeOutMessage(
                                    passengerRequest.getPassengerId(),
                                    currentFloor,
                                    elevatorId,
                                    true
                            )
                    );
                    weightHolder.subtractWeight(passengerRequest.getWeight());
                    iterator.remove();
                    hasOut = true;
                }
            }
        }

        ArrayList<Integer> allowedPassengers = new ArrayList<>();
        synchronized (waitingTable) {
            for (PassengerRequest passengerRequest :
                    waitingTable.getWaitingTable().values()) {
                if (passengerRequest.getFromFloor() == currentFloor &&
                        passengerRequest.getDirection() == direction &&
                        weightHolder.getWeight() + passengerRequest.getWeight() <= capacity) {
                    inMessages.add(
                            Printer.makeInMessage(
                                    passengerRequest.getPassengerId(),
                                    currentFloor,
                                    elevatorId
                            )
                    );
                    requestInElevator.add(passengerRequest);
                    allowedPassengers.add(passengerRequest.getPassengerId());
                    weightHolder.addWeight(passengerRequest.getWeight());
                    hasIn = true;
                }
            }
            waitingTable.removePassengerRequestSeries(allowedPassengers);
        }

        return hasOut || hasIn;
    }

    public ElevatorAction decideAction(int currentFloor, int direction,
                                       ArrayList<PassengerRequest> requestInElevator,
                                       WeightHolder weightHolder, int capacity,
                                       boolean isUnderMaintenance,
                                       WaitingTable globalWaitingTable) {
        if (isUnderMaintenance) {
            return ElevatorAction.MAINTAIN;
        }

        boolean doorOpen = needOpenDoor(currentFloor, direction,
                requestInElevator, weightHolder, capacity, false);
        if (doorOpen) {
            return ElevatorAction.OPEN;
        }
        if (!requestInElevator.isEmpty()) {
            return ElevatorAction.MOVE;
        }

        if (waitingTable.isEmpty()) {
            if (waitingTable.isEnded() && globalWaitingTable.isEmpty()
                && Arguments.getMaintenanceCount() == 0) {
                return ElevatorAction.END;
            } else {
                return ElevatorAction.WAIT;
            }
        }

        boolean turn = needTurn(currentFloor, direction);
        if (turn) {
            return ElevatorAction.TURN;
        } else {
            return ElevatorAction.MOVE;
        }
    }
}
