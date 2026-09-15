import java.util.ArrayList;

public class Elevator extends Thread {
    private int elevatorId;
    private ArrayList<PassengerRequest> passengerRequestsInElevator;
    private WaitingTable waitingTable;  // 还在等待这一部电梯的乘客
    private int direction;
    private int currentFloor;
    private WeightHolder weightHolder;

    private int capacity;
    private int openTime;
    private int closeTime;
    private int moveTime;

    private Strategy strategy;

    private volatile long departureTime;

    public Elevator(int elevatorId, WaitingTable waitingTable) {
        this.elevatorId = elevatorId;
        this.waitingTable = waitingTable;
        this.passengerRequestsInElevator = new ArrayList<>();
        this.direction = 1;
        this.currentFloor = FloorTranslator.translateFloorStrToInt("F1");

        this.weightHolder = new WeightHolder();

        this.capacity = Arguments.CAPACITY;
        this.openTime = Arguments.OPEN_TIME;
        this.closeTime = Arguments.CLOSE_TIME;
        this.moveTime = Arguments.MOVE_TIME;

        this.strategy = new Strategy(waitingTable, elevatorId);

        this.departureTime = System.currentTimeMillis();
    }

    public void doorOperation() {
        Printer.printOpen(currentFloor, elevatorId);
        long startTime = System.currentTimeMillis();
        strategy.printAllInAndOutMessages();
        long waitingTime = startTime + openTime + closeTime - System.currentTimeMillis();
        if (waitingTime < 0) {
            waitingTime = 0;
        }
        try {
            Thread.sleep(waitingTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        strategy.needOpenDoor(
                currentFloor, direction, passengerRequestsInElevator,
                weightHolder, capacity, true
        );
        strategy.printAllInAndOutMessages();
        Printer.printClose(currentFloor, elevatorId);
        departureTime = System.currentTimeMillis();
    }

    public void setDepartureTime(long departureTime) {
        this.departureTime = departureTime;
    }

    public void move() {
        long waitingTime = departureTime + moveTime - System.currentTimeMillis();
        // long waitingTime = moveTime;
        if (waitingTime < 0) {
            waitingTime = 0;
        }
        try {
            Thread.sleep(waitingTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (direction == 1) {
            currentFloor++;
        } else {
            currentFloor--;
        }
        Printer.printArrive(currentFloor, elevatorId);
        departureTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        while (true) {
            ElevatorAction elevatorAction =
                    strategy.decideAction(currentFloor, direction, passengerRequestsInElevator,
                            weightHolder, capacity);
            switch (elevatorAction) {
                case OPEN:
                    doorOperation();
                    break;
                case MOVE:
                    move();
                    break;
                case TURN:
                    direction = -direction;
                    break;
                case WAIT:
                    synchronized (waitingTable) {
                        if (waitingTable.isEmpty() && !waitingTable.isEnded()) {
                            try {
                                waitingTable.wait();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    break;
                case END:
                    return;
                default:
                    break;
            }
        }
    }
}
