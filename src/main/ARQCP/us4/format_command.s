.section .text
.global format_command

# int format_command(char* op, int n, char *cmd)
# Return 1 if success or 0 if failure in a0.

format_command:
    addi sp, sp, -40        # Allocate stack frame (40 bytes)
    sw ra, 36(sp)           # Save return address
    sw s0, 32(sp)           # Save s0 (will hold op)
    sw s1, 28(sp)           # Save s1 (will hold n)
    sw s2, 24(sp)           # Save s2 (will hold cmd)
    sw s3, 20(sp)           # Save s3 (will hold dst pointer)

    mv s0, a0               # s0 = op (src pointer)
    mv s1, a1               # s1 = n
    mv s2, a2               # s2 = cmd


    addi s3, sp, 0          # s3 = dst = start of stack buffer

    # Ignore leading spaces
inline_skip_space:
    lbu t0, 0(s0)           # t0 = *src
    li t1, ' '
    beq t0, t1, inline_space_next
    j inline_copy_loop


inline_space_next:
    addi s0, s0, 1          # src++
    j inline_skip_space

inline_copy_loop:
    lbu t0, 0(s0)           # t0 = *src
    beq t0, x0, inline_trim_done   # if '\0', done
    li t1, ' '
    beq t0, t1, inline_trim_done   # if ' ', done (trailing space)

    li t1, 'a'
    blt t0, t1, inline_not_lower
    li t1, 'z'
    bgt t0, t1, inline_not_lower
    addi t0, t0, -32        # c = c - ('a' - 'A')
inline_not_lower:
    sb t0, 0(s3)            # *dst = t0
    addi s3, s3, 1          # dst++
    addi s0, s0, 1          # src++
    j inline_copy_loop

inline_trim_done:
    sb x0, 0(s3)            # Null-terminate dst


    lbu t0, 0(sp)           # 1st char
    lbu t1, 1(sp)           # 2nd char
    lbu t2, 2(sp)           # 3rd char

    li t3, 'R'
    bne t0, t3, check_YE
    li t3, 'E'
    bne t1, t3, check_YE
    bne t2, x0, check_YE    # Must be null-terminated at 2 chars
    j handle_two_param      # Match "RE"

check_YE:
    li t3, 'Y'
    bne t0, t3, check_GE
    li t3, 'E'
    bne t1, t3, check_GE
    bne t2, x0, check_GE    # Must be null-terminated at 2 chars
    j handle_two_param      # Match "YE"

check_GE:
    li t3, 'G'
    bne t0, t3, check_RB
    li t3, 'E'
    bne t1, t3, check_RB
    bne t2, x0, check_RB    # Must be null-terminated at 2 chars
    j handle_two_param      # Match "GE"

check_RB:
    li t3, 'R'
    bne t0, t3, check_GTH
    li t3, 'B'
    bne t1, t3, check_GTH
    bne t2, x0, check_GTH   # Must be null-terminated at 2 chars
    j handle_two_param      # Match "RB"

check_GTH:
    li t3, 'G'
    bne t0, t3, fail
    li t3, 'T'
    bne t1, t3, fail
    li t3, 'H'
    bne t2, t3, fail
    lbu t3, 3(sp)           # Load 4th char
    bne t3, x0, fail        # Must be null-terminated at 3 chars
    j handle_one_param      # Match "GTH"


handle_two_param:
    blt s1, x0, fail        # if (n < 0) fail
    li t0, 99
    bgt s1, t0, fail        # if (n > 99) fail

    lbu t0, 0(sp)
    sb t0, 0(s2)
    lbu t0, 1(sp)
    sb t0, 1(s2)

    # Write ","
    li t0, ','
    sb t0, 2(s2)

    li t0, 10
    divu t1, s1, t0         # t1 = n / 10 (tens digit)
    remu t2, s1, t0         # t2 = n % 10 (ones digit)

    li t3, '0'
    add t1, t1, t3          # Convert tens digit to ASCII
    add t2, t2, t3          # Convert ones digit to ASCII

    sb t1, 3(s2)
    sb t2, 4(s2)
    sb x0, 5(s2)

    j success

handle_one_param:
    lbu t0, 0(sp)
    sb t0, 0(s2)
    lbu t0, 1(sp)
    sb t0, 1(s2)
    lbu t0, 2(sp)
    sb t0, 2(s2)
    sb x0, 3(s2)            # Write null terminator

    j success

success:
    li a0, 1                # Return 1
    j exit

fail:
    sb x0, 0(s2)            # Write null terminator (empty string)
    li a0, 0                # Return 0
    j exit

exit:
    lw ra, 36(sp)           # Restore return address
    lw s0, 32(sp)           # Restore s0
    lw s1, 28(sp)           # Restore s1
    lw s2, 24(sp)           # Restore s2
    lw s3, 20(sp)           # Restore s3
    addi sp, sp, 40         # Deallocate stack frame
    ret