ALTER TABLE sys_user
  ADD INDEX idx_sys_user_student_no_del_flag (student_no, del_flag),
  ADD INDEX idx_sys_user_user_name_del_flag (user_name, del_flag);

ALTER TABLE competition_register
  ADD INDEX idx_competition_register_user_time (user_id, register_time, register_id),
  ADD INDEX idx_competition_register_team_competition (team_id, competition_id);
