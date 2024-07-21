package io.stack_community.github.stack_java;

public record Functions(Stack.Executor executor, String command) {
    public Functions {
        // Commands of calculation

        // addition
        if (command.equals("add")) {
            double b = executor.popStack().getNumber();
            double a = executor.popStack().getNumber();
            executor.stack.add(new Stack.Type(Stack.Type.TypeEnum.NUMBER, a + b));
        }

        // Subtraction
        else if (command.equals("sub")) {
            double b = executor.popStack().getNumber();
            double a = executor.popStack().getNumber();
            executor.stack.add(new Stack.Type(Stack.Type.TypeEnum.NUMBER, a - b));
        }

        // Pop in stack
        else if (command.equals("pop")) {
            executor.popStack();
        }

        // Exit Stack
        else if (command.equals("exit")) {
            double code = executor.popStack().getNumber();
            executor.exitmode = true;
            executor.exitcode = (int) code;
            if (executor.interpreterMode == false) {
                System.exit(executor.exitcode);
            }
        }

        // TODO Other commands...

        // When other string inputted
        else {
            executor.stack.add(new Stack.Type(Stack.Type.TypeEnum.STRING, command));
        }
    }
}
