<template>
  <main class="event-page">
    <div class="sky-orb orb-one"></div><div class="sky-orb orb-two"></div>
    <header class="site-header container">
      <a class="brand" href="#top" aria-label="河南工业大学第三届讯飞杯 AI+创新应用大赛首页">
        <img src="@/assets/images/henu-seal-source.jpg" alt="河南工业大学校徽" />
        <span>河南工业大学<small>HENAN UNIVERSITY OF TECHNOLOGY</small></span>
      </a>
      <nav aria-label="页面导航">
        <a href="#intro">大赛简介</a><a href="#tracks">参赛方向</a><a href="#rules">参赛要求</a><a href="#schedule">赛程安排</a>
      </nav>
      <button class="header-cta" type="button" @click="goToLogin">立即报名 <span aria-hidden="true">→</span></button>
    </header>

    <section id="top" class="hero container">
      <div class="hero-copy">
        <p class="eyebrow">HENAN UNIVERSITY OF TECHNOLOGY · 2026</p>
        <h1>第三届 <em>“讯飞杯”</em><br />AI+创新应用大赛</h1>
        <p class="theme">—— 智体共生，赋能万象 ——</p>
        <p class="hero-summary">聚焦粮食、工业、城市、教育、医疗及社会民生领域，用人工智能回应真实问题，提出可行且具推广价值的创新方案。</p>
        <div class="host-line"><b>主办单位</b> 教务处、就业创业中心、校团委<br /><b>承办单位</b> 人工智能与大数据学院、科大讯飞股份有限公司</div>
        <div class="hero-actions"><a class="ghost-button" href="#tracks">查看参赛方向 <span aria-hidden="true">↓</span></a><button class="primary-button" type="button" @click="goToLogin">立即报名 <span aria-hidden="true">→</span></button></div>
      </div>
      <div class="hero-art" aria-hidden="true"><div class="planet"></div><div class="code-card code-left">AI</div><div class="code-card code-right">&lt;/&gt;</div><div class="robot">✦</div><div class="hero-person"><span>AI</span></div><p>用 AI 连接无限可能<br /><small>从校园走向更大的世界</small></p></div>
    </section>

    <div class="wave-divider"></div>
    <section id="intro" class="section container introduction">
      <div class="section-title"><span>01</span><div><p>ABOUT THE COMPETITION</p><h2>大赛简介</h2></div></div>
      <div class="intro-grid"><div class="ai-badge">AI</div><p>为加强创新人才自主培养，推动学校教育、科技、人才工作与育人工作深度融合，河南工业大学举办第三届“讯飞杯”AI+创新应用大赛。大赛鼓励同学们将人工智能等前沿技术应用于现实挑战，形成具有创新性、实用性、技术合理性与完成度的作品。</p><aside><b>参赛对象</b><span>河南工业大学全体在校学生</span><b>组队规则</b><span>每队 3–6 人，可跨院、跨专业组队</span></aside></div>
    </section>

    <section id="tracks" class="section container">
      <div class="section-title"><span>02</span><div><p>FIVE COMPETITION TRACKS</p><h2>五大参赛方向</h2></div><small>AI 赋能千行百业，创新点亮美好生活</small></div>
      <div class="track-grid"><article v-for="track in tracks" :key="track.name" class="track-card" :class="track.className"><div class="track-icon">{{ track.icon }}</div><h3>{{ track.name }}</h3><p>{{ track.description }}</p><span>{{ track.examples }}</span></article></div>
    </section>

    <section id="rules" class="section container rules-card">
      <div class="section-title"><span>03</span><div><p>ELIGIBILITY & REQUIREMENTS</p><h2>参赛对象与要求</h2></div></div>
      <div class="rule-list"><div v-for="item in rules" :key="item.title"><span class="rule-symbol" aria-hidden="true">{{ item.icon }}</span><b>{{ item.title }}</b><p>{{ item.content }}</p></div></div>
    </section>

    <section class="container two-column">
      <article class="section process-card"><div class="section-title"><span>04</span><div><p>COMPETITION FORMAT</p><h2>赛制说明</h2></div></div><div class="process"><div><b>初赛</b><p>评审项目策划书和项目视频，前 30 名队伍进入决赛。</p></div><span class="process-arrow" aria-hidden="true">→</span><div><b>决赛</b><p>PPT 讲解（6 分钟）与答辩，结合材料给出最终成绩。</p></div></div><p class="note">鼓励采用科大讯飞相关技术，按技术数量和难度可适当加分。</p></article>
      <article id="schedule" class="section schedule-card"><div class="section-title"><span>05</span><div><p>IMPORTANT DATES</p><h2>赛程安排</h2></div></div><ol><li><b>报名截止</b><time>2026 年 10 月 15 日</time></li><li><b>初赛材料提交</b><time>2026 年 11 月 16 日 — 11 月 27 日</time></li><li><b>初赛评审</b><time>2026 年 12 月 1 日 — 12 月 11 日</time></li><li><b>决赛时间</b><time>2026 年 12 月 14 日 — 12 月 25 日</time></li></ol></article>
    </section>

    <section class="section container awards"><div class="section-title"><span>06</span><div><p>AWARDS</p><h2>奖项设置</h2></div></div><div class="award-grid"><div v-for="award in awards" :key="award.title" :class="award.className"><strong>{{ award.icon }}</strong><b>{{ award.title }}</b><span>{{ award.prize }}</span></div></div></section>

    <section class="container two-column final-row"><article class="section submit-card"><div class="section-title"><span>07</span><div><p>REGISTRATION & MATERIALS</p><h2>报名与提交材料</h2></div></div><p>队长须在报名截止前登录本系统填写队伍信息。初赛应提交项目策划书、项目视频及其他相关材料。</p><ul><li>策划书建议不少于 20 页、10000 字</li><li>项目视频演示界面与功能，时长不超过 7 分钟</li></ul><button class="text-cta" type="button" @click="goToLogin">前往报名 <span aria-hidden="true">→</span></button></article><article class="section contact-card"><div class="section-title"><span>08</span><div><p>CONTACT</p><h2>联系方式</h2></div></div><p><b>官方 QQ 群</b> 928234743 / 978331786<br /><small>请队长及时入群，进其中一个即可。</small></p><div class="contact-teachers"><b>联系人</b><span>龚老师 15136259078<br />王老师 15515950521</span></div></article></section>
    <footer><p>AI 赋能青春，创新点亮未来</p><span>河南工业大学 · 人工智能与大数据学院</span></footer>
  </main>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
const goToLogin = () => router.push('/xfc/login')
const tracks = [
  { name: 'AI+粮食', icon: '🌾', className: 'food', description: '面向粮食生产、仓储、流通、溯源与全链条智能管理。', examples: '耕、种、管、收、储、运、销' },
  { name: 'AI+工业', icon: '⚙', className: 'industry', description: '聚焦智能制造、质检、预测维护与工业机器人。', examples: '数字孪生 · AI 视觉 · 具身智能' },
  { name: 'AI+城市', icon: '▥', className: 'city', description: '围绕交通、环境、民生、政务与公共安全。', examples: '智慧交通 · 城市治理 · 应急' },
  { name: 'AI+教育', icon: '▣', className: 'education', description: '探索智慧教学、个性化学习与教育资源优化。', examples: 'AI 助教 · 红色教育 · 教育机器人' },
  { name: 'AI+医疗', icon: '✚', className: 'medical', description: '服务医学影像、康复、健康管理与公共卫生。', examples: '智能诊断 · 康复设备 · 医疗智能体' }
]
const rules = [
  { icon: '●', title: '参赛对象', content: '河南工业大学全体在校学生。' },
  { icon: '◆', title: '团队报名', content: '每队不少于 3 人、不超过 6 人，允许跨院、跨专业组队。' },
  { icon: '✦', title: '指导教师', content: '每个团队可邀请 1–2 名指导教师。' },
  { icon: '✓', title: '作品要求', content: '作品须原创、真实、健康、合法，并具创新性、实用性与技术合理性。' },
  { icon: '!', title: '限制说明', content: '不接受符合通知中限制条件的既有赛事获奖作品参赛。' }
]
const awards = [
  { icon: '🏆', title: '特等奖 1 项', prize: '3000 元 + 证书', className: 'special' },
  { icon: '🏆', title: '一等奖 3 项', prize: '1500 元 + 证书', className: 'first' },
  { icon: '🏆', title: '二等奖 6 项', prize: '1000 元 + 证书', className: 'second' },
  { icon: '🏆', title: '三等奖 12 项', prize: '500 元 + 证书', className: 'third' }
]
</script>

<style scoped>
:global(html){scroll-behavior:smooth}.event-page{position:relative;overflow:hidden;background:linear-gradient(180deg,#e9f7ff 0,#f9fcff 33%,#e7f6ff 100%);color:#164486;font-family:"Microsoft YaHei",sans-serif}.container{position:relative;z-index:1;width:min(1180px,calc(100% - 48px));margin:0 auto}.sky-orb{position:absolute;border-radius:50%;filter:blur(2px);opacity:.65}.orb-one{width:420px;height:420px;top:-170px;right:-120px;background:#d8baff}.orb-two{width:300px;height:300px;top:750px;left:-170px;background:#b8eaff}.site-header{display:flex;align-items:center;gap:32px;height:92px}.brand{display:flex;align-items:center;gap:10px;color:#124581;font-size:20px;font-weight:900;letter-spacing:2px;text-decoration:none}.brand img{width:52px;height:52px;object-fit:contain}.brand small{display:block;margin-top:2px;font-size:7px;font-weight:700;letter-spacing:1.25px}.site-header nav{display:flex;gap:28px;margin-left:auto}.site-header nav a{color:#426495;font-size:14px;text-decoration:none}.site-header nav a:hover{color:#155bdf}.header-cta,.primary-button{border:0;border-radius:999px;background:linear-gradient(100deg,#5f77ee,#38bce8);box-shadow:0 8px 18px #6b87e666;color:#fff;cursor:pointer;font-weight:800}.header-cta{padding:11px 20px}.header-cta i,.primary-button i{margin-left:7px}.hero{display:grid;grid-template-columns:1.18fr .82fr;min-height:345px;padding:38px 40px 44px;border:1px solid #fff;border-radius:28px;background:linear-gradient(115deg,#ffffffd9 22%,#e3f7ffb8 75%,#d7d1ffb5);box-shadow:0 16px 38px #6aa8c72b}.eyebrow,.section-title p{margin:0;color:#6a86ca;font-size:11px;font-weight:800;letter-spacing:.16em}.hero h1{margin:12px 0 3px;color:#10438d;font-size:clamp(32px,4.2vw,52px);line-height:1.22;letter-spacing:-2px}.hero h1 em{font-style:normal;background:linear-gradient(90deg,#8739f5,#2e71ef);background-clip:text;color:transparent}.theme{margin:0;color:#315aab;font-size:22px;font-weight:900;letter-spacing:7px}.hero-summary{max-width:610px;margin:20px 0 10px;color:#416797;font-size:14px;line-height:1.9}.host-line{color:#315990;font-size:12px;line-height:1.9}.host-line b{margin-right:8px;color:#1e55a1}.hero-actions{display:flex;gap:14px;margin-top:21px}.ghost-button,.primary-button{padding:12px 22px;font-size:14px;text-decoration:none}.ghost-button{border:1px solid #8ab4f7;border-radius:999px;background:#ffffffa8;color:#2165c4;font-weight:800}.hero-art{position:relative;min-height:270px}.planet{position:absolute;right:28px;bottom:8px;width:220px;height:220px;border:16px solid #ffffffa8;border-radius:50%;background:radial-gradient(circle at 35% 30%,#fff,#bae3ff 38%,#a7a1fb 74%);box-shadow:0 20px 30px #6773ca38}.code-card,.robot{position:absolute;display:grid;place-items:center;border:1px solid #ffffffd9;box-shadow:0 10px 20px #525ba440;color:#fff;font-weight:900}.code-card{width:62px;height:62px;border-radius:16px;background:linear-gradient(135deg,#5c88f7,#a071e8);font-size:20px}.code-left{top:70px;left:31px;transform:rotate(-10deg)}.code-right{right:5px;bottom:87px;background:linear-gradient(135deg,#38c6ec,#5e7df0)}.robot{top:38px;right:80px;width:45px;height:45px;border-radius:50%;background:#8c5ce9}.hero-person{position:absolute;right:50px;bottom:16px;width:92px;height:150px;border-radius:52% 52% 35% 35%;background:linear-gradient(135deg,#ff986a 0 31%,#65452f 31% 47%,#fbf7f2 47%);transform:rotate(-10deg);box-shadow:8px 13px 20px #355f9d30}.hero-person span{position:absolute;bottom:8px;left:-38px;padding:12px;border-radius:12px;background:#fff;color:#4f6bea;font-size:22px;font-weight:900}.hero-art p{position:absolute;top:2px;right:0;color:#3676e2;font-size:14px;font-weight:800;line-height:1.7;text-align:center;transform:rotate(-7deg)}.hero-art small{font-size:10px}.wave-divider{height:48px;margin-top:-16px;background:radial-gradient(80% 70px at 50% 0,#a4d9ff33 0 56%,transparent 57%)}.section{box-sizing:border-box;margin:20px auto;padding:26px 30px;border:1px solid #fff;border-radius:22px;background:#ffffffd4;box-shadow:0 12px 30px #588ec11c}.section-title{display:flex;align-items:center;gap:13px;margin-bottom:22px}.section-title>span{display:grid;place-items:center;width:43px;height:43px;border-radius:50%;background:linear-gradient(135deg,#d7f4ff,#e4e1ff);color:#205cdd;font-size:17px;font-weight:900}.section-title h2{margin:2px 0 0;color:#124887;font-size:23px}.section-title>small{margin-left:auto;color:#6c8ff0;font-size:13px;font-weight:700}.intro-grid{display:grid;grid-template-columns:140px 1fr 235px;align-items:center;gap:24px}.ai-badge{display:grid;place-items:center;width:118px;height:118px;border:7px solid #fff;border-radius:40%;background:linear-gradient(135deg,#795eea,#39caef);box-shadow:0 8px 25px #8575e85c;color:#fff;font-size:45px;font-weight:900;transform:rotate(-8deg)}.intro-grid>p{margin:0;color:#4e6d9d;font-size:14px;line-height:2}.intro-grid aside{display:grid;grid-template-columns:78px 1fr;gap:10px 0;color:#5c75a3;font-size:12px;line-height:1.5}.intro-grid aside b{color:#1f58b7}.track-grid{display:grid;grid-template-columns:repeat(5,1fr);gap:13px}.track-card{min-height:217px;padding:16px 14px;border:1px solid #fff;border-radius:17px;background:linear-gradient(165deg,#e7f7ff,#fff);box-shadow:0 8px 18px #7392bd1a}.track-card.industry{background:linear-gradient(165deg,#e1f7ff,#effbff)}.track-card.city{background:linear-gradient(165deg,#daf7ed,#f3fffc)}.track-card.education{background:linear-gradient(165deg,#e9e5ff,#faf8ff)}.track-card.medical{background:linear-gradient(165deg,#ffe2f0,#fff8fb)}.track-icon{display:grid;place-items:center;width:56px;height:56px;border-radius:18px;background:#fff;color:#6a72e8;font-size:29px;box-shadow:0 5px 13px #7190c729}.track-card h3{margin:16px 0 8px;color:#15529b;font-size:19px}.track-card p{margin:0;color:#58739e;font-size:12px;line-height:1.7}.track-card span{display:block;margin-top:10px;color:#7696c1;font-size:10px;line-height:1.5}.rules-card{background:linear-gradient(115deg,#fff,#eff8ff)}.rule-list{display:grid;gap:12px}.rule-list>div{display:grid;grid-template-columns:25px 110px 1fr;align-items:start;color:#4f6d99;font-size:13px;line-height:1.7}.rule-list i{color:#3975e9;padding-top:3px}.rule-list b{color:#1954a2}.rule-list p{margin:0}.two-column{display:grid;grid-template-columns:1.2fr .9fr;gap:20px}.two-column .section{width:100%;margin:0}.process{display:grid;grid-template-columns:1fr 28px 1fr;gap:12px;align-items:center}.process>div{min-height:95px;padding:17px;border-radius:13px;background:linear-gradient(135deg,#e6efff,#f9fbff)}.process b{color:#1b56ae;font-size:18px}.process p,.note,.submit-card>p,.contact-card p{color:#55709c;font-size:12px;line-height:1.75}.process i{color:#5c89ef;text-align:center}.note{margin:14px 0 0;padding:9px 12px;border-radius:8px;background:#edf4ff}.schedule-card ol{position:relative;margin:0;padding:0 0 0 25px;list-style:none}.schedule-card ol::before{position:absolute;top:4px;bottom:5px;left:5px;width:2px;background:#a3bff9;content:""}.schedule-card li{position:relative;display:flex;justify-content:space-between;gap:12px;padding-bottom:14px;color:#2260b7;font-size:12px}.schedule-card li::before{position:absolute;top:3px;left:-24px;width:11px;height:11px;border:3px solid #fff;border-radius:50%;background:#5c6ce9;box-shadow:0 0 0 2px #8da9f5;content:""}.schedule-card time{color:#496fa9;font-weight:700}.award-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:13px}.award-grid>div{display:grid;grid-template-columns:49px 1fr;align-items:center;gap:2px 11px;padding:15px;border-radius:13px;background:#fff1d7}.award-grid>div.first{background:#e3f3ff}.award-grid>div.second{background:#ffe8ec}.award-grid>div.third{background:#f0e7ff}.award-grid strong{grid-row:span 2;font-size:35px}.award-grid b{color:#a05a12;font-size:15px}.award-grid span{color:#b16c1c;font-size:12px;font-weight:800}.award-grid .first b,.award-grid .first span{color:#2260b7}.award-grid .second b,.award-grid .second span{color:#cb5362}.award-grid .third b,.award-grid .third span{color:#7552c0}.final-row{padding-bottom:26px}.submit-card ul{margin:10px 0;padding-left:18px;color:#6880a5;font-size:12px;line-height:1.9}.text-cta{border:0;background:none;color:#296ae1;cursor:pointer;font-weight:900}.contact-card p{font-size:13px}.contact-card p b{display:inline-block;min-width:72px;color:#2458a5}.contact-card small{padding-left:72px;color:#8b9db8}footer{padding:44px 20px 54px;color:#1e5abb;text-align:center}footer p{margin:0;font-size:28px;font-weight:900;letter-spacing:6px}footer span{display:block;margin-top:9px;font-size:11px;letter-spacing:3px}@media(max-width:900px){.site-header nav{display:none}.hero{grid-template-columns:1fr}.hero-art{display:none}.track-grid{grid-template-columns:repeat(3,1fr)}.intro-grid{grid-template-columns:120px 1fr}.intro-grid aside{grid-column:1/-1}.two-column{grid-template-columns:1fr}.award-grid{grid-template-columns:repeat(2,1fr)}}@media(max-width:560px){.container{width:calc(100% - 28px)}.site-header{height:76px}.brand{font-size:15px;letter-spacing:1px}.brand img{width:42px;height:42px}.header-cta{margin-left:auto;padding:9px 13px;font-size:12px}.hero{min-height:0;padding:28px 22px}.hero h1{font-size:32px}.theme{font-size:15px;letter-spacing:3px}.hero-summary{font-size:13px}.section{padding:21px 18px;border-radius:17px}.section-title h2{font-size:19px}.section-title>small{display:none}.intro-grid{grid-template-columns:1fr}.ai-badge{width:80px;height:80px;font-size:30px}.track-grid{grid-template-columns:1fr 1fr}.track-card{min-height:187px}.track-card h3{font-size:16px}.rule-list>div{grid-template-columns:22px 83px 1fr;font-size:12px}.process{grid-template-columns:1fr}.process i{transform:rotate(90deg)}.schedule-card li{display:grid;grid-template-columns:1fr}.award-grid{grid-template-columns:1fr}.final-row{padding-bottom:15px}footer p{font-size:20px;letter-spacing:3px}}
.brand img{object-fit:cover;clip-path:circle(48% at 50% 50%)}
.header-cta span,.primary-button span,.ghost-button span,.text-cta span{display:inline-block;margin-left:7px;font-size:1.1em;line-height:1}.rule-symbol{color:#3975e9;font-size:16px;font-weight:900;line-height:1.4}.process-arrow{color:#5c89ef;font-size:23px;font-weight:900;text-align:center}@media(max-width:560px){.process-arrow{transform:rotate(90deg)}}
.contact-teachers{display:grid;grid-template-columns:72px 1fr;align-items:start;margin:13px 0 0;color:#55709c;font-size:13px;line-height:1.75}.contact-teachers b{color:#2458a5}.contact-teachers span{white-space:nowrap}
</style>
