<template>
  <div class="registered-page">
    <StudentNavbar />

    <main class="registered-page__main">
      <section class="registered-page__hero">
        <div>
          <p class="registered-page__eyebrow">学生端</p>
          <h1 class="registered-page__title">已报名的竞赛</h1>
          <p class="registered-page__description">
            这里仅展示你已经报名的赛事记录，不再展示个人资料信息。
          </p>
        </div>
      </section>

      <StudentRegisteredCompetitionList
        :items="registeredCompetitions"
        @detail="gotoCompetition"
        @cancel="onCancel"
      />

      <Pagination
        v-if="total > 0"
        v-model:page="pageNum"
        v-model:limit="pageSize"
        :total="total"
        :auto-scroll="false"
        class="registered-page__pagination"
        @pagination="loadRegistered"
      />
    </main>
  </div>
</template>

<script setup name="StudentRegisteredCompetitions">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import StudentNavbar from "@/components/StudentNavbar.vue";
import StudentRegisteredCompetitionList from "@/components/StudentRegisteredCompetitionList.vue";
import { getRegistrationPage, cancelRegistration } from "@/api/competition";
import { getProfileRecordStatusInfo } from "@/utils/competitionStatus";
import {
  readRegisteredCompetitionsCache,
  shouldReuseRegisteredCompetitionsCache,
  sortRegisteredCompetitions,
  writeRegisteredCompetitionsCache,
} from "@/utils/registeredCompetition";

const router = useRouter();
const registeredCompetitions = ref([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

function deriveLevel(name) {
  const text = name || "";
  if (text.includes("国") || text.includes("国家")) return { code: "national", label: "国赛" };
  if (text.includes("省") || text.includes("市")) return { code: "provincial", label: "省赛" };
  if (text.includes("校")) return { code: "school", label: "校赛" };
  return { code: "", label: "竞赛" };
}

function normalizeRegisteredCompetition(item) {
  const level = deriveLevel(item.competitionName);
  const status = getProfileRecordStatusInfo(item);
  return {
    id: item.registerId || item.id || `${item.competitionId}-${item.teamId}-${item.registerTime}`,
    competitionId: item.competitionId || null,
    name: item.competitionName || "未知竞赛",
    teamName: item.teamName || null,
    teamId: item.teamId || null,
    registerId: item.registerId || null,
    displayRegisterId: item.displayRegisterId || null,
    registerTime: item.registerTime || "",
    canCancel: !!item.canCancel,
    levelCode: level.code,
    levelLabel: level.label,
    recordStatus: item.recordStatus || "",
    reviewStatusCode: item.reviewStatusCode || item.reviewStatus || "",
    statusText: status.text,
    statusClass: status.cls,
  };
}

function shouldUseSubmissionDetail(item) {
  const displayText = String(item?.statusText || "").trim();
  const recordStatus = String(item?.recordStatus || "").trim().toLowerCase();
  const reviewStatusCode = String(item?.reviewStatusCode || "").trim().toLowerCase();
  return (
    ["待审核", "评审中", "已评审"].includes(displayText) ||
    ["submitted", "in_review", "reviewed"].includes(recordStatus) ||
    ["pending_review", "in_review", "reviewed"].includes(reviewStatusCode)
  );
}

async function loadRegistered() {
  try {
    const response = await getRegistrationPage(pageNum.value, pageSize.value);
    const payload = response?.data || {};
    const list = sortRegisteredCompetitions(Array.isArray(payload.rows) ? payload.rows : []);
    total.value = Number(payload.total) || 0;
    if (pageNum.value === 1) {
      writeRegisteredCompetitionsCache(list);
    }
    registeredCompetitions.value = list.map(normalizeRegisteredCompetition);
  } catch (error) {
    total.value = 0;
    registeredCompetitions.value = [];
  }
}

function gotoCompetition(item) {
  if (item?.registerId && shouldUseSubmissionDetail(item)) {
    router.push({
      path: `/student-work-detail/${item.registerId}`,
      query: {
        competitionId: item.competitionId || "",
        name: item.name || "",
      },
    });
    return;
  }

  const id = item.competitionId;
  const name = item.name;
  const teamId = item.teamId || item.displayTeamId;
  if (!id) return;
  router.push({
    path: `/competition/register/${id}`,
    query: {
      name,
      teamId,
      registerId: item.registerId || item.displayRegisterId || "",
    },
  });
}

async function onCancel(item) {
  if (!item.registerId) return;

  try {
    await ElMessageBox.confirm("确认取消该竞赛的报名？", "提示", { type: "warning" });
  } catch (error) {
    return;
  }

  try {
    await cancelRegistration(item.registerId);
    ElMessage.success("已取消报名");
    pageNum.value = 1;
    await loadRegistered();
  } catch (error) {
    const msg =
      (error && error.msg) ||
      (error && error.message) ||
      (error && error.response && error.response.data && error.response.data.msg) ||
      "取消失败";
    ElMessage.error(msg);
  }
}

onMounted(async () => {
  const cache = readRegisteredCompetitionsCache();
  if (shouldReuseRegisteredCompetitionsCache(cache)) {
    registeredCompetitions.value = cache.items.map(normalizeRegisteredCompetition);
    total.value = Math.max(total.value, registeredCompetitions.value.length);
  }
  await loadRegistered();
});
</script>

<style scoped>
.registered-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(129, 140, 248, 0.16), transparent 28%),
    linear-gradient(180deg, #f4f6ff 0%, #f8fafc 56%, #eef2ff 100%);
}

.registered-page__main {
  max-width: 1480px;
  margin: 0 auto;
  padding: 34px 28px 56px;
}

.registered-page__hero {
  margin-bottom: 22px;
  padding: 6px 4px 2px;
}

.registered-page__eyebrow {
  margin: 0 0 8px;
  color: #6366f1;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.registered-page__title {
  margin: 0;
  color: #111827;
  font-size: 32px;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.registered-page__description {
  margin: 10px 0 0;
  color: #6b7280;
  font-size: 15px;
}

.registered-page__pagination {
  margin-top: 20px;
  border-radius: 18px;
  overflow: hidden;
}

@media (max-width: 768px) {
  .registered-page__main {
    padding: 22px 14px 40px;
  }

  .registered-page__title {
    font-size: 26px;
  }
}
</style>
