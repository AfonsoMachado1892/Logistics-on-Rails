.section .text
.global dequeue_value

# int dequeue_value(int* buffer, int length, int* nelem, int* tail, int* head, int* value)
# a0 = buffer
# a1 = length
# a2 = nelem (pointer)
# a3 = tail (pointer)
# a4 = head (pointer)
# a5 = value (pointer)

dequeue_value:
    # Verificar se o buffer está vazio (nelem == 0)
    lw t0, 0(a2)          # t0 = *nelem
    beqz t0, empty_buffer  # se nelem == 0, retornar 0
    
    # Buffer não está vazio, proceder com a remoção
    lw t1, 0(a3)          # t1 = *tail (índice do elemento mais antigo)
    
    # Calcular offset: tail * 4 (cada int tem 4 bytes)
    slli t2, t1, 2        # t2 = tail * 4
    add t2, a0, t2        # t2 = &buffer[tail]
    
    # Ler o valor do elemento mais antigo
    lw t3, 0(t2)          # t3 = buffer[tail]
    
    # Guardar o valor no pointer de output
    sw t3, 0(a5)          # *value = buffer[tail]
    
    # Atualizar tail: tail = (tail + 1) % length
    addi t1, t1, 1        # tail++
    rem t1, t1, a1        # tail = tail % length
    sw t1, 0(a3)          # *tail = novo valor
    
    # Decrementar nelem
    addi t0, t0, -1       # nelem--
    sw t0, 0(a2)          # *nelem = novo valor
    
    # Retornar 1 (sucesso)
    li a0, 1
    ret

empty_buffer:
    # Buffer vazio, retornar 0
    li a0, 0
    ret
