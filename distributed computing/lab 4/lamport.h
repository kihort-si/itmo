#ifndef PA4_LAMPORT_H
#define PA4_LAMPORT_H

#include "ipc.h"

timestamp_t get_lamport_time(void);
timestamp_t lamport_tick(void);
void lamport_update(timestamp_t received_time);
void message_init(Message *message, MessageType type,
                  const char *payload, uint16_t length);

#endif
