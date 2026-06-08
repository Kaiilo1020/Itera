# Build stage
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Install sbt
RUN apt-get update && apt-get install -y curl gnupg && \
    curl -fL https://github.com/sbt/sbt/releases/download/v1.10.0/sbt-1.10.0.tgz | tar xz -C /usr/local && \
    ln -s /usr/local/sbt/bin/sbt /usr/local/bin/sbt && \
    rm -rf /var/lib/apt/lists/*

COPY build.sbt .
COPY project project
RUN sbt update
COPY . .
# Compile and stage to verify build
RUN sbt compile stage

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Install curl for healthchecks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/universal/stage .
EXPOSE 8080
ENV PLAY_HTTP_SECRET_KEY="changeme"
# The binary name is lowercase "itera" because we set 'name := "itera"' in build.sbt
ENTRYPOINT ["./bin/itera", "-Dplay.server.provider=play.core.server.NettyServerProvider"]
