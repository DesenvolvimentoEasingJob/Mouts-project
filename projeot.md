# 📦 Order Service - Documentação de Implementação

## 🎯 Objetivo

Criar um serviço chamado `order`, utilizando Java + Spring Boot, que:

- Consome pedidos do Produto Externo A (sistema externo)
- Processa esses pedidos, somando os valores dos produtos
- Armazena os pedidos processados em um banco de dados
- Publica os resultados processados em um tópico Kafka
- Permite que o Produto Externo B consuma esses resultados
- Seja escalável, seguro e testável isoladamente via Docker

---

## 🧰 Tecnologias Utilizadas

- Java 17
- Spring Boot 3+
- Apache Kafka (`pedidos.recebidos`, `pedidos.processados`)
- PostgreSQL
- Docker / Docker Compose
- JUnit 5 + Testcontainers
- Lombok
- MapStruct
- Spring Validation
- Spring Actuator
- Spring Retry (para falhas de Kafka)
- Swagger/OpenAPI (para documentação de API)

---

## 🗂️ Estrutura do Projeto

```bash
order-service/
├── docker/
│   ├── kafka/
│   ├── postgres/
├── src/
│   ├── main/java/com/example/order/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── repository/
│   │   ├── service/
│   │   ├── mapper/
│   │   ├── config/
│   │   └── OrderServiceApplication.java
│   ├── test/java/com/example/order/
│   └── resources/
├── Dockerfile
├── docker-compose.yml
└── README.md
📦 Entidades e DTOs
PedidoEntity.java
java
Copiar
Editar
@Entity
@Table(name = "orders")
public class PedidoEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String externalId;
    private BigDecimal total;
    private PedidoStatus status;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<ProdutoEntity> produtos;
}
ProdutoEntity.java
java
Copiar
Editar
@Entity
@Table(name = "products")
public class ProdutoEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private BigDecimal preco;

    @ManyToOne
    @JoinColumn(name = "pedido_id")
    private PedidoEntity pedido;
}
PedidoDTO.java
java
Copiar
Editar
public class PedidoDTO {
    @NotBlank
    private String externalId;

    @NotEmpty
    private List<@Valid ProdutoDTO> produtos;
}
ProdutoDTO.java
java
Copiar
Editar
public class ProdutoDTO {
    @NotBlank
    private String nome;

    @NotNull @Positive
    private BigDecimal preco;
}
⚙️ Serviço de Processamento
PedidoService.java
java
Copiar
Editar
@Service
public class PedidoService {

    @Autowired private PedidoRepository repository;
    @Autowired private PedidoMapper mapper;
    @Autowired private KafkaPedidoProducer producer;

    public PedidoEntity processarPedido(PedidoDTO dto) {
        PedidoEntity pedido = mapper.toEntity(dto);
        pedido.setStatus(PedidoStatus.PROCESSADO);
        BigDecimal total = pedido.getProdutos().stream()
            .map(ProdutoEntity::getPreco)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setTotal(total);
        PedidoEntity salvo = repository.save(pedido);
        producer.enviarPedidoProcessado(salvo);
        return salvo;
    }
}
🎯 Controller REST
PedidoController.java
java
Copiar
Editar
@RestController
@RequestMapping("/api/pedidos")
@Validated
public class PedidoController {

    @Autowired private PedidoService service;

    @PostMapping
    public ResponseEntity<PedidoEntity> criar(@RequestBody @Valid PedidoDTO dto) {
        PedidoEntity entity = service.processarPedido(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(entity);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoEntity> buscar(@PathVariable Long id) {
        return ResponseEntity.of(repository.findById(id));
    }
}
🧩 Middlewares
LoggingInterceptor.java
java
Copiar
Editar
@Component
public class LoggingInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        log.info("[{}] {}", req.getMethod(), req.getRequestURI());
        return true;
    }
}
WebConfig.java
java
Copiar
Editar
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired private LoggingInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor);
    }
}
☁️ Kafka - Producer & Consumer
KafkaPedidoConsumer.java
java
Copiar
Editar
@KafkaListener(topics = "pedidos.recebidos", groupId = "order-group")
public void consumir(String json) {
    try {
        PedidoDTO dto = objectMapper.readValue(json, PedidoDTO.class);
        service.processarPedido(dto);
    } catch (Exception ex) {
        log.error("Erro ao consumir pedido: {}", ex.getMessage());
        // pode publicar em DLQ ou usar SeekToCurrentErrorHandler
    }
}
KafkaPedidoProducer.java
java
Copiar
Editar
public void enviarPedidoProcessado(PedidoEntity pedido) {
    String json = objectMapper.writeValueAsString(pedido);
    kafkaTemplate.send("pedidos.processados", json);
}
🐳 Docker
Dockerfile
dockerfile
Copiar
Editar
FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/order-service.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
docker-compose.yml
yaml
Copiar
Editar
version: '3.8'

services:
  postgres:
    image: postgres
    environment:
      POSTGRES_DB: orderdb
      POSTGRES_USER: user
      POSTGRES_PASSWORD: pass
    ports:
      - "5432:5432"

  zookeeper:
    image: bitnami/zookeeper:latest
    ports:
      - "2181:2181"

  kafka:
    image: bitnami/kafka:latest
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_CFG_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_CFG_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
    ports:
      - "9092:9092"

  order:
    build: .
    depends_on:
      - kafka
      - postgres
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/orderdb
      SPRING_DATASOURCE_USERNAME: user
      SPRING_DATASOURCE_PASSWORD: pass
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    ports:
      - "8080:8080"
🧪 Testes Automatizados
PedidoServiceTest.java
java
Copiar
Editar
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public class PedidoServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired PedidoService service;

    @Test
    void deveProcessarEPersistirPedido() {
        PedidoDTO dto = new PedidoDTO(...);
        PedidoEntity entity = service.processarPedido(dto);
        assertNotNull(entity.getId());
        assertEquals(PedidoStatus.PROCESSADO, entity.getStatus());
    }
}
🧪 Testes Kafka
Use @EmbeddedKafka para simular Kafka localmente nos testes:

java
Copiar
Editar
@EmbeddedKafka(partitions = 1, topics = { "pedidos.recebidos", "pedidos.processados" })
@SpringBootTest
public class KafkaIntegrationTest { ... }
▶️ Execução Local
bash
Copiar
Editar
# 1. Clonar o repositório
git clone https://github.com/seuusuario/order-service.git
cd order-service

# 2. Buildar o projeto
./mvnw clean package -DskipTests

# 3. Subir infraestrutura com Docker
docker-compose up --build

# 4. Testar API
curl -X POST http://localhost:8080/api/pedidos -H "Content-Type: application/json" -d '{...}'

# 5. Consumir/produzir mensagens com Kafka CLI