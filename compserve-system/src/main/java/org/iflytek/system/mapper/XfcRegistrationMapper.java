package org.iflytek.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/** 第三届讯飞杯报名草稿数据访问。 */
public interface XfcRegistrationMapper
{
    Map<String, Object> selectCurrentDraft(@Param("captainUserId") Long captainUserId);

    int insertRegistration(Map<String, Object> registration);

    int updateDraft(Map<String, Object> registration);

    List<Map<String, Object>> selectMembers(@Param("registrationId") Long registrationId);

    List<Map<String, Object>> selectAdvisors(@Param("registrationId") Long registrationId);

    int deleteMembers(@Param("registrationId") Long registrationId);

    int deleteAdvisors(@Param("registrationId") Long registrationId);

    int insertMember(Map<String, Object> member);

    int insertAdvisor(Map<String, Object> advisor);

    int updateRegistrationNo(@Param("id") Long id, @Param("registrationNo") String registrationNo);

    List<Map<String, Object>> selectMaterials(@Param("registrationId") Long registrationId);

    Map<String, Object> selectMaterial(@Param("registrationId") Long registrationId, @Param("materialType") String materialType);

    int upsertMaterial(Map<String, Object> material);
}
