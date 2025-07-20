#!/bin/bash

# Script de execução de testes do Order Service
# Autor: Order Service Team
# Data: 2024

set -e

echo "🧪 Executando testes do Order Service..."

# Verificar se Maven está instalado
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven não está instalado. Por favor, instale o Maven primeiro."
    exit 1
fi

# Verificar se Docker está instalado (para Testcontainers)
if ! command -v docker &> /dev/null; then
    echo "❌ Docker não está instalado. Testcontainers precisa do Docker."
    exit 1
fi

echo "✅ Dependências verificadas"

# Executar testes unitários
echo "🔬 Executando testes unitários..."
./mvnw test

if [ $? -ne 0 ]; then
    echo "❌ Testes unitários falharam"
    exit 1
fi

echo "✅ Testes unitários passaram"

# Executar testes de integração
echo "🔗 Executando testes de integração..."
./mvnw verify

if [ $? -ne 0 ]; then
    echo "❌ Testes de integração falharam"
    exit 1
fi

echo "✅ Testes de integração passaram"

# Gerar relatório de cobertura
echo "📊 Gerando relatório de cobertura..."
./mvnw jacoco:report

if [ $? -eq 0 ]; then
    echo "✅ Relatório de cobertura gerado em target/site/jacoco/index.html"
else
    echo "⚠️  Erro ao gerar relatório de cobertura"
fi

echo ""
echo "🎉 Todos os testes passaram!"
echo ""
echo "📋 Resumo:"
echo "   • Testes unitários: ✅"
echo "   • Testes de integração: ✅"
echo "   • Cobertura de código: Gerada"
echo ""
echo "📝 Para ver o relatório de cobertura, abra: target/site/jacoco/index.html" 