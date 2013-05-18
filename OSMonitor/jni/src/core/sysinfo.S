/*
 * Thanks David Turner~
 * https://groups.google.com/forum/?fromgroups=#!topic/android-ndk/eJiGknrCWos
 */

#define __NR_sysinfo 116
#include <sys/linux-syscalls.h>

#ifdef __i386__

.text
.type sysinfo, @function
.globl sysinfo
.align 4

sysinfo:
  pushl   %ebx
  mov     8(%esp), %ebx
  movl    $__NR_sysinfo, %eax
  int     $0x80
  cmpl    $-129, %eax
  jb      1f
  negl    %eax
  pushl   %eax
  call    __set_errno
  addl    $4, %esp
  orl     $-1, %eax
1:
  popl    %ebx
  ret

#endif

#ifdef __arm__

.text
.type sysinfo, #function
.globl sysinfo
.align 4
.fnstart

sysinfo:
  .save   {r4, r7}
  stmfd   sp!, {r4, r7}
  ldr     r7, =__NR_sysinfo
  swi     #0
  ldmfd   sp!, {r4, r7}
  movs    r0, r0
  bxpl    lr

  // TODO: check this function, although prototype is same, but still need more check
  //       because __set_syscall_errno couldn't find in headers, __set_errno instead of it.
  //b       __set_syscall_errno

  b       __set_errno
.fnend

#endif
