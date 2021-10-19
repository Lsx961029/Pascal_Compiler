import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.nio.ByteBuffer;

public final class Parser {

    private static Token currentToken;

    private static Iterator<Token> iterator; // Token list iterator.

    private static final int INSTRUCTION_SIZE = 10000;

    private static Byte[] codeArray = new Byte[INSTRUCTION_SIZE];

    private static int dp = 0; // Data pointer, only for allocation during compilation

    private static final int ADDRESS_SIZE = 4; // Each address contains 4 bytes.

    private static int IP = 0; // Instruction pointer.

    enum DATATYPE {
        INT, CHAR, BOOL, ENUM, REAL, ARR, STR/* var */, PTR, REC, SET, PROC, FUNC, FILE, CLASS, LABEL // RECORD(STRUCT)
    }

    enum OP_CODE {
        PUSH, POP, PUSHI, PUSHF, JMP, JFALSE, JTRUE, JTAB, CALL, RETURN, CVR, CVI, DUP, XCHG, REMOVE, ADD, SUB, MULT,
        DIV, NEG, NOT, OR, XOR, AND, FADD, FSUB, FMULT, FDIV, FNEG, EQL, FEQL, NEQL, FNEQL, GEQ, FGEQ, LEQ, FLEQ, GTR,
        FGTR, LSS, FLSS, HALT, PRINTINT, PRINTCHR, PRINTLN, PRINTBOOL, PRINTREAL, GET, GET1, GET2, GET4, PUT, PUT1,
        PUT2, PUT4, ABS, FABS, FSIN, BOUND// check array range,
    }// 256 OP_CODES LIMIT

    private static final HashMap<String, DATATYPE> DECLARATION_TYPE;

    static {
        DECLARATION_TYPE = new HashMap<>();
        DECLARATION_TYPE.put("integer", DATATYPE.INT);
        DECLARATION_TYPE.put("real", DATATYPE.REAL);
        DECLARATION_TYPE.put("boolean", DATATYPE.BOOL);
        DECLARATION_TYPE.put("char", DATATYPE.CHAR);
        DECLARATION_TYPE.put("string", DATATYPE.STR);
        DECLARATION_TYPE.put("array", DATATYPE.ARR);
    }

    public static void setTokenArrayListIterator(ArrayList<Token> tokenArrayList) { // make token Array a Iterator.
        iterator = tokenArrayList.iterator();
    }

    public static Byte[] compile() {
        getToken(); // Get first token
        header();
        declarations();
        // SymbolTable.getMainrScope().print(); // Print main scope symbol table
        begin_statement();
        match("TK_DOT");
        match("TK_EOF");
        emit_opcode(OP_CODE.HALT);

        return codeArray;
    }

    public static void header() { // omit header.
        return;
    }

    public static void getToken() { // get next toke name.
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            //System.out.println("Working on Token:" + currentToken.getTokenName());
        }
    }

    public static void match(String t) { // match current token name and move to next.
        if (!t.equals(currentToken.getTokenName())) {
            throw new Error(String.format(
                    "Token type \"%s\" does not match current token type \"%s\", rows: \"%s\" cols: \"%s\"", t,
                    currentToken.getTokenName(), currentToken.getRow(), currentToken.getCol()));
        } else {
            getToken(); // get next token;
        }
    }

    public static void begin_statement() {
        match("TK_BEGIN");
        statements();
        if (currentToken.getTokenName() == "TK_END") {
            match("TK_END");
        } else {
            throw new Error(String.format("Program stop at token \"%s\".", currentToken.getTokenName()));
        }

    }

    /*
     * <declarations> ⇒ <declaration> <declarations> | \epsilon
     */

    /*
     * <declaration> ⇒ <label declaration> ; | <var declaration> ; | <const
     * declaration> ; | < type declaration> ; | <procedure declaration> ; |
     * <function declaration> ; | ;
     */
    public static void declarations() {
        while (true) {
            switch (currentToken.getTokenName()) {
                case "TK_VAR":
                    var_declarration();
                    break;

                case "TK_LABEL":
                    label_declaration();
                    break;

                case "TK_CONST":
                    const_declaration(); // not handeled yet
                    break;

                case "TK_PROCEDURE":
                    procedure_declaration(); // not handeled yet
                    break;

                case "TK_TYPE":
                    type_declaration(); // not handeled yet
                    break;

                case "TK_FUNCTION": // empty delcaration end with ";".
                    function_declaration();
                    break;

                case "TK_SEMI_COLON": // empty delcaration end with ";".
                    match("TK_SEMI_COLON");
                    break;

                default: // no declaration anymore.
                    return;
            }
        }
    }

    public static void function_declaration() {
        throw new Error("function delcaration has not been handeled yet");
    }

    public static void type_declaration() {
        throw new Error("type delcaration has not been handeled yet");
    }

    public static void const_declaration() {
        throw new Error("procedure delcaration has not been handeled yet");
    }

    public static void procedure_declaration() {
        throw new Error("procedure delcaration has not been handeled yet");
    }

    public static void pointer_declaration(ArrayList<Token> pointerList) {
        match("TK_HAT");

        String dataType = currentToken.getTokenName();// read type declaration

        for (Token pointer : pointerList) {

            Symbol symbol = new Symbol(pointer.getTokenValue(), "TK_A_POINTER",
                    DECLARATION_TYPE.get(dataType.toLowerCase().substring(3)), dp);

            dp += 4;

            if (SymbolTable.find_symbol_by_name(pointer.getTokenValue()) == null) {
                SymbolTable.insert(symbol);
            } else {
                throw new Error(String.format("Duplicate identifier \"%s\"", symbol.getName()));
            }
        }
        match(dataType);
    }

    /*
     * <variable declaration part> ::= <empty> | var <variable declaration> {;
     * <variable declaration>} ; <variable declaration> ::= <identifier>
     * {,<identifier>} : <type>
     */
    public static void var_declarration() {
        match("TK_VAR");
        while (true) {
            ArrayList<Token> variablesList = new ArrayList<>(); // temporary token list

            while ("TK_UNKNOWN".equals(currentToken.getTokenName())) { // read all unknowns
                currentToken.setTokenName("TK_A_VAR");

                variablesList.add(currentToken);

                match("TK_A_VAR");

                if (currentToken.getTokenName().equals("TK_COMMA")) {
                    match("TK_COMMA");
                }
            }

            if (currentToken.getTokenName().equals("TK_COLON")) {
                match("TK_COLON");
            } else {
                throw new Error(
                        String.format("Syntax Error: No colon found for variable \"%s\" declaration. rows: %s cols %s",
                                currentToken.getTokenName(), currentToken.getRow(), currentToken.getCol()));
            }
            if (currentToken.getTokenName().equals("TK_HAT")) {
                pointer_declaration(variablesList);
            } else {
                String dataType = currentToken.getTokenName();// read type declaration
                
                for (Token var : variablesList) {// go through the list and enter all variables into the symbol table

                    Symbol symbol = new Symbol(var.getTokenValue(), "TK_A_VAR",
                            DECLARATION_TYPE.get(dataType.toLowerCase().substring(3)), dp); // substring(3) to skip
                                                                                            // "TK_"

                    dp += 4; // compute the size based on type

                    if (SymbolTable.find_symbol_by_name(var.getTokenValue()) == null) {
                        SymbolTable.insert(symbol);
                    } else {
                        throw new Error(String.format("Duplicate identifier \"%s\"", symbol.getName()));
                    }
                }
                match(dataType);
                if (dataType.equals("TK_ARRAY")) {
                    array_declaration(variablesList);
                }
            }
            
            match("TK_SEMI_COLON"); // end one line of declaration
            if (!currentToken.getTokenName().equals("TK_UNKNOWN")) {
                break;
            }
        }
    }

    /*
     * <structured type> ::= <array type> | <record type> | <set type> | <file type>
     * <array type> ::= array [<index type>{,<index type>}] of <component type>
     * <index type> ::= <simple type> <component type> ::= <type> 199<simple type>
     * ::= <scalar type> | <subrange type> | <type identifier>
     */
    private static void array_declaration(ArrayList<Token> variablesArrayList) {
        match("TK_LB");

        String index1 = currentToken.getTokenValue();
        DATATYPE indexType1 = getLitDataType(currentToken.getTokenName()); // cant use F(), cant make changes on stack
        match(currentToken.getTokenName());

        match("TK_RANGE");

        String index2 = currentToken.getTokenValue();
        DATATYPE indexType2 = getLitDataType(currentToken.getTokenName());
        match(currentToken.getTokenName());
        match("TK_RB");
        match("TK_OF");

        String componentType = currentToken.getTokenName();
        match(componentType);
        if (indexType1 != null && indexType2 != null) {
            if (indexType1 != indexType2) {
                throw new Error(String.format("Array index LHS type \"%s\" doesn't match RHS type: \"%s\"", indexType1,
                        indexType2));
            } else {
                switch (indexType1) {
                    case INT:
                        int low = Integer.valueOf(index1);
                        int high = Integer.valueOf(index2);
                        if (low > high) {
                            throw new Error(String.format("Invalid range: %s..%s. rows: %s cols: %s.", low, high,
                                    currentToken.getRow(), currentToken.getCol()));
                        }

                        Symbol firstArray = SymbolTable.find_symbol_by_name(variablesArrayList.get(0).getTokenValue());
                        if (firstArray != null) {
                            dp = firstArray.getAddress();
                        }

                        for (Token var : variablesArrayList) {
                            Symbol symbol = SymbolTable.find_symbol_by_name(var.getTokenValue());
                            if (symbol != null) {

                                int typeSize = 4;
                                int totalSize = typeSize * (high - low + 1);

                                symbol.setAddress(dp);
                                symbol.setLow(low);
                                symbol.setHigh(high);
                                symbol.setTokenName("TK_AN_ARRAY");
                                symbol.setIndexType(DATATYPE.INT);
                                symbol.setValueType(DECLARATION_TYPE.get(componentType.toLowerCase().substring(3)));
                                dp += totalSize;
                            }
                        }
                        break;

                    case CHAR:
                        char lowChar = index1.toCharArray()[0];
                        char highChar = index2.toCharArray()[0];
                        if (lowChar > highChar) {
                            throw new Error(String.format("Invalid range: %s..%s. rows: %s cols: %s.", lowChar,
                                    highChar, currentToken.getRow(), currentToken.getCol()));
                        }

                        Symbol firstCharArray = SymbolTable
                                .find_symbol_by_name(variablesArrayList.get(0).getTokenValue());
                        if (firstCharArray != null) {
                            dp = firstCharArray.getAddress();
                        }

                        for (Token var : variablesArrayList) {
                            Symbol symbol = SymbolTable.find_symbol_by_name(var.getTokenValue());
                            if (symbol != null) {

                                int typeSize = 4;
                                int totalSize = typeSize * (highChar - lowChar + 1);

                                symbol.setAddress(dp);
                                symbol.setLow(lowChar);
                                symbol.setHigh(highChar);
                                symbol.setTokenName("TK_AN_ARRAY");
                                symbol.setIndexType(DATATYPE.CHAR);
                                symbol.setValueType(DECLARATION_TYPE.get(componentType.toLowerCase().substring(3)));

                                dp += totalSize;
                            }
                        }

                        break;

                    default:
                        throw new Error(String.format("Index type %s of array is invalid. rows: %s cols: %s.",
                                indexType1, currentToken.getRow(), currentToken.getCol()));
                }

            }
        } else {
            throw new Error(String.format("Unknown index type. rows: %s cols: %s.", currentToken.getRow(),
                    currentToken.getCol()));
        }

    }

    /*
     * <label declaration part> ::= <empty> | label <label> {, <label>} ; <label>
     * ::= <unsigned integer>
     */
    private static void label_declaration() {
        match("TK_LABEL");
        while (true) {
            ArrayList<Token> labelsList = new ArrayList<>();

            while ("TK_UNKNOWN".equals(currentToken.getTokenName())) {
                currentToken.setTokenName("TK_A_LABEL");

                labelsList.add(currentToken);

                match("TK_A_LABEL");

                if ("TK_COMMA".equals(currentToken.getTokenName())) {
                    match("TK_COMMA");
                }
            }

            for (Token label : labelsList) {
                Symbol symbol = new Symbol(label.getTokenValue(), "TK_A_LABEL", DATATYPE.LABEL, 0);

                if (SymbolTable.find_symbol_by_name(label.getTokenValue()) == null) {
                    SymbolTable.insert(symbol);
                } else {
                    throw new Error(String.format("Duplicate identifier \"%s\"", symbol.getName()));
                }
            }

            match("TK_SEMI_COLON");

            if ("TK_LABEL".equals(currentToken.getTokenName())) {
                match("TK_LABEL");
            } else {
                break;
            }
        }
    }

    public static void statements() {
        while (true) {
            switch (currentToken.getTokenName()) {

                case "TK_UNKNOWN": // if token name == "TK_UNKNOWN"
                    Symbol symbol = SymbolTable.find_symbol_by_name(currentToken.getTokenValue());
                    if (symbol != null) {
                        currentToken.setTokenName(symbol.getTokenName()); // set TK_UNKNOWN to correspeding token name
                                                                          // TK_A_VAR, TK_A_LABEL....
                    } else {
                        throw new Error(String.format(
                                "Can't assign value to var \"%s\" without declaration.Symbol not found in the symbol table. rows: %s cols: %s",
                                currentToken.getTokenValue(), currentToken.getRow(), currentToken.getCol()));
                    }
                    break;

                case "TK_A_VAR":
                    assignment();
                    break;

                case "TK_WRITELN": // TK_WRITELN is not a keyword, we implement it as keyword for now.
                    writeln_statement();
                    break;

                case "TK_SEMI_COLON":// empty statement.
                    match("TK_SEMI_COLON");
                    break;

                case "TK_REPEAT":
                    repeat_statement();
                    break;

                case "TK_WHILE":
                    while_statement();
                    break;

                case "TK_IF":
                    if_statement();
                    break;

                case "TK_FOR":
                    for_statement();
                    break;

                case "TK_GOTO":
                    goto_statement();
                    break;

                case "TK_A_LABEL":
                    process_a_label();
                    break;

                case "TK_AN_ARRAY":
                    array_assignment();
                    break;

                case "TK_CASE": // not handel yet
                    case_statement();
                    break;

                case "TK_A_POINTER":
                    pointer_assignment();
                    break;

                default:
                    return;
            }
        }

    }

    public static void assignment() {
        Symbol symbol = SymbolTable.find_symbol_by_name(currentToken.getTokenValue());

        if (symbol != null) {
            DATATYPE tLHS = symbol.getDataType();

            match("TK_A_VAR");

            match("TK_ASSIGN");

            DATATYPE tRHS = E();
            if (tLHS == tRHS) {
                emit_opcode(OP_CODE.POP);
                emit_int(symbol.getAddress());
            } else {
                throw new Error(String.format("Type Error: can't assign type \"%s\" to \"%s\". ros: %s cols: %s", tRHS,
                        tLHS, currentToken.getRow(), currentToken.getCol()));
            }
        } else {
            throw new Error(String.format(
                    "Can't assign value to var \"%s\" without declaration.(Symbol not found in the symbol table.)",
                    currentToken.getTokenValue()));
        }
    }

    public static void pointer_assignment() {
        Symbol symbol = SymbolTable.find_symbol_by_name(currentToken.getTokenValue());
        if (symbol != null) {
            match("TK_A_POINTER");
            DATATYPE tLHS = symbol.getDataType();
            if (currentToken.getTokenName().equals("TK_ASSIGN")) { // reference
                match("TK_ASSIGN");
                match("TK_REFERENCE");
                Symbol symbolRHS = SymbolTable.find_symbol_by_name(currentToken.getTokenValue());
                DATATYPE tRHS = symbolRHS.getDataType();
                if (tLHS == tRHS) {
                    symbol.setAddress(symbolRHS.getAddress());
                    /*
                     * emit_opcode(OP_CODE.PUSHI); emit_int(symbolRHS.getAddress());
                     * emit_int(OP_CODE.POP); emit_int(symbol.getAddress());
                     */
                } else {
                    throw new Error(String.format("Type Error: can't assign type \"%s\" to \"%s\". ros: %s cols: %s",
                            tRHS, tLHS, currentToken.getRow(), currentToken.getCol()));
                }
                match("TK_UNKNOWN");

            } else if (currentToken.getTokenName().equals("TK_HAT")) {// dereference
                match("TK_HAT");
                match("TK_ASSIGN");
                emit_opcode(OP_CODE.PUSHI);
                emit_int(symbol.getAddress());
                DATATYPE tRHS = E();
                if (tRHS == tLHS) {
                    emit_opcode(OP_CODE.PUT);
                } else {
                    throw new Error(String.format("Type Error: can't assign type \"%s\" to \"%s\". ros: %s cols: %s",
                            tRHS, tLHS, currentToken.getRow(), currentToken.getCol()));
                }
            } else {
                throw new Error(
                        String.format("Syntax Error: ros: %s cols: %s", currentToken.getRow(), currentToken.getCol()));
            }
            match("TK_SEMI_COLON");
        } else {
            throw new Error(String.format(
                    "Can't assign value to var \"%s\" without declaration.(Symbol not found in the symbol table.)",
                    currentToken.getTokenValue()));
        }
    }

    private static void array_assignment() {// LHS use
        Symbol symbol = SymbolTable.find_symbol_by_name(currentToken.getTokenValue());
        match("TK_AN_ARRAY");
        Object lo = symbol.getLow();
        Object hi = symbol.getHigh();
        int addr = symbol.getAddress();
        DATATYPE index_type = symbol.getIndexType();
        DATATYPE component_type = symbol.getComponentType();
        int component_size = 4;
        match("TK_LB");
        DATATYPE t = E(); // dynamically push index

        // check index range here:
        // emit_opcode(OP_CODE.BOUND, lo, hi)

        if (t != index_type) {
            throw new Error(String.format("Index type doesn't match: %s != %s. rows: %s cols: %s.",
                    symbol.getIndexType(), t, currentToken.getRow(), currentToken.getCol()));
        }
        switch (t) {
            case INT: // index is int
                emit_opcode(OP_CODE.PUSHI);
                emit_int((int) lo); // push low index

                emit_opcode(OP_CODE.SUB); // index diff
                emit_opcode(OP_CODE.PUSHI);
                emit_int(component_size);

                emit_opcode(OP_CODE.MULT);// SIZE * DIFF = ADDRESS DIFF

                emit_opcode(OP_CODE.PUSHI);
                emit_int(addr);// GET ADDRESS OF FIRST INDEX

                emit_opcode(OP_CODE.ADD);// FINIAL ADDRESS OF LHS
                break;

            case CHAR:
                emit_opcode(OP_CODE.PUSHI);
                emit_int((char) lo);
                emit_opcode(OP_CODE.SUB);

                emit_opcode(OP_CODE.PUSHI);
                emit_int(addr);

                emit_opcode(OP_CODE.ADD);

                break;
        }
        match("TK_RB");
        match("TK_ASSIGN");

        t = E();// push value
        if (component_type == t) {
            emit_opcode(OP_CODE.PUT);
        } else {
            throw new Error(String.format("Type Error: can't assign type \"%s\" to \"%s\". ros: %s cols: %s", t,
                    component_type, currentToken.getRow(), currentToken.getCol()));
        }
        match("TK_SEMI_COLON");
    }

    public static void writeln_statement() {
        match("TK_WRITELN");
        match("TK_LP");
        DATATYPE t = E(); // E() returns the type of x in write(X)
        match("TK_RP");
        match("TK_SEMI_COLON");

        if (t != null) {
            switch (t) {
                case REAL:
                    emit_opcode(OP_CODE.PRINTREAL);
                    emit_opcode(OP_CODE.PRINTLN);
                    break;

                case INT:
                    emit_opcode(OP_CODE.PRINTINT);
                    emit_opcode(OP_CODE.PRINTLN);
                    break;

                case BOOL:
                    emit_opcode(OP_CODE.PRINTBOOL);
                    emit_opcode(OP_CODE.PRINTLN);
                    break;

                case CHAR:
                    emit_opcode(OP_CODE.PRINTCHR);
                    emit_opcode(OP_CODE.PRINTLN);
                    break;

                default:
                    throw new Error("Unsupported type");
            }
        } else {
            throw new Error("E() returns null");
        }
    }

    public static DATATYPE getLitDataType(String tokenType) { // used in array declaration
        switch (tokenType) {
            case "TK_INTLIT":
                return DATATYPE.INT;
            case "TK_REALLIT":
                return DATATYPE.REAL;
            case "TK_CHARLIT":
                return DATATYPE.CHAR;
            case "TK_BOOLLIT":
                return DATATYPE.BOOL;
            default:
                return null;
        }
    }

    public static void emit_opcode(OP_CODE b) {
        codeArray[IP++] = (byte) (b.ordinal()); // convert to signed byte -128~127
    }

    public static void emit_int(int a) {
        byte[] intBytes = ByteBuffer.allocate(ADDRESS_SIZE).putInt(a).array(); // allocate int to byte array[4]

        for (byte b : intBytes) {
            codeArray[IP++] = b;
        }
    }

    public static void addAddress(float a) {
        byte[] intBytes = ByteBuffer.allocate(ADDRESS_SIZE).putFloat(a).array(); // allocate float to byte array[4]

        for (byte b : intBytes) {
            codeArray[IP++] = b;
        }
    }

    // E -> TE'
    // E' -> +T[ADD]E'|-T[SUB]E'|or T[OR]E'|\epsilon
    public static DATATYPE E() {
        DATATYPE t1, t2;
        t1 = T();
        while (currentToken.getTokenName().equals("TK_PLUS") || currentToken.getTokenName().equals("TK_MINUS")
                || currentToken.getTokenName().equals("TK_XOR") || currentToken.getTokenName().equals("TK_OR")) {
            String operator = currentToken.getTokenName();
            match(operator);
            t2 = T();

            t1 = do_binary_operation(operator, t1, t2);
        }

        return t1;
    }

    // T -> FT'
    // T' -> *F[MUL]T|/T[DIVIDE]F|mod F[MOD]T|and F[AND]T|div F[DIV]T|\epsilon
    public static DATATYPE T() {
        DATATYPE f1, f2;
        f1 = F();
        while (currentToken.getTokenName().equals("TK_MULTIPLY") || currentToken.getTokenName().equals("TK_DIVIDE")
                || currentToken.getTokenName().equals("TK_DIV") || currentToken.getTokenName().equals("TK_MOD")
                || currentToken.getTokenName().equals("TK_AND")) {
            String operator = currentToken.getTokenName();
            match(operator);
            f2 = F();

            f1 = do_binary_operation(operator, f1, f2);
        }
        return f1;
    }

    // F -> +F[NOP]|-F[NEG]|not F[NOT]|(E)|LIT[LIT]
    public static DATATYPE F() {
        switch (currentToken.getTokenName()) {
            case "TK_PLUS":
                match("TK_PLUS");
                return F();

            case "TK_MINUS":
                match("TK_MINUS");
                DATATYPE t1 = F();
                return do_unary_operation("TK_MINUS", t1);

            case "TK_NOT":
                match("TK_NOT");
                DATATYPE t2 = F();
                emit_opcode(OP_CODE.NOT);
                return t2;

            case "TK_UNKNOWN": // F returns a variable
                Symbol symbol = SymbolTable.find_symbol_by_name(currentToken.getTokenValue());
                if (symbol != null) {
                    if (symbol.getTokenName().equals("TK_A_VAR")) {
                        // variable
                        currentToken.setTokenName("TK_A_VAR");

                        emit_opcode(OP_CODE.PUSH);
                        emit_int(symbol.getAddress());

                        match("TK_A_VAR");
                        return symbol.getDataType();

                    } else if (symbol.getTokenName().equals("TK_AN_ARRAY")) {// RHS use

                        currentToken.setTokenName("TK_AN_ARRAY");
                        match("TK_AN_ARRAY");
                        Object lo = symbol.getLow();
                        Object hi = symbol.getHigh();
                        int addr = symbol.getAddress();
                        DATATYPE index_type = symbol.getIndexType();
                        DATATYPE component_type = symbol.getComponentType();
                        int component_size = 4;

                        match("TK_LB");
                        DATATYPE t = E(); // dynamically push index
                        // check index range here:
                        // emit_opcode(OP_CODE.BOUND, lo, hi)

                        if (t != index_type) {
                            throw new Error(String.format("Index type doesn't match: %s != %s. rows: %s cols: %s.",
                                    symbol.getIndexType(), t, currentToken.getRow(), currentToken.getCol()));
                        }

                        switch (t) {
                            case INT: // index is int
                                emit_opcode(OP_CODE.PUSHI);
                                emit_int((int) lo); // push low
                                emit_opcode(OP_CODE.SUB); // index diff

                                emit_opcode(OP_CODE.PUSHI);
                                emit_int(component_size);

                                emit_opcode(OP_CODE.MULT);// SIZE * DIFF = ADDRESS DIFF

                                emit_opcode(OP_CODE.PUSHI);
                                emit_int(addr);// GET ADDRESS OF FIRST INDEX

                                emit_opcode(OP_CODE.ADD);// FINIAL ADDRESS OF LHS
                                break;

                            case CHAR:// index is char
                                emit_opcode(OP_CODE.PUSHI);
                                emit_int((char) lo);
                                emit_opcode(OP_CODE.SUB);

                                emit_opcode(OP_CODE.PUSHI);
                                emit_int(addr);

                                emit_opcode(OP_CODE.ADD);

                                break;
                        }
                        match("TK_RB");

                        emit_opcode(OP_CODE.GET);
                        return component_type;

                    } else if (symbol.getTokenName().equals("TK_A_POINTER")) { // Pointer RHS
                        currentToken.setTokenName("TK_A_POINTER");
                        int addr = symbol.getAddress();
                        DATATYPE dataType = symbol.getDataType();
                        match("TK_A_POINTER");
                        if (currentToken.getTokenName().equals("TK_HAT")) {// Dereference
                            emit_opcode(OP_CODE.PUSHI);
                            emit_int(addr);
                            emit_opcode(OP_CODE.GET);
                            match("TK_HAT");
                            return dataType;
                        } else {
                            emit_opcode(OP_CODE.PUSHI);
                            emit_int(addr);
                            return DATATYPE.INT;
                        }
                    }
                } else {
                    throw new Error(String.format("Symbol \"%s\" not found in the symbol table. rows: %s cols: %s",
                            currentToken.getTokenValue(), currentToken.getRow(), currentToken.getCol()));
                }

            case "TK_INTLIT":// push int
                emit_opcode(OP_CODE.PUSHI);
                emit_int(Integer.valueOf(currentToken.getTokenValue())); // put int literal value to instruction.

                match("TK_INTLIT");
                return DATATYPE.INT;

            case "TK_REALLIT":// push float
                emit_opcode(OP_CODE.PUSHF);
                addAddress(Float.valueOf(currentToken.getTokenValue()));

                match("TK_REALLIT");
                return DATATYPE.REAL;

            case "TK_TRUE":
                emit_opcode(OP_CODE.PUSHI);
                emit_int(1);
                match("TK_TRUE");
                return DATATYPE.BOOL;

            case "TK_FALSE":
                emit_opcode(OP_CODE.PUSHI);
                emit_int(0);
                match("TK_FALSE");
                return DATATYPE.BOOL;

            case "TK_CHARLIT":// push char ascii value
                emit_opcode(OP_CODE.PUSHI);
                emit_int(currentToken.getTokenValue().charAt(0));

                match("TK_CHARLIT");
                return DATATYPE.CHAR;

            case "TK_STRLIT":// push each char in string one by one.
                for (char c : currentToken.getTokenName().toCharArray()) {
                    emit_opcode(OP_CODE.PUSHI);
                    emit_int(c);
                }

                match("TK_STRLIT");
                return DATATYPE.STR;

            case "TK_LP":
                match("TK_LP");
                DATATYPE t = E();
                match("TK_RP");
                return t;

            default:
                throw new Error(
                        String.format("Unknown token type \"%s:%s\". row: %s cols: %s", currentToken.getTokenName(),
                                currentToken.getTokenValue(), currentToken.getRow(), currentToken.getCol()));
        }
    }

    /*
     * <expression> ::= <simple expression> | <simple expression> <relational op>
     * <simple expression> <relational op> ::= = | <> | < | <= | >= | > | in <simple
     * expression> ::= <term> | <sign> <term> | <simple expression> <addingoperator>
     * <term>
     */
    public static void condition() {
        DATATYPE e1 = E();

        while (currentToken.getTokenName().equals("TK_LESS_THAN")
                || currentToken.getTokenName().equals("TK_GREATER_THAN")
                || currentToken.getTokenName().equals("TK_LESS_OR_EQUAL")
                || currentToken.getTokenName().equals("TK_GREATER_OR_EQUAL")
                || currentToken.getTokenName().equals("TK_EQUAL") || currentToken.getTokenName().equals("TK_NOT_EQUAL")
                || currentToken.getTokenName().equals("TK_IN")) {
            String operator = currentToken.getTokenName();
            match(operator);
            DATATYPE e2 = E();

            e1 = do_binary_operation(operator, e1, e2); // operator can only be comparison operator.
        }
    }

    /*
     * <repeat statement> ::= repeat <statement> {; <statement>} until <expression>
     */
    private static void repeat_statement() {
        match("TK_REPEAT"); // never fails
        int target = IP;
        statements(); // generates some balanced code
        match("TK_UNTIL");
        condition(); // compare exoression
        emit_opcode(OP_CODE.JFALSE);
        emit_int(target);
    }

    /* <while statement> ::= while <expression> do <statement> */
    private static void while_statement() {
        match("TK_WHILE"); // never fails
        int target = IP; // for JMP back
        condition();

        emit_opcode(OP_CODE.JFALSE);
        int hole = IP;
        emit_int(0);
        match("TK_DO");

        match("TK_BEGIN");
        statements();
        match("TK_END");
        match("TK_SEMI_COLON");

        emit_opcode(OP_CODE.JMP);
        emit_int(target);

        int save_IP = IP;
        IP = hole;
        emit_int(save_IP);
        IP = save_IP;

    }

    /*
     * <if statement> ::= if <expression> then <statement> | if <expression> then
     * <statement> else <statement>
     */
    public static void if_statement() {
        match("TK_IF");
        condition();
        match("TK_THEN");

        emit_opcode(OP_CODE.JFALSE);
        int hole = IP;
        emit_int(0);
        match("TK_BEGIN");
        statements();

        if (currentToken.getTokenName().equals("TK_ELSE")) { // Check if there is a else.
            emit_opcode(OP_CODE.JMP);
            int hole_else = IP;
            emit_int(0);
            int save_IP = IP;
            IP = hole; // go back to give JFALSE address
            emit_int(save_IP);
            IP = save_IP;
            hole = hole_else; // hole is the place to put the jump-to-end address.
            match("TK_ELSE");
            statements();
        }
        match("TK_END");
        match("TK_SEMI_COLON");

        int save_IP = IP;
        IP = hole;
        emit_int(save_IP);
        IP = save_IP;
    }

    /*
     * <for statement> ::= for <control variable> := <for list> do <statement>
     * <control variable> ::= <identifier> <for list> ::= <initial value> to <final
     * value> | <initial value> downto <final value> <initial value> ::=
     * <expression>
     */
    private static void for_statement() {
        match("TK_FOR"); // never fail

        String varName = currentToken.getTokenValue();
        currentToken.setTokenName("TK_A_VAR");
        assignment();
        match("TK_TO");

        Symbol symbol = SymbolTable.find_symbol_by_name(varName);
        int address = symbol.getAddress();

        int target = IP;

        emit_opcode(OP_CODE.PUSH);
        emit_int(address); // push value of this symbol

        emit_opcode(OP_CODE.PUSHI); // push intlit
        emit_int(Integer.valueOf(currentToken.getTokenValue()));
        match("TK_INTLIT");

        emit_opcode(OP_CODE.LEQ);
        emit_opcode(OP_CODE.JFALSE);
        match("TK_DO");

        int hole = IP;
        emit_int(0);

        match("TK_BEGIN");
        statements();
        match("TK_END");
        match("TK_SEMI_COLON");

        emit_opcode(OP_CODE.PUSH);
        emit_int(address);

        emit_opcode(OP_CODE.PUSHI);
        emit_int(1);
        emit_opcode(OP_CODE.ADD);

        emit_opcode(OP_CODE.POP);
        emit_int(address); // save value

        emit_opcode(OP_CODE.JMP);
        emit_int(target);

        int save_IP = IP;
        IP = hole;
        emit_int(save_IP);
        IP = save_IP;
    }

    /* <statement> ::= <unlabelled statement> | <label> : <statement> */
    private static void process_a_label() {
        Symbol symbol = SymbolTable.find_symbol_by_name(currentToken.getTokenValue());
        if (symbol != null) {
            if (symbol.getSeen()) {
                throw new Error("Label has been seen before");
            } else {
                symbol.setSeen(true);
                symbol.setAddress(IP);
                if (!symbol.getHole().isEmpty()) {
                    IP = symbol.getHole().get(0);
                    //System.out.println(IP);
                    emit_int(symbol.getAddress());
                    IP = symbol.getAddress();
                }
            }
        } else {
            throw new Error(String.format("Unknown symbol \"%s\". row: %s, cols: %s", currentToken.getTokenValue(),
                    currentToken.getRow(), currentToken.getCol()));
        }
        match("TK_A_LABEL");
        match("TK_COLON");
    }

    private static void goto_statement() {
        match("TK_GOTO");// never fail

        Symbol symbol = SymbolTable.find_symbol_by_name(currentToken.getTokenValue());// label

        if (symbol == null) {
            throw new Error(String.format("Unknown symbol \"%s\". row: %s, cols: %s", currentToken.getTokenValue(),
                    currentToken.getRow(), currentToken.getCol()));
        }
        if (!symbol.getTokenName().equals("TK_A_LABEL")) {
            throw new Error(String.format("Symbol \"%s\" is not a label. row: %s, cols: %s",
                    currentToken.getTokenValue(), currentToken.getRow(), currentToken.getCol()));
        }

        currentToken.setTokenName("TK_A_LABEL");
        match("TK_A_LABEL");

        emit_opcode(OP_CODE.JMP);
        if (symbol.getSeen()) {
            emit_int(symbol.getAddress());
        } else {
            //System.out.println(IP);
            symbol.setHole(IP);
            emit_int(0); // PLACE HOLDER
        }
        match("TK_SEMI_COLON");
    }

    private static void resolve_gotos() {
        // not handeled yet
    }

    /*
     * <conditional statement> ::= <if statement> | <case statement> <case
     * statement> ::= case <expression> of <case list element> {; <case list
     * element> } end <case list element> ::= <case label list> : <statement> |
     * <empty> <case label list> ::= <case label> {, <case label> }
     */
    public static void case_statement() {
        match("TK_CASE");
    }

    public static DATATYPE do_unary_operation(String operatorToken, DATATYPE t) {
        switch (operatorToken) {
            case "TK_MINUS":
                if (t == DATATYPE.INT) {
                    emit_opcode(OP_CODE.NEG);
                    return DATATYPE.INT;
                } else if (t == DATATYPE.REAL) {
                    emit_opcode(OP_CODE.FNEG);
                    return DATATYPE.REAL;
                }
                break;

            default:
                throw new Error("Unhandled unary opreation in parser");
        }
        return t;
    }

    public static DATATYPE do_binary_operation(String operatorToken, DATATYPE t1, DATATYPE t2) {
        switch (operatorToken) {
            case "TK_PLUS":
                if (t1 == DATATYPE.INT && t2 == DATATYPE.INT) { // int + int
                    emit_opcode(OP_CODE.ADD);
                    return DATATYPE.INT;
                } else if (t1 == DATATYPE.INT && t2 == DATATYPE.REAL) { // int + real
                    emit_opcode(OP_CODE.XCHG);
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.FADD);
                    return DATATYPE.REAL;
                } else if (t1 == DATATYPE.REAL && t2 == DATATYPE.INT) {// real + int(top)
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.FADD);
                    return DATATYPE.REAL;
                } else if (t1 == DATATYPE.REAL && t2 == DATATYPE.REAL) {// real + real(top)
                    emit_opcode(OP_CODE.FADD);
                    return DATATYPE.REAL;
                } else {
                    throw new Error(
                            String.format("Type \"%s\" and \"%s\" is not legal for operation +. rows: %s, cols: %s.",
                                    t1, t2, currentToken.getRow(), currentToken.getCol()));
                }

            case "TK_MINUS":
                if (t1 == DATATYPE.INT && t2 == DATATYPE.INT) {// int - int(top)
                    emit_opcode(OP_CODE.SUB);
                    return DATATYPE.INT;
                } else if (t1 == DATATYPE.INT && t2 == DATATYPE.REAL) {// int - real(top)
                    emit_opcode(OP_CODE.XCHG);
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.XCHG);
                    emit_opcode(OP_CODE.FSUB);
                    return DATATYPE.REAL;
                } else if (t1 == DATATYPE.REAL && t2 == DATATYPE.INT) {// real - int(top)
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.FSUB);
                    return DATATYPE.REAL;
                } else if (t1 == DATATYPE.REAL && t2 == DATATYPE.REAL) {// real - real(top)
                    emit_opcode(OP_CODE.FSUB);
                    return DATATYPE.REAL;
                } else {
                    throw new Error(
                            String.format("Type \"%s\" and \"%s\" is not legal for operation -. rows: %s, cols: %s.",
                                    t1, t2, currentToken.getRow(), currentToken.getCol()));
                }

            case "TK_MULTIPLY":
                if (t1 == DATATYPE.INT && t2 == DATATYPE.INT) {
                    emit_opcode(OP_CODE.MULT);
                    return DATATYPE.INT;
                } else if (t1 == DATATYPE.INT && t2 == DATATYPE.REAL) {
                    emit_opcode(OP_CODE.XCHG);
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.FMULT);
                    return DATATYPE.REAL;
                } else if (t1 == DATATYPE.REAL && t2 == DATATYPE.INT) {
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.FMULT);
                    return DATATYPE.REAL;
                } else if (t1 == DATATYPE.REAL && t2 == DATATYPE.REAL) {
                    emit_opcode(OP_CODE.FMULT);
                    return DATATYPE.REAL;
                } else {
                    throw new Error(
                            String.format("Type \"%s\" and \"%s\" is not legal for operation *. rows: %s, cols: %s.",
                                    t1, t2, currentToken.getRow(), currentToken.getCol()));
                }

            case "TK_DIVIDE":
                if (t1 == DATATYPE.INT && t2 == DATATYPE.INT) {
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.XCHG);
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.XCHG);
                    emit_opcode(OP_CODE.FDIV);
                    return DATATYPE.REAL;
                } else if (t1 == DATATYPE.INT && t2 == DATATYPE.REAL) {
                    emit_opcode(OP_CODE.XCHG);
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.XCHG);
                    emit_opcode(OP_CODE.FDIV);
                    return DATATYPE.REAL;
                } else if (t1 == DATATYPE.REAL && t2 == DATATYPE.INT) {
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.FDIV);
                    return DATATYPE.REAL;
                } else if (t1 == DATATYPE.REAL && t2 == DATATYPE.REAL) {
                    emit_opcode(OP_CODE.FDIV);
                    return DATATYPE.REAL;
                } else {
                    throw new Error(
                            String.format("Type \"%s\" and \"%s\" is not legal for operation /. rows: %s, cols: %s.",
                                    t1, t2, currentToken.getRow(), currentToken.getCol()));
                }

            case "TK_DIV":
                if (t1 == DATATYPE.INT && t2 == DATATYPE.INT) {
                    emit_opcode(OP_CODE.DIV);
                    return DATATYPE.INT;
                } else {
                    throw new Error(
                            String.format("Type \"%s\" and \"%s\" is not legal for operation DIV. rows: %s, cols: %s.",
                                    t1, t2, currentToken.getRow(), currentToken.getCol()));
                }

                // CONDITION:
            case "TK_LESS_THAN":
                if (t1 == DATATYPE.INT && t2 == DATATYPE.INT) {
                    emit_opcode(OP_CODE.LSS);
                    return DATATYPE.BOOL;

                } else if (t1 == DATATYPE.REAL && t2 == DATATYPE.REAL) {
                    emit_opcode(OP_CODE.FLSS);
                    return DATATYPE.BOOL;

                } else if (t1 == DATATYPE.INT && t2 == DATATYPE.REAL) {
                    emit_opcode(OP_CODE.XCHG);
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.XCHG);
                    emit_opcode(OP_CODE.FLSS);
                    return DATATYPE.BOOL;

                } else if (t1 == DATATYPE.REAL && t2 == DATATYPE.INT) {
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.FLSS);
                    return DATATYPE.BOOL;
                } else {
                    throw new Error(
                            String.format("Type \"%s\" and \"%s\" is not legal for operation <. rows: %s, cols: %s.",
                                    t1, t2, currentToken.getRow(), currentToken.getCol()));
                }

            case "TK_GREATER_THAN":
                if (t1 == DATATYPE.INT && t2 == DATATYPE.INT) {
                    emit_opcode(OP_CODE.GTR);
                    return DATATYPE.BOOL;

                } else if (t1 == DATATYPE.REAL && t2 == DATATYPE.REAL) {
                    emit_opcode(OP_CODE.FGTR);
                    return DATATYPE.BOOL;

                } else if (t1 == DATATYPE.INT && t2 == DATATYPE.REAL) {
                    emit_opcode(OP_CODE.XCHG);
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.XCHG);
                    emit_opcode(OP_CODE.FGTR);
                    return DATATYPE.BOOL;

                } else if (t1 == DATATYPE.REAL && t2 == DATATYPE.INT) {
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.FGTR);
                    return DATATYPE.BOOL;
                } else {
                    throw new Error(
                            String.format("Type \"%s\" and \"%s\" is not legal for operation >. rows: %s, cols: %s.",
                                    t1, t2, currentToken.getRow(), currentToken.getCol()));
                }

            case "TK_GREATER_OR_EQUAL":
                if (t1 == DATATYPE.INT && t2 == DATATYPE.INT) {
                    emit_opcode(OP_CODE.GEQ);
                    return DATATYPE.BOOL;

                } else if (t1 == DATATYPE.REAL && t2 == DATATYPE.REAL) {
                    emit_opcode(OP_CODE.FGEQ);
                    return DATATYPE.BOOL;

                } else if (t1 == DATATYPE.INT && t2 == DATATYPE.REAL) {
                    emit_opcode(OP_CODE.XCHG);
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.XCHG);
                    emit_opcode(OP_CODE.FGEQ);
                    return DATATYPE.BOOL;

                } else if (t1 == DATATYPE.REAL && t2 == DATATYPE.INT) {
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.FGEQ);
                    return DATATYPE.BOOL;
                } else {
                    throw new Error(
                            String.format("Type \"%s\" and \"%s\" is not legal for operation >=. rows: %s, cols: %s.",
                                    t1, t2, currentToken.getRow(), currentToken.getCol()));
                }

            case "TK_LESS_OR_EQUAL":
                if (t1 == DATATYPE.INT && t2 == DATATYPE.INT) {
                    emit_opcode(OP_CODE.LEQ);
                    return DATATYPE.BOOL;

                } else if (t1 == DATATYPE.REAL && t2 == DATATYPE.REAL) {
                    emit_opcode(OP_CODE.FLEQ);
                    return DATATYPE.BOOL;

                } else if (t1 == DATATYPE.INT && t2 == DATATYPE.REAL) {
                    emit_opcode(OP_CODE.XCHG);
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.XCHG);
                    emit_opcode(OP_CODE.FLEQ);
                    return DATATYPE.BOOL;

                } else if (t1 == DATATYPE.REAL && t2 == DATATYPE.INT) {
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.FLEQ);
                    return DATATYPE.BOOL;
                } else {
                    throw new Error(
                            String.format("Type \"%s\" and \"%s\" is not legal for operation <=. rows: %s, cols: %s.",
                                    t1, t2, currentToken.getRow(), currentToken.getCol()));
                }

            case "TK_OR":
                if ((t1 == DATATYPE.INT || t1 == DATATYPE.BOOL) && (t2 == DATATYPE.INT || t2 == DATATYPE.BOOL)) {
                    emit_opcode(OP_CODE.OR);
                    return DATATYPE.BOOL;

                } else {
                    throw new Error(
                            String.format("Type \"%s\" and \"%s\" is not legal for operation OR. rows: %s, cols: %s.",
                                    t1, t2, currentToken.getRow(), currentToken.getCol()));
                }

            case "TK_AND":
                if ((t1 == DATATYPE.INT || t1 == DATATYPE.BOOL) && (t2 == DATATYPE.INT || t2 == DATATYPE.BOOL)) {
                    emit_opcode(OP_CODE.AND);
                    return DATATYPE.BOOL;

                } else {
                    throw new Error(
                            String.format("Type \"%s\" and \"%s\" is not legal for operation AND. rows: %s, cols: %s.",
                                    t1, t2, currentToken.getRow(), currentToken.getCol()));
                }

            case "TK_XOR":
                if ((t1 == DATATYPE.INT || t1 == DATATYPE.BOOL) && (t2 == DATATYPE.INT || t2 == DATATYPE.BOOL)) {
                    emit_opcode(OP_CODE.XOR);
                    return DATATYPE.BOOL;

                } else {
                    throw new Error(
                            String.format("Type \"%s\" and \"%s\" is not legal for operation XOR. rows: %s, cols: %s.",
                                    t1, t2, currentToken.getRow(), currentToken.getCol()));
                }

            case "TK_EQUAL":
                if (t1 == DATATYPE.INT && t2 == DATATYPE.INT || t1 == DATATYPE.BOOL && t2 == DATATYPE.BOOL) {
                    emit_opcode(OP_CODE.EQL);
                    return DATATYPE.BOOL;

                } else if (t1 == DATATYPE.REAL && t2 == DATATYPE.REAL) {
                    emit_opcode(OP_CODE.FEQL);
                    return DATATYPE.BOOL;

                } else if (t1 == DATATYPE.INT && t2 == DATATYPE.REAL) {
                    emit_opcode(OP_CODE.XCHG);
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.FEQL);
                    return DATATYPE.BOOL;

                } else if (t1 == DATATYPE.REAL && t2 == DATATYPE.INT) {
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.FEQL);
                    return DATATYPE.BOOL;
                } else {
                    throw new Error(
                            String.format("Type \"%s\" and \"%s\" is not legal for operation =. rows: %s, cols: %s.",
                                    t1, t2, currentToken.getRow(), currentToken.getCol()));
                }

            case "TK_NOT_EQUAL":
                if (t1 == DATATYPE.INT && t2 == DATATYPE.INT) {
                    emit_opcode(OP_CODE.NEQL);
                    return DATATYPE.BOOL;

                } else if (t1 == DATATYPE.REAL && t2 == DATATYPE.REAL) {
                    emit_opcode(OP_CODE.FNEQL);
                    return DATATYPE.BOOL;

                } else if (t1 == DATATYPE.INT && t2 == DATATYPE.REAL) {
                    emit_opcode(OP_CODE.XCHG);
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.FNEQL);
                    return DATATYPE.BOOL;

                } else if (t1 == DATATYPE.REAL && t2 == DATATYPE.INT) {
                    emit_opcode(OP_CODE.CVR);
                    emit_opcode(OP_CODE.FNEQL);
                    return DATATYPE.BOOL;
                } else {
                    throw new Error(
                            String.format("Type \"%s\" and \"%s\" is not legal for operation <>. rows: %s, cols: %s.",
                                    t1, t2, currentToken.getRow(), currentToken.getCol()));
                }

        }
        throw new Error("Uncomputable types in the E(), T(), F() function.");
    }

}
