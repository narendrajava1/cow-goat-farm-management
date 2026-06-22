# 06 — Microservices Implementation

## 1. Project Structure (Spring Boot)

```
farm-management/
├── farm-gateway/                    # Spring Cloud Gateway
├── farm-animal-service/             # Animal & Herd management
├── farm-health-service/             # Health records
├── farm-breeding-service/           # Breeding & reproduction
├── farm-milk-service/               # Milk production
├── farm-inventory-service/          # Feed & stock
├── farm-finance-service/            # Financial tracking
├── farm-notification-service/       # Alerts & notifications
├── farm-report-service/             # Reports & analytics
├── farm-common/                     # Shared DTOs, events, utils (library)
├── docker-compose.yml
├── docker-compose.dev.yml
└── k8s/
    ├── namespaces.yml
    ├── deployments/
    ├── services/
    └── configmaps/
```

### Per-Service Structure
```
farm-animal-service/
├── src/main/java/com/cgfms/animal/
│   ├── AnimalServiceApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── RedisConfig.java
│   │   └── KafkaConfig.java
│   ├── controller/
│   │   ├── AnimalController.java
│   │   └── HerdController.java
│   ├── service/
│   │   ├── AnimalService.java
│   │   └── HerdService.java
│   ├── repository/
│   │   └── AnimalRepository.java
│   ├── domain/
│   │   ├── Animal.java             (JPA entity)
│   │   └── Herd.java
│   ├── dto/
│   │   ├── request/AnimalCreateRequest.java
│   │   └── response/AnimalResponse.java
│   ├── event/
│   │   └── AnimalEventPublisher.java
│   ├── exception/
│   │   └── GlobalExceptionHandler.java
│   └── mapper/
│       └── AnimalMapper.java       (MapStruct)
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/               (Flyway)
│       ├── V1__create_animals.sql
│       └── V2__create_herds.sql
└── pom.xml
```

---

## 2. Common Dependencies (`pom.xml` template)

```xml
<properties>
    <java.version>21</java.version>
    <spring-boot.version>3.3.0</spring-boot.version>
    <spring-cloud.version>2023.0.2</spring-cloud.version>
    <mapstruct.version>1.5.5.Final</mapstruct.version>
</properties>

<dependencies>
    <!-- Core -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>

    <!-- Messaging -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>

    <!-- Cache -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>

    <!-- Observability -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
    <dependency>
        <groupId>io.opentelemetry.instrumentation</groupId>
        <artifactId>opentelemetry-spring-boot-starter</artifactId>
    </dependency>

    <!-- DB -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>

    <!-- Utilities -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${mapstruct.version}</version>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>

    <!-- Resilience -->
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-spring-boot3</artifactId>
    </dependency>
</dependencies>
```

---

## 3. Key Code Patterns

### 3.1 Animal Entity (JPA)
```java
@Entity
@Table(name = "animals")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "farm_id", nullable = false)
    private UUID farmId;

    @Column(name = "tag_number", nullable = false)
    private String tagNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "animal_type", nullable = false)
    private AnimalType animalType;       // COW | GOAT

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnimalStatus status = AnimalStatus.ACTIVE;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "herd_id")
    private Herd herd;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    // Business method — encapsulate status transitions
    public void markAsSold(LocalDate effectiveDate) {
        if (this.status != AnimalStatus.ACTIVE) {
            throw new InvalidStatusTransitionException(
                "Only ACTIVE animals can be sold. Current: " + this.status);
        }
        this.status = AnimalStatus.SOLD;
    }
}
```

### 3.2 Service Layer with Caching
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final AnimalEventPublisher eventPublisher;
    private final AnimalMapper mapper;

    @Transactional
    public AnimalResponse registerAnimal(AnimalCreateRequest request, UUID farmId) {
        // Validate tag uniqueness
        if (animalRepository.existsByFarmIdAndTagNumber(farmId, request.getTagNumber())) {
            throw new DuplicateTagException(request.getTagNumber());
        }

        Animal animal = mapper.toEntity(request);
        animal.setFarmId(farmId);
        Animal saved = animalRepository.save(animal);

        // Publish event for downstream services
        eventPublisher.publishAnimalRegistered(saved);

        log.info("Animal registered: tag={}, type={}", saved.getTagNumber(), saved.getAnimalType());
        return mapper.toResponse(saved);
    }

    @Cacheable(value = "animals", key = "#animalId")
    @Transactional(readOnly = true)
    public AnimalResponse getAnimal(UUID animalId) {
        return animalRepository.findById(animalId)
            .map(mapper::toResponse)
            .orElseThrow(() -> new AnimalNotFoundException(animalId));
    }

    @CacheEvict(value = "animals", key = "#animalId")
    @Transactional
    public AnimalResponse updateAnimal(UUID animalId, AnimalUpdateRequest request) {
        Animal animal = findOrThrow(animalId);
        mapper.updateEntity(request, animal);
        return mapper.toResponse(animalRepository.save(animal));
    }
}
```

### 3.3 Kafka Event Publishing
```java
@Component
@RequiredArgsConstructor
public class AnimalEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishAnimalRegistered(Animal animal) {
        AnimalRegisteredEvent event = AnimalRegisteredEvent.builder()
            .animalId(animal.getId())
            .farmId(animal.getFarmId())
            .tagNumber(animal.getTagNumber())
            .animalType(animal.getAnimalType().name())
            .occurredAt(Instant.now())
            .build();

        kafkaTemplate.send("animal.events", animal.getId().toString(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish animal.registered event for {}", animal.getId(), ex);
                }
            });
    }
}
```

### 3.4 Kafka Event in farm-common (shared DTO)
```java
// farm-common/src/main/java/com/cgfms/common/event/AnimalRegisteredEvent.java
@Data
@Builder
public class AnimalRegisteredEvent {
    private UUID animalId;
    private UUID farmId;
    private String tagNumber;
    private String animalType;
    private Instant occurredAt;
    public static final String TOPIC = "animal.events";
    public static final String EVENT_TYPE = "ANIMAL_REGISTERED";
}
```

### 3.5 Health Service consuming Animal events
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class AnimalEventConsumer {

    private final HealthProfileService healthProfileService;

    @KafkaListener(topics = "animal.events", groupId = "health-service")
    public void handleAnimalEvent(@Payload AnimalRegisteredEvent event,
                                  @Header("eventType") String eventType) {
        if ("ANIMAL_REGISTERED".equals(eventType)) {
            log.info("Creating health profile for new animal: {}", event.getAnimalId());
            healthProfileService.createInitialProfile(event);
        }
    }
}
```

### 3.6 Circuit Breaker (inter-service calls)
```java
@Service
public class BreedingService {

    @CircuitBreaker(name = "animal-service", fallbackMethod = "getAnimalFallback")
    @Retry(name = "animal-service")
    public AnimalInfo getAnimalInfo(UUID animalId) {
        return animalServiceClient.getAnimal(animalId);
    }

    private AnimalInfo getAnimalFallback(UUID animalId, Exception ex) {
        log.warn("Animal service unavailable, using cached data for {}", animalId);
        return cacheService.getCachedAnimal(animalId)
            .orElseThrow(() -> new ServiceUnavailableException("animal-service"));
    }
}
```

---

## 4. Kafka Topics & Schemas

| Topic | Producer | Consumers | Events |
|---|---|---|---|
| `animal.events` | Animal Service | Health, Breeding, Notification, Finance | REGISTERED, STATUS_CHANGED |
| `health.events` | Health Service | Notification Service | VACCINATION_DUE, TREATMENT_STARTED |
| `breeding.events` | Breeding Service | Animal Service, Notification | PREGNANCY_CONFIRMED, BIRTH_DUE, BIRTH_RECORDED |
| `milk.events` | Milk Service | Report Service | DAILY_SUMMARY_READY |
| `inventory.events` | Inventory Service | Notification, Finance | LOW_STOCK, PROCUREMENT_DONE |
| `notification.send` | All services | Notification Service | EMAIL, SMS, PUSH |

---

## 5. Configuration (`application.yml` template)

```yaml
spring:
  application:
    name: farm-animal-service
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5432/farm_animals
    username: ${DB_USER:farm_user}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
  jpa:
    hibernate:
      ddl-auto: validate        # Flyway manages schema
    properties:
      hibernate.default_schema: farm_animals
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: 6379
      password: ${REDIS_PASSWORD:}
      lettuce:
        pool:
          max-active: 8
  kafka:
    bootstrap-servers: ${KAFKA_BROKERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: animal-service
      auto-offset-reset: earliest
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_URL}/realms/farm-management

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  metrics:
    export:
      prometheus:
        enabled: true

resilience4j:
  circuitbreaker:
    instances:
      animal-service:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s

logging:
  level:
    com.cgfms: DEBUG
  pattern:
    console: "%d{HH:mm:ss} [%thread] [traceId=%X{traceId}] %-5level %logger{36} - %msg%n"
```
