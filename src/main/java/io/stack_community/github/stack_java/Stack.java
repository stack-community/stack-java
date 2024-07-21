package io.stack_community.github.stack_java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Stack {
    public static String name = "Stack Programming Language: Java Edition";
    public static String baseVersion = "1.12";
    public static String version = "1.0.0-Beta";

    public Stack() {

    }

    public static void main(String[] args) {
        Stack stack = new Stack();

        stack.interpreter(args);
    }

    public void interpreter(String[] args) {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);

        System.out.println(name);
        System.out.println("Version: " + version);
        System.out.println("BaseVersion(Stack): " + baseVersion);

        Executor executor = new Stack.Executor(Mode.Debug, true);
        // REPL Execution
        while (!executor.exitmode) {
            StringBuilder code = new StringBuilder();
            while (!executor.exitmode) {
                String enter = input(br, "> ");
                code.append(enter).append("\n");
                if (enter.isEmpty()) {
                    break;
                }
            }

            executor.evaluateProgram(code.toString());
        }

        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(executor.exitcode);
    }

    public static String input(BufferedReader reader, String prompt) {
        System.out.print(prompt);
        String str = null;
        try {
            str = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    interface Displayable {
        String display();
    }

    interface Convertible<T> {
        T convert();
    }

    public record Type(TypeEnum type, Object value) {
        public enum TypeEnum {
            NUMBER, STRING, BOOL, LIST, OBJECT, ERROR
        }

        public String display() {
            switch (type) {
                case NUMBER:
                    return value.toString();
                case STRING:
                    return "(" + value + ")";
                case BOOL:
                    return value.toString();
                case LIST:
                    StringBuilder result = new StringBuilder("[");
                    List<Type> list = (List<Type>) value;
                    for (int i = 0; i < list.size(); i++) {
                        result.append(list.get(i).display());
                        if (i < list.size() - 1) {
                            result.append(" ");
                        }
                    }
                    result.append("]");
                    return result.toString();
                case ERROR:
                    return "error:" + value;
                case OBJECT:
                    return "Object<" + value.toString() + ">";
                default:
                    return "";
            }
        }

        public String getString() {
            switch (type) {
                case STRING:
                    return (String) value;
                case NUMBER:
                    return value.toString();
                case BOOL:
                    return value.toString();
                case LIST:
                    return ((List<Type>) value).get(0).display(); // Return first element of list as string
                case ERROR:
                    return "error:" + value;
                case OBJECT:
                    return "Object<" + value.toString() + ">";
                default:
                    return "";
            }
        }

        public double getNumber() {
            switch (type) {
                case STRING:
                    try {
                        return Double.parseDouble((String) value);
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                case NUMBER:
                    return (double) value;
                case BOOL:
                    return (boolean) value ? 1.0 : 0.0;
                case LIST:
                    return (double) ((List<Type>) value).size();
                case ERROR:
                    try {
                        return Double.parseDouble((String) value);
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                case OBJECT:
                    return (double) ((HashMap<String, Type>) value).size();
                default:
                    return 0.0;
            }
        }

        public boolean getBool() {
            switch (type) {
                case STRING:
                    return !((String) value).isEmpty();
                case NUMBER:
                    return (double) value != 0.0;
                case BOOL:
                    return (boolean) value;
                case LIST:
                    return !((List<Type>) value).isEmpty();
                case ERROR:
                    try {
                        return Boolean.parseBoolean((String) value);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                case OBJECT:
                    return ((HashMap<String, Type>) value).isEmpty();
                default:
                    return false;
            }
        }

        public List<Type> getList() {
            switch (type) {
                case STRING:
                    String str = (String) value;
                    List<Type> result = new ArrayList<>();
                    for (int i = 0; i < str.length(); i++) {
                        result.add(new Type(TypeEnum.STRING, str.substring(i, i + 1)));
                    }
                    return result;
                case NUMBER:
                    return Collections.singletonList(new Type(TypeEnum.NUMBER, (double) value));
                case BOOL:
                    return Collections.singletonList(new Type(TypeEnum.BOOL, (boolean) value));
                case LIST:
                    return (List<Type>) value;
                case ERROR:
                    return Collections.singletonList(new Type(TypeEnum.ERROR, "error:" + value));
                case OBJECT:
                    return new ArrayList<>(((HashMap<String, Type>) value).values());
                default:
                    return Collections.emptyList();
            }
        }
    }

    public static enum Mode {
        Debug, // Define Mode Enum
        Release,
    }

    public static class Executor {
        protected List<Type> stack; // Data stack
        protected Map<String, Type> memory; // Variable's memory
        private Mode mode; // Execution mode
        private Functions functions;
        public boolean exitmode = false;
        public int exitcode = 0;
        public boolean interpreterMode = false;

        public Executor(Mode mode) { // Constructor
            this.stack = new ArrayList<>();
            this.memory = new HashMap<>();
            this.mode = mode;
            this.functions = new Functions(this);
        }

        public Executor(Mode mode, boolean interpreterMode) {
            this(mode);
            this.interpreterMode = interpreterMode;
        }

        public void logPrint(String msg) { // Output log
            if (this.mode == Mode.Debug) {
                System.out.print(msg);
            }
        }

        public void showVariables() { // Show variable inside memory
            this.logPrint("Variables {\n");
            int max = this.memory.keySet().stream().mapToInt(String::length).max().orElse(0);
            for (Map.Entry<String, Type> entry : this.memory.entrySet()) {
                this.logPrint(String.format(" %-" + max + "s: %s\n", entry.getKey(), entry.getValue().display()));
            }
            this.logPrint("}\n");
        }

        public String showStack() { // Show inside the stack
            StringBuilder result = new StringBuilder("Stack { ");
            for (int i = 0; i < this.stack.size(); i++) {
                Type item = this.stack.get(i);
                result.append(item.display());
                if (i != this.stack.size() - 1) {
                    result.append(" | ");
                }
            }
            result.append(" }");
            return result.toString();
        }

        public List<String> analyzeSyntax(String code) { // Parse token by analyzing syntax
            // Convert tabs, line breaks, and full-width spaces to half-width spaces
            String formattedCode = code.replaceAll("[\\n\\t\\r　]", " ");
            List<String> syntax = new ArrayList<>(); // Token string
            StringBuilder buffer = new StringBuilder(); // Temporary storage
            int brackets = 0; // String's nest structure
            int parentheses = 0; // List's nest structure
            boolean hash = false; // Is it Comment
            boolean escape = false; // Flag to indicate next character is escaped

            for (char c : formattedCode.toCharArray()) {
                switch (c) {
                    case '\\' -> escape = !escape;
                    case '(' -> {
                        if (!hash && !escape) {
                            brackets++;
                            buffer.append('(');
                        }
                    }
                    case ')' -> {
                        if (!hash && !escape) {
                            brackets--;
                            buffer.append(')');
                        }
                    }
                    case '#' -> {
                        if (!hash && !escape) {
                            hash = true;
                            buffer.append('#');
                        } else if (hash && !escape) {
                            hash = false;
                            buffer.append('#');
                        }
                    }
                    case '[' -> {
                        if (!hash && brackets == 0 && !escape) {
                            parentheses++;
                            buffer.append('[');
                        }
                    }
                    case ']' -> {
                        if (!hash && brackets == 0 && !escape) {
                            parentheses--;
                            buffer.append(']');
                        }
                    }
                    case ' ' -> {
                        if (!hash && parentheses == 0 && brackets == 0 && !escape) {
                            if (buffer.length() > 0) {
                                syntax.add(buffer.toString());
                                buffer.setLength(0);
                            }
                        } else {
                            buffer.append(' ');
                        }
                    }
                    default -> {
                        if (parentheses == 0 && brackets == 0 && !hash) {
                            if (escape) {
                                switch (c) {
                                    case 'n' -> buffer.append("\\n");
                                    case 't' -> buffer.append("\\t");
                                    case 'r' -> buffer.append("\\r");
                                    default -> buffer.append(c);
                                }
                            } else {
                                buffer.append(c);
                            }
                        } else {
                            if (escape) {
                                buffer.append('\\');
                            }
                            buffer.append(c);
                        }
                        escape = false; // Reset escape flag for non-escape characters
                    }
                }
            }

            if (buffer.length() > 0) {
                syntax.add(buffer.toString());
            }
            return syntax;
        }

        public void evaluateProgram(String code) { // evaluate string as program
            // Parse into token string
            List<String> syntax = this.analyzeSyntax(code);

            for (String token : syntax) {
                // Show inside stack to debug
                String stack = this.showStack();
                this.logPrint(stack + " <- " + token + "\n");

                char[] chars = token.toCharArray();

                // Judge what the token is
                try {
                    double num = Double.parseDouble(token);
                    this.stack.add(new Type(Type.TypeEnum.NUMBER, num)); // Push number value on the stack
                } catch (NumberFormatException ignored) {
                    if (token.equals("true") || token.equals("false")) {
                        this.stack.add(new Type(Type.TypeEnum.BOOL, Boolean.parseBoolean(token))); // Push bool value on the stack
                    } else if (chars[0] == '(' && chars[chars.length - 1] == ')') {
                        // Processing string escape
                        String string = token.substring(1, token.length() - 1);
                        this.stack.add(new Type(Type.TypeEnum.STRING, string)); // Push string value on the stack
                    } else if (chars[0] == '[' && chars[chars.length - 1] == ']') {
                        // Push list value on the stack
                        String slice = token.substring(1, token.length() - 1);
                        this.evaluateProgram(slice); // Recursively evaluate list
                        List<Type> list = new ArrayList<>();
                        int oldSize = this.stack.size();
                        for (int i = oldSize; i < this.stack.size(); i++) {
                            list.add(this.stack.remove(oldSize));
                        }
                        list.forEach(this.stack::add); // Add elements to the stack
                    } else if (token.startsWith("error:")) {
                        this.stack.add(new Type(Type.TypeEnum.STRING, token.substring("error:".length()))); // Push error value on the stack
                    } else if (this.memory.containsKey(token)) {
                        this.stack.add(this.memory.get(token)); // Push variable's data on stack
                    } else if (chars[0] == '#' && chars[chars.length - 1] == '#') {
                        // Processing comments
                        this.logPrint("* Comment \"" + token.replace('#', ' ') + "\"\n");
                    } else {
                        // Else, execute as command
                        this.executeCommand(token);
                    }
                }
            }

            // Show inside stack, after execution
            String stack = this.showStack();
            this.logPrint(stack + "\n");
        }

        // execute string as commands
        public void executeCommand(String command) {
            functions.execute(command);
        }

        public Type popStack() { // Pop stack's top value
            if (!this.stack.isEmpty()) {
                return this.stack.removeLast();
            } else {
                this.logPrint("Error! There are not enough values on the stack. returns default value\n");
                return new Type(Type.TypeEnum.STRING, "");
            }
        }
    }
}
