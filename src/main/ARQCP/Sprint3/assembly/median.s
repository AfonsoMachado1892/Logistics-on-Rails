.section .text
.global median

# int median(int* vec, int length, int *me)
# a0 = vec, a1 = length, a2 = me

median:
    # Save registers (ra, s0-s4)
    addi sp, sp, -24
    sw ra, 20(sp)
    sw s0, 16(sp)      # s0 = vec (base address of array)
    sw s1, 12(sp)      # s1 = length (N)
    sw s2, 8(sp)       # s2 = me (pointer to result)
    sw s3, 4(sp)       # s3 = Outer loop counter (i)
    sw s4, 0(sp)       # s4 = Inner loop counter (j)

    mv s0, a0
    mv s1, a1
    mv s2, a2

    # Check: if (length <= 0) -> fail
    ble s1, x0, fail

    # t0 = length - 1 (Outer loop limit)
    addi t0, s1, -1          
    li s3, 0                 # s3 = i = 0 (Outer loop counter)

outer_loop:
    bge s3, t0, sort_end     # If i >= length - 1, sort done
    
    # t1 = (length - 1) - i (Inner loop limit)
    sub t1, t0, s3          
    li s4, 0                 # s4 = j = 0 (Inner loop counter)

inner_loop:
    bge s4, t1, inner_loop_end # If j >= length - i - 1, inner loop done

    # Calculate address of vec[j]: t2 = &vec[j]
    slli t2, s4, 2           # t2 = j * 4 (byte offset)
    add t2, s0, t2           # t2 = &vec[j]
    
    # Load vec[j] and vec[j+1]
    lw t3, 0(t2)             # t3 = vec[j]
    lw t4, 4(t2)             # t4 = vec[j+1]

    # Comparison: If vec[j] > vec[j+1], swap (Ascending sort, signed comparison)
    ble t3, t4, skip_swap    # If vec[j] <= vec[j+1], skip swap
    
    # SWAP:
    sw t4, 0(t2)             # vec[j] = vec[j+1] (t4)
    sw t3, 4(t2)             # vec[j+1] = vec[j] (t3, original value)

skip_swap:
    # j++ and continue inner loop
    addi s4, s4, 1           # j++
    j inner_loop

inner_loop_end:
    # i++ and continue outer loop
    addi s3, s3, 1           # i++
    j outer_loop
    
sort_end:

	#calculate median
    
    # Check if length is odd (length % 2 != 0)
    andi t0, s1, 1           # t0 = length % 2 (1 if odd, 0 if even)
    bne t0, x0, median_odd   # If odd, jump to the simple median logic

    # Average of two middle elements if even length, floored)
    
    # Lower index calculation: t0 = (length / 2) - 1
    srli t0, s1, 1           # t0 = length / 2 
    addi t0, t0, -1          # t0 = lower index (e.g., 6/2 - 1 = 2)

    # Calculate address of vec[lower_index] in t3
    slli t3, t0, 2           # t3 = lower index * 4
    add t3, s0, t3           # t3 = address of vec[lower_index]
    
    # Load lower and upper values
    lw t2, 0(t3)             # t2 = lower_val
    lw t5, 4(t3)             # t5 = upper_val
    
    # Calculate sum: t6 = lower_val + upper_val (e.g., -3 + -2 = -5)
    add t6, t2, t5           
    
    # Calculate average (Truncates toward zero): t2 = sum / 2. 
    li t4, 2
    div t2, t6, t4           
    
        
    # If t6 >= 0, branch to median_finish (No flooring needed for positive sum)
    bge t6, x0, median_finish 
    
    # If t6 < 0, check if it was an odd number (t6 & 1 == 1)
    # Reuse t4 to check LSB of sum (t6)
    andi t4, t6, 1           
    
    # If sum was odd and negative, decrement the result to floor it.
    bne t4, x0, decrement_result 
    
    j median_finish          # If sum was even and negative, result is fine.

decrement_result:
    addi t2, t2, -1          # t2 = t2 - 1 (e.g., -2 - 1 = -3)
    j median_finish

    # Odd Length Median (middle element)
    
median_odd:
    # Index calculation: t0 = (length - 1) / 2
    addi t0, s1, -1          # t0 = length - 1
    li t1, 2
    div t0, t0, t1           # t0 = index

    # Get index value: t2 = vec[t0]
    slli t3, t0, 2           # t3 = index * 4 (byte offset)
    add t4, s0, t3           # t4 = address of vec[index]
    lw t2, 0(t4)             # t2 = median value

median_finish:
    # Store result
    sw t2, 0(s2)             # *me = t2

    li a0, 1                 # Return 1 (Success)
    j exit

fail:
    li a0, 0                 # Return 0 (Failure)

exit:
    # Restore registers
    lw ra, 20(sp)
    lw s0, 16(sp)
    lw s1, 12(sp)
    lw s2, 8(sp)
    lw s3, 4(sp)
    lw s4, 0(sp)
    addi sp, sp, 24
    ret
