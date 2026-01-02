.section .text
.globl enqueue_value

# int enqueue_value(int* buffer, int length, int* nelem_ptr, int* rd_ptr, int* wr_ptr, int value)
#
# Mapeamento de Argumentos:
# a0 = buffer (int*)           
# a1 = length (int)            
# a2 = nelem_ptr (int*)        # Endereço do número de elementos (*nelem)
# a3 = rd_ptr (int*)           # Endereço de Leitura/Head (*rd)
# a4 = wr_ptr (int*)           # Endereço de Escrita/Tail (*wr)
# a5 = value (int)             

enqueue_value:
    # 1. Carregar valores das variáveis de estado
    lw t0, 0(a2)                # t0 = *nelem (Número de elementos atuais)
    lw t1, 0(a3)                # t1 = *rd (Índice de leitura)
    lw t2, 0(a4)                # t2 = *wr (Índice de escrita)
    
    # 2. Inicializar valor de retorno
    li t6, 0                    # t6 = 0 (retorno: sucesso)
    
    # 3. Verificar se o buffer está cheio (*nelem >= length)
    blt t0, a1, not_full        # Se *nelem < length, não está cheio
    
    # Buffer está cheio - marcar overflow
    li t6, 1                    # t6 = 1 (retorno: overflow)
    
    # Avançar o ponteiro de leitura (circular)
    addi t1, t1, 1              # rd++
    blt t1, a1, skip_rd_wrap    # Se rd < length, não precisa wrap
    li t1, 0                    # rd = 0 (wrap around)
    
skip_rd_wrap:
    sw t1, 0(a3)                # Salvar novo *rd
    j insert_value              # Ir para inserção
    
not_full:
    # Buffer não está cheio - incrementar nelem
    addi t0, t0, 1              # nelem++
    sw t0, 0(a2)                # Salvar novo *nelem
    
    # Verificar se ficou cheio após incrementar
    beq t0, a1, became_full     # Se nelem == length agora, ficou cheio
    j insert_value
    
became_full:
    li t6, 1                    # t6 = 1 (retorno: ficou cheio)
    
insert_value:
    # 4. Inserir o valor na posição de escrita
    slli t3, t2, 2              # t3 = wr * 4 (offset em bytes)
    add t3, a0, t3              # t3 = &buffer[wr]
    sw a5, 0(t3)                # buffer[wr] = value
    
    # 5. Avançar o ponteiro de escrita (circular)
    addi t2, t2, 1              # wr++
    blt t2, a1, skip_wr_wrap    # Se wr < length, não precisa wrap
    li t2, 0                    # wr = 0 (wrap around)
    
skip_wr_wrap:
    sw t2, 0(a4)                # Salvar novo *wr
    
    # 6. Retornar
    mv a0, t6                   # a0 = valor de retorno (0 ou 1)
    ret
