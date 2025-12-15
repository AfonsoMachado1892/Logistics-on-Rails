
.section .text
.global init_track_struct
.global update_track_struct

# void init_track_struct(Track* t, int id, int state, int train_id)
# a0 = Track* t
# a1 = id
# a2 = state
# a3 = train_id
# Offsets: id=0, state=4, train_id=8

init_track_struct:
    beqz a0, end_init
    sw a1, 0(a0)      # id (offset 0)
    sw a2, 4(a0)      # state (offset 4)
    sw a3, 8(a0)      # train_id (offset 8)
end_init:
    ret

# void update_track_struct(Track* t, int new_state, int new_train_id)
# Atualiza o estado e o ID do comboio de uma track existente
# a0 = Track* t
# a1 = new_state
# a2 = new_train_id

update_track_struct:
    beqz a0, end_update
    
    # Atualiza state (offset 4)
    sw a1, 4(a0)
    
    # Atualiza train_id (offset 8)
    sw a2, 8(a0)

end_update:
    ret
