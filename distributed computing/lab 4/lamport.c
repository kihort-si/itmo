#include "lamport.h"

#include <string.h>

static timestamp_t clock_value = 0;

timestamp_t get_lamport_time(void) {
    return clock_value;
}

timestamp_t lamport_tick(void) {
    return ++clock_value;
}

void lamport_update(timestamp_t received_time) {
    if (clock_value < received_time)
        clock_value = received_time;
    ++clock_value;
}

void message_init(Message *message, MessageType type,
                  const char *payload, uint16_t length) {
    message->s_header.s_magic = MESSAGE_MAGIC;
    message->s_header.s_type = type;
    message->s_header.s_payload_len = length;
    message->s_header.s_local_time = lamport_tick();
    if (length != 0)
        memcpy(message->s_payload, payload, length);
}
