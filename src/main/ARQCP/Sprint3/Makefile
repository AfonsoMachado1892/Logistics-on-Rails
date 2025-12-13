CC=riscv32-linux-gcc
CFLAGS=-Wall -Wextra -fanalyzer -g
ARCHFLAGS=-march=rv32imd -mabi=ilp32d
LIBRV32=${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

# 1. Definição das pastas de includes (Necessário para encontrar domain.h e board.h)
INCLUDES = -I./common -I./manager -I./board

# 2. Fontes C (Adicionados setup.c e board.c)
MANAGER_SOURCES = manager/manager.c manager/sensors_comm.c manager/setup.c board/board.c

# 3. Fontes Assembly (Adicionados encrypt_data.s e track_utils.s)
ASSEMBLY_SOURCES = assembly/extract_data.s assembly/encrypt_data.s assembly/track_utils.s

# 4. Regra Principal (Atualizada para usar as variáveis e includes)
program.elf: $(MANAGER_SOURCES) $(ASSEMBLY_SOURCES)
	$(CC) $(CFLAGS) $(ARCHFLAGS) $(INCLUDES) $(MANAGER_SOURCES) $(ASSEMBLY_SOURCES) -o program.elf

# Nota: Removi a target 'manager' (gcc host) porque não consegue compilar
# assembly RISC-V (.s) na tua máquina local, o que daria erro.

clean:
	rm -f *.o *.elf

run: program.elf
	qemu-riscv32 -L $(LIBRV32) ./program.elf

debug: program.elf
	@echo " On other terminal window do : "
	@echo " riscv32-linux-gdb -tui ./program.elf"
	qemu-riscv32 -L $(LIBRV32) -g 1234 program.elf -S