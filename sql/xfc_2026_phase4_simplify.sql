-- 阶段 4：将系统从“线上报名审核”简化为“填写信息并生成打印表”。
-- 仅移除阻止不同学生填写相同信息的约束；不删除任何既有草稿或用户数据。
ALTER TABLE `xfc_2026_registration` DROP INDEX `uk_xfc_2026_team_name`;
ALTER TABLE `xfc_2026_member` DROP INDEX `uk_xfc_2026_member_registration_student`;
