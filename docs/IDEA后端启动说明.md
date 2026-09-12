# IDEA 启动讯飞杯后端

## 已完成的项目配置

- 后端入口：`compserve-admin` 模块中的 `org.iflytek.RuoYiApplication`。
- 本地联调 Profile：`druid,xfc-local`。
- 数据库地址、端口、库名与用户名已写入 `application-xfc-local.yml`。
- 数据库密码与讯飞杯 Token 密钥不保存到项目文件，必须在 IDEA 的运行配置中填写。

## 创建运行配置

1. 用 IDEA 打开项目根目录的 `pom.xml`，等待 Maven 导入完成。
2. 在 **Run → Edit Configurations → + → Spring Boot** 新建配置。
3. 填写：
   - Name：`XFC Backend`
   - Main class：`org.iflytek.RuoYiApplication`
   - Use classpath of module：`compserve-admin`
   - Working directory：`$PROJECT_DIR$/compserve-admin`
   - Program arguments：`--spring.profiles.active=druid,xfc-local`
4. 在 **Environment variables** 设置：
   - `DB_PASSWORD`：数据库密码
   - `XFC_TOKEN_SECRET`：至少 64 字节的随机字符串
5. 点击运行按钮。启动成功后端口为 `8080`。

## 前端联调

在 `compserve-ui` 目录执行 `pnpm dev -- --host 127.0.0.1 --port 5173`，然后访问：

`http://127.0.0.1:5173/xfc/login`

使用有效学生账号密码登录后，系统会调用 `/api/xfc/auth/login`，并跳转到 `/xfc/registration` 显示当前学生资料。
