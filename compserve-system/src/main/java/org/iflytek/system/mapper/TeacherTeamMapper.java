package org.iflytek.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.iflytek.system.domain.TeacherTeam;

public interface TeacherTeamMapper
{
    TeacherTeam selectBaseTeacherByPhone(String phone);

    List<TeacherTeam> selectBaseTeachersByPhone(String phone);

    TeacherTeam selectBaseTeacherByUserName(String userName);

    List<TeacherTeam> selectBaseTeachersByUserName(String userName);

    List<TeacherTeam> selectBaseTeachersByName(String teacherName);

    List<TeacherTeam> selectBaseTeachersByIds(@Param("ids") List<Long> ids);

    TeacherTeam selectBaseTeacherById(Long id);

    int insertBaseTeacher(TeacherTeam teacherTeam);

    int updateBaseTeacherByPhone(TeacherTeam teacherTeam);

    int updateBaseTeacherById(TeacherTeam teacherTeam);

    int deleteBaseTeacherByPhone(String phone);

    int deleteByPhone(String phone);
}
