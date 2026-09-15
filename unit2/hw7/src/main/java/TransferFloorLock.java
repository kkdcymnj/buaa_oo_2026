public class TransferFloorLock {
    private boolean locked;

    public TransferFloorLock() {
        locked = false;
    }

    public synchronized void releaseTransferFloorLock() {
        locked = false;
        notifyAll();
    }

    public synchronized void getTransferFloorLock() {
        if (locked) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        locked = true;
    }
}
