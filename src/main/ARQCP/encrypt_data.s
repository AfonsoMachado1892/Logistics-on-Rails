.section .text
.global encrypt_data

encrypt_data:
    # a0 = in, a1 = key, a2 = out
    # Check if in == NULL
    beqz a0, fail           # if in == 0 -> fail
    
    # Check if key < 1
    li t0, 1
    blt a1, t0, fail
    
    # Check if key > 26
    li t0, 26
    bgt a1, t0, fail
    
loop:
    lbu t1, 0(a0)           # load current char from input
    beqz t1, done           # if '\0', end of string
    
    # Check if t1 in ['A', 'Z']
    li t2, 'A'
    blt t1, t2, fail
    li t3, 'Z'
    bgt t1, t3, fail
    
    # Compute encrypted char:
    # (t1 - 'A' + key) % 26 + 'A'
    addi t1, t1, -65        # t1 = t1 - 'A'
    add  t1, t1, a1         # t1 = t1 + key
    li   t2, 26
    rem  t1, t1, t2         # t1 = t1 % 26
    addi t1, t1, 65         # t1 = t1 + 'A'
    
    sb   t1, 0(a2)          # store encrypted char in output
    addi a0, a0, 1          # move to next input char
    addi a2, a2, 1          # move to next output position
    j loop
    
done:
    sb x0, 0(a2)            # terminate output string '\0'
    li a0, 1                # return 1 (success)
    ret
    
fail:
    sb x0, 0(a2)            # set out = ""
    li a0, 0                # return 0 (failure)
    ret
