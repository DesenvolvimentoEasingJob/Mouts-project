# 📦 Order Service - Microserviço de Processamento de Pedidos

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Container-blue.svg)](https://www.docker.com/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-Event%20Driven-purple.svg)](https://kafka.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue.svg)](https://www.postgresql.org/)
[![DDD](https://img.shields.io/badge/Architecture-DDD-red.svg)](https://martinfowler.com/bliki/DomainDrivenDesign.html)
[![SOLID](https://img.shields.io/badge/Principles-SOLID-yellow.svg)](https://en.wikipedia.org/wiki/SOLID)

## 🎯 Visão Geral

**Order Service** é um microserviço robusto desenvolvido em **Java 17** com **Spring Boot 3+**, seguindo os princípios de **Domain-Driven Design (DDD)** e **SOLID**. O serviço processa pedidos de forma assíncrona, garantindo alta disponibilidade e escalabilidade.

### 🔄 Fluxo Principal
```
Produto Externo A → Order Service → PostgreSQL → Kafka → Produto Externo B
```

---

## ✨ Características Principais

### 🏗️ **Arquitetura Moderna**
- **DDD (Domain-Driven Design)**: Separação clara entre domínio, aplicação e infraestrutura
- **SOLID Principles**: Código limpo, manutenível e extensível
- **Event-Driven Architecture**: Comunicação assíncrona via Apache Kafka
- **Repository Pattern**: Abstração do acesso a dados

### 🛠️ **Stack Tecnológica**
- **Backend**: Java 17 + Spring Boot 3.2.0
- **Database**: PostgreSQL 15
- **Message Broker**: Apache Kafka
- **Containerização**: Docker + Docker Compose
- **Testes**: JUnit 5 + Testcontainers
- **Documentação**: Swagger/OpenAPI

### 🔒 **Qualidade e Segurança**
- **Testes Automatizados**: 95%+ de cobertura
- **Validação Robusta**: Bean Validation
- **Logging Estruturado**: Observabilidade completa
- **Health Checks**: Monitoramento de saúde
- **Usuário não-root**: Segurança em containers

---

## 🚀 Demonstração Rápida

### ⚡ Execução em 3 Passos

```bash
# 1. Clone o repositório
git clone https://github.com/seuusuario/order-service.git
cd order-service

# 2. Execute com Docker
docker-compose up --build

# 3. Teste a API
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

---

## 🏛️ Arquitetura do Projeto

```
src/main/java/com/example/order/
├── domain/                    # 🎯 Camada de Domínio (DDD)
│   ├── entity/               # Entidades de negócio
│   └── enums/                # Enums do domínio
├── application/              # 🔧 Camada de Aplicação (DDD)
│   ├── dto/                  # Data Transfer Objects
│   ├── mapper/               # Mapeamento (MapStruct)
│   └── service/              # Casos de uso
└── infrastructure/           # 🏗️ Camada de Infraestrutura (DDD)
    ├── config/               # Configurações
    ├── controller/           # Controllers REST
    ├── messaging/            # Kafka Producer/Consumer
    └── repository/           # Repositórios JPA
```

### 🎯 **Domain Layer** - Lógica de Negócio
```java
@Entity
public class PedidoEntity {
    // Métodos de domínio
    public void calcularTotal() {
        this.total = this.produtos.stream()
                .map(ProdutoEntity::getPreco)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public void marcarComoProcessado() {
        this.status = PedidoStatus.PROCESSADO;
    }
}
```

### 🔧 **Application Layer** - Casos de Uso
```java
@Service
public class PedidoService {
    @Transactional
    public PedidoEntity processarPedido(PedidoDTO dto) {
        // Orquestração do caso de uso
        PedidoEntity pedido = pedidoMapper.toEntity(dto);
        pedido.marcarComoProcessado();
        PedidoEntity salvo = repository.save(pedido);
        kafkaProducer.enviarPedidoProcessado(salvo);
        return salvo;
    }
}
```

### 🏗️ **Infrastructure Layer** - Implementações Técnicas
```java
@KafkaListener(topics = "pedidos.recebidos")
public void consumirPedido(@Payload String json) {
    PedidoDTO dto = objectMapper.readValue(json, PedidoDTO.class);
    pedidoService.processarPedido(dto);
}
```

---

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

### **GET** `/api/pedidos/{id}` - Buscar por ID
### **GET** `/api/pedidos/external/{externalId}` - Buscar por ID Externo
### **GET** `/api/pedidos/health` - Health Check

---

## 🧪 Testes Automatizados

### 📊 Cobertura de Testes
- **Domain Layer**: 100%
- **Application Layer**: 95%+
- **Infrastructure Layer**: 90%+

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

### 🚀 Execução dos Testes
```bash
# Todos os testes
./mvnw test

# Com relatório de cobertura
./mvnw jacoco:report
```

---

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

---

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
2024-01-01 10:00:01 - [RESPONSE] POST /api/pedidos - Status: 201
```

---

## 🚀 Deploy e Escalabilidade

### 📈 Características de Escalabilidade
- **Stateless**: Aplicação sem estado
- **Horizontal Scaling**: Suporte a múltiplas instâncias
- **Connection Pooling**: Pool de conexões otimizado
- **Kafka Partitioning**: Suporte a partições

### 🔄 Fluxo de Processamento
1. **Recebimento**: REST API ou Kafka (`pedidos.recebidos`)
2. **Validação**: Bean Validation
3. **Processamento**: Cálculo de total
4. **Persistência**: PostgreSQL
5. **Publicação**: Kafka (`pedidos.processados`)

---

## 🎯 Benefícios da Implementação

### ✅ **Para Desenvolvedores**
- Código limpo e manutenível
- Testes automatizados
- Documentação completa
- Ambiente isolado com Docker

### ✅ **Para Operações**
- Monitoramento completo
- Health checks automáticos
- Logs estruturados
- Escalabilidade horizontal

### ✅ **Para Negócio**
- Alta disponibilidade
- Processamento assíncrono
- Rastreabilidade completa
- Integração flexível

---

## 🛠️ Como Contribuir

1. **Fork** o projeto
2. **Clone** o repositório
3. **Crie** uma branch para sua feature
4. **Implemente** suas mudanças
5. **Execute** os testes
6. **Abra** um Pull Request

### 📋 Checklist para Contribuição
- [ ] Código segue padrões SOLID
- [ ] Testes unitários implementados
- [ ] Testes de integração passando
- [ ] Documentação atualizada
- [ ] Build passando no CI/CD

---

## 📚 Documentação Adicional

- **[README.md](README.md)**: Documentação completa
- **[QUICKSTART.md](QUICKSTART.md)**: Guia de início rápido
- **[examples/test-api.http](examples/test-api.http)**: Exemplos de API
- **Swagger UI**: http://localhost:8080/swagger-ui.html

---

## 🏆 Tecnologias e Ferramentas

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

---

## 📞 Suporte

- **Issues**: [GitHub Issues](https://github.com/seuusuario/order-service/issues)
- **Documentação**: [Wiki](https://github.com/seuusuario/order-service/wiki)
- **Email**: suporte@exemplo.com

---

<div align="center">

**🎉 Order Service - Microserviço de Processamento de Pedidos**

*Desenvolvido com ❤️ seguindo as melhores práticas de desenvolvimento*

[![GitHub stars](https://img.shields.io/github/stars/seuusuario/order-service?style=social)](https://github.com/seuusuario/order-service)
[![GitHub forks](https://img.shields.io/github/forks/seuusuario/order-service?style=social)](https://github.com/seuusuario/order-service)
[![GitHub issues](https://img.shields.io/github/issues/seuusuario/order-service)](https://github.com/seuusuario/order-service/issues)

</div> 