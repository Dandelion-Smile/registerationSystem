package org.iflytek.web.controller.exam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iflytek.common.core.controller.BaseController;
import org.iflytek.common.core.domain.AjaxResult;
import org.iflytek.system.domain.MockExamQuestion;
import org.iflytek.system.domain.MockExamRecord;
import org.iflytek.system.domain.PracticeRecord;
import org.iflytek.system.service.IMockExamQuestionService;
import org.iflytek.system.service.IMockExamRecordService;
import org.iflytek.system.service.IPracticeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生端刷题训练相关接口
 *
 * 概览数据仍为示例；推荐题目列表已对接模拟题库 mock_exam_question。
 */
@RestController
@RequestMapping("/student/practice")
public class PracticeStudentController extends BaseController {

    @Autowired
    private IMockExamQuestionService mockExamQuestionService;

    @Autowired
    private IMockExamRecordService mockExamRecordService;

    @Autowired
    private IPracticeRecordService practiceRecordService;

    /**
     * 训练概览：统计卡片 + 正确率趋势
     */
    @GetMapping("/overview")
    public AjaxResult overview() {
        Long userId = getUserId();

        Map<String, Object> data = new HashMap<>();

        // 1. 题库统计：从模拟题库实时计算
        List<MockExamQuestion> allQuestions = mockExamQuestionService.selectMockExamQuestionList(new MockExamQuestion());
        data.put("questionCount", allQuestions.size());

        // 统计分类数量与名称（取第一个非空分类名）
        java.util.Set<String> categorySet = new java.util.HashSet<>();
        String firstCategoryName = null;
        for (MockExamQuestion q : allQuestions) {
            if (q.getCategory() != null && !q.getCategory().isEmpty()) {
                categorySet.add(q.getCategory());
                if (firstCategoryName == null) {
                    firstCategoryName = q.getCategory();
                }
            }
        }
        data.put("categoryName", firstCategoryName != null ? firstCategoryName : "全部题库");
        data.put("categoryCount", categorySet.size());

        // 2. 训练记录统计：基于模拟考试记录
        List<MockExamRecord> records = mockExamRecordService.selectRecordsByUserId(userId);
        data.put("recordCount", records.size());

        int wrongCount = 0;
        java.util.Set<Long> answeredQuestionIds = new java.util.HashSet<>();
        // 简单做法：遍历每条记录的明细
        for (MockExamRecord record : records) {
            Map<String, Object> detail = mockExamRecordService.getRecordDetail(record.getRecordId(), userId);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> detailList = (List<Map<String, Object>>) detail.get("details");
            if (detailList == null) {
                continue;
            }
            for (Map<String, Object> d : detailList) {
                Object qidObj = d.get("questionId");
                Object correctObj = d.get("correct");
                if (qidObj == null) {
                    continue;
                }
                Long qid = Long.valueOf(qidObj.toString());
                answeredQuestionIds.add(qid);
                if (Integer.valueOf(String.valueOf(correctObj)) == 0) {
                    wrongCount++;
                }
            }
        }
        data.put("wrongCount", wrongCount);

        // 覆盖率：当前用户已作答过的不同题目数量 / 题库总数
        int coverageRate = 0;
        if (!allQuestions.isEmpty()) {
            coverageRate = (int) Math.round(answeredQuestionIds.size() * 100.0 / allQuestions.size());
        }
        data.put("coverageRate", coverageRate);

        // 最近训练时长：这里简单按记录数估算（例如一条记录 5 分钟）
        int minutes = records.size() * 5;
        if (minutes <= 0) {
            data.put("recentDuration", "0分钟");
        } else if (minutes < 60) {
            data.put("recentDuration", minutes + "分钟");
        } else {
            data.put("recentDuration", (minutes / 60) + "小时");
        }

        // 3. 正确率趋势目前仍使用示例数据（可以后续基于时间维度统计）
        List<Map<String, Object>> trend = new ArrayList<>();
        trend.add(buildTrendPoint("最近一次", coverageRate));
        data.put("trend", trend);

        return success(data);
    }

    /**
     * 推荐题目列表
     */
    @GetMapping("/recommend")
    public AjaxResult recommend(String keyword, String categoryKey) {
        // 从模拟题库中选取一部分题目作为“推荐题目”
        MockExamQuestion query = new MockExamQuestion();
        // 仅选择启用的题目
        query.setEnabled(1);

        // 关键字搜索：按题干模糊匹配
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.setContent(keyword.trim());
        }

        // 分类筛选：前端 categorySelect 使用 dev/db/base/ai 这几个编码
        if (categoryKey != null && !categoryKey.trim().isEmpty()) {
            String cat;
            switch (categoryKey) {
                case "dev":
                    cat = "编程开发";
                    break;
                case "db":
                    cat = "数据库";
                    break;
                case "base":
                    cat = "计算机基础";
                    break;
                case "ai":
                    cat = "人工智能";
                    break;
                default:
                    cat = null;
            }
            if (cat != null) {
                query.setCategory(cat);
            }
        }

        List<MockExamQuestion> questions = mockExamQuestionService.selectMockExamQuestionList(query);

        // 最多取前 10 道题
        List<Map<String, Object>> list = new ArrayList<>();
        int limit = Math.min(10, questions.size());
        for (int i = 0; i < limit; i++) {
            MockExamQuestion q = questions.get(i);
            String difficulty = q.getDifficulty() != null ? q.getDifficulty() : "easy";
            String difficultyLabel;
            switch (difficulty) {
                case "medium":
                    difficultyLabel = "中等";
                    break;
                case "hard":
                    difficultyLabel = "困难";
                    break;
                default:
                    difficultyLabel = "简单";
                    difficulty = "easy";
                    break;
            }
            String category = q.getCategory() != null ? q.getCategory() : "综合训练";

            // 组装选项（适用于单选/多选题）
            List<String> options = new ArrayList<>();
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

            list.add(buildQuestion(
                    q.getQuestionId(),
                    q.getContent(),
                    "请阅读题干，根据要求作答。",
                    difficulty,
                    difficultyLabel,
                    5,
                    80,
                    category,
                    q.getType(),
                    options,
                    q.getCorrectAnswer()
            ));
        }

        return success(list);
    }

    /**
     * 提交刷题训练答案并判题
     *
     * 请求示例：
     * POST /student/practice/submit
     * body: { "questionId": 1, "answer": "A. xxx" 或 "对"/"错" }
     */
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody Map<String, Object> body) {
        Long userId = getUserId();
        Object qidObj = body.get("questionId");
        Object ansObj = body.get("answer");
        if (qidObj == null) {
            return error("题目ID不能为空");
        }
        Long questionId = Long.valueOf(qidObj.toString());
        String answer = ansObj == null ? "" : ansObj.toString().trim();

        MockExamQuestion q = mockExamQuestionService.selectMockExamQuestionById(questionId);
        if (q == null || (q.getEnabled() != null && q.getEnabled() == 0)) {
            return error("题目不存在或已下线");
        }

        String type = q.getType();
        String correctAnswer = q.getCorrectAnswer() == null ? "" : q.getCorrectAnswer().trim();
        String userRaw = answer;

        // 规范化用户答案：
        // - 判断题："对"/"错" -> T/F
        // - 单选/多选："A. xxx" -> A
        String normalizedUser;
        if ("judge".equals(type)) {
            if ("对".equals(userRaw)) {
                normalizedUser = "T";
            } else if ("错".equals(userRaw)) {
                normalizedUser = "F";
            } else {
                normalizedUser = userRaw.toUpperCase();
            }
        } else {
            // 选项题：如果以 "X. " 开头，就取第一个字母
            if (userRaw.length() >= 2 && userRaw.charAt(1) == '.') {
                normalizedUser = String.valueOf(userRaw.charAt(0)).toUpperCase();
            } else {
                normalizedUser = userRaw.toUpperCase();
            }
        }

        boolean correct = false;
        if ("judge".equals(type)) {
            correct = correctAnswer.equalsIgnoreCase(normalizedUser);
        } else if ("single".equals(type)) {
            correct = correctAnswer.equalsIgnoreCase(normalizedUser);
        } else if ("multiple".equals(type)) {
            // 多选题：按逗号分隔，忽略顺序
            java.util.function.Function<String, java.util.Set<String>> splitter = s -> {
                java.util.Set<String> set = new java.util.HashSet<>();
                if (s == null || s.trim().isEmpty()) return set;
                for (String part : s.split(",")) {
                    if (part != null && !part.trim().isEmpty()) {
                        set.add(part.trim().toUpperCase());
                    }
                }
                return set;
            };
            java.util.Set<String> correctSet = splitter.apply(correctAnswer);
            java.util.Set<String> userSet = splitter.apply(normalizedUser);
            correct = !correctSet.isEmpty() && correctSet.equals(userSet);
        } else {
            // 其他类型：简单字符串比较
            correct = correctAnswer.equalsIgnoreCase(normalizedUser);
        }

        int totalScore = q.getScore() != null ? q.getScore() : 0;
        int gotScore = correct ? totalScore : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("questionId", questionId);
        result.put("type", type);
        result.put("userAnswer", normalizedUser);
        result.put("correctAnswer", correctAnswer);
        result.put("correct", correct);
        result.put("score", gotScore);
        result.put("totalScore", totalScore);

        // 写入训练记录表，方便“训练记录”Tab 查询
        PracticeRecord record = new PracticeRecord();
        record.setUserId(userId);
        record.setQuestionId(questionId);
        record.setType(type);
        record.setCategory(q.getCategory());
        record.setQuestionTitle(q.getContent());
        record.setUserAnswer(normalizedUser);
        record.setCorrectAnswer(correctAnswer);
        record.setCorrect(correct ? 1 : 0);
        record.setScore(gotScore);
        record.setTotalScore(totalScore);
        practiceRecordService.insertPracticeRecord(record);

        return success(result);
    }

    /**
     * 当前用户的刷题训练记录列表
     */
    @GetMapping("/records")
    public AjaxResult records() {
        Long userId = getUserId();
        List<PracticeRecord> list = practiceRecordService.selectPracticeRecordListByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        if (list != null) {
            for (PracticeRecord r : list) {
                Map<String, Object> m = new HashMap<>();
                m.put("time", r.getCreateTime());
                m.put("questionId", r.getQuestionId());
                m.put("title", r.getQuestionTitle());
                m.put("category", r.getCategory());
                m.put("correct", r.getCorrect() != null && r.getCorrect() == 1);
                m.put("score", r.getScore());
                m.put("totalScore", r.getTotalScore());
                m.put("userAnswer", r.getUserAnswer());
                m.put("correctAnswer", r.getCorrectAnswer());
                result.add(m);
            }
        }
        return success(result);
    }

    private Map<String, Object> buildTrendPoint(String date, int rate) {
        Map<String, Object> m = new HashMap<>();
        m.put("date", date);
        m.put("rate", rate);
        return m;
    }

    private Map<String, Object> buildQuestion(Long id, String title, String desc,
                                              String difficulty, String difficultyLabel,
                                              Integer minutes, Integer passRate,
                                              String category,
                                              String type,
                                              List<String> options,
                                              String correctAnswer) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("title", title);
        m.put("desc", desc);
        m.put("difficulty", difficulty);
        m.put("difficultyLabel", difficultyLabel);
        m.put("minutes", minutes);
        m.put("passRate", passRate);
        m.put("category", category);
        m.put("type", type);
        m.put("options", options);
        m.put("correctAnswer", correctAnswer);
        return m;
    }
}

