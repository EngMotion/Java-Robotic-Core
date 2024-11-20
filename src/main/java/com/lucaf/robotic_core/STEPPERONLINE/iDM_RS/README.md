## Common issues

### 1. The motor does not automatically start
0x00F Pr0.07 Forced enable by software. 

Software forced enable has a higher priority than IO
enable, and when this value is 0, the enable status of
the drive only depends on the IO signal. When this
value is 1, the motor is enabled regardless of the IO
signal status.