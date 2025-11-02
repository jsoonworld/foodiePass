# FoodiePass Backend

Spring Boot REST API server for FoodiePass application.

## Tech Stack

- **Framework**: Spring Boot 3.5.3
- **Language**: Java 21
- **Database**: H2 (development), MySQL (production)
- **Build Tool**: Gradle 8.x

## Prerequisites

- Java 21 or higher
- Gradle (or use included wrapper)

## Getting Started

### 1. Install Dependencies
```bash
./gradlew build
```

### 2. Run the Application
```bash
./gradlew bootRun
```

The server will start on `http://localhost:8080`

### 3. Run Tests
```bash
./gradlew test
```

### 4. Run Performance Tests
```bash
node currency-api-performance-test.js
node reconfigure-menu-load-test.js
```

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/foodiepass/server/
│   │   │   ├── common/           # Common utilities and value objects
│   │   │   ├── currency/         # Currency conversion service
│   │   │   ├── language/         # Translation service
│   │   │   ├── menu/             # Menu OCR and processing
│   │   │   ├── order/            # Order management
│   │   │   ├── script/           # Order script generation
│   │   │   └── global/           # Global configurations and error handling
│   │   └── resources/
│   │       └── application.yml   # Application configuration
│   └── test/
│       └── java/foodiepass/server/
```

## Domain Modules

### Menu
- OCR text extraction from menu images
- Food image scraping from TasteAtlas
- Menu item enrichment with Gemini AI

### Language
- Translation using Google Cloud Translation API
- Support for multiple languages

### Currency
- Real-time currency conversion
- Rate caching and updates

### Order
- Order cart management
- Order item processing

### Script
- Generate bilingual order scripts
- Format for restaurant staff

## Configuration

### Environment Variables
Create `src/main/resources/application-local.yml` for local development:

```yaml
# Add your configuration here
```

### API Keys Required
- Google Cloud (for Vertex AI and Translation)
- Currency API key
- (Add others as needed)

## API Endpoints

See [API Contract](../docs/API_CONTRACT.md) _(TBD)_ for detailed API documentation.

## Development

### Code Style
- Follow Java standard conventions
- Use Lombok for boilerplate reduction
- Write tests for all business logic

### Testing Strategy
- Unit tests for domain logic
- Integration tests for API endpoints
- Performance tests for external API calls

## Troubleshooting

### Common Issues
1. **Port 8080 already in use**: Change port in `application.yml`
2. **API key errors**: Check your environment variables
3. **Build failures**: Run `./gradlew clean build`

## Related Documentation

- [Project README](../README.md)
- [Technical Specification](../docs/TECH_SPEC.md)
- [API Contract](../docs/API_CONTRACT.md) _(TBD)_
