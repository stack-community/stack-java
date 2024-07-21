package io.stack_community.github.stack_java;


import java.util.HashMap;
import java.util.function.Supplier;

public class Functions {
    private static HashMap<String, Command> functions = new HashMap<>();
    private Stack.Executor executor;

    @FunctionalInterface
    public interface Command {
        void execute(Stack.Executor executor);
    }

    public Functions(Stack.Executor executor) {
        this.executor = executor;
    }

    static {
        { // Add Builtin Commands
            // Commands of calculation

            // addition
            addFunction("add", (e) -> {
                double b = e.popStack().getNumber();
                double a = e.popStack().getNumber();
                e.stack.add(new Stack.Type(Stack.Type.TypeEnum.NUMBER, a + b));
            });

            // Subtraction
            addFunction("sub", (e) -> {
                double b = e.popStack().getNumber();
                double a = e.popStack().getNumber();
                e.stack.add(new Stack.Type(Stack.Type.TypeEnum.NUMBER, a - b));
            });

            // Pop in stack
            addFunction("pop", (e) -> {
                e.popStack();
            });

            // Exit Stack
            addFunction("exit", (e) -> {
                double code = e.popStack().getNumber();
                e.exitmode = true;
                e.exitcode = (int) code;
                if (!e.interpreterMode) {
                    System.exit(e.exitcode);
                }
            });

            // Print String
            addFunction("print", (e) -> {
                String str = e.popStack().getString();
                System.out.print(str);
                if (e.interpreterMode) {
                    System.out.println();
                }
            });

            // Println String
            addFunction("println", (e) -> {
                String str = e.popStack().getString();
                System.out.println(str);
            });

            // TODO Other commands...
        }
    }

    public static void addFunction(String command, Command func) {
        functions.put(command, func);
    }

    public void execute(String command) {
        if (functions.containsKey(command)) {
            functions.get(command).execute(this.executor);
        } else {
            this.executor.stack.add(new Stack.Type(Stack.Type.TypeEnum.STRING, command));
        }
    }
}
