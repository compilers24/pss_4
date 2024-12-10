# Problem: Evaluating Complex Expressions Using Registers and Postfix Conversion

We need to process a **complex arithmetic expression**, such as:

```plaintext
6 * 3 - 2 + 4 * 5
```

The goal is to:
1. Convert the **infix expression** into a **postfix expression**.
2. Perform the operations using **registers** managed by a **register allocator**.
3. Evaluate the expression while respecting **operator precedence**.

---

## **Steps to Solve**

### **1. Process Numbers and Move to Registers**
- Each time a **number** is encountered in the expression:
    - Use the **register allocator** to get a free register.
    - Move the number into the allocated register.
    - Push the register containing the number onto the operand stack.

### **2. Handle Operators and Precedence**
- Each time an **operator** is encountered:
    - Push the operator onto the operator stack **only if its precedence is higher** than the operator already on the stack.
    - If the precedence of the current operator is **lower or equal** to the top operator on the stack:
        1. **Pop** the operator from the stack.
        2. Use the last two numbers from the operand stack.
        3. Perform the operation and store the result in one of the registers.
        4. Push the result register back onto the operand stack.
    - Push the current operator onto the operator stack.

### **3. Finalize the Evaluation**
- After processing all tokens, **pop all remaining operators** from the operator stack and evaluate them in order.
- The final result will be in one of the registers.

---

## **Algorithm Outline**

### **Input**:
An infix arithmetic expression (e.g., `6 * 3 - 2 + 4 * 5`).

### **Output**:
The evaluated result of the expression, with intermediate steps using registers.

### **Steps**:
1. Initialize:
    - A **register allocator** to manage free registers.
    - An **operand stack** to hold registers storing numbers.
    - An **operator stack** to hold operators.

2. Tokenize the expression into numbers and operators.

3. Iterate through the tokens:
    - **If the token is a number**:
        - Allocate a free register and move the number into it.
        - Push the register onto the operand stack.
    - **If the token is an operator**:
        - While the operator stack is not empty and the precedence of the current operator is **less than or equal** to the operator at the top of the stack:
            1. Pop the operator from the stack.
            2. Pop two registers from the operand stack.
            3. Perform the operation using those registers.
            4. Store the result in one of the registers.
            5. Push the result register back onto the operand stack.
        - Push the current operator onto the stack.

4. After processing all tokens:
    - Pop and evaluate all remaining operators in the stack using the operand stack.

5. The final result will be in a register.

---

## **Operator Precedence**

| Operator | Precedence | Associativity |
|----------|------------|---------------|
| `*`      | High       | Left-to-Right |
| `+`      | Low        | Left-to-Right |
| `-`      | Low        | Left-to-Right |

---

## **Example Walkthrough**

### **Expression**:
```plaintext
6 * 3 - 2 + 4 * 5
```

### **Step-by-Step Execution**:

#### **1. Tokenize the Expression**
Tokens:
```plaintext
6, *, 3, -, 2, +, 4, *, 5
```

#### **2. Process Each Token**

| Step | Token | Operand Stack             | Operator Stack | Action                                                                                   |
|------|-------|---------------------------|----------------|------------------------------------------------------------------------------------------|
| 1    | `6`   | `[R1]`                    | `[]`           | Allocate `R1`, move `6` to `R1`.                                                        |
| 2    | `*`   | `[R1]`                    | `[*]`          | Push `*` to the operator stack.                                                         |
| 3    | `3`   | `[R1, R2]`                | `[*]`          | Allocate `R2`, move `3` to `R2`.                                                        |
| 4    | `-`   | `[R3]`                    | `[-]`          | Pop `*`, multiply `R1 * R2`, store in `R3`. Push `-` to the operator stack.             |
| 5    | `2`   | `[R3, R4]`                | `[-]`          | Allocate `R4`, move `2` to `R4`.                                                        |
| 6    | `+`   | `[R5]`                    | `[+]`          | Pop `-`, subtract `R3 - R4`, store in `R5`. Push `+` to the operator stack.             |
| 7    | `4`   | `[R5, R6]`                | `[+]`          | Allocate `R6`, move `4` to `R6`.                                                        |
| 8    | `*`   | `[R5, R6]`                | `[+, *]`       | Push `*` to the operator stack.                                                         |
| 9    | `5`   | `[R5, R6, R7]`            | `[+, *]`       | Allocate `R7`, move `5` to `R7`.                                                        |
| 10   | End   | `[R8]`                    | `[]`           | Pop `*`, multiply `R6 * R7`, store in `R8`. Then pop `+`, add `R5 + R8`, store in `R9`. |

#### **Final Result**:
The final result is in `R9`.

---

### **Output Assembly Instructions**

For the expression `6 * 3 - 2 + 4 * 5`, the generated assembly might look like:

```asm
movq $6, %r1          # Move 6 into R1
movq $3, %r2          # Move 3 into R2
imulq %r2, %r1        # R1 = R1 * R2
movq $2, %r3          # Move 2 into R3
subq %r3, %r1         # R1 = R1 - R3
movq $4, %r4          # Move 4 into R4
movq $5, %r5          # Move 5 into R5
imulq %r5, %r4        # R4 = R4 * R5
addq %r4, %r1         # R1 = R1 + R4
```

---

### **Testing Suggestions**

- **Simple Expression**: `2 + 3 * 4`
    - Expected Result: `14`
- **Complex Expression**: `5 * 3 - 4 + 6 * 2`
    - Expected Result: `29`
- **Edge Case**: `8 - 6 + 3 * 2`
    - Expected Result: `8`
