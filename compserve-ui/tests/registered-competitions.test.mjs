import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";

const currentDir = path.dirname(fileURLToPath(import.meta.url));
const projectRoot = path.resolve(currentDir, "..");

let sortRegisteredCompetitions = null;
let readRegisteredCompetitionsCache = null;
let writeRegisteredCompetitionsCache = null;
let shouldReuseRegisteredCompetitionsCache = null;

try {
  const moduleUrl = pathToFileURL(
    path.join(projectRoot, "src/utils/registeredCompetition.js")
  ).href;
  ({
    sortRegisteredCompetitions,
    readRegisteredCompetitionsCache,
    writeRegisteredCompetitionsCache,
    shouldReuseRegisteredCompetitionsCache,
  } = await import(moduleUrl));
} catch (error) {
  sortRegisteredCompetitions = null;
  readRegisteredCompetitionsCache = null;
  writeRegisteredCompetitionsCache = null;
  shouldReuseRegisteredCompetitionsCache = null;
}

assert.equal(
  typeof sortRegisteredCompetitions,
  "function",
  "sortRegisteredCompetitions should be exported from src/utils/registeredCompetition.js"
);

assert.equal(
  typeof readRegisteredCompetitionsCache,
  "function",
  "readRegisteredCompetitionsCache should be exported from src/utils/registeredCompetition.js"
);

assert.equal(
  typeof writeRegisteredCompetitionsCache,
  "function",
  "writeRegisteredCompetitionsCache should be exported from src/utils/registeredCompetition.js"
);

assert.equal(
  typeof shouldReuseRegisteredCompetitionsCache,
  "function",
  "shouldReuseRegisteredCompetitionsCache should be exported from src/utils/registeredCompetition.js"
);

const originalRecords = [
  {
    registerId: 1,
    competitionName: "较早报名的竞赛",
    registerTime: "2026-06-20 09:00:00",
  },
  {
    registerId: 2,
    competitionName: "同一时间报名的竞赛",
    registerTime: "2026-06-23 18:00:00",
  },
  {
    registerId: 3,
    competitionName: "最新报名的竞赛",
    registerTime: "2026-06-23 18:00:00",
  },
];

const sortedRecords = sortRegisteredCompetitions(originalRecords);

assert.deepEqual(
  sortedRecords.map((item) => item.registerId),
  [3, 2, 1],
  "registered competitions should be sorted by newest registerTime first"
);

assert.deepEqual(
  originalRecords.map((item) => item.registerId),
  [1, 2, 3],
  "sortRegisteredCompetitions should not mutate the original input"
);

const fakeSessionStorage = (() => {
  const store = new Map();
  return {
    getItem(key) {
      return store.has(key) ? store.get(key) : null;
    },
    setItem(key, value) {
      store.set(key, String(value));
    },
    removeItem(key) {
      store.delete(key);
    },
  };
})();

globalThis.window = { sessionStorage: fakeSessionStorage };

writeRegisteredCompetitionsCache(originalRecords, 1234567890);

const cachePayload = readRegisteredCompetitionsCache();

assert.deepEqual(
  cachePayload?.items?.map((item) => item.registerId),
  [1, 2, 3],
  "registered competitions cache should round-trip stored items"
);

assert.equal(
  cachePayload?.updatedAt,
  1234567890,
  "registered competitions cache should preserve updatedAt"
);

assert.equal(
  shouldReuseRegisteredCompetitionsCache({ updatedAt: Date.now() - 2000 }, 5000),
  true,
  "fresh registered competitions cache should be reused"
);

assert.equal(
  shouldReuseRegisteredCompetitionsCache({ updatedAt: Date.now() - 20000 }, 5000),
  false,
  "stale registered competitions cache should not be reused"
);

const navbarSource = await readFile(
  path.join(projectRoot, "src/components/StudentNavbar.vue"),
  "utf8"
);

assert.match(
  navbarSource,
  /已报名竞赛/,
  "StudentNavbar should render an 已报名竞赛 entry"
);

assert.match(
  navbarSource,
  /student-registered-competitions/,
  "StudentNavbar entry should navigate to a standalone registered competitions page"
);

assert.match(
  navbarSource,
  /getRecentRegistrations\(0\)|writeRegisteredCompetitionsCache|prefetchRegisteredCompetitions/,
  "StudentNavbar should prefetch or warm the registered competitions cache"
);

assert.match(
  navbarSource,
  /getRegistrationPage\(1,\s*10\)/,
  "StudentNavbar should warm the registered competitions cache with the paged endpoint instead of the full-history endpoint"
);

assert.doesNotMatch(
  navbarSource,
  /getRecentRegistrations\(0\)/,
  "StudentNavbar should no longer prefetch the full registered competitions history"
);

assert.match(
  navbarSource,
  /route\.path\s*!==\s*['"]\/student-registered-competitions['"]|route\.path\s*===\s*['"]\/student-registered-competitions['"]/,
  "StudentNavbar should guard its prefetch behavior based on whether the standalone registered competitions page is already active"
);

assert.doesNotMatch(
  navbarSource,
  /path:\s*['"]\/student-center\/profile['"][\s\S]*tab:\s*['"]registered['"]/,
  "StudentNavbar entry should no longer point at the profile page registered tab"
);

const routerSource = await readFile(
  path.join(projectRoot, "src/router/index.js"),
  "utf8"
);

assert.match(
  routerSource,
  /path:\s*["']\/student-registered-competitions["']/,
  "router should define a standalone registered competitions route"
);

const standalonePageSource = await readFile(
  path.join(projectRoot, "src/views/student/registered-competitions.vue"),
  "utf8"
);

assert.match(
  standalonePageSource,
  /已报名的竞赛|宸叉姤鍚嶇殑绔炶禌/,
  "standalone page should render the registered competitions title"
);

assert.doesNotMatch(
  standalonePageSource,
  /角色信息|账户信息|学籍信息|教师信息|资料完善度|最近登录时间|安全设置状态|已报名数量/,
  "standalone page should only render registered competitions content and no personal info summary"
);

const profilePageSource = await readFile(
  path.join(projectRoot, "src/views/student/profile/index.vue"),
  "utf8"
);

assert.match(
  profilePageSource,
  /key:\s*['"]registered['"]/,
  "student profile sidenav should continue to include a registered competitions section"
);

assert.match(
  profilePageSource,
  /activeSection === 'registered'/,
  "student profile page should continue to render the registered competitions panel"
);

assert.match(
  standalonePageSource,
  /shouldUseSubmissionDetail|student-work-detail|submitted|in_review|reviewed/,
  "registered competitions page should route reviewed and submitted states to the new work detail page"
);

assert.match(
  standalonePageSource,
  /pageNum|pageSize|total|getRegistrationPage|pagination/,
  "registered competitions page should keep its data loading paginated instead of requesting the full dataset at once"
);

assert.doesNotMatch(
  standalonePageSource,
  /getRecentRegistrations\(0\)/,
  "registered competitions page should no longer request all registrations in a single call"
);

assert.match(
  routerSource,
  /path:\s*["']\/student-work-detail\/:registerId["']/,
  "router should define a student work detail route"
);

const workDetailPageSource = await readFile(
  path.join(projectRoot, "src/views/student/work-detail.vue"),
  "utf8"
);

assert.match(
  workDetailPageSource,
  /作品|队伍信息|指导老师|评审老师|评审分数|评审状态/,
  "student work detail page should render work, team, teacher and review sections"
);

const competitionApiSource = await readFile(
  path.join(projectRoot, "src/api/competition.js"),
  "utf8"
);

assert.match(
  competitionApiSource,
  /student\/competition\/registered-page/,
  "competition api should expose a paged registered competitions endpoint"
);

const druidConfigSource = await readFile(
  path.join(projectRoot, "..", "compserve-admin", "src", "main", "resources", "application-druid.yml"),
  "utf8"
);

assert.match(
  druidConfigSource,
  /validationQueryTimeout:\s*3/,
  "druid config should cap validation query time to fail stale MySQL connections faster"
);

assert.match(
  druidConfigSource,
  /keepAliveBetweenTimeMillis:\s*60000/,
  "druid config should actively keep idle MySQL connections alive before wait_timeout is reached"
);

const perfSqlSource = await readFile(
  path.join(projectRoot, "..", "sql", "registered_competitions_perf.sql"),
  "utf8"
);

assert.match(
  perfSqlSource,
  /idx_sys_user_student_no_del_flag|student_no,\s*del_flag/i,
  "performance migration should add an index for sys_user student_no lookups"
);

assert.match(
  perfSqlSource,
  /idx_competition_register_user_time|competition_register.*user_id/i,
  "performance migration should add an index for competition_register user/time lookups"
);

const userAgentUtilsSource = await readFile(
  path.join(projectRoot, "..", "compserve-common", "src", "main", "java", "org", "iflytek", "common", "utils", "http", "UserAgentUtils.java"),
  "utf8"
);

assert.match(
  userAgentUtilsSource,
  /warmUp\(|warmup\(/,
  "UserAgentUtils should expose a warm-up entrypoint so heavy user-agent analyzer initialization can happen before the first student request"
);

const tokenServiceSource = await readFile(
  path.join(projectRoot, "..", "compserve-framework", "src", "main", "java", "org", "iflytek", "framework", "web", "service", "TokenService.java"),
  "utf8"
);

assert.match(
  tokenServiceSource,
  /UserAgentUtils\.warmUp\(\)|UserAgentUtils\.warmup\(\)/,
  "TokenService should warm the user-agent analyzer during startup instead of letting the first student page request pay that initialization cost"
);

const competitionStudentServiceSource = await readFile(
  path.join(projectRoot, "..", "compserve-system", "src", "main", "java", "org", "iflytek", "system", "service", "impl", "CompetitionStudentServiceImpl.java"),
  "utf8"
);

assert.match(
  competitionStudentServiceSource,
  /getBaseUserByIdCached|getBaseUserByStudentNoCached|getBaseUserByUserNameCached/,
  "student competition service should introduce lightweight cached user lookups for the registered competitions page instead of repeatedly hitting the heavy joined sys_user queries"
);

assert.match(
  competitionStudentServiceSource,
  /preloadRegistrationPageContext|selectByIds|selectByTeamIds|selectByParticipationIds/,
  "student competition service should batch-preload registration page dependencies to avoid N+1 database round trips"
);

assert.match(
  competitionStudentServiceSource,
  /registered-page timing|registered page timing|registration page timing|System\.currentTimeMillis\(\)/,
  "student competition service should emit request-stage timing evidence for the registered competitions page so remaining backend hotspots can be measured directly"
);

assert.match(
  workDetailPageSource,
  /review-focus|review-spotlight|review-stage|review-primary/,
  "student work detail page should elevate the review section as the primary visual focus"
);

assert.match(
  workDetailPageSource,
  /page-toolbar|back-button|detail-top-grid|work-panel|team-panel|mentor-panel|review-summary-bar|review-table/,
  "student work detail page should follow the requested dashboard-like layout structure"
);

assert.match(
  workDetailPageSource,
  /竞赛作品详情|下载作品资料|作品信息|作品内容|队伍信息|指导老师|评审情况/,
  "student work detail page should mirror the requested section titles and toolbar actions"
);

assert.match(
  workDetailPageSource,
  /artifact-header|artifact-meta-cards|content-card|side-info-card|review-status-strip|review-score-table/,
  "student work detail page should expose the requested screenshot-like layout blocks"
);

assert.match(
  workDetailPageSource,
  /team-member-chip|team-member-pills|leader-inline-name/,
  "student work detail page should simplify team member display to screenshot-like name chips"
);

assert.match(
  workDetailPageSource,
  /teamDetailDialogVisible|mentorDetailDialogVisible|openTeamDetailDialog|openMentorDetailDialog|el-dialog/,
  "student work detail page should open team and mentor detail dialogs from the detail buttons"
);

assert.match(
  competitionApiSource,
  /student\/competition\/work-detail\/\$\{registerId\}|student\/competition\/work-detail/,
  "competition api should expose a student work detail endpoint"
);

const studentControllerSource = await readFile(
  path.join(projectRoot, "..", "compserve-admin", "src/main/java/org/iflytek/web/controller/competition/CompetitionStudentController.java"),
  "utf8"
);

assert.match(
  studentControllerSource,
  /@GetMapping\(\"\/work-detail\/\{registerId\}\"\)/,
  "student competition controller should expose a work detail endpoint"
);

const studentServiceSource = await readFile(
  path.join(projectRoot, "..", "compserve-system", "src/main/java/org/iflytek/system/service/ICompetitionStudentService.java"),
  "utf8"
);

assert.match(
  studentServiceSource,
  /getStudentWorkDetail\(/,
  "student competition service should declare a work detail method"
);

const adminTeamDetailSource = await readFile(
  path.join(projectRoot, "src/views/admin/competition/teamDetail.vue"),
  "utf8"
);

assert.match(
  adminTeamDetailSource,
  /Detailed Archives|快捷操作|基本信息|作品信息|成员列表|评分信息|教师评分详情/,
  "admin team detail page should expose the requested archive-style sections"
);

assert.match(
  adminTeamDetailSource,
  /quick-actions|archive-hero|score-overview|teacher-score-table|member-list-card/,
  "admin team detail page should use the requested archive dashboard layout blocks"
);

assert.match(
  adminTeamDetailSource,
  /admin-detail-grid|summary-row|detail-row-two|detail-row-three|basic-info-card|work-info-card|mentor-info-card/,
  "admin team detail page should follow the requested three-row full-width card composition"
);

assert.doesNotMatch(
  adminTeamDetailSource,
  /quick-actions__title|action-button--primary|蹇嵎鎿嶄綔/,
  "admin team detail page should no longer render the quick actions sidebar"
);

console.log("registered competitions checks passed");
