gdb hello
break <_start>
break <_start + 1>
break <_start + 2>
break <_start + 3>
break <_start + 5>
run
stepi
print $rax
stepi
print $rip
stepi
print $rip
stepi
print $rip