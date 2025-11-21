.text
    .globl decrypt_data
decrypt_data:
    # a0 = in, a1 = key, a2 = out

    # Registos: t0/t2/t3 for consts, t1 for char, a0/a1/a2 for args
    
    # ----------------------------------------------------
    # 1. Check for Invalid Inputs (Ex 6, 7)
    # ----------------------------------------------------
    
    # Check if key is valid (1 <= key <= 26)
    li t0, 1
    blt a1, t0, fail        # if key < 1 -> fail (Ex 7 fails here)
    li t0, 26
    bgt a1, t0, fail        # if key > 26 -> fail (Ex 4 passes, Ex 8 key 999 fails here)

    # Check if in == NULL (Empty string case - Ex 8)
    beqz a0, done_empty     
    
loop:
    lbu t1, 0(a0)           # load current char from input (t1)
    beqz t1, done           # if '\0', end of string

    # Check if t1 in ['A', 'Z'] (Ex 6 fails here)
    li t2, 'A'
    blt t1, t2, fail
    li t3, 'Z'
    bgt t1, t3, fail

    # ----------------------------------------------------
    # 2. Caesar Decryption Formula
    # Formula: (t1 - 'A' - key + 26) % 26 + 'A'
    # ----------------------------------------------------
    addi t1, t1, -65        # t1 = t1 - 'A' (char to position 0-25)
    sub  t1, t1, a1         # t1 = t1 - key 
    
    # Add 26 to handle negative results (wraparound to the end of the alphabet)
    # This ensures the modulo works correctly for positions < 0 (e.g., A-3)
    addi t1, t1, 26
    
    li   t2, 26
    rem  t1, t1, t2         # t1 = t1 % 26 
    
    addi t1, t1, 65         # t1 = t1 + 'A' (position back to ASCII char)

    # ----------------------------------------------------
    # 3. Store and Advance
    # ----------------------------------------------------
    sb   t1, 0(a2)          # store decrypted char in output

    addi a0, a0, 1          # move to next input char
    addi a2, a2, 1          # move to next output position
    j loop

# ----------------------------------------------------
# Success Outcomes
# ----------------------------------------------------
done_empty:
    # Special case for empty string. The key check already failed key=999, 
    # but the logic for key 1-26 should succeed on empty input.
    # We continue to 'done' to terminate the string.
    
done:
    sb x0, 0(a2)            # terminate output string '\0'
    li a0, 1                # return 1 (success)
    ret

# ----------------------------------------------------
# Failure Outcome
# ----------------------------------------------------
fail:
    sb x0, 0(a2)            # set out = ""
    li a0, 0                # return 0 (failure)
    ret