package org.iflytek.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.iflytek.system.domain.TeacherTeamRel;

public interface TeacherTeamRelMapper
{
    List<TeacherTeamRel> selectByTeamId(Long teamId);

    List<TeacherTeamRel> selectByTeamIds(@Param("teamIds") List<Long> teamIds);

    List<TeacherTeamRel> selectByTeacherId(Long teacherId);

    List<TeacherTeamRel> selectByTeacherIds(@Param("teacherIds") List<Long> teacherIds);

    int deleteByTeamId(Long teamId);

    int deleteByTeacherId(Long teacherId);

    int batchInsert(@Param("list") List<TeacherTeamRel> list);
}