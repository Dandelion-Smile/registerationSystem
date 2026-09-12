﻿﻿﻿﻿﻿<template>
  <div class="teacher-page">
    <header class="header-nav">
      <div class="nav-container">
        <div class="logo-section" @click="switchMenu('competition')">
          <i class="fas fa-trophy"></i>
          <span class="logo-text">智启赛途</span>
        </div>
        <nav class="main-nav">
          <router-link :to="{ path: '/teacher', query: { menu: 'competition' } }" class="header-nav-item" :class="{ active: activeMenu === 'competition' }" @click="switchMenu('competition')">赛事管理</router-link>
          <router-link :to="{ path: '/teacher', query: { menu: 'review' } }" class="header-nav-item" :class="{ active: activeMenu === 'review' }" @click="switchMenu('review')">评审管理</router-link>
          <router-link :to="{ path: '/teacher', query: { menu: 'my-teams' } }" class="header-nav-item" :class="{ active: activeMenu === 'my-teams' }" @click="switchMenu('my-teams')">我的队伍赛事</router-link>
          <router-link :to="{ path: '/teacher', query: { menu: 'mock-test' } }" class="header-nav-item" :class="{ active: activeMenu === 'mock-test' }" @click="switchMenu('mock-test')">竞赛模拟测试</router-link>
          <router-link :to="{ path: '/teacher', query: { menu: 'smart-training' } }" class="header-nav-item" :class="{ active: activeMenu === 'smart-training' }" @click="switchMenu('smart-training')">智能训练出题</router-link>
        </nav>
        <div class="nav-actions">
          <el-dropdown @command="handleCommand" class="avatar-container" trigger="hover">
            <div class="avatar-wrapper">
              <img :src="userStore.avatar" class="user-avatar-circle" />
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <div class="main-container">
      <!-- 左侧导航栏 -->
      <aside class="sidebar">
        <nav class="sidebar-nav">
          <div 
            v-for="item in menuItems" 
            :key="item.key"
            class="nav-item"
            :class="{ active: activeMenu === item.key }"
            @click="switchMenu(item.key)"
          >
            <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
            <span class="nav-text">{{ item.label }}</span>
          </div>
        </nav>
      </aside>

      <!-- 主内容区 -->
      <main class="content-area">
        <!-- 竞赛查看 -->
        <div v-if="activeMenu === 'competition'" class="content-section">
          <div class="section-header">
            <h2 class="section-title">竞赛管理</h2>
            <p class="section-subtitle">分析竞赛数据</p>
          </div>

          <!-- 统计卡片 -->
           <!--
          <div class="stats-cards">
            <div class="stat-card">
              <div class="stat-icon purple">
                <el-icon><Trophy /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.totalCompetitions }}</div>
                <div class="stat-label">总竞赛数</div>
                <div class="stat-trend up">
                  <el-icon><ArrowUp /></el-icon>
                  <span>较上月增加{{ stats.competitionIncrease }}%</span>
                </div>
              </div>
            </div>

            <div class="stat-card">
              <div class="stat-icon orange">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.inProgress }}</div>
                <div class="stat-label">进行中竞赛</div>
                <div class="stat-trend up">
                  <el-icon><ArrowUp /></el-icon>
                  <span>{{ stats.endingSoon }}个即将结束</span>
                </div>
              </div>
            </div>

            <div class="stat-card">
              <div class="stat-icon green">
                <el-icon><UserFilled /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.avgParticipation }}%</div>
                <div class="stat-label">平均参与率</div>
                <div class="stat-trend up">
                  <el-icon><ArrowUp /></el-icon>
                  <span>较上月提升{{ stats.participationIncrease }}%</span>
                </div>
              </div>
            </div>

            <div class="stat-card">
              <div class="stat-icon purple">
                <el-icon><Star /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.avgScore }}</div>
                <div class="stat-label">平均得分</div>
                <div class="stat-trend down">
                  <el-icon><ArrowDown /></el-icon>
                  <span>较上月下降{{ stats.scoreDecrease }}</span>
                </div>
              </div>
            </div>
          </div>
        -->

          <!-- 竞赛列表 -->
          <div class="competition-list-section">
            <div class="list-header">
              <h3 class="list-title">竞赛列表</h3>
              <div class="list-actions">
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索竞赛名称..."
                  class="search-input"
                  clearable
                  @input="handleSearch"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
                <el-select
                  v-model="filterStatus"
                  placeholder="全部状态"
                  class="status-select"
                  @change="handleFilter"
                >
                  <el-option label="全部状态" value="" />
                  <el-option label="未开始" value="not_started" />
                  <el-option label="进行中" value="in_progress" />
                  <el-option label="已结束" value="ended" />
                </el-select>
              </div>
            </div>

            <div 
              class="competition-list-container" 
              ref="listContainerRef"
              @scroll="handleScroll"
            >
              <div class="competition-grid">
                <div
                  v-for="comp in filteredCompetitions"
                  :key="comp.competitionId"
                  class="competition-card"
                >
                  <div class="card-header">
                    <div class="card-status" :class="getStatusClass(comp.status)">
                      <el-icon v-if="comp.status === 'ended'"><Check /></el-icon>
                      <el-icon v-else-if="comp.status === 'in_progress'"><Clock /></el-icon>
                      <el-icon v-else><Calendar /></el-icon>
                      <span>{{ getStatusText(comp.status) }}</span>
                    </div>
                  </div>
                  <h4 class="card-title">{{ comp.competitionName }}</h4>
                  <div class="card-time">
                    {{ formatDate(comp.registerStartTime) }} ~ {{ formatDate(comp.registerEndTime) }}
                  </div>
                  <div class="card-metrics">
                    <div class="metric-item" v-if="comp.participantCount">
                      <span class="metric-label">参与{{ comp.participantType === 'team' ? '队伍' : '人数' }}：</span>
                      <span class="metric-value">{{ comp.participantCount }}{{ comp.participantType === 'team' ? '队' : '人' }}</span>
                    </div>
                    <div class="metric-item" v-if="comp.avgScore">
                      <span class="metric-label">平均分：</span>
                      <span class="metric-value">{{ comp.avgScore }}</span>
                    </div>
                    <div class="metric-item" v-if="comp.submissionRate">
                      <span class="metric-label">提交率：</span>
                      <span class="metric-value">{{ comp.submissionRate }}%</span>
                    </div>
                    <div class="metric-item" v-if="comp.remainingTime">
                      <span class="metric-label">剩余时间：</span>
                      <span class="metric-value">{{ comp.remainingTime }}</span>
                    </div>
                    <div class="metric-item" v-if="comp.awardRate">
                      <span class="metric-label">获奖率：</span>
                      <span class="metric-value">{{ comp.awardRate }}%</span>
                    </div>
                    <div class="metric-item" v-if="comp.duration">
                      <span class="metric-label">时长：</span>
                      <span class="metric-value">{{ comp.duration }}</span>
                    </div>
                    <div class="metric-item" v-if="comp.theme">
                      <span class="metric-label">主题：</span>
                      <span class="metric-value">{{ comp.theme }}</span>
                    </div>
                  </div>
                  <el-button type="primary" class="card-action" @click="viewCompetitionDetail(comp)">
                    查看详情
                  </el-button>
                </div>
              </div>
              <div v-if="loadingMore" class="load-more-tip">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>加载中...</span>
              </div>
              <div v-else-if="!hasMore && filteredCompetitions.length > 0" class="load-more-tip">
                <span>没有更多数据了</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 评审管理 -->
        <div v-if="activeMenu === 'review'" class="content-section">
          <div class="section-header">
            <h2 class="section-title">评审管理</h2>
            <p class="section-subtitle">查看可评审竞赛，对参赛队伍进行评分</p>
          </div>

          <!-- 可评审竞赛列表 -->
          <div v-loading="loadingReviewable" class="review-competition-list">
            <div
              v-for="comp in reviewableCompetitions"
              :key="comp.competitionId"
              class="review-competition-card"
            >
              <div class="review-card-header">
                <h4 class="review-card-title">{{ comp.competitionName }}</h4>
                <el-tag type="success" class="review-badge">可评审</el-tag>
              </div>
              <div class="review-card-info">
                <div class="info-item">
                  <span class="info-label">报名时间：</span>
                  <span class="info-value">{{ formatDate(comp.registerStartTime) }} ~ {{ formatDate(comp.registerEndTime) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">评审进度：</span>
                  <span class="info-value">{{ comp.reviewedCount || 0 }}/{{ comp.totalTeams || 0 }} 队伍</span>
                </div>
              </div>
              <el-button type="primary" @click="startReview(comp)">
                开始评审
              </el-button>
              <el-button type="info" plain @click="viewReviewDetail(comp)" style="margin-left: 10px">
                查看详情
              </el-button>
            </div>
            <div v-if="!loadingReviewable && reviewableCompetitions.length === 0" class="empty-review-list">
              <el-empty description="暂无待评审竞赛" />
              <p class="empty-tip">请联系管理员为您分配竞赛评审权限</p>
            </div>
          </div>
        </div>

        <!-- 我的队伍赛事 -->
        <div v-if="activeMenu === 'my-teams'" class="content-section">
          <!-- 老师基本信息 -->
          <div class="teacher-info-card">
            <div class="info-header">
              <div class="avatar">
                <el-icon class="avatar-icon"><User /></el-icon>
              </div>
              <div class="personal-info">
                <h3 class="teacher-name">{{ teacherInfo.name }}</h3>
                <div class="teacher-details">
                  <span class="detail-item">{{ teacherInfo.position }}</span>
                  <span class="detail-item">{{ teacherInfo.department }}</span>
                  <span class="detail-item">{{ teacherInfo.email }}</span>
                  <span class="detail-item">{{ teacherInfo.phone }}</span>
                </div>
              </div>
            </div>
            <div class="teacher-stats-grid">
              <div class="teacher-stat-card">
                <div class="teacher-stat-value">{{ myTeamStats.competitionCount }}</div>
                <div class="teacher-stat-label">指导竞赛</div>
              </div>
              <div class="teacher-stat-card">
                <div class="teacher-stat-value">{{ myTeamStats.teamCount }}</div>
                <div class="teacher-stat-label">指导队伍</div>
              </div>
              <div class="teacher-stat-card">
                <div class="teacher-stat-value">{{ myTeamStats.submittedCount }}</div>
                <div class="teacher-stat-label">已提交作品</div>
              </div>
            </div>
          </div>

          <div class="section-header my-teams-header">
            <div class="my-teams-header-main">
              <h2 class="section-title">我的队伍赛事</h2>
              <p class="section-subtitle">查看我作为指导老师的队伍参加的竞赛</p>
            </div>
            <div class="my-teams-toolbar">
              <el-input
                v-model="myTeamsKeyword"
                placeholder="搜索竞赛名或队伍名"
                class="search-input"
                clearable
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-select v-model="myTeamsStatusFilter" class="status-select" placeholder="提交状态">
                <el-option label="全部状态" value="" />
                <el-option label="未提交" value="未提交" />
                <el-option label="已提交" value="已提交" />
                <el-option label="已评分" value="已评分" />
              </el-select>
            </div>
          </div>

          <div v-loading="loadingMyTeams" class="my-teams-list-container">

            <div v-if="filteredMyTeamCompetitions.length > 0" class="my-team-competition-list">
              <div
                v-for="comp in filteredMyTeamCompetitions"
                :key="comp.competitionId"
                class="my-team-competition-card"
              >
                <div class="competition-header">
                  <div>
                    <h3 class="competition-title">{{ comp.competitionName }}</h3>
                    <div class="competition-meta">
                      <span>{{ formatDate(comp.registerStartTime) }} - {{ formatDate(comp.registerEndTime) }}</span>
                      <span>{{ comp.teams?.length || 0 }} 支队伍</span>
                    </div>
                  </div>
                  <el-button type="info" plain @click="viewCompetitionDetail(comp)">竞赛详情</el-button>
                </div>

                <div class="my-team-card-grid">
                  <div
                    v-for="team in comp.teams"
                    :key="`${comp.competitionId}-${team.teamId}`"
                    class="team-card"
                  >
                    <div class="team-card-top">
                      <div>
                        <div class="team-name">{{ team.teamName || '未命名队伍' }}</div>
                        <div class="team-subtitle">{{ team.workName || '待上传作品' }}</div>
                      </div>
                      <div class="team-status-group">
                        <el-tag :type="normalizeSubmissionStatus(team) === '已评分' ? 'primary' : normalizeSubmissionStatus(team) === '已提交' ? 'success' : 'info'">
                          {{ normalizeSubmissionStatus(team) }}
                        </el-tag>
                        <el-tag :type="team.hasMaterials ? 'warning' : 'info'">
                          {{ team.materialStatus || '未上传材料' }}
                        </el-tag>
                        <el-tag v-if="team.finalScore !== null && team.finalScore !== undefined" type="primary">
                          成绩 {{ team.finalScore }}
                        </el-tag>
                      </div>
                    </div>

                    <div class="team-details">
                      <div class="detail-row">
                        <span class="detail-label">负责人</span>
                        <span class="detail-value">{{ team.leaderName || '未识别' }}</span>
                      </div>
                      <div class="detail-row">
                        <span class="detail-label">学号</span>
                        <span class="detail-value">{{ team.leaderStudentNo || '-' }}</span>
                      </div>
                      <div class="detail-row">
                        <span class="detail-label">专业</span>
                        <span class="detail-value">{{ team.leaderMajor || '-' }}</span>
                      </div>
                      <div class="detail-row">
                        <span class="detail-label">指导老师</span>
                        <span class="detail-value">{{ team.advisor || '未指定' }}</span>
                      </div>
                      <div class="detail-row">
                        <span class="detail-label">提交时间</span>
                        <span class="detail-value">{{ formatDate(team.submitTime) || '未提交' }}</span>
                      </div>
                    </div>

                    <div class="team-card-actions">
                      <el-button type="primary" size="small" @click="viewTeamDetail(team)">
                        查看材料
                      </el-button>
                      <el-button
                        v-if="team.canReview"
                        type="success"
                        plain
                        size="small"
                        @click="goToReviewFromMyTeams(team)"
                      >
                        进入评审
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
                        <div v-if="!loadingMyTeams && filteredMyTeamCompetitions.length === 0" class="empty-table">
              <el-empty :description="myTeamsEmptyDescription" />
              <p class="empty-tip">{{ myTeamsEmptyTip }}</p>
            </div>
            <!-- ???? -->
            <div v-if="false" class="teams-table-container">
              <div class="table-header">
                <h3 class="table-title">已报名队伍展示</h3>
                <div class="table-actions">
                  <span class="team-count">{{ totalTeamsCount }} 活跃团队</span>
                </div>
              </div>
              
              <div class="teams-table">
                <div class="table-header-row">
                  <div class="table-header-cell">队伍名称</div>
                  <div class="table-header-cell">作品名称</div>
                  <div class="table-header-cell">负责人</div>
                  <div class="table-header-cell">指导老师</div>
                  <div class="table-header-cell">平均分</div>
                  <div class="table-header-cell">详情</div>
                </div>
                
                <div v-if="allTeams.length > 0">
                  <div 
                    v-for="team in allTeams" 
                    :key="team.teamId" 
                    class="table-row"
                  >
                    <div class="table-cell">{{ team.teamName }}</div>
                    <div class="table-cell">《{{ team.workName || '待提交作品' }}》</div>
                    <div class="table-cell">
                      <div class="leader-info">
                        <span class="leader-avatar">{{ getLeaderInitial(team.leaderName) }}</span>
                        <span class="leader-name">{{ team.leaderName || '未知' }}</span>
                      </div>
                    </div>
                    <div class="table-cell">{{ team.advisor || '未指定' }}</div>
                    <div class="table-cell">
                      <span class="score-tag">{{ team.finalScore || '0.00' }}</span>
                    </div>
                    <div class="table-cell">
                      <el-button 
                        type="primary" 
                        size="small" 
                        @click="viewTeamDetail(team)"
                      >
                        详情
                      </el-button>
                    </div>
                  </div>
                </div>
                
                <div v-else-if="!loadingMyTeams" class="empty-table">
                  <el-empty description="暂无队伍赛事" />
                  <p class="empty-tip">您还没有作为指导老师带领队伍参加任何竞赛</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="activeMenu === 'mock-test'" class="content-section feature-content-section">
          <div class="section-header feature-header">
            <div>
              <h2 class="section-title">竞赛模拟测试</h2>
              <p class="section-subtitle">查看已发布赛事，并对赛事过程与成绩表现进行静态分析</p>
            </div>
            <div class="feature-actions">
              <el-button type="warning" @click="notifyStaticAction('发布新竞赛')">
                <i class="fa fa-plus feature-button-icon"></i>
                发布新竞赛
              </el-button>
              <el-button plain @click="notifyStaticAction('导出数据')">
                <i class="fa fa-download feature-button-icon"></i>
                导出数据
              </el-button>
            </div>
          </div>

          <div class="feature-stat-grid">
            <div class="feature-stat-card accent-violet">
              <div class="feature-stat-label">已发布赛事</div>
              <div class="feature-stat-value">{{ mockTestSummary.total }}</div>
              <div class="feature-stat-meta">覆盖 {{ mockTestSummary.totalTeams }} 支参赛队伍</div>
            </div>
            <div class="feature-stat-card accent-amber">
              <div class="feature-stat-label">进行中赛事</div>
              <div class="feature-stat-value">{{ mockTestSummary.inProgress }}</div>
              <div class="feature-stat-meta">未开始 {{ mockTestSummary.notStarted }} 场，已结束 {{ mockTestSummary.ended }} 场</div>
            </div>
            <div class="feature-stat-card accent-blue">
              <div class="feature-stat-label">平均提交率</div>
              <div class="feature-stat-value">{{ mockTestSummary.avgSubmission }}%</div>
              <div class="feature-stat-meta">平均完赛分 {{ mockTestSummary.avgScore }}</div>
            </div>
          </div>

          <div class="feature-top-grid">
            <section class="feature-panel">
              <div class="panel-head">
                <div>
                  <h3 class="panel-title">赛事状态分布</h3>
                  <p class="panel-caption">按当前发布状态统计模拟测试安排</p>
                </div>
              </div>
              <div class="distribution-list">
                <div
                  v-for="item in mockStatusDistribution"
                  :key="item.key"
                  class="distribution-row"
                >
                  <div class="distribution-meta">
                    <span class="distribution-name">{{ item.label }}</span>
                    <span class="distribution-value">{{ item.count }} 场</span>
                  </div>
                  <div class="distribution-track">
                    <div class="distribution-fill" :style="{ width: `${item.percent}%`, background: item.color }"></div>
                  </div>
                </div>
              </div>
            </section>

            <section class="feature-panel">
              <div class="panel-head">
                <div>
                  <h3 class="panel-title">赛事参与热度</h3>
                  <p class="panel-caption">展示近期开启的模拟赛事参与变化</p>
                </div>
              </div>
              <div class="trend-card">
                <svg viewBox="0 0 420 180" class="trend-svg" aria-hidden="true">
                  <defs>
                    <linearGradient id="mockTrendArea" x1="0" x2="0" y1="0" y2="1">
                      <stop offset="0%" stop-color="#6366f1" stop-opacity="0.28" />
                      <stop offset="100%" stop-color="#6366f1" stop-opacity="0.04" />
                    </linearGradient>
                  </defs>
                  <polyline class="trend-area" :points="mockParticipationArea" fill="url(#mockTrendArea)" />
                  <polyline class="trend-line violet-line" :points="mockParticipationLine" />
                  <circle
                    v-for="point in mockParticipationPlot"
                    :key="point.label"
                    :cx="point.x"
                    :cy="point.y"
                    r="4"
                    class="trend-dot violet-dot"
                  />
                </svg>
                <div class="trend-axis">
                  <span v-for="point in mockParticipationPlot" :key="`${point.label}-axis`">{{ point.label }}</span>
                </div>
              </div>
            </section>
          </div>

          <section class="feature-panel feature-panel-stretch">
            <div class="feature-tabs">
              <button
                v-for="item in mockTestTabs"
                :key="item.key"
                type="button"
                class="feature-tab"
                :class="{ active: mockTestTab === item.key }"
                @click="mockTestTab = item.key"
              >
                {{ item.label }}
              </button>
            </div>

            <div v-if="mockTestTab === 'published'" class="feature-body">
              <div class="panel-toolbar">
                <div>
                  <h3 class="panel-title">最近发布的赛事</h3>
                  <p class="panel-caption">支持按名称与进行状态快速筛选赛事</p>
                </div>
                <div class="toolbar-actions">
                  <el-input
                    v-model="mockTestKeyword"
                    placeholder="搜索竞赛名称..."
                    class="search-input"
                    clearable
                  >
                    <template #prefix>
                      <el-icon><Search /></el-icon>
                    </template>
                  </el-input>
                  <el-select v-model="mockTestStatusFilter" placeholder="全部状态" class="status-select">
                    <el-option label="全部状态" value="" />
                    <el-option label="未开始" value="not_started" />
                    <el-option label="进行中" value="in_progress" />
                    <el-option label="已结束" value="ended" />
                  </el-select>
                </div>
              </div>

              <div v-if="filteredMockPublishedEvents.length" class="feature-card-grid">
                <article
                  v-for="event in filteredMockPublishedEvents"
                  :key="event.id"
                  class="feature-card"
                >
                  <div class="feature-card-top">
                    <div class="card-status" :class="getStatusClass(event.status)">
                      <el-icon v-if="event.status === 'ended'"><Check /></el-icon>
                      <el-icon v-else-if="event.status === 'in_progress'"><Clock /></el-icon>
                      <el-icon v-else><Calendar /></el-icon>
                      <span>{{ getStatusText(event.status) }}</span>
                    </div>
                    <span class="feature-pill">{{ event.category }}</span>
                  </div>
                  <h4 class="feature-card-title">{{ event.name }}</h4>
                  <p class="feature-card-time">{{ event.time }}</p>
                  <div class="feature-metric-grid">
                    <div class="feature-metric-cell">
                      <span class="feature-metric-name">参与队伍</span>
                      <span class="feature-metric-value">{{ event.teams }} 队</span>
                    </div>
                    <div class="feature-metric-cell">
                      <span class="feature-metric-name">提交率</span>
                      <span class="feature-metric-value">{{ event.submissionRate }}%</span>
                    </div>
                    <div class="feature-metric-cell">
                      <span class="feature-metric-name">题目数量</span>
                      <span class="feature-metric-value">{{ event.questionCount }} 题</span>
                    </div>
                    <div class="feature-metric-cell">
                      <span class="feature-metric-name">平均成绩</span>
                      <span class="feature-metric-value">{{ event.avgScore }}</span>
                    </div>
                  </div>
                  <div class="feature-card-foot">
                    <span>赛程：{{ event.duration }}</span>
                    <span>侧重：{{ event.focus }}</span>
                  </div>
                </article>
              </div>
              <div v-else class="feature-empty">
                <el-empty description="未找到匹配的模拟赛事" />
              </div>
            </div>

            <div v-else-if="mockTestTab === 'analysis'" class="feature-body">
              <div class="analysis-grid">
                <section class="analysis-subpanel">
                  <div class="panel-head">
                    <div>
                      <h3 class="panel-title">赛事分析</h3>
                      <p class="panel-caption">从参与热度与完成度看赛事运行表现</p>
                    </div>
                  </div>
                  <div class="insight-list">
                    <div
                      v-for="item in mockCompetitionInsights"
                      :key="item.id"
                      class="insight-item"
                    >
                      <div class="insight-top">
                        <span class="insight-name">{{ item.name }}</span>
                        <span class="insight-value">{{ item.value }}</span>
                      </div>
                      <div class="distribution-track">
                        <div class="distribution-fill" :style="{ width: `${item.percent}%`, background: item.color }"></div>
                      </div>
                      <p class="insight-caption">{{ item.caption }}</p>
                    </div>
                  </div>
                </section>

                <section class="analysis-subpanel">
                  <div class="panel-head">
                    <div>
                      <h3 class="panel-title">重点观察</h3>
                      <p class="panel-caption">便于教师定位需要重点跟进的赛事</p>
                    </div>
                  </div>
                  <div class="tag-summary-grid">
                    <div class="tag-summary-card">
                      <span class="tag-summary-label">高参与赛事</span>
                      <strong class="tag-summary-value">{{ mockPrioritySummary.highEngagement }}</strong>
                      <p class="tag-summary-caption">参与队伍达到 30 队以上</p>
                    </div>
                    <div class="tag-summary-card">
                      <span class="tag-summary-label">需督促赛事</span>
                      <strong class="tag-summary-value">{{ mockPrioritySummary.needFollowUp }}</strong>
                      <p class="tag-summary-caption">提交率低于 70% 建议提醒</p>
                    </div>
                    <div class="tag-summary-card">
                      <span class="tag-summary-label">高质量赛事</span>
                      <strong class="tag-summary-value">{{ mockPrioritySummary.highScore }}</strong>
                      <p class="tag-summary-caption">平均成绩达到 80 分以上</p>
                    </div>
                  </div>
                </section>
              </div>
            </div>

            <div v-else class="feature-body">
              <div class="analysis-grid">
                <section class="analysis-subpanel">
                  <div class="panel-head">
                    <div>
                      <h3 class="panel-title">成绩分析</h3>
                      <p class="panel-caption">查看成绩分层，识别高分和待提升区间</p>
                    </div>
                  </div>
                  <div class="distribution-list">
                    <div
                      v-for="item in mockScoreBands"
                      :key="item.label"
                      class="distribution-row"
                    >
                      <div class="distribution-meta">
                        <span class="distribution-name">{{ item.label }}</span>
                        <span class="distribution-value">{{ item.count }} 队</span>
                      </div>
                      <div class="distribution-track">
                        <div class="distribution-fill" :style="{ width: `${item.percent}%`, background: item.color }"></div>
                      </div>
                    </div>
                  </div>
                </section>

                <section class="analysis-subpanel">
                  <div class="panel-head">
                    <div>
                      <h3 class="panel-title">赛事成绩概览</h3>
                      <p class="panel-caption">按平均成绩排序，便于查看训练效果</p>
                    </div>
                  </div>
                  <div class="ranking-list">
                    <div
                      v-for="item in mockScoreRankings"
                      :key="item.id"
                      class="ranking-item"
                    >
                      <div class="ranking-index">{{ item.rank }}</div>
                      <div class="ranking-content">
                        <div class="ranking-name">{{ item.name }}</div>
                        <div class="ranking-meta">{{ item.teams }} 队参与，提交率 {{ item.submissionRate }}%</div>
                      </div>
                      <div class="ranking-score">{{ item.avgScore }}</div>
                    </div>
                  </div>
                </section>
              </div>
            </div>
          </section>
        </div>

        <div v-if="activeMenu === 'smart-training'" class="content-section feature-content-section">
          <div class="section-header feature-header">
            <div>
              <h2 class="section-title">智能训练出题</h2>
              <p class="section-subtitle">面向不同赛事发布智能训练，并追踪训练完成情况与效果变化</p>
            </div>
            <div class="feature-actions">
              <el-button type="warning" @click="notifyStaticAction('发布新训练')">
                <i class="fa fa-plus feature-button-icon"></i>
                发布新训练
              </el-button>
              <el-button type="warning" plain @click="notifyStaticAction('AI分析建议')">
                <i class="fa fa-lightbulb feature-button-icon"></i>
                AI分析建议
              </el-button>
              <el-button plain @click="notifyStaticAction('导出数据')">
                <i class="fa fa-download feature-button-icon"></i>
                导出数据
              </el-button>
            </div>
          </div>

          <section class="feature-panel feature-panel-stretch">
            <div class="panel-head panel-head-inline">
              <div>
                <h3 class="panel-title">训练分析仪表盘</h3>
                <p class="panel-caption">汇总当前已发布训练的完成率、训练时长与效果表现</p>
              </div>
              <span class="update-hint">更新时间：刚刚</span>
            </div>

            <div class="feature-stat-grid dashboard-stat-grid">
              <div class="feature-stat-card accent-violet">
                <div class="feature-stat-label">训练完成率</div>
                <div class="feature-stat-value">{{ trainingDashboard.completionRate }}%</div>
                <div class="feature-stat-meta">较上周提升 {{ trainingDashboard.completionDelta }}%</div>
              </div>
              <div class="feature-stat-card accent-blue">
                <div class="feature-stat-label">平均训练时长</div>
                <div class="feature-stat-value">{{ trainingDashboard.avgDuration }} 分钟</div>
                <div class="feature-stat-meta">最佳训练耗时 {{ trainingDashboard.bestDuration }} 分钟</div>
              </div>
              <div class="feature-stat-card accent-amber">
                <div class="feature-stat-label">训练效果评分</div>
                <div class="feature-stat-value">{{ trainingDashboard.effectScore }}</div>
                <div class="feature-stat-meta">高于平均水平 {{ trainingDashboard.effectDelta }}%</div>
              </div>
            </div>

            <div class="feature-top-grid">
              <section class="feature-panel inner-panel">
                <div class="panel-head">
                  <div>
                    <h3 class="panel-title">训练类型分布</h3>
                    <p class="panel-caption">查看不同训练方向在当前阶段的投放占比</p>
                  </div>
                </div>
                <div class="distribution-list">
                  <div
                    v-for="item in trainingTypeDistribution"
                    :key="item.label"
                    class="distribution-row"
                  >
                    <div class="distribution-meta">
                      <span class="distribution-name">{{ item.label }}</span>
                      <span class="distribution-value">{{ item.count }} 个</span>
                    </div>
                    <div class="distribution-track">
                      <div class="distribution-fill" :style="{ width: `${item.percent}%`, background: item.color }"></div>
                    </div>
                  </div>
                </div>
              </section>

              <section class="feature-panel inner-panel">
                <div class="panel-head">
                  <div>
                    <h3 class="panel-title">训练效果趋势</h3>
                    <p class="panel-caption">最近六个训练周期效果评分变化</p>
                  </div>
                </div>
                <div class="trend-card">
                  <svg viewBox="0 0 420 180" class="trend-svg" aria-hidden="true">
                    <defs>
                      <linearGradient id="trainingTrendArea" x1="0" x2="0" y1="0" y2="1">
                        <stop offset="0%" stop-color="#f59e0b" stop-opacity="0.26" />
                        <stop offset="100%" stop-color="#f59e0b" stop-opacity="0.05" />
                      </linearGradient>
                    </defs>
                    <polyline class="trend-area" :points="trainingTrendArea" fill="url(#trainingTrendArea)" />
                    <polyline class="trend-line amber-line" :points="trainingTrendLine" />
                    <circle
                      v-for="point in trainingTrendPlot"
                      :key="point.label"
                      :cx="point.x"
                      :cy="point.y"
                      r="4"
                      class="trend-dot amber-dot"
                    />
                  </svg>
                  <div class="trend-axis">
                    <span v-for="point in trainingTrendPlot" :key="`${point.label}-axis`">{{ point.label }}</span>
                  </div>
                </div>
              </section>
            </div>
          </section>

          <section class="feature-panel feature-panel-stretch">
            <div class="panel-toolbar">
              <div>
                <h3 class="panel-title">已发布的训练</h3>
                <p class="panel-caption">支持按训练名称和训练类型进行筛选</p>
              </div>
              <div class="toolbar-actions">
                <el-select v-model="trainingTypeFilter" class="status-select" placeholder="全部训练">
                  <el-option label="全部训练" value="" />
                  <el-option
                    v-for="item in trainingTypeOptions"
                    :key="item"
                    :label="item"
                    :value="item"
                  />
                </el-select>
                <el-input
                  v-model="trainingKeyword"
                  placeholder="搜索训练..."
                  class="search-input"
                  clearable
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </div>
            </div>

            <div v-if="filteredTrainingList.length" class="feature-card-grid training-card-grid">
              <article
                v-for="item in filteredTrainingList"
                :key="item.id"
                class="feature-card training-card"
              >
                <div class="feature-card-top">
                  <div class="training-title-group">
                    <h4 class="feature-card-title">{{ item.title }}</h4>
                    <p class="feature-card-time">{{ item.competitionName }}</p>
                  </div>
                  <span class="training-status">{{ item.status }}</span>
                </div>
                <div class="training-meta-line">
                  <span>{{ item.publishTime }}</span>
                  <span>时长：{{ item.duration }}</span>
                  <span>难度：{{ item.difficulty }}</span>
                </div>
                <div class="feature-metric-grid">
                  <div class="feature-metric-cell">
                    <span class="feature-metric-name">参与人数</span>
                    <span class="feature-metric-value">{{ item.participants }}</span>
                  </div>
                  <div class="feature-metric-cell">
                    <span class="feature-metric-name">完成率</span>
                    <span class="feature-metric-value">{{ item.completionRate }}%</span>
                  </div>
                  <div class="feature-metric-cell">
                    <span class="feature-metric-name">效果评分</span>
                    <span class="feature-metric-value">{{ item.effectScore }}</span>
                  </div>
                  <div class="feature-metric-cell">
                    <span class="feature-metric-name">训练类型</span>
                    <span class="feature-metric-value">{{ item.type }}</span>
                  </div>
                </div>
                <p class="training-summary">{{ item.summary }}</p>
                <div class="training-tags">
                  <span
                    v-for="tag in item.tags"
                    :key="tag"
                    class="training-tag"
                  >
                    {{ tag }}
                  </span>
                </div>
              </article>
            </div>
            <div v-else class="feature-empty">
              <el-empty description="未找到匹配的训练任务" />
            </div>
          </section>
        </div>
      </main>
    </div>

    <!-- 竞赛详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="currentCompetition?.competitionName"
      width="800px"
    >
      <div class="detail-content" v-if="currentCompetition">
        <div class="detail-item">
          <span class="detail-label">竞赛类型：</span>
          <span class="detail-value">{{ currentCompetition.competitionType }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">报名时间：</span>
          <span class="detail-value">
            {{ formatDate(currentCompetition.registerStartTime) }} ~ 
            {{ formatDate(currentCompetition.registerEndTime) }}
          </span>
        </div>
        <div class="detail-item">
          <span class="detail-label">竞赛描述：</span>
          <span class="detail-value">{{ currentCompetition.description }}</span>
        </div>
      </div>
    </el-dialog>

    <!-- 评审详情弹窗 -->
    <el-dialog
      v-model="reviewDialogVisible"
      :title="currentReviewCompetition ? `评审管理 - ${currentReviewCompetition.competitionName}` : '评审管理'"
      width="1200px"
      :close-on-click-modal="false"
      @close="handleReviewDialogClose"
    >
      <div v-if="currentReviewCompetition" class="review-detail-content">
        <!-- 竞赛信息 -->
        <div class="review-competition-info">
          <div class="info-row">
            <span class="info-label">竞赛名称：</span>
            <span class="info-value">{{ currentReviewCompetition.competitionName }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">报名时间：</span>
            <span class="info-value">
              {{ formatDate(currentReviewCompetition.registerStartTime) }} ~ 
              {{ formatDate(currentReviewCompetition.registerEndTime) }}
            </span>
          </div>
          <div class="info-row">
            <span class="info-label">评审进度：</span>
            <span class="info-value">
              {{ currentReviewCompetition.reviewedCount || 0 }}/{{ currentReviewCompetition.totalTeams || 0 }} 队伍
            </span>
          </div>
        </div>

        <!-- 队伍列表 -->
        <div class="review-teams-section">
          <h3 class="section-subtitle">参赛队伍列表</h3>
          <div v-if="loadingTeams" class="loading-container">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <div v-else-if="reviewTeams.length === 0" class="empty-container">
            <span>暂无参赛队伍</span>
          </div>
          <div v-else class="teams-list">
            <div
              v-for="team in reviewTeams"
              :key="team.teamId"
              class="team-item"
              :class="{ 'reviewed': team.reviewed }"
            >
              <div class="team-info">
                <div class="team-name">{{ team.teamName || '未命名队伍' }}</div>
                <div class="team-meta">
                  <span class="meta-item">成员数：{{ getTeamMemberCount(team) }}</span>
                  <span class="meta-item" v-if="team.advisor">指导老师：{{ team.advisor }}</span>
                </div>
              </div>
              <div class="team-actions">
                <el-tag v-if="isTeamReviewed(team)" type="success" size="small">已评审</el-tag>
                <el-tag v-else type="info" size="small">未评审</el-tag>
                <el-button type="primary" size="small" @click="goToReview(team)">
                  开始批阅
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 队伍详情弹窗 -->
    <el-dialog
      v-model="teamDetailDialogVisible"
      :title="currentTeam ? `队伍详情 - ${currentTeam.teamName || '未命名队伍'}` : '队伍详情'"
      width="900px"
      :close-on-click-modal="false"
    >
      <div v-if="currentTeam" class="team-detail-content">
        <div v-if="loadingTeamDetail" class="loading-container">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        <div v-else>
          <!-- 队伍基本信息 -->
          <div class="team-basic-info">
            <h4 class="info-title">队伍信息</h4>
            <div class="info-grid">
              <div class="info-item">
                <span class="item-label">队伍名称：</span>
                <span class="item-value">{{ currentTeam.teamName || '未命名队伍' }}</span>
              </div>
              <div class="info-item">
                <span class="item-label">所属竞赛：</span>
                <span class="item-value">{{ currentTeam.competitionName || '未识别' }}</span>
              </div>
              <div class="info-item" v-if="currentTeam.advisor">
                <span class="item-label">指导老师：</span>
                <span class="item-value">{{ currentTeam.advisor }}</span>
              </div>
              <div class="info-item">
                <span class="item-label">成员数量：</span>
                <span class="item-value">{{ getTeamMemberCount(currentTeam) }} 人</span>
              </div>
              <div class="info-item">
                <span class="item-label">提交状态：</span>
                <span class="item-value">{{ normalizeSubmissionStatus(currentTeam) }}</span>
              </div>
              <div class="info-item">
                <span class="item-label">提交时间：</span>
                <span class="item-value">{{ formatDate(currentTeam.submitTime) || '未提交' }}</span>
              </div>
              <div class="info-item">
                <span class="item-label">最终成绩：</span>
                <span class="item-value">{{ currentTeam.finalScore ?? '暂无' }}</span>
              </div>
            </div>
          </div>

          <!-- 队伍成员 -->
          <div class="team-members-section">
            <h4 class="info-title">队伍成员</h4>
            <div class="members-list">
              <div
                v-for="(member, index) in currentTeam.members"
                :key="index"
                class="member-card"
              >
                <div class="member-info">
                  <div class="member-header">
                    <span class="member-name">{{ member.name || '未填写' }}</span>
                    <span 
                      v-if="member.status" 
                      :class="['member-status', member.status]"
                    >
                      {{ member.status === 'pending' ? '待同意' : member.status === 'approved' ? '已加入' : member.status }}
                    </span>
                  </div>
                  <div class="member-details">
                    <span v-if="member.studentNo">学号：{{ member.studentNo }}</span>
                    <span v-if="member.major">专业：{{ member.major }}</span>
                    <span v-if="member.college">学院：{{ member.college }}</span>
                    <span v-if="member.phone">电话：{{ member.phone }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 参赛材料 -->
          <div class="team-materials-section">
            <h4 class="info-title">参赛材料</h4>
            <div class="materials-list">
              <div
                v-for="file in materialFiles"
                :key="`${file.type || 'file'}-${file.url}`"
                class="material-item"
              >
                <el-icon><Document /></el-icon>
                <button class="material-name material-preview-link" type="button" @click="previewMaterial(file)">
                  {{ file.name || getFileName(file.url) }}
                </button>
                <el-tag size="small" type="info">{{ file.type || '作品文件' }}</el-tag>
                <el-button type="success" size="small" @click="downloadMaterial(file)">
                  下载
                </el-button>
              </div>
              <div v-if="!materialFiles.length" class="empty-materials">
                <span>暂无参赛材料</span>
              </div>
            </div>
          </div>

          <!-- 评分记录 -->
          <div class="team-scores-section">
            <h4 class="info-title">评分记录</h4>
            <div v-if="teamScores.length > 0" class="scores-list">
              <div
                v-for="score in teamScores"
                :key="score.scoreId"
                class="score-item"
              >
                <div class="score-info">
                  <div class="score-value">分数：{{ score.score }}</div>
                  <div class="score-reviewer">评审人：{{ score.reviewerName }}</div>
                  <div class="score-time">时间：{{ formatDate(score.scoreTime) }}</div>
                  <div v-if="score.comment" class="score-comment">评语：{{ score.comment }}</div>
                </div>
                <div v-if="currentTeam?.canReview" class="score-actions">
                  <el-button type="primary" size="small" @click="editScore(score)">编辑</el-button>
                  <el-button type="danger" size="small" @click="deleteScoreRecord(score.scoreId)">删除</el-button>
                </div>
              </div>
            </div>
            <div v-else class="empty-scores">
              <span>暂无评分记录</span>
            </div>
              <el-button v-if="currentTeam?.canReview" type="primary" @click="openScoreForm">添加评分</el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 评分表单弹窗 -->
    <el-dialog
      v-model="scoreFormVisible"
      :title="editingScore ? '编辑评分' : '添加评分'"
      width="600px"
    >
      <el-form :model="scoreForm" label-width="100px">
        <el-form-item label="分数" required>
          <el-input-number
            v-model="scoreForm.score"
            :min="0"
            :max="100"
            :precision="1"
            placeholder="请输入分数（0-100）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="评语">
          <el-input
            v-model="scoreForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入评语"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scoreFormVisible = false">取消</el-button>
        <el-button type="primary" @click="submitScore">确定</el-button>
      </template>
    </el-dialog>

    <!-- 材料预览弹窗 -->
    <el-dialog
      v-model="materialPreviewVisible"
      :title="currentPreviewFile?.name || '材料预览'"
      width="82vw"
      class="material-preview-dialog"
      append-to-body
      destroy-on-close
      @closed="destroyMaterialPreview"
    >
      <div v-loading="materialPreviewLoading" class="material-preview-body">
        <div v-if="materialPreviewType === 'image'" class="material-preview-image">
          <el-image :src="materialPreviewUrl" fit="contain" :preview-src-list="[materialPreviewUrl]" />
        </div>
        <video
          v-else-if="materialPreviewType === 'video'"
          :src="materialPreviewUrl"
          controls
          class="material-preview-video"
        ></video>
        <div v-else-if="materialPreviewType === 'office'" id="teacher-material-onlyoffice" class="material-preview-office"></div>
        <el-empty v-else description="该文件格式暂不支持在线预览，请下载查看" />
      </div>
      <template #footer>
        <el-button @click="materialPreviewVisible = false">关闭</el-button>
        <el-button v-if="currentPreviewFile?.url" type="primary" @click="downloadMaterial(currentPreviewFile)">下载</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch, onBeforeUnmount, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  User, Setting, Trophy, Clock, UserFilled, Star, 
  ArrowUp, ArrowDown, Search, Check, Calendar, Loading,
  TrendCharts, Promotion, Document, Collection
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import useUserStore from '@/store/modules/user'
import { getInfo as getLoginInfo } from '@/api/login'
import { 
  listTeacherCompetitions, 
  getReviewableCompetitions, 
  getCompetitionStats,
  getReviewTeams,
  getReviewTeamDetail,
  getTeamScores,
  submitScore as submitScoreApi,
  updateScore as updateScoreApi,
  deleteScore as deleteScoreApi,
  getMyTeamCompetitions
} from '@/api/teacher'
import request from '@/utils/request'
import { proxyFileUrl } from '@/utils/media'
import { PARTICIPATION_STATUS } from '@/utils/competitionStatus'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const activeMenu = ref(route.query.menu || 'competition')

const menuItems = [
  { key: 'competition', label: '赛事概览查看', icon: TrendCharts },
  { key: 'review', label: '评审管理', icon: Document },
  { key: 'my-teams', label: '我的队伍赛事', icon: UserFilled },
  { key: 'mock-test', label: '竞赛模拟测试', icon: Promotion },
  { key: 'smart-training', label: '智能训练出题', icon: Collection }
]

// 统计数据
const stats = ref({
  totalCompetitions: 0,
  competitionIncrease: 0,
  inProgress: 0,
  endingSoon: 0,
  avgParticipation: 0,
  participationIncrease: 0,
  avgScore: 0,
  scoreDecrease: 0
})

// 竞赛列表
const competitions = ref([])
const searchKeyword = ref('')
const filterStatus = ref('')
const pageNum = ref(1)
const pageSize = ref(9)
const hasMore = ref(true)
const loadingMore = ref(false)
const listContainerRef = ref(null)
const totalCompetitionsCount = ref(0) // 保存后端返回的总竞赛数

// 可评审竞赛列表
const reviewableCompetitions = ref([])
const loadingReviewable = ref(false)

// 老师基本信息（静态）
const teacherInfo = ref({
  name: '教师',
  position: '未填写职称',
  department: '未填写院系',
  email: '未填写邮箱',
  phone: '未填写手机号'
})

// 我的队伍赛事
const myTeamCompetitions = ref([])
const loadingMyTeams = ref(false)
const myTeamsKeyword = ref('')
const myTeamsStatusFilter = ref('')

const mockTestTab = ref('published')
const mockTestKeyword = ref('')
const mockTestStatusFilter = ref('')
const mockTestTabs = [
  { key: 'published', label: '已发布赛事' },
  { key: 'analysis', label: '竞赛分析' },
  { key: 'score', label: '成绩分析' }
]

const mockPublishedEvents = ref([
  {
    id: 1,
    name: '蓝桥杯算法模拟赛',
    status: 'ended',
    category: '省赛冲刺',
    time: '2026-05-08 09:00 - 13:00',
    teams: 32,
    submissionRate: 88,
    questionCount: 6,
    avgScore: 82.6,
    duration: '4小时',
    focus: '动态规划 / 搜索'
  },
  {
    id: 2,
    name: '数字建模模拟测试',
    status: 'in_progress',
    category: '建模实战',
    time: '2026-05-16 08:00 - 12:00',
    teams: 28,
    submissionRate: 67,
    questionCount: 3,
    avgScore: 76.4,
    duration: '4小时',
    focus: '数据处理 / 报告表达'
  },
  {
    id: 3,
    name: 'ACM 程序设计模拟赛',
    status: 'not_started',
    category: '校队选拔',
    time: '2026-05-20 14:00 - 19:00',
    teams: 24,
    submissionRate: 0,
    questionCount: 10,
    avgScore: 0,
    duration: '5小时',
    focus: '图论 / 字符串'
  },
  {
    id: 4,
    name: '创新创业商业计划模拟答卷',
    status: 'ended',
    category: '项目路演',
    time: '2026-05-05 19:00 - 21:30',
    teams: 18,
    submissionRate: 94,
    questionCount: 4,
    avgScore: 85.2,
    duration: '150分钟',
    focus: '项目包装 / 财务测算'
  },
  {
    id: 5,
    name: '电子设计赛题预演测试',
    status: 'in_progress',
    category: '赛题预演',
    time: '2026-05-18 13:30 - 17:30',
    teams: 21,
    submissionRate: 72,
    questionCount: 5,
    avgScore: 79.5,
    duration: '4小时',
    focus: '电路设计 / 文档规范'
  }
])

const mockParticipationTrendData = [
  { label: '第1场', value: 18 },
  { label: '第2场', value: 24 },
  { label: '第3场', value: 29 },
  { label: '第4场', value: 26 },
  { label: '第5场', value: 32 }
]

const mockScoreBandsBase = [
  { label: '90分以上', count: 12, color: 'linear-gradient(90deg, #10b981 0%, #34d399 100%)' },
  { label: '80-89分', count: 21, color: 'linear-gradient(90deg, #3b82f6 0%, #60a5fa 100%)' },
  { label: '70-79分', count: 18, color: 'linear-gradient(90deg, #6366f1 0%, #8b5cf6 100%)' },
  { label: '60-69分', count: 9, color: 'linear-gradient(90deg, #f59e0b 0%, #fbbf24 100%)' },
  { label: '60分以下', count: 4, color: 'linear-gradient(90deg, #ef4444 0%, #f87171 100%)' }
]

const trainingKeyword = ref('')
const trainingTypeFilter = ref('')
const trainingTrendData = [
  { label: '第1周', value: 7.2 },
  { label: '第2周', value: 7.5 },
  { label: '第3周', value: 7.8 },
  { label: '第4周', value: 8.0 },
  { label: '第5周', value: 8.1 },
  { label: '第6周', value: 8.4 }
]

const publishedTrainingList = ref([
  {
    id: 1,
    title: '算法竞赛-动态规划专项',
    competitionName: '蓝桥杯算法模拟赛',
    publishTime: '2026-05-15 14:00',
    duration: '90分钟',
    difficulty: '中等',
    participants: 48,
    completionRate: 82,
    effectScore: 8.9,
    type: '算法专项',
    status: '已发布',
    summary: '围绕动态规划、背包问题和状态设计发布分层训练，适合作为赛前强化。',
    tags: ['动态规划', '状态压缩', '赛前强化']
  },
  {
    id: 2,
    title: '数学建模-数据清洗与可视化',
    competitionName: '数字建模模拟测试',
    publishTime: '2026-05-14 09:30',
    duration: '75分钟',
    difficulty: '中高',
    participants: 36,
    completionRate: 74,
    effectScore: 8.1,
    type: '建模实战',
    status: '进行中',
    summary: '补足数据清洗、图表呈现和结论表达的训练短板，提升建模报告完成度。',
    tags: ['数据清洗', '可视化', '报告表达']
  },
  {
    id: 3,
    title: '程序设计-图论冲刺训练',
    competitionName: 'ACM 程序设计模拟赛',
    publishTime: '2026-05-13 19:00',
    duration: '110分钟',
    difficulty: '高',
    participants: 29,
    completionRate: 69,
    effectScore: 7.8,
    type: '赛前冲刺',
    status: '已发布',
    summary: '针对最短路、最小生成树与拓扑排序题型进行高强度冲刺训练。',
    tags: ['图论', '冲刺', '复杂度优化']
  },
  {
    id: 4,
    title: '双创项目-商业计划书复盘',
    competitionName: '创新创业商业计划模拟答卷',
    publishTime: '2026-05-12 16:00',
    duration: '60分钟',
    difficulty: '中等',
    participants: 41,
    completionRate: 86,
    effectScore: 8.6,
    type: '真题复盘',
    status: '已结束',
    summary: '通过往届商业计划书案例复盘，帮助团队梳理市场分析和财务测算表达。',
    tags: ['真题复盘', '商业计划书', '路演准备']
  }
])

// 计算所有队伍
const allTeams = computed(() => {
  let teams = []
  myTeamCompetitions.value.forEach(comp => {
    if (comp.teams && Array.isArray(comp.teams)) {
      teams = [...teams, ...comp.teams.map(team => ({
        ...team,
        competitionId: comp.competitionId,
        competitionName: comp.competitionName
      }))]
    }
  })
  return teams
})

// 计算总队伍数
const totalTeamsCount = computed(() => {
  return allTeams.value.length
})

const myTeamStats = computed(() => ({
  competitionCount: myTeamCompetitions.value.length,
  teamCount: allTeams.value.length,
  submittedCount: allTeams.value.filter(team => normalizeSubmissionStatus(team) !== '未提交').length
}))

const filteredMyTeamCompetitions = computed(() => {
  const keyword = (myTeamsKeyword.value || '').trim().toLowerCase()
  return myTeamCompetitions.value
    .map(comp => {
      const teams = (comp.teams || []).filter(team => {
        const matchesKeyword = !keyword
          || (comp.competitionName || '').toLowerCase().includes(keyword)
          || (team.teamName || '').toLowerCase().includes(keyword)
        if (!matchesKeyword) {
          return false
        }
        if (!myTeamsStatusFilter.value) {
          return true
        }
        if (myTeamsStatusFilter.value === '已评分') {
          return isTeamReviewed(team)
        }
        return normalizeSubmissionStatus(team) === myTeamsStatusFilter.value
      })
      return { ...comp, teams }
    })
    .filter(comp => comp.teams.length > 0)
})

const filteredMockPublishedEvents = computed(() => {
  const keyword = (mockTestKeyword.value || '').trim().toLowerCase()
  return mockPublishedEvents.value.filter(event => {
    const matchesKeyword = !keyword || event.name.toLowerCase().includes(keyword)
    const matchesStatus = !mockTestStatusFilter.value || event.status === mockTestStatusFilter.value
    return matchesKeyword && matchesStatus
  })
})

const mockTestSummary = computed(() => {
  const total = mockPublishedEvents.value.length
  const inProgress = mockPublishedEvents.value.filter(item => item.status === 'in_progress').length
  const notStarted = mockPublishedEvents.value.filter(item => item.status === 'not_started').length
  const ended = mockPublishedEvents.value.filter(item => item.status === 'ended').length
  const totalTeams = mockPublishedEvents.value.reduce((sum, item) => sum + item.teams, 0)
  const avgSubmission = total
    ? (mockPublishedEvents.value.reduce((sum, item) => sum + item.submissionRate, 0) / total).toFixed(1)
    : '0.0'
  const scoreEvents = mockPublishedEvents.value.filter(item => item.avgScore > 0)
  const avgScore = scoreEvents.length
    ? (scoreEvents.reduce((sum, item) => sum + item.avgScore, 0) / scoreEvents.length).toFixed(1)
    : '0.0'
  return { total, inProgress, notStarted, ended, totalTeams, avgSubmission, avgScore }
})

const mockStatusDistribution = computed(() => {
  const total = mockPublishedEvents.value.length || 1
  const config = [
    { key: 'not_started', label: '未开始', color: 'linear-gradient(90deg, #60a5fa 0%, #3b82f6 100%)' },
    { key: 'in_progress', label: '进行中', color: 'linear-gradient(90deg, #fbbf24 0%, #f59e0b 100%)' },
    { key: 'ended', label: '已结束', color: 'linear-gradient(90deg, #34d399 0%, #10b981 100%)' }
  ]
  return config.map(item => {
    const count = mockPublishedEvents.value.filter(event => event.status === item.key).length
    return {
      ...item,
      count,
      percent: Math.max(12, Math.round((count / total) * 100))
    }
  })
})

const mockCompetitionInsights = computed(() => {
  const maxTeams = Math.max(...mockPublishedEvents.value.map(item => item.teams), 1)
  return mockPublishedEvents.value
    .map(item => ({
      id: item.id,
      name: item.name,
      value: `${item.submissionRate}%`,
      percent: Math.max(18, Math.round((item.teams / maxTeams) * 100)),
      color: item.status === 'ended'
        ? 'linear-gradient(90deg, #10b981 0%, #34d399 100%)'
        : item.status === 'in_progress'
          ? 'linear-gradient(90deg, #f59e0b 0%, #fbbf24 100%)'
          : 'linear-gradient(90deg, #6366f1 0%, #8b5cf6 100%)',
      caption: `参与 ${item.teams} 队，平均成绩 ${item.avgScore || '--'}`
    }))
    .sort((a, b) => Number(b.value.replace('%', '')) - Number(a.value.replace('%', '')))
    .slice(0, 3)
})

const mockPrioritySummary = computed(() => ({
  highEngagement: `${mockPublishedEvents.value.filter(item => item.teams >= 30).length} 场`,
  needFollowUp: `${mockPublishedEvents.value.filter(item => item.submissionRate > 0 && item.submissionRate < 70).length} 场`,
  highScore: `${mockPublishedEvents.value.filter(item => item.avgScore >= 80).length} 场`
}))

const mockScoreBands = computed(() => {
  const total = mockScoreBandsBase.reduce((sum, item) => sum + item.count, 0) || 1
  return mockScoreBandsBase.map(item => ({
    ...item,
    percent: Math.max(12, Math.round((item.count / total) * 100))
  }))
})

const mockScoreRankings = computed(() => {
  return mockPublishedEvents.value
    .filter(item => item.avgScore > 0)
    .slice()
    .sort((a, b) => b.avgScore - a.avgScore)
    .map((item, index) => ({
      id: item.id,
      rank: index + 1,
      name: item.name,
      teams: item.teams,
      submissionRate: item.submissionRate,
      avgScore: item.avgScore.toFixed(1)
    }))
})

const trainingTypeOptions = computed(() => {
  return [...new Set(publishedTrainingList.value.map(item => item.type))]
})

const filteredTrainingList = computed(() => {
  const keyword = (trainingKeyword.value || '').trim().toLowerCase()
  return publishedTrainingList.value.filter(item => {
    const matchesKeyword = !keyword
      || item.title.toLowerCase().includes(keyword)
      || item.competitionName.toLowerCase().includes(keyword)
    const matchesType = !trainingTypeFilter.value || item.type === trainingTypeFilter.value
    return matchesKeyword && matchesType
  })
})

const trainingDashboard = computed(() => {
  const total = publishedTrainingList.value.length || 1
  const completionRate = (publishedTrainingList.value.reduce((sum, item) => sum + item.completionRate, 0) / total).toFixed(0)
  const avgDuration = (
    publishedTrainingList.value.reduce((sum, item) => sum + Number(item.duration.replace('分钟', '')), 0) / total
  ).toFixed(0)
  const effectScore = (
    publishedTrainingList.value.reduce((sum, item) => sum + item.effectScore, 0) / total
  ).toFixed(1)
  const bestDuration = Math.min(...publishedTrainingList.value.map(item => Number(item.duration.replace('分钟', ''))))
  return {
    completionRate,
    completionDelta: 12,
    avgDuration,
    bestDuration,
    effectScore,
    effectDelta: 15
  }
})

const trainingTypeDistribution = computed(() => {
  const total = publishedTrainingList.value.length || 1
  const colorMap = {
    '算法专项': 'linear-gradient(90deg, #6366f1 0%, #8b5cf6 100%)',
    '建模实战': 'linear-gradient(90deg, #3b82f6 0%, #60a5fa 100%)',
    '赛前冲刺': 'linear-gradient(90deg, #f59e0b 0%, #fbbf24 100%)',
    '真题复盘': 'linear-gradient(90deg, #10b981 0%, #34d399 100%)'
  }
  return trainingTypeOptions.value.map(label => {
    const count = publishedTrainingList.value.filter(item => item.type === label).length
    return {
      label,
      count,
      percent: Math.max(18, Math.round((count / total) * 100)),
      color: colorMap[label] || 'linear-gradient(90deg, #94a3b8 0%, #cbd5e1 100%)'
    }
  })
})

const myTeamsEmptyDescription = computed(() => {
  if (teacherInfo.value.phone === '未填写手机号') {
    return '当前老师账号未维护手机号'
  }
  return '暂无指导队伍'
})

function buildTrendPlot(series, width = 420, height = 180, baseline = 160) {
  if (!series.length) return []
  const paddingX = 28
  const paddingTop = 20
  const paddingBottom = height - baseline
  const usableWidth = width - paddingX * 2
  const usableHeight = height - paddingTop - paddingBottom
  const values = series.map(item => Number(item.value) || 0)
  const max = Math.max(...values, 1)
  const min = Math.min(...values, 0)
  const range = max === min ? 1 : max - min

  return series.map((item, index) => {
    const value = Number(item.value) || 0
    const x = series.length === 1 ? width / 2 : paddingX + (usableWidth * index) / (series.length - 1)
    const y = paddingTop + usableHeight - ((value - min) / range) * usableHeight
    return { ...item, x: Number(x.toFixed(2)), y: Number(y.toFixed(2)) }
  })
}

function toPolyline(points) {
  return points.map(point => `${point.x},${point.y}`).join(' ')
}

function toArea(points, baseline = 160) {
  if (!points.length) return ''
  const first = points[0]
  const last = points[points.length - 1]
  return `${first.x},${baseline} ${toPolyline(points)} ${last.x},${baseline}`
}

const mockParticipationPlot = computed(() => buildTrendPlot(mockParticipationTrendData))
const mockParticipationLine = computed(() => toPolyline(mockParticipationPlot.value))
const mockParticipationArea = computed(() => toArea(mockParticipationPlot.value))

const trainingTrendPlot = computed(() => buildTrendPlot(trainingTrendData))
const trainingTrendLine = computed(() => toPolyline(trainingTrendPlot.value))
const trainingTrendArea = computed(() => toArea(trainingTrendPlot.value))

const myTeamsEmptyTip = computed(() => {
  if (teacherInfo.value.phone === '未填写手机号') {
    return '请先在教师账号中补充手机号，系统会按手机号匹配指导队伍。'
  }
  return '当前手机号尚未关联任何指导队伍，或队伍还未完成报名。'
})

// 详情弹窗
const detailDialogVisible = ref(false)
const currentCompetition = ref(null)

// 评审管理相关
const reviewDialogVisible = ref(false)
const currentReviewCompetition = ref(null)
const reviewTeams = ref([])
const currentTeam = ref(null)
const teamDetailDialogVisible = ref(false)
const loadingTeams = ref(false)
const loadingTeamDetail = ref(false)
const scoreForm = ref({
  participationId: null,
  score: null,
  comment: ''
})
const scoreFormVisible = ref(false)
const editingScore = ref(null) // 正在编辑的评分ID
const teamScores = ref([]) // 当前队伍的评分记录
const materialPreviewVisible = ref(false)
const materialPreviewLoading = ref(false)
const currentPreviewFile = ref(null)
const materialPreviewType = ref('')
const materialPreviewUrl = ref('')
const materialDocEditor = shallowRef(null)

const materialFiles = computed(() => {
  const team = currentTeam.value || {}
  const files = Array.isArray(team.files) ? team.files.filter(file => file && file.url) : []
  if (files.length > 0) {
    return uniqueMaterialFiles(files)
  }

  const fallback = []
  addLegacyMaterial(fallback, team.pptPath, 'ppt')
  splitMaterialPaths(team.pdfPath).forEach(path => addLegacyMaterial(fallback, path, guessFileType(path)))
  return uniqueMaterialFiles(fallback)
})

// 筛选后的竞赛列表（前端筛选，仅用于显示）
const filteredCompetitions = computed(() => {
  let result = competitions.value

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(comp => 
      comp.competitionName.toLowerCase().includes(keyword)
    )
  }

  // 状态筛选
  if (filterStatus.value) {
    result = result.filter(comp => comp.status === filterStatus.value)
  }

  return result
})

function parseTeamMembers(teamMembers) {
  if (!teamMembers) return []
  if (Array.isArray(teamMembers)) return teamMembers
  try {
    const parsed = typeof teamMembers === 'string' ? JSON.parse(teamMembers) : teamMembers
    return Array.isArray(parsed) ? parsed : []
  } catch (e) {
    return []
  }
}

function getTeamMemberCount(team) {
  if (typeof team?.memberCount === 'number') return team.memberCount
  return parseTeamMembers(team?.teamMembers).length
}

function isTeamReviewed(team) {
  return team?.reviewed === true
    || team?.submissionStatus === PARTICIPATION_STATUS.SCORED
    || (team?.finalScore !== null && team?.finalScore !== undefined)
}

function normalizeSubmissionStatus(team) {
  if (team?.submissionStatus === PARTICIPATION_STATUS.SCORED || team?.reviewed === true) {
    return PARTICIPATION_STATUS.SCORED
  }
  if (team?.submissionStatus === PARTICIPATION_STATUS.SUBMITTED || team?.submitTime) {
    return PARTICIPATION_STATUS.SUBMITTED
  }
  return PARTICIPATION_STATUS.NOT_SUBMITTED
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 获取状态样式类
function getStatusClass(status) {
  const statusMap = {
    'not_started': 'status-not-started',
    'in_progress': 'status-in-progress',
    'ended': 'status-ended'
  }
  return statusMap[status] || ''
}

// 获取状态文本
function getStatusText(status) {
  const statusMap = {
    'not_started': '未开始',
    'in_progress': '进行中',
    'ended': '已结束'
  }
  return statusMap[status] || '未知'
}

function notifyStaticAction(action) {
  ElMessage.info(`${action}为静态演示功能，当前页面暂未接入后端。`)
}

// 搜索处理
function handleSearch() {
  // 重置分页并重新加载
  loadCompetitions(true)
}

// 筛选处理
function handleFilter() {
  // 重置分页并重新加载
  loadCompetitions(true)
}

// 查看竞赛详情
function viewCompetitionDetail(comp) {
  currentCompetition.value = comp
  detailDialogVisible.value = true
}

// 开始评审
function startReview(comp) {
  console.log('[TeacherStartReview] clicked', {
    competitionId: comp?.competitionId,
    competitionName: comp?.competitionName
  })
  router.push({
    path: `/teacher/review/${comp.competitionId}/detail`,
    query: { autoStart: '1' }
  })
}

// 查看评审详情
function viewReviewDetail(comp) {
  router.push(`/teacher/review/${comp.competitionId}/detail`)
}

// 加载评审队伍列表 (This function is no longer used by startReview but kept if needed by other logic)
function loadReviewTeams(competitionId) {
  loadingTeams.value = true
  getReviewTeams(competitionId)
    .then(res => {
      reviewTeams.value = (res.data || []).map(team => ({
        ...team,
        reviewed: team.reviewed || false
      }))
    })
    .catch(() => {
      ElMessage.error('加载队伍列表失败')
    })
    .finally(() => {
      loadingTeams.value = false
    })
}

// 跳转到评审页面
function goToReview(team) {
  if (!currentReviewCompetition.value) return
  if (!team?.registerId) {
    ElMessage.warning('无法获取评审报名记录')
    return
  }
  router.push(`/teacher/review/${currentReviewCompetition.value.competitionId}/${team.registerId}`)
}

// 查看队伍详情
function viewTeamDetail(team) {
  currentTeam.value = team
  teamDetailDialogVisible.value = true
  loadingTeamDetail.value = true

  const competitionId = team.competitionId || currentReviewCompetition.value?.competitionId
  if (!competitionId) {
    ElMessage.warning('无法获取竞赛信息')
    loadingTeamDetail.value = false
    return
  }

  if (!team?.registerId) {
    ElMessage.warning('无法获取队伍报名记录')
    loadingTeamDetail.value = false
    return
  }

  getReviewTeamDetail(competitionId, team.registerId)
    .then(res => {
      const detail = res.data || {}
      // 解析队伍成员JSON
      if (detail.teamMembers) {
        try {
          const members = typeof detail.teamMembers === 'string' 
            ? JSON.parse(detail.teamMembers) 
            : detail.teamMembers
          currentTeam.value = {
            ...currentTeam.value,
            ...detail,
            members: Array.isArray(members) ? members : [],
            memberCount: Array.isArray(members) ? members.length : getTeamMemberCount(detail)
          }
        } catch (e) {
          currentTeam.value = {
            ...currentTeam.value,
            ...detail,
            members: [],
            memberCount: getTeamMemberCount(detail)
          }
        }
      } else {
        currentTeam.value = {
          ...currentTeam.value,
          ...detail,
          members: [],
          memberCount: getTeamMemberCount(detail)
        }
      }
      
      loadTeamScores()
    })
    .catch(() => {
      ElMessage.error('加载队伍详情失败')
    })
    .finally(() => {
      loadingTeamDetail.value = false
    })
}

// 加载队伍评分记录
function loadTeamScores() {
  if (!currentTeam.value) return
  
  if (!currentTeam.value.registerId) {
    ElMessage.warning('无法获取评分记录对应的报名记录')
    return
  }

  const competitionId = currentTeam.value.competitionId || currentReviewCompetition.value?.competitionId
  if (!competitionId) {
    ElMessage.warning('无法获取评分记录对应的竞赛信息')
    return
  }

  getTeamScores(competitionId, currentTeam.value.registerId)
    .then(res => {
      teamScores.value = res.data || []
    })
    .catch(() => {
      ElMessage.error('加载评分记录失败')
    })
}

// 获取文件名
function getFileName(filePath) {
  if (!filePath) return ''
  const cleanPath = String(filePath).split('?')[0]
  const parts = cleanPath.split('/')
  return parts[parts.length - 1] || filePath
}

function splitMaterialPaths(value) {
  if (!value) return []
  return String(value).split('|').map(item => item.trim()).filter(Boolean)
}

function addLegacyMaterial(list, url, type) {
  if (!url) return
  list.push({
    name: getFileName(url),
    url,
    type: type || guessFileType(url)
  })
}

function uniqueMaterialFiles(files) {
  const seen = new Set()
  const result = []
  files.forEach(file => {
    const url = file?.url
    if (!url) return
    const key = String(url).split('?')[0].trim()
    if (!key || seen.has(key)) return
    seen.add(key)
    const name = file.name || getFileName(url)
    result.push({
      ...file,
      name,
      type: guessFileType(name || url)
    })
  })
  return result
}

function guessFileType(filePath) {
  const name = getFileName(filePath).toLowerCase()
  const ext = name.includes('.') ? name.substring(name.lastIndexOf('.') + 1) : ''
  if (['ppt', 'pptx'].includes(ext)) return 'ppt'
  if (['doc', 'docx', 'pdf'].includes(ext)) return ext
  if (['zip', 'rar', '7z'].includes(ext)) return 'zip'
  return ext || '作品文件'
}

function getPreviewType(file) {
  const name = `${file?.name || getFileName(file?.url)}`.toLowerCase()
  const ext = name.includes('.') ? name.substring(name.lastIndexOf('.') + 1) : ''
  if (['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext)) return 'image'
  if (['mp4', 'webm', 'ogg'].includes(ext)) return 'video'
  if (['doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx', 'pdf'].includes(ext)) return 'office'
  return 'unknown'
}

function loadOnlyOfficeScript() {
  return new Promise((resolve, reject) => {
    if (window.DocsAPI) {
      resolve()
      return
    }
    const script = document.createElement('script')
    script.src = import.meta.env.VITE_APP_ONLYOFFICE_API
    script.onload = () => resolve()
    script.onerror = reject
    document.head.appendChild(script)
  })
}

function destroyMaterialPreview() {
  if (materialDocEditor.value) {
    materialDocEditor.value.destroyEditor()
    materialDocEditor.value = null
  }
  materialPreviewUrl.value = ''
  materialPreviewType.value = ''
  materialPreviewLoading.value = false
}

async function previewMaterial(file) {
  if (!file?.url) {
    ElMessage.warning('材料路径不存在')
    return
  }
  destroyMaterialPreview()
  currentPreviewFile.value = {
    ...file,
    name: file.name || getFileName(file.url)
  }
  materialPreviewType.value = getPreviewType(currentPreviewFile.value)
  materialPreviewVisible.value = true
  materialPreviewLoading.value = true

  try {
    if (['image', 'video'].includes(materialPreviewType.value)) {
      materialPreviewUrl.value = proxyFileUrl(file.url, { name: currentPreviewFile.value.name })
      return
    }

    if (materialPreviewType.value === 'office') {
      materialPreviewUrl.value = file.url
      await loadOnlyOfficeScript()
      const res = await request({
        url: '/common/onlyoffice/config',
        method: 'get',
        params: {
          url: file.url,
          name: currentPreviewFile.value.name
        }
      })
      const config = res.data
      if (!config || !window.DocsAPI) {
        throw new Error('OnlyOffice config unavailable')
      }
      materialPreviewLoading.value = false
      await nextTick()
      materialDocEditor.value = new window.DocsAPI.DocEditor('teacher-material-onlyoffice', {
        width: '100%',
        height: '100%',
        documentType: config.documentType,
        document: config.document,
        editorConfig: config.editorConfig,
        token: config.token
      })
      return
    }
  } catch (e) {
    console.error(e)
    materialPreviewType.value = 'unknown'
    ElMessage.error('预览失败，请下载查看')
  } finally {
    materialPreviewLoading.value = false
  }
}

// 获取负责人首字母
function getLeaderInitial(name) {
  if (!name) return '未'
  return name.charAt(0)
}

function goToReviewFromMyTeams(team) {
  if (!team?.competitionId || !team?.registerId) {
    ElMessage.warning('无法获取评审跳转信息')
    return
  }
  router.push(`/teacher/review/${team.competitionId}/${team.registerId}`)
}

function loadTeacherProfile() {
  getLoginInfo()
    .then(res => {
      const user = res.user || {}
      teacherInfo.value = {
        name: user.nickName || user.userName || '教师',
        position: user.remark || '未填写职称',
        department: user.dept?.deptName || '未填写院系',
        email: user.email || '未填写邮箱',
        phone: user.phonenumber || '未填写手机号'
      }
    })
    .catch(() => {
      teacherInfo.value = {
        ...teacherInfo.value,
        name: userStore.nickName || userStore.name || '教师'
      }
    })
}

// 下载材料
function downloadMaterial(file) {
  const filePath = typeof file === 'string' ? file : file?.url
  const fileName = typeof file === 'string' ? getFileName(file) : (file?.name || getFileName(filePath))
  if (!filePath) {
    ElMessage.warning('材料路径不存在')
    return
  }

  const link = document.createElement('a')
  link.href = proxyFileUrl(filePath, { download: true, name: fileName })
  link.download = fileName
  link.style.display = 'none'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 打开评分表单
function openScoreForm() {
  if (!currentTeam.value || !currentTeam.value.registerId) {
    ElMessage.warning('无法获取队伍信息')
    return
  }
  
  editingScore.value = null
  scoreForm.value = {
    participationId: currentTeam.value.registerId,
    score: null,
    comment: ''
  }
  scoreFormVisible.value = true
}

// 编辑评分
function editScore(score) {
  editingScore.value = score.scoreId
  scoreForm.value = {
    participationId: score.participationId,
    score: score.score,
    comment: score.comment || ''
  }
  scoreFormVisible.value = true
}

// 提交评分
function submitScore() {
  if (scoreForm.value.score === null || scoreForm.value.score === undefined) {
    ElMessage.warning('请输入分数')
    return
  }
  
  if (editingScore.value) {
    // 编辑评分
    updateScoreApi(editingScore.value, {
      score: scoreForm.value.score,
      comment: scoreForm.value.comment
    })
      .then(() => {
        ElMessage.success('评分更新成功')
        scoreFormVisible.value = false
        loadTeamScores()
        // 刷新队伍列表
        if (currentReviewCompetition.value) {
          loadReviewTeams(currentReviewCompetition.value.competitionId)
        }
      })
      .catch(() => {
        ElMessage.error('评分更新失败')
      })
  } else {
    // 新增评分
    submitScoreApi({
      participationId: scoreForm.value.participationId,
      score: scoreForm.value.score,
      comment: scoreForm.value.comment
    })
      .then(() => {
        ElMessage.success('评分提交成功')
        scoreFormVisible.value = false
        loadTeamScores()
        // 刷新队伍列表
        if (currentReviewCompetition.value) {
          loadReviewTeams(currentReviewCompetition.value.competitionId)
        }
      })
      .catch(() => {
        ElMessage.error('评分提交失败')
      })
  }
}

// 删除评分记录
function deleteScoreRecord(scoreId) {
  ElMessageBox.confirm('确定要删除这条评分记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      deleteScoreApi(scoreId)
        .then(() => {
          ElMessage.success('删除成功')
          loadTeamScores()
          // 刷新队伍列表
          if (currentReviewCompetition.value) {
            loadReviewTeams(currentReviewCompetition.value.competitionId)
          }
        })
        .catch(() => {
          ElMessage.error('删除失败')
        })
    })
    .catch(() => {
      // 用户取消
    })
}

// 评审弹窗关闭时的清理
function handleReviewDialogClose() {
  currentReviewCompetition.value = null
  reviewTeams.value = []
  currentTeam.value = null
  teamDetailDialogVisible.value = false
  teamScores.value = []
  scoreFormVisible.value = false
  editingScore.value = null
}

// 加载竞赛列表
function loadCompetitions(reset = false) {
  if (reset) {
    pageNum.value = 1
    competitions.value = []
    hasMore.value = true
  }
  
  if (!hasMore.value && !reset) {
    return
  }

  listTeacherCompetitions({
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    keyword: searchKeyword.value || undefined,
    status: filterStatus.value || undefined
  })
    .then(res => {
      const newCompetitions = (res.rows || res.data || []).map(comp => ({
        ...comp,
        status: getCompetitionStatus(comp)
      }))
      
      if (reset) {
        competitions.value = newCompetitions
      } else {
        competitions.value = [...competitions.value, ...newCompetitions]
      }
      
      // 判断是否还有更多数据
      const total = res.total || 0
      const currentCount = competitions.value.length
      // 如果返回的数据量等于pageSize，可能还有更多数据
      // 如果返回的数据量小于pageSize，说明已经是最后一页
      hasMore.value = newCompetitions.length >= pageSize.value && (total === 0 || currentCount < total)
      
      // 保存总竞赛数（仅在重置时更新，避免分页加载时覆盖）
      if (reset) {
        totalCompetitionsCount.value = total
      }

      ensureCompetitionListFilled()
    })
    .catch(() => {
      ElMessage.error('加载竞赛列表失败')
    })
    .finally(() => {
      loadingMore.value = false
    })
}

// 加载更多
function loadMore() {
  if (loadingMore.value || !hasMore.value) {
    return
  }
  loadingMore.value = true
  pageNum.value++
  loadCompetitions(false)
}

async function ensureCompetitionListFilled() {
  await nextTick()
  const container = listContainerRef.value
  if (!container) return

  let guard = 0
  while (
    hasMore.value &&
    !loadingMore.value &&
    container.scrollHeight <= container.clientHeight + 20 &&
    guard < 10
  ) {
    guard++
    loadMore()
    await nextTick()
  }
}

// 滚动监听，实现上拉加载更多
function handleScroll(event) {
  const container = event.target
  if (!container) return
  
  // 计算是否滚动到底部（距离底部50px内触发）
  const scrollTop = container.scrollTop
  const scrollHeight = container.scrollHeight
  const clientHeight = container.clientHeight
  
  // 当滚动到距离底部50px以内时，触发加载更多
  if (scrollHeight - scrollTop - clientHeight < 50) {
    if (hasMore.value && !loadingMore.value) {
      loadMore()
    }
  }
}

// 加载可评审竞赛列表
function loadReviewableCompetitions() {
  loadingReviewable.value = true
  getReviewableCompetitions()
    .then(res => {
      // 处理返回的数据，确保数据结构正确
      const data = res.data || res.rows || []
      reviewableCompetitions.value = Array.isArray(data) ? data : []
      
      if (reviewableCompetitions.value.length === 0) {
        ElMessage.info('暂无待评审竞赛，请联系管理员分配评审权限')
      }
    })
    .catch((err) => {
      ElMessage.error('加载可评审竞赛失败：' + (err.message || '未知错误'))
      console.error('加载可评审竞赛失败:', err)
      reviewableCompetitions.value = []
    })
    .finally(() => {
      loadingReviewable.value = false
    })
}

// 加载我的队伍赛事
function loadMyTeamCompetitions() {
  loadingMyTeams.value = true
  getMyTeamCompetitions()
    .then(res => {
      const data = res.data || res.rows || []
      myTeamCompetitions.value = Array.isArray(data)
        ? data.map(comp => ({
            ...comp,
            teams: Array.isArray(comp.teams)
              ? comp.teams.map(team => ({
                  ...team,
                  memberCount: getTeamMemberCount(team),
                  submissionStatus: normalizeSubmissionStatus(team),
                  reviewed: isTeamReviewed(team)
                }))
              : []
          }))
        : []
      
      if (myTeamCompetitions.value.length === 0) {
        ElMessage.info('您还没有作为指导老师带领队伍参加任何竞赛')
      }
    })
    .catch((err) => {
      ElMessage.error('加载我的队伍赛事失败：' + (err.message || '未知错误'))
      console.error('加载我的队伍赛事失败:', err)
      myTeamCompetitions.value = []
    })
    .finally(() => {
      loadingMyTeams.value = false
    })
}

// 判断竞赛状态
function getCompetitionStatus(comp) {
  const now = new Date()
  const startTime = new Date(comp.registerStartTime)
  const endTime = new Date(comp.registerEndTime)

  if (now < startTime) {
    return 'not_started'
  } else if (now >= startTime && now <= endTime) {
    return 'in_progress'
  } else {
    return 'ended'
  }
}

// 更新统计数据
function updateStats() {
  // 从后端获取统计数据，确保数据准确
  getCompetitionStats()
    .then(res => {
      const data = res.data || {}
      stats.value.totalCompetitions = data.totalCompetitions || 0
      stats.value.inProgress = data.inProgress || 0
      stats.value.endingSoon = data.endingSoon || 0
      stats.value.avgParticipation = data.avgParticipation || 75
      stats.value.avgScore = data.avgScore || 72.5
      stats.value.competitionIncrease = data.competitionIncrease || 15
      stats.value.participationIncrease = data.participationIncrease || 3
      stats.value.scoreDecrease = data.scoreDecrease || 1.5
    })
    .catch(() => {
      // 如果接口调用失败，使用默认值
      ElMessage.warning('获取统计数据失败，使用默认值')
    })
}

// 监听菜单切换，当切换到相应菜单时自动加载数据
watch(activeMenu, (newMenu) => {
  if (newMenu === 'review') {
    loadReviewableCompetitions()
  } else if (newMenu === 'my-teams') {
    loadMyTeamCompetitions()
  }
})

// 退出登录
function logout() {
  ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logOut().then(() => {
      location.href = '/login'
    })
  }).catch(() => {})
}

function handleCommand(command) {
  if (command === 'profile') {
    router.push('/teacher/profile')
    return
  }
  if (command === 'logout') {
    logout()
  }
}

function switchMenu(menu) {
  activeMenu.value = menu
  router.push({ path: '/teacher', query: { menu } })
  if (menu === 'review') {
    loadReviewableCompetitions()
  } else if (menu === 'my-teams') {
    loadMyTeamCompetitions()
  } else if (menu === 'competition') {
    loadCompetitions(true)
  }
}

watch(() => route.query.menu, (menu) => {
  if (menu === 'review' || menu === 'my-teams' || menu === 'competition' || menu === 'mock-test' || menu === 'smart-training') {
    activeMenu.value = menu
    return
  }
  activeMenu.value = 'competition'
})

onMounted(() => {
  loadTeacherProfile()
  loadOnlyOfficeScript().catch(() => {})
  // 先加载统计数据
  updateStats()
  // 然后加载竞赛列表
  loadCompetitions(true)
  if (activeMenu.value === 'review') {
    loadReviewableCompetitions()
  } else if (activeMenu.value === 'my-teams') {
    loadMyTeamCompetitions()
  }
})

onBeforeUnmount(() => {
  destroyMaterialPreview()
})
</script>

<style scoped>
.teacher-page {
  height: 100vh;
  background-color: #f5f7fa;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Helvetica Neue", Arial, sans-serif;
  margin: 0 !important;
  padding: 0 !important;
  width: 100vw;
  overflow: hidden;
  position: relative;
  left: 0;
  top: 0;
  display: flex;
  flex-direction: column;
}

/* 顶部导航 */
.header-nav {
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
  width: 100%;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-container {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  height: 64px;
  gap: 32px;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
  font-size: 20px;
  font-weight: 600;
  cursor: pointer;
}

.logo-text {
  font-size: 18px;
}

.main-nav {
  display: flex;
  align-items: center;
  gap: 24px;
  flex: 1;
}

.header-nav-item {
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 0;
  position: relative;
  cursor: pointer;
  transition: all 0.3s;
}

.header-nav-item:hover,
.header-nav-item.active {
  color: white;
}

.header-dropdown {
  position: relative;
}

.header-dropdown::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  height: 20px;
  background: transparent;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  margin-top: 8px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 12px;
  min-width: 200px;
  display: none;
  z-index: 1000;
}

.header-dropdown:hover .dropdown-menu {
  display: block;
}

.dropdown-menu a {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  color: #4b5563;
  text-decoration: none;
  border-radius: 4px;
  font-size: 13px;
  transition: all 0.2s;
}

.dropdown-menu a:hover,
.dropdown-menu a.active {
  background: #f3f4f6;
  color: #6366f1;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-container {
  display: flex;
  align-items: center;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-avatar-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid #ffffff;
  box-shadow: 0 0 0 2px rgba(255,255,255,0.3);
}

/* 主容器 */
.main-container {
  display: flex;
  height: calc(100vh - 64px);
  max-height: calc(100vh - 64px);
  width: 100%;
  margin: 0 !important;
  padding: 0 !important;
  position: relative;
  left: 0;
  overflow: hidden;
  flex: 1;
  min-height: 0;
}

/* 左侧导航栏 */
.sidebar {
  width: 240px;
  background: #ffffff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  position: relative;
  flex-shrink: 0;
  margin: 0 !important;
  padding: 0 !important;
  left: 0 !important;
  transform: translateX(0);
}

.sidebar-nav {
  padding: 20px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  cursor: pointer;
  transition: all 0.3s;
  color: #606266;
  position: relative;
}

.nav-item:hover {
  background-color: #f5f7fa;
}

.nav-item.active {
  color: #8b5cf6;
  background-color: #f5f7fa;
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background-color: #8b5cf6;
}

.nav-icon {
  font-size: 18px;
  margin-right: 12px;
}

.nav-text {
  font-size: 14px;
}

/* 主内容区 */
.content-area {
  flex: 1;
  padding: 24px;
  overflow-x: hidden;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  height: 100%;
  max-height: 100%;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  min-height: 0;
}

.content-area::-webkit-scrollbar {
  width: 8px;
}

.content-area::-webkit-scrollbar-track {
  background: #eef2f7;
  border-radius: 999px;
}

.content-area::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 999px;
}

.content-area::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.content-section {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  height: 100%;
  max-height: 100%;
  overflow: hidden;
  box-sizing: border-box;
  padding: 0;
  min-height: 0;
}

.feature-content-section {
  height: auto;
  max-height: none;
  overflow: visible;
  min-height: auto;
  padding-bottom: 28px;
}

.section-header {
  margin-bottom: 20px;
  padding-top: 8px;
  flex-shrink: 0;
}

.my-teams-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.my-teams-header-main {
  min-width: 0;
}

.section-title {
  font-size: 26px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.section-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
  flex-shrink: 0;
}

.stat-card {
  background: #ffffff;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s, box-shadow 0.3s;
  min-height: 90px;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: white;
  margin-right: 16px;
  flex-shrink: 0;
}

.stat-icon.purple {
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
}

.stat-icon.orange {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.stat-icon.green {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 26px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 6px;
  font-weight: 500;
}

.stat-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  line-height: 1.3;
}

.stat-trend.up {
  color: #10b981;
}

.stat-trend.down {
  color: #ef4444;
}

/* 竞赛列表区域 */
.competition-list-section {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px 24px 12px 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
  min-height: 0;
  height: 100%;
  max-height: 100%;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-shrink: 0;
}

.list-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.list-actions {
  display: flex;
  gap: 12px;
}

.search-input {
  width: 300px;
}

.status-select {
  width: 150px;
}

/* 竞赛卡片网格 */
.competition-list-container {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 8px;
  padding-bottom: 40px;
  min-height: 0;
  max-height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.competition-list-container::-webkit-scrollbar {
  width: 6px;
}

.competition-list-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.competition-list-container::-webkit-scrollbar-thumb {
  background: #c0c0c0;
  border-radius: 3px;
}

.competition-list-container::-webkit-scrollbar-thumb:hover {
  background: #a0a0a0;
}

.competition-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 0;
  flex-shrink: 0;
  padding-bottom: 0;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.load-more-tip {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 20px;
  margin-bottom: 20px;
  padding: 16px 0;
  flex-shrink: 0;
  width: 100%;
  color: #6b7280;
  font-size: 14px;
}

.load-more-tip .el-icon {
  font-size: 16px;
}

.competition-card {
  background: #fafafa;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s;
  border: 1px solid #e5e7eb;
}

.competition-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.card-status {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
}

.status-not-started {
  color: #409eff;
  background-color: #ecf5ff;
}

.status-in-progress {
  color: #f59e0b;
  background-color: #fef3c7;
}

.status-ended {
  color: #10b981;
  background-color: #d1fae5;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 12px 0;
}

.card-time {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 16px;
}

.card-metrics {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.metric-item {
  font-size: 13px;
  color: #4b5563;
}

.metric-label {
  color: #6b7280;
}

.metric-value {
  color: #1f2937;
  font-weight: 500;
}

.card-action {
  width: 100%;
}

/* 评审管理 */
.review-competition-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.review-competition-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.review-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.review-card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.review-badge {
  background-color: #d1fae5;
  color: #059669;
  border: none;
}

.review-card-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.info-item {
  font-size: 14px;
  color: #4b5563;
}

.info-label {
  color: #6b7280;
}

.info-value {
  color: #1f2937;
  font-weight: 500;
}

/* 详情弹窗 */
.detail-content {
  padding: 10px 0;
}

.detail-item {
  margin-bottom: 16px;
  font-size: 14px;
}

.detail-label {
  color: #6b7280;
  font-weight: 500;
  margin-right: 8px;
}

.detail-value {
  color: #1f2937;
}

/* 弹窗背景色修改 - 白色背景 */
:deep(.el-dialog) {
  background-color: #ffffff !important;
}

:deep(.el-dialog__header) {
  background-color: #fafafa !important;
  padding: 20px 20px 10px 20px;
  border-bottom: 1px solid #e5e7eb;
}

:deep(.el-dialog__title) {
  color: #1f2937 !important;
  font-weight: 600;
}

:deep(.el-dialog__body) {
  background-color: #ffffff !important;
  color: #1f2937 !important;
  padding: 20px;
}

:deep(.el-dialog__footer) {
  background-color: #fafafa !important;
  padding: 10px 20px 20px 20px;
  border-top: 1px solid #e5e7eb;
}

:deep(.el-dialog__headerbtn) {
  color: #6b7280 !important;
}

:deep(.el-dialog__headerbtn:hover) {
  color: #1f2937 !important;
}

/* 评审管理相关样式 */
.review-detail-content {
  padding: 10px 0;
}

.review-competition-info {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 24px;
}

.info-row {
  display: flex;
  margin-bottom: 12px;
  font-size: 14px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .info-label {
  color: #6b7280;
  font-weight: 500;
  min-width: 100px;
}

.info-row .info-value {
  color: #1f2937;
  flex: 1;
}

.review-teams-section {
  margin-top: 24px;
}

.section-subtitle {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.loading-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
  color: #6b7280;
  gap: 8px;
}

.teams-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.team-item {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s;
}

.team-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.team-item.reviewed {
  background: #f0fdf4;
  border-color: #86efac;
}

.team-info {
  flex: 1;
}

.team-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.team-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #6b7280;
}

.meta-item {
  display: flex;
  align-items: center;
}

.team-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 队伍详情样式 */
.team-detail-content {
  padding: 10px 0;
}

.team-basic-info,
.team-members-section,
.team-materials-section,
.team-scores-section {
  margin-bottom: 24px;
}

.info-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e5e7eb;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-label {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
}

.item-value {
  font-size: 14px;
  color: #1f2937;
}

.members-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.member-card {
  background: #fafafa;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
}

.member-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.member-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.member-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
}

.member-status.pending {
  background: #fff3cd;
  color: #856404;
}

.member-status.approved {
  background: #d4edda;
  color: #155724;
}

.member-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: #6b7280;
}

.materials-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.material-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.material-name {
  flex: 1;
  font-size: 14px;
  color: #1f2937;
  word-break: break-all;
}

.empty-materials,
.empty-scores {
  padding: 20px;
  text-align: center;
  color: #6b7280;
  font-size: 14px;
}

.scores-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.score-item {
  background: #fafafa;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.score-info {
  flex: 1;
}

.score-value {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.score-reviewer,
.score-time {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 4px;
}

.score-comment {
  font-size: 14px;
  color: #1f2937;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #e5e7eb;
}

.score-actions {
  display: flex;
  gap: 8px;
}

.team-scores-section > .el-button {
  display: none;
}

.score-action-buttons {
  margin-top: 16px;
}

/* 可评审竞赛列表样式 */
.review-competition-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.review-competition-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s;
}

.review-competition-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.review-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.review-card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.review-badge {
  flex-shrink: 0;
}

.review-card-info {
  margin-bottom: 16px;
}

.review-card-info .info-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.review-card-info .info-item:last-child {
  margin-bottom: 0;
}

.review-card-info .info-label {
  color: #6b7280;
  min-width: 80px;
}

.review-card-info .info-value {
  color: #1f2937;
  flex: 1;
}

.empty-review-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-tip {
  margin-top: 16px;
  color: #6b7280;
  font-size: 14px;
}

/* 我的队伍赛事样式 */
.my-teams-list-container {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 8px;
  padding-bottom: 40px;
  margin-top: 8px;
  min-height: 0;
  max-height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
}

.my-teams-list-container::-webkit-scrollbar {
  width: 6px;
}

.my-teams-list-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.my-teams-list-container::-webkit-scrollbar-thumb {
  background: #c0c0c0;
  border-radius: 3px;
}

.my-teams-list-container::-webkit-scrollbar-thumb:hover {
  background: #a0a0a0;
}

.my-teams-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.my-teams-toolbar {
  display: flex;
  gap: 12px;
  margin-left: auto;
  align-items: center;
  flex-wrap: wrap;
  flex-shrink: 0;
}

.my-teams-toolbar .search-input {
  width: 300px;
}

.my-teams-toolbar .status-select {
  width: 150px;
}

.my-team-competition-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 34px;
  transition: all 0.3s;
}

.my-team-competition-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.competition-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 22px;
  padding-bottom: 18px;
  border-bottom: 2px solid #f3f4f6;
}

.competition-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.competition-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 10px;
  color: #6b7280;
  font-size: 13px;
}

.competition-info {
  margin-bottom: 20px;
}

.teams-section {
  margin-top: 26px;
}

.teams-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e5e7eb;
}

.team-card {
  background: #fafafa;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 22px;
  margin-bottom: 16px;
}

.my-team-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(430px, 1fr));
  gap: 26px;
}

.team-card:last-child {
  margin-bottom: 0;
}

.team-card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 14px;
}

.team-subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: #6b7280;
}

.team-status-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}

.team-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.team-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.team-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-row {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.detail-label {
  color: #6b7280;
  min-width: 80px;
  font-weight: 500;
}

.detail-value {
  color: #1f2937;
  flex: 1;
}

.team-card-actions {
  margin-top: 16px;
  display: flex;
  gap: 10px;
}

.material-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.no-material {
  color: #9ca3af;
  font-size: 13px;
}

.empty-my-teams {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

/* 老师信息卡片 */
.teacher-info-card {
  background: #fff;
  border-radius: 12px;
  padding: 12px 18px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  margin-bottom: 8px;
}

.teacher-stats-grid {
  margin-top: 8px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.teacher-stat-card {
  background: linear-gradient(135deg, #f8fbff 0%, #eef4ff 100%);
  border: 1px solid #dbeafe;
  border-radius: 12px;
  padding: 10px 14px;
}

.teacher-stat-value {
  font-size: 19px;
  font-weight: 700;
  color: #1d4ed8;
}

.teacher-stat-label {
  margin-top: 2px;
  font-size: 12px;
  color: #64748b;
}

.info-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.avatar-icon {
  font-size: 28px;
}

.personal-info {
  flex: 1;
}

.teacher-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 2px;
}

.teacher-details {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 12px;
}

.detail-item {
  font-size: 12px;
  color: #606266;
  display: flex;
  align-items: center;
  line-height: 1.2;
}

/* 队伍表格 */
.teams-table-container {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
}

.table-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.table-actions {
  display: flex;
  align-items: center;
}

.team-count {
  background: #ecf5ff;
  color: #409eff;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.teams-table {
  width: 100%;
}

.table-header-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr 1fr 100px;
  background: #f5f7fa;
  padding: 12px 24px;
  font-weight: 600;
  color: #606266;
  font-size: 14px;
  border-bottom: 1px solid #ebeef5;
}

.table-header-cell {
  text-align: left;
}

.table-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr 1fr 100px;
  padding: 16px 24px;
  border-bottom: 1px solid #ebeef5;
  align-items: center;
  transition: background-color 0.3s;
}

.table-row:hover {
  background-color: #f5f7fa;
}

.table-cell {
  text-align: left;
  font-size: 14px;
  color: #303133;
}

.leader-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.leader-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #ecf5ff;
  color: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.leader-name {
  font-size: 14px;
  color: #303133;
}

.score-tag {
  background: #ecf5ff;
  color: #409eff;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
}

.material-preview-link {
  flex: 1;
  min-width: 0;
  border: 0;
  padding: 0;
  background: transparent;
  color: #1f2f46;
  cursor: pointer;
  text-align: left;
  font: inherit;
}

.material-preview-link:hover {
  color: #6f49f5;
  text-decoration: underline;
}

.material-preview-dialog :deep(.el-dialog__body) {
  padding-top: 8px;
}

.material-preview-body {
  height: 72vh;
  min-height: 520px;
  display: flex;
  flex-direction: column;
}

.material-preview-frame,
.material-preview-office {
  width: 100%;
  height: 100%;
  flex: 1;
  border: 0;
  background: #fff;
}

.material-preview-office :deep(iframe) {
  width: 100% !important;
  height: 100% !important;
}

.material-preview-image {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.material-preview-image :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.material-preview-video {
  width: 100%;
  max-height: 100%;
  background: #000;
}

.feature-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.feature-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.feature-button-icon {
  margin-right: 6px;
}

.feature-stat-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  margin-bottom: 18px;
}

.dashboard-stat-grid {
  margin-bottom: 22px;
}

.feature-stat-card {
  position: relative;
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 20px 22px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.05);
}

.feature-stat-card::after {
  content: '';
  position: absolute;
  right: -24px;
  top: -24px;
  width: 88px;
  height: 88px;
  border-radius: 50%;
  opacity: 0.18;
}

.feature-stat-card.accent-violet::after {
  background: #8b5cf6;
}

.feature-stat-card.accent-amber::after {
  background: #f59e0b;
}

.feature-stat-card.accent-blue::after {
  background: #3b82f6;
}

.feature-stat-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 12px;
}

.feature-stat-value {
  font-size: 30px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.1;
}

.feature-stat-meta {
  margin-top: 10px;
  color: #475569;
  font-size: 13px;
}

.feature-top-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
  margin-bottom: 18px;
}

.feature-panel {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 22px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.05);
}

.feature-panel-stretch {
  display: flex;
  flex-direction: column;
  min-height: auto;
}

.inner-panel {
  box-shadow: none;
  padding: 0;
  border: 0;
}

.feature-body {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.feature-tabs {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding-bottom: 14px;
  border-bottom: 1px solid #eef2f7;
  flex-wrap: wrap;
}

.feature-tab {
  border: 0;
  background: #f8fafc;
  color: #475569;
  padding: 10px 16px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.feature-tab:hover,
.feature-tab.active {
  background: linear-gradient(135deg, #ede9fe 0%, #dbeafe 100%);
  color: #4f46e5;
  box-shadow: inset 0 0 0 1px rgba(99, 102, 241, 0.12);
}

.panel-head,
.panel-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
}

.panel-head-inline {
  align-items: center;
}

.panel-title {
  margin: 0;
  color: #111827;
  font-size: 18px;
  font-weight: 700;
}

.panel-caption {
  margin: 8px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.update-hint {
  color: #64748b;
  font-size: 13px;
  white-space: nowrap;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.distribution-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.distribution-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.distribution-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #334155;
  font-size: 14px;
}

.distribution-name {
  font-weight: 600;
}

.distribution-value {
  color: #64748b;
}

.distribution-track {
  width: 100%;
  height: 10px;
  border-radius: 999px;
  background: #eef2f7;
  overflow: hidden;
}

.distribution-fill {
  height: 100%;
  border-radius: 999px;
}

.trend-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.trend-svg {
  width: 100%;
  height: 180px;
  display: block;
}

.trend-line {
  fill: none;
  stroke-width: 3;
  stroke-linejoin: round;
  stroke-linecap: round;
}

.trend-area {
  stroke: none;
}

.violet-line {
  stroke: #6366f1;
}

.amber-line {
  stroke: #f59e0b;
}

.trend-dot {
  stroke: #ffffff;
  stroke-width: 2;
}

.violet-dot {
  fill: #6366f1;
}

.amber-dot {
  fill: #f59e0b;
}

.trend-axis {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(48px, 1fr));
  gap: 10px;
  color: #64748b;
  font-size: 12px;
}

.trend-axis span {
  text-align: center;
}

.feature-card-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.training-card-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.feature-card {
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 18px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.feature-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08);
}

.feature-card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.feature-pill,
.training-status,
.training-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  font-size: 12px;
  line-height: 1;
}

.feature-pill {
  padding: 7px 10px;
  background: #eff6ff;
  color: #2563eb;
  font-weight: 600;
}

.feature-card-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #111827;
}

.feature-card-time {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.feature-metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.feature-metric-cell {
  padding: 12px 14px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.feature-metric-name {
  color: #64748b;
  font-size: 12px;
}

.feature-metric-value {
  color: #111827;
  font-size: 16px;
  font-weight: 700;
}

.feature-card-foot {
  margin-top: 14px;
  display: flex;
  justify-content: space-between;
  gap: 10px;
  color: #64748b;
  font-size: 13px;
  flex-wrap: wrap;
}

.analysis-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.analysis-subpanel {
  border: 1px solid #eef2f7;
  border-radius: 14px;
  padding: 18px;
  background: #fcfdff;
}

.insight-list,
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.insight-item {
  padding: 14px;
  border-radius: 14px;
  background: #ffffff;
  border: 1px solid #eef2f7;
}

.insight-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.insight-name {
  font-size: 14px;
  font-weight: 700;
  color: #111827;
}

.insight-value {
  font-size: 14px;
  color: #4f46e5;
  font-weight: 700;
}

.insight-caption {
  margin: 10px 0 0;
  color: #64748b;
  font-size: 13px;
}

.tag-summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.tag-summary-card {
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 16px;
}

.tag-summary-label {
  display: block;
  color: #64748b;
  font-size: 13px;
}

.tag-summary-value {
  display: block;
  margin-top: 10px;
  color: #111827;
  font-size: 24px;
  line-height: 1.1;
}

.tag-summary-caption {
  margin: 10px 0 0;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 14px;
  background: #ffffff;
  border: 1px solid #eef2f7;
}

.ranking-index {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ede9fe 0%, #dbeafe 100%);
  color: #4f46e5;
  font-weight: 700;
  flex-shrink: 0;
}

.ranking-content {
  min-width: 0;
  flex: 1;
}

.ranking-name {
  color: #111827;
  font-size: 14px;
  font-weight: 700;
}

.ranking-meta {
  margin-top: 6px;
  color: #64748b;
  font-size: 12px;
}

.ranking-score {
  color: #0f172a;
  font-size: 22px;
  font-weight: 700;
}

.training-title-group {
  min-width: 0;
}

.training-status {
  padding: 7px 10px;
  background: #dcfce7;
  color: #15803d;
  font-weight: 700;
  white-space: nowrap;
}

.training-meta-line {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 16px;
  color: #64748b;
  font-size: 13px;
  margin-bottom: 14px;
}

.training-summary {
  margin: 14px 0 0;
  color: #475569;
  font-size: 13px;
  line-height: 1.7;
}

.training-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
}

.training-tag {
  padding: 7px 10px;
  background: #f1f5f9;
  color: #475569;
  font-weight: 600;
}

.feature-empty {
  padding: 24px 0 8px;
}

/* 空表格状态 */
.empty-table {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  grid-column: 1 / -1;
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .competition-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .my-teams-header {
    flex-wrap: wrap;
  }

  .feature-card-grid,
  .analysis-grid,
  .feature-top-grid,
  .tag-summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .training-card-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .competition-grid {
    grid-template-columns: 1fr;
  }
  
  .review-competition-list {
    grid-template-columns: 1fr;
  }

  .my-teams-header {
    flex-direction: column;
    align-items: stretch;
  }

  .my-teams-toolbar {
    margin-left: 0;
    width: 100%;
  }

  .my-teams-toolbar .search-input,
  .my-teams-toolbar .status-select {
    width: 100%;
  }

  .nav-container {
    gap: 18px;
    padding: 0 16px;
  }

  .main-nav {
    gap: 14px;
    flex-wrap: wrap;
  }

  .feature-header,
  .panel-head,
  .panel-toolbar,
  .feature-actions,
  .toolbar-actions,
  .feature-card-top {
    flex-direction: column;
    align-items: stretch;
  }

  .feature-stat-grid,
  .feature-top-grid,
  .feature-card-grid,
  .analysis-grid,
  .tag-summary-grid,
  .feature-metric-grid {
    grid-template-columns: 1fr;
  }

  .search-input,
  .status-select {
    width: 100%;
  }

  .ranking-item {
    align-items: flex-start;
  }

  .ranking-score {
    font-size: 18px;
  }
}
</style>

<style>
/* 确保教师页面从最左侧开始，覆盖可能的全局样式 */
body .teacher-page,
body .teacher-page * {
  box-sizing: border-box;
}

body .teacher-page {
  margin: 0 !important;
  padding: 0 !important;
  margin-left: 0 !important;
  padding-left: 0 !important;
  position: relative;
  left: 0 !important;
  transform: translateX(0) !important;
}

body .teacher-page .main-container {
  margin: 0 !important;
  padding: 0 !important;
  margin-left: 0 !important;
  padding-left: 0 !important;
  left: 0 !important;
  transform: translateX(0) !important;
}

body .teacher-page .sidebar {
  margin: 0 !important;
  padding: 0 !important;
  margin-left: 0 !important;
  padding-left: 0 !important;
  left: 0 !important;
  transform: translateX(0) !important;
}

body .teacher-page .header-nav {
  margin: 0 !important;
  margin-left: 0 !important;
  left: 0 !important;
  transform: translateX(0) !important;
}
</style>
