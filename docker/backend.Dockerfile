# ==================== Stage 1: 构建阶段 ====================
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /build

# 1. 先拷贝【根目录】的 pom.xml
COPY pom.xml .

# 2. 拷贝【所有子模块】的 pom.xml (保持目录结构！)
COPY wknetic-common/pom.xml wknetic-common/
COPY wknetic-admin/pom.xml wknetic-admin/
COPY wknetic-auth/pom.xml wknetic-auth/
COPY wknetic-community/pom.xml wknetic-community/
COPY wknetic-sync/pom.xml wknetic-sync/
COPY wknetic-socket/pom.xml wknetic-socket/
COPY wknetic-api/pom.xml wknetic-api/

# 3. 现在子模块 pom 都在了，可以安心下载依赖了 (利用缓存)
RUN mvn dependency:go-offline -B

# 4. 拷贝所有源代码 (把整个项目拷进去，因为后面编译需要所有模块的代码)
COPY . .

# 5. 打包 (指定只构建 admin 模块，但同时会编译它依赖的 common 等兄弟模块)
# -pl wknetic-admin : 只构建 admin 模块
# -am : 自动构建依赖的模块 (also make dependents)
RUN mvn clean package -pl wknetic-admin -am -DskipTests

# ==================== Stage 2: 运行阶段 ====================
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

WORKDIR /app

# 从 builder 阶段拷贝打好的 jar 包
# 注意路径：在多模块构建下，jar 包会在 wknetic-admin/target/ 下
COPY --from=builder /build/wknetic-admin/target/*.jar app.jar

EXPOSE 8080 8081

ENTRYPOINT ["java", "-jar", "app.jar"]