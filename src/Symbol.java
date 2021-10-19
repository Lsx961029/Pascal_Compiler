import java.util.ArrayList;

public class Symbol {
    private String name = ""; // key for searching
    private String tokenName = "";
    private Parser.DATATYPE dataType = null;
    private int address;
    Symbol next; // pointer to next symbol in the same bucket
    
    //label:
    private boolean seen = false;
    private ArrayList<Integer> gotolist = new ArrayList<>();

    //array:
    private boolean stat = true; //
    private String flags = "";
    private Parser.DATATYPE componentType; // limiting to ordinal type(int, char,bool, enum) or real.
    private Parser.DATATYPE indexType; // index type - orinal type(int, char, bool, enum)

    private Object low; // low limit of range
    private Object high; // high limit of range

    public Symbol(String name, String tokenType, Parser.DATATYPE dataType, int address) {
        this.name = name;
        this.tokenName = tokenType;
        this.dataType = dataType;
        this.address = address;
    }

    public ArrayList<Integer> getHole() {
        return gotolist;
    }

    public void setHole(int hole_address) {
        this.gotolist.add(hole_address);
    }

    public boolean getSeen() {
        return seen;
    }

    public void setSeen(boolean b) {
        this.seen = b;
    }

    public String getName() {
        return name;
    }

    public Parser.DATATYPE getDataType() {
        return dataType;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public Object getLow() {
        return low;
    }

    public void setLow(Object low) {
        this.low = low;
    }

    public Object getHigh() {
        return high;
    }

    public void setHigh(Object high) {
        this.high = high;
    }

    public Parser.DATATYPE getIndexType() {
        return indexType;
    }

    public void setIndexType(Parser.DATATYPE indexType) {
        this.indexType = indexType;
    }

    public Parser.DATATYPE getComponentType() {
        return componentType;
    }

    public void setValueType(Parser.DATATYPE valueType) {
        this.componentType = valueType;
    }
}
