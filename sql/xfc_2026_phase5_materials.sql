-- 已执行 xfc_2026_registration.sql 的环境，请额外执行本迁移以启用参赛资料上传。
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
