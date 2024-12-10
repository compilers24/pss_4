import java.util.*;

public class InfixToPostfixConverter {
    private static final Map<String, Integer> OPERATOR_PRECEDENCE = Map.of(
            "+", 1,
            "-", 1,
            "*", 2
    );

    private static final List<String> GENERATED_CODE = new ArrayList<>();
    private static final String PRINTF_CODE = """
            movq $fmt, %rdi
            movq $0, %rax
            call printf
            """;

    private static final String EXIT_CODE = """
            movq $60, %rax
            xor %rdi, %rdi
            syscall
            """;

    public static void convert(String expression) {
        RegisterAllocator allocator = new RegisterAllocator();
        Stack<String> operationStack = new Stack<>();
        Stack<String> operandStack = new Stack<>();
        String[] tokens = expression.split(" ");

        for (String token : tokens) {
            if (isNumber(token)) {
                String reg = allocator.allocate();
                GENERATED_CODE.add(String.format("movq $%s, %%%s\n", token, reg));
                operandStack.push(reg);
            } else if (isOperator(token)) {
                while (!operationStack.isEmpty() &&
                        OPERATOR_PRECEDENCE.get(operationStack.peek()) >= OPERATOR_PRECEDENCE.get(token)) {
                    handleOperation(allocator, operandStack, operationStack.pop());
                }
                operationStack.push(token);
            }
        }

        while (!operationStack.isEmpty()) {
            handleOperation(allocator, operandStack, operationStack.pop());
        }

        String resultReg = operandStack.pop();
        GENERATED_CODE.add(String.format("movq %%%s, %%rsi\n", resultReg));
        allocator.free(resultReg);

        GENERATED_CODE.add(PRINTF_CODE);
        GENERATED_CODE.add(EXIT_CODE);
    }

    private static void handleOperation(RegisterAllocator allocator, Stack<String> operandStack, String operator) {
        String reg2 = operandStack.pop();
        String reg1 = operandStack.pop();

        switch (operator) {
            case "+" -> GENERATED_CODE.add(String.format("addq %%%s, %%%s\n", reg2, reg1));
            case "-" -> GENERATED_CODE.add(String.format("subq %%%s, %%%s\n", reg2, reg1));
            case "*" -> GENERATED_CODE.add(String.format("imulq %%%s, %%%s\n", reg2, reg1));
            default -> throw new RuntimeException("Unsupported operator: " + operator);
        }

        allocator.free(reg2);
        operandStack.push(reg1);
    }

    private static boolean isNumber(String token) {
        return token.matches("\\d+");
    }

    private static boolean isOperator(String token) {
        return OPERATOR_PRECEDENCE.containsKey(token);
    }

    public static void start(String expression) {
        GENERATED_CODE.add("""
                .section .rodata
                fmt:
                    .asciz "%d\\n"
                .section .text
                .globl _start
                _start:
                """);

        convert(expression);

        System.out.println("Generated ASM Code:");
        GENERATED_CODE.forEach(System.out::print);
    }
}
