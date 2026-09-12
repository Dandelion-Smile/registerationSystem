ALTER TABLE `competition_register`
  DROP INDEX `competition_user`,
  DROP INDEX `competition_id`,
  ADD UNIQUE KEY `competition_team_user` (`competition_id`, `team_id`, `user_id`);
