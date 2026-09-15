package tool;

import com.oocourse.elevator3.TimableOutput;

public class Printer {
    public static void outPrint(String text) {
        TimableOutput.println(text);
    }

    public static String makeOpenMessage(int floor, int elevatorId) {
        return "OPEN-" + FloorTranslator.translateFloorIntToStr(floor) +
                "-" + elevatorId;
    }

    public static String makeCloseMessage(int floor, int elevatorId) {
        return "CLOSE-" + FloorTranslator.translateFloorIntToStr(floor) +
                "-" + elevatorId;
    }

    public static String makeInMessage(int passengerId, int floor, int elevatorId) {
        return "IN-" + passengerId +
                "-" + FloorTranslator.translateFloorIntToStr(floor) +
                "-" + elevatorId;
    }

    public static String makeOutMessage(int passengerId, int floor, int elevatorId,
                                        boolean isDestination) {
        if (isDestination) {
            return "OUT-S-" + passengerId +
                    "-" + FloorTranslator.translateFloorIntToStr(floor) +
                    "-" + elevatorId;
        } else {
            return "OUT-F-" + passengerId +
                    "-" + FloorTranslator.translateFloorIntToStr(floor) +
                    "-" + elevatorId;
        }
    }

    public static String makeReceiveMessage(int passengerId, int elevatorId) {
        return "RECEIVE-" + passengerId +
                "-" + elevatorId;
    }

    public static String makeArriveMessage(int floor, int elevatorId) {
        return "ARRIVE-" + FloorTranslator.translateFloorIntToStr(floor) +
                "-" + elevatorId;
    }

    public static String makeMaintMessage1(int elevatorId) {
        return "MAINT1-BEGIN-" + elevatorId;
    }

    public static String makeMaintMessage2(int elevatorId) {
        return "MAINT2-BEGIN-" + elevatorId;
    }

    public static String makeMaintEndMessage(int elevatorId) {
        return "MAINT-END-" + elevatorId;
    }

    public static String makeUpdateBeginMessage(int elevatorId) {
        return "UPDATE-BEGIN-" + elevatorId;
    }

    public static String makeUpdateEndMessage(int elevatorId) {
        return "UPDATE-END-" + elevatorId;
    }

    public static String makeRecycleBeginMessage(int elevatorId) {
        return "RECYCLE-BEGIN-" + elevatorId;
    }

    public static String makeRecycleEndMessage(int elevatorId) {
        return "RECYCLE-END-" + elevatorId;
    }

    public static void printOpen(int floor, int elevatorId) {
        outPrint(makeOpenMessage(floor, elevatorId));
    }

    public static void printClose(int floor, int elevatorId) {
        outPrint(makeCloseMessage(floor, elevatorId));
    }

    public static void printIn(int passengerId, int floor, int elevatorId) {
        outPrint(makeInMessage(passengerId, floor, elevatorId));
    }

    public static void printOut(int passengerId, int floor, int elevatorId,
                                boolean isDestination) {
        outPrint(makeOutMessage(passengerId, floor, elevatorId, isDestination));
    }

    public static void printReceive(int passengerId, int elevatorId) {
        outPrint(makeReceiveMessage(passengerId, elevatorId));
    }

    public static void printArrive(int floor, int elevatorId) {
        outPrint(makeArriveMessage(floor, elevatorId));
    }

    public static void printMaintMessage1(int elevatorId) {
        outPrint(makeMaintMessage1(elevatorId));
    }

    public static void printMaintMessage2(int elevatorId) {
        outPrint(makeMaintMessage2(elevatorId));
    }

    public static void printMaintEndMessage(int elevatorId) {
        outPrint(makeMaintEndMessage(elevatorId));
    }

    public static void printUpdateBeginMessage(int elevatorId) {
        outPrint(makeUpdateBeginMessage(elevatorId));
    }

    public static void printUpdateEndMessage(int elevatorId) {
        outPrint(makeUpdateEndMessage(elevatorId));
    }

    public static void printRecycleBeginMessage(int elevatorId) {
        outPrint(makeRecycleBeginMessage(elevatorId));
    }

    public static void printRecycleEndMessage(int elevatorId) {
        outPrint(makeRecycleEndMessage(elevatorId));
    }

    public static void printLogMessage(String message) {
        // outPrint("LOG-" + message);
    }
}