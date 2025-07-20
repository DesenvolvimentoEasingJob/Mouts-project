#!/bin/bash

# Script de inicialização do Order Service
# Autor: Order Service Team
# Data: 2024

set -e

echo "🚀 Iniciando Order Service..."

# Verificar se Docker está instalado
if ! command -v docker &> /dev/null; then
    echo "❌ Docker não está instalado. Por favor, instale o Docker primeiro."
    exit 1
fi

# Verificar se Docker Compose está instalado
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose não está instalado. Por favor, instale o Docker Compose primeiro."
    exit 1
fi

# Verificar se Maven está instalado
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven não está instalado. Por favor, instale o Maven primeiro."
    exit 1
fi

echo "✅ Dependências verificadas"

# Build do projeto
echo "🔨 Fazendo build do projeto..."
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Erro no build do projeto"
    exit 1
fi

echo "✅ Build concluído"

# Parar containers existentes
echo "🛑 Parando containers existentes..."
docker-compose down

# Iniciar serviços
echo "🐳 Iniciando serviços com Docker Compose..."
docker-compose up --build -d

# Aguardar serviços ficarem prontos
echo "⏳ Aguardando serviços ficarem prontos..."
sleep 30

# Verificar status dos serviços
echo "🔍 Verificando status dos serviços..."

# Verificar PostgreSQL
if docker-compose ps postgres | grep -q "Up"; then
    echo "✅ PostgreSQL está rodando"
else
    echo "❌ PostgreSQL não está rodando"
    exit 1
fi

# Verificar Kafka
if docker-compose ps kafka | grep -q "Up"; then
    echo "✅ Kafka está rodando"
else
    echo "❌ Kafka não está rodando"
    exit 1
fi

# Verificar Order Service
if docker-compose ps order-service | grep -q "Up"; then
    echo "✅ Order Service está rodando"
else
    echo "❌ Order Service não está rodando"
    exit 1
fi

echo ""
echo "🎉 Order Service iniciado com sucesso!"
echo ""
echo "📋 URLs dos serviços:"
echo "   • Order Service API: http://localhost:8080"
echo "   • Swagger UI: http://localhost:8080/swagger-ui.html"
echo "   • Kafka UI: http://localhost:8081"
echo "   • Health Check: http://localhost:8080/actuator/health"
echo ""
echo "📝 Para parar os serviços, execute: docker-compose down"
echo "📝 Para ver os logs, execute: docker-compose logs -f" 