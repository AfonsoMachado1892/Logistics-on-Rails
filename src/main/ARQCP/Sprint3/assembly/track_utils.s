.section .text
.global init_track_struct

# void init_track_struct(Track* t, int id, int state, int train_id)
# a0 = Track* t
# a1 = id
# a2 = state
# a3 = train_id

init_track_struct:
    beqz a0, end_init
    sw a1, 0(a0)      # id (offset 0)
    sw a2, 4(a0)      # state (offset 4)
    sw a3, 8(a0)      # train_id (offset 8)
end_init:
    ret