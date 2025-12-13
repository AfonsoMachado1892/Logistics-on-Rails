CC=riscv32-linux-gcc
CFLAGS=-Wall -Wextra -fanalyzer -g
ARCHFLAGS=-march=rv32imd -mabi=ilp32d
LIBRV32=${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

program.elf: manager.o sensors_comm.o
	$(CC) $(CFLAGS) $(ARCHFLAGS) manager.o sensors_comm.o -o program.elf

MANAGER_SOURCES = manager/manager.c manager/sensors_comm.c
ASSEMBLY_SOURCES = assembly/extract_data.s

manager: $(MANAGER_SOURCES) $(ASSEMBLY_SOURCES)
	gcc -o bin/manager $(MANAGER_SOURCES) $(ASSEMBLY_SOURCES) -I./manager

clean:
	rm -f *.o *.elf

run: program.elf
	qemu-riscv32 -L $(LIBRV32) ./program.elf

debug: program.elf
	@echo " On other terminal window do : "
	@echo " riscv32-linux-gdb -tui ./program.elf"
	qemu-riscv32 -L $(LIBRV32) -g 1234 program.elf -S