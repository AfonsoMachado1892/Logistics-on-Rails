CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -fanalyzer -g
ARCHFLAGS = -march=rv32imd -mabi=ilp32d
LIBRV32 = ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

program.elf: main.o encrypt_data.o decrypt_data.o sort_array.o remove_oldest.o extract_data.o dequeue_value.o format_command.o
	$(CC) $(CFLAGS) $(ARCHFLAGS) main.o encrypt_data.o decrypt_data.o sort_array.o remove_oldest.o extract_data.o dequeue_value.o format_command.o -o program.elf

main.o: main.c encrypt_data.h decrypt_data.h sort_array.h remove_oldest.h extract_data.h dequeue_value.h format_command.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c main.c -o main.o

encrypt_data.o: encrypt_data.s encrypt_data.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c encrypt_data.s -o encrypt_data.o

decrypt_data.o: decrypt_data.s decrypt_data.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c decrypt_data.s -o decrypt_data.o

sort_array.o: sort_array.s sort_array.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c sort_array.s -o sort_array.o

remove_oldest.o: remove_oldest.s remove_oldest.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c remove_oldest.s -o remove_oldest.o

extract_data.o: extract_data.s extract_data.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c extract_data.s -o extract_data.o
	
dequeue_value.o: dequeue_value.s dequeue_value.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c dequeue_value.s -o dequeue_value.o

format_command.o: format_command.s format_command.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c format_command.s -o format_command.o

clean:
	rm -f *.o *.elf

run: program.elf
	qemu-riscv32 -L $(LIBRV32) ./program.elf

debug: program.elf
	@echo "On other terminal window do: "
	@echo "riscv32-linux-gdb -tui ./program.elf"
	qemu-riscv32 -L $(LIBRV32) -g 1234 program.elf -S