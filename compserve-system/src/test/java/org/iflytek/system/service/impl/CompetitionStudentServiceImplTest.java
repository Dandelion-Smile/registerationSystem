package org.iflytek.system.service.impl;

import org.iflytek.common.core.domain.entity.SysUser;
import org.iflytek.system.domain.CompetitionRegister;
import org.iflytek.system.mapper.CompetitionMapper;
import org.iflytek.system.mapper.CompetitionParticipationMapper;
import org.iflytek.system.mapper.CompetitionRegisterMapper;
import org.iflytek.system.mapper.CompetitionReviewAssignmentMapper;
import org.iflytek.system.mapper.CompetitionWorkMapper;
import org.iflytek.system.mapper.SysUserMapper;
import org.iflytek.system.mapper.TeacherTeamMapper;
import org.iflytek.system.mapper.TeacherTeamRelMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CompetitionStudentServiceImplTest
{
    @Test
    void getRecentRegistrationsShouldReuseTeamLookupWithinSingleRequest() throws Exception
    {
        CompetitionStudentServiceImpl service = new CompetitionStudentServiceImpl();

        AtomicInteger selectByTeamIdCalls = new AtomicInteger();

        CompetitionRegister ownRegister = new CompetitionRegister();
        ownRegister.setRegisterId(101L);
        ownRegister.setCompetitionId(9L);
        ownRegister.setTeamId(88L);
        ownRegister.setTeamName("alpha");
        ownRegister.setTeamMembers("""
                [
                  {"studentNo":"20240001","name":"队长","college":"人工智能学院","major":"软件工程","phone":"13800000001","email":"leader@test.com","role":"leader","status":"approved"},
                  {"studentNo":"20240002","name":"队员","college":"人工智能学院","major":"软件工程","phone":"13800000002","email":"member@test.com","role":"member","status":"approved"}
                ]
                """);

        Map<String, Object> rawRow = new HashMap<>();
        rawRow.put("registerId", 101L);
        rawRow.put("competitionId", 9L);
        rawRow.put("teamId", 88L);
        rawRow.put("teamName", "alpha");
        rawRow.put("teamMembers", ownRegister.getTeamMembers());
        rawRow.put("registerTime", "2026-06-24 09:00:00");
        rawRow.put("competitionName", "测试竞赛");

        CompetitionRegisterMapper registerMapper = proxy(CompetitionRegisterMapper.class, (method, args) -> {
            return switch (method.getName()) {
                case "selectAllRegistrationsByUser" -> List.of(rawRow);
                case "selectRecentRegistrationsByUser" -> List.of(rawRow);
                case "selectById" -> ownRegister;
                case "selectByTeamId" -> {
                    selectByTeamIdCalls.incrementAndGet();
                    yield List.of(ownRegister);
                }
                case "selectByCompetitionAndUserAndTeam", "selectByCompetitionAndTeam" -> ownRegister;
                case "updateTeamMembersByTeamId" -> 1;
                default -> defaultValue(method.getReturnType());
            };
        });

        SysUser leader = new SysUser();
        leader.setUserId(7L);
        leader.setUserName("leaderUser");
        leader.setStudentNo("20240001");
        leader.setStudentName("队长");
        leader.setCollegeName("人工智能学院");
        leader.setMajorName("软件工程");
        leader.setPhonenumber("13800000001");
        leader.setEmail("leader@test.com");

        SysUserMapper userMapper = proxy(SysUserMapper.class, (method, args) -> {
            return switch (method.getName()) {
                case "selectUserById" -> leader;
                case "selectUserByStudentNo" -> {
                    String studentNo = (String) args[0];
                    if ("20240001".equals(studentNo) || "20240002".equals(studentNo)) {
                        SysUser user = new SysUser();
                        user.setUserId("20240001".equals(studentNo) ? 7L : 8L);
                        user.setUserName(studentNo);
                        user.setStudentNo(studentNo);
                        user.setStudentName("20240001".equals(studentNo) ? "队长" : "队员");
                        user.setCollegeName("人工智能学院");
                        user.setMajorName("软件工程");
                        user.setPhonenumber("20240001".equals(studentNo) ? "13800000001" : "13800000002");
                        user.setEmail("20240001".equals(studentNo) ? "leader@test.com" : "member@test.com");
                        yield user;
                    }
                    yield null;
                }
                case "selectUserByUserName" -> null;
                default -> defaultValue(method.getReturnType());
            };
        });

        setField(service, "competitionRegisterMapper", registerMapper);
        setField(service, "competitionParticipationMapper", proxy(CompetitionParticipationMapper.class, (method, args) -> null));
        setField(service, "competitionReviewAssignmentMapper", proxy(CompetitionReviewAssignmentMapper.class, (method, args) -> List.of()));
        setField(service, "sysUserMapper", userMapper);
        setField(service, "teacherTeamRelMapper", proxy(TeacherTeamRelMapper.class, (method, args) -> List.of()));
        setField(service, "teacherTeamMapper", proxy(TeacherTeamMapper.class, (method, args) -> List.of()));
        setField(service, "competitionWorkMapper", proxy(CompetitionWorkMapper.class, (method, args) -> defaultValue(method.getReturnType())));
        setField(service, "competitionMapper", proxy(CompetitionMapper.class, (method, args) -> defaultValue(method.getReturnType())));
        setField(service, "minioUtils", null);

        service.getRecentRegistrations(7L, 0);

        assertEquals(
                1,
                selectByTeamIdCalls.get(),
                "recent registrations should resolve the same team only once per request"
        );
    }

    @Test
    void getRegistrationPageShouldKeepFetchingUntilVisibleRowsFillRequestedPage() throws Exception
    {
        CompetitionStudentServiceImpl service = new CompetitionStudentServiceImpl();

        AtomicInteger pagedQueryCalls = new AtomicInteger();
        AtomicInteger countQueryCalls = new AtomicInteger();

        Map<String, Object> invisibleRow = new HashMap<>();
        invisibleRow.put("registerId", 201L);
        invisibleRow.put("competitionId", 9L);
        invisibleRow.put("teamId", 88L);
        invisibleRow.put("teamName", "alpha");
        invisibleRow.put("teamMembers", """
                [
                  {"studentNo":"20249999","name":"他人","role":"leader","status":"approved"}
                ]
                """);
        invisibleRow.put("registerTime", "2026-06-24 09:00:00");
        invisibleRow.put("competitionName", "不可见报名");

        Map<String, Object> visibleRow = new HashMap<>();
        visibleRow.put("registerId", 202L);
        visibleRow.put("competitionId", 9L);
        visibleRow.put("teamId", 89L);
        visibleRow.put("teamName", "beta");
        visibleRow.put("teamMembers", """
                [
                  {"studentNo":"20240001","name":"队长","role":"leader","status":"approved"}
                ]
                """);
        visibleRow.put("registerTime", "2026-06-24 08:00:00");
        visibleRow.put("competitionName", "可见报名");

        CompetitionRegister visibleRegister = new CompetitionRegister();
        visibleRegister.setRegisterId(202L);
        visibleRegister.setCompetitionId(9L);
        visibleRegister.setTeamId(89L);
        visibleRegister.setTeamName("beta");
        visibleRegister.setUserId(7L);
        visibleRegister.setTeamMembers((String) visibleRow.get("teamMembers"));

        CompetitionRegister invisibleRegister = new CompetitionRegister();
        invisibleRegister.setRegisterId(201L);
        invisibleRegister.setCompetitionId(9L);
        invisibleRegister.setTeamId(88L);
        invisibleRegister.setTeamName("alpha");
        invisibleRegister.setUserId(8L);
        invisibleRegister.setTeamMembers((String) invisibleRow.get("teamMembers"));

        CompetitionRegisterMapper registerMapper = proxy(CompetitionRegisterMapper.class, (method, args) -> {
            return switch (method.getName()) {
                case "countRegistrationsByUser" -> {
                    countQueryCalls.incrementAndGet();
                    yield 1L;
                }
                case "selectRegistrationsByUserPage" -> {
                    pagedQueryCalls.incrementAndGet();
                    int offset = ((Number) args[2]).intValue();
                    int limit = ((Number) args[3]).intValue();
                    if (offset == 0 && limit == 1) {
                        yield List.of(visibleRow);
                    }
                    yield List.of();
                }
                case "selectById" -> {
                    Long registerId = ((Number) args[0]).longValue();
                    yield registerId.equals(201L) ? invisibleRegister : visibleRegister;
                }
                case "selectByTeamId" -> {
                    Long teamId = ((Number) args[0]).longValue();
                    yield teamId.equals(88L) ? List.of(invisibleRegister) : List.of(visibleRegister);
                }
                case "selectByCompetitionAndUserAndTeam", "selectByCompetitionAndTeam" -> visibleRegister;
                case "updateTeamMembersByTeamId" -> 1;
                default -> defaultValue(method.getReturnType());
            };
        });

        SysUser leader = new SysUser();
        leader.setUserId(7L);
        leader.setUserName("leaderUser");
        leader.setStudentNo("20240001");
        leader.setStudentName("队长");

        SysUserMapper userMapper = proxy(SysUserMapper.class, (method, args) -> {
            return switch (method.getName()) {
                case "selectUserById" -> leader;
                default -> defaultValue(method.getReturnType());
            };
        });

        setField(service, "competitionRegisterMapper", registerMapper);
        setField(service, "competitionParticipationMapper", proxy(CompetitionParticipationMapper.class, (method, args) -> null));
        setField(service, "competitionReviewAssignmentMapper", proxy(CompetitionReviewAssignmentMapper.class, (method, args) -> List.of()));
        setField(service, "sysUserMapper", userMapper);
        setField(service, "teacherTeamRelMapper", proxy(TeacherTeamRelMapper.class, (method, args) -> List.of()));
        setField(service, "teacherTeamMapper", proxy(TeacherTeamMapper.class, (method, args) -> List.of()));
        setField(service, "competitionWorkMapper", proxy(CompetitionWorkMapper.class, (method, args) -> defaultValue(method.getReturnType())));
        setField(service, "competitionMapper", proxy(CompetitionMapper.class, (method, args) -> defaultValue(method.getReturnType())));
        setField(service, "minioUtils", null);

        Method method = CompetitionStudentServiceImpl.class.getMethod(
                "getRegistrationPage",
                Long.class,
                Integer.class,
                Integer.class
        );

        Object pageResult = method.invoke(service, 7L, 1, 1);
        assertNotNull(pageResult, "paged registration result should not be null");
        Map<?, ?> payload = assertInstanceOf(Map.class, pageResult);

        assertEquals(1L, payload.get("total"), "total should reflect the paged query's visible record set");
        List<?> rows = assertInstanceOf(List.class, payload.get("rows"));
        assertEquals(1, rows.size(), "paged query should return the requested number of visible rows");
        Map<?, ?> firstRow = assertInstanceOf(Map.class, rows.get(0));
        assertEquals(202L, firstRow.get("registerId"), "visible registration should be returned after skipping hidden rows");
        assertEquals(1, pagedQueryCalls.get(), "service should issue a single paged query once the SQL has already filtered invisible rows");
        assertEquals(1, countQueryCalls.get(), "count query should execute once");
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, ThrowingHandler handler)
    {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> handler.invoke(method, args == null ? new Object[0] : args)
        );
    }

    private static Object defaultValue(Class<?> type)
    {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0f;
        }
        if (type == double.class) {
            return 0d;
        }
        if (type == char.class) {
            return '\0';
        }
        return null;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception
    {
        Field field = CompetitionStudentServiceImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @FunctionalInterface
    private interface ThrowingHandler
    {
        Object invoke(java.lang.reflect.Method method, Object[] args) throws Throwable;
    }
}
