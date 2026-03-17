# Multi-User Support — Implementation Plan

## Context

The expense tracker is currently single-tenant: all expenses and categories are globally visible with no authentication.
The `AuthController` is a stub (GET-only, no POST), login/signup templates use `console.log` instead of real form submission,
and no `User` entity or Spring Security dependency exists. This plan wires up full Spring Security authentication, 
introduces per-user expense scoping, and preserves global categories.

---

## Phase 1: Foundation — domain and persistence

### pom.xml
Add three dependencies:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

### New: `domain/Role.java`
```java
public enum Role { USER, ADMIN }
```

### New: `domain/User.java`
Implements `UserDetails` directly (avoids a wrapper class; `@AuthenticationPrincipal User` works out of the box).
Fields: `id` (Long PK), `email` (unique, not null), `password` (not null), `role` (enum), `createdAt`, `updatedAt`.
- `getUsername()` → returns `email`
- `getAuthorities()` → `List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))`
- All boolean flags return `true`
- `@PrePersist`/`@PreUpdate` for timestamps (same pattern as `Expense`)

### New: `repository/UserRepository.java`
```java
Optional<User> findByEmail(String email);
boolean existsByEmail(String email);
```

### Modify: `domain/Expense.java`
Add: `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") User owner;`
Column is nullable at DB level initially — `DataInitializer` fills orphans before the app is used.

### Modify: `service/ExpenseSpecification.java`
Add:
```java
public static Specification<Expense> hasOwner(Long userId) {
    return (root, query, cb) -> cb.equal(root.get("owner").get("id"), userId);
}
```

### Modify: `repository/ExpenseRepository.java`
Add orphan-assignment query for `DataInitializer`:
```java
@Modifying
@Query("UPDATE Expense e SET e.owner = :owner WHERE e.owner IS NULL")
int assignOrphanedExpensesToOwner(@Param("owner") User owner);
```

---

## Phase 2: User service layer

### New: `service/DuplicateEmailException.java`
Follows `DuplicateCategoryException` pattern (extends `RuntimeException`).

### New: `service/UserDetailsServiceImpl.java`
Implements `UserDetailsService`. `loadUserByUsername(email)` calls `userRepository.findByEmail`, throws `UsernameNotFoundException` if absent.

### New: `web/form/SignupForm.java`
Fields: `email` (`@NotBlank @Email`), `password` (`@NotBlank @Size(min=8)`), `confirmPassword` (`@NotBlank`).
Password-match check done imperatively in the controller (consistent with how duplicate-category errors are handled).

### New: `service/UserService.java`
`register(SignupForm)`: checks `existsByEmail` → throws `DuplicateEmailException`; encodes password with `BCryptPasswordEncoder`; sets role `USER`; saves and returns `User`.

---

## Phase 3: Security configuration

### New: `config/SecurityConfig.java`
- Permits `/login`, `/signup`, `/css/**`, `/js/**` without auth
- All other routes require authentication
- `formLogin().loginPage("/login").usernameParameter("email").defaultSuccessUrl("/expenses")`
- `logout().logoutSuccessUrl("/login")`
- Defines `PasswordEncoder` bean (`BCryptPasswordEncoder`)
- Defines `AuthenticationManager` wired to `UserDetailsServiceImpl`

---

## Phase 4: Expense scoping

### Modify: `service/ExpenseService.java`
All public methods gain a `User currentUser` parameter:
- `getExpenses(filter, pageable, currentUser)` — composes `hasOwner(userId)` into spec unless user has `ADMIN` role
- `getExpense(id, currentUser)` — after fetch, if owner doesn't match and not ADMIN → throw `ResourceNotFoundException` (404, avoids ID enumeration)
- `createExpense(form, currentUser)` — sets `expense.setOwner(currentUser)`
- `updateExpense(id, form, currentUser)` — ownership check before mutating
- `deleteExpense(id, currentUser)` — ownership check before deleting

### Modify: `web/ExpenseController.java`
Add `@AuthenticationPrincipal User currentUser` to every handler method; thread through to service calls.

### Modify: `web/GlobalExceptionHandler.java`
Add `@ExceptionHandler(AccessDeniedException.class)` as a safety net (redirects to `/expenses` with flash error).

### Modify: `config/DataInitializer.java`
1. Seed `user@example.com` (role USER) and `admin@example.com` (role ADMIN) if no users exist
2. Resolve default user (`user@example.com`)
3. Call `expenseRepository.assignOrphanedExpensesToOwner(defaultUser)` to migrate pre-existing expenses

---

## Phase 5: Auth controller and templates

### Modify: `web/AuthController.java`
- Keep `GET /login` (renders `auth/login`; Spring Security handles the POST itself)
- Keep `GET /signup` — add `SignupForm` model attribute
- Add `POST /signup`: validate form, check password match, call `UserService.register`, handle `DuplicateEmailException` as field error on `email`, on success redirect to `/login?registered`

### Modify: `templates/auth/login.html`
- Replace JS form with `<form th:action="@{/login}" method="post">`
- Input names: `email` and `password` (matches `usernameParameter("email")`)
- `th:if="${param.error}"` — show auth failure message
- `th:if="${param.registered}"` — show registration success message
- CSRF hidden input
- Remove JavaScript submit handler

### Modify: `templates/auth/signup.html`
- Replace JS form with `<form th:action="@{/signup}" th:object="${signupForm}" method="post">`
- `th:field` / `th:errors` for all fields
- CSRF hidden input
- Remove JavaScript submit handler

### Modify: `templates/fragments.html`
Add to nav: logged-in user email (`#authentication.name`) and a logout form (`POST /logout` with CSRF).

---

## Phase 6: Tests

### Fix existing tests (Spring Security now active in @WebMvcTest)
- `CategoryControllerTest` — add `@WithMockUser` at class level
- `ExpenseControllerTest` — add `@WithMockUser` at class level; update service mock call signatures to include `User` parameter
- `ExpenseRepositoryTest` — add `savedUser()` helper; update `savedExpense()` to call `expense.setOwner(savedUser())`
- `ExpenseServiceTest` — update all calls to pass a `User` object; add ownership check tests
- `AuthControllerTest` — rewrite entirely (see below)

### New test classes
| Class | Type | Key scenarios |
|---|---|---|
| `UserRepositoryTest` | `@DataJpaTest` | save/findByEmail round-trip; existsByEmail; duplicate email → `DataIntegrityViolationException` |
| `UserServiceTest` | Mockito unit | register stores BCrypt hash (not plain text); duplicate email → `DuplicateEmailException` |
| `AuthControllerTest` (rewritten) | `@WebMvcTest` | GET /login renders form; GET /signup renders form with `signupForm`; POST /signup valid → redirect `/login?registered`; POST /signup duplicate email → form with error; POST /signup password mismatch → form with error |
| `ExpenseControllerSecurityTest` | `@WebMvcTest` | Unauthenticated GET /expenses → redirect to `/login`; unauthenticated POST /expenses → redirect to `/login`; authenticated GET /expenses returns 200 |

---

## Verification

1. `mvn test` — all tests pass
2. `docker compose up -d && ./mvnw spring-boot:run`
3. Navigate to `http://localhost:8080` — redirects to `/login`
4. Register a new user at `/signup` — form validates, success → `/login?registered`
5. Log in as `user@example.com` — lands on `/expenses`; sees only own expenses
6. Log in as `admin@example.com` — sees all expenses
7. Attempt to navigate to another user's expense by ID — gets 404/redirect
8. Logout — redirects to `/login`