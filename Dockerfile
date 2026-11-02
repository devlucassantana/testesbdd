# =============================================
# Dockerfile — Open Investment API
# Multi-stage build: compila com Maven e roda com JRE slim
# =============================================

# ---- Estágio 1: Build ----
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copia apenas o pom.xml primeiro para aproveitar o cache do Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código-fonte e compila (sem rodar testes — Oracle não estará disponível)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Estágio 2: Runtime ----
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Cria usuário sem privilégios de root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copia o JAR gerado no estágio de build
COPY --from=builder /app/target/open-investment-1.0.0.jar app.jar

# Porta padrão da aplicação
EXPOSE 8080

# Variáveis não sensíveis com padrão; DB_USER, DB_PASS e JWT_SECRET vêm do compose/.env
ENV DB_HOST=oracle-db \
    DB_PORT=1521 \
    DB_SERVICE=XEPDB1

# Health check para o Docker aguardar a app estar pronta
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
