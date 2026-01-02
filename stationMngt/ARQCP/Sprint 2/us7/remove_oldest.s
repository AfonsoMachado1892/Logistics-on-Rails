    .section .text
    .globl move_n_to_array

move_n_to_array:

    # verificar se nelem >= n
    lw t0, 0(a2)          # t0 = *nelem
    bge t0, a5, ok_to_move
    li a0, 0              # return 0
    ret

ok_to_move:
    lw t1, 0(a3)          # t1 = tail

    # n -> t2 (contador)
    mv t2, a5

    # i = 0 (t3)
    li t3, 0

loop_copy:
    beq t2, zero, finish

    # pos = (tail + i) % length
    add t4, t1, t3
    rem t4, t4, a1

    # endereço: buffer + pos*4
    slli t5, t4, 2
    add  t5, t5, a0
    lw   t6, 0(t5)        # t6 = buffer[pos]

    # array[i] = buffer[pos]
    slli t4, t3, 2
    add  t4, t4, a6
    sw   t6, 0(t4)

    addi t3, t3, 1        # i++
    addi t2, t2, -1       # contador--

    j loop_copy


finish:
    # tail = (tail + n) % length
    lw t1, 0(a3)
    add t1, t1, a5
    rem t1, t1, a1
    sw t1, 0(a3)

    # nelem = nelem - n
    lw t0, 0(a2)
    sub t0, t0, a5
    sw t0, 0(a2)

    # return 1
    li a0, 1
    ret
