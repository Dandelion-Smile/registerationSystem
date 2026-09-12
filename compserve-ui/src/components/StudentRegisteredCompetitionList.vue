<template>
  <section class="registered-card">
    <div class="registered-card__header">
      <div class="registered-card__title-wrap">
        <span class="registered-card__icon">
          <i class="fa fa-list-ul"></i>
        </span>
        <div>
          <h2 class="registered-card__title">已报名的竞赛</h2>
          <p class="registered-card__subtitle">
            仅展示你已成功提交过报名记录的竞赛，按报名时间倒序排列。
          </p>
        </div>
      </div>
      <div class="registered-card__count">共 {{ items.length }} 项</div>
    </div>

    <div v-if="items.length > 0" class="registered-list">
      <article
        v-for="item in items"
        :key="item.id"
        class="registered-item"
      >
        <div class="registered-item__status">
          <span class="status-pill" :class="item.statusClass">{{ item.statusText }}</span>
        </div>
        <div class="registered-item__body">
          <div class="registered-item__name">
            {{ item.name }}
            <span v-if="item.teamName" class="registered-item__team">
              - {{ item.teamName }}
            </span>
          </div>
          <div class="registered-item__meta">
            <span v-if="item.registerTime">报名时间：{{ item.registerTime }}</span>
          </div>
        </div>
        <div class="registered-item__actions">
          <button class="link-detail" type="button" @click="$emit('detail', item)">
            查看详情
            <i class="fas fa-angle-right"></i>
          </button>
          <button
            v-if="item.registerId && item.canCancel"
            class="link-cancel"
            type="button"
            @click="$emit('cancel', item)"
          >
            <i class="fa fa-times"></i>
            取消报名
          </button>
        </div>
      </article>
    </div>

    <div v-else class="registered-empty">
      暂无报名记录
    </div>
  </section>
</template>

<script setup>
defineProps({
  items: {
    type: Array,
    default: () => [],
  },
});

defineEmits(["detail", "cancel"]);
</script>

<style scoped>
.registered-card {
  background: #ffffff;
  border-radius: 24px;
  border: 1px solid rgba(99, 102, 241, 0.16);
  box-shadow: 0 20px 50px rgba(99, 102, 241, 0.12);
  padding: 28px;
}

.registered-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.registered-card__title-wrap {
  display: flex;
  align-items: center;
  gap: 14px;
}

.registered-card__icon {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eef2ff, #e0e7ff);
  color: #4f46e5;
  font-size: 22px;
}

.registered-card__title {
  margin: 0;
  color: #4338ca;
  font-size: 18px;
  font-weight: 700;
}

.registered-card__subtitle {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.registered-card__count {
  color: #4f46e5;
  font-size: 14px;
  font-weight: 700;
  background: #eef2ff;
  border-radius: 999px;
  padding: 8px 14px;
  white-space: nowrap;
}

.registered-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.registered-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 16px;
  padding: 16px 18px;
  border-radius: 18px;
  background: linear-gradient(180deg, #fcfcff 0%, #f6f7ff 100%);
  border: 1px solid rgba(99, 102, 241, 0.1);
}

.registered-item__body {
  min-width: 0;
}

.registered-item__name {
  color: #1f2937;
  font-size: 17px;
  font-weight: 600;
  line-height: 1.4;
}

.registered-item__team {
  color: #4b5563;
}

.registered-item__meta {
  margin-top: 6px;
  color: #6b7280;
  font-size: 13px;
}

.registered-item__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-pill {
  border-radius: 999px;
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
}

.status-approved {
  background: #dcfce7;
  color: #16a34a;
}

.status-reviewing {
  background: #dbeafe;
  color: #2563eb;
}

.status-pending {
  background: #fef3c7;
  color: #ca8a04;
}

.status-rejected {
  background: #fee2e2;
  color: #dc2626;
}

.link-detail,
.link-cancel {
  border: none;
  background: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  padding: 0;
}

.link-detail {
  color: #6366f1;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.link-cancel {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 999px;
  border: 1px solid #fecaca;
  background: #fff1f2;
  color: #dc2626;
}

.registered-empty {
  padding: 36px 16px;
  text-align: center;
  color: #9ca3af;
  font-size: 14px;
  background: #f9fafb;
  border-radius: 18px;
}

@media (max-width: 900px) {
  .registered-card {
    padding: 20px;
  }

  .registered-card__header {
    flex-direction: column;
    align-items: stretch;
  }

  .registered-item {
    grid-template-columns: 1fr;
    align-items: flex-start;
  }

  .registered-item__actions {
    width: 100%;
    justify-content: space-between;
    flex-wrap: wrap;
  }
}

@media (max-width: 640px) {
  .registered-card__title-wrap {
    align-items: flex-start;
  }

  .registered-item__name {
    font-size: 15px;
  }

  .link-detail,
  .link-cancel {
    font-size: 13px;
  }
}
</style>
