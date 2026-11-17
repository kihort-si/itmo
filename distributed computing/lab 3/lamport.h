#ifndef __LAMPORT__H
#define __LAMPORT__H

#include "ipc.h"

/** Increment lamport clock by 1 and return new value.
 *  Must be called before each send event.
 */
timestamp_t lamport_tick(void);

/** Update lamport clock on message receive:
 *  clock = max(clock, received_time) + 1
 */
void lamport_update(timestamp_t received_time);

#endif
