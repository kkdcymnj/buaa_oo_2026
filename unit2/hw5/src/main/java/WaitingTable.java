import java.util.ArrayList;
import java.util.HashMap;

public class WaitingTable {
    // key: passenger_id, value: passenger_request
    private HashMap<Integer, PassengerRequest> waitingTable = new HashMap<>();
    private boolean isEnded = false;

    public synchronized void addPassengerRequest(PassengerRequest passengerRequest) {
        waitingTable.put(passengerRequest.getPassengerId(), passengerRequest);
        notifyAll();
    }

    public synchronized void removePassengerRequest(int passengerId) {
        waitingTable.remove(passengerId);
    }

    public synchronized void removePassengerRequestSeries(ArrayList<Integer> passengerIds) {
        for (Integer passengerId : passengerIds) {
            removePassengerRequest(passengerId);
        }
    }

    public synchronized void setEnded() {
        isEnded = true;
        notifyAll();
    }

    public synchronized boolean isEnded() {
        return isEnded;
    }

    public synchronized HashMap<Integer, PassengerRequest> getWaitingTable() {
        return waitingTable;
    }

    public synchronized boolean isEmpty() {
        return waitingTable.isEmpty();
    }

    public synchronized PassengerRequest removeAndDispatchRequest() {
        if (waitingTable.isEmpty() && !isEnded) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (waitingTable.isEmpty()) {
            return null;
        }
        PassengerRequest passengerRequest = waitingTable.values().iterator().next();
        waitingTable.remove(passengerRequest.getPassengerId());
        notifyAll();
        return passengerRequest;
    }
}
