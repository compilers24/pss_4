import java.util.HashSet;
import java.util.Set;

public class RegisterAllocator {
    private final Set<String> usedRegisters;

    public RegisterAllocator() {
        this.usedRegisters = new HashSet<>();
    }

    public String allocate() {
        for (String reg : Registers.REGISTERS) {
            if (!usedRegisters.contains(reg)) {
                usedRegisters.add(reg);
                return reg;
            }
        }
        throw new RuntimeException("No available registers!");
    }

    public void free(String register) {
        if (usedRegisters.contains(register)) {
            usedRegisters.remove(register);
        } else {
            throw new RuntimeException("Register " + register + " is not allocated!");
        }
    }
}
