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
    
    # Pula '&' após token
    lbu t0, 0(s4)
    li t1, '&'
    bne t0, t1, error       # Se não for '&', erro
    addi s4, s4, 1          # Avança para depois de '&'

    # Pula "unit:"
    mv a0, s4
    jal skip_unit_label     # Procura e pula "unit:"
    beqz a0, error
    mv s4, a0               # s4 = posição após "unit:"
    
    # Extrai unidade até '&' (sua rotina extract_unit)
    mv a0, s4               # a0 = posição atual (início da unidade)
    mv a1, s2               # a1 = unit (destino)
    jal extract_unit        
    beqz a0, error          # Se falhou, erro
    mv s4, a0               # s4 = posição após '&' (que segue a unidade)
    
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

# =========================================================
# SUB-ROTINAS (CORRIGIDAS)
# =========================================================

# find_token: Procura token em str (CORRIGIDA)
# a0 = str, a1 = token
# Retorna: a0 = ponteiro após token OU 0
find_token:
    # t0: ponteiro de busca em str (str_ptr)
    # t1: caractere lido de str
    # t2: ponteiro de str para match (match_ptr)
    # t3: ponteiro de token (token_ptr)
    # t4: caractere lido de token
    mv t0, a0               # t0 = str_ptr (inicia em str)
    
find_loop:
    lbu t1, 0(t0)
    beqz t1, find_fail      # Fim da string
    
    # Compara o caractere atual t1 com o primeiro caractere do token
    lbu t4, 0(a1)           # t4 = token[0]
    bne t1, t4, find_next   # Se não for o primeiro char, avança
    
    # Possível correspondência encontrada. Inicia comparação completa.
    mv t2, t0               # t2 = match_ptr (posição atual em str)
    mv t3, a1               # t3 = token_ptr
    
compare_loop:
    lbu t1, 0(t2)           # t1 = str[i]
    lbu t4, 0(t3)           # t4 = token[j]

    beqz t4, check_prefix_delim # Token completo encontrado!

    bne t1, t4, find_next   # Não coincide, continua a busca no próximo caractere de str
    
    addi t2, t2, 1
    addi t3, t3, 1
    j compare_loop

# ------------------------------------------------------------------
# NOVO PASSO: Verificar o caractere anterior e posterior (Delimitação)
# ------------------------------------------------------------------
check_prefix_delim:
    # T2 é o endereço após o token.
    # Verificar o caractere após o token (deve ser '&')
    lbu t1, 0(t2)           # t1 = char após o token
    li t4, '&'
    bne t1, t4, find_next   # Se não for '&', não é um campo completo -> continua a busca
    
    # Verificar o caractere ANTES do token:
    # Deve ser '#', ou deve ser o início da string (t0 == a0)
    bne t0, a0, check_hash  # Se não for o início da string, verifica '#'
    
    # É o início da string, e o sufixo é '&'. SUCESSO.
    mv a0, t2               # Retorna a posição após o token
    ret
    
check_hash:
    # t0 é o início da correspondência. Verificar o caractere em t0 - 1.
    addi t3, t0, -1         # t3 = char antes do token
    lbu t1, 0(t3)           # t1 = caractere anterior
    li t4, '#'              # t4 = '#'
    bne t1, t4, find_next   # Se não for '#', não é um campo completo -> continua a busca
    
    # É depois de '#' e o sufixo é '&'. SUCESSO.
    mv a0, t2               # Retorna a posição após o token
    ret

find_next:
    addi t0, t0, 1
    j find_loop

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

# skip_unit_label: Pula "unit:"
# a0 = str
# Retorna: a0 = posição após "unit:" ou 0
skip_unit_label:
    mv t0, a0
    # Verifica a string "unit:" (5 caracteres)
    li t1, 'u'
    li t2, 'n'
    li t3, 'i'
    li t4, 't'
    li t5, ':'

    lbu t6, 0(t0)
    bne t6, t1, skip_unit_fail

    lbu t6, 1(t0)
    bne t6, t2, skip_unit_fail

    lbu t6, 2(t0)
    bne t6, t3, skip_unit_fail

    lbu t6, 3(t0)
    bne t6, t4, skip_unit_fail

    lbu t6, 4(t0)
    bne t6, t5, skip_unit_fail
    
    addi a0, t0, 5          # Pula "unit:" (5 caracteres)
    ret

skip_unit_fail:
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
    
    addi a0, t0, 6          # Pula "value:" (6 caracteres)
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
