 .section .text
 .global extract_data

# int extract_data(char* str, char* token, char* unit, int* value)
# Argumentos:
#   a0 = char* str (string de entrada)
#   a1 = char* token (token a procurar)
#   a2 = char* unit (saída: unidade extraída)
#   a3 = int* value (saída: ponteiro para valor)
# Retorna:
#   a0 = 1 se sucesso, 0 se falha

extract_data:
    # Salva registradores
    addi sp, sp, -32
    sw ra, 28(sp)
    sw s0, 24(sp)
    sw s1, 20(sp)
    sw s2, 16(sp)
    sw s3, 12(sp)
    sw s4, 8(sp)
    sw s5, 4(sp)
    
    # Salva parâmetros
    mv s0, a0               # s0 = str
    mv s1, a1               # s1 = token
    mv s2, a2               # s2 = unit
    mv s3, a3               # s3 = value
    
    # Procura token em str
    mv a0, s0               # a0 = str
    mv a1, s1               # a1 = token
    jal find_token          # Procura token
    beqz a0, error          # Se não encontrou, erro
    mv s4, a0               # s4 = posição após token
    
    # Pula ':' após token
    lbu t0, 0(s4)
    li t1, ':'
    bne t0, t1, error       # Se não for ':', erro
    addi s4, s4, 1          # Avança para depois de ':'
    
    # Extrai unidade até '&'
    mv a0, s4               # a0 = posição atual
    mv a1, s2               # a1 = unit (destino)
    jal extract_unit        # Extrai unidade
    beqz a0, error          # Se falhou, erro
    mv s4, a0               # s4 = posição após '&'
    
    # Pula "value:"
    mv a0, s4
    jal skip_value_label    # Pula "value:"
    beqz a0, error
    mv s4, a0               # s4 = posição após "value:"
    
    # Extrai número
    mv a0, s4
    jal parse_number        # Converte string para int
    beqz a1, error          # Se falhou (a1 = flag sucesso)
    sw a0, 0(s3)            # Armazena valor em *value
    
    # Sucesso
    li a0, 1
    j cleanup

error:
    # Define unit = "" e *value = 0
    sb zero, 0(s2)          # unit[0] = '\0'
    sw zero, 0(s3)          # *value = 0
    li a0, 0

cleanup:
    # Restaura registradores
    lw ra, 28(sp)
    lw s0, 24(sp)
    lw s1, 20(sp)
    lw s2, 16(sp)
    lw s3, 12(sp)
    lw s4, 8(sp)
    lw s5, 4(sp)
    addi sp, sp, 32
    ret

# find_token: Procura token em str
# a0 = str, a1 = token
# Retorna: a0 = ponteiro após token ou 0
find_token:
    mv t0, a0               # t0 = str atual
find_loop:
    lbu t1, 0(t0)
    beqz t1, find_fail      # Fim da string
    
    # Compara com token
    mv t2, a0               # t2 = início de str
    mv t3, a1               # t3 = token
    mv t4, t0               # t4 = posição atual
compare_loop:
    lbu t5, 0(t4)
    lbu t6, 0(t3)
    beqz t6, find_success   # Token completo encontrado
    bne t5, t6, find_next   # Não coincide
    addi t4, t4, 1
    addi t3, t3, 1
    j compare_loop

find_next:
    addi t0, t0, 1
    j find_loop

find_success:
    mv a0, t4               # Retorna posição após token
    ret

find_fail:
    li a0, 0
    ret

# extract_unit: Extrai unidade até '&'
# a0 = str, a1 = unit (destino)
# Retorna: a0 = posição após '&' ou 0
extract_unit:
    mv t0, a0               # t0 = str
    mv t1, a1               # t1 = unit
    li t2, 0                # t2 = contador
extract_loop:
    lbu t3, 0(t0)
    beqz t3, extract_fail   # Fim inesperado
    li t4, '&'
    beq t3, t4, extract_end # Encontrou '&'
    
    sb t3, 0(t1)            # Copia char
    addi t0, t0, 1
    addi t1, t1, 1
    addi t2, t2, 1
    j extract_loop

extract_end:
    sb zero, 0(t1)          # Termina string unit
    addi a0, t0, 1          # Retorna posição após '&'
    ret

extract_fail:
    li a0, 0
    ret

# skip_value_label: Pula "value:"
# a0 = str
# Retorna: a0 = posição após "value:" ou 0
skip_value_label:
    mv t0, a0
    # Procura 'v'
    lbu t1, 0(t0)
    li t2, 'v'
    bne t1, t2, skip_fail
    # Procura 'a'
    lbu t1, 1(t0)
    li t2, 'a'
    bne t1, t2, skip_fail
    # Procura 'l'
    lbu t1, 2(t0)
    li t2, 'l'
    bne t1, t2, skip_fail
    # Procura 'u'
    lbu t1, 3(t0)
    li t2, 'u'
    bne t1, t2, skip_fail
    # Procura 'e'
    lbu t1, 4(t0)
    li t2, 'e'
    bne t1, t2, skip_fail
    # Procura ':'
    lbu t1, 5(t0)
    li t2, ':'
    bne t1, t2, skip_fail
    
    addi a0, t0, 6          # Pula "value:"
    ret

skip_fail:
    li a0, 0
    ret

# parse_number: Converte string para int
# a0 = str
# Retorna: a0 = número, a1 = 1 (sucesso) ou 0 (falha)
parse_number:
    li t0, 0                # t0 = resultado
    li t1, 0                # t1 = flag tem dígito
    mv t2, a0               # t2 = str
    
parse_loop:
    lbu t3, 0(t2)
    
    # Verifica se é dígito (0-9)
    li t4, '0'
    blt t3, t4, parse_done
    li t4, '9'
    bgt t3, t4, parse_done
    
    # É dígito: resultado = resultado * 10 + (char - '0')
    li t4, 10
    mul t0, t0, t4
    addi t3, t3, -48        # t3 = t3 - '0'
    add t0, t0, t3
    li t1, 1                # Marca que tem dígito
    
    addi t2, t2, 1
    j parse_loop

parse_done:
    mv a0, t0               # Retorna número
    mv a1, t1               # Retorna flag sucesso
    ret
