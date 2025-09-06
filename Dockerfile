# ============================
# Etapa 1: build da aplicação
# ============================
FROM gradle:8.10.2-jdk21 AS builder

WORKDIR /app

# Copia os arquivos de configuração primeiro (cache eficiente)
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Dá permissão de execução ao gradlew
RUN chmod +x gradlew

# Baixa as dependências (ignora testes para cachear dependências)
RUN ./gradlew build -x test --no-daemon || return 0

# Copia o código da aplicação
COPY . .

# Build final (gera o .jar no build/libs)
RUN ./gradlew clean bootJar --no-daemon

# ============================
# Etapa 2: imagem final
# ============================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copia apenas o .jar do builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Expõe a porta (ajuste se não for 8080)
EXPOSE 8080

# Comando para rodar
ENTRYPOINT ["java","-jar","app.jar"]
