CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -fanalyzer -g
ARCHFLAGS = -march=rv32imd -mabi=ilp32d
LIBRV32 = ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

program.elf: main.o extract_data.o ../unity.o callfunc.o
	$(CC) $(CFLAGS) $(ARCHFLAGS) main.o extract_data.o ../unity.o callfunc.o -o program.elf

main.o: main.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c main.c -o main.o

extract_data.o: extract_data.s extract_data.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c extract_data.s -o extract_data.o

unity.o: ../unity.c ../unity.h ../unity_internals.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -I../ -c ../unity.c -o unity.o

callfunc.o: callfunc.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c callfunc.s -o callfunc.o

clean:
	rm -f *.o *.elf

run: program.elf
	qemu-riscv32 -L $(LIBRV32) ./program.elf

debug: program.elf
	@echo "On other terminal window do: "
	@echo "riscv32-linux-gdb -tui ./program.elf"
	qemu-riscv32 -L $(LIBRV32) -g 1234 program.elf -S