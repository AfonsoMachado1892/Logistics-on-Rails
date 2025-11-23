CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -fanalyzer -g
ARCHFLAGS = -march=rv32imd -mabi=ilp32d
LIBRV32 = ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

program.elf: main.o enqueue_value.o ../unity.o callfunc.o
	$(CC) $(CFLAGS) $(ARCHFLAGS) main.o enqueue_value.o ../unity.o callfunc.o -o program.elf

main.o: main.c enqueue_value.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c main.c -o main.o

enqueue_value.o: enqueue_value.s enqueue_value.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c enqueue_value.s -o enqueue_value.o
	
unity.o: ../unity.c ../unity.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c ../unity.c -o unity.o
	
callfunc.o: callfunc.s callfunc.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c .callfunc.s -o callfunc.o

clean:
	rm -f *.o *.elf

run: program.elf
	qemu-riscv32 -L $(LIBRV32) ./program.elf

debug: program.elf
	@echo "On other terminal window do: "
	@echo "riscv32-linux-gdb -tui ./program.elf"
	qemu-riscv32 -L $(LIBRV32) -g 1234 program.elf -S
