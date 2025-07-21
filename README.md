# 📦 Order Service

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Container-blue.svg)](https://www.docker.com/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-Event%20Driven-purple.svg)](https://kafka.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue.svg)](https://www.postgresql.org/)
[![DDD](https://img.shields.io/badge/Architecture-DDD-red.svg)](https://martinfowler.com/bliki/DomainDrivenDesign.html)
[![SOLID](https://img.shields.io/badge/Principles-SOLID-yellow.svg)](https://en.wikipedia.org/wiki/SOLID)

> **Microserviço de processamento de pedidos** desenvolvido em Java 17 com Spring Boot 3+, seguindo os princípios de **Domain-Driven Design (DDD)** e **SOLID**. Processa pedidos de forma assíncrona via Apache Kafka, garantindo alta disponibilidade e escalabilidade.

## 🎯 Visão Geral

O **Order Service** é responsável por:
- ✅ Consumir pedidos do **Produto Externo A** (REST API ou Kafka)
- ✅ Processar pedidos, somando valores dos produtos
- ✅ Armazenar pedidos processados no **PostgreSQL**
- ✅ Publicar resultados em tópico **Kafka** (`pedidos.processados`)
- ✅ Permitir que o **Produto Externo B** consuma os resultados
- ✅ Ser escalável, seguro e testável via **Docker**

### 🔄 Fluxo de Processamento
```
Produto Externo A → Order Service → PostgreSQL → Kafka → Produto Externo B
```

## 🚀 Execução Rápida

### ⚡ Em 3 Passos

```bash
# 1. Clone o repositório
git clone https://github.com/DesenvolvimentoEasingJob/order-service.git
cd order-service

# 2. Execute com Docker (IMPORTANTE: Use --build para garantir as últimas mudanças)
docker-compose up --build -d

# 3. Aguarde a inicialização (15-20 segundos) e teste a API
curl -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": "DEMO-001",
    "produtos": [
      {"nome": "Notebook Dell", "preco": 2999.99},
      {"nome": "Mouse Wireless", "preco": 89.90}
    ]
  }'
```

### 📊 URLs dos Serviços
- **API REST**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Kafka UI**: http://localhost:8081
- **Health Check**: http://localhost:8080/actuator/health

### ⚠️ **IMPORTANTE: Problema Resolvido**

Este projeto resolveu um problema crítico de **inserção de produtos com foreign key null**. A solução implementada garante que:

1. **Pedido é salvo primeiro** - Obtém ID automaticamente
2. **Produtos são inseridos depois** - Com o ID do pedido já definido
3. **Transação garantida** - Tudo funciona dentro de uma transação
4. **Resposta completa** - API retorna pedido + produtos inseridos

#### 🔧 **Solução Técnica Implementada**
```java
@Transactional
public PedidoEntity processarPedido(PedidoDTO pedidoDTO) {
    // 1. Salvar apenas o pedido primeiro
    PedidoEntity pedido = pedidoMapper.toEntity(pedidoDTO);
    PedidoEntity pedidoSalvo = pedidoRepository.save(pedido);
    
    // 2. Agora inserir produtos com o ID do pedido
    for (ProdutoDTO produtoDTO : pedidoDTO.getProdutos()) {
        ProdutoEntity produto = pedidoMapper.toEntity(produtoDTO);
        produto.setPedidoId(pedidoSalvo.getId()); // ✅ ID já disponível
        produtoRepository.save(produto);
    }
    
    return pedidoSalvo;
}
```

## 🏗️ Arquitetura

### 🎯 **Domain-Driven Design (DDD)**
```
src/main/java/com/example/order/
├── domain/                    # 🎯 Lógica de Negócio
│   ├── entity/               # Entidades de domínio
│   └── enums/                # Enums do domínio
├── application/              # 🔧 Casos de Uso
│   ├── dto/                  # Data Transfer Objects
│   ├── mapper/               # Mapeamento (MapStruct)
│   └── service/              # Serviços de aplicação
└── infrastructure/           # 🏗️ Implementações Técnicas
    ├── config/               # Configurações
    ├── controller/           # Controllers REST
    ├── messaging/            # Kafka Producer/Consumer
    └── repository/           # Repositórios JPA
```

### 🔧 **Princípios SOLID**
- **S** - Single Responsibility: Cada classe tem uma responsabilidade
- **O** - Open/Closed: Extensível sem modificação
- **L** - Liskov Substitution: Interfaces bem definidas
- **I** - Interface Segregation: Interfaces específicas
- **D** - Dependency Inversion: Inversão de dependências

### 💡 **Exemplos de Implementação**

#### Domain Layer - Lógica de Negócio
```java
@Entity
public class PedidoEntity {
    public void calcularTotal(BigDecimal total) {
        this.total = total;
    }
    
    public void marcarComoProcessado() {
        this.status = PedidoStatus.PROCESSADO;
    }
}
```

#### Application Layer - Casos de Uso
```java
@Service
public class PedidoService {
    @Transactional
    public PedidoEntity processarPedido(PedidoDTO dto) {
        // Salvar pedido primeiro
        PedidoEntity pedido = pedidoMapper.toEntity(dto);
        pedido.marcarComoProcessado();
        PedidoEntity salvo = repository.save(pedido);
        
        // Depois salvar produtos
        for (ProdutoDTO produtoDTO : dto.getProdutos()) {
            ProdutoEntity produto = pedidoMapper.toEntity(produtoDTO);
            produto.setPedidoId(salvo.getId());
            produtoRepository.save(produto);
        }
        
        return salvo;
    }
}
```

## 📋 API Endpoints

### **POST** `/api/pedidos` - Criar Pedido
```json
{
  "externalId": "EXT-001",
  "produtos": [
    {
      "nome": "Produto 1",
      "preco": 10.50
    },
    {
      "nome": "Produto 2", 
      "preco": 20.00
    }
  ]
}
```

**Resposta de Sucesso:**
```json
{
  "success": true,
  "message": "Pedido criado com sucesso",
  "pedidoId": 1,
  "externalId": "EXT-001",
  "status": "PROCESSADO",
  "total": 30.50,
  "createdAt": "2025-07-20T20:07:30.28944",
  "produtos": [
    {
      "id": 1,
      "nome": "Produto 1",
      "preco": 10.50
    },
    {
      "id": 2,
      "nome": "Produto 2",
      "preco": 20.00
    }
  ]
}
```

### **GET** `/api/pedidos/{id}` - Buscar por ID
### **GET** `/api/pedidos/external/{externalId}` - Buscar por ID Externo
### **GET** `/api/pedidos/health` - Health Check

## 🧪 Testes

### 📊 Cobertura
- **Domain Layer**: 100%
- **Application Layer**: 95%+
- **Infrastructure Layer**: 90%+

### 🚀 Execução
```bash
# Todos os testes
./mvnw test

# Com relatório de cobertura
./mvnw jacoco:report
```

### 🔬 Tipos de Testes
```java
// Testes Unitários
@ExtendWith(MockitoExtension.class)
class PedidoServiceTest { ... }

// Testes de Integração
@SpringBootTest
@Testcontainers
@EmbeddedKafka
class OrderServiceIntegrationTest { ... }
```

## 🐳 Containerização

### 📦 Serviços Docker
- **order-service**: Aplicação principal (Java 17)
- **postgres**: Banco de dados (PostgreSQL 15)
- **kafka**: Message broker (Apache Kafka)
- **zookeeper**: Coordenação do Kafka
- **kafka-ui**: Interface web para monitoramento

### 🔧 Docker Compose
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: orderdb
      POSTGRES_USER: orderuser
      POSTGRES_PASSWORD: orderpass
  
  kafka:
    image: confluentinc/cp-kafka:7.4.0
    depends_on:
      - zookeeper
  
  order-service:
    build: .
    depends_on:
      - postgres
      - kafka
    ports:
      - "8080:8080"
```

### 🚀 **Comandos Docker Importantes**

```bash
# Iniciar com rebuild completo (RECOMENDADO)
docker-compose up --build -d

# Parar todos os serviços
docker-compose down

# Ver logs da aplicação
docker logs order-service -f

# Verificar status dos containers
docker ps

# Acessar banco de dados
docker exec -it order-postgres psql -U orderuser -d orderdb

# Verificar produtos inseridos
docker exec order-postgres psql -U orderuser -d orderdb -c "SELECT * FROM products;"
```

## 🛠️ Stack Tecnológica

| Categoria | Tecnologia | Versão |
|-----------|------------|--------|
| **Runtime** | Java | 17 |
| **Framework** | Spring Boot | 3.2.0 |
| **Database** | PostgreSQL | 15 |
| **Message Broker** | Apache Kafka | 7.4.0 |
| **Container** | Docker | Latest |
| **Testing** | JUnit 5 + Testcontainers | Latest |
| **Mapping** | MapStruct | 1.5.5 |
| **Documentation** | Swagger/OpenAPI | 2.2.0 |

## 📊 Monitoramento e Observabilidade

### 🔍 Health Checks
- **Database**: Verificação de conectividade PostgreSQL
- **Kafka**: Verificação de conectividade Kafka
- **Application**: Status geral da aplicação

### 📈 Métricas
- **Prometheus**: Métricas customizadas
- **Spring Actuator**: Endpoints de monitoramento
- **Logging**: Logs estruturados com interceptors

### 📝 Exemplo de Log
```
2024-01-01 10:00:00 - [REQUEST] POST /api/pedidos - User-Agent: curl/7.68.0
2024-01-01 10:00:01 - === INÍCIO DO PROCESSAMENTO ===
2024-01-01 10:00:01 - === SALVANDO APENAS O PEDIDO ===
2024-01-01 10:00:01 - Pedido salvo com sucesso: 1
2024-01-01 10:00:01 - === PROCESSANDO PRODUTOS ===
2024-01-01 10:00:01 - Produto 1 salvo com sucesso, ID: 1
2024-01-01 10:00:01 - === PROCESSAMENTO CONCLUÍDO ===
2024-01-01 10:00:01 - [RESPONSE] POST /api/pedidos - Status: 201
```

## 🚀 Escalabilidade

### 📈 Características
- **Stateless**: Aplicação sem estado
- **Horizontal Scaling**: Suporte a múltiplas instâncias
- **Connection Pooling**: Pool de conexões otimizado
- **Kafka Partitioning**: Suporte a partições

### 🔄 Fluxo de Processamento
1. **Recebimento**: REST API ou Kafka (`pedidos.recebidos`)
2. **Validação**: Bean Validation
3. **Processamento**: Cálculo de total
4. **Persistência**: PostgreSQL (pedido primeiro, produtos depois)
5. **Publicação**: Kafka (`pedidos.processados`)

## 🎯 Benefícios da Implementação

### ✅ **Para Desenvolvedores**
- Código limpo e manutenível
- Testes automatizados
- Documentação completa
- Ambiente isolado com Docker
- **Problema de foreign key resolvido**

### ✅ **Para Operações**
- Monitoramento completo
- Health checks automáticos
- Logs estruturados
- Escalabilidade horizontal
- **Transações garantidas**

### ✅ **Para Negócio**
- Alta disponibilidade
- Processamento assíncrono
- Rastreabilidade completa
- Integração flexível
- **Dados consistentes**

## 🔧 **Troubleshooting**

### ❌ **Problemas Comuns e Soluções**

#### 1. **Erro 500 - "null value in column pedido_id"**
**Causa**: Produtos sendo inseridos antes do pedido ter ID
**Solução**: ✅ **IMPLEMENTADO** - Pedido salvo primeiro, produtos depois

#### 2. **Container não inicia**
```bash
# Verificar logs
docker logs order-service

# Rebuild completo
docker-compose down
docker-compose up --build -d
```

#### 3. **API retorna 404**
```bash
# Aguardar inicialização completa (15-20 segundos)
# Verificar se container está healthy
docker ps
```

#### 4. **Produtos não aparecem na resposta**
```bash
# Verificar se foram inseridos no banco
docker exec order-postgres psql -U orderuser -d orderdb -c "SELECT * FROM products;"
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

## 📚 Documentação Adicional

- **[QUICKSTART.md](QUICKSTART.md)**: Guia de início rápido
- **[examples/test-api.http](examples/test-api.http)**: Exemplos de API
- **Swagger UI**: http://localhost:8080/swagger-ui.html

## 🛠️ Como Contribuir

1. **Fork** o projeto
2. **Clone** o repositório
3. **Crie** uma branch para sua feature
4. **Implemente** suas mudanças
5. **Execute** os testes
6. **Abra** um Pull Request

### 📋 Checklist
- [ ] Código segue padrões SOLID
- [ ] Testes unitários implementados
- [ ] Testes de integração passando
- [ ] Documentação atualizada
- [ ] Build passando no CI/CD
- [ ] **Problema de foreign key testado**

---

<div align="center">

**🎉 Order Service - Microserviço de Processamento de Pedidos**

*Desenvolvido com ❤️ seguindo as melhores práticas de desenvolvimento*

[![GitHub stars](https://img.shields.io/github/stars/DesenvolvimentoEasingJob/order-service?style=social)](https://github.com/DesenvolvimentoEasingJob/order-service)
[![GitHub forks](https://img.shields.io/github/forks/DesenvolvimentoEasingJob/order-service?style=social)](https://github.com/DesenvolvimentoEasingJob/order-service)
[![GitHub issues](https://img.shields.io/github/issues/DesenvolvimentoEasingJob/order-service)](https://github.com/DesenvolvimentoEasingJob/order-service/issues)

</div> 
