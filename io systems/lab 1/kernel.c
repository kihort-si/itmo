#include "kernel.h"

extern char __bss[], __bss_end[], __stack_top[];

void kernel_main(void);
void kernel_start(void);

__attribute__((section(".text.boot"), naked)) void boot(void) {
    __asm__ __volatile__(
        "la sp, __stack_top\n"
        "j kernel_start\n");
}

void kernel_start(void) {
    for (char *p = __bss; p < __bss_end; p++) {
        *p = 0;
    }
    kernel_main();
}

struct sbiret sbi_call(long arg0, long arg1, long arg2, long arg3, long arg4,
                       long arg5, long fid, long eid) {
    register long a0 __asm__("a0") = arg0;
    register long a1 __asm__("a1") = arg1;
    register long a2 __asm__("a2") = arg2;
    register long a3 __asm__("a3") = arg3;
    register long a4 __asm__("a4") = arg4;
    register long a5 __asm__("a5") = arg5;
    register long a6 __asm__("a6") = fid;
    register long a7 __asm__("a7") = eid;

    __asm__ __volatile__("ecall"
                         : "=r"(a0), "=r"(a1)
                         : "r"(a0), "r"(a1), "r"(a2), "r"(a3), "r"(a4), "r"(a5),
                           "r"(a6), "r"(a7)
                         : "memory");
    return (struct sbiret){.error = a0, .value = a1};
}

void putchar(char ch) {
    sbi_call(ch, 0, 0, 0, 0, 0, 0, 1 /* Console Putchar */);
}

int getchar(void) {
    struct sbiret ret = sbi_call(0, 0, 0, 0, 0, 0, 0, 2 /* Console Getchar */);
    return (int)ret.error;
}

#define SBI_EID_BASE   0x10
#define SBI_EID_HSM    0x48534D
#define SBI_EID_SRST   0x53525354

#define SBI_FID_GET_IMPL_VERSION   2
#define SBI_FID_HSM_HART_STOP      1
#define SBI_FID_HSM_HART_GET_STATUS 2

#define SBI_FID_SYSTEM_RESET       0
#define SRST_RESET_TYPE_SHUTDOWN   0x00000000
#define SRST_RESET_REASON_NONE     0x00000000

static void puts(const char *s) {
    for (int i = 0; s[i] != '\0'; i++) putchar(s[i]);
}

static void print_hex(unsigned long x) {
    const char *hex = "0123456789ABCDEF";
    puts("0x");
    for (int i = (int)(sizeof(unsigned long) * 2) - 1; i >= 0; i--) {
        unsigned v = (x >> (i * 4)) & 0xF;
        putchar(hex[v]);
    }
}

static void print_dec(unsigned long x) {
    char buf[32];
    int n = 0;
    if (x == 0) { putchar('0'); return; }
    while (x > 0 && n < (int)sizeof(buf)) {
        buf[n++] = (char)('0' + (x % 10));
        x /= 10;
    }
    for (int i = n - 1; i >= 0; i--) putchar(buf[i]);
}

static int getchar_blocking(void) {
    for (;;) {
        int ch = getchar();
        if (ch >= 0) return ch;
    }
}

static unsigned long read_u32_dec_bs(void) {
    char buf[32];
    int len = 0;

    for (;;) {
        int ch = getchar_blocking();

        if (ch == '\r' || ch == '\n') {
            putchar('\r'); putchar('\n');
            unsigned long v = 0;
            for (int i = 0; i < len; i++) v = v * 10 + (unsigned long)(buf[i] - '0');
            return v;
        }

        if (ch == '\b' || ch == 0x7f) {
            if (len > 0) {
                len--;
                putchar('\b'); putchar(' '); putchar('\b');
            }
            continue;
        }

        if (ch >= '0' && ch <= '9') {
            if (len < (int)sizeof(buf)) {
                buf[len++] = (char)ch;
                putchar((char)ch);
            }
            continue;
        }
    }
}

static struct sbiret sbi_get_impl_version(void) {
    return sbi_call(0, 0, 0, 0, 0, 0, SBI_FID_GET_IMPL_VERSION, SBI_EID_BASE);
}

static struct sbiret sbi_hart_get_status(unsigned long hartid) {
    return sbi_call(hartid, 0, 0, 0, 0, 0, SBI_FID_HSM_HART_GET_STATUS, SBI_EID_HSM);
}

static struct sbiret sbi_hart_stop(void) {
    return sbi_call(0, 0, 0, 0, 0, 0, SBI_FID_HSM_HART_STOP, SBI_EID_HSM);
}

static struct sbiret sbi_system_shutdown(void) {
    return sbi_call(SRST_RESET_TYPE_SHUTDOWN, SRST_RESET_REASON_NONE, 0, 0, 0, 0,
                    SBI_FID_SYSTEM_RESET, SBI_EID_SRST);
}

static void print_hart_status(unsigned long st) {
    puts("status = "); 
    print_dec(st); 
    puts(" (");
    if (st == 0) puts("STARTED");
    else if (st == 1) puts("STOPPED");
    else if (st == 2) puts("STOP_PENDING");
    else if (st == 3) puts("SUSPENDED");
    else puts("UNKNOWN");
    puts(")\r\n");
}

static void print_sbi_error(long err) {
    if (err == 0) puts("SUCCESS");
    else if (err == -1) puts("FAILED");
    else if (err == -2) puts("NOT_SUPPORTED");
    else if (err == -3) puts("INVALID_PARAM");
    else if (err == -4) puts("DENIED");
    else if (err == -5) puts("INVALID_ADDRESS");
    else if (err == -6) puts("ALREADY_AVAILABLE");
    else {
        puts("UNKNOWN (");
        print_dec(err);
        puts(")");
    }
}

static void print_menu(void) {
    puts("\r\n=== OpenSBI menu ===\r\n");
    puts("1. Get SBI implementation version\r\n");
    puts("2. Hart get status\r\n");
    puts("3. Hart stop\r\n");
    puts("4. System Shutdown\r\n");
    puts("> ");
}

void kernel_main(void) {
    puts("\r\nOpenSBI HSM/Base demo started.\r\n");

    for (;;) {
        print_menu();
        unsigned long choice = read_u32_dec_bs();

        if (choice == 1) {
            struct sbiret r = sbi_get_impl_version();
            puts("impl_version raw: "); 
            print_hex((unsigned long)r.value); 
            puts("\r\n");
            puts("impl_version dec: "); 
            print_dec((unsigned long)r.value); 
            puts("\r\n");

        } else if (choice == 2) {
            puts("Enter hart id: ");
            unsigned long hartid = read_u32_dec_bs();
            struct sbiret r = sbi_hart_get_status(hartid);
            if (r.error != 0) {
                puts("Error: "); 
                long err = (long)r.error;
                print_sbi_error((long)r.error);
                puts("\r\n");
            } else {
                puts("Hart "); 
                print_dec(hartid); 
                puts(": ");
                print_hart_status((unsigned long)r.value);
            }

        } else if (choice == 3) {
            puts("Hart stop requested for current hart...\r\n");
            struct sbiret r = sbi_hart_stop();
            puts("Returned: error="); 
            print_dec((unsigned long)r.error);
            puts(" value="); 
            print_dec((unsigned long)r.value); 
            puts("\r\n");

        } else if (choice == 4) {
            puts("Shutdown requested...\r\n");
            sbi_system_shutdown();
            puts("Shutdown returned (not supported?)\r\n");

        } else {
            puts("Unknown option.\r\n");
        }
    }
}
