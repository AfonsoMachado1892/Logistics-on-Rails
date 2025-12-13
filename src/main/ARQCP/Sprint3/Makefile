MANAGER_SOURCES = manager/manager.c manager/sensors_comm.c
ASSEMBLY_SOURCES = assembly/extract_data.s

manager: $(MANAGER_SOURCES) $(ASSEMBLY_SOURCES)
	gcc -o bin/manager $(MANAGER_SOURCES) $(ASSEMBLY_SOURCES) -I./manager