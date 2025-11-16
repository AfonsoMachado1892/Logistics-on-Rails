.section .text
.global median
.global sort_array  # Import the sort_array function

# int median(int* vec, int length, int *me)

median:
    addi sp, sp, -20
    sw ra, 16(sp)
    sw s0, 12(sp)
    sw s1, 8(sp)
    sw s2, 4(sp)

    mv s0, a0             # s0 = vec
    mv s1, a1             # s1 = length
    mv s2, a2             # s2 = me

    # if (length <= 0) -> fail
    ble s1, x0, fail

    # sort_array(vec, length, 1)
    mv a0, s0             # Set arg 0: vec
    mv a1, s1             # Set arg 1: length
    li a2, 1              # Set arg 2: order = 1 (ascending)

    jal ra, sort_array    # a0 holds return value of sort_array

    # Calculate median index: index = (length - 1) / 2
    mv t0, s1             # t0 = length
    addi t0, t0, -1       # t0 = length - 1
    li t1, 2
    divu t0, t0, t1       # t0 = index

    # Get index value
    # Address = vec + (index * 4)
    slli t0, t0, 2        # t0 = index * 4 (byte offset)
    add t1, s0, t0        # t1 = address of vec[index]
    lw t2, 0(t1)          # t2 = vec[index] (the median value)

    # Store result
    sw t2, 0(s2)          # *me = t2

    li a0, 1              # Return 1
    j exit

fail:
    li a0, 0              # Return 0

exit:
    lw ra, 16(sp)
    lw s0, 12(sp)
    lw s1, 8(sp)
    lw s2, 4(sp)
    addi sp, sp, 20
    ret