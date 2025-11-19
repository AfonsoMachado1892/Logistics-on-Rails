CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -fanalyzer -g
ARCHFLAGS = -march=rv32imd -mabi=ilp32d
LIBRV32 = ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

program.elf: main.o sort_array.o 
	$(CC) $(CFLAGS) $(ARCHFLAGS) main.o sort_array.o 

main.o: main.c main.o
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c main.c -o main.o

sort_array.o: sort_array.s sort_array.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c sort_array.s -o sort_array.o

clean:
	rm -f *.o *.elf

run: program.elf
	qemu-riscv32 -L $(LIBRV32) ./program.elf

debug: program.elf
	@echo "On other terminal window do: "
	@echo "riscv32-linux-gdb -tui ./program.elf"
	qemu-riscv32 -L $(LIBRV32) -g 1234 program.elf -S