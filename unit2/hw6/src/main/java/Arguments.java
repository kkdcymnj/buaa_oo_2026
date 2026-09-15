public class Arguments {
    public static final int CAPACITY = 400;
    public static final int MOVE_TIME = 400;
    public static final int MOVE_TIME_UNDER_TEST = 200;
    public static final int OPEN_TIME = 200;
    public static final int CLOSE_TIME = 200;

    private static int MAINTENANCE_COUNT = 0;

    public static synchronized int getMaintenanceCount() {
        return MAINTENANCE_COUNT;
    }

    public static synchronized void addMaintenanceCount() {
        MAINTENANCE_COUNT += 1;
    }

    public static synchronized void subMaintenanceCount() {
        MAINTENANCE_COUNT -= 1;
    }
}
