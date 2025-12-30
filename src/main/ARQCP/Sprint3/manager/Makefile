CC=riscv32-linux-gcc
CFLAGS=-Wall -Wextra -g -I. -I../common -DTEST
ARCHFLAGS=-march=rv32imd -mabi=ilp32d
LIBRV32=${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

# Nome do executável final
TARGET=run_tests.elf

# Lista de objetos
OBJS=test_manager.o unity.o track_utils.o encrypt_data.o decrypt_data.o

# Regra principal (Linkagem)
$(TARGET): $(OBJS)
	$(CC) $(CFLAGS) $(ARCHFLAGS) $(OBJS) -o $(TARGET)

# --- Regras de Compilação Individual (como no teu template) ---

test_manager.o: test_manager.c unity.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c test_manager.c -o test_manager.o

unity.o: unity.c unity.h unity_internals.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c unity.c -o unity.o

# Para os ficheiros Assembly, temos de ir buscá-los à pasta ../assembly/

track_utils.o: ../assembly/track_utils.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c ../assembly/track_utils.s -o track_utils.o

encrypt_data.o: ../assembly/encrypt_data.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c ../assembly/encrypt_data.s -o encrypt_data.o

decrypt_data.o: ../assembly/decrypt_data.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c ../assembly/decrypt_data.s -o decrypt_data.o

# Limpeza
clean:
	rm -f *.o *.elf

# Executar no QEMU
run: $(TARGET)
	qemu-riscv32 -L $(LIBRV32) ./$(TARGET)

# Debug
debug: $(TARGET)
	@echo "On other terminal window do: "
	@echo "riscv32-linux-gdb -tui ./$(TARGET)"
	qemu-riscv32 -L $(LIBRV32) -g 1234 $(TARGET) -S