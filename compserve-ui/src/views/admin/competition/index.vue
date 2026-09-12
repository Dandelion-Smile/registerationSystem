<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>竞赛信息维护</span>
        </div>
      </template>

      <!-- 查询条件 -->
      <el-form
        :model="queryParams"
        ref="queryRef"
        :inline="true"
        v-show="showSearch"
        label-width="68px"
      >
        <el-form-item label="竞赛名称" prop="competitionName">
          <el-input
            v-model="queryParams.competitionName"
            placeholder="请输入竞赛名称"
            clearable
            style="width: 240px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="竞赛类型" prop="competitionType">
          <el-input
            v-model="queryParams.competitionType"
            placeholder="请输入竞赛类型"
            clearable
            style="width: 240px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery"
            >搜索</el-button
          >
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="Plus" @click="handleAdd"
            >新增</el-button
          >
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="success"
            plain
            icon="Edit"
            :disabled="single"
            @click="handleUpdate"
            >修改</el-button
          >
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="danger"
            plain
            icon="Delete"
            :disabled="multiple"
            @click="handleDelete"
            >删除</el-button
          >
        </el-col>
        <right-toolbar
          v-model:showSearch="showSearch"
          @queryTable="getList"
        ></right-toolbar>
      </el-row>

      <el-table
        v-loading="loading"
        :data="competitionList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column
          label="竞赛ID"
          align="center"
          prop="competitionId"
          width="100"
        />
        <el-table-column
          label="竞赛名称"
          align="center"
          prop="competitionName"
          :show-overflow-tooltip="true"
          min-width="200"
        />
        <el-table-column
          label="竞赛类型"
          align="center"
          prop="competitionType"
          width="120"
        />
        <el-table-column
          label="报名开始时间"
          align="center"
          prop="registerStartTime"
          width="180"
        >
          <template #default="scope">
            <span>{{ parseTime(scope.row.registerStartTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="报名结束时间"
          align="center"
          prop="registerEndTime"
          width="180"
        >
          <template #default="scope">
            <span>{{ parseTime(scope.row.registerEndTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="创建时间"
          align="center"
          prop="createTime"
          width="180"
        >
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          align="center"
          width="200"
          class-name="small-padding fixed-width"
        >
          <template #default="scope">
            <el-button
              link
              type="primary"
              icon="Edit"
              @click="handleUpdate(scope.row)"
              >修改</el-button
            >
            <el-button
              link
              type="danger"
              icon="Delete"
              @click="handleDelete(scope.row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <!-- 添加/修改竞赛弹窗 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="700px"
      append-to-body
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="竞赛名称" prop="competitionName">
          <el-input
            v-model="form.competitionName"
            placeholder="请输入竞赛名称"
          />
        </el-form-item>
        <el-form-item label="竞赛类型" prop="competitionType">
          <el-input
            v-model="form.competitionType"
            placeholder="请输入竞赛类型（如：算法设计、软件开发等）"
          />
        </el-form-item>
        <el-form-item label="竞赛描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入竞赛描述"
          />
        </el-form-item>
        <el-form-item label="竞赛链接" prop="competitionLink">
          <el-input
            v-model="form.competitionLink"
            placeholder="请输入竞赛官方网站链接"
          />
        </el-form-item>
        <el-form-item label="宣传图片" prop="posterImage">
          <el-upload
            class="avatar-uploader"
            :action="`${baseUrl}/common/upload`"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            :on-error="handleImageError"
            :before-upload="beforeImageUpload"
            :headers="{ Authorization: 'Bearer ' + getToken() }"
          >
            <img
              v-if="form.posterImage || form.bannerImage"
              :src="previewImageSrc(form.posterImage || form.bannerImage)"
              class="avatar"
            />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="通知公告" prop="announcement">
          <el-input
            v-model="form.announcement"
            type="textarea"
            :rows="4"
            placeholder="请输入竞赛通知公告"
          />
        </el-form-item>
        <el-form-item label="学习链接" prop="learningLink">
          <el-input
            v-model="form.learningLink"
            placeholder="请输入学习资源链接"
          />
        </el-form-item>
        <el-form-item label="学习图片" prop="learningImage">
          <el-upload
            class="avatar-uploader"
            :action="`${baseUrl}/common/upload`"
            :show-file-list="false"
            :on-success="handleLearningImageSuccess"
            :on-error="handleImageError"
            :before-upload="beforeImageUpload"
            :headers="{ Authorization: 'Bearer ' + getToken() }"
          >
            <img
              v-if="form.learningImage"
              :src="previewImageSrc(form.learningImage)"
              class="avatar"
            />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="学习简介" prop="learningDescription">
          <el-input
            v-model="form.learningDescription"
            type="textarea"
            :rows="4"
            placeholder="请输入学习资源简介"
          />
        </el-form-item>
        <el-form-item label="报名开始时间" prop="registerStartTime">
          <el-date-picker
            v-model="form.registerStartTime"
            type="datetime"
            placeholder="选择报名开始时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="报名结束时间" prop="registerEndTime">
          <el-date-picker
            v-model="form.registerEndTime"
            type="datetime"
            placeholder="选择报名结束时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancel">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Competition">
import { ref, reactive } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import { getToken } from "@/utils/auth";
import { isExternal } from "@/utils/validate";
import { previewImageSrc } from "@/utils/media";
import {
  listCompetition,
  getCompetition,
  addCompetition,
  updateCompetition,
  delCompetition,
} from "@/api/admin/competition";
import { parseTime } from "@/utils/ruoyi";

const loading = ref(false);
const competitionList = ref([]);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const dialogVisible = ref(false);
const dialogTitle = ref("");
const baseUrl = import.meta.env.VITE_APP_BASE_API;

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  competitionName: null,
  competitionType: null,
});

const form = ref({});
const rules = {
  competitionName: [
    { required: true, message: "竞赛名称不能为空", trigger: "blur" },
  ],
  registerStartTime: [
    { required: true, message: "报名开始时间不能为空", trigger: "change" },
  ],
  registerEndTime: [
    { required: true, message: "报名结束时间不能为空", trigger: "change" },
  ],
};

const formRef = ref(null);

// 查询列表
function getList() {
  loading.value = true;
  listCompetition(queryParams).then((res) => {
    competitionList.value = res.rows;
    total.value = res.total;
    loading.value = false;
  });
}

// 搜索
function handleQuery() {
  queryParams.pageNum = 1;
  getList();
}

function cancel() {
  dialogVisible.value = false;
  reset();
}

// 重置
function resetQuery() {
  queryParams.competitionName = null;
  queryParams.competitionType = null;
  handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.competitionId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

// 新增
function handleAdd() {
  reset();
  dialogTitle.value = "添加竞赛";
  dialogVisible.value = true;
}

// 修改
function handleUpdate(row) {
  reset();
  const competitionId = row.competitionId || ids.value[0];
  getCompetition(competitionId).then((res) => {
    // 确保获取到的是完整的竞赛对象
    console.log("后端返回的数据:", res);

    // 直接使用返回的竞赛对象
    form.value = { ...res.data.competition };

    // 统一字段名，确保前后端一致
    if (form.value.bannerImage && !form.value.posterImage) {
      form.value.posterImage = form.value.bannerImage;
    }

    // 确保学习相关字段被正确绑定
    console.log("绑定到表单的数据:", form.value);

    dialogTitle.value = "修改竞赛";
    dialogVisible.value = true;
  });
}

// 提交表单
function submitForm() {
  formRef.value.validate((valid) => {
    if (valid) {
      // 确保提交的数据包含正确的字段名
      const submitData = { ...form.value };
      submitData.bannerImage = form.value.posterImage;
      console.log("提交的数据:", submitData); // 查看提交的数据

      if (submitData.competitionId) {
        updateCompetition(submitData).then(() => {
          ElMessage.success("修改成功");
          dialogVisible.value = false;
          getList();
        });
      } else {
        addCompetition(submitData).then(() => {
          ElMessage.success("新增成功");
          dialogVisible.value = false;
          getList();
        });
      }
    }
  });
}

// 表单重置
function reset() {
  form.value = {
    competitionId: null,
    competitionName: null,
    competitionType: null,
    description: null,
    competitionLink: null,
    posterImage: null,
    announcement: null,
    learningLink: null,
    learningImage: null,
    learningDescription: null,
    registerStartTime: null,
    registerEndTime: null,
  };
  formRef.value?.resetFields();
}

// 图片上传成功处理
const handleImageSuccess = (response, file, fileList) => {
  console.log("上传成功响应:", response);
  if (response && (response.code === 200 || response.success)) {
    const imageUrl = response.data?.url || response.url;
    if (imageUrl) {
      form.value.posterImage = imageUrl;
      // 同步到bannerImage字段，确保前后端一致
      form.value.bannerImage = imageUrl;
      ElMessage.success("图片上传成功");
    } else {
      console.error("响应中没有图片URL:", response);
      ElMessage.error("图片URL获取失败");
    }
  } else {
    console.error("上传失败响应:", response);
    ElMessage.error(
      "图片上传失败: " + (response?.msg || response?.message || "未知错误"),
    );
  }
};

// 图片上传失败处理
const handleImageError = (error, file, fileList) => {
  console.error("上传错误:", error);
  ElMessage.error("图片上传失败: 网络或服务器错误");
};

const handleLearningImageSuccess = (response, file, fileList) => {
  console.log("学习图片上传成功响应:", response);
  if (response && (response.code === 200 || response.success)) {
    const imageUrl = response.data?.url || response.url;
    if (imageUrl) {
      form.value.learningImage = imageUrl;
      ElMessage.success("学习图片上传成功");
    } else {
      console.error("响应中没有图片URL:", response);
      ElMessage.error("学习图片URL获取失败");
    }
  } else {
    console.error("上传失败响应:", response);
    ElMessage.error(
      "学习图片上传失败: " + (response?.msg || response?.message || "未知错误"),
    );
  }
};

// 图片上传前处理
function beforeImageUpload(file) {
  const isImage = file.type.startsWith("image/");
  if (!isImage) {
    ElMessage.error("只能上传图片文件");
    return false;
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    ElMessage.error("图片大小不能超过 2MB");
    return false;
  }
  return true;
}

getList();
</script>

<style scoped>
.app-container {
  padding: 20px;
}

/* 图片上传样式 */
.avatar-uploader {
  display: flex;
  align-items: center;
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #d9d9d9;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
}
</style>
