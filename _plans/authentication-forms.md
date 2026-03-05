# Plan: Authentication Forms (/login and /signup)

## Context

The spec (`_specs/authentication-forms.md`) calls for two static authentication pages. Form submissions only log to the browser console — no backend processing, no service layer, no database work. The password field needs a show/hide toggle (inline SVG). Each page links to the other. Client-side validation is desired (HTML5 `type="email"` + `required` attributes are sufficient).

## Step 1 — Create `AuthController.java`

**File:** `src/main/java/com/example/expensetracker/web/AuthController.java`

A minimal `@Controller` with no dependencies:

```java
@GetMapping("/login")  → return "auth/login"
@GetMapping("/signup") → return "auth/signup"
```

No constructor, no service injection, no POST handlers.

**Pattern reference:** `CategoryController.java` lines 28-32 (simple GET returning a view name).

## Step 2 — Create `auth/login.html` template

**File:** `src/main/resources/templates/auth/login.html`

Structure (follows `categories/form.html` pattern):
- Standard `<!DOCTYPE html>` + Thymeleaf namespace
- `<link rel="stylesheet" th:href="@{/css/app.css}">`
- DO NOT Include any headers keep the pages clean.
- `<section class="card auth-card">` centered wrapper
- `<h1>Login</h1>`
- Plain HTML `<form id="loginForm" class="form-grid">` (no `th:object`, no `action` — JS handles submit)
  - `<label>Email <input type="email" id="email" required></label>`
  - `<label>Password` with a wrapper `<div class="password-field">` containing:
    - `<input type="password" id="password" required>`
    - `<button type="button" class="toggle-password">` with inline SVG eye icon
  - `</label>`
  - `<div class="actions"><button type="submit">Login</button></div>`
- `<p>Don't have an account? <a th:href="@{/signup}">Sign up</a></p>`
- Inline `<script>`:
  - Toggle: flip `input.type` between `password`/`text`, swap SVG icon
  - Submit: `e.preventDefault(); console.log({ email: ..., password: ... })`

## Step 3 — Create `auth/signup.html` template

**File:** `src/main/resources/templates/auth/signup.html`

Same structure as login.html with these differences:
- `<h1>Sign Up</h1>`
- Form id `signupForm`
- Submit button text: `Sign Up`
- Footer link: `Already have an account? <a th:href="@{/login}">Log in</a>`

## Step 4 — Append auth styles to `app.css`

**File:** `src/main/resources/static/css/app.css` (append only)

Add minimal styles:
- `.auth-card` — `max-width: 420px; margin: 2rem auto;` to center the form card
- `.password-field` — `position: relative;` wrapper so the toggle icon sits inside the input
- `.toggle-password` — absolutely positioned icon button inside the password input, no background, no border

Reuse existing CSS variables (`--border`, `--primary`, `--surface`, etc.) and class conventions.

## Step 5 — Create `AuthControllerTest.java`

**File:** `src/test/java/com/example/expensetracker/web/AuthControllerTest.java`

Pattern reference: `CategoryControllerTest.java`.

```java
@WebMvcTest(controllers = AuthController.class)
@ActiveProfiles("test")
```

No `@MockBean` needed (controller has no dependencies).

Tests:
- `login_shouldRenderLoginPage` — GET /login → 200, view `"auth/login"`
- `signup_shouldRenderSignupPage` — GET /signup → 200, view `"auth/signup"`
- `loginPage_shouldContainLinkToSignup` — response body `.content().string(containsString("/signup"))`
- `signupPage_shouldContainLinkToLogin` — response body `.content().string(containsString("/login"))`
- `loginPage_shouldContainEmailAndPasswordInputs` — response body contains `type="email"` and `type="password"`
- `signupPage_shouldContainEmailAndPasswordInputs` — same for signup

## Files changed (summary)

| Action | File |
|--------|------|
| Create | `src/main/java/com/example/expensetracker/web/AuthController.java` |
| Create | `src/main/resources/templates/auth/login.html` |
| Create | `src/main/resources/templates/auth/signup.html` |
| Edit   | `src/main/resources/static/css/app.css` (append auth styles) |
| Create | `src/test/java/com/example/expensetracker/web/AuthControllerTest.java` |

## Verification

1. `mvn test -Dtest=AuthControllerTest` — all 6 tests pass
2. `mvn spring-boot:run` — visit `/login` and `/signup`, verify:
   - Forms render with email + password fields
   - Show/hide toggle works
   - Submit logs `{ email, password }` to browser console
   - Cross-links navigate between pages
