# Plan: Expense CRUD Feature

## Context
Adding a full CRUD `Expense` entity to the expense tracker app. Expenses have a many-to-one relationship with `Category`. The feature includes pagination, filtering by category/date/amount, and avoids N+1 queries using `@EntityGraph`. Follows all existing patterns (no Lombok, PRG, Bean Validation on form objects, typed exceptions, Thymeleaf templates).

---

## Files to Create

### Domain
- `src/main/java/com/example/expensetracker/domain/Expense.java`
  - Fields: `Long id`, `String description`, `BigDecimal amount`, `LocalDate date`, `Category category`, `LocalDateTime createdAt`, `LocalDateTime updatedAt`
  - Annotations: `@Entity`, `@Table(name = "expenses")`
  - `@ManyToOne(fetch = FetchType.LAZY)` + `@JoinColumn(name = "category_id", nullable = false)` on `category`
  - `@PrePersist`/`@PreUpdate` lifecycle hooks for audit timestamps
  - No Lombok — plain getters/setters

### Repository
- `src/main/java/com/example/expensetracker/repository/ExpenseRepository.java`
  - Extends `JpaRepository<Expense, Long>`, `JpaSpecificationExecutor<Expense>`
  - Override `findAll(Specification<Expense>, Pageable)` with `@EntityGraph(attributePaths = {"category"})` to avoid N+1 on all filtered/paginated queries
  - Add `boolean existsByCategoryId(Long categoryId)` for category-in-use check

### Service
- `src/main/java/com/example/expensetracker/service/ExpenseSpecification.java`
  - Static factory methods returning `Specification<Expense>` predicates:
    - `hasCategory(Long categoryId)`
    - `dateFrom(LocalDate from)`
    - `dateTo(LocalDate to)`
    - `minAmount(BigDecimal min)`
    - `maxAmount(BigDecimal max)`
  - Compose with `Specification.where(...).and(...)` in the service

- `src/main/java/com/example/expensetracker/service/ExpenseService.java`
  - `@Service`, constructor injection of `ExpenseRepository` + `CategoryRepository`
  - Methods:
    - `Page<Expense> getExpenses(ExpenseFilterForm filter, Pageable pageable)` — builds spec, calls `expenseRepository.findAll(spec, pageable)`
    - `Expense getExpense(Long id)` — throws `ResourceNotFoundException`
    - `Expense createExpense(ExpenseForm form)` — resolves category, throws `ResourceNotFoundException` if not found
    - `Expense updateExpense(Long id, ExpenseForm form)` — same category resolution
    - `void deleteExpense(Long id)` — throws `ResourceNotFoundException` if not found

### Form Objects
- `src/main/java/com/example/expensetracker/web/form/ExpenseForm.java`
  - Fields: `Long categoryId` (@NotNull), `String description` (@NotBlank, @Size(max=255)), `BigDecimal amount` (@NotNull, @DecimalMin("0.01")), `LocalDate date` (@NotNull, @PastOrPresent)
  - Plain getters/setters

- `src/main/java/com/example/expensetracker/web/form/ExpenseFilterForm.java`
  - Fields (all optional): `Long categoryId`, `LocalDate dateFrom`, `LocalDate dateTo`, `BigDecimal minAmount`, `BigDecimal maxAmount`
  - No validation annotations — used only as filter input

### Controller
- `src/main/java/com/example/expensetracker/web/ExpenseController.java`
  - Constructor injection: `ExpenseService`, `CategoryService`
  - `GET /expenses` — accepts `ExpenseFilterForm` as `@ModelAttribute` + `Pageable` (default: 20/page, sort by date desc); populates `expenses` (Page), `filter`, `categories`, `currentPage`, `totalPages`
  - `GET /expenses/new` — populates `expenseForm`, `categories`, `isEdit=false`
  - `POST /expenses` — creates, redirects to `/expenses` with success flash
  - `GET /expenses/{id}/edit` — populates `expenseForm`, `categories`, `isEdit=true`, `expenseId`
  - `POST /expenses/{id}` — updates, redirects to `/expenses` with success flash
  - `POST /expenses/{id}/delete` — deletes, redirects to `/expenses` with success flash
  - All validation failures re-render form with `categories` list repopulated

### Templates
- `src/main/resources/templates/expenses/list.html`
  - Header/flash fragments reused from `fragments.html`
  - Filter form at top: category dropdown, dateFrom/dateTo date inputs, minAmount/maxAmount number inputs, "Filter" button, "Clear" link
  - Table: Date | Description | Category | Amount | Actions (Edit, Delete)
  - Pagination controls (prev/next page links, current/total pages)
  - Empty state message when no expenses

- `src/main/resources/templates/expenses/form.html`
  - Same layout as `categories/form.html`
  - Fields: description (text), amount (number, step="0.01"), date (date), category (select dropdown)
  - Dynamic title/action (new vs. edit) via `isEdit` flag
  - Inline validation errors per field

---

## Files to Modify

### CategoryService — add expense-in-use check
- `src/main/java/com/example/expensetracker/service/CategoryService.java`
  - Add `ExpenseRepository` constructor parameter
  - In `deleteCategory(Long id)`: call `expenseRepository.existsByCategoryId(id)`, throw `CategoryInUseException("Category is in use by one or more expenses.")` if true
  - CategoryController already catches `CategoryInUseException` and shows flash error — no controller changes needed

### fragments.html — add Expenses nav link
- `src/main/resources/templates/fragments.html`
  - Add `<a href="/expenses">Expenses</a>` to the nav alongside the existing Categories link

---

## Tests to Create

### Repository Test
- `src/test/java/com/example/expensetracker/repository/ExpenseRepositoryTest.java`
  - `@DataJpaTest`, `@ActiveProfiles("test")` (H2 auto-configured)
  - Test: `save_findById_update_delete` — basic CRUD lifecycle
  - Test: `existsByCategoryId_returnsTrue_whenExpenseExists`
  - Test (N+1 guard): Use `@DataJpaTest` + `DataSource` + a SQL counter approach (or assert via `JdbcTemplate` query count) — alternatively, verify that a query with JOIN is executed by checking `findAll(spec, pageable)` returns the category eagerly (no `LazyInitializationException` outside transaction)

### Service Test
- `src/test/java/com/example/expensetracker/service/ExpenseServiceTest.java`
  - `@ExtendWith(MockitoExtension.class)`, `@Mock` repository deps, `@InjectMocks ExpenseService`
  - Test: `createExpense_throwsResourceNotFoundException_whenCategoryNotFound`
  - Test: `getExpense_throwsResourceNotFoundException_whenNotFound`
  - Test: `deleteExpense_throwsResourceNotFoundException_whenNotFound`

### Controller Test
- `src/test/java/com/example/expensetracker/web/ExpenseControllerTest.java`
  - `@WebMvcTest(controllers = {ExpenseController.class, GlobalExceptionHandler.class})`
  - `@MockBean ExpenseService`, `@MockBean CategoryService`
  - Test: `GET /expenses` → 200, view `expenses/list`, model has `expenses`
  - Test: `GET /expenses/new` → 200, view `expenses/form`, model has `categories`
  - Test: `POST /expenses` (valid) → 3xx redirect to `/expenses`
  - Test: `POST /expenses` (invalid — blank description) → 200, view `expenses/form`, `model().hasErrors()`
  - Test: `GET /expenses/{id}/edit` → 200, view `expenses/form`, model has `expenseForm`
  - Test: `POST /expenses/{id}` (valid) → 3xx redirect to `/expenses`
  - Test: `POST /expenses/{id}/delete` → 3xx redirect to `/expenses`
  - Test: `GET /expenses/{nonexistent}/edit` → handled by `GlobalExceptionHandler` → redirect to `/expenses`

---

## Key Design Decisions

| Decision | Choice | Reason |
|---|---|---|
| N+1 avoidance | `@EntityGraph` on overridden `findAll(Spec, Pageable)` | Safe with ManyToOne + works with dynamic Specification filtering |
| Filtering | `JpaSpecificationExecutor` + `ExpenseSpecification` | Dynamic — avoids combinatorial explosion of query methods |
| Pagination | Spring Data `Pageable` + `Page<Expense>` | Standard, works with Specification executor |
| Audit timestamps | `@PrePersist`/`@PreUpdate` | Consistent, no extra dependency |
| CategoryService dependency | Inject `ExpenseRepository` directly | Avoids circular deps; services can depend on any repo |

---

## Verification

```bash
# Run all tests
mvn clean test

# Run only expense-related tests
mvn clean test -Dtest=ExpenseRepositoryTest
mvn clean test -Dtest=ExpenseServiceTest
mvn clean test -Dtest=ExpenseControllerTest

# Start DB and run app
docker-compose up -d
mvn spring-boot:run
# Visit http://localhost:8080/expenses
```