# Build stage - Compilar con SBT
FROM openjdk:11-jdk-slim as builder

WORKDIR /app

# Instalar SBT
RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://github.com/sbt/sbt/releases/download/v1.9.7/sbt-1.9.7.tgz | tar xz -C /opt && \
    ln -s /opt/sbt/bin/sbt /usr/local/bin/sbt && \
    rm -rf /var/lib/apt/lists/*

# Copiar build.sbt y project/
COPY build.sbt build.sbt
COPY project/ project/

# Descargar dependencias (crear capas en caché)
RUN sbt update

# Copiar código fuente
COPY src/ src/

# Compilar y empaquetar
RUN sbt dist

# Runtime stage - Imagen mínima
FROM openjdk:11-jdk-slim

WORKDIR /app

# Copiar distribución del build anterior
COPY --from=builder /app/target/universal/stage /app

# Crear usuario no-root
RUN useradd -m -u 1000 appuser && chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Ejecutar la aplicación
CMD ["/app/bin/itera", "-Dplay.server.provider=play.core.server.NettyServerProvider"]
