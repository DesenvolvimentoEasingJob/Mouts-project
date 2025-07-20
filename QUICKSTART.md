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

# Execute o script de inicialização
chmod +x scripts/start.sh
./scripts/start.sh
```

### 3. Verifique se está funcionando
- **API**: http://localhost:8080
- **Swagger**: http://localhost:8080/swagger-ui.html
- **Kafka UI**: http://localhost:8081
- **Health**: http://localhost:8080/actuator/health

## 🧪 Testando a API

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

## 🔧 Comandos Úteis

### Docker Compose
```bash
# Iniciar todos os serviços
docker-compose up -d

# Ver logs
docker-compose logs -f

# Parar todos os serviços
docker-compose down

# Rebuild e iniciar
docker-compose up --build -d
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
docker-compose logs -f order-service

# Logs do banco
docker-compose logs -f postgres

# Logs do Kafka
docker-compose logs -f kafka
```

## 🐛 Troubleshooting

### Problemas Comuns

**1. Porta 8080 já em uso**
```bash
# Verificar o que está usando a porta
netstat -ano | findstr :8080

# Parar o processo ou mudar a porta no docker-compose.yml
```

**2. Docker não consegue baixar as imagens**
```bash
# Verificar conexão com internet
docker pull hello-world

# Limpar cache do Docker
docker system prune -a
```

**3. Banco de dados não conecta**
```bash
# Verificar se o PostgreSQL está rodando
docker-compose ps postgres

# Verificar logs
docker-compose logs postgres
```

**4. Kafka não inicia**
```bash
# Verificar se o Zookeeper está rodando
docker-compose ps zookeeper

# Reiniciar Kafka
docker-compose restart kafka
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
3. Execute `docker-compose logs` para ver os logs de erro
4. Abra uma issue no GitHub com os logs de erro

---

**🎉 Parabéns!** Se chegou até aqui, o Order Service está funcionando perfeitamente! 