#include "lamport.h"
#include "banking.h"

static timestamp_t lamport_clock = 0;

timestamp_t get_lamport_time(void) {
    return lamport_clock;
}

timestamp_t lamport_tick(void) {
    return ++lamport_clock;
}

void lamport_update(timestamp_t received_time) {
    if (received_time > lamport_clock)
        lamport_clock = received_time;
    lamport_clock++;
}
