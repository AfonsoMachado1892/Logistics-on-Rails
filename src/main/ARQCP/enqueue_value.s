.section .text
.global enqueue_value

# int enqueue_value(int* buffer, int length, int nelem, int tail, int* head, int value)
# Parâmetros:
# rdi = buffer (int*)
# rsi = length (int)
# rdx = nelem (int)
# rcx = tail (int)
# r8 = head (int*)
# r9 = value (int)

enqueue_value:
    # Verificar se o buffer está cheio (nelem == length)
    cmp %edx, %esi          # compara nelem com length
    jl .not_full            # se nelem < length, buffer não está cheio
    
    # Buffer está cheio - precisa remover o elemento mais antigo
    # Incrementar head (circular)
    movl (%r8), %r10d       # r10d = *head (valor atual de head)
    incl %r10d              # head++
    cmp %r10d, %esi         # compara head com length
    jl .update_head         # se head < length, não precisa wrap
    xorl %r10d, %r10d       # head = 0 (wrap around)
    
.update_head:
    movl %r10d, (%r8)       # *head = novo valor de head

.not_full:
    # Inserir o valor na posição tail
    movl %r9d, (%rdi, %rcx, 4)  # buffer[tail] = value (cada int = 4 bytes)
    
    # Calcular novo tail (circular)
    incl %ecx               # tail++
    cmp %ecx, %esi          # compara tail com length
    jl .check_full          # se tail < length, não precisa wrap
    xorl %ecx, %ecx         # tail = 0 (wrap around)

.check_full:
    # Verificar se após inserção o buffer ficou cheio
    # Buffer está cheio se nelem == length OU se após incrementar nelem == length
    cmp %edx, %esi          # compara nelem com length
    je .return_full         # se já estava cheio, continua cheio
    
    # Incrementar nelem e verificar
    incl %edx               # nelem++
    cmp %edx, %esi          # compara novo nelem com length
    je .return_full         # se nelem == length, buffer ficou cheio
    
    # Buffer não está cheio
    xorl %eax, %eax         # return 0
    ret

.return_full:
    movl $1, %eax           # return 1
    ret