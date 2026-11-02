# Executa os testes BDD contra uma API já em execução.
# Build : docker build -t open-investment-bdd .
# Run   : docker run --rm -e BASE_URL=http://host.docker.internal:8080 open-investment-bdd
FROM maven:3.9.6-eclipse-temurin-17
WORKDIR /bdd
COPY pom.xml .
COPY src ./src
ENV BASE_URL=http://host.docker.internal:8080
CMD ["mvn", "-B", "-ntp", "test"]
