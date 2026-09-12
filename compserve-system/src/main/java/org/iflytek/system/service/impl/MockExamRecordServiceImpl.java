package org.iflytek.system.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.iflytek.common.exception.ServiceException;
import org.iflytek.system.domain.MockExamPaper;
import org.iflytek.system.domain.MockExamPaperQuestion;
import org.iflytek.system.domain.MockExamQuestion;
import org.iflytek.system.domain.MockExamRecord;
import org.iflytek.system.domain.MockExamRecordQuestion;
import org.iflytek.system.mapper.MockExamPaperMapper;
import org.iflytek.system.mapper.MockExamPaperQuestionMapper;
import org.iflytek.system.mapper.MockExamQuestionMapper;
import org.iflytek.system.mapper.MockExamRecordMapper;
import org.iflytek.system.mapper.MockExamRecordQuestionMapper;
import org.iflytek.system.service.IMockExamRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 模拟考试作答记录服务实现
 */
@Service
public class MockExamRecordServiceImpl implements IMockExamRecordService
{
    @Autowired
    private MockExamPaperMapper mockExamPaperMapper;

    @Autowired
    private MockExamPaperQuestionMapper mockExamPaperQuestionMapper;

    @Autowired
    private MockExamQuestionMapper mockExamQuestionMapper;

    @Autowired
    private MockExamRecordMapper mockExamRecordMapper;

    @Autowired
    private MockExamRecordQuestionMapper mockExamRecordQuestionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submitAndMark(Long userId, Long paperId, List<Map<String, Object>> answers)
    {
        if (userId == null)
        {
            throw new ServiceException("用户未登录");
        }
        MockExamPaper paper = mockExamPaperMapper.selectMockExamPaperById(paperId);
        if (paper == null || paper.getEnabled() == null || paper.getEnabled() == 0)
        {
            throw new ServiceException("试卷不存在或已下线");
        }

        // 试卷关联题目
        List<MockExamPaperQuestion> relations = mockExamPaperQuestionMapper.selectByPaperId(paperId);
        if (relations == null || relations.isEmpty())
        {
            throw new ServiceException("试卷尚未配置题目");
        }

        // 用户答案映射：questionId -> answer
        Map<Long, String> answerMap = new HashMap<>();
        if (answers != null)
        {
            for (Map<String, Object> a : answers)
            {
                if (a == null) continue;
                Object qidObj = a.get("questionId");
                Object ansObj = a.get("answer");
                if (qidObj == null || ansObj == null) continue;
                Long qid = Long.valueOf(qidObj.toString());
                String ans = ansObj.toString();
                answerMap.put(qid, ans);
            }
        }

        int totalScore = paper.getTotalScore() != null ? paper.getTotalScore() : 0;
        int userScore = 0;
        List<MockExamRecordQuestion> detailList = new ArrayList<>();

        for (MockExamPaperQuestion rel : relations)
        {
            MockExamQuestion q = mockExamQuestionMapper.selectMockExamQuestionById(rel.getQuestionId());
            if (q == null || (q.getEnabled() != null && q.getEnabled() == 0))
            {
                continue;
            }
            String correctAnswer = q.getCorrectAnswer();
            String userAnswer = answerMap.getOrDefault(q.getQuestionId(), "");

            boolean correct = compareAnswer(q.getType(), correctAnswer, userAnswer);
            int questionScore = rel.getScore() != null ? rel.getScore() : (q.getScore() != null ? q.getScore() : 0);
            int got = correct ? questionScore : 0;
            userScore += got;

            MockExamRecordQuestion rq = new MockExamRecordQuestion();
            rq.setQuestionId(q.getQuestionId());
            rq.setUserAnswer(userAnswer);
            rq.setCorrectAnswer(correctAnswer);
            rq.setScore(got);
            rq.setCorrect(correct ? 1 : 0);
            detailList.add(rq);
        }

        // 保存主记录
        MockExamRecord record = new MockExamRecord();
        record.setPaperId(paperId);
        record.setUserId(userId);
        record.setStartTime(new Date());
        record.setSubmitTime(new Date());
        record.setScore(userScore);
        record.setTotalScore(totalScore);
        record.setStatus("finished");
        record.setPaperTitle(paper.getTitle());
        mockExamRecordMapper.insertMockExamRecord(record);

        // 保存明细
        for (MockExamRecordQuestion rq : detailList)
        {
            rq.setRecordId(record.getRecordId());
        }
        if (!detailList.isEmpty())
        {
            mockExamRecordQuestionMapper.batchInsert(detailList);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("recordId", record.getRecordId());
        result.put("paperId", record.getPaperId());
        result.put("score", record.getScore());
        result.put("totalScore", record.getTotalScore());
        result.put("paperTitle", record.getPaperTitle());
        result.put("submitTime", record.getSubmitTime());
        result.put("details", detailList.stream().map(d -> {
            Map<String, Object> m = new HashMap<>();
            m.put("questionId", d.getQuestionId());
            m.put("userAnswer", d.getUserAnswer());
            m.put("correctAnswer", d.getCorrectAnswer());
            m.put("score", d.getScore());
            m.put("correct", d.getCorrect());
            return m;
        }).collect(Collectors.toList()));

        return result;
    }

    @Override
    public List<MockExamRecord> selectRecordsByUserId(Long userId)
    {
        return mockExamRecordMapper.selectRecordsByUserId(userId);
    }

    @Override
    public Map<String, Object> getRecordDetail(Long recordId, Long userId)
    {
        MockExamRecord record = mockExamRecordMapper.selectMockExamRecordById(recordId);
        if (record == null)
        {
            throw new ServiceException("记录不存在");
        }
        if (userId != null && record.getUserId() != null && !record.getUserId().equals(userId))
        {
            throw new ServiceException("无权查看该记录");
        }

        MockExamPaper paper = mockExamPaperMapper.selectMockExamPaperById(record.getPaperId());
        if (paper == null)
        {
            throw new ServiceException("试卷不存在或已下线");
        }

        // 试卷关联题目
        List<MockExamPaperQuestion> relations = mockExamPaperQuestionMapper.selectByPaperId(record.getPaperId());
        List<MockExamRecordQuestion> detailList = mockExamRecordQuestionMapper.selectByRecordId(recordId);

        Map<Long, MockExamRecordQuestion> detailMap = new HashMap<>();
        if (detailList != null)
        {
            for (MockExamRecordQuestion d : detailList)
            {
                detailMap.put(d.getQuestionId(), d);
            }
        }

        // 构建题目列表
        List<Map<String, Object>> questions = new ArrayList<>();
        if (relations != null)
        {
            for (MockExamPaperQuestion rel : relations)
            {
                MockExamQuestion q = mockExamQuestionMapper.selectMockExamQuestionById(rel.getQuestionId());
                if (q == null)
                {
                    continue;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("id", q.getQuestionId());
                m.put("type", q.getType());
                m.put("content", q.getContent());
                List<String> options = new ArrayList<>();
                if (q.getOptionA() != null && !q.getOptionA().isEmpty())
                {
                    options.add("A. " + q.getOptionA());
                }
                if (q.getOptionB() != null && !q.getOptionB().isEmpty())
                {
                    options.add("B. " + q.getOptionB());
                }
                if (q.getOptionC() != null && !q.getOptionC().isEmpty())
                {
                    options.add("C. " + q.getOptionC());
                }
                if (q.getOptionD() != null && !q.getOptionD().isEmpty())
                {
                    options.add("D. " + q.getOptionD());
                }
                if (!options.isEmpty())
                {
                    m.put("options", options);
                }
                Integer score = rel.getScore() != null ? rel.getScore() : q.getScore();
                m.put("score", score);
                // 当前正确答案字母（例如 A/B/C 或 A,B）
                m.put("correctAnswer", q.getCorrectAnswer());
                questions.add(m);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("recordId", record.getRecordId());
        result.put("paperId", record.getPaperId());
        result.put("score", record.getScore());
        result.put("totalScore", record.getTotalScore());
        result.put("paperTitle", record.getPaperTitle());
        result.put("submitTime", record.getSubmitTime());

        // 明细列表
        List<Map<String, Object>> detailVOs = new ArrayList<>();
        if (detailList != null)
        {
            for (MockExamRecordQuestion d : detailList)
            {
                Map<String, Object> m = new HashMap<>();
                m.put("questionId", d.getQuestionId());
                m.put("userAnswer", d.getUserAnswer());
                m.put("correctAnswer", d.getCorrectAnswer());
                m.put("score", d.getScore());
                m.put("correct", d.getCorrect());
                detailVOs.add(m);
            }
        }
        result.put("details", detailVOs);

        // 试卷/题目快照，便于前端直接使用
        Map<String, Object> exam = new HashMap<>();
        exam.put("id", paper.getPaperId());
        exam.put("title", paper.getTitle());
        exam.put("desc", paper.getDescription());
        exam.put("duration", paper.getDuration());
        exam.put("score", paper.getTotalScore());
        exam.put("questionCount", questions.size());

        result.put("exam", exam);
        result.put("questions", questions);

        return result;
    }
    /**
     * 比较答案是否正确：
     * - judge：T/F 忽略大小写
     * - single：单选 A/B/C/D
     * - multiple：多选，按逗号分隔，忽略顺序与空格
     */
    private boolean compareAnswer(String type, String correctAnswer, String userAnswer)
    {
        if (correctAnswer == null) correctAnswer = "";
        if (userAnswer == null) userAnswer = "";

        correctAnswer = correctAnswer.trim();
        userAnswer = userAnswer.trim();

        if ("judge".equals(type) || "single".equals(type))
        {
            return correctAnswer.equalsIgnoreCase(userAnswer);
        }
        if ("multiple".equals(type))
        {
            if (correctAnswer.isEmpty() && userAnswer.isEmpty())
            {
                return true;
            }
            List<String> c = splitOptions(correctAnswer);
            List<String> u = splitOptions(userAnswer);
            return c.size() == u.size() && c.containsAll(u) && u.containsAll(c);
        }
        // 未知类型，默认按字符串全等
        return correctAnswer.equals(userAnswer);
    }

    private List<String> splitOptions(String s)
    {
        if (s == null || s.trim().isEmpty())
        {
            return new ArrayList<>();
        }
        String[] arr = s.split(",");
        List<String> list = new ArrayList<>();
        for (String item : arr)
        {
            if (item != null)
            {
                String t = item.trim().toUpperCase();
                if (!t.isEmpty())
                {
                    list.add(t);
                }
            }
        }
        return list;
    }
}

