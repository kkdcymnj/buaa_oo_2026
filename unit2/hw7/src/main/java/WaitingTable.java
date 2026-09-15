import request.PassengerRequest;

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

    public synchronized void removePassengerRequestSeries(ArrayList<Integer> passengerIds) {
        for (Integer passengerId : passengerIds) {
            waitingTable.remove(passengerId);
        }
        notifyAll();
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

    public synchronized void clearWaitingTable() {
        waitingTable.clear();
        notifyAll();
    }

    public synchronized PassengerRequest removeRequestForDispatch() {
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
        synchronized (Arguments.dispatchLock) {
            Arguments.dispatchLock.notifyAll();
        }
        return passengerRequest;
    }

    public synchronized WaitingTable deepCopy() {
        WaitingTable newTable = new WaitingTable();
        for (PassengerRequest passengerRequest : waitingTable.values()) {
            newTable.waitingTable.put(passengerRequest.getPassengerId(),
                    passengerRequest.deepCopy());
        }
        newTable.isEnded = isEnded;
        return newTable;
    }

    public synchronized WaitingTable deepCopyForDouble(boolean isUpperDeck) {
        WaitingTable newTable = new WaitingTable();
        for (PassengerRequest passengerRequest : waitingTable.values()) {
            newTable.waitingTable.put(passengerRequest.getPassengerId(),
                    passengerRequest.deepCopyForDouble(isUpperDeck));
        }
        newTable.isEnded = isEnded;
        return newTable;
    }

    public synchronized boolean waitingDestCheck(int targetFloor) {
        for (PassengerRequest passengerRequest : waitingTable.values()) {
            if (passengerRequest.getToFloor() == targetFloor) {
                return true;
            }
        }
        return false;
    }
}
