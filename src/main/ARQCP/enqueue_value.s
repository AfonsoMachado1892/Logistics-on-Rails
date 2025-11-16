.section .text
.globl enqueue_value

# int enqueue_value(int* buffer, int length, int nelem, int tail, int* head, int value)
# Parâmetros:
# a0 = buffer (int*)
# a1 = length (int)
# a2 = nelem (int)
# a3 = tail (int)
# a4 = head (int*)
# a5 = value (int)

enqueue_value:
    # Verificar se o buffer está cheio (nelem == length)
    blt a2, a1, not_full        # se nelem < length, buffer não está cheio

    # Buffer está cheio - precisa remover o elemento mais antigo
    # Incrementar head (circular)
    lw t0, 0(a4)                # t0 = *head (valor atual de head)
    addi t0, t0, 1              # head++
    blt t0, a1, update_head     # se head < length, não precisa wrap
    li t0, 0                    # head = 0 (wrap around)

update_head:
    sw t0, 0(a4)                # *head = novo valor de head

not_full:
    # Inserir o valor na posição tail
    slli t1, a3, 2              # t1 = tail * 4 (cada int = 4 bytes)
    add t1, a0, t1              # t1 = &buffer[tail]
    sw a5, 0(t1)                # buffer[tail] = value

    # Calcular novo tail (circular)
    addi a3, a3, 1              # tail++
    blt a3, a1, check_full      # se tail < length, não precisa wrap
    li a3, 0                    # tail = 0 (wrap around)

check_full:
    # Verificar se após inserção o buffer ficou cheio
    # Buffer está cheio se nelem == length OU se após incrementar nelem == length
    beq a2, a1, return_full     # se já estava cheio, continua cheio