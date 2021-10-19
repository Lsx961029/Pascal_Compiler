public class SymbolTable {
    private static Scope mainScope = new Scope();

    private static final int HASH_TABLE_SIZE = 101;

    static class Scope {
        Symbol[] symbolTable = new Symbol[HASH_TABLE_SIZE]; // symbol table for the current scope
        Scope next = null; // pointer to the next outer scope

        public void print() { // method to print symbol table
            System.out.println("Symbol Table:");
            for (int i = 0; i < HASH_TABLE_SIZE; i++) {
                System.out.print(i + "  ");
                Symbol symbol = symbolTable[i];
                while (symbol != null) {
                    System.out.print(symbol.getName() + " ");
                    symbol = symbol.next;
                }
                System.out.println();
            }
        }
    }

    public static void insert(Symbol symbol) {
        int hashValue = hash(symbol.getName());

        Symbol bucketCursor = mainScope.symbolTable[hashValue];
        if (bucketCursor == null) {// Empty
            mainScope.symbolTable[hashValue] = symbol;
        } else {
            while (bucketCursor.next != null) {// not empty
                bucketCursor = bucketCursor.next;
            }
            bucketCursor.next = symbol; // append to last
        }
    }

    public static Symbol find_symbol_by_name(String symbolName) {
        int hashValue = hash(symbolName); // hash symbol name
        Symbol bucket = mainScope.symbolTable[hashValue];
        Scope scope = mainScope;

        while (scope != null) {
            while (bucket != null) {
                if (bucket.getName().equals(symbolName)) {
                    return bucket;
                } else {
                    bucket = bucket.next; // next symbol in this bucket
                }
            }
            scope = scope.next; // not in this scope
        }

        return null;// this symbol name is not in the symbol table
    }

    public static int hash1(String symbolName) {// hash algorithm 1
        long sum = 0, mul = 1;
        for (int i = 0; i < symbolName.length(); i++) {
            mul = (i % 4 == 0) ? 1 : mul * 256;
            sum += symbolName.charAt(i) * mul;
        }
        return (int) (Math.abs(sum) % HASH_TABLE_SIZE);
    }

    public static int hash2(String symbolName) { // hash algorithm 2
        char ch[];
        ch = symbolName.toCharArray();

        int i, sum;
        for (sum = 0, i = 0; i < symbolName.length(); i++)
            sum += ch[i];
        return sum % HASH_TABLE_SIZE;
    }

    public static int hash(String symbolName) { // hash algorithm 3
        int h = 0;
        for (int i = 0; i < symbolName.length(); i++) {
            h = h + h + symbolName.charAt(i);
        }

        h = h % HASH_TABLE_SIZE;

        return h;
    }
}
