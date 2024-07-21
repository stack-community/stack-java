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

            // Addition
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

            // Multiplication
            addFunction("mul", (e) -> {
                double b = e.popStack().getNumber();
                double a = e.popStack().getNumber();
                e.stack.add(new Stack.Type(Stack.Type.TypeEnum.NUMBER, a * b));
            });

            // Division
            addFunction("div", (e) -> {
                double b = e.popStack().getNumber();
                double a = e.popStack().getNumber();
                e.stack.add(new Stack.Type(Stack.Type.TypeEnum.NUMBER, a / b));
            });

            // Remainder of division
            addFunction("mod", (e) -> {
                double b = e.popStack().getNumber();
                double a = e.popStack().getNumber();
                e.stack.add(new Stack.Type(Stack.Type.TypeEnum.NUMBER, a % b));
            });

            // Exponentiation
            addFunction("pow", (e) -> {
                double b = e.popStack().getNumber();
                double a = e.popStack().getNumber();
                e.stack.add(new Stack.Type(Stack.Type.TypeEnum.NUMBER, Math.pow(a, b)));
            });

            // Rounding off
            addFunction("round", (e) -> {
                double a = e.popStack().getNumber();
                e.stack.add(new Stack.Type(Stack.Type.TypeEnum.NUMBER, Math.round(a)));
            });

            // Trigonometric sine
            addFunction("sin", (e) -> {

            });

            // Commands of control

            // Evaluate string as program
            addFunction("eval", (e) -> {
                String code = e.popStack().getString();
                e.evaluateProgram(code);
            });

            // Conditional branch
            addFunction("if", (e) -> {
                boolean condition = e.popStack().getBool();
                String code_else = e.popStack().getString();
                String code_if = e.popStack().getString();

                if (condition) {
                    e.evaluateProgram(code_if);
                } else {
                    e.evaluateProgram(code_else);
                }
            });

            // Loop while condition is true
            addFunction("while", (e) -> {
                String cond = e.popStack().getString();
                String code = e.popStack().getString();

                while (true) {
                    e.evaluateProgram(cond);
                    if (!e.popStack().getBool()) break;

                    e.evaluateProgram(code);
                }
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

            // Commands of memory manage

            // Pop in the stack
            addFunction("pop", Stack.Executor::popStack);

            // Define variable at memory
            addFunction("var", (e) -> {
                String name = e.popStack().getString();
                Stack.Type data = e.popStack();
                if (e.memory.containsKey(name)) {
                    e.memory.replace(name, data);
                } else {
                    e.memory.put(name, data);
                }
                e.showVariables();
            });

            // Free up memory space of variable
            addFunction("free", (e) -> {
                String name = e.popStack().getString();
                e.memory.remove(name);
                e.showVariables();
            });

            // Copy stack's top value
            addFunction("copy", (e) -> {
                Stack.Type data = e.popStack();
                e.stack.add(data);
                e.stack.add(data);
            });

            // Swap stack's top 2 value
            addFunction("swap", (e) -> {
                Stack.Type b = e.popStack();
                Stack.Type a = e.popStack();
                e.stack.add(b);
                e.stack.add(a);
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
