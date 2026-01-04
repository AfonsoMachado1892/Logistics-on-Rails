# --- Configurações do Ambiente ---
CC=riscv32-linux-gcc
CFLAGS=-Wall -Wextra -g -I. -I../manager -I../common -I../board -I../ui -DTEST
ARCHFLAGS=-march=rv32imd -mabi=ilp32d
LIBRV32=${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

# --- Ficheiros e Directórios ---
UNITY_DIR=.
ASM_DIR=../assembly
COMMON_DIR=../common
MANAGER_DIR=../manager
BOARD_DIR=../board
UI_DIR=../ui

# --- Alvos (Executáveis) ---
TARGET_BUF=test_buf.elf
TARGET_SENS=test_sens.elf
TARGET_MAN=test_manager.elf
TARGET_SETUP=test_setup.elf
TARGET_BOARD=test_board.elf
TARGET_UI=test_ui.elf

# Compilar tudo por defeito
all: $(TARGET_BUF) $(TARGET_SENS) $(TARGET_MAN) $(TARGET_SETUP) $(TARGET_BOARD) $(TARGET_UI)

# -----------------------------------------------------------
# 1. TESTES DE BUFFERS (Enqueue, Dequeue, Remove Oldest)
# -----------------------------------------------------------
OBJS_BUF=test_buffers.o unity.o enqueue_value.o dequeue_value.o move_n_to_array.o

$(TARGET_BUF): $(OBJS_BUF)
	$(CC) $(CFLAGS) $(ARCHFLAGS) $(OBJS_BUF) -o $(TARGET_BUF)

# -----------------------------------------------------------
# 2. TESTES DE SENSORES (Extract, Format)
# -----------------------------------------------------------
OBJS_SENS=test_sensors.o unity.o extract_data.o format_command.o

$(TARGET_SENS): $(OBJS_SENS)
	$(CC) $(CFLAGS) $(ARCHFLAGS) $(OBJS_SENS) -o $(TARGET_SENS)

# -----------------------------------------------------------
# 3. TESTES DO MANAGER (Logica de Negocio + Track Utils)
# -----------------------------------------------------------
# Nota: Inclui track_utils.s, encrypt_data.s e decrypt_data.s
OBJS_MAN=test_manager.o unity.o track_utils.o encrypt_data.o decrypt_data.o

$(TARGET_MAN): $(OBJS_MAN)
	$(CC) $(CFLAGS) $(ARCHFLAGS) $(OBJS_MAN) -o $(TARGET_MAN)

# -----------------------------------------------------------
# 4. TESTES DO SETUP (Configuração)
# -----------------------------------------------------------
OBJS_SETUP=test_setup.o unity.o setup.o track_utils.o encrypt_data.o mocks.o

$(TARGET_SETUP): $(OBJS_SETUP)
	$(CC) $(CFLAGS) $(ARCHFLAGS) $(OBJS_SETUP) -o $(TARGET_SETUP)

# -----------------------------------------------------------
# 5. TESTES DO BOARD (Display)
# -----------------------------------------------------------
OBJS_BOARD=test_board.o unity.o board.o

$(TARGET_BOARD): $(OBJS_BOARD)
	$(CC) $(CFLAGS) $(ARCHFLAGS) $(OBJS_BOARD) -o $(TARGET_BOARD)

# -----------------------------------------------------------
# 6. TESTES DO UI (Input)
# -----------------------------------------------------------
OBJS_UI=test_ui.o unity.o

$(TARGET_UI): $(OBJS_UI)
	$(CC) $(CFLAGS) $(ARCHFLAGS) $(OBJS_UI) -o $(TARGET_UI)

# ====================================================================
# COMPILATION RULES
# ====================================================================

unity.o: $(UNITY_DIR)/unity.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(UNITY_DIR)/unity.c -o unity.o

test_buffers.o: test_buffers.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c test_buffers.c -o test_buffers.o

test_sensors.o: test_sensors.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c test_sensors.c -o test_sensors.o

test_manager.o: test_manager.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c test_manager.c -o test_manager.o

test_setup.o: test_setup.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c test_setup.c -o test_setup.o

test_board.o: test_board.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c test_board.c -o test_board.o

test_ui.o: test_ui.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c test_ui.c -o test_ui.o

mocks.o: mocks.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c mocks.c -o mocks.o

enqueue_value.o: $(ASM_DIR)/enqueue_value.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(ASM_DIR)/enqueue_value.s -o enqueue_value.o

dequeue_value.o: $(ASM_DIR)/dequeue_value.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(ASM_DIR)/dequeue_value.s -o dequeue_value.o

move_n_to_array.o: $(ASM_DIR)/remove_oldest.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(ASM_DIR)/remove_oldest.s -o move_n_to_array.o

extract_data.o: $(ASM_DIR)/extract_data.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(ASM_DIR)/extract_data.s -o extract_data.o

format_command.o: $(ASM_DIR)/format_command.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(ASM_DIR)/format_command.s -o format_command.o

track_utils.o: $(ASM_DIR)/track_utils.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(ASM_DIR)/track_utils.s -o track_utils.o

encrypt_data.o: $(ASM_DIR)/encrypt_data.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(ASM_DIR)/encrypt_data.s -o encrypt_data.o

decrypt_data.o: $(ASM_DIR)/decrypt_data.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(ASM_DIR)/decrypt_data.s -o decrypt_data.o

setup.o: $(MANAGER_DIR)/setup.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(MANAGER_DIR)/setup.c -o setup.o

board.o: $(BOARD_DIR)/board.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(BOARD_DIR)/board.c -o board.o

# ====================================================================
# CLEAN TARGETS
# ====================================================================

clean:
	rm -f *.o *.elf

cleanall: clean
	rm -f *.log

# ====================================================================
# RUN ALL TESTS
# ====================================================================

run: all
	@echo ""
	@echo "========================================================================"
	@echo "                    ARQCP SPRINT 3 TEST SUITE"
	@echo "========================================================================"
	@echo ""
	@echo "--------------------------------------------------------------------"
	@echo "   1. BUFFER TESTS (USAC05: enqueue, USAC06: dequeue, USAC07: move_n)"
	@echo "--------------------------------------------------------------------"
	qemu-riscv32 -L $(LIBRV32) ./$(TARGET_BUF)
	@echo ""
	@echo "--------------------------------------------------------------------"
	@echo "   2. SENSOR TESTS (USAC03: extract_data, USAC04: format_command)"
	@echo "--------------------------------------------------------------------"
	qemu-riscv32 -L $(LIBRV32) ./$(TARGET_SENS)
	@echo ""
	@echo "--------------------------------------------------------------------"
	@echo "   3. MANAGER TESTS (USAC01: encrypt, USAC02: decrypt, Track utils)"
	@echo "--------------------------------------------------------------------"
	qemu-riscv32 -L $(LIBRV32) ./$(TARGET_MAN)
	@echo ""
	@echo "--------------------------------------------------------------------"
	@echo "   4. SETUP TESTS (USAC11: configuration loading & dynamic memory)"
	@echo "--------------------------------------------------------------------"
	qemu-riscv32 -L $(LIBRV32) ./$(TARGET_SETUP)
	@echo ""
	@echo "--------------------------------------------------------------------"
	@echo "   5. BOARD TESTS (USAC15: display component & ASCII art)"
	@echo "--------------------------------------------------------------------"
	qemu-riscv32 -L $(LIBRV32) ./$(TARGET_BOARD)
	@echo ""
	@echo "--------------------------------------------------------------------"
	@echo "   6. UI TESTS (USAC16: robust input handling)"
	@echo "--------------------------------------------------------------------"
	qemu-riscv32 -L $(LIBRV32) ./$(TARGET_UI)
	@echo ""
	@echo "========================================================================"
	@echo "                    ALL TESTS COMPLETED"
	@echo "========================================================================"
	@echo ""

# ====================================================================
# INDIVIDUAL TEST EXECUTION
# ====================================================================

test-buf: $(TARGET_BUF)
	@echo "Running Buffer Tests..."
	qemu-riscv32 -L $(LIBRV32) ./$(TARGET_BUF)

test-sens: $(TARGET_SENS)
	@echo "Running Sensor Tests..."
	qemu-riscv32 -L $(LIBRV32) ./$(TARGET_SENS)

test-man: $(TARGET_MAN)
	@echo "Running Manager Tests..."
	qemu-riscv32 -L $(LIBRV32) ./$(TARGET_MAN)

test-setup: $(TARGET_SETUP)
	@echo "Running Setup Tests..."
	qemu-riscv32 -L $(LIBRV32) ./$(TARGET_SETUP)

test-board: $(TARGET_BOARD)
	@echo "Running Board Tests..."
	qemu-riscv32 -L $(LIBRV32) ./$(TARGET_BOARD)

test-ui: $(TARGET_UI)
	@echo "Running UI Tests..."
	qemu-riscv32 -L $(LIBRV32) ./$(TARGET_UI)

# ====================================================================
# HELP TARGET
# ====================================================================

help:
	@echo "ARQCP Sprint 3 - Test Suite Makefile"
	@echo ""
	@echo "Available targets:"
	@echo "  make all         - Build all test executables"
	@echo "  make run         - Build and run all tests"
	@echo "  make clean       - Remove generated files"
	@echo "  make cleanall    - Remove all generated files including logs"
	@echo ""
	@echo "Individual test targets:"
	@echo "  make test-buf    - Run buffer tests only"
	@echo "  make test-sens   - Run sensor tests only"
	@echo "  make test-man    - Run manager tests only"
	@echo "  make test-setup  - Run setup tests only"
	@echo "  make test-board  - Run board tests only"
	@echo "  make test-ui     - Run UI tests only"
	@echo ""

.PHONY: all clean cleanall run help test-buf test-sens test-man test-setup test-board test-ui