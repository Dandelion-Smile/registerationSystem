package org.iflytek.web.controller.exam;

import org.iflytek.common.core.controller.BaseController;
import org.iflytek.common.core.domain.AjaxResult;
import org.iflytek.system.domain.MockExamPaper;
import org.iflytek.system.domain.MockExamPaperQuestion;
import org.iflytek.system.domain.MockExamQuestion;
import org.iflytek.system.mapper.MockExamPaperMapper;
import org.iflytek.system.mapper.MockExamPaperQuestionMapper;
import org.iflytek.system.mapper.MockExamQuestionMapper;
import org.iflytek.system.service.IMockExamRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生端模拟考试相关接口
 *
 * 已对接真实题库 / 试卷表：
 * - 试卷表：mock_exam_paper
 * - 题库表：mock_exam_question
 * - 关联表：mock_exam_paper_question
 */
@RestController
@RequestMapping("/student/mock-exam")
public class MockExamStudentController extends BaseController {

    @Autowired
    private MockExamPaperMapper mockExamPaperMapper;

    @Autowired
    private MockExamPaperQuestionMapper mockExamPaperQuestionMapper;

    @Autowired
    private MockExamQuestionMapper mockExamQuestionMapper;

    @Autowired
    private IMockExamRecordService mockExamRecordService;

    /**
     * 考试大厅数据：推荐考试 + 分类 + 全部考试
     */
    @GetMapping("/hall")
    public AjaxResult hall() {
        // 所有启用的试卷
        java.util.List<MockExamPaper> papers = mockExamPaperMapper.selectEnabledPapers(null);

        // 简单策略：按创建时间取前3个作为“推荐”，其余作为“全部考试”
        java.util.List<java.util.Map<String, Object>> recommended = new java.util.ArrayList<>();
        java.util.List<java.util.Map<String, Object>> exams = new java.util.ArrayList<>();

        int index = 0;
        for (MockExamPaper p : papers) {
            java.util.Map<String, Object> exam = buildExamFromPaper(p);
            if (index < 3) {
                recommended.add(exam);
            }
            exams.add(exam);
            index++;
        }

        // 分类统计：根据试卷的 categoryCode/categoryName 聚合
        java.util.Map<String, java.util.Map<String, Object>> categoryMap = new java.util.HashMap<>();
        for (MockExamPaper p : papers) {
            String code = p.getCategoryCode();
            String name = p.getCategoryName();
            if (code == null) {
                code = "other";
            }
            if (name == null) {
                name = "其他";
            }
            java.util.Map<String, Object> cat = categoryMap.get(code);
            if (cat == null) {
                cat = new java.util.HashMap<>();
                cat.put("code", code);
                cat.put("name", name);
                cat.put("count", 0);
                categoryMap.put(code, cat);
            }
            int count = (int) cat.get("count");
            cat.put("count", count + 1);
        }
        java.util.List<java.util.Map<String, Object>> categories = new java.util.ArrayList<>(categoryMap.values());

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("recommended", recommended);
        data.put("categories", categories);
        data.put("exams", exams);
        return success(data);
    }

    /**
     * 当前登录学生的模拟考试记录
     */
    @GetMapping("/my")
    public AjaxResult my() {
        Long userId = getUserId();
        java.util.List<org.iflytek.system.domain.MockExamRecord> records =
                mockExamRecordService.selectRecordsByUserId(userId);

        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        if (records != null) {
            for (org.iflytek.system.domain.MockExamRecord r : records) {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("id", r.getRecordId());
                m.put("paperId", r.getPaperId());
                m.put("title", r.getPaperTitle());
                m.put("desc", "模拟考试");
                m.put("duration", null);
                m.put("score", r.getTotalScore());
                m.put("myScore", r.getScore());
                m.put("finishTime", r.getSubmitTime());
                list.add(m);
            }
        }

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("list", list);
        return success(data);
    }

    /**
     * 根据记录ID查看某次考试的详情（用于“查看报告”）
     */
    @GetMapping("/record/{recordId}")
    public AjaxResult recordDetail(@PathVariable Long recordId) {
        Long userId = getUserId();
        java.util.Map<String, Object> data = mockExamRecordService.getRecordDetail(recordId, userId);
        return success(data);
    }

    /**
     * 根据考试ID获取试卷与题目（示例数据）
     */
    @GetMapping("/{examId}")
    public AjaxResult detail(@PathVariable Long examId) {
        MockExamPaper paper = mockExamPaperMapper.selectMockExamPaperById(examId);
        if (paper == null || paper.getEnabled() == null || paper.getEnabled() == 0) {
            return error("试卷不存在或已下线");
        }

        java.util.Map<String, Object> exam = new java.util.HashMap<>();
        exam.put("id", paper.getPaperId());
        exam.put("title", paper.getTitle());
        exam.put("desc", paper.getDescription());
        exam.put("duration", paper.getDuration());
        exam.put("score", paper.getTotalScore());

        java.util.List<MockExamPaperQuestion> relations = mockExamPaperQuestionMapper.selectByPaperId(paper.getPaperId());
        java.util.List<java.util.Map<String, Object>> questions = new java.util.ArrayList<>();

        for (MockExamPaperQuestion rel : relations) {
            MockExamQuestion q = mockExamQuestionMapper.selectMockExamQuestionById(rel.getQuestionId());
            if (q == null || q.getEnabled() != null && q.getEnabled() == 0) {
                continue;
            }
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", q.getQuestionId());
            m.put("type", q.getType());
            m.put("content", q.getContent());
            java.util.List<String> options = new java.util.ArrayList<>();
            if (q.getOptionA() != null && !q.getOptionA().isEmpty()) {
                options.add("A. " + q.getOptionA());
            }
            if (q.getOptionB() != null && !q.getOptionB().isEmpty()) {
                options.add("B. " + q.getOptionB());
            }
            if (q.getOptionC() != null && !q.getOptionC().isEmpty()) {
                options.add("C. " + q.getOptionC());
            }
            if (q.getOptionD() != null && !q.getOptionD().isEmpty()) {
                options.add("D. " + q.getOptionD());
            }
            if (!options.isEmpty()) {
                m.put("options", options);
            }
            // 关联表中优先生效分值
            Integer score = rel.getScore() != null ? rel.getScore() : q.getScore();
            m.put("score", score);
            questions.add(m);
        }

        exam.put("questions", questions);
        exam.put("questionCount", questions.size());

        return success(exam);
    }

    /**
     * 提交试卷并自动判分
     *
     * 前端提交示例：
     * POST /student/mock-exam/{examId}/submit
     * body: {
     *   "answers": [
     *     { "questionId": 1, "answer": "A" },
     *     { "questionId": 2, "answer": "A,B" }
     *   ]
     * }
     */
    @org.springframework.web.bind.annotation.PostMapping("/{examId}/submit")
    public AjaxResult submit(@PathVariable Long examId,
                             @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, Object> body) {
        Long userId = getUserId();
        Object answersObj = body != null ? body.get("answers") : null;
        java.util.List<java.util.Map<String, Object>> answers = new java.util.ArrayList<>();
        if (answersObj instanceof java.util.List) {
            for (Object o : (java.util.List<?>) answersObj) {
                if (o instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> m = (java.util.Map<String, Object>) o;
                    answers.add(m);
                }
            }
        }
        java.util.Map<String, Object> result = mockExamRecordService.submitAndMark(userId, examId, answers);
        return success(result);
    }

    /**
     * 将试卷实体转换为前端需要的考试卡片数据结构
     */
    private java.util.Map<String, Object> buildExamFromPaper(MockExamPaper p) {
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put("id", p.getPaperId());
        m.put("title", p.getTitle());
        m.put("desc", p.getDescription());
        m.put("categoryCode", p.getCategoryCode());
        m.put("category", p.getCategoryName());
        m.put("duration", p.getDuration());
        m.put("score", p.getTotalScore());
        m.put("attendCount", p.getAttendCount() != null ? p.getAttendCount() : 0);
        m.put("level", p.getLevel());
        return m;
    }
}

