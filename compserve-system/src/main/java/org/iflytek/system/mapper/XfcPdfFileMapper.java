package org.iflytek.system.mapper;

import java.util.Map;
import org.apache.ibatis.annotations.Param;

/** 讯飞杯报名表 PDF 文件记录。 */
public interface XfcPdfFileMapper
{
    Integer selectNextVersion(@Param("registrationId") Long registrationId);

    int insertPdfFile(Map<String, Object> file);

    Map<String, Object> selectPdfForCaptain(@Param("id") Long id, @Param("captainUserId") Long captainUserId);
}
