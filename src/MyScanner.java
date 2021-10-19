import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public final class MyScanner {
    private static String curname = "";
    private static int currow = 0;
    private static int curcol = 0;
    private static boolean readingString = false;
    private static boolean readingNumber = false;
    private static boolean readingReal = false;
    private static boolean readingColon = false;
    private static boolean readingDot = false;
    private static boolean readingCodition = false;

    private static ArrayList<Token> tokenList = new ArrayList<>();
    private static final HashMap<String, String> KEYWORD_TOKEN = new HashMap<>();
    private static final HashMap<String, String> OPERATOR_TOKEN = new HashMap<>();
    private static final HashMap<String, TYPE> TYPE_DIC = new HashMap<>();

    enum TYPE {
        LETTER, DIGIT, SPACE, PUNCT_OR_OPER, QUOTE, NEWLINE, TAB
    }

    static {
        OPERATOR_TOKEN.put("+", "TK_PLUS");
        OPERATOR_TOKEN.put("-", "TK_MINUS");
        OPERATOR_TOKEN.put("*", "TK_MULTIPLY");
        OPERATOR_TOKEN.put("/", "TK_DIVIDE");
        OPERATOR_TOKEN.put("=", "TK_EQUAL");
        OPERATOR_TOKEN.put(":=", "TK_ASSIGN");
        OPERATOR_TOKEN.put("<", "TK_LESS_THAN");
        OPERATOR_TOKEN.put(">", "TK_GREATER_THAN");
        OPERATOR_TOKEN.put("<>", "TK_NOT_EQUAL");// not handel yet
        OPERATOR_TOKEN.put("<=", "TK_LESS_OR_EQUAL");// not handel yet
        OPERATOR_TOKEN.put(">=", "TK_GREATER_OR_EQUAL");// not handel yet
        OPERATOR_TOKEN.put(":", "TK_COLON");
        OPERATOR_TOKEN.put(";", "TK_SEMI_COLON");
        OPERATOR_TOKEN.put("(", "TK_LP");
        OPERATOR_TOKEN.put(")", "TK_RP");
        OPERATOR_TOKEN.put("[", "TK_LB");
        OPERATOR_TOKEN.put("]", "TK_RB");
        OPERATOR_TOKEN.put(".", "TK_DOT");
        OPERATOR_TOKEN.put("..", "TK_RANGE");
        OPERATOR_TOKEN.put(",", "TK_COMMA");
        OPERATOR_TOKEN.put("^", "TK_HAT");
        OPERATOR_TOKEN.put("@", "TK_REFERENCE");

        for (int i = 65; i < 91; i++) {
            // Add letters
            String currentChar = String.valueOf(Character.toChars(i));
            TYPE_DIC.put(currentChar, TYPE.LETTER);
            TYPE_DIC.put(currentChar.toLowerCase(), TYPE.LETTER);
        }
        for (int i = 48; i < 58; i++) {
            // Add digits
            String currentChar = String.valueOf(Character.toChars(i));
            TYPE_DIC.put(currentChar, TYPE.DIGIT);
        }
        for (String key : OPERATOR_TOKEN.keySet()) {
            TYPE_DIC.put(key, TYPE.PUNCT_OR_OPER);
        } // Add operators.

        TYPE_DIC.put(String.valueOf(Character.toChars(39)), TYPE.QUOTE); // Add signle quote
        TYPE_DIC.put(String.valueOf(Character.toChars(32)), TYPE.SPACE); // Add spaces
        TYPE_DIC.put(String.valueOf(Character.toChars(13)), TYPE.NEWLINE); // Add newline symbol-"CR"
        TYPE_DIC.put(String.valueOf(Character.toChars(10)), TYPE.NEWLINE); // Add newline symbol-"/n"
        TYPE_DIC.put(String.valueOf(Character.toChars(9)), TYPE.TAB); // Add TAB symbol
        // TYPE_DIC, match char to TYPE. e.g. {"C" : "LETTER"}

        String keyword;
        try { // Add keywords to KEYWORDS_TOKEN e.g. {"begin" : "TK_BEGIN"}
            Scanner sc = new Scanner(new File("src\\keywords.txt"));
            while (sc.hasNext()) {
                keyword = sc.next();

                KEYWORD_TOKEN.put(keyword, String.format("TK_%s", keyword.toUpperCase()));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Start
    public static void scan(File file) throws FileNotFoundException {
        // Delimiter to scan each char
        Scanner sc = new Scanner(file).useDelimiter("");

        while (sc.hasNext()) {
            char c = sc.next().toLowerCase().charAt(0);
            //System.out.print(c); // Uncomment this line for Test
            getToken(c);
        }

        curname = "EOF";
        addToken("TK_EOF"); // put end of file

        //System.out.println(tokenList); // Uncomment this line for Test

        //System.out.println("\nParser output:");
        Parser.setTokenArrayListIterator(tokenList); // set static varible token iterator in Parser class
        Byte[] instructions = Parser.compile(); // start parsing, return a byte array as insturctions.
        System.out.println(tokenList); // Uncomment this line for Test

        //System.out.println("\nProgram Output:");
        OpCodeStackMachine.setOpCode(instructions);
        OpCodeStackMachine.run();

    }

    public static void getToken(char c) {
        if (TYPE_DIC.containsKey(String.valueOf(c))) {
            switch (TYPE_DIC.get(String.valueOf(c))) {

                case LETTER:
                    if (readingString) {
                        curname += c;
                    } else if (readingColon) {
                        addToken(OPERATOR_TOKEN.get(curname));// "a:integer" Add conlon token.
                        curname += c;
                        readingColon = false;
                    } else {
                        curname += c;
                    }
                    break;

                case DIGIT:
                    if (curname.isEmpty()) {
                        readingNumber = true;
                    }

                    curname += c;

                    break;

                case SPACE:
                    if (readingString) { // String concatenate.
                        curname += c;
                    } else if (readingColon) { // e.g. {: }.
                        addToken(OPERATOR_TOKEN.get(curname));

                        readingColon = false;

                    } else if (!readingNumber) {
                        putToken();
                    } else {
                        endNumber();
                    }
                    break;

                case PUNCT_OR_OPER:
                    if (readingDot && c == '.') {
                        if (curname.equals(".")) {// only ".."
                            curname = curname + c;
                            addToken("TK_RANGE");
                            curname = "";
                        } else {
                            //System.out.println(readingDot);
                            addToken(curname.substring(0, curname.length() - 2));
                            addToken("TK_DOT");
                            curname = "";
                        }
                        readingDot = false;

                    } else if (readingString) {
                        curname += c;

                    } else if (readingNumber) {
                        if (readingReal && c == '.') {
                            readingReal = false;
                            curname = curname.substring(0, curname.length() - 1);
                            endNumber();
                            curname = "..";
                            addToken("TK_RANGE");
                            curname = "";

                        } else if (c == '.') { // readingdot stays false 313.
                            readingReal = true;
                            curname += c;

                        } else if(c == '>' || c == '=' || c == '<'){
                            endNumber();
                            curname += c;
                            readingCodition=true;

                        }else{
                            endNumber();
                            curname+=c;
                            putToken();
                        }
                    } else if (readingColon && c == '=') { // Assignment
                        curname += c;
                        addToken(OPERATOR_TOKEN.get(curname));
                        readingColon = false;

                    } else if (readingCodition) {
                        if (c == '>' || c == '=' || c == '<') {
                            curname += c;
                            putToken();
                        }else{
                            putToken();
                            curname += c;
                        }
                    } else {
                        switch (c) {
                            case ';':
                                putToken(); // End of line

                                curname = ";";
                                addToken(OPERATOR_TOKEN.get(String.valueOf(c))); // add TK_SEMI_COLON
                                break;

                            case ':':
                                putToken();
                                readingColon = true;
                                curname += c;
                                break;

                            case '.':
                                curname += c;
                                if (curname.equals("end.")) {
                                    addToken("TK_END");
                                    addToken("TK_DOT");
                                } else {
                                    readingDot = true;
                                }
                                break;

                            case '<':
                            case '>':
                                putToken();
                                curname += c;
                                readingCodition = true;
                                break;

                            default: // (OPERATORS_TOKEN.containsKey(String.valueOf(c)))
                                putToken();
                                curname = String.valueOf(c);
                                addToken(OPERATOR_TOKEN.get(curname));
                        }
                    }
                    break;

                case QUOTE:
                    readingString = !readingString;
                    curname += c;
                    if (!readingString) {
                        curname = curname.substring(1, curname.length() - 1); // get char between ' '
                        if (curname.length() == 1) {
                            addToken("TK_CHARLIT");
                        } else if (curname.length() > 1) {
                            addToken("TK_STRLIT");
                        }
                    }
                    break;

                case NEWLINE:
                    curcol = 0;
                    currow += 1;
                    if (readingString) { // String concatenate.
                        curname += c;
                    } else {
                        putToken();
                    }
                    break;

                case TAB:
                    curcol += 4;
                    break;

                default:
                    throw new Error("Unhandeled character scaned!!!");
            }
        } else {
            throw new Error("Illegal character scaned!!");
        }
    }

    public static void putToken() {
        if (KEYWORD_TOKEN.containsKey(curname)) {
            addToken(KEYWORD_TOKEN.get(curname)); // predefined keywords
        } else if (OPERATOR_TOKEN.containsKey(curname)) {
            addToken(OPERATOR_TOKEN.get(curname));
        } else if (curname.length() > 0) {
            addToken("TK_UNKNOWN"); // or variable
        }

        readingString = false;
        readingNumber = false;
        readingReal = false;
        readingColon = false;
        readingDot = false;
        readingCodition = false;
    }

    public static void addToken(String tokenName) { // write curtoken correspending to a type and save it in tokenList
        Token t = new Token(tokenName, curname, curcol, currow);
        tokenList.add(t);

        curcol += curname.length();

        curname = "";
    }

    public static void endNumber() {
        if (readingReal) {
            addToken("TK_REALLIT");
            readingReal = false;
        } else {
            addToken("TK_INTLIT");
        }
        readingNumber = false;
    }
}