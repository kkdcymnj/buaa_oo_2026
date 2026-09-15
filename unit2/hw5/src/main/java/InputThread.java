import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.Request;

import java.io.IOException;

public class InputThread extends Thread {
    private WaitingTable waitingTable;

    public InputThread(WaitingTable waitingTable) {
        this.waitingTable = waitingTable;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                waitingTable.setEnded();
                break;
            } else {
                if (request instanceof PersonRequest) {
                    PersonRequest personRequest = (PersonRequest) request;
                    waitingTable.addPassengerRequest(new PassengerRequest(personRequest));
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
