-- 河南工业大学第三届“讯飞杯”AI+创新应用大赛报名模块
-- 阶段 1：独立业务表。执行前请在目标 MySQL 数据库备份并确认 sys_user 的实际字段。
-- 本脚本不修改 competition、competition_register、team 等原竞赛业务表。

CREATE TABLE IF NOT EXISTS `xfc_2026_registration` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报名主键',
  `registration_no` VARCHAR(32) DEFAULT NULL COMMENT '提交后生成的报名编号，如 XFC2026-00001',
  `captain_user_id` BIGINT NOT NULL COMMENT '队长 sys_user.user_id',
  `track_code` VARCHAR(16) DEFAULT NULL COMMENT 'FOOD / INDUSTRY / CITY / EDUCATION / MEDICAL',
  `team_name` VARCHAR(100) DEFAULT NULL COMMENT '队伍名称',
  `work_title` VARCHAR(150) DEFAULT NULL COMMENT '作品名称',
  `work_summary` TEXT DEFAULT NULL COMMENT '作品简介',
  `statement_truth` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '声明：报名信息真实',
  `statement_eligibility` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '声明：成员未参加本届其他有效队伍',
  `statement_copyright` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '声明：作品材料不侵权',
  `status` VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT / SUBMITTED / EXPIRED',
  `submitted_at` DATETIME DEFAULT NULL COMMENT '报名提交时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_xfc_2026_registration_no` (`registration_no`),
  KEY `idx_xfc_2026_captain_status` (`captain_user_id`, `status`, `updated_at`),
  CONSTRAINT `fk_xfc_2026_registration_captain`
    FOREIGN KEY (`captain_user_id`) REFERENCES `sys_user` (`user_id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT `ck_xfc_2026_track`
    CHECK (`track_code` IS NULL OR `track_code` IN ('FOOD', 'INDUSTRY', 'CITY', 'EDUCATION', 'MEDICAL')),
  CONSTRAINT `ck_xfc_2026_status`
    CHECK (`status` IN ('DRAFT', 'SUBMITTED', 'EXPIRED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='2026讯飞杯报名';

CREATE TABLE IF NOT EXISTS `xfc_2026_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `registration_id` BIGINT NOT NULL,
  `role` VARCHAR(12) NOT NULL COMMENT 'CAPTAIN / MEMBER',
  `user_id` BIGINT DEFAULT NULL COMMENT '可关联已有平台账号；历史快照不依赖该行后续资料变化',
  `student_no` VARCHAR(32) NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `college` VARCHAR(100) NOT NULL,
  `major` VARCHAR(100) NOT NULL,
  `class_name` VARCHAR(100) NOT NULL,
  `phone` VARCHAR(32) NOT NULL,
  `sort_no` TINYINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_xfc_2026_member_registration_role_sort` (`registration_id`, `role`, `sort_no`),
  KEY `idx_xfc_2026_member_student_no` (`student_no`),
  CONSTRAINT `fk_xfc_2026_member_registration`
    FOREIGN KEY (`registration_id`) REFERENCES `xfc_2026_registration` (`id`)
    ON UPDATE RESTRICT ON DELETE CASCADE,
  CONSTRAINT `ck_xfc_2026_member_role`
    CHECK (`role` IN ('CAPTAIN', 'MEMBER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='2026讯飞杯成员快照';

CREATE TABLE IF NOT EXISTS `xfc_2026_advisor` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `registration_id` BIGINT NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `organization` VARCHAR(150) NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `phone` VARCHAR(32) NOT NULL,
  `email` VARCHAR(100) NOT NULL,
  `sort_no` TINYINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_xfc_2026_advisor_registration_sort` (`registration_id`, `sort_no`),
  CONSTRAINT `fk_xfc_2026_advisor_registration`
    FOREIGN KEY (`registration_id`) REFERENCES `xfc_2026_registration` (`id`)
    ON UPDATE RESTRICT ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='2026讯飞杯指导教师快照';

CREATE TABLE IF NOT EXISTS `xfc_2026_pdf_file` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `registration_id` BIGINT NOT NULL,
  `version` INT NOT NULL DEFAULT 1,
  `file_path` VARCHAR(500) NOT NULL,
  `generated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_xfc_2026_pdf_registration_version` (`registration_id`, `version`),
  CONSTRAINT `fk_xfc_2026_pdf_registration`
    FOREIGN KEY (`registration_id`) REFERENCES `xfc_2026_registration` (`id`)
    ON UPDATE RESTRICT ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='2026讯飞杯PDF记录';

CREATE TABLE IF NOT EXISTS `xfc_2026_material` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `registration_id` BIGINT NOT NULL,
  `material_type` VARCHAR(16) NOT NULL COMMENT 'APPLICATION / PRESENTATION / SUPPLEMENT',
  `original_filename` VARCHAR(150) NOT NULL,
  `file_url` VARCHAR(500) NOT NULL COMMENT '对象存储地址，仅服务端保留',
  `file_size` BIGINT NOT NULL,
  `content_type` VARCHAR(150) DEFAULT NULL,
  `uploaded_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_xfc_2026_material_registration_type` (`registration_id`, `material_type`),
  CONSTRAINT `fk_xfc_2026_material_registration`
    FOREIGN KEY (`registration_id`) REFERENCES `xfc_2026_registration` (`id`)
    ON UPDATE RESTRICT ON DELETE CASCADE,
  CONSTRAINT `ck_xfc_2026_material_type`
    CHECK (`material_type` IN ('APPLICATION', 'PRESENTATION', 'SUPPLEMENT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='2026讯飞杯参赛资料';

-- 预留的报名成员占位表。本期只生成可打印 PDF，不写入或校验该表。
CREATE TABLE IF NOT EXISTS `xfc_2026_member_claim` (
  `student_no` VARCHAR(32) NOT NULL,
  `registration_id` BIGINT NOT NULL,
  `claimed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`student_no`),
  KEY `idx_xfc_2026_claim_registration` (`registration_id`),
  CONSTRAINT `fk_xfc_2026_claim_registration`
    FOREIGN KEY (`registration_id`) REFERENCES `xfc_2026_registration` (`id`)
    ON UPDATE RESTRICT ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='2026讯飞杯已报名成员唯一占位';

-- 提交事务顺序（由阶段 4 的服务端代码实现）：
-- 1. 锁定当前草稿并校验截止时间、人数、教师与声明；
-- 2. 将所有成员写入 xfc_2026_member_claim；任一主键冲突即整体回滚；
-- 3. 写入成员/教师快照，生成 registration_no，并将 status 更新为 SUBMITTED；
-- 4. 提交事务后生成 PDF。PDF 失败不回滚报名，允许后续重新生成。
