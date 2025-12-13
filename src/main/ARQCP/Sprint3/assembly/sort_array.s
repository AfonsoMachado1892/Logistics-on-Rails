 .section .text
 
 .global sort_array
 
# int sort_array(int* vec, int length, char order)
# Argumentos:
#   a0 = int* vec (ponteiro para array)
#   a1 = int length (tamanho)
#   a2 = char order (1=ascendente, 0=descendente)
# Retorna:
#   a0 = 1 se sucesso, 0 se falha
 
 
sort_array:
	# Verifica se length <= 0
    blez a1, error           # Se length <= 0, retorna erro
    
    # Bubble Sort
    li t0, 0                 # t0 = i = 0
outer_loop:
    bge t0, a1, success      # Se i >= length, terminou
    
    li t1, 0                 # t1 = j = 0
    sub t2, a1, t0           # t2 = length - i
    addi t2, t2, -1          # t2 = length - i - 1
    
inner_loop:
    bge t1, t2, next_i       # Se j >= length - i - 1
    
    # Calcula endereços de vec[j] e vec[j+1]
    slli t3, t1, 2           # t3 = j * 4
    add t3, a0, t3           # t3 = &vec[j]
    lw t4, 0(t3)             # t4 = vec[j]
    lw t5, 4(t3)             # t5 = vec[j+1]
    
    # Verifica ordem
    beqz a2, descending      # Se order == 0, descendente
    
ascending:
    ble t4, t5, next_j       # Se vec[j] <= vec[j+1], não troca
    j swap
    
descending:
    bge t4, t5, next_j       # Se vec[j] >= vec[j+1], não troca
    
swap:
    sw t5, 0(t3)             # vec[j] = vec[j+1]
    sw t4, 4(t3)             # vec[j+1] = vec[j]
    
next_j:
    addi t1, t1, 1           # j++
    j inner_loop
    
next_i:
    addi t0, t0, 1           # i++
    j outer_loop
    
success:
    li a0, 1                 # Retorna 1
    ret
    
error:
    li a0, 0                 # Retorna 0
    ret
