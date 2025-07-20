# 🚀 Guia de Início Rápido - Order Service

Este guia te ajudará a executar o Order Service em poucos minutos.

## ⚡ Execução Rápida

### 1. Pré-requisitos
- Docker Desktop instalado e rodando
- Java 17 (opcional, se quiser executar localmente)
- Maven 3.8+ (opcional, se quiser executar localmente)

### 2. Clone e Execute
```bash
# Clone o repositório
git clone <repository-url>
cd order-service

# ⚠️ IMPORTANTE: Use --build para garantir as últimas mudanças
docker-compose up --build -d

# Aguarde 15-20 segundos para inicialização completa
```

### 3. Verifique se está funcionando
- **API**: http://localhost:8080
- **Swagger**: http://localhost:8080/swagger-ui.html
- **Kafka UI**: http://localhost:8081
- **Health**: http://localhost:8080/actuator/health

## 🧪 Testando a API

### ⚠️ **Problema Resolvido: Inserção de Produtos**

Este projeto resolveu um problema crítico de **foreign key null** na inserção de produtos. A solução garante que:
- ✅ Pedido é salvo primeiro (obtém ID)
- ✅ Produtos são inseridos depois (com ID do pedido)
- ✅ Resposta da API inclui produtos inseridos

### Usando o arquivo de exemplo
O projeto inclui um arquivo `examples/test-api.http` que pode ser usado com:
- **IntelliJ IDEA**: Clique no ícone ▶️ ao lado de cada requisição
- **VS Code**: Instale a extensão "REST Client" e use o arquivo
- **cURL**: Copie os comandos do arquivo

### Exemplo de criação de pedido
```bash
curl -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": "EXT-001",
    "produtos": [
      {
        "nome": "Notebook Dell",
        "preco": 2999.99
      },
      {
        "nome": "Mouse Wireless",
        "preco": 89.90
      }
    ]
  }'
```

**Resposta esperada:**
```json
{
  "success": true,
  "message": "Pedido criado com sucesso",
  "pedidoId": 1,
  "externalId": "EXT-001",
  "status": "PROCESSADO",
  "total": 3089.89,
  "createdAt": "2025-07-20T20:07:30.28944",
  "produtos": [
    {
      "id": 1,
      "nome": "Notebook Dell",
      "preco": 2999.99
    },
    {
      "id": 2,
      "nome": "Mouse Wireless",
      "preco": 89.90
    }
  ]
}
```

## 🔧 Comandos Úteis

### Docker Compose
```bash
# ⚠️ RECOMENDADO: Iniciar com rebuild completo
docker-compose up --build -d

# Ver logs em tempo real
docker logs order-service -f

# Verificar status dos containers
docker ps

# Parar todos os serviços
docker-compose down

# Rebuild e iniciar
docker-compose up --build -d
```

### Verificação de Dados
```bash
# Verificar se produtos foram inseridos no banco
docker exec order-postgres psql -U orderuser -d orderdb -c "SELECT * FROM products;"

# Verificar pedidos e produtos juntos
docker exec order-postgres psql -U orderuser -d orderdb -c "SELECT p.id, p.external_id, pr.nome, pr.preco FROM pedidos p JOIN products pr ON p.id = pr.pedido_id;"

# Acessar banco interativamente
docker exec -it order-postgres psql -U orderuser -d orderdb
```

### Testes
```bash
# Executar todos os testes
chmod +x scripts/test.sh
./scripts/test.sh

# Ou manualmente
./mvnw test
./mvnw verify
```

### Desenvolvimento Local
```bash
# Build do projeto
./mvnw clean package

# Executar localmente (requer PostgreSQL e Kafka rodando)
./mvnw spring-boot:run
```

## 📊 Monitoramento

### Health Checks
```bash
# Verificar status geral
curl http://localhost:8080/actuator/health

# Verificar detalhes
curl http://localhost:8080/actuator/health/db
curl http://localhost:8080/actuator/health/kafka
```

### Logs
```bash
# Logs da aplicação
docker logs order-service -f

# Logs do banco
docker logs order-postgres -f

# Logs do Kafka
docker logs order-kafka -f
```

## 🐛 Troubleshooting

### ❌ **Problemas Comuns e Soluções**

**1. Erro 500 - "null value in column pedido_id"**
- **Causa**: Produtos sendo inseridos antes do pedido ter ID
- **Solução**: ✅ **IMPLEMENTADO** - Pedido salvo primeiro, produtos depois

**2. Container não inicia**
```bash
# Verificar logs
docker logs order-service

# Rebuild completo
docker-compose down
docker-compose up --build -d
```

**3. API retorna 404**
```bash
# Aguardar inicialização completa (15-20 segundos)
# Verificar se container está healthy
docker ps
```

**4. Produtos não aparecem na resposta**
```bash
# Verificar se foram inseridos no banco
docker exec order-postgres psql -U orderuser -d orderdb -c "SELECT * FROM products;"
```

**5. Porta 8080 já em uso**
```bash
# Verificar o que está usando a porta
netstat -ano | findstr :8080

# Parar o processo ou mudar a porta no docker-compose.yml
```

**6. Docker não consegue baixar as imagens**
```bash
# Verificar conexão com internet
docker pull hello-world

# Limpar cache do Docker
docker system prune -a
```

**7. Banco de dados não conecta**
```bash
# Verificar se o PostgreSQL está rodando
docker ps | grep postgres

# Verificar logs
docker logs order-postgres
```

**8. Kafka não inicia**
```bash
# Verificar se o Zookeeper está rodando
docker ps | grep zookeeper

# Reiniciar Kafka
docker-compose restart order-kafka
```

### 🧪 **Testes de Validação**

```bash
# Teste 1: Criar pedido com produtos
curl -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"externalId": "TEST-001", "produtos": [{"nome": "Teste", "preco": 100.00}]}'

# Teste 2: Verificar se produtos foram inseridos
docker exec order-postgres psql -U orderuser -d orderdb -c "SELECT p.id, p.external_id, pr.nome, pr.preco FROM pedidos p JOIN products pr ON p.id = pr.pedido_id;"

# Teste 3: Health check
curl http://localhost:8080/actuator/health
```

### Limpeza Completa
```bash
# Parar e remover tudo
docker-compose down -v
docker system prune -a

# Remover volumes
docker volume rm $(docker volume ls -q | grep order)
```

## 📝 Próximos Passos

1. **Explore a API**: Use o Swagger UI em http://localhost:8080/swagger-ui.html
2. **Teste o Kafka**: Use o Kafka UI em http://localhost:8081
3. **Analise os logs**: Monitore o processamento em tempo real
4. **Execute os testes**: Verifique a qualidade do código
5. **Modifique o código**: Adicione novas funcionalidades

## 🆘 Ainda com problemas?

1. Verifique se o Docker Desktop está rodando
2. Verifique se as portas 8080, 8081, 5432, 9092 estão livres
3. Execute `docker logs order-service` para ver os logs de erro
4. Verifique se usou `--build` no docker-compose
5. Aguarde 15-20 segundos para inicialização completa
6. Abra uma issue no GitHub com os logs de erro

---

**🎉 Parabéns!** Se chegou até aqui, o Order Service está funcionando perfeitamente e o problema de inserção de produtos foi resolvido! 