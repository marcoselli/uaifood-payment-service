# UAIFood Payment Service

## Descrição
O UAIFood Payment Service é um microsserviço responsável por gerenciar os pagamentos do sistema UAIFood. Este serviço processa pagamentos, gerencia eventos de pagamento e mantém o histórico de transações.

## Tecnologias Utilizadas
- Kotlin 1.9.x
- Spring Boot 3.2.3
- PostgreSQL 15 (Banco de dados principal)
- MongoDB 6 (Armazenamento de logs e eventos)
- RabbitMQ (Mensageria)
- JUnit 5 (Testes)
- Docker & Docker Compose (Containerização)

## Pré-requisitos
- JDK 17 ou superior
- Docker e Docker Compose
- Gradle 8.x

## Estrutura do Projeto
```
uaifood-payment-service/
├── src/
│   ├── main/
│   │   ├── kotlin/br/edu/uaifood/payment/
│   │   │   ├── config/         # Configurações da aplicação
│   │   │   ├── controller/     # Controladores REST
│   │   │   ├── domain/         # Entidades e modelos de domínio
│   │   │   ├── repository/     # Repositórios JPA e MongoDB
│   │   │   ├── service/        # Lógica de negócio
│   │   │   └── util/           # Utilitários
│   │   └── resources/
│   │       └── application.yml # Configurações da aplicação
│   └── test/                   # Testes unitários e de integração
├── docker-compose.yml          # Configuração dos containers
└── build.gradle.kts           # Dependências e configurações do build
```

## Modelo de Dados

### Tabela: payments
- `id`: UUID (PK)
- `order_id`: UUID (FK para o serviço de pedidos)
- `amount`: DECIMAL(10,2)
- `currency`: VARCHAR(3) (default: 'BRL')
- `payment_method`: VARCHAR(50)
- `status`: VARCHAR(20) (PENDING, PROCESSING, APPROVED, FAILED, REFUNDED)
- `created_at`: TIMESTAMP
- `updated_at`: TIMESTAMP

### Tabela: payment_events
- `id`: UUID (PK)
- `payment_id`: UUID (FK para payments)
- `order_id`: UUID
- `event_type`: VARCHAR(50)
- `status`: VARCHAR(20) (PENDING, PROCESSING, APPROVED, REJECTED, CANCELLED, REFUNDED)
- `amount`: DECIMAL(10,2)
- `metadata`: JSONB
- `created_at`: TIMESTAMP

## Endpoints da API

### Pagamentos
- `POST /api/v1/payments` - Criar novo pagamento
- `GET /api/v1/payments/{id}` - Buscar pagamento por ID
- `GET /api/v1/payments/order/{orderId}` - Buscar pagamento por ID do pedido
- `GET /api/v1/payments` - Listar todos os pagamentos
- `GET /api/v1/payments/status/{status}` - Listar pagamentos por status
- `POST /api/v1/payments/{id}/process` - Processar pagamento
- `PUT /api/v1/payments/{id}/status` - Atualizar status do pagamento

## Configuração do Ambiente

### Variáveis de Ambiente
```yaml
# PostgreSQL
POSTGRES_DB: uaifood_payment
POSTGRES_USER: postgres
POSTGRES_PASSWORD: postgres
POSTGRES_PORT: 5433

# MongoDB
MONGO_INITDB_DATABASE: uaifood_payment
MONGO_PORT: 27018

# RabbitMQ
RABBITMQ_DEFAULT_USER: guest
RABBITMQ_DEFAULT_PASS: guest
RABBITMQ_PORT: 5673
```

### Iniciando o Ambiente
1. Clone o repositório
2. Execute `docker-compose up -d` para iniciar os serviços (PostgreSQL, MongoDB e RabbitMQ)
3. Execute `./gradlew bootRun` para iniciar a aplicação

## Executando os Testes
```bash
# Executar todos os testes
./gradlew test

# Executar testes específicos
./gradlew test --tests "br.edu.uaifood.payment.controller.PaymentControllerTest"
```

## Manutenção

### Logs
- Logs da aplicação: `logs/application.log`
- Logs do PostgreSQL: `docker logs uaifood-payment-postgres`
- Logs do MongoDB: `docker logs uaifood-payment-mongodb`
- Logs do RabbitMQ: `docker logs uaifood-payment-rabbitmq`

### Backup
- PostgreSQL: Os dados são persistidos no volume `postgres_data`
- MongoDB: Os dados são persistidos no volume `mongodb_data`

### Monitoramento
- Health Check: `GET /actuator/health`
- Métricas: `GET /actuator/metrics`

## Fluxo de Pagamento
1. Criação do pagamento (status: PENDING)
2. Processamento do pagamento (status: PROCESSING)
3. Aprovação/Rejeição do pagamento (status: APPROVED/FAILED)
4. Eventual reembolso (status: REFUNDED)

## Troubleshooting

### Problemas Comuns
1. **Erro de conexão com PostgreSQL**
   - Verifique se o container está rodando: `docker ps`
   - Verifique os logs: `docker logs uaifood-payment-postgres`
   - Confirme as credenciais no `application.yml`

2. **Erro de conexão com RabbitMQ**
   - Verifique se o container está rodando: `docker ps`
   - Verifique os logs: `docker logs uaifood-payment-rabbitmq`
   - Confirme as configurações no `application.yml`

3. **Erro de conexão com MongoDB**
   - Verifique se o container está rodando: `docker ps`
   - Verifique os logs: `docker logs uaifood-payment-mongodb`
   - Confirme as configurações no `application.yml`

### Comandos Úteis
```bash
# Reiniciar todos os serviços
docker-compose down && docker-compose up -d

# Limpar dados do PostgreSQL
docker-compose exec postgres psql -U postgres -d uaifood_payment -c "TRUNCATE payments, payment_events CASCADE;"

# Verificar status dos containers
docker-compose ps

# Verificar logs em tempo real
docker-compose logs -f
```

## Contribuição
1. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
2. Faça commit das suas alterações (`git commit -am 'Adiciona nova feature'`)
3. Faça push para a branch (`git push origin feature/nova-feature`)
4. Crie um Pull Request

## Contatos
Para suporte ou dúvidas, entre em contato com a equipe de desenvolvimento. 