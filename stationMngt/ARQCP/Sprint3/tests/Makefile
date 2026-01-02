# --- Configurações do Ambiente ---
CC=riscv32-linux-gcc
CFLAGS=-Wall -Wextra -g -I. -I../manager -I../common -DTEST
ARCHFLAGS=-march=rv32imd -mabi=ilp32d
LIBRV32=${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

# --- Ficheiros e Diretórios ---
UNITY_DIR=.
ASM_DIR=../assembly
COMMON_DIR=../common

# --- Alvos (Executáveis) ---
TARGET_ALGO=test_algo.elf
TARGET_BUF=test_buf.elf
TARGET_SENS=test_sens.elf
TARGET_MAN=test_manager.elf

# Compilar tudo por defeito
all: $(TARGET_ALGO) $(TARGET_BUF) $(TARGET_SENS) $(TARGET_MAN)

# -----------------------------------------------------------
# 1. TESTES DE ALGORITMOS (Sort, Median)
# -----------------------------------------------------------
OBJS_ALGO=test_algorithms.o unity.o sort_array.o median.o

$(TARGET_ALGO): $(OBJS_ALGO)
	$(CC) $(CFLAGS) $(ARCHFLAGS) $(OBJS_ALGO) -o $(TARGET_ALGO)

# -----------------------------------------------------------
# 2. TESTES DE BUFFERS (Enqueue, Dequeue, Remove Oldest)
# -----------------------------------------------------------
OBJS_BUF=test_buffers.o unity.o enqueue_value.o dequeue_value.o remove_oldest.o

$(TARGET_BUF): $(OBJS_BUF)
	$(CC) $(CFLAGS) $(ARCHFLAGS) $(OBJS_BUF) -o $(TARGET_BUF)

# -----------------------------------------------------------
# 3. TESTES DE SENSORES (Extract, Format)
# -----------------------------------------------------------
OBJS_SENS=test_sensors.o unity.o extract_data.o format_command.o

$(TARGET_SENS): $(OBJS_SENS)
	$(CC) $(CFLAGS) $(ARCHFLAGS) $(OBJS_SENS) -o $(TARGET_SENS)

# -----------------------------------------------------------
# 4. TESTES DO MANAGER (Logica de Negocio + Track Utils)
# -----------------------------------------------------------
# Nota: Inclui track_utils.s, encrypt_data.s e decrypt_data.s
OBJS_MAN=test_manager.o unity.o track_utils.o encrypt_data.o decrypt_data.o

$(TARGET_MAN): $(OBJS_MAN)
	$(CC) $(CFLAGS) $(ARCHFLAGS) $(OBJS_MAN) -o $(TARGET_MAN)

# -----------------------------------------------------------
# REGRAS DE COMPILAÇÃO GENÉRICAS
# -----------------------------------------------------------

# Compilar ficheiros C de teste (localizados nesta pasta 'tests')
%.o: %.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $< -o $@

# Compilar Unity (localizado em ../manager)
unity.o: $(UNITY_DIR)/unity.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(UNITY_DIR)/unity.c -o unity.o

# --- Compilar Assembly (localizado em ../assembly) ---

sort_array.o: $(ASM_DIR)/sort_array.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $< -o $@

median.o: $(ASM_DIR)/median.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $< -o $@

enqueue_value.o: $(ASM_DIR)/enqueue_value.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $< -o $@

dequeue_value.o: $(ASM_DIR)/dequeue_value.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $< -o $@

remove_oldest.o: $(ASM_DIR)/remove_oldest.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $< -o $@

extract_data.o: $(ASM_DIR)/extract_data.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $< -o $@

format_command.o: $(ASM_DIR)/format_command.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $< -o $@

track_utils.o: $(ASM_DIR)/track_utils.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $< -o $@

encrypt_data.o: $(ASM_DIR)/encrypt_data.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $< -o $@

decrypt_data.o: $(ASM_DIR)/decrypt_data.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $< -o $@

# -----------------------------------------------------------
# COMANDOS DE UTILIDADE
# -----------------------------------------------------------

clean:
	rm -f *.o *.elf

# Corre todos os testes no QEMU
run: all
	@echo "========================================"
	@echo "   RUNNING ALGORITHM TESTS"
	@echo "========================================"
	qemu-riscv32 -L $(LIBRV32) ./$(TARGET_ALGO)
	@echo ""
	@echo "========================================"
	@echo "   RUNNING BUFFER TESTS"
	@echo "========================================"
	qemu-riscv32 -L $(LIBRV32) ./$(TARGET_BUF)
	@echo ""
	@echo "========================================"
	@echo "   RUNNING SENSOR TESTS"
	@echo "========================================"
	qemu-riscv32 -L $(LIBRV32) ./$(TARGET_SENS)
	@echo ""
	@echo "========================================"
	@echo "   RUNNING MANAGER TESTS"
	@echo "========================================"
	qemu-riscv32 -L $(LIBRV32) ./$(TARGET_MAN)