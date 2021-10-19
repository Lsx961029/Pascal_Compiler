import java.nio.ByteBuffer;
import java.util.Stack;
import java.lang.Integer;

public class OpCodeStackMachine {

    private static int ip = 0;
    private static int dp = 0;

    private static Stack<Object> stack = new Stack<>();

    private static final int DATA_SIZE = 1000;

    private static Byte[] dataArray = new Byte[DATA_SIZE];

    private static Byte[] codeArray;

    public static void run() {
        Parser.OP_CODE opCode;
        while (true) {
            opCode = getOpCode();
            // System.out.print(" " + opCode + " "); // uncomment to print opCode
            // System.out.print(stack);
            // instructions
            switch (opCode) {
                case PUSH:
                    myPush();
                    break;
                case PUSHI:
                    pushi();
                    break;
                case PUSHF:
                    pushf();
                    break;
                case POP:
                    myPOP();
                    break;
                case PUT: // may have different Put for different size PUT1, PUT2, PUT4
                    put();
                    break;
                case GET: // different size GET1, GET2, GET4
                    get();
                    break;
                case NOT:
                    not();
                    break;
                case OR:
                    or();
                    break;
                case AND:
                    and();
                    break;
                case XOR:
                    xor();
                    break;
                case CVR:
                    cvr();
                    break;
                case XCHG:
                    xchg();
                    break;
                case JMP:
                    jmp();
                    break;
                case JFALSE: // absolute jump if false
                    jfalse();
                    break;
                case JTRUE:
                    jtrue();
                    break;
                case ADD:
                    add();
                    break;
                case FADD:
                    fadd();
                    break;
                case SUB:
                    sub();
                    break;
                case FSUB:
                    fsub();
                    break;
                case MULT:
                    mult();
                    break;
                case FMULT:
                    fmult();
                    break;
                case DIV:
                    div();
                    break;
                case FDIV:
                    fdiv();
                    break;
                case FNEG:
                    fneg();
                    break;
                case NEG:
                    neg();
                    break;
                case PRINTREAL:
                    printf();
                    break;
                case PRINTINT:
                    printint();
                    break;
                case PRINTBOOL:
                    printbool();
                    break;
                case PRINTCHR:
                    printchar();
                    break;
                case PRINTLN:
                    System.out.println();
                    break;
                case EQL:
                    eql();
                    break;
                case FEQL:
                    feql();
                    break;
                case NEQL:
                    neql();
                    break;
                case FNEQL:
                    fneql();
                    break;
                case GTR:
                    gtr();
                    break;
                case FGTR:
                    fgtr();
                    break;
                case LSS:
                    less();
                    break;
                case FLSS:
                    fless();
                    break;
                case LEQ:
                    leq();
                    break;
                case FLEQ:
                    fleq();
                    break;
                case GEQ:
                    geq();
                    break;
                case FGEQ:
                    fgeq();
                    break;
                case HALT:
                    halt();
                    break;

                default:
                    throw new Error(
                            String.format("Runtime Error: illehal instruction: unhandled case : \'%s\'", opCode));
            }
        }
    }

    public static void myPush() {
        dp = getNext4Bytes();
        // System.out.println("Var data address: "+dp); //Uncomment to debug
        stack.push(readData(dp));
    }

    public static Object myPOP() { // Assign value to dataArray. POP data on the stack to dataArray, argument is a
        // 32-bit integer denoting memory address of a variable.

        Object value = stack.pop(); // pop 4 bytes value from the top of stack.
        dp = getNext4Bytes(); // move data pointer.

        byte[] bytes;
        if (value instanceof Integer) { // Allocate value to 4 bytes space.
            bytes = ByteBuffer.allocate(4).putInt((int) value).array(); // int
        } else {
            bytes = ByteBuffer.allocate(4).putFloat((float) value).array(); // float
        }

        for (byte b : bytes) {// Write each byte to reserved place.
            dataArray[dp++] = b;
        }

        return value;
    }

    private static void neg() {
        int val = -(int) stack.pop();
        stack.push(val);
    }

    private static void fneg() {
        float val = -popFloat();
        stack.push(val);
    }

    public static void pushi() {
        int val = getNext4Bytes();
        stack.push(val);
    }

    private static void pushf() {
        float val = getFloatValue();
        stack.push(val);
    }

    private static void not() {
        Object val = stack.pop();
        int val2;
        if (val instanceof Integer) {
            val2 = (Integer) val;
            if (val2 == 0) {
                val2 = 1;
            } else {
                val2 = 0;
            }
        } else if (val.toString().equals("false")) {
            val2 = 1;
        } else {
            val2 = 0;
        }
        stack.push(val2);
    }

    private static void or() {
        Object val1 = stack.pop();
        Object val2 = stack.pop();
        boolean val3;
        boolean val4;
        if (val1 instanceof Integer) {
            val3 = ((val1.equals(1)) ? true : false);
        } else {
            val3 = (Boolean) val1;
        }

        if (val2 instanceof Integer) {
            val4 = ((val2.equals(1)) ? true : false);
        } else {
            val4 = (Boolean) val2;
        }
        stack.push((val3 || val4) ? 1 : 0);
    }

    private static void and() {
        Object val1 = stack.pop();
        Object val2 = stack.pop();
        boolean val3;
        boolean val4;
        if (val1 instanceof Integer) {
            val3 = ((val1.equals(1)) ? true : false);
        } else {
            val3 = (Boolean) val1;
        }

        if (val2 instanceof Integer) {
            val4 = ((val2.equals(1)) ? true : false);
        } else {
            val4 = (Boolean) val2;
        }
        stack.push((val3 && val4) ? 1 : 0);
    }

    private static void xor() {
        Object val1 = stack.pop();
        Object val2 = stack.pop();
        boolean val3;
        boolean val4;
        if (val1 instanceof Integer) {
            val3 = ((val1.equals(1)) ? true : false);
        } else {
            val3 = (Boolean) val1;
        }

        if (val2 instanceof Integer) {
            val4 = ((val2.equals(1)) ? true : false);
        } else {
            val4 = (Boolean) val2;
        }
        stack.push((val3 ^ val4) ? 1 : 0);
    }

    private static void get() {
        dp = (int) stack.pop();
        stack.push(readData(dp));
    }

    private static Object put() {
        Object val = stack.pop(); // pop value
        dp = (int) stack.pop(); // pop address
        // System.out.println(dp);
        byte[] valBytes;
        if (val instanceof Integer) {
            valBytes = ByteBuffer.allocate(4).putInt((int) val).array();
        } else {
            valBytes = ByteBuffer.allocate(4).putFloat((float) val).array();
        }

        for (byte b : valBytes) {
            dataArray[dp++] = b;
        }

        return val;
    }

    public static void jmp() {
        ip = getNext4Bytes();
    }

    private static void jfalse() {
        Object val = stack.pop();
        if (val instanceof Integer) {
            int val2 = (Integer) val;
            if (val2 == 0) {
                ip = getNext4Bytes();
            } else {
                getNext4Bytes(); // skip next 4 bytes
            }
        } else {
            if (val.toString().equals("false")) {
                ip = getNext4Bytes();
            } else {
                getNext4Bytes();
            }
        }
    }

    public static void halt() {
        System.out.print("\nProgram finished with exit code 0\n");
        System.exit(0);
    }

    private static void jtrue() {
        Object val = stack.pop();
        if (val instanceof Integer) {
            int val2 = (Integer) val;
            if (val2 == 1) {
                ip = getNext4Bytes();
            } else {
                getNext4Bytes();
            }
        } else {
            if (val.toString().equals("true")) {
                ip = getNext4Bytes();
            } else {
                getNext4Bytes();
            }
        }
    }

    private static void eql() {
        Integer val1 = (Integer) stack.pop();
        Integer val2 = (Integer) stack.pop();

        stack.push(val2.equals(val1));
    }

    private static void feql() {
        Float val1 = popFloat();
        Float val2 = popFloat();

        stack.push(val2.equals(val1));
    }

    private static void neql() {
        Integer val1 = (Integer) stack.pop();
        Integer val2 = (Integer) stack.pop();

        stack.push(!val2.equals(val1));
    }

    private static void fneql() {
        Float val1 = popFloat();
        Float val2 = popFloat();

        stack.push(!val2.equals(val1));
    }

    private static void less() {
        Integer val1 = (Integer) stack.pop();
        Integer val2 = (Integer) stack.pop();

        stack.push(val2 < val1);
    }

    private static void fless() {
        Float val1 = popFloat();
        Float val2 = popFloat();

        stack.push(val2 < val1);
    }

    private static void gtr() {
        Integer val1 = (Integer) stack.pop();
        Integer val2 = (Integer) stack.pop();

        stack.push(val2 > val1);
    }

    private static void fgtr() {
        Float val1 = popFloat();
        Float val2 = popFloat();

        stack.push(val2 > val1);
    }

    private static void leq() {
        Integer val1 = (Integer) stack.pop();
        Integer val2 = (Integer) stack.pop();

        stack.push(val2 <= val1);
    }

    private static void fleq() {
        Float val1 = popFloat();
        Float val2 = popFloat();

        stack.push(val2 <= val1);
    }

    private static void geq() {
        Integer val1 = (Integer) stack.pop();
        Integer val2 = (Integer) stack.pop();

        stack.push(val2 >= val1);
    }

    private static void fgeq() {
        Float val1 = popFloat();
        Float val2 = popFloat();

        stack.push(val2 >= val1);
    }

    public static void printint() {
        System.out.print(stack.pop());

    }

    private static void printf() {
        Float val = popFloat();
        System.out.print(val);
    }

    public static void printchar() {
        System.out.print(Character.toChars((Integer) stack.pop())[0]);
    }

    private static void printbool() {
        int val = (int) stack.pop();
        if (val == 1) {
            System.out.print("TRUE");
        } else {
            System.out.print("FALSE");
        }
    }

    public static void add() {
        int val1 = (int) stack.pop();
        int val2 = (int) stack.pop();
        stack.push(val1 + val2);
    }

    private static void fadd() { // get bytes array(conveted to int) from stak<Object>, cast value to int,
                                 // convert to float.
        float val1 = popFloat();
        float val2 = popFloat();
        stack.push(val1 + val2);
    }

    public static void sub() {
        int val2 = (int) stack.pop();
        int val1 = (int) stack.pop();
        stack.push(val1 - val2);
    }

    public static void fsub() {
        float val2 = popFloat();
        float val1 = popFloat();
        stack.push(val1 - val2);
    }

    public static void mult() {
        int val1 = (int) stack.pop();
        int val2 = (int) stack.pop();
        stack.push(val1 * val2);
    }

    public static void fmult() {
        float val1 = popFloat();
        float val2 = popFloat();
        stack.push(val1 * val2);
    }

    public static void fdiv() {
        float val2 = popFloat();
        float val1 = popFloat();

        stack.push(val1 / val2);
    }

    public static void div() {
        int val2 = (int) stack.pop();
        int val1 = (int) stack.pop();
        stack.push(val1 / val2);
    }

    public static void cvr() {
        float val = Float.parseFloat(String.valueOf(stack.pop()));
        stack.push(val);
    }

    public static void xchg() {
        Object val1 = stack.pop();
        Object val2 = stack.pop();
        stack.push(val1);
        stack.push(val2);
    }

    public static int getNext4Bytes() {
        byte[] valArray = new byte[4];
        for (int i = 0; i < 4; i++) {
            valArray[i] = codeArray[ip++]; // read 4 bytes from instuctions
        }

        return ByteBuffer.wrap(valArray).getInt();
    }

    public static float getFloatValue() {
        byte[] valArray = new byte[4];
        for (int i = 0; i < 4; i++) {
            valArray[i] = codeArray[ip++];
        }

        return ByteBuffer.wrap(valArray).getFloat();
    }

    public static int readData(int dp) { // read value from dataArray as int.
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            // System.out.println(dataArray[dp]); //Uncoment to debug, print bytes data in
            // data array
            if (dataArray[dp] != null) {
                bytes[i] = dataArray[dp++];
            } else {
                throw new Error("Runtime Error: Haven't assign value to varibale yet.");
            }

        }
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static float popFloat() {
        Object val = stack.pop();
        if (val instanceof Integer) {
            byte[] bytes = ByteBuffer.allocate(4).putInt((int) val).array();
            return ByteBuffer.wrap(bytes).getFloat();
        } else {
            return (float) val;
        }
    }

    public static Parser.OP_CODE getOpCode() {
        return Parser.OP_CODE.values()[codeArray[ip++]];
    }

    public static void setOpCode(Byte[] instructions) {
        OpCodeStackMachine.codeArray = instructions;
    }

}
