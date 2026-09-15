package tool;

public class FloorTranslator {
    public static int translateFloorStrToInt(String floorStr) {
        switch (floorStr) {
            case "B4":
                return 1;
            case "B3":
                return 2;
            case "B2":
                return 3;
            case "B1":
                return 4;
            case "F1":
                return 5;
            case "F2":
                return 6;
            case "F3":
                return 7;
            case "F4":
                return 8;
            case "F5":
                return 9;
            case "F6":
                return 10;
            case "F7":
                return 11;
            default:
                Printer.printLogMessage("Invalid floor string: " + floorStr);
                return -1;
        }
    }

    public static String translateFloorIntToStr(int floorInt) {
        switch (floorInt) {
            case 1:
                return "B4";
            case 2:
                return "B3";
            case 3:
                return "B2";
            case 4:
                return "B1";
            case 5:
                return "F1";
            case 6:
                return "F2";
            case 7:
                return "F3";
            case 8:
                return "F4";
            case 9:
                return "F5";
            case 10:
                return "F6";
            case 11:
                return "F7";
            default:
                Printer.printLogMessage("Invalid floor integer: " + floorInt);
                return null;
        }
    }
}