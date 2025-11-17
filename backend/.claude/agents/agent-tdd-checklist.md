# Agent TDD Checklist (Simplified)

## 🎯 TDD Cycle

Every implementation follows this cycle:

```
🔴 RED: Write failing test
🟢 GREEN: Make it pass (minimal code)
🔵 REFACTOR: Improve code quality
✅ VERIFY: Run all tests
```

---

## 📋 Per-Class Checklist

### For Each Domain Entity (e.g., MenuScan, SurveyResponse)

**🔴 RED**:
- [ ] Write test for valid object creation
- [ ] Write tests for null field validation
- [ ] Write tests for business rule validation
- [ ] Run tests → Expect failures

**🟢 GREEN**:
- [ ] Create entity class with fields
- [ ] Add validation logic
- [ ] Run tests → All pass

**🔵 REFACTOR**:
- [ ] Add Javadoc
- [ ] Use Builder pattern
- [ ] Add factory methods if needed
- [ ] Run tests → Still pass

**✅ VERIFY**:
- [ ] Coverage >90% for entity
- [ ] Git commit

---

### For Each Repository (e.g., MenuScanRepository, SurveyResponseRepository)

**🔴 RED**:
- [ ] Write test for save/retrieve
- [ ] Write tests for custom queries
- [ ] Write tests for edge cases (not found, empty results)
- [ ] Run tests → Expect failures

**🟢 GREEN**:
- [ ] Create repository interface (extends JpaRepository)
- [ ] Add custom query methods
- [ ] Run tests → All pass

**🔵 REFACTOR**:
- [ ] Add Javadoc
- [ ] Optimize query methods if needed
- [ ] Run tests → Still pass

**✅ VERIFY**:
- [ ] Coverage 100% for repository
- [ ] Git commit

---

### For Each Service (e.g., ABTestService, SurveyService)

**🔴 RED**:
- [ ] Write tests for main business logic
- [ ] Write tests for validation (invalid inputs)
- [ ] Write tests for edge cases (empty data, etc.)
- [ ] Write tests for exception handling
- [ ] Run tests → Expect failures

**🟢 GREEN**:
- [ ] Implement service class
- [ ] Add business logic
- [ ] Add validation
- [ ] Add exception handling
- [ ] Run tests → All pass

**🔵 REFACTOR**:
- [ ] Extract helper methods
- [ ] Add Javadoc
- [ ] Simplify complex logic
- [ ] Run tests → Still pass

**✅ VERIFY**:
- [ ] Coverage >90% for service
- [ ] Service matches interface contract (INTERFACE_CONTRACT.md)
- [ ] Git commit

---

### For Each Controller (e.g., ABTestController, SurveyController)

**🔴 RED**:
- [ ] Write test for successful HTTP 200 responses
- [ ] Write tests for validation errors (HTTP 400)
- [ ] Write tests for JSON response structure
- [ ] Run tests → Expect failures

**🟢 GREEN**:
- [ ] Create controller class
- [ ] Add endpoint methods
- [ ] Add request validation (@Valid)
- [ ] Add error handling
- [ ] Run tests → All pass

**🔵 REFACTOR**:
- [ ] Extract DTOs if needed
- [ ] Add Javadoc
- [ ] Simplify error responses
- [ ] Run tests → Still pass

**✅ VERIFY**:
- [ ] Coverage >85% for controller
- [ ] API matches REST contract (INTERFACE_CONTRACT.md)
- [ ] Git commit

---

## 🚨 TDD Violations (Never Do This)

❌ **Write production code before tests**
❌ **Modify tests to make them pass** (only modify production code)
❌ **Skip refactoring step**
❌ **Commit code without running all tests**
❌ **Leave TODO comments in production code**
❌ **Skip tests to "save time"**

---

## ✅ Final Module Checklist

### Before Marking Agent Complete

**Domain Layer**:
- [ ] All entities tested (>90% coverage)
- [ ] Validation logic tested
- [ ] No null pointer risks

**Repository Layer**:
- [ ] All queries tested (100% coverage)
- [ ] Edge cases covered (not found, empty, etc.)
- [ ] Unique constraints tested (if applicable)

**Service Layer**:
- [ ] All business logic tested (>90% coverage)
- [ ] Exception handling tested
- [ ] Interface contract matched exactly

**Controller Layer** (if applicable):
- [ ] All endpoints tested (>85% coverage)
- [ ] HTTP status codes tested
- [ ] JSON response format tested
- [ ] Request validation tested

**Overall**:
- [ ] Total coverage >85%
- [ ] No compilation errors
- [ ] No skipped tests
- [ ] All tests pass
- [ ] Code follows existing patterns (menu/, currency/)
- [ ] Javadoc for all public methods
- [ ] Git commits have meaningful messages

---

## 📝 Quick Commands

```bash
# Run tests for your module
./gradlew test --tests "foodiepass.server.abtest.*"

# Check coverage
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html

# Run all tests (before marking complete)
./gradlew test

# Check compilation
./gradlew compileJava compileTestJava
```

---

## 🎓 Remember

- **Test first, code second** (that's the "Test-Driven" part)
- **Minimal code** to pass tests (no over-engineering)
- **Refactor without fear** (tests are your safety net)
- **Verify often** (run tests after every change)
- **Coverage is not the goal** (well-tested code is)

**Each class should take 30-120 minutes** depending on complexity.

---

## 💡 Pro Tips

- **Red phase**: Think about what the code should do (specification)
- **Green phase**: Make it work (implementation)
- **Refactor phase**: Make it clean (quality)
- **Verify phase**: Make it reliable (validation)

**If a test is hard to write**, your code design might need improvement.
**If you need to mock many things**, your class has too many dependencies.
**If a test is flaky**, it's testing time, randomness, or external state.

---

**Good luck! 🚀 Follow the cycle, and you'll produce high-quality code.**
