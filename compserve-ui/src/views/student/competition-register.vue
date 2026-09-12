<template>
  <div class="register-page">
    <StudentNavbar />

    <section class="page-hero">
      <div class="hero-container">
        <div class="hero-main">
          <div class="hero-title">
            <i class="fas fa-trophy"></i>
            <h1>{{ compName || "竞赛报名" }}</h1>
            <el-tag
              v-if="statusText"
              size="small"
              type="success"
              effect="dark"
              class="hero-badge"
            >
              {{ statusText }}
            </el-tag>
          </div>
          <p v-if="compDesc" class="hero-desc">
            {{ compDesc }}
          </p>
          <div class="hero-meta">
            <el-tag
              effect="plain"
              type="info"
              v-if="compType"
              class="hero-chip"
            >
              {{ compType }}
            </el-tag>
            <el-tag
              effect="plain"
              type="info"
              class="hero-chip"
              v-if="registerRange"
            >
              报名时间：{{ registerRange }}
            </el-tag>
          </div>
        </div>
        <div class="hero-actions">
          <el-button
            v-if="registered && !isReadOnlyMemberView"
            type="success"
            :class="['premium-submit-btn', { 'upload-fail-btn': showUploadSubmitError }]"
            :loading="materialLoading"
            @click="submitMaterial"
          >
            <i class="fas fa-cloud-upload-alt"></i>
            {{ heroUploadButtonText }}
          </el-button>
          <el-button
            v-else-if="!isReadOnlyMemberView"
            type="primary"
            :class="{ 'upload-fail-btn': showUploadSubmitError }"
            :loading="registerLoading || materialLoading"
            @click="registerForm.registerType === 'join' ? submitJoinApplication() : handleUploadMaterial()"
          >{{ heroUploadButtonText }}</el-button
          >
        </div>
      </div>
    </section>

    <div class="process-steps">
      <div
        class="step-item"
        :class="{ active: !registered, completed: registered }"
      >
        <div class="step-num">1</div>
        <div class="step-label">完善报名信息</div>
      </div>
      <div class="step-line" :class="{ completed: registered }"></div>
      <div
        class="step-item"
        :class="{ active: registered && !hasRequiredMaterialFiles, completed: hasRequiredMaterialFiles }"
      >
        <div class="step-num">2</div>
        <div class="step-label">上传参赛资料</div>
      </div>
      <div class="step-line" :class="{ completed: isFinalSuccess }"></div>
      <div
        class="step-item"
        :class="{
          active: hasRequiredMaterialFiles && !isFinalSuccess,
          completed: isFinalSuccess,
        }"
      >
        <div class="step-num">3</div>
        <div class="step-label">报名成功</div>
      </div>
    </div>

    <section class="content">
      <div class="guidance">
        <el-alert type="info" :closable="false" show-icon>
          <template #title>完善队伍与指导老师信息后，再上传参赛资料</template>
        </el-alert>
      </div>
      <div class="panel">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <i class="fas fa-clipboard-list card-icon"></i>
              <span>报名信息</span>
            </div>
          </template>

          <div v-if="showSummaryCard" class="register-summary-card">
            <div class="register-summary-content">
              <div class="register-summary-head">
                <span class="register-summary-title">当前报名信息</span>
                <span class="summary-status-badge" :class="{ 'is-pending': summaryMissingFields.length }">
                  {{ summaryStatusText }}
                </span>
              </div>
              <div class="register-summary-item">
                <span class="summary-item-label">队伍名称</span>
                <span v-if="registered && !editingTeamName" class="summary-team-name">{{ userStatus.teamName || "未命名队伍" }}</span>
                <span v-else-if="registered && editingTeamName" class="summary-team-edit">
                  <el-input v-model="editTeamNameForm.name" placeholder="请输入队伍名称" size="small" class="summary-team-input" />
                  <el-button type="primary" size="small" @click="saveTeamName" class="summary-action-btn">保存</el-button>
                  <el-button size="small" @click="cancelEditTeamName" class="summary-cancel-btn">取消</el-button>
                </span>
                <span v-else class="summary-team-name">{{ summaryTeamName }}</span>
                <el-button v-if="registered && !editingTeamName && !isReadOnlyMemberView" link size="small" class="summary-edit-btn" @click="startEditTeamName">编辑</el-button>
              </div>
              <div class="register-summary-item">
                <span class="summary-item-label">指导老师</span>
                <span class="summary-value">{{ summaryTeacherNames || "未填写" }}</span>
              </div>
              <div v-if="summaryMissingFields.length" class="summary-missing-block">
                <span class="summary-missing-title">待完善项：</span>
                <span class="summary-missing-text">{{ summaryMissingFields.join("、") }}</span>
              </div>
            </div>
          </div>

          <!-- 已报名：展示我的报名信息 -->
          <div v-if="showRegisteredSection" class="my-register">
            <div class="info-section">
              <div class="section-title-wrapper">
                <i class="fas fa-chalkboard-teacher section-icon"></i>
                <span class="section-title">指导老师信息</span>
                <span v-if="showTeacherRequiredError" class="required-hint">必填写</span>
              </div>
              <div class="card-grid">
                <div
                  class="info-card teacher-card-styled clickable-card"
                  v-for="(t, ti) in teacherList"
                  :key="ti"
                  @click="viewTeacher(ti)"
                >
                  <div class="card-shine"></div>
                  <div class="card-badge teacher-badge">指导老师</div>
                  <div v-if="!isReadOnlyMemberView" class="card-actions-overlay">
                    <el-button
                      type="primary"
                      circle
                      size="small"
                      @click.stop="editTeacher(ti)"
                      title="编辑详情"
                      class="action-btn-mini"
                      ><i class="fas fa-edit"></i
                    ></el-button>
                    <el-button
                      type="danger"
                      circle
                      size="small"
                      @click.stop="deleteTeacher(ti)"
                      title="删除老师"
                      class="action-btn-mini"
                      ><i class="fas fa-trash"></i
                    ></el-button>
                  </div>
                  <div class="card-body">
                    <div class="avatar-container-outer">
                      <div class="avatar-circle-modern teacher-avatar">
                        <i class="fas fa-user-tie"></i>
                      </div>
                    </div>
                    <div class="info-lines-modern">
                      <div class="line name-line">{{ t.name }}</div>
                      <div class="line detail-line">
                        <i class="fas fa-venus-mars icon-accent"></i>
                        <span class="label">性别：</span
                        ><span class="value">{{ t.gender || "未填写" }}</span>
                      </div>
                      <div class="line detail-line">
                        <i class="fas fa-briefcase icon-accent"></i>
                        <span class="label">职称：</span
                        ><span class="value">{{ t.title || "未填写" }}</span>
                      </div>
                      <div class="line detail-line">
                        <i class="fas fa-phone-alt icon-accent"></i>
                        <span class="label">电话：</span
                        ><span class="value">{{ t.phone || "未填写" }}</span>
                      </div>
                      <div class="line tip-line">
                        点击卡片查看详情，点击右上角编辑
                      </div>
                    </div>
                  </div>
                </div>
                <div v-if="!isReadOnlyMemberView" class="info-card add-card-modern" @click="openAddTeacher">
                  <div class="card-shine"></div>
                  <div class="add-content">
                    <div class="plus-circle"><i class="fas fa-plus"></i></div>
                    <div class="add-text">添加指导老师</div>
                  </div>
                </div>
              </div>
            </div>

            <div class="student-section">
              <div class="section-title-wrapper">
                <i class="fas fa-user-graduate section-icon"></i>
                <span class="section-title">学生成员信息</span>
              </div>

              <!-- 队长区域：单独一行 -->
              <div
                class="card-grid leader-grid"
                v-if="leaderMember"
                style="margin-bottom: 24px"
              >
                <div class="info-card leader-card-modern" @click="openViewLeaderDialog">
                  <div class="card-shine"></div>
                  <div class="card-badge leader-badge">
                    <i class="fas fa-crown"></i> 队长
                  </div>
                  <div class="card-body">
                    <div class="avatar-container-outer">
                      <div class="avatar-circle-modern">
                        <img
                          :src="avatarFor(leaderMember)"
                          class="avatar-img-modern"
                        />
                      </div>
                    </div>
                    <div class="info-lines-modern">
                      <div class="line name-line">
                        {{ leaderMember.name || "未填写" }}
                      </div>
                      <div class="line detail-line">
                        <i class="fas fa-book icon-accent"></i>
                        <span class="label">专业：</span
                        ><span class="value">{{
                          leaderMember.major || "未填写"
                        }}</span>
                      </div>
                      <div class="line detail-line">
                        <i class="fas fa-id-card icon-accent"></i>
                        <span class="label">学号：</span
                        ><span class="value">{{
                          leaderMember.studentNo || "未填写"
                        }}</span>
                      </div>
                      <div class="line detail-line">
                        <i class="fas fa-phone icon-accent"></i>
                        <span class="label">电话：</span
                        ><span class="value">{{
                          leaderMember.phone || "未填写"
                        }}</span>
                      </div>
                      <div class="line tip-line">点击卡片查看所有信息</div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 队员区域：另起一行 -->
              <div class="card-grid members-grid">
                <!-- 队员卡片 -->
                <div
                  class="info-card member-card-modern clickable-card"
                  v-for="(m, mi) in otherMembers"
                  :key="mi"
                  @click="viewMemberDetails(m, mi)"
                >
                  <div class="card-shine"></div>
                  <div class="card-badge member-badge" :class="{ 'pending-status': m.status === 'pending' }">
                    {{ getMemberBadgeText(m, mi) }}
                  </div>
                  <div v-if="!isReadOnlyMemberView" class="card-actions-overlay">
                    <el-button
                      type="primary"
                      circle
                      size="small"
                      @click.stop="handleMemberEdit(m, mi, 'registered')"
                      class="action-btn-mini"
                      title="编辑队员"
                      ><i class="fas fa-edit"></i
                    ></el-button>
                    <el-button
                      type="danger"
                      circle
                      size="small"
                      @click.stop="handleMemberRemove(m, mi, 'registered')"
                      class="action-btn-mini"
                      title="移除队员"
                      ><i class="fas fa-user-minus"></i
                    ></el-button>
                  </div>
                  <div class="card-body">
                    <div class="avatar-container-outer">
                      <div class="avatar-circle-modern">
                        <img :src="avatarFor(m)" class="avatar-img-modern" />
                      </div>
                    </div>
                    <div class="info-lines-modern">
                      <div class="line name-line">{{ m.name || "未填写" }}</div>
                      <div class="line detail-line">
                        <i class="fas fa-book icon-accent"></i>
                        <span class="label">专业：</span
                        ><span class="value">{{ m.major || "未填写" }}</span>
                      </div>
                      <div class="line detail-line">
                        <i class="fas fa-id-card icon-accent"></i>
                        <span class="label">学号：</span
                        ><span class="value">{{ m.studentNo || "未填写" }}</span>
                      </div>
                      <div class="line detail-line">
                        <i class="fas fa-phone icon-accent"></i>
                        <span class="label">电话：</span
                        ><span class="value">{{ m.phone || "未填写" }}</span>
                      </div>
                      <div class="line tip-line">点击卡片查看详情，点击右上角编辑或移除</div>
                    </div>
                  </div>
                </div>
                <!-- 邀请队员卡片 -->
                <div
                  v-if="!isReadOnlyMemberView"
                  class="info-card add-card-modern"
                  @click="openInviteDialog"
                >
                  <div class="add-content">
                    <div class="plus-circle">
                      <i class="fas fa-user-plus"></i>
                    </div>
                    <div class="add-text">邀请队员</div>
                  </div>
                </div>
                
                <!-- 添加队员卡片 -->
                <div
                  v-if="!isReadOnlyMemberView"
                  class="info-card add-card-modern"
                  @click="openAddMemberDialog"
                >
                  <div class="add-content">
                    <div class="plus-circle">
                      <i class="fas fa-user-plus"></i>
                    </div>
                    <div class="add-text">添加队员</div>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="!isReadOnlyMemberView" class="cancel-registration-zone">
              <el-button
                type="danger"
                link
                @click="cancelMyRegistration"
                class="cancel-btn-text"
              >
                <i class="fas fa-sign-out-alt"></i> 取消本次竞赛报名
              </el-button>
            </div>
          </div>

          <!-- 未报名：显示报名表单 -->
          <div v-else class="register-form-container">
            <el-form :model="registerForm" label-width="110px">
              <el-form-item label="报名方式" required>
                <el-radio-group
                  v-model="registerForm.registerType"
                  @change="handleRegisterTypeChange"
                >
                  <el-radio label="create">创建新队伍</el-radio>
                  <el-radio label="join">加入已有队伍</el-radio>
                </el-radio-group>
              </el-form-item>

              <template v-if="registerForm.registerType === 'join'">
                <el-form-item label="选择队伍" required>
                  <el-select
                    v-model="registerForm.selectedTeamId"
                    placeholder="请选择要加入的队伍"
                    filterable
                    @focus="loadTeams"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="t in availableTeams"
                      :key="t.teamId"
                      :label="`${t.teamName}（${t.memberCount}人）`"
                      :value="t.teamId"
                    />
                  </el-select>
                </el-form-item>

                <el-alert type="info" :closable="false" show-icon>
                  <template #title>加入队伍将提交申请，待队长审批</template>
                </el-alert>
                <div class="join-apply-actions">
                  <el-button type="primary" @click="submitJoinApplication">
                    申请加入
                  </el-button>
                </div>
              </template>

              <template v-else>
                <el-form-item label="队伍名称" required>
                  <div class="create-team-name-box">
                    <el-input
                      v-model="registerForm.teamName"
                      placeholder="请输入队伍名称"
                      class="create-team-name-input"
                      :class="{ 'is-saved': createTeamNameSaved && !createTeamNameEditing }"
                      :readonly="createTeamNameSaved && !createTeamNameEditing"
                    />
                    <el-button
                      class="create-team-name-save"
                      :class="{ 'is-save-state': !createTeamNameSaved || createTeamNameEditing, 'is-modify-state': createTeamNameSaved && !createTeamNameEditing }"
                      @click="toggleCreateTeamNameAction"
                    >
                      {{ createTeamNameSaved && !createTeamNameEditing ? "修改" : "保存" }}
                    </el-button>
                  </div>
                </el-form-item>

                <!-- 指导老师卡片区域 -->
                <div class="info-section">
                  <div class="section-title-wrapper">
                    <i class="fas fa-chalkboard-teacher section-icon"></i>
                    <span class="section-title">指导老师信息</span>
                    <span v-if="showTeacherRequiredError" class="required-hint">必填写</span>
                  </div>
                  <div class="card-grid">
                    <div
                      class="info-card teacher-card-styled clickable-card"
                      v-for="(t, ti) in registerForm.teachers"
                      :key="ti"
                      @click="viewTeacherRegister(ti)"
                    >
                      <div class="card-shine"></div>
                      <div class="card-badge teacher-badge">指导老师</div>
                      <div class="card-actions-overlay">
                        <el-button
                          type="primary"
                          circle
                          size="small"
                          @click.stop="editTeacherRegister(ti)"
                          title="编辑详情"
                          class="action-btn-mini"
                          ><i class="fas fa-edit"></i
                        ></el-button>
                        <el-button
                          type="danger"
                          circle
                          size="small"
                          @click.stop="removeTeacher(ti)"
                          title="删除老师"
                          class="action-btn-mini"
                          ><i class="fas fa-trash"></i
                        ></el-button>
                      </div>
                      <div class="card-body">
                        <div class="avatar-container-outer">
                          <div class="avatar-circle-modern teacher-avatar">
                            <i class="fas fa-user-tie"></i>
                          </div>
                        </div>
                        <div class="info-lines-modern">
                          <div class="line name-line">{{ t.name || '未填写' }}</div>
                          <div class="line detail-line">
                            <i class="fas fa-venus-mars icon-accent"></i>
                            <span class="label">性别：</span
                            ><span class="value">{{ t.gender || "未填写" }}</span>
                          </div>
                          <div class="line detail-line">
                            <i class="fas fa-briefcase icon-accent"></i>
                            <span class="label">职称：</span
                            ><span class="value">{{ t.title || "未填写" }}</span>
                          </div>
                          <div class="line detail-line">
                            <i class="fas fa-phone-alt icon-accent"></i>
                            <span class="label">电话：</span
                            ><span class="value">{{ t.phone || "未填写" }}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div class="info-card add-card-modern" @click="openAddTeacherRegister">
                      <div class="card-shine"></div>
                      <div class="add-content">
                        <div class="plus-circle"><i class="fas fa-plus"></i></div>
                        <div class="add-text">添加指导老师</div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 学生成员信息 -->
                <div class="student-section">
                  <div class="section-title-wrapper">
                    <i class="fas fa-user-graduate section-icon"></i>
                    <span class="section-title">学生成员信息</span>
                  </div>

                  <!-- 队长信息标题 -->
                  <div class="section-title-wrapper" style="margin-bottom: 16px">
                    <span class="section-title">队长信息</span>
                  </div>

                  <!-- 队长信息卡片 -->
                  <div class="card-grid leader-grid" style="margin-bottom: 24px">
                    <div v-if="registerForm.leader.name" class="info-card leader-card-modern" @click="openViewLeaderDialog">
                      <div class="card-shine"></div>
                      <div class="card-badge leader-badge">
                        <i class="fas fa-crown"></i> 队长
                      </div>
                      <div class="card-body">
                        <div class="avatar-container-outer">
                          <div class="avatar-circle-modern">
                            <img
                              :src="avatarFor(registerForm.leader)"
                              class="avatar-img-modern"
                            />
                          </div>
                        </div>
                        <div class="info-lines-modern">
                          <div class="line name-line">
                            {{ registerForm.leader.name || "未填写" }}
                          </div>
                          <div class="line detail-line">
                            <i class="fas fa-book icon-accent"></i>
                            <span class="label">专业：</span
                            ><span class="value">{{
                              registerForm.leader.major || "未填写"
                            }}</span>
                          </div>
                          <div class="line detail-line">
                            <i class="fas fa-id-card icon-accent"></i>
                            <span class="label">学号：</span
                            ><span class="value">{{
                              registerForm.leader.studentNo || "未填写"
                            }}</span>
                          </div>
                          <div class="line detail-line">
                            <i class="fas fa-phone icon-accent"></i>
                            <span class="label">电话：</span
                            ><span class="value">{{
                              registerForm.leader.phone || "未填写"
                            }}</span>
                          </div>
                          <div class="line tip-line">点击卡片查看所有信息</div>
                        </div>
                      </div>
                    </div>
                    <div v-else
                      class="info-card add-card-modern"
                      @click="goToProfileCenter"
                    >
                      <div class="add-content">
                        <div class="plus-circle">
                          <i class="fas fa-user"></i>
                        </div>
                        <div class="add-text">去个人中心完善队长信息</div>
                      </div>
                    </div>
                  </div>

                  <!-- 队员信息标题 -->
                  <div class="section-title-wrapper" style="margin-bottom: 16px">
                    <span class="section-title">队员信息</span>
                  </div>

                  <!-- 队员区域：另起一行 -->
                  <div class="card-grid members-grid">
                    <!-- 队员卡片 -->
                    <div
                      class="info-card member-card-modern clickable-card"
                      v-for="(m, mi) in registerForm.members"
                      :key="mi"
                      @click="viewMemberDetails(m, mi)"
                    >
                      <div class="card-shine"></div>
                      <div class="card-badge member-badge">队员 {{ mi + 1 }}</div>
                      <div class="card-actions-overlay">
                        <el-button
                          type="primary"
                          circle
                          size="small"
                          @click.stop="handleMemberEdit(m, mi, 'draft')"
                          class="action-btn-mini"
                          title="编辑队员"
                          ><i class="fas fa-edit"></i>
                        </el-button>
                        <el-button
                          type="danger"
                          circle
                          size="small"
                          @click.stop="handleMemberRemove(m, mi, 'draft')"
                          class="action-btn-mini"
                          title="移除队员"
                          ><i class="fas fa-user-minus"></i>
                        </el-button>
                      </div>
                      <div class="card-body">
                        <div class="avatar-container-outer">
                          <div class="avatar-circle-modern">
                            <i class="fas fa-user"></i>
                          </div>
                        </div>
                        <div class="info-lines-modern">
                          <div class="line name-line">{{ m.name || "未填写" }}</div>
                          <div class="line detail-line">
                            <i class="fas fa-book icon-accent"></i>
                            <span class="label">专业：</span
                            ><span class="value">{{ m.major || "未填写" }}</span>
                          </div>
                          <div class="line detail-line">
                            <i class="fas fa-id-card icon-accent"></i>
                            <span class="label">学号：</span
                            ><span class="value">{{ m.studentNo || "未填写" }}</span>
                          </div>
                          <div class="line detail-line">
                            <i class="fas fa-phone icon-accent"></i>
                            <span class="label">电话：</span
                            ><span class="value">{{ m.phone || "未填写" }}</span>
                          </div>
                          <div class="line tip-line">点击卡片查看详情，点击右上角编辑或移除</div>
                        </div>
                      </div>
                    </div>

                    <!-- 邀请队员卡片 -->
                    <div
                      class="info-card add-card-modern"
                      @click="openInviteDialogRegister"
                    >
                      <div class="add-content">
                        <div class="plus-circle">
                          <i class="fas fa-user-plus"></i>
                        </div>
                        <div class="add-text">邀请队员</div>
                      </div>
                    </div>

                    <!-- 添加队员卡片 -->
                    <div
                      class="info-card add-card-modern"
                      @click="openAddMemberDialogRegister"
                    >
                      <div class="add-content">
                        <div class="plus-circle">
                          <i class="fas fa-user-plus"></i>
                        </div>
                        <div class="add-text">添加队员</div>
                      </div>
                    </div>
                  </div>
                </div>
              </template>


            </el-form>
          </div>
        </el-card>
      </div>

      <div class="panel material-upload-section">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <i class="fas fa-upload card-icon"></i>
              <span>资料上传</span>
              <span v-if="showMaterialRequiredError" class="required-hint">必填写</span>
              <el-tag
                v-if="!registered && !isReadOnlyMemberView"
                type="primary"
                effect="plain"
                style="margin-left: 8px"
                >请先完成报名</el-tag
              >
            </div>
          </template>

          <div class="upload-form">
            <el-form
              label-width="90px"
              label-position="left"
              class="upload-el-form"
            >
              <el-alert
                v-if="lastSubmitTime && !hasUnsavedChanges"
                type="success"
                :closable="false"
                show-icon
                style="margin-bottom: 10px"
              >
                <template #title>
                  {{ displayParticipationStatus }}<span v-if="lastSubmitTime"
                    >：{{ fmt(lastSubmitTime) }}</span
                  >
                </template>
              </el-alert>

              <el-alert
                v-if="!isReadOnlyMemberView && lastSubmitTime && hasUnsavedChanges"
                type="warning"
                :closable="false"
                show-icon
                style="margin-bottom: 10px"
              >
                <template #title>
                  你已修改作品信息或上传材料，但还没有重新提交，点击上方“上传资料”后会更新为最新版本。
                </template>
              </el-alert>
              <!-- 隐藏队伍ID -->
              <!-- <el-form-item label="队伍ID">
                <el-input :model-value="String(userStatus.teamId || '')" placeholder="自动填写" disabled />
              </el-form-item> -->

              <div class="work-info-grid">
                <el-form-item label="作品名称" required>
                  <el-input
                    v-model="materialForm.workName"
                    :readonly="isReadOnlyMemberView"
                    :disabled="isReadOnlyMemberView"
                    maxlength="100"
                    show-word-limit
                    placeholder="请输入参赛作品名称"
                  />
                </el-form-item>
                <el-form-item label="作品简介" required class="work-description-item">
                  <el-input
                    v-model="materialForm.workDescription"
                    :readonly="isReadOnlyMemberView"
                    :disabled="isReadOnlyMemberView"
                    type="textarea"
                    :rows="4"
                    maxlength="500"
                    show-word-limit
                    placeholder="请输入作品简介"
                  />
                </el-form-item>
                <div v-if="showWorkRequiredError" class="required-warning-text">
                  请填写作品名称和作品简介
                </div>
              </div>

              <div class="triple-upload-grid">
                <div class="upload-item-card">
                  <div class="upload-icon-wrapper doc-icon">
                    <i class="fas fa-file-alt"></i>
                  </div>
                  <div class="upload-label">申报书资料 <span class="required-star">*</span></div>
                  <el-form-item label-width="0">
                    <!-- 未上传：显示上传虚线框 -->
                    <FileUpload
                      v-if="!materialForm.docPath && !isReadOnlyMemberView"
                      v-model="materialForm.docPath"
                      :fileType="['doc', 'docx', 'pdf']"
                      :fileSize="50"
                      :limit="1"
                      :dragUpload="true"
                      @update:files="(files) => (materialForm.docFiles = files)"
                    />
                    <!-- 已上传：极简列表风格进度卡片 -->
                    <div v-else-if="materialForm.docPath" class="file-minimal-item">
                      <div class="item-main-content">
                        <div
                          :class="[
                            'file-icon-solid',
                            getFileTypeClass(
                              materialForm.docPath,
                              materialForm.docFiles,
                            ),
                          ]"
                        >
                          <i
                            :class="
                              getFileIcon(
                                materialForm.docPath,
                                materialForm.docFiles,
                              )
                            "
                          ></i>
                        </div>
                        <div class="file-details-box">
                          <div class="file-name-row">
                            <span
                              class="fname-text"
                              :title="
                                getSafeName(
                                  materialForm.docFiles,
                                  materialForm.docPath,
                                )
                              "
                              >{{
                                getSafeName(
                                  materialForm.docFiles,
                                  materialForm.docPath,
                                )
                              }}</span
                            >
                            <i
                              v-if="!isReadOnlyMemberView"
                              class="fas fa-times remove-trigger"
                              @click="confirmRemoveFile('docPath')"
                              title="移除文件"
                            ></i>
                          </div>
                          <div class="file-meta-row">
                            <span class="meta-tag success-state"
                              ><i class="fas fa-check-circle"></i> 已就绪</span
                            >
                            <span class="meta-dot">·</span>
                            <span class="meta-size">{{
                              fileSizes.docPath || "获取中..."
                            }}</span>
                          </div>
                          <div class="mini-progress-track">
                            <div
                              :class="[
                                'mini-progress-fill',
                                getFileTypeClass(
                                  materialForm.docPath,
                                  materialForm.docFiles,
                                ),
                                'pulse-active',
                              ]"
                            ></div>
                          </div>
                        </div>
                      </div>
                      <div class="item-actions-footer">
                        <el-button
                          link
                          class="mini-act-btn download"
                          @click="
                            downloadFile(
                              getSafePath(materialForm.docPath),
                              materialForm.docFiles,
                            )
                          "
                        >
                          <i class="fas fa-download"></i> 下载
                        </el-button>
                        <el-button
                          v-if="!isReadOnlyMemberView"
                          link
                          class="mini-act-btn update"
                          @click="materialForm.docPath = ''"
                          ><i class="fas fa-sync-alt"></i> 更新</el-button
                        >
                        <el-button
                          v-if="!isReadOnlyMemberView"
                          link
                          class="mini-act-btn delete-btn"
                          @click="confirmRemoveFile('docPath')"
                          ><i class="fas fa-trash-alt"></i> 删除</el-button
                        >
                      </div>
                    </div>
                    <div v-else class="file-readonly-empty">队长暂未上传申报书资料</div>
                    <span class="help-text"
                      >支持 doc, docx, pdf，单个不超过50MB</span
                    >
                  </el-form-item>
                </div>

                <div class="upload-item-card">
                  <div class="upload-icon-wrapper ppt-icon">
                    <i class="fas fa-file-powerpoint"></i>
                  </div>
                  <div class="upload-label">PPT演示资料 <span class="required-star">*</span></div>
                  <el-form-item label-width="0">
                    <!-- 未上传 -->
                    <FileUpload
                      v-if="!materialForm.pptPath && !isReadOnlyMemberView"
                      v-model="materialForm.pptPath"
                      :fileType="['ppt', 'pptx', 'pdf']"
                      :fileSize="100"
                      :limit="1"
                      :dragUpload="true"
                      @update:files="(files) => (materialForm.pptFiles = files)"
                    />
                    <!-- 已上传：极简列表风格PPT进度卡片 -->
                    <div v-else-if="materialForm.pptPath" class="file-minimal-item">
                      <div class="item-main-content">
                        <div
                          :class="[
                            'file-icon-solid',
                            getFileTypeClass(
                              materialForm.pptPath,
                              materialForm.pptFiles,
                            ),
                          ]"
                        >
                          <i
                            :class="
                              getFileIcon(
                                materialForm.pptPath,
                                materialForm.pptFiles,
                              )
                            "
                          ></i>
                        </div>
                        <div class="file-details-box">
                          <div class="file-name-row">
                            <span
                              class="fname-text"
                              :title="
                                getSafeName(
                                  materialForm.pptFiles,
                                  materialForm.pptPath,
                                )
                              "
                              >{{
                                getSafeName(
                                  materialForm.pptFiles,
                                  materialForm.pptPath,
                                )
                              }}</span
                            >
                            <i
                              v-if="!isReadOnlyMemberView"
                              class="fas fa-times remove-trigger"
                              @click="confirmRemoveFile('pptPath')"
                              title="移除文件"
                            ></i>
                          </div>
                          <div class="file-meta-row">
                            <span class="meta-tag success-state"
                              ><i class="fas fa-check-circle"></i> 已就绪</span
                            >
                            <span class="meta-dot">·</span>
                            <span class="meta-size">{{
                              fileSizes.pptPath || "获取中..."
                            }}</span>
                          </div>
                          <div class="mini-progress-track">
                            <div
                              :class="[
                                'mini-progress-fill',
                                getFileTypeClass(
                                  materialForm.pptPath,
                                  materialForm.pptFiles,
                                ),
                                'pulse-active',
                              ]"
                            ></div>
                          </div>
                        </div>
                      </div>
                      <div class="item-actions-footer">
                        <el-button
                          link
                          class="mini-act-btn download"
                          @click="
                            downloadFile(
                              getSafePath(materialForm.pptPath),
                              materialForm.pptFiles,
                            )
                          "
                        >
                          <i class="fas fa-download"></i> 下载
                        </el-button>
                        <el-button
                          v-if="!isReadOnlyMemberView"
                          link
                          class="mini-act-btn update"
                          @click="materialForm.pptPath = ''"
                          ><i class="fas fa-sync-alt"></i> 更新</el-button
                        >
                        <el-button
                          v-if="!isReadOnlyMemberView"
                          link
                          class="mini-act-btn delete-btn"
                          @click="confirmRemoveFile('pptPath')"
                          ><i class="fas fa-trash-alt"></i> 删除</el-button
                        >
                      </div>
                    </div>
                    <div v-else class="file-readonly-empty">队长暂未上传PPT演示资料</div>
                    <span class="help-text"
                      >支持 ppt, pptx, pdf，单个不超过100MB</span
                    >
                  </el-form-item>
                </div>

                <div class="upload-item-card">
                  <div class="upload-icon-wrapper zip-icon">
                    <i class="fas fa-file-archive"></i>
                  </div>
                  <div class="upload-label">其他补充资料</div>
                  <el-form-item label-width="0">
                    <!-- 未上传 -->
                    <FileUpload
                      v-if="!materialForm.otherPath && !isReadOnlyMemberView"
                      v-model="materialForm.otherPath"
                      :fileType="['zip', 'rar', '7z']"
                      :fileSize="200"
                      :limit="1"
                      :dragUpload="true"
                      @update:files="
                        (files) => (materialForm.otherFiles = files)
                      "
                    />
                    <!-- 已上传：极简列表风格其他资料进度卡片 -->
                    <div v-else-if="materialForm.otherPath" class="file-minimal-item">
                      <div class="item-main-content">
                        <div
                          :class="[
                            'file-icon-solid',
                            getFileTypeClass(
                              materialForm.otherPath,
                              materialForm.otherFiles,
                            ),
                          ]"
                        >
                          <i
                            :class="
                              getFileIcon(
                                materialForm.otherPath,
                                materialForm.otherFiles,
                              )
                            "
                          ></i>
                        </div>
                        <div class="file-details-box">
                          <div class="file-name-row">
                            <span
                              class="fname-text"
                              :title="
                                getSafeName(
                                  materialForm.otherFiles,
                                  materialForm.otherPath,
                                )
                              "
                              >{{
                                getSafeName(
                                  materialForm.otherFiles,
                                  materialForm.otherPath,
                                )
                              }}</span
                            >
                            <i
                              v-if="!isReadOnlyMemberView"
                              class="fas fa-times remove-trigger"
                              @click="confirmRemoveFile('otherPath')"
                              title="移除文件"
                            ></i>
                          </div>
                          <div class="file-meta-row">
                            <span class="meta-tag success-state"
                              ><i class="fas fa-check-circle"></i> 已就绪</span
                            >
                            <span class="meta-dot">·</span>
                            <span class="meta-size">{{
                              fileSizes.otherPath || "获取中..."
                            }}</span>
                          </div>
                          <div class="mini-progress-track">
                            <div
                              :class="[
                                'mini-progress-fill',
                                getFileTypeClass(
                                  materialForm.otherPath,
                                  materialForm.otherFiles,
                                ),
                                'pulse-active',
                              ]"
                            ></div>
                          </div>
                        </div>
                      </div>
                      <div class="item-actions-footer">
                        <el-button
                          link
                          class="mini-act-btn download"
                          @click="
                            downloadFile(
                              getSafePath(materialForm.otherPath),
                              materialForm.otherFiles,
                            )
                          "
                        >
                          <i class="fas fa-download"></i> 下载
                        </el-button>
                        <el-button
                          v-if="!isReadOnlyMemberView"
                          link
                          class="mini-act-btn update"
                          @click="materialForm.otherPath = ''"
                          ><i class="fas fa-sync-alt"></i> 更新</el-button
                        >
                        <el-button
                          v-if="!isReadOnlyMemberView"
                          link
                          class="mini-act-btn delete-btn"
                          @click="confirmRemoveFile('otherPath')"
                          ><i class="fas fa-trash-alt"></i> 删除</el-button
                        >
                      </div>
                    </div>
                    <div v-else class="file-readonly-empty">当前没有其他补充资料</div>
                    <span class="help-text"
                      >支持 zip, rar, 7z 压缩包，单个不超过200MB</span
                    >
                  </el-form-item>
                </div>
              </div>

              <!-- 移除了原来的提交按钮，已迁移至顶部 Hero 区域 -->
            </el-form>
          </div>
        </el-card>
      </div>
    </section>
    <el-dialog
      v-model="showTeacherDialog"
      :title="isEditMode ? '编辑指导老师' : '查看指导老师'"
      width="580px"
      class="premium-dialog"
    >
      <div class="dialog-body-scroller">
        <div class="dialog-header-accent">
          <div class="accent-icon">
            <i class="fas fa-chalkboard-teacher"></i>
          </div>
          <div class="accent-text">
            <h3>{{ isEditMode ? "编辑信息" : "详细资料" }}</h3>
            <p>
              {{
                isEditMode
                  ? "请准确填写导师的相关信息"
                  : "以下是该导师的完整登记资料"
              }}
            </p>
          </div>
        </div>

        <div v-if="isEditMode" class="form-section-card">
          <div class="form-grid-2">
            <el-form-item label="老师手机号">
              <el-input
                v-model="teacherSearchForm.phone"
                placeholder="请输入老师手机号"
                class="custom-input"
                clearable
              />
            </el-form-item>
            <el-form-item label="老师姓名">
              <el-input
                v-model="teacherSearchForm.name"
                placeholder="请输入老师姓名"
                class="custom-input"
                clearable
              />
            </el-form-item>
          </div>
          <div class="teacher-search-actions">
            <el-button type="primary" @click="searchTeacherProfiles">查询老师</el-button>
          </div>
          <div
            v-if="teacherSearchMessage"
            class="teacher-search-feedback"
            :class="`is-${teacherSearchState}`"
          >
            <i
              class="fas"
              :class="teacherSearchState === 'single' ? 'fa-check-circle' : teacherSearchState === 'multiple' ? 'fa-layer-group' : 'fa-info-circle'"
            ></i>
            <span>{{ teacherSearchMessage }}</span>
          </div>
          <div
            v-if="(teacherSearchState === 'single' || teacherSearchState === 'multiple') && teacherSearchResults.length"
            class="teacher-search-result-list"
          >
            <div
              v-for="item in teacherSearchResults"
              :key="`${item.teacherId}-${item.phone}`"
              class="teacher-search-result-item"
              :class="teacherSearchState === 'single' ? 'is-single' : 'is-disabled'"
            >
              <div class="teacher-result-main">
                <span class="teacher-result-name">{{ item.teacherName }}</span>
                <span class="teacher-result-phone">{{ item.phone }}</span>
              </div>
              <div class="teacher-result-badge">重复命中</div>
            </div>
          </div>
        </div>

        <el-form
          :model="tempTeacher"
          label-width="90px"
          label-position="top"
          class="premium-form"
        >
          <div class="form-section-card">
            <div class="form-grid-2">
              <el-form-item label="姓名">
                <el-input
                  v-model="tempTeacher.name"
                  placeholder="请输入老师姓名"
                  disabled
                  class="custom-input"
                />
              </el-form-item>
              <el-form-item label="性别">
                <el-radio-group
                  v-model="tempTeacher.gender"
                  disabled
                  class="premium-radio-group"
                >
                  <el-radio-button label="男">男</el-radio-button>
                  <el-radio-button label="女">女</el-radio-button>
                </el-radio-group>
              </el-form-item>
            </div>
            <div class="form-grid-2">
              <el-form-item label="联系电话">
                <el-input
                    v-model="tempTeacher.phone"
                    placeholder="请输入联系电话，自动填充老师信息"
                    :disabled="!isEditMode"
                    class="custom-input"
                    @input="handlePhoneInput"
                >
                  <template #prefix>
                    <i class="fas fa-phone-alt icon-fade"></i>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="电子邮箱">
                <el-input
                  v-model="tempTeacher.email"
                  placeholder="请输入电子邮箱"
                  :disabled="!isEditMode"
                  class="custom-input"
                >
                  <template #prefix
                    ><i class="fas fa-envelope icon-fade"></i
                  ></template>
                </el-input>
              </el-form-item>
            </div>
          </div>

          <div class="form-section-card">
            <div class="form-grid-2">
              <el-form-item label="单位">
                <el-input
                  v-model="tempTeacher.unit"
                  placeholder="请输入工作单位"
                  :disabled="!isEditMode"
                  class="custom-input"
                />
              </el-form-item>
              <el-form-item label="职称">
                <el-input
                  v-model="tempTeacher.title"
                  placeholder="请输入职称"
                  :disabled="!isEditMode"
                  class="custom-input"
                />
              </el-form-item>
            </div>
            <div class="form-grid-2">
              <el-form-item label="职务">
                <el-input
                  v-model="tempTeacher.position"
                  placeholder="请输入职务"
                  :disabled="!isEditMode"
                  class="custom-input"
                />
              </el-form-item>
              <el-form-item label="政治面貌">
                <el-select
                  v-model="tempTeacher.politicalStatus"
                  placeholder="请选择"
                  :disabled="!isEditMode"
                  style="width: 100%"
                  class="custom-input"
                >
                  <el-option label="中共党员" value="中共党员" />
                  <el-option label="中共预备党员" value="中共预备党员" />
                  <el-option label="共青团员" value="共青团员" />
                  <el-option label="群众" value="群众" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
            </div>
          </div>

          <div class="form-section-card">
            <div class="form-grid-2">
              <el-form-item label="出生年月">
                <el-date-picker
                  v-model="tempTeacher.birthDate"
                  type="month"
                  placeholder="选择年月"
                  style="width: 100%"
                  :disabled="!isEditMode"
                  class="custom-input"
                />
              </el-form-item>

            </div>
            <el-form-item label="电子邮箱">
              <el-input
                v-model="tempTeacher.email"
                placeholder="请输入电子邮箱"
                :disabled="!isEditMode"
                class="custom-input"
              >
                <template #prefix
                  ><i class="fas fa-envelope icon-fade"></i
                ></template>
              </el-input>
            </el-form-item>
          </div>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer-premium">
          <el-button @click="showTeacherDialog = false" class="btn-cancel">{{
            isEditMode ? "取消" : "关闭"
          }}</el-button>
          <el-button
            v-if="isEditMode"
            type="primary"
            :disabled="teacherSearchState !== 'single' || !tempTeacher.teacherId"
            @click="confirmTeacherEdit"
            class="btn-confirm"
            >保存修改</el-button
          >
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showMemberDialog"
      title="查看学生成员"
      width="640px"
      class="premium-dialog"
    >
      <div class="dialog-body-scroller">
        <div class="dialog-header-accent">
          <div class="accent-icon">
            <i class="fas fa-user-graduate"></i>
          </div>
          <div class="accent-text">
            <h3>详细资料</h3>
            <p>以下是该学生成员当前可查看的完整信息</p>
          </div>
        </div>

        <el-form label-position="top" class="premium-form">
          <div class="form-section-card">
            <div class="form-grid-2">
              <el-form-item
                v-for="item in memberDetailItems"
                :key="item.key"
                :label="item.label"
              >
                <el-input
                  :model-value="item.value"
                  disabled
                  class="custom-input"
                />
              </el-form-item>
            </div>
            <div v-if="!memberDetailItems.length" class="member-detail-empty">
              暂无可展示的成员信息
            </div>
          </div>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer-premium">
          <el-button @click="showMemberDialog = false" class="btn-cancel"
            >关闭</el-button
          >
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showAddMember"
      title="添加队员"
      width="500px"
      class="premium-dialog"
    >
      <div class="invite-dialog-content">
        <div class="invite-icon"><i class="fas fa-user-plus"></i></div>
        <p class="invite-tip">请填写队员信息</p>
        <el-form ref="newMemberFormRef" :model="newMemberForm" :rules="memberRules" label-width="100px">
          <el-form-item label="姓名" prop="name">
            <el-input
              v-model="newMemberForm.name"
              placeholder="请输入姓名"
              class="invite-input"
            >
              <template #prefix><i class="fas fa-user"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item label="学号" prop="studentNo">
            <el-input
              v-model="newMemberForm.studentNo"
              placeholder="请输入学号（输入后自动填充其他信息）"
              class="invite-input"
              @blur="fetchUserInfoByStudentNo(newMemberForm)"
            >
              <template #prefix><i class="fas fa-id-card"></i></template>
            </el-input>
            <div class="student-no-hint">输入学号后，系统将自动为您填充其他信息</div>
          </el-form-item>
          <el-form-item label="学院" prop="college">
            <el-select
              v-model="newMemberForm.college"
              placeholder="请选择学院"
              class="invite-input"
              filterable
              @change="onMemberCollegeChange(newMemberForm)"
            >
              <el-option v-for="college in collegeOptions" :key="college" :label="college" :value="college" />
            </el-select>
          </el-form-item>
          <el-form-item label="专业" prop="major">
            <el-select
              v-model="newMemberForm.major"
              placeholder="请先选择学院"
              class="invite-input"
              filterable
              :disabled="!newMemberForm.college"
            >
              <el-option v-for="major in newMemberMajorOptions" :key="major" :label="major" :value="major" />
            </el-select>
          </el-form-item>
          <el-form-item label="电话" prop="phone">
            <el-input
              v-model="newMemberForm.phone"
              placeholder="请输入电话"
              class="invite-input"
            >
              <template #prefix><i class="fas fa-phone"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model="newMemberForm.email"
              placeholder="请输入邮箱"
              class="invite-input"
            >
              <template #prefix><i class="fas fa-envelope"></i></template>
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer-premium">
          <el-button @click="showAddMember = false" class="btn-cancel"
            >取消</el-button
          >
          <el-button
            type="primary"
            @click="addMemberDirectly"
            class="btn-confirm"
            >添加队员</el-button
          >
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showInvite"
      title="邀请队员"
      width="400px"
      class="premium-dialog"
    >
      <div class="invite-dialog-content">
        <div class="invite-icon"><i class="fas fa-user-plus"></i></div>
        <p class="invite-tip">请输入对方的学号以发送入队邀请</p>
        <el-input
          v-model="inviteStudentNo"
          placeholder="请输入队员学号"
          class="invite-input"
        >
          <template #prefix><i class="fas fa-id-card"></i></template>
        </el-input>
      </div>
      <template #footer>
        <div class="dialog-footer-premium">
          <el-button @click="showInvite = false" class="btn-cancel"
            >取消</el-button
          >
          <el-button
            type="primary"
            @click="
              () => {
                showInvite = false;
                sendInvite();
              }
            "
            class="btn-confirm"
            >发送邀请</el-button
          >
        </div>
      </template>
    </el-dialog>

    <!-- 报名阶段：添加指导老师对话框 -->
    <el-dialog
      v-model="addTeacherDialogVisibleRegister"
      title="添加指导老师"
      width="580px"
      class="premium-dialog"
      destroy-on-close
    >
      <div class="dialog-body-scroller">
        <div class="dialog-header-accent">
          <div class="accent-icon">
            <i class="fas fa-chalkboard-teacher"></i>
          </div>
          <div class="accent-text">
            <h3>添加信息</h3>
            <p>请准确填写导师的相关信息</p>
          </div>
        </div>

        <div class="form-section-card">
          <div class="form-grid-2">
            <el-form-item label="老师手机号">
              <el-input
                v-model="teacherSearchFormRegister.phone"
                placeholder="请输入老师手机号"
                class="custom-input"
                clearable
              />
            </el-form-item>
            <el-form-item label="老师姓名">
              <el-input
                v-model="teacherSearchFormRegister.name"
                placeholder="请输入老师姓名"
                class="custom-input"
                clearable
              />
            </el-form-item>
          </div>
          <div class="teacher-search-actions">
            <el-button type="primary" @click="searchTeacherProfilesRegister">查询老师</el-button>
          </div>
          <div
            v-if="teacherSearchMessageRegister"
            class="teacher-search-feedback"
            :class="`is-${teacherSearchStateRegister}`"
          >
            <i
              class="fas"
              :class="teacherSearchStateRegister === 'single' ? 'fa-check-circle' : teacherSearchStateRegister === 'multiple' ? 'fa-layer-group' : 'fa-info-circle'"
            ></i>
            <span>{{ teacherSearchMessageRegister }}</span>
          </div>
          <div
            v-if="(teacherSearchStateRegister === 'single' || teacherSearchStateRegister === 'multiple') && teacherSearchResultsRegister.length"
            class="teacher-search-result-list"
          >
            <div
              v-for="item in teacherSearchResultsRegister"
              :key="`${item.teacherId}-${item.phone}`"
              class="teacher-search-result-item"
              :class="teacherSearchStateRegister === 'single' ? 'is-single' : 'is-disabled'"
            >
              <div class="teacher-result-main">
                <span class="teacher-result-name">{{ item.teacherName }}</span>
                <span class="teacher-result-phone">{{ item.phone }}</span>
              </div>
              <div class="teacher-result-badge">重复命中</div>
            </div>
          </div>
        </div>

        <el-form
          :model="newTeacherRegister"
          label-width="90px"
          label-position="top"
          class="premium-form"
        >
          <div class="form-section-card">
            <div class="form-grid-2">
              <el-form-item label="姓名">
                <el-input
                  v-model="newTeacherRegister.name"
                  placeholder="请输入老师姓名"
                  disabled
                  class="custom-input"
                />
              </el-form-item>
              <el-form-item label="性别">
                <el-radio-group
                  v-model="newTeacherRegister.gender"
                  disabled
                  class="premium-radio-group"
                >
                  <el-radio-button label="男">男</el-radio-button>
                  <el-radio-button label="女">女</el-radio-button>
                </el-radio-group>
              </el-form-item>
            </div>
            <div class="form-grid-2">
              <el-form-item label="联系电话">
                <el-input
                  v-model="newTeacherRegister.phone"
                  placeholder="请输入联系电话，自动填充老师信息"
                  disabled
                  class="custom-input"
                >
                  <template #prefix>
                    <i class="fas fa-phone-alt icon-fade"></i>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="电子邮箱">
                <el-input
                  v-model="newTeacherRegister.email"
                  placeholder="请输入电子邮箱"
                  disabled
                  class="custom-input"
                >
                  <template #prefix><i class="fas fa-envelope icon-fade"></i></template>
                </el-input>
              </el-form-item>
            </div>
          </div>

          <div class="form-section-card">
            <div class="form-grid-2">
              <el-form-item label="单位">
                <el-input
                  v-model="newTeacherRegister.unit"
                  placeholder="请输入工作单位"
                  disabled
                  class="custom-input"
                />
              </el-form-item>
              <el-form-item label="职称">
                <el-input
                  v-model="newTeacherRegister.title"
                  placeholder="请输入职称"
                  disabled
                  class="custom-input"
                />
              </el-form-item>
            </div>
            <div class="form-grid-2">
              <el-form-item label="职务">
                <el-input
                  v-model="newTeacherRegister.position"
                  placeholder="请输入职务"
                  disabled
                  class="custom-input"
                />
              </el-form-item>
              <el-form-item label="政治面貌">
                <el-select
                  v-model="newTeacherRegister.politicalStatus"
                  placeholder="请选择"
                  disabled
                  style="width: 100%"
                  class="custom-input"
                >
                  <el-option label="中共党员" value="中共党员" />
                  <el-option label="中共预备党员" value="中共预备党员" />
                  <el-option label="共青团员" value="共青团员" />
                  <el-option label="群众" value="群众" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
            </div>
          </div>

          <div class="form-section-card">
            <div class="form-grid-2">
              <el-form-item label="出生年月">
                <el-date-picker
                  v-model="newTeacherRegister.birthDate"
                  type="month"
                  placeholder="选择年月"
                  style="width: 100%"
                  disabled
                  class="custom-input"
                />
              </el-form-item>
              <el-form-item label="电子邮箱">
                <el-input
                  v-model="newTeacherRegister.email"
                  placeholder="请输入电子邮箱"
                  disabled
                  class="custom-input"
                >
                  <template #prefix><i class="fas fa-envelope icon-fade"></i></template>
                </el-input>
              </el-form-item>
            </div>
          </div>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addTeacherDialogVisibleRegister = false">取消</el-button>
          <el-button type="primary" @click="confirmAddTeacherRegister">确认添加</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 指导老师详情对话框（未报名状态） -->
    <el-dialog
      v-model="showTeacherDialogRegister"
      :title="isEditModeRegister ? '编辑指导老师' : '查看指导老师'"
      width="580px"
      class="premium-dialog"
    >
      <div class="dialog-body-scroller">
        <div class="dialog-header-accent">
          <div class="accent-icon">
            <i class="fas fa-chalkboard-teacher"></i>
          </div>
          <div class="accent-text">
            <h3>{{ isEditModeRegister ? "编辑信息" : "详细资料" }}</h3>
            <p>输入手机号或姓名自动填充老师信息</p>
          </div>
        </div>

        <div class="search-section">
          <div class="form-grid-2">
            <el-form-item label="老师手机号">
              <el-input
                v-model="teacherSearchFormRegister.phone"
                placeholder="输入老师手机号"
                class="custom-input"
              >
                <template #prefix><i class="fas fa-phone-alt icon-fade"></i></template>
              </el-input>
            </el-form-item>
            <el-form-item label="老师姓名">
              <el-input
                v-model="teacherSearchFormRegister.name"
                placeholder="输入老师姓名"
                class="custom-input"
              >
                <template #prefix><i class="fas fa-user icon-fade"></i></template>
              </el-input>
            </el-form-item>
          </div>
          <div class="search-actions">
            <el-button
              type="primary"
              @click="searchTeacherProfilesRegister"
              :loading="teacherSearchStateRegister === 'searching'"
              class="search-button"
            >
              <i class="fas fa-search"></i> 查找老师
            </el-button>
          </div>
          <div v-if="teacherSearchMessageRegister" class="search-message">
            {{ teacherSearchMessageRegister }}
          </div>
        </div>

        <div class="form-section-card">
          <div class="form-grid-2">
            <el-form-item label="姓名" required>
              <el-input
                v-model="newTeacherRegister.name"
                placeholder="请输入老师姓名"
                :disabled="!isEditModeRegister"
                class="custom-input"
              >
                <template #prefix><i class="fas fa-user icon-fade"></i></template>
              </el-input>
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="newTeacherRegister.gender" :disabled="!isEditModeRegister">
                <el-radio-button label="男">男</el-radio-button>
                <el-radio-button label="女">女</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </div>
          <div class="form-grid-2">
            <el-form-item label="联系电话">
              <el-input
                v-model="newTeacherRegister.phone"
                placeholder="请输入联系电话"
                :disabled="!isEditModeRegister"
                class="custom-input"
              >
                <template #prefix><i class="fas fa-phone-alt icon-fade"></i></template>
              </el-input>
            </el-form-item>
            <el-form-item label="电子邮箱">
              <el-input
                v-model="newTeacherRegister.email"
                placeholder="请输入电子邮箱"
                :disabled="!isEditModeRegister"
                class="custom-input"
              >
                <template #prefix><i class="fas fa-envelope icon-fade"></i></template>
              </el-input>
            </el-form-item>
          </div>
        </div>

        <div class="form-section-card">
          <div class="form-grid-2">
            <el-form-item label="单位">
              <el-input
                v-model="newTeacherRegister.unit"
                placeholder="请输入工作单位"
                :disabled="!isEditModeRegister"
                class="custom-input"
              />
            </el-form-item>
            <el-form-item label="职称">
              <el-input
                v-model="newTeacherRegister.title"
                placeholder="请输入职称"
                :disabled="!isEditModeRegister"
                class="custom-input"
              />
            </el-form-item>
          </div>
          <div class="form-grid-2">
            <el-form-item label="职务">
              <el-input
                v-model="newTeacherRegister.position"
                placeholder="请输入职务"
                :disabled="!isEditModeRegister"
                class="custom-input"
              />
            </el-form-item>
            <el-form-item label="政治面貌">
              <el-select
                v-model="newTeacherRegister.politicalStatus"
                placeholder="请选择"
                :disabled="!isEditModeRegister"
                style="width: 100%"
                class="custom-input"
              >
                <el-option label="中共党员" value="中共党员" />
                <el-option label="共青团员" value="共青团员" />
                <el-option label="民主党派" value="民主党派" />
                <el-option label="群众" value="群众" />
              </el-select>
            </el-form-item>
          </div>
        </div>

        <div class="form-section-card">
          <div class="form-grid-2">
            <el-form-item label="出生年月">
              <el-date-picker
                v-model="newTeacherRegister.birthDate"
                type="date"
                placeholder="选择日期"
                :disabled="!isEditModeRegister"
                style="width: 100%"
                class="custom-input"
              />
            </el-form-item>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showTeacherDialogRegister = false">取消</el-button>
          <el-button type="primary" v-if="isEditModeRegister" @click="confirmAddTeacherRegister">
            确认修改
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 队长信息编辑对话框 -->
    <el-dialog
      v-model="editLeaderDialogVisible"
      title="编辑队长信息"
      width="500px"
      class="premium-dialog"
      destroy-on-close
    >
      <div class="invite-dialog-content">
        <div class="invite-icon"><i class="fas fa-user-edit"></i></div>
        <p class="invite-tip">在这里补充后，会同步更新到个人中心信息</p>
        <el-form ref="leaderFormRef" :model="leaderForm" :rules="memberRules" label-width="100px">
          <el-form-item label="姓名" prop="name" required>
            <el-input v-model="leaderForm.name" placeholder="请输入队长姓名" class="invite-input">
              <template #prefix><i class="fas fa-user"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item label="学号" prop="studentNo" required>
            <el-input v-model="leaderForm.studentNo" placeholder="请输入队长学号" class="invite-input">
              <template #prefix><i class="fas fa-id-card"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item label="学院" prop="college">
            <el-select
              v-model="leaderForm.college"
              placeholder="请选择队长学院"
              class="invite-input"
              filterable
              @change="onMemberCollegeChange(leaderForm)"
            >
              <el-option v-for="college in collegeOptions" :key="college" :label="college" :value="college" />
            </el-select>
          </el-form-item>
          <el-form-item label="专业" prop="major" required>
            <el-select
              v-model="leaderForm.major"
              :disabled="!leaderForm.college"
              placeholder="请先选择学院"
              class="invite-input"
              filterable
            >
              <el-option v-for="major in leaderMajorOptions" :key="major" :label="major" :value="major" />
            </el-select>
          </el-form-item>
          <el-form-item label="电话" prop="phone">
            <el-input v-model="leaderForm.phone" placeholder="请输入队长电话" class="invite-input">
              <template #prefix><i class="fas fa-phone"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="leaderForm.email" placeholder="请输入队长邮箱" class="invite-input">
              <template #prefix><i class="fas fa-envelope"></i></template>
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer-premium">
          <el-button @click="editLeaderDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="leaderSaving" @click="saveLeaderProfile">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="editMemberDialogVisible"
      :title="isMemberEditMode ? '编辑队员信息' : '查看队员信息'"
      width="500px"
      class="premium-dialog"
      destroy-on-close
    >
      <div class="invite-dialog-content">
        <div class="invite-icon"><i class="fas fa-user-edit"></i></div>
        <p class="invite-tip">{{ isMemberEditMode ? "请编辑队员信息" : "队员信息详情" }}</p>
        <el-form ref="editMemberFormRef" :model="editMemberForm" :rules="memberRules" label-width="100px">
          <el-form-item label="姓名" prop="name">
            <el-input v-model="editMemberForm.name" :disabled="!isMemberEditMode" placeholder="请输入队员姓名" class="invite-input">
              <template #prefix><i class="fas fa-user"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item label="学号" prop="studentNo">
            <el-input v-model="editMemberForm.studentNo" :disabled="!isMemberEditMode" placeholder="请输入队员学号" class="invite-input">
              <template #prefix><i class="fas fa-id-card"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item label="学院" prop="college">
            <el-select
              v-model="editMemberForm.college"
              :disabled="!isMemberEditMode"
              placeholder="请选择队员学院"
              class="invite-input"
              filterable
              @change="onMemberCollegeChange(editMemberForm)"
            >
              <el-option v-for="college in collegeOptions" :key="college" :label="college" :value="college" />
            </el-select>
          </el-form-item>
          <el-form-item label="专业" prop="major">
            <el-select
              v-model="editMemberForm.major"
              :disabled="!isMemberEditMode || !editMemberForm.college"
              placeholder="请先选择学院"
              class="invite-input"
              filterable
            >
              <el-option v-for="major in editMemberMajorOptions" :key="major" :label="major" :value="major" />
            </el-select>
          </el-form-item>
          <el-form-item label="电话" prop="phone">
            <el-input v-model="editMemberForm.phone" :disabled="!isMemberEditMode" placeholder="请输入队员电话" class="invite-input">
              <template #prefix><i class="fas fa-phone"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="editMemberForm.email" :disabled="!isMemberEditMode" placeholder="请输入队员邮箱" class="invite-input">
              <template #prefix><i class="fas fa-envelope"></i></template>
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer-premium">
          <el-button @click="editMemberDialogVisible = false">{{ isMemberEditMode ? "取消" : "关闭" }}</el-button>
          <el-button v-if="isMemberEditMode" type="primary" @click="confirmEditMember" class="btn-confirm">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 报名阶段：邀请队员对话框 -->
    <el-dialog
      v-model="inviteDialogVisibleRegister"
      title="邀请队员"
      width="400px"
      destroy-on-close
    >
      <el-form :model="inviteFormRegister" label-width="80px">
        <el-form-item label="学号" required>
          <el-input v-model="inviteFormRegister.studentNo" placeholder="请输入队员学号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="inviteDialogVisibleRegister = false">取消</el-button>
          <el-button type="primary" @click="confirmInviteRegister">发送邀请</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 报名阶段：添加队员对话框 -->
    <el-dialog
      v-model="addMemberDialogVisibleRegister"
      title="添加队员"
      width="500px"
      class="premium-dialog"
      destroy-on-close
    >
      <div class="invite-dialog-content">
        <div class="invite-icon"><i class="fas fa-user-plus"></i></div>
        <p class="invite-tip">请填写队员信息</p>
        <el-form ref="newMemberRegisterRef" :model="newMemberRegister" :rules="memberRules" label-width="100px">
          <el-form-item label="姓名" prop="name">
            <el-input
              v-model="newMemberRegister.name"
              placeholder="请输入姓名"
              class="invite-input"
            >
              <template #prefix><i class="fas fa-user"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item label="学号" prop="studentNo">
            <el-input
              v-model="newMemberRegister.studentNo"
              placeholder="请输入学号（输入后自动填充其他信息）"
              class="invite-input"
              @blur="fetchUserInfoByStudentNo(newMemberRegister)"
            >
              <template #prefix><i class="fas fa-id-card"></i></template>
            </el-input>
            <div class="student-no-hint">输入学号后，系统将自动为您填充其他信息</div>
          </el-form-item>
          <el-form-item label="学院" prop="college">
            <el-select
              v-model="newMemberRegister.college"
              placeholder="请选择学院"
              class="invite-input"
              filterable
              @change="onMemberCollegeChange(newMemberRegister)"
            >
              <el-option v-for="college in collegeOptions" :key="college" :label="college" :value="college" />
            </el-select>
          </el-form-item>
          <el-form-item label="专业" prop="major">
            <el-select
              v-model="newMemberRegister.major"
              placeholder="请先选择学院"
              class="invite-input"
              filterable
              :disabled="!newMemberRegister.college"
            >
              <el-option v-for="major in newMemberRegisterMajorOptions" :key="major" :label="major" :value="major" />
            </el-select>
          </el-form-item>
          <el-form-item label="电话" prop="phone">
            <el-input
              v-model="newMemberRegister.phone"
              placeholder="请输入电话"
              class="invite-input"
            >
              <template #prefix><i class="fas fa-phone"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model="newMemberRegister.email"
              placeholder="请输入邮箱"
              class="invite-input"
            >
              <template #prefix><i class="fas fa-envelope"></i></template>
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer-premium">
          <el-button @click="addMemberDialogVisibleRegister = false" class="btn-cancel"
            >取消</el-button
          >
          <el-button
            type="primary"
            @click="confirmAddMemberRegister"
            class="btn-confirm"
            >添加队员</el-button
          >
        </div>
      </template>
    </el-dialog>

    <!-- 队员详情对话框 -->
    <el-dialog
      v-model="memberDetailDialogVisible"
      :title="`队员 ${currentMemberIndex + 1} 详情`"
      width="500px"
      class="premium-dialog"
      destroy-on-close
    >
      <div class="invite-dialog-content">
        <div class="invite-icon"><i class="fas fa-user"></i></div>
        <p class="invite-tip">队员信息详情</p>
        <el-form :model="currentMember" label-width="100px">
          <el-form-item label="姓名" required>
            <el-input v-model="currentMember.name" disabled placeholder="请输入姓名" class="invite-input">
              <template #prefix><i class="fas fa-user"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item label="学号" required>
            <el-input v-model="currentMember.studentNo" disabled placeholder="请输入学号" class="invite-input">
              <template #prefix><i class="fas fa-id-card"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item label="学院">
            <el-input v-model="currentMember.college" disabled placeholder="请输入学院" class="invite-input">
              <template #prefix><i class="fas fa-university"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item label="专业" required>
            <el-input v-model="currentMember.major" disabled placeholder="请输入专业" class="invite-input">
              <template #prefix><i class="fas fa-book"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item label="电话">
            <el-input v-model="currentMember.phone" disabled placeholder="请输入电话" class="invite-input">
              <template #prefix><i class="fas fa-phone"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="currentMember.email" disabled placeholder="请输入邮箱" class="invite-input">
              <template #prefix><i class="fas fa-envelope"></i></template>
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer-premium">
          <el-button @click="memberDetailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import StudentNavbar from "@/components/StudentNavbar.vue";
import { ref, computed, onMounted, reactive, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox, ElLoading } from "element-plus";
import FileUpload from "@/components/FileUpload";
import {
  getCompetitionTeams,
  getCompetitionDetail,
  registerCompetition,
  saveCompetitionTeamName,
  applyJoinTeam,
  getCompetitionStatus,
  getUploadedMaterials,
  submitMaterials,
  inviteTeamMember,
  removeTeamMember,
  cancelRegistration,
} from "@/api/competition";
import { getToken } from "@/utils/auth";
import useUserStore from "@/store/modules/user";
import defAva from "@/assets/images/profile.jpg";
import { getUserProfile, getUserByStudentNo, updateUserProfile } from "@/api/system/user";
import { getStudentAvatars, getUserAvatarsByIds, getStudentAvatarsByNames } from "@/api/admin/user";
import { searchTeachers } from "@/api/teacher";
import { getColleges, getMajorsByCollege } from "@/api/college-major";
import { proxyFileUrl } from "@/utils/media";
import { isStudentRegistrationProfileComplete } from "@/utils/studentProfile";
import {
  getMemberStatusLabel,
  isApprovedMemberStatus,
  isPendingMemberStatus,
  isReviewLockedStatus,
  PARTICIPATION_STATUS,
} from "@/utils/competitionStatus";

const route = useRoute();
const router = useRouter();
const competitionId = route.params.id;
const apiBase = import.meta.env.VITE_APP_BASE_API;
const compName = ref(route.query.name || "");
const compDesc = ref(route.query.desc || "");
const compType = ref(route.query.type || "");
const startRaw = ref(route.query.start || "");
const endRaw = ref(route.query.end || "");

const availableTeams = ref([]);
const loadingTeams = ref(false);
const registerLoading = ref(false);

const registerForm = ref({
  registerType: "create",
  teamName: "",
  selectedTeamId: null,
  existingMemberCount: 0,
  leader: {
    name: "",
    studentNo: "",
    college: "",
    major: "",
    phone: "",
    email: "",
  },
  members: [],
  teachers: [],
});

// 队伍名称编辑相关
const editingTeamName = ref(false);
const editTeamNameForm = ref({ name: "" });

const userStatus = ref({
  registered: false,
  hasRecord: false,
  draft: false,
  complete: false,
  recordStatus: "",
  recordStatusText: "",
  reviewStatusCode: "",
  reviewStatusText: "",
  teamName: "",
  isLeader: false,
  canCancel: false,
  displayRegisterId: "",
  teamMembers: [],
  allMembers: [],
  teamId: null,
  missingFields: [],
});

// 队伍名称编辑相关
const createTeamNameSaved = ref(false);
const createTeamNameEditing = ref(false);

const lastSubmittedPaths = ref({
  workName: "",
  workDescription: "",
  docPath: "",
  pptPath: "",
  otherPath: "",
});

const hasUnsavedChanges = computed(() => {
  const current = {
    workName: (materialForm.value.workName || "").trim(),
    workDescription: (materialForm.value.workDescription || "").trim(),
    docPath: getPathString(materialForm.value.docPath),
    pptPath: getPathString(materialForm.value.pptPath),
    otherPath: getPathString(materialForm.value.otherPath),
  };
  return (
    current.workName !== lastSubmittedPaths.value.workName ||
    current.workDescription !== lastSubmittedPaths.value.workDescription ||
    current.docPath !== lastSubmittedPaths.value.docPath ||
    current.pptPath !== lastSubmittedPaths.value.pptPath ||
    current.otherPath !== lastSubmittedPaths.value.otherPath
  );
});

// Helper function to get URL string from potential array/object
const getPathString = (pathVal) => {
  if (!pathVal) return "";
  // If it's a string (e.g. from FileUpload modelValue), return it
  if (typeof pathVal === "string") {
    // Remove any trailing commas if multiple files
    return pathVal.split(",")[0].trim();
  }
  // If it's an array of objects (e.g. from initial load)
  if (Array.isArray(pathVal)) {
    if (pathVal.length > 0) {
      const first = pathVal[0];
      if (typeof first === "object" && first !== null) {
        return first.url || first.path || "";
      }
      return String(first);
    }
    return "";
  }
  if (typeof pathVal === "object" && pathVal !== null) {
    return pathVal.url || pathVal.path || "";
  }
  return String(pathVal);
};

const registered = ref(false);
const materialForm = ref({
  registerId: "",
  workName: "",
  workDescription: "",
  docPath: "",
  docFiles: [],
  pptPath: "",
  pptFiles: [],
  otherPath: "",
  otherFiles: [],
});
const displayRegisterId = ref("");
const materialLoading = ref(false);
const inviteStudentNo = ref("");
const lastSubmitTime = ref("");
const participationStatus = ref("");
const isTeamReviewLocked = computed(() =>
  isReviewLockedStatus(
    userStatus.value.reviewStatusCode,
    userStatus.value.reviewStatusText,
  ),
);
const isReadOnlyMemberView = computed(
  () => userStatus.value.hasRecord && (!userStatus.value.isLeader || isTeamReviewLocked.value),
);
const showRegisteredSection = computed(
  () => userStatus.value.hasRecord || isReadOnlyMemberView.value,
);
const hasAnyFiles = computed(
  () =>
    !!(
      materialForm.value.docPath ||
      materialForm.value.pptPath ||
      materialForm.value.otherPath
    ),
);
const hasRequiredMaterialFiles = computed(
  () =>
    !!(
      getPathString(materialForm.value.docPath) &&
      getPathString(materialForm.value.pptPath)
    ),
);
const hasWorkInfo = computed(
  () =>
    !!(
      (materialForm.value.workName || "").trim() &&
      (materialForm.value.workDescription || "").trim()
    ),
);
const currentTeacherList = computed(() =>
  showRegisteredSection.value ? teacherList.value : registerForm.value.teachers,
);
const needsTeacherValidation = computed(
  () => registered.value || registerForm.value.registerType === "create",
);
const hasTeacherInfo = computed(
  () =>
    Array.isArray(currentTeacherList.value) &&
    currentTeacherList.value.some((item) => {
      const name = String(item?.name || item?.teacherName || "").trim();
      return !!name;
    }),
);
const submissionValidationTriggered = ref(false);
const showTeacherRequiredError = computed(
  () =>
    submissionValidationTriggered.value &&
    needsTeacherValidation.value &&
    !hasTeacherInfo.value,
);
const showWorkRequiredError = computed(
  () => submissionValidationTriggered.value && !hasWorkInfo.value,
);
const showMaterialRequiredError = computed(
  () => submissionValidationTriggered.value && !hasRequiredMaterialFiles.value,
);
const showUploadSubmitError = computed(
  () =>
    showTeacherRequiredError.value ||
    showWorkRequiredError.value ||
    showMaterialRequiredError.value,
);
const isFinalSuccess = computed(() => {
  // 必须提交申报书和 PPT，且没有未保存的更改。
  if (hasUnsavedChanges.value) return false;
  if (!hasRequiredMaterialFiles.value) return false;
  if (!hasWorkInfo.value) return false;
  return !!(
    lastSubmittedPaths.value.docPath &&
    lastSubmittedPaths.value.pptPath
  );
});
const submitBtnText = computed(() =>
  hasAnyFiles.value ? "更新并提交资料" : "提交资料",
);
const heroUploadButtonText = computed(() =>
  !registered.value && registerForm.value.registerType === "join"
    ? "申请加入"
    :
  userStatus.value.recordStatus === "draft" && userStatus.value.hasRecord
    ? "待完善后上传资料"
    : showUploadSubmitError.value ? "有未填写，上传失败" : "上传资料",
);
const summaryTeacherNames = computed(() => {
  const source = showRegisteredSection.value ? teacherList.value : registerForm.value.teachers;
  return Array.isArray(source) ? source.map((item) => item?.name).filter(Boolean).join("、") : "";
});
const summaryTeamName = computed(() =>
  showRegisteredSection.value ? userStatus.value.teamName || registerForm.value.teamName || "未命名队伍" : registerForm.value.teamName || userStatus.value.teamName || "未命名队伍",
);
function buildLiveMissingFields() {
  const missing = [];
  if (!summaryTeamName.value || summaryTeamName.value === "未命名队伍") {
    missing.push("队伍名称");
  }
  if (!hasTeacherInfo.value) {
    missing.push("指导老师");
  }
  const leader = getEffectiveLeaderInfo();
  const leaderComplete =
    !!leader.name &&
    !!leader.studentNo &&
    !!leader.college &&
    !!leader.major &&
    !!leader.phone &&
    !!leader.email;
  if (!leaderComplete) {
    missing.push("队长信息");
  }
  const members = registered.value ? otherMembers.value : registerForm.value.members;
  const hasCompleteMember = Array.isArray(members) && members.some(
    (member) =>
      !!member?.name &&
      !!member?.studentNo &&
      !!member?.college &&
      !!member?.major &&
      !!member?.phone &&
      !!member?.email &&
      !isPendingMemberStatus(member?.status),
  );
  if (!hasCompleteMember) {
    missing.push("至少一位队员");
  }
  if (!(materialForm.value.workName || "").trim()) {
    missing.push("作品名称");
  }
  if (!(materialForm.value.workDescription || "").trim()) {
    missing.push("作品简介");
  }
  if (!hasRequiredMaterialFiles.value) {
    missing.push("申报书资料/PPT演示资料");
  }
  if ((userStatus.value.teamMembers || []).some((m) => isPendingMemberStatus(m.status))) {
    missing.push("待队员确认");
  }
  return missing;
}
const summaryMissingFields = computed(() => {
  if (userStatus.value.hasRecord && isReadOnlyMemberView.value) {
    return Array.isArray(userStatus.value.missingFields)
      ? [...userStatus.value.missingFields]
      : [];
  }
  return buildLiveMissingFields();
});
const showSummaryCard = computed(
  () =>
    !!(
      userStatus.value.hasRecord ||
      registerForm.value.teamName ||
      registerForm.value.teachers.length ||
      registerForm.value.members.length
    ),
);
const summaryStatusText = computed(() =>
  userStatus.value.hasRecord && userStatus.value.recordStatusText
    ? userStatus.value.recordStatusText
    : summaryMissingFields.value.length ? "待完善信息" : "状态正常",
);
const displayParticipationStatus = computed(() =>
  userStatus.value.reviewStatusText ||
    (participationStatus.value === PARTICIPATION_STATUS.SCORED
      ? "已评审"
      : lastSubmitTime.value
        ? "待审核"
        : participationStatus.value || PARTICIPATION_STATUS.NOT_SUBMITTED),
);
const isCurrentProfileComplete = computed(() => {
  return isStudentRegistrationProfileComplete(
    currentProfile.value || {},
    registerForm.value.leader || {},
  );
});

function getEffectiveLeaderInfo() {
  const profile = currentProfile.value || {};
  const leaderFromForm = registerForm.value.leader || {};
  const leaderFromTeam = leaderMember.value || {};
  return {
    name:
      leaderFromTeam.name ||
      profile.studentName ||
      profile.name ||
      profile.nickName ||
      leaderFromForm.name ||
      "",
    studentNo:
      leaderFromTeam.studentNo ||
      profile.studentNo ||
      leaderFromForm.studentNo ||
      "",
    college:
      leaderFromTeam.college ||
      profile.dept?.deptName ||
      profile.collegeName ||
      profile.college ||
      leaderFromForm.college ||
      "",
    major:
      leaderFromTeam.major ||
      profile.majorName ||
      profile.major ||
      leaderFromForm.major ||
      "",
    phone:
      leaderFromTeam.phone ||
      profile.phonenumber ||
      profile.phone ||
      leaderFromForm.phone ||
      "",
    email:
      leaderFromTeam.email ||
      profile.email ||
      leaderFromForm.email ||
      "",
  };
}

const isLeaderProfileComplete = computed(() => {
  const leader = getEffectiveLeaderInfo();
  return !!(
    leader.name &&
    leader.studentNo &&
    leader.college &&
    leader.major &&
    leader.phone &&
    leader.email
  );
});

function ensureLeaderInfoReadyForTeacher() {
  if (isLeaderProfileComplete.value) {
    return true;
  }
  ElMessage.warning("请先完善队长信息后再添加指导老师");
  return false;
}
const showTeacherDialog = ref(false);
const showMemberDialog = ref(false);
const isEditMode = ref(false);
const isEditModeRegister = ref(false);
const showTeacherDialogRegister = ref(false);
const currentEditTeacherIndex = ref(null);
const editTeacherIndex = ref(null);
const teacherSearchForm = reactive({
  phone: "",
  name: "",
});
const tempTeacher = reactive({
  teacherId: "",
  name: "",
  gender: "",
  birthDate: "",
  enrollYear: "",
  phone: "",
  email: "",
  unit: "",
  title: "",
  position: "",
  politicalStatus: ""
});
const teacherSearchState = ref("idle");
const teacherSearchMessage = ref("");
const teacherSearchResults = ref([]);
const currentMember = ref({});
const currentMemberIndex = ref(0);
const memberDetailDialogVisible = ref(false);
const showInvite = ref(false);
const showAddMember = ref(false);
const newMemberForm = reactive({
  name: '',
  studentNo: '',
  college: '',
  major: '',
  phone: '',
  email: ''
});

// 查看队员详情
function viewMemberDetails(member, index) {
  currentMember.value = member;
  currentMemberIndex.value = index;
  memberDetailDialogVisible.value = true;
}

const collegeMajorMap = ref({});
const collegeOptions = computed(() => Object.keys(collegeMajorMap.value));

async function loadCollegeMajorData() {
  try {
    const res = await getColleges();
    if (res && res.data) {
      const colleges = res.data;
      const map = {};
      const majorEntries = await Promise.all(
        colleges.map(async (college) => {
          try {
            const majorsRes = await getMajorsByCollege(college);
            return [college, majorsRes && majorsRes.data ? majorsRes.data : []];
          } catch (error) {
            console.error(`加载学院 ${college} 专业失败:`, error);
            return [college, []];
          }
        }),
      );
      majorEntries.forEach(([college, majors]) => {
        map[college] = majors;
      });
      collegeMajorMap.value = map;
    }
  } catch (error) {
    console.error('加载学院专业数据失败:', error);
    collegeMajorMap.value = {
      "计算机学院": ["计算机科学与技术", "软件工程", "网络工程", "数据科学与大数据技术"],
      "电子信息学院": ["电子信息工程", "通信工程", "微电子科学与工程", "集成电路设计与集成系统"],
      "数学学院": ["数学与应用数学", "信息与计算科学", "统计学", "应用统计学"],
      "人工智能学院": ["人工智能", "智能科学与技术", "机器人工程", "模式识别与智能系统"],
    };
  }
}

const getMajorOptions = (college) => collegeMajorMap.value[college] || [];
const newMemberMajorOptions = computed(() => getMajorOptions(newMemberForm.college));
const newMemberRegisterMajorOptions = computed(() => getMajorOptions(newMemberRegister.value.college));
const editMemberMajorOptions = computed(() => getMajorOptions(editMemberForm.value.college));

function onMemberCollegeChange(member, field = "major") {
  if (member) {
    member[field] = "";
  }
}

async function fetchUserInfoByStudentNo(member) {
  if (!member.studentNo) return;
  
  try {
    const res = await getUserByStudentNo(member.studentNo);
    if (res && res.data) {
      const user = res.data;
      if (!member.name) {
        member.name = user.studentName || user.nickName || user.userName || "";
      }
      if (!member.college) {
        member.college = user.collegeName || (user.dept && user.dept.deptName) || "";
      }
      if (!member.major) {
        member.major = user.majorName || user.major || "";
      }
      if (!member.phone) {
        member.phone = user.phonenumber || user.phone || "";
      }
      if (!member.email) {
        member.email = user.email || "";
      }
      ElMessage.success("已自动填充用户信息");
    } else {
      ElMessage.info("未找到该学号的用户，请手动填写信息");
    }
  } catch (error) {
    console.error('查询用户信息失败:', error);
    ElMessage.warning("查询用户信息失败，请手动填写");
  }
}

// 队员表单验证规则
const memberRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { len: 12, message: '学号必须为12位数字', trigger: ['blur', 'change'] },
    { pattern: /^\d{12}$/, message: '学号格式不正确，必须为12位数字', trigger: ['blur', 'change'] }
  ],
  college: [{ required: true, message: '请选择学院', trigger: 'change' }],
  major: [{ required: true, message: '请选择专业', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入电话', trigger: 'blur' },
    { len: 11, message: '手机号必须为11位数字', trigger: ['blur', 'change'] },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确，必须为11位数字且1开头', trigger: ['blur', 'change'] }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ]
};

const newMemberFormRef = ref(null);
const newMemberRegisterRef = ref(null);
const editMemberFormRef = ref(null);

const userStore = useUserStore();
const myClass = ref("");
const currentProfile = ref({});
const fileSizes = ref({ docPath: "", pptPath: "", otherPath: "" });

function formatBytes(bytes) {
  if (!bytes || bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + " " + sizes[i];
}

async function fetchFileSize(path, field) {
  if (!path) {
    fileSizes.value[field] = "";
    return;
  }
  try {
    let url = viewUrl(path);
    if (!url.startsWith("http")) {
      url = window.location.origin + url;
    }

    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 3000);

    const response = await fetch(url, {
      method: "HEAD",
      headers: {
        Authorization: "Bearer " + getToken(),
      },
      signal: controller.signal,
    });
    clearTimeout(timeoutId);

    const size = response.headers.get("Content-Length");
    if (size && parseInt(size) > 2048) {
      // 过滤掉极小的错误页面响应 (通常 < 2KB)
      fileSizes.value[field] = formatBytes(parseInt(size));
    } else if (size && parseInt(size) > 0) {
      // 如果真的很小，也显示
      fileSizes.value[field] = formatBytes(parseInt(size));
    } else {
      fileSizes.value[field] = "已就绪";
    }
  } catch (e) {
    fileSizes.value[field] = "已就绪";
  }
}

watch(
  () => materialForm.value.docPath,
  (v) => fetchFileSize(v, "docPath"),
  { immediate: true },
);
watch(
  () => materialForm.value.pptPath,
  (v) => fetchFileSize(v, "pptPath"),
  { immediate: true },
);
watch(
  () => materialForm.value.otherPath,
  (v) => fetchFileSize(v, "otherPath"),
  { immediate: true },
);

const avatarTs = ref(Date.now());
watch(
  () => userStore.avatar,
  () => {
    avatarTs.value = Date.now();
  },
);
watch(
  () => [route.query.teamId, route.query.registerId],
  () => {
    refreshStatus();
  },
);
const myStudentNo = ref("");
async function loadCurrentProfile() {
  try {
    const res = await getUserProfile();
    const u = res?.data || {};
    currentProfile.value = u;
    myClass.value = u.className || u.clazz || "";
    myStudentNo.value = u.studentNo || "";
    return u;
  } catch {
    currentProfile.value = {};
    return {};
  }
}

function fmt(d) {
  if (!d) return "";
  const dt = new Date(d);
  if (isNaN(dt.getTime())) return String(d).slice(0, 16);
  const y = dt.getFullYear();
  const m = String(dt.getMonth() + 1).padStart(2, "0");
  const da = String(dt.getDate()).padStart(2, "0");
  const hh = String(dt.getHours()).padStart(2, "0");
  const mm = String(dt.getMinutes()).padStart(2, "0");
  return `${y}-${m}-${da} ${hh}:${mm}`;
}

const registerRange = computed(() => {
  const s = fmt(startRaw.value);
  const e = fmt(endRaw.value);
  if (!s && !e) return "";
  return `${s || "未设定"} ~ ${e || "未设定"}`;
});

const statusText = computed(() => {
  if (!startRaw.value || !endRaw.value) return "";
  const now = Date.now();
  const s = new Date(startRaw.value).getTime();
  const e = new Date(endRaw.value).getTime();
  if (isNaN(s) || isNaN(e)) return "";
  if (now < s) return "未开始";
  if (now > e) return "已结束";
  return "报名中";
});

async function ensureCompMeta() {
  if (
    compName.value &&
    compDesc.value &&
    compType.value &&
    startRaw.value &&
    endRaw.value
  )
    return;
  const cacheKey = `comp_meta_${competitionId}`;
  try {
    const cached = sessionStorage.getItem(cacheKey);
    if (cached) {
      const m = JSON.parse(cached);
      compName.value = compName.value || m.competitionName || "";
      compDesc.value = compDesc.value || m.description || "";
      compType.value = compType.value || m.competitionType || "";
      startRaw.value = startRaw.value || m.registerStartTime || "";
      endRaw.value = endRaw.value || m.registerEndTime || "";
      return;
    }
  } catch {}
  try {
    const res = await getCompetitionDetail(competitionId);
    const m = res?.data || {};
    compName.value = compName.value || m.competitionName || "";
    compDesc.value = compDesc.value || m.description || "";
    compType.value = compType.value || m.competitionType || "";
    startRaw.value = startRaw.value || m.registerStartTime || "";
    endRaw.value = endRaw.value || m.registerEndTime || "";
    try {
      sessionStorage.setItem(cacheKey, JSON.stringify(m));
    } catch {}
  } catch (error) {
    console.error("加载赛事元数据失败:", error);
  }
}

const advisorNames = computed(() => {
  return teacherList.value.map((t) => t.name).join("、");
});
const teacherList = ref([]);

function buildTeamMembersPayload(members) {
  if (!Array.isArray(members)) {
    return [];
  }
  return members.map((member) => {
    const { teacherName, teacherDetails, ...rest } = member || {};
    return { ...rest };
  });
}

const leaderMember = computed(() => {
  const all = userStatus.value.allMembers || [];
  const found = all.find(
    (m) => String(m.role || "").toLowerCase() === "leader",
  );
  return found || all[0] || null;
});
const otherMembers = computed(() => {
  const all = userStatus.value.teamMembers || [];
  return all.filter((m) => String(m.role || "").toLowerCase() !== "leader");
});

const memberLabelMap = {
  name: "姓名",
  studentName: "姓名",
  nickName: "昵称",
  userName: "用户名",
  studentNo: "学号",
  major: "专业",
  majorName: "专业",
  phone: "联系电话",
  phonenumber: "联系电话",
  email: "电子邮箱",
  sex: "性别",
  gender: "性别",
  class: "班级",
  className: "班级",
  clazz: "班级",
  college: "学院",
  collegeName: "学院",
  deptName: "院系",
  teamName: "队伍名称",
  role: "队内角色",
  status: "状态",
  teacherName: "指导老师",
  userId: "用户ID"
};

const memberDetailItems = computed(() => {
  const raw = currentMember.value || {};
  const orderedKeys = [
    "name",
    "studentName",
    "studentNo",
    "major",
    "majorName",
    "phone",
    "phonenumber",
    "email",
    "sex",
    "gender",
    "collegeName",
    "college",
    "className",
    "class",
    "clazz",
    "teamName",
    "role",
    "status",
    "teacherName",
    "userId"
  ];
  const hiddenKeys = new Set([
    "avatarUrl",
    "teacherDetails",
    "docFiles",
    "pptFiles",
    "otherFiles"
  ]);
  const items = [];
  const used = new Set();

  orderedKeys.forEach((key) => {
    const value = formatMemberFieldValue(key, raw[key]);
    if (value === "") return;
    items.push({
      key,
      label: memberLabelMap[key] || key,
      value
    });
    used.add(key);
  });

  Object.entries(raw).forEach(([key, value]) => {
    if (used.has(key) || hiddenKeys.has(key)) return;
    const formatted = formatMemberFieldValue(key, value);
    if (formatted === "") return;
    items.push({
      key,
      label: memberLabelMap[key] || key,
      value: formatted
    });
  });

  return items;
});

function isCurrentMember(member) {
  if (!member) return false;
  return (
    (String(member.userId || "") !== "" &&
      String(member.userId) === String(userStore.id || "")) ||
    (!!myStudentNo.value &&
      String(member.studentNo || "") === String(myStudentNo.value || ""))
  );
}

function formatMemberFieldValue(key, value) {
  if (value === undefined || value === null) return "";
  if (typeof value === "string" && value.trim() === "") return "";

  if (key === "sex" || key === "gender") {
    return normalizeGenderValue(value) || String(value);
  }

  if (key === "role") {
    const role = String(value).toLowerCase();
    if (role === "leader") return "队长";
    if (role === "member") return "队员";
  }

  if (key === "status") {
    return getMemberStatusLabel(value);
  }

  if (Array.isArray(value)) {
    return value.length ? value.join("，") : "";
  }

  if (typeof value === "object") {
    try {
      return JSON.stringify(value);
    } catch {
      return "";
    }
  }

  return String(value);
}

function getMemberBadgeText(member, index) {
  const statusText = getMemberStatusLabel(member?.status, member?.type);
  return statusText || `队员 ${index + 1}`;
}

async function viewMemberDetail(member) {
  if (!member) return;

  const detail = {
    ...member,
    name: member.name || member.studentName || member.nickName || "未填写",
    major: member.major || member.majorName || "未填写",
    studentNo: member.studentNo || "未填写",
    phone: member.phone || member.phonenumber || "",
    className: member.className || member.class || member.clazz || classFor(member)
  };

  if (isCurrentMember(member)) {
    try {
      const res = await getUserProfile();
      const profile = res?.data || {};
      detail.name = detail.name || profile.studentName || profile.nickName || profile.userName || "未填写";
      detail.studentNo = detail.studentNo || profile.studentNo || "未填写";
      detail.major = detail.major || profile.majorName || profile.major || "未填写";
      detail.phone = detail.phone || profile.phonenumber || "";
      detail.email = detail.email || profile.email || "";
      detail.sex = detail.sex ?? profile.sex;
      detail.className = detail.className || profile.className || profile.clazz || "";
      detail.collegeName = detail.collegeName || profile.collegeName || (profile.dept && profile.dept.deptName) || "";
      detail.userId = detail.userId || profile.userId || "";
    } catch {}
  }

  currentMember.value = detail;
  showMemberDialog.value = true;
}

function avatarFor(member) {
  try {
    if (!member)
      return userStore.avatar
        ? `${userStore.avatar}?__t=${avatarTs.value}`
        : defAva;

    // 1. 优先判断是否是“我本人” - 本人头像始终以 store 为准，确保实时性
    const isMe =
      (String(member.userId || "") !== "" &&
        String(member.userId) === String(userStore.id || "")) ||
      (!!myStudentNo.value &&
        String(member.studentNo || "") === String(myStudentNo.value || ""));

    if (isMe) {
      return userStore.avatar
        ? `${userStore.avatar}?__t=${avatarTs.value}`
        : defAva;
    }

    // 2. 其他成员：使用团队数据中回填的 avatarUrl
    if (member.avatarUrl && typeof member.avatarUrl === "string") {
      return member.avatarUrl;
    }

    // 3. 兜底：占位图
    return defAva;
  } catch {
    return defAva;
  }
}

function classFor(member) {
  try {
    if (!member) return myClass.value || "";
    if (
      String(member.userId || "") !== "" &&
      String(member.userId) === String(userStore.id || "")
    ) {
      return (
        myClass.value ||
        member.class ||
        member.className ||
        member.clazz ||
        "未填写"
      );
    }
    const myName = (userStore.name || userStore.nickName || "").trim();
    if ((member.name || "").trim() && (member.name || "").trim() === myName) {
      return (
        myClass.value ||
        member.class ||
        member.className ||
        member.clazz ||
        "未填写"
      );
    }
    return member.class || member.className || member.clazz || "未填写";
  } catch {
    return member?.class || member?.className || member?.clazz || "未填写";
  }
}

function loadTeams() {
  if (loadingTeams.value) return;
  loadingTeams.value = true;
  getCompetitionTeams(competitionId)
    .then((res) => {
      const list = Array.isArray(res.data) ? res.data : [];
      availableTeams.value = list.map((t) => {
        let members = [];
        try {
          members = t.teamMembers ? JSON.parse(t.teamMembers) : [];
          if (!Array.isArray(members)) members = [];
        } catch (e) {
          members = [];
        }
        const approved = members.filter((m) => {
          const s = String(m.status || "").toLowerCase();
          return s === "" || s === "approved";
        });
        return {
          ...t,
          memberCount: approved.length,
          teamMembers: JSON.stringify(approved),
        };
      });
    })
    .finally(() => {
      loadingTeams.value = false;
    });
}

function handleRegisterTypeChange() {
  if (registerForm.value.registerType === "join") {
    registerForm.value.teamName = "";
    registerForm.value.members = [];
    registerForm.value.existingMemberCount = 0;
    loadTeams();
  } else {
    registerForm.value.members = [];
  }
}

function addMember() {
  registerForm.value.members.push({
    name: "",
    major: "",
    studentNo: "",
    teacherName: "",
  });
}

function removeMember(index) {
  ElMessageBox.confirm("确定要移除该队员吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      registerForm.value.members.splice(index, 1);
      ElMessage.success("已移除队员");
      persistDraftRegistration({ silent: true });
    })
    .catch(() => {});
}

function canManageTeamMembers() {
  if (!userStatus.value.isLeader) {
    ElMessage.warning("只有队长才能操作");
    return false;
  }
  if (isTeamReviewLocked.value) {
    ElMessage.warning("队伍已进入评审中或已评审，不能再修改报名信息");
    return false;
  }
  return true;
}

function handleMemberEdit(member, index, source = "draft") {
  if (!canManageTeamMembers()) return;
  if (source === "registered") {
    openEditMemberDialog(member, index);
    return;
  }
  openEditMemberDialog(member, index);
}

function handleMemberRemove(member, index, source = "draft") {
  if (!canManageTeamMembers()) return;
  if (source === "registered") {
    removeMemberFromTeam(member.studentNo);
    return;
  }
  removeMember(index);
}

function removeTeacher(index) {
  registerForm.value.teachers.splice(index, 1);
  persistDraftRegistration({ silent: true });
}

// 打开添加指导老师对话框（报名阶段）
const addTeacherDialogVisibleRegister = ref(false);
const newTeacherRegister = ref({
  teacherId: "",
  name: "",
  gender: "",
  title: "",
  phone: "",
  email: "",
  unit: "",
  position: "",
  politicalStatus: "",
  birthDate: "",
});

// 添加指导老师搜索相关变量
const teacherSearchFormRegister = ref({
  phone: "",
  name: "",
});
const teacherSearchStateRegister = ref("idle");
const teacherSearchMessageRegister = ref("");
const teacherSearchResultsRegister = ref([]);

function openAddTeacherRegister() {
  if (!ensureLeaderInfoReadyForTeacher()) {
    return;
  }
  newTeacherRegister.value = { teacherId: "", name: "", gender: "", title: "", phone: "", email: "", unit: "", position: "", politicalStatus: "", birthDate: "" };
  teacherSearchFormRegister.value = { phone: "", name: "" };
  teacherSearchStateRegister.value = "idle";
  teacherSearchMessageRegister.value = "";
  teacherSearchResultsRegister.value = [];
  addTeacherDialogVisibleRegister.value = true;
}

function searchTeacherProfilesRegister() {
  const phone = (teacherSearchFormRegister.value.phone || "").trim();
  const name = (teacherSearchFormRegister.value.name || "").trim();
  teacherSearchResultsRegister.value = [];
  teacherSearchStateRegister.value = "idle";
  teacherSearchMessageRegister.value = "";
  // 重置表单
  newTeacherRegister.value = { teacherId: "", name: "", gender: "", title: "", phone: "", email: "", unit: "" };
  
  if (!phone && !name) {
    ElMessage.warning("请输入老师手机号或姓名");
    return;
  }

  if (phone && phone.length !== 11) {
    ElMessage.warning("手机号必须为11位数字");
    return;
  }

  teacherSearchStateRegister.value = "searching";

  const params = phone ? { phone } : { name };
  searchTeachers(params)
    .then((response) => {
      const data = response?.data || {};
      const items = Array.isArray(data.items) ? data.items : [];
      teacherSearchResultsRegister.value = items;

      if (data.matchType === "single" && items.length === 1) {
        teacherSearchStateRegister.value = "single";
        teacherSearchMessageRegister.value = "已匹配到唯一老师，请确认添加";
        // 自动填充老师信息
        const teacher = items[0];
        newTeacherRegister.value = {
          teacherId: teacher.teacherId || teacher.id || "",
          name: teacher.teacherName || teacher.name || "",
          gender: normalizeGenderValue(teacher.sex) || teacher.gender || "",
          title: teacher.title || "",
          phone: teacher.phone || "",
          email: teacher.email || "",
          unit: teacher.workUnit || teacher.unit || "",
          position: teacher.position || "",
          politicalStatus: teacher.politicalStatus || "",
          birthDate: teacher.birthDate || "",
        };
        return;
      }

      if (data.matchType === "multiple" && items.length > 1) {
        teacherSearchStateRegister.value = "multiple";
        teacherSearchMessageRegister.value = "存在重复老师信息，请检查后重新输入";
        ElMessage.warning("存在重复老师信息，仅展示，不可选择");
        return;
      }

      teacherSearchStateRegister.value = "none";
      teacherSearchMessageRegister.value = "手机号或姓名不存在，请检查后重新输入";
      ElMessage.warning("未找到对应老师，请检查后重新输入");
    })
    .catch((error) => {
      console.error("查询老师失败:", error);
      teacherSearchStateRegister.value = "idle";
      teacherSearchMessageRegister.value = "";
      ElMessage.error(error?.message || "查询老师失败");
    });
}

function confirmAddTeacherRegister() {
  if (!ensureLeaderInfoReadyForTeacher()) {
    return;
  }
  if (!newTeacherRegister.value.name) {
    ElMessage.warning("请先查询并选择老师");
    return;
  }
  
  if (currentEditTeacherIndex.value === null) {
    // 添加新老师
    registerForm.value.teachers.push({ ...newTeacherRegister.value });
    ElMessage.success("添加指导老师成功");
  } else {
    // 编辑现有老师
    registerForm.value.teachers[currentEditTeacherIndex.value] = { ...newTeacherRegister.value };
    ElMessage.success("编辑指导老师成功");
    currentEditTeacherIndex.value = null;
  }
  
  addTeacherDialogVisibleRegister.value = false;
  showTeacherDialogRegister.value = false;
  persistDraftRegistration({ silent: true });
}

// 打开邀请队员对话框（报名阶段）
const inviteDialogVisibleRegister = ref(false);
const inviteFormRegister = ref({ studentNo: "" });

function openInviteDialogRegister() {
  inviteFormRegister.value = { studentNo: "" };
  inviteDialogVisibleRegister.value = true;
}

function confirmInviteRegister() {
  sendDraftInvite(inviteFormRegister.value.studentNo).then((ok) => {
    if (ok) {
      inviteDialogVisibleRegister.value = false;
      inviteFormRegister.value = { studentNo: "" };
    }
  });
}

// 队长信息查看对话框（数据来自个人中心）
const editLeaderDialogVisible = ref(false);
const leaderSaving = ref(false);
const leaderFormRef = ref(null);
const leaderForm = ref({
  name: "",
  studentNo: "",
  college: "",
  major: "",
  phone: "",
  email: "",
});
const leaderMajorOptions = computed(() => getMajorOptions(leaderForm.value.college));
const editMemberDialogVisible = ref(false);
const editMemberForm = ref({
  name: "",
  studentNo: "",
  college: "",
  major: "",
  phone: "",
  email: "",
});
const editMemberIndex = ref(-1);
const isMemberEditMode = ref(false);

function openViewLeaderDialog() {
  const src = leaderMember.value || registerForm.value.leader || {};
  leaderForm.value = {
    name: src.name || "",
    studentNo: src.studentNo || "",
    college: src.college || "",
    major: src.major || "",
    phone: src.phone || "",
    email: src.email || "",
  };
  editLeaderDialogVisible.value = true;
}

function saveLeaderProfile() {
  if (!leaderFormRef.value) {
    ElMessage.error("表单未准备完成，请稍后重试");
    return;
  }
  leaderFormRef.value.validate(async (valid) => {
    if (!valid) return;
    leaderSaving.value = true;
    try {
      await updateUserProfile({
        studentName: leaderForm.value.name,
        studentNo: leaderForm.value.studentNo,
        collegeName: leaderForm.value.college,
        majorName: leaderForm.value.major,
        phonenumber: leaderForm.value.phone,
        email: leaderForm.value.email,
      });
      registerForm.value.leader = {
        ...registerForm.value.leader,
        ...leaderForm.value,
      };
      if (registered.value || userStatus.value.hasRecord) {
        await refreshStatus();
      }
      editLeaderDialogVisible.value = false;
      ElMessage.success("队长信息已同步到个人中心");
    } catch (err) {
      ElMessage.error(err?.msg || err?.message || "保存失败");
    } finally {
      leaderSaving.value = false;
    }
  });
}

function goToProfileCenter() {
  router.push("/student-center/profile");
}

function openEditMemberDialog(member, index) {
  editMemberForm.value = {
    name: member.name || "",
    studentNo: member.studentNo || "",
    college: member.college || "",
    major: member.major || "",
    phone: member.phone || "",
    email: member.email || "",
  };
  editMemberIndex.value = index;
  isMemberEditMode.value = true;
  editMemberDialogVisible.value = true;
}

function openViewMemberDialog(member, index) {
  editMemberForm.value = {
    name: member.name || "",
    studentNo: member.studentNo || "",
    college: member.college || "",
    major: member.major || "",
    phone: member.phone || "",
    email: member.email || "",
  };
  editMemberIndex.value = index;
  isMemberEditMode.value = false;
  editMemberDialogVisible.value = true;
}

function confirmEditMember() {
  if (!isMemberEditMode.value) {
    editMemberDialogVisible.value = false;
    return;
  }
  
  editMemberFormRef.value.validate(valid => {
    if (!valid) return;

    if (editMemberIndex.value < 0) {
      ElMessage.warning("未找到要编辑的队员");
      return;
    }
    if (!userStatus.value.teamId) {
      ElMessage.warning("未找到队伍信息");
      return;
    }
    const allMembers = [...(userStatus.value.allMembers || [])];
    const memberIndexes = [];
    allMembers.forEach((m, i) => {
      if (String(m.role || "").toLowerCase() !== "leader") {
        memberIndexes.push(i);
      }
    });
    const targetIndex = memberIndexes[editMemberIndex.value];
    if (targetIndex === undefined) {
      ElMessage.warning("未找到要编辑的队员");
      return;
    }
    allMembers[targetIndex] = {
      ...allMembers[targetIndex],
      ...editMemberForm.value,
    };
    const payload = {
      competitionId: competitionId,
      teamId: userStatus.value.teamId,
      teamName: userStatus.value.teamName,
      teamMembersJson: JSON.stringify(allMembers),
    };
    registerCompetition(payload)
      .then(() => {
        ElMessage.success("队员信息更新成功");
        editMemberDialogVisible.value = false;
        refreshStatus();
      })
      .catch((err) => {
        ElMessage.error(err?.message || "队员信息更新失败");
      });
  });
}

// 打开添加队员对话框（报名阶段）
const addMemberDialogVisibleRegister = ref(false);
const newMemberRegister = ref({
  name: "",
  studentNo: "",
  college: "",
  major: "",
  phone: "",
  email: "",
  teacherName: "",
});

function openAddMemberDialogRegister() {
  newMemberRegister.value = { name: "", studentNo: "", college: "", major: "", phone: "", email: "" };
  addMemberDialogVisibleRegister.value = true;
}

function confirmAddMemberRegister() {
  console.log("开始添加队员");
  console.log("newMemberRegisterRef:", newMemberRegisterRef.value);
  console.log("newMemberRegister:", newMemberRegister.value);
  
  newMemberRegisterRef.value.validate(valid => {
    console.log("表单验证结果:", valid);
    if (!valid) {
      console.log("表单验证失败");
      return;
    }
    
    try {
      console.log("添加队员到registerForm:", newMemberRegister.value);
      registerForm.value.members.push({
        ...newMemberRegister.value,
        role: "member",
        status: "approved",
      });
      console.log("添加队员成功，关闭弹框");
      addMemberDialogVisibleRegister.value = false;
      ElMessage.success("添加队员成功");
      persistDraftRegistration({ silent: true });
    } catch (error) {
      console.error("添加队员失败:", error);
      ElMessage.error("添加队员失败，请重试");
      // 即使失败也要关闭弹框
      addMemberDialogVisibleRegister.value = false;
    }
  });
}

async function ensureDraftTeamRecord(options = {}) {
  const name = (registerForm.value.teamName || "").trim();
  if (!name) {
    return false;
  }
  if (userStatus.value.teamId) {
    return true;
  }
  const response = await saveCompetitionTeamName({
    competitionId: competitionId,
    teamId: null,
    teamName: name,
  });
  const register = response?.data || response;
  if (register?.registerId) {
    materialForm.value.registerId = register.registerId;
  }
  if (register?.teamId) {
    userStatus.value.teamId = register.teamId;
  }
  userStatus.value.teamName = name;
  registerForm.value.teamName = name;
  createTeamNameSaved.value = true;
  createTeamNameEditing.value = false;
  if (!options.silent) {
    ElMessage.success("草稿已保存");
  }
  return true;
}

async function sendDraftInvite(studentNo, options = {}) {
  const sno = String(studentNo || "").trim();
  if (!sno) {
    ElMessage.warning("请填写队员学号");
    return false;
  }
  const canPersist = await ensureDraftTeamRecord({ silent: true });
  if (!canPersist || !userStatus.value.teamId) {
    ElMessage.error("请先保存队伍名称");
    return false;
  }
  try {
    await inviteTeamMember(userStatus.value.teamId, sno);
    await refreshStatus();
    if (!options.silent) {
      ElMessage.success("已发送邀请，等待对方同意");
    }
    return true;
  } catch (error) {
    if (!options.silent) {
      ElMessage.error(error?.message || error?.msg || "邀请失败");
    }
    return false;
  }
}

async function persistDraftRegistration(options = {}) {
  if (registered.value || registerForm.value.registerType !== "create") {
    return false;
  }
  const canPersist = await ensureDraftTeamRecord({ silent: true });
  if (!canPersist || !userStatus.value.teamId) {
    return false;
  }
  const allMembers = [
    { ...registerForm.value.leader, role: "leader", status: "approved" },
    ...registerForm.value.members.map((m) => ({ ...m, role: "member", status: m.status || "approved" })),
  ];
  const payload = {
    competitionId: competitionId,
    teamId: userStatus.value.teamId,
    teamName: registerForm.value.teamName,
    workName: materialForm.value.workName,
    workDescription: materialForm.value.workDescription,
    teamMembersJson: JSON.stringify(buildTeamMembersPayload(allMembers)),
    teacherList: registerForm.value.teachers.map((t) => ({ ...t })),
  };
  try {
    await registerCompetition(payload);
    await refreshStatus();
    if (!options.silent) {
      ElMessage.success("草稿已更新");
    }
    return true;
  } catch (error) {
    if (!options.silent) {
      ElMessage.error(error?.message || "草稿保存失败");
    }
    return false;
  }
}

function submitRegister(options = {}) {
  return new Promise((resolve, reject) => {
    // 表单校验
    if (registerForm.value.registerType === "create") {
      if (!registerForm.value.teamName) {
        ElMessage.warning("请填写队伍名称");
        reject(new Error("请填写队伍名称"));
        return;
      }
      // 校验队长信息
      const leader = registerForm.value.leader;
      if (!leader.name || !leader.studentNo || !leader.college || !leader.major || !leader.phone || !leader.email) {
        ElMessage.warning("请补充队长信息");
        reject(new Error("请补充队长信息"));
        return;
      }
      if (!/^\d{12}$/.test(leader.studentNo)) {
        ElMessage.warning("队长学号必须为12位数字");
        reject(new Error("队长学号必须为12位数字"));
        return;
      }
      if (!/^1[3-9]\d{9}$/.test(leader.phone)) {
        ElMessage.warning("队长手机号格式不正确");
        reject(new Error("队长手机号格式不正确"));
        return;
      }
      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(leader.email)) {
        ElMessage.warning("队长邮箱格式不正确");
        reject(new Error("队长邮箱格式不正确"));
        return;
      }
      if (!Array.isArray(registerForm.value.members) || registerForm.value.members.length < 1) {
        ElMessage.warning("请至少添加一位队员");
        reject(new Error("请至少添加一位队员"));
        return;
      }
      const hasEmpty = registerForm.value.members.some(
        (m) => !m.name || !m.studentNo || !m.college || !m.major || !m.phone || !m.email,
      );
      if (hasEmpty) {
        ElMessage.warning("请完整填写所有成员信息");
        reject(new Error("请完整填写所有成员信息"));
        return;
      }
      if (!hasTeacherInfo.value) {
        ElMessage.warning("请填写指导老师信息");
        reject(new Error("请填写指导老师信息"));
        return;
      }
    } else {
      if (!registerForm.value.selectedTeamId) {
        ElMessage.warning("请选择要加入的队伍");
        reject(new Error("请选择要加入的队伍"));
        return;
      }
    }

    registerLoading.value = true;

    if (registerForm.value.registerType === "join") {
      submitJoinApplication()
        .then(resolve)
        .catch(reject);
      return;
    }

    // 构建包含队长的成员列表
    const allMembers = [
      { ...registerForm.value.leader, role: 'leader' },
      ...registerForm.value.members.map(m => ({ ...m, role: 'member' }))
    ];

    const payload = {
      competitionId: competitionId,
      teamId: userStatus.value.teamId || null,
      teamName: registerForm.value.teamName,
      workName: materialForm.value.workName,
      workDescription: materialForm.value.workDescription,
      teamMembersJson: JSON.stringify(
        buildTeamMembersPayload(allMembers),
      ),
      teacherList: registerForm.value.teachers.map((t) => ({ ...t })),
    };
    registerCompetition(payload)
      .then(async (response) => {
        await refreshStatus();
        const isDraftRecord = userStatus.value.recordStatus === "draft";
        const successMessage = isDraftRecord
          ? "草稿已保存，待所有成员确认后可提交资料"
          : "报名信息已保存，可继续上传资料";
        ElMessage.success(successMessage);
        resolve(response?.data || response);
      })
      .catch((err) => {
        ElMessage.error(err.message || "报名失败");
        reject(err);
      })
      .finally(() => {
        registerLoading.value = false;
      });
  });
}

function refreshStatus() {
  const rawTeamId = route.query.teamId;
  const parsedTeamId = rawTeamId === undefined || rawTeamId === null || rawTeamId === ""
    ? null
    : Number(rawTeamId);
  const statusParams = Number.isFinite(parsedTeamId) ? { teamId: parsedTeamId } : {};
  getCompetitionStatus(competitionId, statusParams).then(async (res) => {
    const data = res.data || {};
    registered.value = !!data.registered;
    userStatus.value.registered = registered.value;
    userStatus.value.hasRecord = !!data.hasRecord || !!data.registerId;
    userStatus.value.draft = !!data.draft;
    userStatus.value.complete = !!data.complete;
    userStatus.value.recordStatus = data.recordStatus || "";
    userStatus.value.recordStatusText = data.recordStatusText || "";
    userStatus.value.reviewStatusCode = data.reviewStatusCode || "";
    userStatus.value.reviewStatusText = data.reviewStatusText || "";
    userStatus.value.teamName = data.teamName || "";
    userStatus.value.isLeader = !!data.isLeader;
    userStatus.value.canCancel = !!data.canCancel;
    userStatus.value.displayRegisterId = data.displayRegisterId || data.registerId || "";
    userStatus.value.teamId = data.teamId || null;
    userStatus.value.missingFields = Array.isArray(data.missingFields) ? data.missingFields : [];
    displayRegisterId.value = userStatus.value.displayRegisterId;
    if (!registered.value && data.teamName) {
      registerForm.value.teamName = data.teamName;
      createTeamNameSaved.value = true;
      createTeamNameEditing.value = false;
    }

    // 确保拿到本人学号，用于后续识别“我本人”
    console.log("Competition Status Data:", data);

    const profile = await loadCurrentProfile();

    // 解析队伍成员
    let members = [];
    try {
      members = data.teamMembers ? JSON.parse(data.teamMembers) : [];
      if (!Array.isArray(members)) members = [];
    } catch (e) {
      members = [];
    }

    const profileStudentNo = String(profile.studentNo || myStudentNo.value || "");
    const profileUserId = String(profile.userId || userStore.id || "");
    const profileName = String(
      profile.studentName || profile.name || profile.nickName || "",
    ).trim();
    members = members.map((member) => {
      const isCurrentUser =
        (profileStudentNo &&
          String(member.studentNo || "") === profileStudentNo) ||
        (profileUserId && String(member.userId || "") === profileUserId) ||
        (profileName && String(member.name || "").trim() === profileName);
      if (!isCurrentUser) {
        return member;
      }
      // 仅在字段为空时用 profile 数据补充，保留手动编辑的值
      const merged = { ...member };
      if (!String(merged.name || "").trim()) {
        merged.name = profile.studentName || profile.name || profile.nickName || "";
      }
      if (!String(merged.college || "").trim()) {
        merged.college =
          profile.dept?.deptName ||
          profile.collegeName ||
          profile.college ||
          "";
      }
      if (!String(merged.major || "").trim()) {
        merged.major = profile.majorName || profile.major || "";
      }
      if (!String(merged.phone || "").trim()) {
        merged.phone = profile.phonenumber || profile.phone || "";
      }
      if (!String(merged.email || "").trim()) {
        merged.email = profile.email || "";
      }
      return merged;
    });

    registerForm.value.leader = {
      ...registerForm.value.leader,
      name:
        profile.studentName ||
        profile.name ||
        profile.nickName ||
        registerForm.value.leader.name,
      studentNo: profile.studentNo || registerForm.value.leader.studentNo,
      college:
        profile.dept?.deptName ||
        profile.collegeName ||
        profile.college ||
        registerForm.value.leader.college,
      major: profile.majorName || profile.major || registerForm.value.leader.major,
      phone:
        profile.phonenumber || profile.phone || registerForm.value.leader.phone,
      email: profile.email || registerForm.value.leader.email,
    };

    userStatus.value.allMembers = members;
    userStatus.value.teamMembers = members.filter((m) => {
      // 保留已通过和待处理的成员
      return isApprovedMemberStatus(m.status) || isPendingMemberStatus(m.status);
    });

    teacherList.value = Array.isArray(data.teacherList) ? data.teacherList : [];
    registerForm.value.teachers = Array.isArray(data.teacherList)
      ? data.teacherList.map((item) => ({ ...item }))
      : [];
    registerForm.value.members = members
      .filter((m) => String(m.role || "").toLowerCase() !== "leader")
      .map((m) => ({
        name: m.name || "",
        studentNo: m.studentNo || "",
        college: m.college || "",
        major: m.major || "",
        phone: m.phone || "",
        email: m.email || "",
        role: m.role || "member",
        status: m.status || "approved",
      }));
    materialForm.value.workName = data.workName || "";
    materialForm.value.workDescription = data.workDescription || "";

    // 批量同步头像逻辑... (保持不变，但需要确保不要覆盖老师信息)
    try {
      const nos = Array.from(
        new Set(members.map((m) => m.studentNo).filter(Boolean)),
      );
      const ids = Array.from(
        new Set(
          members
            .map((m) => Number(m.userId))
            .filter((n) => !isNaN(n) && n > 0),
        ),
      );
      const names = Array.from(
        new Set(members.map((m) => (m.name || "").trim()).filter(Boolean)),
      );

      const [mapByNo, mapById, mapByName] = await Promise.all([
        nos.length
          ? getStudentAvatars(nos).then((r) => r.data || {})
          : Promise.resolve({}),
        ids.length
          ? getUserAvatarsByIds(ids).then((r) => r.data || {})
          : Promise.resolve({}),
        names.length
          ? getStudentAvatarsByNames(names).then((r) => r.data || {})
          : Promise.resolve({}),
      ]);

      let anyChanged = false;
      const ts = Date.now();
      members.forEach((m) => {
        let rawPath =
          mapByNo[m.studentNo] || mapById[m.userId] || mapByName[m.name];
        if (!rawPath) return;
        const fullUrl =
          (rawPath.startsWith("http") ? rawPath : `${apiBase}${rawPath}`) +
          `?__t=${ts}`;
        const oldBase = (m.avatarUrl || "").split("?")[0];
        const newBase = fullUrl.split("?")[0];
        if (oldBase !== newBase) {
          m.avatarUrl = fullUrl;
          anyChanged = true;
        } else {
          m.avatarUrl = fullUrl;
        }
      });

      // 头像仅用于前端展示，避免异步刷新时把旧成员列表回写到后端。
    } catch (err) {
      console.error("头像同步失败:", err);
    }

    materialForm.value.registerId = data.registerId || "";
    if (displayRegisterId.value) {
      lastSubmitTime.value = "";
      participationStatus.value = PARTICIPATION_STATUS.NOT_SUBMITTED;
      getUploadedMaterials(displayRegisterId.value).then((mres) => {
        const d = mres.data || {};
        const pdfVal = d.pdfPath || "";
        const hasServerMaterials = !!(pdfVal || d.pptPath);
        const shouldKeepLocalUploads =
          !hasServerMaterials && hasAnyFiles.value && hasUnsavedChanges.value;

        if (shouldKeepLocalUploads) {
          lastSubmitTime.value = "";
          participationStatus.value = d.participationStatus || PARTICIPATION_STATUS.NOT_SUBMITTED;
          return;
        }

        lastSubmittedPaths.value.workName = (materialForm.value.workName || "").trim();
        lastSubmittedPaths.value.workDescription = (materialForm.value.workDescription || "").trim();

        if (pdfVal) {
          const parts = pdfVal.split("|");
          // 假设后端返回的 pdfNames 也是以 | 分隔的字符串
          const nameParts = (d.pdfNames || "").split("|");

          // 第一部分是 doc
          if (parts[0]) {
            const name = nameParts[0] || parts[0].split("/").pop();
            materialForm.value.docPath = [{ name: name, url: parts[0] }];
            materialForm.value.docFiles = [{ name: name, url: parts[0] }];
            lastSubmittedPaths.value.docPath = parts[0];
          } else {
            lastSubmittedPaths.value.docPath = "";
          }

          // 第二部分是 other
          if (parts[1]) {
            const name = nameParts[1] || parts[1].split("/").pop();
            materialForm.value.otherPath = [{ name: name, url: parts[1] }];
            materialForm.value.otherFiles = [{ name: name, url: parts[1] }];
            lastSubmittedPaths.value.otherPath = parts[1];
          } else {
            lastSubmittedPaths.value.otherPath = "";
          }
        } else {
          lastSubmittedPaths.value.docPath = "";
          lastSubmittedPaths.value.otherPath = "";
        }

        // 澶勭悊 PPT
        materialForm.value.pptPath = d.pptPath || "";
        if (d.pptPath) {
          const name = d.pptName || d.pptPath.split("/").pop();
          materialForm.value.pptFiles = [{ name: name, url: d.pptPath }];
          materialForm.value.pptPath = [{ name: name, url: d.pptPath }];
          lastSubmittedPaths.value.pptPath = d.pptPath;
        } else {
          materialForm.value.pptFiles = [];
          lastSubmittedPaths.value.pptPath = "";
        }

        // 只有当至少有一个文件路径非空时，才显示“已提交”状态
        if (
          materialForm.value.docPath ||
          materialForm.value.pptPath ||
          materialForm.value.otherPath
        ) {
          lastSubmitTime.value = d.submitTime || "";
          participationStatus.value = d.participationStatus || "";
        } else {
          lastSubmitTime.value = "";
          participationStatus.value = PARTICIPATION_STATUS.NOT_SUBMITTED;
        }
      });
    } else {
      lastSubmitTime.value = "";
      participationStatus.value = PARTICIPATION_STATUS.NOT_SUBMITTED;
    }
  });
}

function submitJoinApplication() {
  return new Promise((resolve, reject) => {
    if (!registerForm.value.selectedTeamId) {
      ElMessage.warning("请选择要加入的队伍");
      reject(new Error("请选择要加入的队伍"));
      return;
    }
    if (!isCurrentProfileComplete.value) {
      ElMessage.warning("请先到个人中心完善个人信息后再申请加入队伍");
      reject(new Error("请先完善个人信息"));
      return;
    }
    registerLoading.value = true;
    applyJoinTeam(registerForm.value.selectedTeamId)
      .then(() => {
        ElMessage.success("已提交申请，待队长审批");
        refreshStatus();
        resolve();
      })
      .catch((err) => {
        ElMessage.error(err.message || "申请失败");
        reject(err);
      })
      .finally(() => {
        registerLoading.value = false;
      });
  });
}

async function handleUploadMaterial() {
  if (registerLoading.value || materialLoading.value) {
    return;
  }
  submissionValidationTriggered.value = true;
  if (summaryMissingFields.value.includes("队长信息")) {
    ElMessage.warning("请补充队长信息");
    return;
  }
  if (summaryMissingFields.value.length) {
    ElMessage.warning(`请先完善报名信息：${summaryMissingFields.value.join("、")}`);
    return;
  }
  
  // 先创建队伍
  try {
    const register = await submitRegister({ refresh: false });
    if (register?.registerId) {
      materialForm.value.registerId = register.registerId;
    }
    teacherList.value = registerForm.value.teachers.map((t) => ({ ...t }));
    registered.value = true;
    userStatus.value.registered = true;
    if (register?.teamId) {
      userStatus.value.teamId = register.teamId;
    }
    if (register?.teamName) {
      userStatus.value.teamName = register.teamName;
    }
    // 滚动到资料上传区域
    const materialSection = document.querySelector('.material-upload-section');
    if (materialSection) {
      materialSection.scrollIntoView({ behavior: 'smooth' });
    }
    // 上传资料
    submitMaterial();
  } catch (error) {
    console.error('创建队伍失败:', error);
  }
}

function submitMaterial() {
  if (materialLoading.value) {
    return;
  }
  if (isReadOnlyMemberView.value) {
    canManageTeamMembers();
    return;
  }
  submissionValidationTriggered.value = true;
  if (summaryMissingFields.value.includes("队长信息")) {
    ElMessage.warning("请补充队长信息");
    return;
  }
  if (needsTeacherValidation.value && !hasTeacherInfo.value) {
    ElMessage.warning("请先完善指导老师信息");
    return;
  }
  if (!hasWorkInfo.value) {
    ElMessage.warning("请填写作品名称和作品简介");
    return;
  }
  if (!hasRequiredMaterialFiles.value) {
    ElMessage.warning("请上传申报书资料和PPT演示资料后再提交");
    return;
  }
  if (!materialForm.value.registerId) {
    ElMessage.warning("请先完成报名");
    return;
  }

  const hasFiles =
    materialForm.value.docPath ||
    materialForm.value.pptPath ||
    materialForm.value.otherPath;

  materialLoading.value = true;

  // 构造文件信息 JSON
  const filesInfo = {
    ppt:
      materialForm.value.pptFiles && materialForm.value.pptFiles.length
        ? materialForm.value.pptFiles[0].name
        : "",
    doc:
      materialForm.value.docFiles && materialForm.value.docFiles.length
        ? materialForm.value.docFiles[0].name
        : "",
    other:
      materialForm.value.otherFiles && materialForm.value.otherFiles.length
        ? materialForm.value.otherFiles[0].name
        : "",
  };

  const pptPathStr = getPathString(materialForm.value.pptPath);
  const docPathStr = getPathString(materialForm.value.docPath);
  const otherPathStr = getPathString(materialForm.value.otherPath);

  // 将申报书和其他资料合并存入 pdfPath，PPT 存入 pptPath
  // Ensure we don't send just "|" if both are empty, though backend handles it
  let combinedPdfPath = "";
  if (docPathStr || otherPathStr) {
    combinedPdfPath = `${docPathStr}|${otherPathStr}`;
  }

  submitMaterials({
    registerId: materialForm.value.registerId,
    workName: materialForm.value.workName,
    workDescription: materialForm.value.workDescription,
    pptPath: pptPathStr,
    pdfPath: combinedPdfPath,
    fileNames: JSON.stringify(filesInfo),
  })
    .then(() => {
      submissionValidationTriggered.value = false;
      lastSubmittedPaths.value.docPath = docPathStr;
      lastSubmittedPaths.value.pptPath = pptPathStr;
      lastSubmittedPaths.value.otherPath = otherPathStr;
      lastSubmittedPaths.value.workName = (materialForm.value.workName || "").trim();
      lastSubmittedPaths.value.workDescription = (materialForm.value.workDescription || "").trim();
      participationStatus.value = PARTICIPATION_STATUS.SUBMITTED;
      ElMessage.success(hasFiles ? "提交成功" : "已清空并保存状态");
      if (!hasFiles) {
        lastSubmitTime.value = "";
        participationStatus.value = PARTICIPATION_STATUS.NOT_SUBMITTED;
      }
      refreshStatus();
    })
    .catch(() => {
      ElMessage.error("提交失败");
    })
    .finally(() => {
      materialLoading.value = false;
    });
}

function viewUrl(path) {
  if (!path) return "#";
  const realPath = path.includes("|") ? path.split("|")[0] : path;
  if (!realPath) return "#";
  return proxyFileUrl(realPath);
}

function getFileName(path) {
  if (!path) return "未知文件";
  // 如果是合并路径，取第一个
  const realPath = path.includes("|") ? path.split("|")[0] : path;
  if (!realPath) return "未知文件";
  let filename = realPath.split("/").pop();

  // 深度清洗：移除后端自动添加的各种特征后缀 (时间戳、UUID、随机串)
  // 支持格式：文件名_20260306.docx, 文件名_a1b2c3d4.pdf 等
  const dotIndex = filename.lastIndexOf(".");
  if (dotIndex !== -1) {
    let namePart = filename.substring(0, dotIndex);
    const extPart = filename.substring(dotIndex);

    // 1. 匹配 _ 或 - 后跟 8位以上数字或字母组合
    namePart = namePart.replace(/[_-][a-zA-Z0-9]{8,}$/, "");

    // 2. 特殊处理：如果清洗后文件名为空（说明原名就是纯随机串），则保留原名
    filename = (namePart || filename.substring(0, dotIndex)) + extPart;
  }

  return filename;
}

function getSafePath(val) {
  if (!val) return "";
  if (typeof val === "string")
    return val.includes("|") ? val.split("|")[0] : val;
  if (Array.isArray(val) && val.length > 0) {
    const item = val[0];
    return typeof item === "object"
      ? item.url || item.path || ""
      : String(item);
  }
  if (typeof val === "object" && val !== null) return val.url || val.path || "";
  return String(val);
}

function getSafeName(val, fallbackPath) {
  const files = Array.isArray(val) ? val : [];
  if (files.length > 0 && files[0].name) {
    return files[0].name;
  }
  return getFileName(getSafePath(fallbackPath));
}

function getFileTypeClass(val, files) {
  // 优先从文件对象中获取原始名称来判断类型
  const fileList = Array.isArray(files) ? files : [];
  let name = "";
  if (fileList.length > 0 && fileList[0].name) {
    name = fileList[0].name;
  } else {
    name = getSafePath(val);
  }

  if (!name) return "default";

  // 关键修复：移除 URL 中的查询参数（如 ?token=...），再提取后缀
  const cleanName = name.split("?")[0];
  const parts = cleanName.split(".");
  const ext = parts.length > 1 ? parts.pop().toLowerCase() : "";

  if (["doc", "docx"].includes(ext)) return "word";
  if (["ppt", "pptx"].includes(ext)) return "ppt";
  if (ext === "pdf") return "pdf";
  if (["xls", "xlsx"].includes(ext)) return "excel";
  if (["zip", "rar", "7z"].includes(ext)) return "archive";
  if (["jpg", "jpeg", "png", "gif"].includes(ext)) return "image";
  return "default";
}

function getFileIcon(val, files) {
  const fileList = Array.isArray(files) ? files : [];
  let name = "";
  if (fileList.length > 0 && fileList[0].name) {
    name = fileList[0].name;
  } else {
    name = getSafePath(val);
  }

  // 关键修复：移除 URL 中的查询参数，再提取后缀
  const cleanName = name.split("?")[0];
  const ext = cleanName.split(".").pop().toLowerCase();

  if (["doc", "docx"].includes(ext)) return "fas fa-file-word";
  if (["ppt", "pptx"].includes(ext)) return "fas fa-file-powerpoint";
  if (ext === "pdf") return "fas fa-file-pdf";
  if (["xls", "xlsx"].includes(ext)) return "fas fa-file-excel";
  if (["zip", "rar", "7z"].includes(ext)) return "fas fa-file-archive";
  if (["jpg", "jpeg", "png", "gif"].includes(ext)) return "fas fa-file-image";
  return "fas fa-file-alt";
}

function downloadFile(path, originalFiles) {
  if (!path) return;
  const realPath = path.includes("|") ? path.split("|")[0] : path;
  let url = proxyFileUrl(realPath, { download: true });
  const filename = getSafeName(originalFiles, path);

  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  link.style.display = "none";
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

function confirmRemoveFile(field) {
  ElMessageBox.confirm(
    "确定要移除该文件吗？移除后需重新上传或点击更新。",
    "提示",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    },
  )
    .then(() => {
      materialForm.value[field] = "";
      ElMessage.success("文件已移除");
    })
    .catch(() => {});
}

function sendInvite() {
  if (!userStatus.value.teamId) {
    ElMessage.warning("未找到队伍信息");
    return;
  }
  const sno = (inviteStudentNo.value || "").trim();
  if (!sno) {
    ElMessage.warning("请填写学号");
    return;
  }
  inviteTeamMember(userStatus.value.teamId, sno)
    .then((response) => {
      ElMessage.success("已发送邀请，等待对方同意");
      inviteStudentNo.value = "";
      
      // 将邀请的队员添加到列表中，状态为待同意
      if (response && response.data) {
        const invitedMember = response.data;
        const newMember = {
          name: invitedMember.name || "待确认",
          studentNo: sno,
          role: "member",
          status: "pending",
          college: invitedMember.college || "",
          major: invitedMember.major || "",
          phone: invitedMember.phone || "",
          userId: invitedMember.userId || null,
          avatarUrl: invitedMember.avatarUrl || ""
        };
        
        // 添加到队伍成员列表
        if (userStatus.value.teamMembers) {
          userStatus.value.teamMembers.push(newMember);
        }
      }
    })
    .catch((e) => {
      const msg = (e && e.msg) || (e && e.message) || "邀请失败";
      ElMessage.error(msg);
    });
}

function removeMemberFromTeam(studentNo) {
  if (!userStatus.value.teamId) {
    ElMessage.warning("未找到队伍信息");
    return;
  }
  if (!studentNo) {
    ElMessage.warning("缺少队员学号");
    return;
  }
  ElMessageBox.confirm("确定移除该队员吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      return removeTeamMember(userStatus.value.teamId, studentNo);
    })
    .then(() => {
      ElMessage.success("已移除队员");
      refreshStatus();
    })
    .catch(() => {});
}

function cancelMyRegistration() {
  if (isReadOnlyMemberView.value || !userStatus.value.canCancel) {
    canManageTeamMembers();
    return;
  }
  if (!materialForm.value.registerId) {
    ElMessage.warning("未找到报名记录");
    return;
  }
  ElMessageBox.confirm("确认取消该竞赛的报名？", "提示", { type: "warning" })
    .then(() => cancelRegistration(materialForm.value.registerId))
    .then(() => {
      ElMessage.success("已取消报名");
      registered.value = false;
      inviteStudentNo.value = "";
      materialForm.value = {
        registerId: "",
        workName: "",
        workDescription: "",
        docPath: "",
        pptPath: "",
        otherPath: "",
      };
      refreshStatus();
    })
    .catch(() => {});
}

function saveAdvisor() {
  if (isReadOnlyMemberView.value) {
    canManageTeamMembers();
    return Promise.reject(new Error("当前不可编辑指导老师"));
  }
  if (!userStatus.value.teamId) {
    ElMessage.warning("未找到队伍信息");
    return Promise.reject(new Error("未找到队伍信息"));
  }
  if (!ensureLeaderInfoReadyForTeacher()) {
    return Promise.reject(new Error("请先完善队长信息后再添加指导老师"));
  }
  if (!teacherList.value || !teacherList.value.length) {
    ElMessage.warning("请至少添加一位指导老师");
    return Promise.reject(new Error("请至少添加一位指导老师"));
  }
  const payload = {
    competitionId: competitionId,
    teamId: userStatus.value.teamId,
    teacherList: teacherList.value.map((t) => ({ ...t })),
  };
  return registerCompetition(payload)
    .then(() => {
      ElMessage.success("指导老师保存成功");
      return refreshStatus();
    })
    .catch((err) => {
      ElMessage.error(err?.message || "保存失败");
      throw err;
    });
}

function editTeacher(i) {
  if (isReadOnlyMemberView.value) {
    canManageTeamMembers();
    return;
  }
  const t = teacherList.value[i];
  applyTeacherDetails(t);
  teacherSearchForm.phone = t.phone || "";
  teacherSearchForm.name = t.name || "";
  teacherSearchResults.value = [];
  teacherSearchState.value = "single";
  teacherSearchMessage.value = "";
  editTeacherIndex.value = i;
  isEditMode.value = true;
  showTeacherDialog.value = true;
}

// 未报名状态下查看指导老师信息
function viewTeacherRegister(i) {
  const t = registerForm.teachers[i];
  applyTeacherDetailsRegister(t);
  teacherSearchFormRegister.phone = t.phone || "";
  teacherSearchFormRegister.name = t.name || "";
  teacherSearchResultsRegister.value = [];
  teacherSearchStateRegister.value = "single";
  teacherSearchMessageRegister.value = "";
  currentEditTeacherIndex.value = i;
  isEditModeRegister.value = false;
  showTeacherDialogRegister.value = true;
}

// 未报名状态下编辑指导老师信息
function editTeacherRegister(i) {
  const t = registerForm.teachers[i];
  applyTeacherDetailsRegister(t);
  teacherSearchFormRegister.phone = t.phone || "";
  teacherSearchFormRegister.name = t.name || "";
  teacherSearchResultsRegister.value = [];
  teacherSearchStateRegister.value = "single";
  teacherSearchMessageRegister.value = "";
  currentEditTeacherIndex.value = i;
  isEditModeRegister.value = true;
  showTeacherDialogRegister.value = true;
}

function viewTeacher(i) {
  const t = teacherList.value[i];
  applyTeacherDetails(t);
  teacherSearchForm.phone = t.phone || "";
  teacherSearchForm.name = t.name || "";
  teacherSearchResults.value = [];
  teacherSearchState.value = "single";
  teacherSearchMessage.value = "";
  editTeacherIndex.value = i;
  isEditMode.value = false;
  showTeacherDialog.value = true;
}

function deleteTeacher(i) {
  if (isReadOnlyMemberView.value) {
    canManageTeamMembers();
    return;
  }
  ElMessageBox.confirm("确定要删除这位指导老师吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      teacherList.value.splice(i, 1);
      saveAdvisor();
    })
    .catch(() => {});
}

function openAddTeacher() {
  if (isReadOnlyMemberView.value) {
    canManageTeamMembers();
    return;
  }
  if (!ensureLeaderInfoReadyForTeacher()) {
    return;
  }
  resetTeacherDialog();
  editTeacherIndex.value = null;
  isEditMode.value = true;
  showTeacherDialog.value = true;
}

// 处理手机号输入，自动填充老师信息
function legacyHandlePhoneInput() {
  const phone = tempTeacher.phone.trim();
  if (phone.length === 11 && phone !== lastQueriedPhone.value) {
    lastQueriedPhone.value = phone;
    getTeacherByPhone(phone).then(response => {
      if (response.code === 200 && response.data) {
        const teacherData = response.data;
        const teacherInfo = {
          name: teacherData.nickName || teacherData.userName || "",
          gender: teacherData.sex === "0" ? "男" : teacherData.sex === "1" ? "女" : "",
          phone: teacherData.phonenumber || phone,
          email: teacherData.email || "",
          unit: teacherData.workUnit || teacherData.deptName || (teacherData.dept && teacherData.dept.deptName) || "",
          title: teacherData.title || "",
          position: teacherData.position || "",
          politicalStatus: teacherData.politicalStatus || "",
          birthDate: teacherData.birthDate || ""
        };

        Object.assign(tempTeacher, teacherInfo);
        ElMessage.success("老师信息已自动填充");
      } else {
        ElMessage.warning("未找到该手机号对应的老师信息");
      }
    }).catch(error => {
      console.error("获取老师信息失败:", error);
      ElMessage.error("获取老师信息失败，请检查网络连接");
    });
  } else if (phone.length < 11) {
    lastQueriedPhone.value = "";
  }
}

function handlePhoneInput() {
  return;
}

function resetTeacherDialog() {
  teacherSearchForm.phone = "";
  teacherSearchForm.name = "";
  teacherSearchResults.value = [];
  teacherSearchState.value = "idle";
  teacherSearchMessage.value = "";
  applyTeacherDetails({});
}

function applyTeacherDetails(teacher) {
  tempTeacher.teacherId = teacher.teacherId || "";
  tempTeacher.name = teacher.name || teacher.teacherName || "";
  tempTeacher.gender = teacher.gender || normalizeGenderValue(teacher.sex) || "";
  tempTeacher.birthDate = teacher.birthDate || "";
  tempTeacher.enrollYear = teacher.enrollYear || "";
  tempTeacher.phone = teacher.phone || "";
  tempTeacher.email = teacher.email || "";
  tempTeacher.unit = teacher.unit || teacher.workUnit || "";
  tempTeacher.title = teacher.title || "";
  tempTeacher.position = teacher.position || "";
  tempTeacher.politicalStatus = teacher.politicalStatus || "";
}

function applyTeacherDetailsRegister(teacher) {
  newTeacherRegister.value.teacherId = teacher.teacherId || "";
  newTeacherRegister.value.name = teacher.name || teacher.teacherName || "";
  newTeacherRegister.value.gender = teacher.gender || normalizeGenderValue(teacher.sex) || "";
  newTeacherRegister.value.birthDate = teacher.birthDate || "";
  newTeacherRegister.value.phone = teacher.phone || "";
  newTeacherRegister.value.email = teacher.email || "";
  newTeacherRegister.value.unit = teacher.unit || teacher.workUnit || "";
  newTeacherRegister.value.title = teacher.title || "";
  newTeacherRegister.value.position = teacher.position || "";
  newTeacherRegister.value.politicalStatus = teacher.politicalStatus || "";
}

/* legacy normalize helper retained from old flow
function normalizeGender(sex) {
  if (sex === "0" || sex === 0 || sex === "男") return "男";
  if (sex === "1" || sex === 1 || sex === "女") return "女";
  return "";
}
*/
function normalizeGenderValue(sex) {
  if (sex === "0" || sex === 0) return "男";
  if (sex === "1" || sex === 1) return "女";
  return "";
}

function searchTeacherProfiles() {
  const phone = (teacherSearchForm.phone || "").trim();
  const name = (teacherSearchForm.name || "").trim();
  teacherSearchResults.value = [];
  teacherSearchState.value = "idle";
  teacherSearchMessage.value = "";
  applyTeacherDetails({});
  if (!phone && !name) {
    ElMessage.warning("请输入老师手机号或姓名");
    return;
  }

  if (phone && phone.length !== 11) {
    ElMessage.warning("手机号必须为11位数字");
    return;
  }

  teacherSearchState.value = "searching";

  const params = phone ? { phone } : { name };
  searchTeachers(params)
    .then((response) => {
      const data = response?.data || {};
      const items = Array.isArray(data.items) ? data.items : [];
      teacherSearchResults.value = items;

      if (data.matchType === "single" && items.length === 1) {
        teacherSearchState.value = "single";
        teacherSearchMessage.value = "已匹配到唯一老师，请确认添加";
        applyTeacherDetails(items[0]);
        return;
      }

      if (data.matchType === "multiple" && items.length > 1) {
        teacherSearchState.value = "multiple";
        teacherSearchMessage.value = "存在重复老师信息，请检查后重新输入";
        ElMessage.warning("存在重复老师信息，仅展示，不可选择");
        return;
      }

      teacherSearchState.value = "none";
      teacherSearchMessage.value = "手机号或姓名不存在，请检查后重新输入";
      ElMessage.warning("未找到对应老师，请检查后重新输入");
    })
    .catch((error) => {
      console.error("查询老师失败:", error);
      teacherSearchState.value = "idle";
      teacherSearchMessage.value = "";
      ElMessage.error(error?.message || "查询老师失败");
    });
}

function confirmTeacherEdit() {
  if (isReadOnlyMemberView.value) {
    canManageTeamMembers();
    return;
  }
  if (!ensureLeaderInfoReadyForTeacher()) {
    return;
  }
  if (!tempTeacher.teacherId) {
    ElMessage.warning("请先查询并确认唯一老师");
    return;
  }
  if (!tempTeacher.phone) {
    ElMessage.warning("请输入老师手机号");
    return;
  }

  const newTeacher = { ...tempTeacher };
  const nextTeachers = Array.isArray(teacherList.value)
    ? teacherList.value.map((item) => ({ ...item }))
    : [];
  if (editTeacherIndex.value === null) {
    nextTeachers.push(newTeacher);
  } else {
    nextTeachers[editTeacherIndex.value] = newTeacher;
  }

  showTeacherDialog.value = false;
  const previousTeachers = teacherList.value;
  teacherList.value = nextTeachers;
  saveAdvisor().catch(() => {
    teacherList.value = previousTeachers;
  });
}

function openInviteDialog() {
  if (isReadOnlyMemberView.value) {
    canManageTeamMembers();
    return;
  }
  showInvite.value = true;
}

function openAddMemberDialog() {
  if (isReadOnlyMemberView.value) {
    canManageTeamMembers();
    return;
  }
  // 重置表单
  newMemberForm.name = "";
  newMemberForm.studentNo = "";
  newMemberForm.college = "";
  newMemberForm.major = "";
  newMemberForm.phone = "";
  newMemberForm.email = "";
  showAddMember.value = true;
}

function addMemberDirectly() {
  if (!canManageTeamMembers()) {
    return;
  }
  if (!userStatus.value.teamId) {
    ElMessage.warning("未找到队伍信息");
    return;
  }

  newMemberFormRef.value.validate(valid => {
    if (!valid) return;

    const newMember = {
      name: newMemberForm.name,
      studentNo: newMemberForm.studentNo,
      college: newMemberForm.college,
      major: newMemberForm.major,
      phone: newMemberForm.phone,
      email: newMemberForm.email,
      role: "member",
      status: "approved"
    };

    const currentMembers = [...userStatus.value.allMembers];
    const existingMember = currentMembers.find(m => m.studentNo === newMemberForm.studentNo);
    if (existingMember) {
      ElMessage.warning("该队员已在队伍中");
      return;
    }

    currentMembers.push(newMember);

    const payload = {
      competitionId: competitionId,
      teamId: userStatus.value.teamId,
      teamName: userStatus.value.teamName,
      teamMembersJson: JSON.stringify(currentMembers),
    };

    registerCompetition(payload)
      .then(() => {
        ElMessage.success("队员添加成功");
        showAddMember.value = false;
        refreshStatus();
      })
      .catch((err) => {
        ElMessage.error(err?.message || "添加失败");
      });
  });
}

// 处理用户操作命令
const handleCommand = (command) => {
  if (command === "logout") {
    ElMessageBox.confirm("确定注销并退出系统吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    })
      .then(() => {
        userStore.logOut().then(() => {
          location.href = "/index";
        });
      })
      .catch(() => {});
  }
};

onMounted(async () => {
  await ensureCompMeta();
  await loadCurrentProfile();
  await refreshStatus();
  loadCollegeMajorData();
  const u = currentProfile.value || {};
  if (!registerForm.value.leader.name && !registerForm.value.leader.studentNo && !registerForm.value.leader.major) {
    registerForm.value.leader = {
      name: u.studentName || u.name || u.nickName || "",
      studentNo: u.studentNo || "",
      college: u.dept?.deptName || u.collegeName || u.college || "",
      major: u.majorName || u.major || "",
      phone: u.phonenumber || u.phone || "",
      email: u.email || "",
    };
  }
});

// 编辑队伍名称
function startEditTeamName() {
  if (isReadOnlyMemberView.value) {
    canManageTeamMembers();
    return;
  }
  editTeamNameForm.value.name = userStatus.value.teamName || "";
  editingTeamName.value = true;
}

function cancelEditTeamName() {
  editingTeamName.value = false;
  editTeamNameForm.value.name = "";
}

function saveTeamName() {
  if (isReadOnlyMemberView.value) {
    canManageTeamMembers();
    return;
  }
  const name = (editTeamNameForm.value.name || "").trim();
  if (!name) {
    ElMessage.warning("请输入队伍名称");
    return;
  }
  if (!userStatus.value.teamId) {
    ElMessage.warning("未找到队伍信息");
    return;
  }
  saveCompetitionTeamName({
    competitionId: competitionId,
    teamId: userStatus.value.teamId,
    teamName: name,
  }).then(() => {
    ElMessage.success("队伍名称更新成功");
    editingTeamName.value = false;
    editTeamNameForm.value.name = "";
    refreshStatus();
  }).catch((err) => {
    ElMessage.error(err?.message || "队伍名称更新失败");
  });
}

function saveCreateTeamName() {
  const name = (registerForm.value.teamName || "").trim();
  if (!name) {
    ElMessage.warning("请输入队伍名称");
    return;
  }
  registerLoading.value = true;
  saveCompetitionTeamName({
    competitionId: competitionId,
    teamId: userStatus.value.teamId || null,
    teamName: name,
  }).then((response) => {
    const register = response?.data || response;
    if (register?.registerId) {
      materialForm.value.registerId = register.registerId;
    }
  if (register?.teamId) {
    userStatus.value.teamId = register.teamId;
  }
  userStatus.value.teamName = name;
    registerForm.value.teamName = name;
    createTeamNameSaved.value = true;
    createTeamNameEditing.value = false;
    ElMessage.success("队伍名称已保存");
  }).catch((err) => {
    ElMessage.error(err?.message || "队伍名称保存失败");
  }).finally(() => {
    registerLoading.value = false;
  });
}

function editCreateTeamName() {
  createTeamNameEditing.value = true;
}

function toggleCreateTeamNameAction() {
  if (createTeamNameSaved.value && !createTeamNameEditing.value) {
    editCreateTeamName();
    return;
  }
  saveCreateTeamName();
}
</script>

<style scoped>
/* 椤堕儴瀵艰埅鏍忔牱寮?*/
.header-nav {
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  height: 64px;
  gap: 40px;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
  font-size: 20px;
  font-weight: 600;
}

.logo-text {
  font-size: 18px;
}

.main-nav {
  display: flex;
  align-items: center;
  gap: 32px;
  flex: 1;
}

.nav-item {
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 0;
  position: relative;
  cursor: pointer;
  transition: all 0.3s;
}

.nav-item:hover,
.nav-item.active {
  color: white;
}

.nav-item.active::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: white;
  border-radius: 2px;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-container {
  cursor: pointer;
}

.avatar-wrapper {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
}

.user-avatar-circle {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.page-hero {
  padding: 0;
  margin-bottom: 32px;
  color: #7c4dff;
  background: linear-gradient(180deg, #ffffff 0%, #f8f6ff 100%);
  width: 100%;
  border-top: 1px solid rgba(124, 77, 255, 0.2);
  border-bottom: none;
  border-radius: 0 0 24px 24px;
  position: sticky;
  top: 64px;
  z-index: 90;
  box-shadow:
    0 10px 30px rgba(124, 77, 255, 0.12),
    0 4px 12px rgba(124, 77, 255, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  overflow: hidden;
}

.hero-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px 40px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.hero-main {
  flex: 1;
  padding-top: 10px;
}

.hero-title {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: flex-start;
}

.hero-title h1 {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  color: #7c4dff;
}

.hero-badge {
  background-color: rgba(82, 196, 26, 0.15) !important;
  color: #7c4dff !important;
  border: none;
  font-weight: 600;
}

.hero-desc {
  color: #7c4dff;
  margin: 12px 0 16px 0;
  opacity: 0.92;
  font-size: 14px;
  line-height: 1.6;
}

.hero-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.hero-meta :deep(.el-tag.hero-chip) {
  border-color: rgba(124, 77, 255, 0.3) !important;
  color: #7c4dff !important;
  background-color: rgba(124, 77, 255, 0.08) !important;
  font-weight: 500;
}

.hero-actions {
  display: flex;
  align-items: center;
}

.hero-actions .el-button--primary,
.hero-actions .el-button--success {
  padding: 18px 36px;
  font-size: 18px;
  font-weight: 600;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.hero-actions .el-button--primary {
  background-color: #7c4dff;
  color: #ffffff;
  border-color: #7c4dff;
  box-shadow: 0 4px 12px rgba(124, 77, 255, 0.2);
}

.hero-actions .el-button--success {
  background-color: #34d399;
  color: #ffffff;
  border-color: #34d399;
  box-shadow: 0 4px 12px rgba(52, 211, 153, 0.2);
}

.hero-actions .el-button--primary:hover {
  background-color: #6a3dd8;
  border-color: #6a3dd8;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(124, 77, 255, 0.3);
}

.hero-actions .el-button--success:hover {
  background-color: #10b981;
  border-color: #10b981;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(52, 211, 153, 0.3);
}

.register-page {
  background-color: #f5f7fa;
  min-height: 100vh;
}

.process-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  background: #fff;
  width: 100%;
  max-width: 1400px;
  margin: 0 auto 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  border-radius: 16px;
  border: 1px solid #f1f5f9;
}

.content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  position: relative;
  z-index: 1;
}

.step-num {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f1f5f9;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 16px;
  transition: all 0.4s;
  border: 2px solid #e2e8f0;
}

.step-label {
  font-size: 14px;
  font-weight: 700;
  color: #94a3b8;
  transition: all 0.4s;
}

.step-line {
  width: 100px;
  height: 2px;
  background: #f1f5f9;
  margin: 0 20px;
  margin-top: -24px;
  transition: all 0.4s;
}

.step-line.completed {
  background: #34d399;
}

.step-item.active .step-num {
  background: #8b5cf6;
  color: #fff;
  border-color: #8b5cf6;
  box-shadow: 0 0 15px rgba(139, 92, 246, 0.4);
  transform: scale(1.1);
}

.step-item.active .step-label {
  color: #8b5cf6;
}

.step-item.completed .step-num {
  background: #34d399;
  color: #fff;
  border-color: #34d399;
}

.step-item.completed .step-label {
  color: #059669;
}

/* 璧勬枡涓婁紶鍖哄煙閲嶅 */
.triple-upload-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
  background: #f8fafc;
  padding: 40px 32px;
  border-radius: 32px;
  border: 2px dashed #e2e8f0;
}

.upload-item-card {
  background: #fff;
  padding: 32px 24px;
  border-radius: 24px;
  border: none;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  transition: all 0.3s;
}

.upload-item-card :deep(.el-form-item) {
  margin-bottom: 0;
  width: 100%;
  display: flex;
  justify-content: center;
}

.upload-item-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.08);
  border-color: var(--el-color-primary-light);
}

.upload-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-bottom: 16px;
}

.doc-icon {
  background: #eff6ff;
  color: #2563eb;
}
.ppt-icon {
  background: #fff7ed;
  color: #ea580c;
}
.zip-icon {
  background: #fdf2f8;
  color: #db2777;
}

.upload-label {
  font-size: 16px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 20px;
}

.required-star {
  color: #f56c6c;
  font-weight: 900;
}

.upload-item-card :deep(.el-upload-dragger) {
  width: 100%;
  max-width: 320px; /* 闄愬埗瀹藉害锛岄槻姝㈣繃瀹藉鑷村唴瀹瑰垎鏁?*/
  border-radius: 16px;
  padding: 24px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-item-card :deep(.el-upload) {
  width: 100%;
  display: flex;
  justify-content: center;
}

.upload-item-card :deep(.el-form-item__content) {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  margin-left: 0 !important; /* 强制消除可能的 label 留白 */
}

.preview-action-box {
  margin-top: 12px;
  display: flex;
  justify-content: center;
  width: 100%;
}

.preview-btn {
  font-weight: 700 !important;
  font-size: 13px !important;
  color: var(--el-color-primary) !important;
  background: var(--el-color-primary-light) !important;
  padding: 6px 16px !important;
  border-radius: 10px !important;
  transition: all 0.3s !important;
}

.preview-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.15);
  background: #ede9fe !important;
}

.preview-btn i {
  margin-right: 6px;
}

/* 已上传文件极简列表风格 - 深度还原图片设计 */
.file-minimal-item {
  width: 100%;
  max-width: 420px;
  background: #ffffff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.04);
  border: 1px solid #f1f5f9;
  margin-bottom: 12px;
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
}

.file-minimal-item:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 30px rgba(139, 92, 246, 0.12);
}

.item-main-content {
  display: flex;
  gap: 14px;
  margin-bottom: 12px;
}

.file-icon-solid {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
  flex-shrink: 0;
}

/* 文件图标 3D 质感重构 - 对标图片风格 */
.file-icon-solid {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
  flex-shrink: 0;
  position: relative;
  /* 搴曢儴娣辫壊鎶曞奖鏁堟灉锛岃惀閫?3D 鍧楃姸鎰?*/
  box-shadow: inset 0 -4px 0 rgba(0, 0, 0, 0.15);
}

.file-icon-solid i {
  /* 寰皟鍥炬爣闃村奖澧炲己瀵规瘮搴?*/
  filter: drop-shadow(0 1px 1px rgba(0, 0, 0, 0.1));
}

/* 鍖归厤鍥剧墖涓殑楂橀ケ鍜屽害瀹炶壊 */
.file-icon-solid.word {
  background-color: #2196f3;
} /* Word 浜摑鑹?*/
.file-icon-solid.pdf {
  background-color: #ff4d4f;
} /* PDF 浜孩鑹?*/
.file-icon-solid.ppt {
  background-color: #ff9800;
} /* PPT 浜鑹?*/
.file-icon-solid.excel {
  background-color: #4caf50;
} /* Excel 缁胯壊 */
.file-icon-solid.archive {
  background-color: #795548;
} /* 褰掓。 瑜愯壊 */
.file-icon-solid.image {
  background-color: #9c27b0;
} /* 鍥剧墖 绱壊 */
.file-icon-solid.default {
  background-color: #607d8b;
} /* 榛樿 钃濈伆鑹?*/

.file-details-box {
  flex: 1;
  min-width: 0;
}

.file-name-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 4px;
}

.fname-text {
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  text-align: left;
}

.remove-trigger {
  font-size: 14px;
  color: #94a3b8;
  cursor: pointer;
  margin-left: 10px;
  transition: all 0.2s;
  padding: 2px;
}

.remove-trigger:hover {
  color: #ef4444;
  transform: scale(1.2) rotate(90deg);
}

.file-meta-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.success-state {
  color: #10b981;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-dot {
  color: #cbd5e1;
}

.mini-progress-track {
  width: 100%;
  height: 4px;
  background: #f1f5f9;
  border-radius: 2px;
  overflow: hidden;
}

.mini-progress-fill {
  height: 100%;
  width: 100%;
}

@keyframes progressPulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
  100% {
    opacity: 1;
  }
}

.pulse-active {
  animation: progressPulse 2s infinite ease-in-out;
}

/* 杩涘害鏉￠鑹插悓姝?- 浣跨敤瀹炶壊浠ュ尮閰嶅浘鏍?*/
.mini-progress-fill.word {
  background-color: #2196f3;
}
.mini-progress-fill.pdf {
  background-color: #f44336;
}
.mini-progress-fill.ppt {
  background-color: #ff9800;
}
.mini-progress-fill.excel {
  background-color: #4caf50;
}
.mini-progress-fill.archive {
  background-color: #795548;
}
.mini-progress-fill.image {
  background-color: #9c27b0;
}
.mini-progress-fill.default {
  background-color: #607d8b;
}

.item-actions-footer {
  display: flex;
  gap: 16px;
  justify-content: center;
  padding-top: 12px;
  border-top: 1px solid #f8fafc;
}

.mini-act-btn {
  font-size: 12px !important;
  font-weight: 700 !important;
  padding: 6px 12px !important;
  border-radius: 8px !important;
  color: #64748b !important;
  transition: all 0.2s !important;
}

.mini-act-btn:hover {
  background: #f5f3ff !important;
  color: var(--el-color-primary) !important;
}

.mini-act-btn.delete-btn:hover {
  background: #fff1f0 !important;
  color: #ef4444 !important;
}

.mini-act-btn i {
  margin-right: 4px;
  font-size: 11px;
}

.file-readonly-empty {
  width: 100%;
  max-width: 420px;
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  margin-bottom: 12px;
  border: 1px dashed #d8dee9;
  border-radius: 16px;
  background: #f8fafc;
  color: #64748b;
  text-align: center;
  line-height: 1.6;
}

.help-text {
  font-size: 13px;
  color: #64748b;
  margin-top: 12px;
  text-align: center;
  line-height: 1.5;
}

.accent-tip {
  color: var(--el-color-primary);
  font-weight: 600;
  font-size: 12px;
}

.cancel-registration-zone {
  margin-top: 48px;
  padding: 32px 0 0;
  border-top: 1px dashed #e2e8f0;
  display: flex;
  justify-content: center;
  width: 100%;
}

.cancel-btn-text {
  font-size: 14px;
  color: #e11d48 !important;
  background: #fff1f2 !important;
  border: 1px solid #fecdd3 !important;
  border-radius: 24px !important;
  padding: 10px 32px !important;
  font-weight: 800 !important;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 4px 12px rgba(225, 29, 72, 0.08);
  display: flex;
  align-items: center;
  gap: 10px;
}

.cancel-btn-text:hover {
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 10px 20px rgba(225, 29, 72, 0.15);
  background: #ffe4e6 !important;
  border-color: #fda4af !important;
}

.cancel-btn-text :deep(i) {
  font-size: 16px;
}

/* 寮圭獥楂樼骇缇庡寲 */
:deep(.premium-dialog) {
  border-radius: 28px !important;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25) !important;
  border: 1px solid rgba(255, 255, 255, 0.8);
}

:deep(.premium-dialog .el-dialog__header) {
  margin: 0;
  padding: 24px 32px;
  background: #f8fafc;
  border-bottom: 1px solid #f1f5f9;
}

:deep(.premium-dialog .el-dialog__title) {
  font-weight: 900;
  color: #1e293b;
  font-size: 20px;
  letter-spacing: -0.5px;
}

.dialog-body-scroller {
  padding: 0 32px 32px;
  max-height: 70vh;
  overflow-y: auto;
  background-image:
    radial-gradient(at 0% 0%, rgba(99, 102, 241, 0.03) 0px, transparent 50%),
    radial-gradient(at 100% 100%, rgba(124, 77, 255, 0.03) 0px, transparent 50%);
}

.dialog-header-accent {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px 0;
  margin-bottom: 12px;
  border-bottom: 1px dashed #e2e8f0;
}

.accent-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 100%);
  color: #8b5cf6;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: 0 8px 16px rgba(139, 92, 246, 0.1);
}

/* 鎻愪氦鎸夐挳鍖哄煙灞呬腑涓庨啋鐩編鍖栧凡杩佺Щ鑷抽《閮?Hero 鍖哄煙 */

.accent-text h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  color: #1e293b;
}

.accent-text p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #94a3b8;
}

.form-section-card {
  background: #ffffff;
  border: 1px solid #f1f5f9;
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.teacher-search-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.teacher-search-feedback {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  border-radius: 16px;
  font-size: 14px;
  font-weight: 600;
}

.teacher-search-feedback.is-single {
  background: #effaf3;
  color: #1f7a45;
  border: 1px solid #ccefd8;
}

.teacher-search-feedback.is-multiple,
.teacher-search-feedback.is-none {
  background: #fff7ed;
  color: #b45309;
  border: 1px solid #fed7aa;
}

.teacher-search-result-list {
  margin-top: 16px;
  display: grid;
  gap: 12px;
}

.teacher-search-result-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border-radius: 18px;
  background: linear-gradient(135deg, #ffffff, #f8fafc);
  border: 1px solid #e2e8f0;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
}

.teacher-search-result-item.is-single {
  background: linear-gradient(135deg, #f0fdf4, #ecfeff);
  border-color: #86efac;
  box-shadow: 0 12px 28px rgba(34, 197, 94, 0.12);
}

.teacher-search-result-item.is-disabled {
  opacity: 0.92;
}

.teacher-result-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.teacher-result-name {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
}

.teacher-result-phone {
  font-size: 13px;
  color: #64748b;
  letter-spacing: 0.3px;
}

.teacher-result-badge {
  flex-shrink: 0;
  min-width: 88px;
  height: 32px;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 0;
  line-height: 0;
  font-weight: 700;
  color: transparent !important;
  position: relative;
  overflow: hidden;
  text-indent: -9999px;
  white-space: nowrap;
  box-sizing: border-box;
}

.teacher-result-badge::after {
  content: "不可选";
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  line-height: 1;
  text-indent: 0;
  color: #be123c;
  white-space: nowrap;
}

.teacher-result-badge {
  background: #fff1f2;
}

.teacher-search-result-item.is-single .teacher-result-badge {
  background: #dcfce7;
}

.teacher-search-result-item.is-single .teacher-result-badge::after {
  content: "已匹配";
  color: #15803d;
}

.custom-input :deep(.el-input__wrapper) {
  background-color: #f8fafc !important;
  border: 1px solid #f1f5f9 !important;
  box-shadow: none !important;
  border-radius: 12px !important;
  transition: all 0.3s;
}

.custom-input :deep(.el-input__wrapper:hover) {
  background-color: #f1f5f9 !important;
  border-color: #e2e8f0 !important;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  background-color: #ffffff !important;
  border-color: #8b5cf6 !important;
  box-shadow: 0 0 0 4px rgba(139, 92, 246, 0.1) !important;
}

.icon-fade {
  color: #94a3b8;
  font-size: 14px;
}

.premium-form :deep(.el-form-item__label) {
  font-weight: 800;
  color: #475569;
  font-size: 13px;
  margin-bottom: 8px;
  padding-left: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.premium-radio-group {
  width: 100%;
  display: flex;
  gap: 12px;
}

.premium-radio-group :deep(.el-radio-button__inner) {
  flex: 1;
  border-radius: 12px !important;
  border: 1px solid #f1f5f9 !important;
  background: #f8fafc !important;
  color: #64748b !important;
  box-shadow: none !important;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  transition: all 0.3s;
}

.premium-radio-group
  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: #8b5cf6 !important;
  color: #ffffff !important;
  border-color: #8b5cf6 !important;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3) !important;
}

.dialog-footer-premium {
  padding: 12px 32px 32px;
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  background: #ffffff;
}

.btn-cancel {
  border-radius: 14px !important;
  padding: 10px 24px !important;
  font-weight: 700 !important;
  border: 1px solid #e2e8f0 !important;
}

.btn-confirm {
  border-radius: 14px !important;
  padding: 10px 28px !important;
  font-weight: 800 !important;
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%) !important;
  border: none !important;
  box-shadow: 0 8px 15px rgba(139, 92, 246, 0.25);
}

/* 閭€璇峰脊绐楃壒鏈?*/
.invite-dialog-content {
  text-align: center;
  padding: 20px 0;
}

.invite-icon {
  width: 80px;
  height: 80px;
  background: #f5f3ff;
  color: #8b5cf6;
  border-radius: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  margin: 0 auto 24px;
}

.invite-tip {
  font-size: 15px;
  color: #64748b;
  margin-bottom: 24px;
  font-weight: 500;
}

.invite-input {
  margin-top: 8px;
  width: 100%;
}

.student-no-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
  line-height: 1.4;
  padding-left: 4px;
}

.invite-dialog-content {
  padding: 20px 0;
}

.invite-icon {
  text-align: center;
  font-size: 48px;
  color: #7c4dff;
  margin-bottom: 16px;
}

.invite-tip {
  text-align: center;
  margin-bottom: 24px;
  color: #666;
}

.invite-input :deep(.el-input__wrapper) {
  border-radius: 16px !important;
  height: 48px;
}

.back-btn-premium {
  background: rgba(255, 255, 255, 0.2) !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  color: #fff !important;
  backdrop-filter: blur(8px);
  transition: all 0.3s;
}

.back-btn-premium:hover {
  background: rgba(255, 255, 255, 0.3) !important;
  transform: translateX(-5px);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(520px, 1fr));
  gap: 24px;
}

.info-card {
  width: 100%;
  max-width: 520px;
  height: 200px;
  background: #ffffff;
  border-radius: 24px;
  border: 1px solid #f1f5f9;
  position: relative;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  display: flex;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.info-card:hover {
  transform: translateY(-8px) scale(1.01);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.1);
}

/* 鎸夐挳缇庡寲 */
.actions-line-modern :deep(.el-button--success) {
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 800;
  border-radius: 14px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  box-shadow: 0 8px 20px rgba(16, 185, 129, 0.3);
  transition: all 0.3s;
}

.actions-line-modern :deep(.el-button--success:hover) {
  transform: translateY(-2px);
  box-shadow: 0 12px 25px rgba(16, 185, 129, 0.4);
}

.hero-title {
  display: flex;
  align-items: center;
  gap: 16px;
}

.hero-title i {
  font-size: 24px; /* 鍑忓皬鍥炬爣澶у皬 */
  background: rgba(255, 255, 255, 0.2);
  padding: 8px; /* 鍑忓皬鍐呰竟璺?*/
  border-radius: 12px;
  backdrop-filter: blur(4px);
}

.hero-title h1 {
  font-size: 24px; /* 鍑忓皬鏍囬瀛楀彿 */
  font-weight: 800;
  margin: 0;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.hero-desc {
  margin-top: 6px; /* 鍑忓皬涓庢爣棰樼殑闂磋窛 */
  font-size: 14px; /* 鍑忓皬鎻忚堪鏂囧瓧瀛楀彿 */
  opacity: 0.85;
  max-width: 800px;
  line-height: 1.4;
}

.hero-meta {
  display: flex;
  gap: 12px;
  margin-top: 10px; /* 鍑忓皬鏍囩鏍忎笌涓婃柟鐨勯棿璺?*/
}

.hero-chip {
  background: rgba(255, 255, 255, 0.15) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  color: #fff !important;
  font-weight: 600 !important;
  border-radius: 8px !important;
  padding: 0 12px !important;
  height: 28px !important; /* 鍑忓皬鏍囩楂樺害 */
  line-height: 26px !important;
  font-size: 12px !important;
}

/* 鎻愮ず鏍忕編鍖?*/
.guidance {
  margin-bottom: 24px;
}
.guidance :deep(.el-alert) {
  border-radius: 16px;
  padding: 14px 24px;
  border: 1px solid #e2e8f0;
  background-color: #fff;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.02);
}

/* 鍗＄墖瀹瑰櫒闈㈡澘 */
.panel {
  margin-bottom: 32px;
}
.panel :deep(.el-card) {
  border-radius: 24px;
  border: none;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.04) !important;
  overflow: visible;
}

.panel :deep(.el-card__header) {
  border-bottom: 1px solid #f8fafc;
  padding: 24px 32px;
}

.card-header {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: 800;
  color: var(--section-title-color);
}

.required-hint {
  margin-left: 10px;
  font-size: 12px;
  line-height: 1;
  color: #ef4444;
  font-weight: 600;
}

.card-icon {
  margin-right: 12px;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light);
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

/* 鎸囧鑰佸笀涓庡鐢熷崱鐗?*/
.section-title-wrapper {
  display: flex;
  align-items: center;
  margin: 32px 0 20px;
  padding-left: 8px;
}

.section-icon {
  font-size: 20px;
  color: var(--el-color-primary);
  margin-right: 12px;
}

.section-title {
  font-size: 18px;
  font-weight: 800;
  color: #334155;
  letter-spacing: -0.3px;
}

.upload-fail-btn {
  background: #ef4444 !important;
  border-color: #ef4444 !important;
  color: #fff !important;
  box-shadow: 0 12px 24px rgba(239, 68, 68, 0.24) !important;
}

.upload-fail-btn:hover,
.upload-fail-btn:focus {
  background: #dc2626 !important;
  border-color: #dc2626 !important;
  color: #fff !important;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(520px, 1fr));
  gap: 24px;
}

.info-card {
  width: 100%;
  max-width: 520px;
  height: 200px;
  background: linear-gradient(
    145deg,
    rgba(255, 255, 255, 0.9) 0%,
    rgba(250, 251, 255, 0.95) 100%
  );
  backdrop-filter: blur(10px);
  border-radius: 28px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  position: relative;
  transition: all 0.5s cubic-bezier(0.23, 1, 0.32, 1);
  display: flex;
  overflow: hidden;
  box-shadow:
    0 4px 15px rgba(0, 0, 0, 0.03),
    0 10px 30px rgba(99, 102, 241, 0.04);
}

.info-card:hover {
  transform: translateY(-10px) scale(1.02);
  border-color: rgba(99, 102, 241, 0.2);
  box-shadow:
    0 20px 40px rgba(0, 0, 0, 0.06),
    0 15px 25px rgba(99, 102, 241, 0.08);
}

/* 鎵厜鐗规晥 */
.card-shine {
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(
    to right,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.4) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: skewX(-25deg);
  transition: none;
  pointer-events: none;
  z-index: 5;
}

.info-card:hover .card-shine {
  left: 150%;
  transition: all 0.8s ease-in-out;
}

.clickable-card {
  cursor: pointer;
}

.card-body {
  display: flex;
  align-items: center;
  padding: 24px 32px;
  width: 100%;
  z-index: 2;
}

/* 澶村儚璁捐鍗囩骇 */
.avatar-container-outer {
  margin-right: 28px;
}

.avatar-circle-modern {
  width: 110px;
  height: 110px;
  border-radius: 34px; /* 鏇村姞骞虫粦鐨勬柟鍦?*/
  overflow: hidden;
  background: #fff;
  border: 4px solid #fff;
  box-shadow:
    0 8px 20px rgba(0, 0, 0, 0.05),
    inset 0 0 0 1px rgba(0, 0, 0, 0.02);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
}

.info-card:hover .avatar-circle-modern {
  transform: scale(1.08) rotate(-3deg);
  box-shadow:
    0 15px 30px rgba(99, 102, 241, 0.15),
    0 0 0 4px rgba(99, 102, 241, 0.05);
}

.teacher-avatar {
  background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 100%);
  color: #8b5cf6;
  font-size: 48px;
}

.avatar-img-modern {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 鏂囨湰淇℃伅璁捐鍗囩骇 */
.info-lines-modern {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.name-line {
  font-size: 24px;
  font-weight: 800;
  margin-bottom: 8px;
  letter-spacing: -0.6px;
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  transition: all 0.3s ease;
}

.info-card:hover .name-line {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.detail-line {
  font-size: 14px;
  display: flex;
  align-items: center;
  line-height: 1.4;
}

.detail-line .label {
  color: #64748b;
  font-weight: 500;
  transition: color 0.3s ease;
}

.detail-line .value {
  color: #334155;
  font-weight: 600;
  transition: color 0.3s ease;
}

.info-card:hover .detail-line .label {
  color: #475569;
}

.info-card:hover .detail-line .value {
  color: #1e293b;
}

.icon-accent {
  width: 20px;
  font-size: 14px;
  color: #8b5cf6;
  opacity: 0.8;
  transition: all 0.3s ease;
}

.info-card:hover .icon-accent {
  opacity: 1;
  transform: scale(1.1);
}

.tip-line {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 10px;
  font-style: normal;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: color 0.3s ease;
}

.info-card:hover .tip-line {
  color: #64748b;
}

.tip-line::before {
  content: "•";
  color: #8b5cf6;
}

/* 寰界珷璁捐鍗囩骇 */
.card-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 5px 14px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  z-index: 10;
  transition: all 0.3s;
}

.info-card:hover .card-badge {
  transform: translateY(32px);
  opacity: 0.7;
}

.teacher-badge {
  background: #f5f3ff;
  color: #7c3aed;
  border: 1px solid rgba(124, 58, 237, 0.1);
}
.leader-badge {
  background: #fffbeb;
  color: #b45309;
  border: 1px solid rgba(245, 158, 11, 0.1);
}
.member-badge {
  background: #f8fafc;
  color: #64748b;
  border: 1px solid rgba(148, 163, 184, 0.1);
}

/* 鎮诞鎿嶄綔鎸夐挳鍗囩骇 */
.card-actions-overlay {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  gap: 8px;
  opacity: 1;
  transform: translateY(0);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 20;
}

.member-card-modern .card-actions-overlay {
  top: 48px;
  right: 18px;
}

.info-card:hover .card-actions-overlay {
  opacity: 1;
  transform: translateY(0);
}

.action-btn-mini {
  width: 28px !important;
  height: 28px !important;
  min-height: 28px !important;
  padding: 0 !important;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none !important;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1) !important;
  transition: all 0.2s !important;
}

.action-btn-mini:hover {
  transform: scale(1.15);
}

.action-btn-mini i {
  font-size: 12px;
}

/* 队长卡片特殊侧边 */
.leader-card-modern {
  /* 移除特殊侧边和背景，使用默认卡片样式 */
}

.leader-badge {
  background: #fffbeb;
  color: #b45309;
  border: 1px solid rgba(245, 158, 11, 0.1);
}

/* 娣诲姞鍗＄墖鏋佽嚧缇庡寲鍗囩骇 */
.add-card-modern {
  cursor: pointer;
  border: 2px dashed #e2e8f0 !important;
  background: rgba(255, 255, 255, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 28px;
  transition: all 0.4s !important;
}

.add-card-modern:hover {
  border-color: #8b5cf6 !important;
  background: #f5f3ff;
  transform: translateY(-10px);
}

.plus-circle {
  width: 60px;
  height: 60px;
  border-radius: 22px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  color: #94a3b8;
  font-size: 24px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.add-card-modern:hover .plus-circle {
  background: #8b5cf6;
  color: #fff;
  transform: rotate(90deg) scale(1.1);
  box-shadow: 0 10px 25px rgba(139, 92, 246, 0.3);
}

.add-text {
  font-weight: 800;
  color: #64748b;
  font-size: 16px;
  letter-spacing: -0.2px;
}

.hero-actions :deep(.el-button) {
  height: 44px;
  padding: 0 24px;
  border-radius: 12px;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 鍝嶅簲寮忛€傞厤 */
@media (max-width: 768px) {
  .page-hero {
    padding: 24px;
    flex-direction: column;
    text-align: center;
  }
  .hero-title {
    justify-content: center;
  }
  .card-grid {
    grid-template-columns: 1fr;
  }
}

/* 美化表单元素 */
.el-form-item {
  margin-bottom: 24px !important;
}

.el-form-item__label {
  font-weight: 600 !important;
  color: #374151 !important;
  font-size: 14px !important;
}

.el-input {
  border-radius: 8px !important;
  border: 1px solid #e5e7eb !important;
  transition: all 0.3s ease !important;
}

.el-input:focus-within {
  border-color: #8b5cf6 !important;
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.1) !important;
}

.el-select {
  border-radius: 8px !important;
  border: 1px solid #e5e7eb !important;
  transition: all 0.3s ease !important;
}

.el-select:focus-within {
  border-color: #8b5cf6 !important;
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.1) !important;
}

.el-radio {
  margin-right: 20px !important;
}

.el-radio__label {
  font-size: 14px !important;
  color: #4b5563 !important;
}

.el-radio__input.is-checked .el-radio__inner {
  border-color: #8b5cf6 !important;
  background-color: #8b5cf6 !important;
}

.register-summary-card {
  position: relative;
  padding: 28px;
  margin-bottom: 28px;
  border-radius: 12px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  transition: all 0.3s ease;
}

.register-summary-card:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
  border-color: #d1d5db;
}



.my-register {
  width: 100%;
}

.member-visible-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(148, 163, 184, 0.16);
}

.register-summary-content {
  min-width: 0;
}

.register-summary-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f3f4f6;
}

.register-summary-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  letter-spacing: 0;
  line-height: 1.3;
}

.summary-status-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 14px;
  height: 28px;
  border-radius: 14px;
  border: 1px solid #dbeafe;
  background: #eff6ff;
  color: #2563eb;
  font-size: 12px;
  font-weight: 600;
}

.summary-status-badge.is-pending {
  border-color: #fde68a;
  background: #fffbeb;
  color: #d97706;
}

.register-summary-item {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-top: 16px;
  padding: 12px 0;
  min-height: 50px;
}

.summary-item-label {
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  min-width: 100px;
  flex-shrink: 0;
  line-height: 1.3;
}

.summary-team-name {
  display: inline-flex;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
  color: #374151;
  letter-spacing: 0;
  line-height: 1.3;
  flex: 1;
  min-height: 32px;
}

.summary-value {
  font-size: 15px;
  color: #4b5563;
  font-weight: 400;
  flex: 1;
  min-height: 32px;
  display: flex;
  align-items: center;
}

.summary-edit-btn {
  color: #3b82f6 !important;
  font-weight: 500 !important;
  padding: 6px 16px !important;
  height: 32px !important;
  border-radius: 6px !important;
  border: 1px solid #dbeafe !important;
  background: #ffffff !important;
  transition: all 0.2s ease !important;
}

.summary-edit-btn:hover {
  background: #f3f4f6 !important;
  border-color: #93c5fd !important;
  transform: translateY(-1px) !important;
}

.summary-team-edit {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-height: 32px;
}

.summary-team-input {
  width: 280px;
  min-height: 32px;
}

.summary-team-input .el-input {
  min-height: 32px !important;
}

.summary-team-input .el-input__wrapper {
  min-height: 32px !important;
}

.summary-action-btn {
  font-weight: 700 !important;
  border-radius: 10px !important;
}

.summary-cancel-btn {
  border-radius: 10px !important;
}

.summary-missing-block {
  margin-top: 18px;
  padding: 14px 16px;
  border-radius: 12px;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  color: #c2410c;
  font-size: 13px;
  line-height: 1.6;
}

.summary-missing-title {
  font-weight: 700;
  margin-right: 6px;
}

.work-info-grid {
  margin-bottom: 20px;
}

.work-description-item {
  margin-bottom: 8px;
}

.required-warning-text {
  margin-top: -6px;
  margin-bottom: 16px;
  color: #dc2626;
  font-size: 13px;
  font-weight: 500;
}

.create-team-name-box {
  display: flex;
  align-items: center;
  gap: 12px;
}

.create-team-name-input {
  width: 380px;
  max-width: 100%;
}

.create-team-name-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  min-height: 42px;
  box-shadow: 0 0 0 1px #e2e8f0 inset !important;
}

.create-team-name-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #8b5cf6 inset, 0 0 0 4px rgba(139, 92, 246, 0.12) !important;
}

.create-team-name-input.is-saved :deep(.el-input__wrapper) {
  background: #faf5ff !important;
  box-shadow: 0 0 0 1px #d8b4fe inset !important;
}

.create-team-name-input.is-saved :deep(.el-input__inner) {
  color: #7c3aed !important;
  font-weight: 700 !important;
}

.create-team-name-save {
  border-radius: 12px !important;
  padding: 0 20px !important;
  height: 42px !important;
  font-weight: 700 !important;
  color: #ffffff !important;
  background: linear-gradient(135deg, #8b5cf6, #6366f1) !important;
  border: none !important;
  box-shadow: 0 6px 14px rgba(99, 102, 241, 0.28) !important;
}

.create-team-name-save.is-save-state {
  background: linear-gradient(135deg, #8b5cf6, #6366f1) !important;
  border: none !important;
  color: #ffffff !important;
  box-shadow: 0 6px 14px rgba(99, 102, 241, 0.28) !important;
}

.create-team-name-save.is-modify-state {
  background: linear-gradient(135deg, #8b5cf6, #6366f1) !important;
  border: none !important;
  color: #ffffff !important;
  box-shadow: 0 6px 14px rgba(99, 102, 241, 0.28) !important;
}

.create-team-name-save:hover,
.create-team-name-save:focus,
.create-team-name-save:active,
.create-team-name-save.is-modify-state:hover,
.create-team-name-save.is-modify-state:focus,
.create-team-name-save.is-modify-state:active {
  background: linear-gradient(135deg, #8b5cf6, #6366f1) !important;
  border: none !important;
  color: #ffffff !important;
  box-shadow: 0 6px 14px rgba(99, 102, 241, 0.28) !important;
  opacity: 1 !important;
}

/* 美化卡片 */
.add-card-modern {
  cursor: pointer;
  border: 2px dashed #e2e8f0 !important;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  transition: all 0.4s ease !important;
  padding: 40px 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.add-card-modern:hover {
  border-color: #8b5cf6 !important;
  background: #f5f3ff;
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(139, 92, 246, 0.15);
}

.plus-circle {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #8b5cf6, #a78bfa);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);
  transition: all 0.3s ease;
}

.add-card-modern:hover .plus-circle {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(139, 92, 246, 0.4);
}

.plus-circle i {
  color: white;
  font-size: 20px;
}

.add-text {
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  transition: all 0.3s ease;
}

.add-card-modern:hover .add-text {
  color: #8b5cf6;
  font-weight: 600;
}

/* 美化按钮 */
.el-button--primary {
  background: linear-gradient(135deg, #8b5cf6, #a78bfa) !important;
  border: none !important;
  border-radius: 8px !important;
  padding: 10px 24px !important;
  font-weight: 600 !important;
  transition: all 0.3s ease !important;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3) !important;
}

.el-button--primary:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 6px 16px rgba(139, 92, 246, 0.4) !important;
}

.el-button--primary:active {
  transform: translateY(0) !important;
}

/* 美化区域标题 */
.section-title-wrapper {
  margin-bottom: 20px !important;
  margin-top: 32px !important;
}

.section-title {
  font-size: 16px !important;
  font-weight: 700 !important;
  color: #111827 !important;
  margin-left: 8px !important;
}

.section-icon {
  color: #8b5cf6 !important;
  font-size: 18px !important;
}

/* 美化注册表单容器 */
.register-form-container {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-top: 24px;
}

/* 美化卡片网格 */
.card-grid {
  margin-bottom: 24px !important;
  gap: 20px !important;
}

/* 美化提示信息 */
.el-alert {
  border-radius: 8px !important;
  margin: 16px 0 !important;
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.register-form-container {
  animation: fadeInUp 0.5s ease-out;
}

.info-card {
  animation: fadeInUp 0.5s ease-out;
}

.info-card:nth-child(1) { animation-delay: 0.1s; }
.info-card:nth-child(2) { animation-delay: 0.2s; }
.info-card:nth-child(3) { animation-delay: 0.3s; }
.info-card:nth-child(4) { animation-delay: 0.4s; }

.nav-item-dropdown {
  position: relative;
  display: inline-block;
  height: auto;
}

.nav-item-dropdown .nav-link {
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 0;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-item-dropdown:hover .nav-link,
.nav-item-dropdown.active .nav-link {
  color: white;
}

.dropdown-menu {
  display: none;
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  background-color: #fff;
  min-width: 140px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-radius: 8px;
  padding: 8px 0;
  z-index: 1000;
}

.nav-item-dropdown:hover .dropdown-menu {
  display: block;
}

.dropdown-item {
  display: block;
  padding: 10px 20px;
  color: #333;
  text-decoration: none;
  white-space: nowrap;
  transition: all 0.2s;
  font-size: 14px;
  text-align: center;
}

.dropdown-item:hover {
  background-color: #f3f4f6;
  color: #6366f1;
}

.dropdown-item.active {
  color: #6366f1;
  background-color: #eef2ff;
  font-weight: 600;
}

/* 讯飞杯报名页视觉层：不触碰表单模型、提交事件或登录会话 */
.register-page {
  min-height: 100vh;
  padding: 28px 0 120px;
  background: #c8e5ff url("@/assets/images/xfc-register-background.png") center top / 100% auto no-repeat fixed;
  font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
}

.register-page :deep(.student-navbar),
.register-page > :deep(.navbar) { display: none; }

.xfc-masthead {
  position: relative;
  width: min(1180px, calc(100% - 48px));
  height: 222px;
  margin: 0 auto 12px;
  color: #0c3f89;
  overflow: hidden;
}
.school-mark { position: absolute; top: 8px; left: 16px; display: flex; align-items: center; gap: 10px; font-size: 23px; font-weight: 800; letter-spacing: 3px; }
.school-mark small { display: block; margin-top: 3px; font-size: 7px; letter-spacing: 1px; font-weight: 700; }
.school-seal { display: grid; place-items: center; width: 43px; height: 43px; border: 2px solid #1552a2; border-radius: 50%; font-size: 10px; letter-spacing: 0; }
.xfc-masthead h2 { position: absolute; top: 78px; left: 18px; margin: 0; font-size: clamp(27px, 3vw, 42px); line-height: 1.25; letter-spacing: 1px; font-weight: 900; text-shadow: 0 2px 10px rgba(255,255,255,.75); }
.xfc-masthead h2 b { color: #8339ee; }
.xfc-masthead h2 small { display: block; margin-top: 8px; font-size: 20px; font-weight: 500; letter-spacing: 8px; }
.xfc-slogan { position: absolute; right: 12px; top: 25px; color: #185bda; font-size: 17px; line-height: 1.6; text-align: center; transform: rotate(-8deg); font-weight: 700; }
.xfc-slogan em { font-style: normal; font-size: 14px; }

.page-hero { width: min(880px, calc(100% - 276px)); margin: 0 calc((100% - min(1180px, calc(100% - 48px))) / 2) 12px auto; padding: 0; color: #123d75; background: rgba(255,255,255,.86); border: 1px solid rgba(255,255,255,.78); border-radius: 18px; box-shadow: 0 10px 26px rgba(43,111,179,.13); position: static; overflow: visible; }
.hero-container { max-width: none; padding: 16px 22px; align-items: center; }
.hero-title h1 { font-size: 21px; color: #0c3976; }
.hero-title i { color: #2368eb; }
.hero-desc { color: #7489a9; margin: 5px 0; font-size: 13px; }
.hero-meta { margin-top: 7px; }
.hero-actions :deep(.el-button) { border-radius: 9px !important; background: linear-gradient(100deg,#7968f5,#43b9ed) !important; box-shadow: none !important; }
.process-steps, .guidance { display: none; }

.xfc-registration-layout { width: min(1180px, calc(100% - 48px)); margin: 0 auto; display: grid; grid-template-columns: 214px minmax(0, 1fr); gap: 16px; align-items: start; }
.xfc-progress-rail { position: sticky; top: 22px; min-height: 540px; padding: 24px 22px 20px; border-radius: 16px; background: linear-gradient(180deg,rgba(255,255,255,.92),rgba(247,248,255,.78)); border: 1px solid rgba(255,255,255,.85); box-shadow: 0 10px 26px rgba(64,107,184,.13); color: #5a6f95; }
.rail-title { color: #2072db; font-size: 16px; font-weight: 800; margin-bottom: 26px; }
.rail-title i { margin-right: 10px; }
.rail-step { position: relative; display: flex; align-items: center; gap: 12px; min-height: 57px; font-weight: 700; color: #a2afc4; }
.rail-step:not(:last-of-type)::after { content: ""; position: absolute; height: 27px; left: 10px; top: 36px; width: 2px; background: #dae2ef; }
.rail-step b { display: grid; place-items: center; width: 21px; height: 21px; border-radius: 50%; background: #cdd6e7; color: white; font-size: 12px; z-index: 1; }
.rail-step.current { color: #135fe3; }.rail-step.current b { background: #235be9; }
.rail-message { margin: 54px -8px 28px; color: #1458e0; font-size: 18px; font-style: italic; line-height: 1.6; transform: rotate(-7deg); text-align: center; }
.rail-deadline { padding-top: 16px; border-top: 1px solid #dce5f1; display: flex; gap: 10px; align-items: flex-start; color: #114c9e; font-size: 12px; line-height: 1.7; }.rail-deadline i { margin-top: 4px; }.rail-deadline strong { font-size: 12px; }

.content { width: auto; max-width: none; margin: 0; padding: 0; }
.panel > :deep(.el-card) { background: transparent; border: 0; overflow: visible; }
.panel > :deep(.el-card__header) { margin-bottom: 10px; padding: 17px 24px; background: rgba(255,255,255,.91); border: 1px solid rgba(255,255,255,.85); border-radius: 17px; box-shadow: 0 9px 24px rgba(43,111,179,.12); }
.card-header { color: #103f7b; font-size: 20px; font-weight: 800; }.card-icon { color: #2b70ec !important; }
.panel > :deep(.el-card__body) { padding: 0 !important; }
.register-form-container, .register-summary-card, .info-section, .student-section, .material-section, .my-register > .info-section { box-sizing: border-box; padding: 22px 24px !important; margin: 0 0 12px !important; background: rgba(255,255,255,.9) !important; border: 1px solid rgba(255,255,255,.86) !important; border-radius: 17px !important; box-shadow: 0 9px 24px rgba(43,111,179,.11) !important; }
.info-section, .student-section { margin-top: 12px !important; }.section-title { color: #103f7b !important; }.section-icon { color: #2a72ef !important; }
.info-card { max-width: none; height: auto; min-height: 154px; border-radius: 14px; }.card-grid { grid-template-columns: repeat(auto-fit, minmax(280px,1fr)); }.card-body { padding: 18px 20px; }.avatar-circle-modern { width: 70px; height: 70px; border-radius: 20px; }.name-line { font-size: 18px; }
.register-page :deep(.el-input__wrapper), .register-page :deep(.el-textarea__inner) { box-shadow: 0 0 0 1px #dbe4f0 inset !important; border-radius: 7px !important; background: rgba(255,255,255,.86); }.register-page :deep(.el-input__wrapper.is-focus), .register-page :deep(.el-textarea__inner:focus) { box-shadow: 0 0 0 1px #4c83f3 inset !important; }
.register-page :deep(.el-button--primary) { background: linear-gradient(100deg,#716df2,#40bde9) !important; }

@media (max-width: 820px) { .register-page { padding-top: 12px; background-size: auto 100%; }.xfc-masthead { height: 160px; width: calc(100% - 28px); }.xfc-masthead h2 { top: 72px; left: 0; font-size: 23px; }.xfc-masthead h2 small { font-size: 13px; letter-spacing: 3px; }.school-mark { left: 0; font-size: 16px; }.xfc-slogan { display: none; }.page-hero { width: calc(100% - 28px); margin: 0 auto 10px; }.hero-container { padding: 13px; }.xfc-registration-layout { width: calc(100% - 28px); grid-template-columns: 1fr; }.xfc-progress-rail { display: none; }.card-grid { grid-template-columns: 1fr; } }
</style>
