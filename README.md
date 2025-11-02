# FoodiePass 🌮🍜🌍

> **📌 Monorepo Structure - v2 Development**
>
> This repository has been restructured to support both frontend and backend development.
> Looking for the original prototype? Check out [v1.0.0 Release](https://github.com/jsoonworld/foodiePass/releases/tag/v1.0.0) or [legacy-v1 branch](https://github.com/jsoonworld/foodiePass/tree/legacy-v1)

<img width="100%" alt="Adobe Express - foodiepass-시연영상 (1)" src="https://github.com/user-attachments/assets/73cc8fb6-793a-4c89-a05b-8a809fe7b068">

> **Scan, See, Taste. Instantly understand any foreign menu with translations, currency conversion, and food photos.**

FoodiePass is designed for every traveler who has ever hesitated to order from a menu in a foreign language. It's more than just a translator; it's a visual guide to your next great meal. Simply take a picture of the menu, and our OCR technology will serve you a fully understandable menu with translations, converted prices, and pictures of the dishes.

## 📁 Project Structure

```
foodiePass/
├── backend/          # Spring Boot API server
├── frontend/         # React/Next.js web application
├── docs/            # Project documentation
├── scripts/         # Development and deployment scripts
└── .claude/         # AI assistant context
```

## 🚀 Quick Start

### Option 1: Using Scripts (Recommended)

From the project root:

```bash
# Start backend server
./scripts/dev-backend.sh

# Run tests
./scripts/test-backend.sh

# Build project
./scripts/build-backend.sh
```

### Option 2: Manual Commands

#### Backend
```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

#### Frontend (once set up)
```bash
cd frontend
npm install
npm run dev
```

For detailed setup instructions, see:
- [Backend README](./backend/README.md)
- [Frontend README](./frontend/README.md)

## 📚 Documentation

- [1-Pager](./docs/1-PAGER.md) - Project vision and goals
- [PRD](./docs/PRD.md) - Product requirements
- [Tech Spec](./docs/TECH_SPEC.md) - Technical architecture
- [API Contract](./docs/API_CONTRACT.md) _(TBD)_ - Frontend-Backend API specification

## Core Features

* **Instant Menu Scan**: Capture any menu, and our OCR engine instantly extracts the dishes and prices.
* **Real-time Translation**: Menu items are immediately translated into your preferred language.
* **Live Currency Conversion**: See prices in your home currency, calculated with up-to-date exchange rates.
* **Food Photo Display**: Don't just read the name—see a photo of the dish to know exactly what you're ordering.
* **Allergen & Ingredient Info**: We provide detailed food descriptions, including potential allergens and main ingredients, to help you make safe and informed choices.
* **Easy Ordering & Customization**: Add items to your cart, adjust quantities, and select custom options through a user-friendly interface. Finalize your order and show the generated script to the waiter.

## Tech Stack

### Backend
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 21
- **Database**: H2 (dev), MySQL (prod)
- **External APIs**:
  - Google Cloud (Vertex AI, Translation)
  - TasteAtlas (food images)
  - Currency API

### Frontend
- **Framework**: TBD (React/Next.js)
- **Language**: TypeScript
- **State Management**: TBD
- **Styling**: TBD

## Development Workflow

### Branch Strategy
- `main`: Production-ready code
- `develop`: Main development branch
- `feature/*`: Feature development branches

### Commit Guidelines
- Follow conventional commits format
- Include meaningful commit messages
- Reference issue numbers when applicable

## Contributing

1. Create a feature branch from `develop`
2. Make your changes
3. Write/update tests
4. Submit a pull request to `develop`

## License

[License information to be added]

## Goal

To empower any traveler, in any restaurant worldwide, to enjoy the delightful experience of food without barriers. Our mission is to make FoodiePass an essential and joyful companion for every culinary journey.
