// mocks.c - Provides definitions for global variables used in setup.c tests
#include <stddef.h>
#include "../common/domain.h"

User* users = NULL;
int num_users = 0;
Track* tracks = NULL;
int num_tracks = 0;

// Default values as used in setup.c when file is missing
SystemConfig sys_config = {10, 5, 10, 5};