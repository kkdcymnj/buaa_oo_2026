import tool.Printer;

public class Arguments {
    public static final int CAPACITY = 400;
    public static final int MOVE_TIME = 400;
    public static final int MOVE_TIME_UNDER_TEST = 200;
    public static final int OPEN_TIME = 200;
    public static final int CLOSE_TIME = 200;
    public static final int PUNISHMENT_TIME = 1000;

    public static final Object dispatchLock = new Object();

    private static int MAINTENANCE_COUNT = 0;

    public static int getMaintenanceCount() {
        synchronized (dispatchLock) {
            return MAINTENANCE_COUNT;
        }
    }

    public static void addMaintenanceCount() {
        synchronized (dispatchLock) {
            MAINTENANCE_COUNT += 1;
        }
    }

    public static void subMaintenanceCount() {
        synchronized (dispatchLock) {
            MAINTENANCE_COUNT -= 1;
            dispatchLock.notifyAll();
        }
    }

    private static int UPDATE_COUNT = 0;

    public static int getUpdateCount() {
        synchronized (dispatchLock) {
            return UPDATE_COUNT;
        }
    }

    public static void addUpdateCount() {
        synchronized (dispatchLock) {
            UPDATE_COUNT += 1;
        }
    }

    public static void subUpdateCount() {
        synchronized (dispatchLock) {
            UPDATE_COUNT -= 1;
            dispatchLock.notifyAll();
        }
    }

    private static int RECYCLE_COUNT = 0;

    public static int getRecycleCount() {
        synchronized (dispatchLock) {
            return RECYCLE_COUNT;
        }
    }

    public static void addRecycleCount() {
        synchronized (dispatchLock) {
            RECYCLE_COUNT += 1;
        }
    }

    public static void subRecycleCount() {
        synchronized (dispatchLock) {
            RECYCLE_COUNT -= 1;
            dispatchLock.notifyAll();
        }
    }

    /*private static int TRANSFER_COUNT = 0;

    public static int getTransferCount() {
        synchronized (dispatchLock) {
            return TRANSFER_COUNT;
        }
    }

    public static void addTransferCount() {
        synchronized (dispatchLock) {
            TRANSFER_COUNT += 1;
        }
    }

    public static void subTransferCount() {
        synchronized (dispatchLock) {
            TRANSFER_COUNT -= 1;
        }
    }*/

    public static boolean noSpecialState() {
        synchronized (dispatchLock) {
            return MAINTENANCE_COUNT == 0 && UPDATE_COUNT == 0 && RECYCLE_COUNT == 0;
        }
    }

    public static boolean notEnoughElevator() {
        synchronized (dispatchLock) {
            int count =  MAINTENANCE_COUNT + UPDATE_COUNT + RECYCLE_COUNT;
            if (count >= 5) {
                Printer.printLogMessage("notEnoughElevator=true, count=" + count
                        + " (MAINT=" + MAINTENANCE_COUNT
                        + ", UPDATE=" + UPDATE_COUNT
                        + ", RECYCLE=" + RECYCLE_COUNT + ")");
                return true;
            } else {
                return false;
            }
        }
    }
}