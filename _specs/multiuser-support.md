# Spec file for Multi-User Support

branch: claude/feature/multiuser-support

## Summary

Add multi-user support to the expense tracker application. Each user will have their own account with an email, hashed password, and role. Expenses will be scoped to individual users, while categories remain shared/global across all users.

## Functional Requirements

- Add a `User` entity with `email`, `password` (hashed), and `role` fields
- Users must be able to register with email and password
- Users must be able to log in with email and password
- Passwords must be stored hashed (never in plain text)
- Each `Expense` must belong to a `User` via a many-to-one relationship
- When a user views, creates, edits, or deletes expenses, only their own expenses are accessible
- Categories remain global (shared across all users); any user can use any category
- Role field supports at least two roles: `USER` and `ADMIN`
- Users with `ADMIN` role may have elevated privileges (exact admin features are out of scope for this spec, but the role must be stored and accessible)
- Unauthenticated users must be redirected to the login page for any protected route
- Spring Security must be properly integrated with the existing `AuthController`, login, and signup templates

## Possible Edge Cases

- Duplicate email registration attempts should be rejected with a clear error message
- A user attempting to access another user's expense (by URL manipulation) should receive a 403 or 404 response
- Existing expenses in the database (pre-multiuser) have no owner — define a migration or initialization strategy for orphaned expenses
- Empty or invalid email/password on registration or login should produce validation feedback
- Password confirmation mismatch on signup should be caught before persisting
- Session expiry should redirect the user back to login cleanly

## Acceptance Criteria

- A `User` entity exists with `id`, `email`, `password` (BCrypt hashed), `role`, and standard audit timestamps
- Registration creates a new user with a hashed password and `USER` role by default
- Login authenticates via Spring Security using the stored hashed password
- After login, all expense operations are scoped to the authenticated user
- Attempting to view/edit/delete another user's expense returns an error (not silent data leakage)
- Categories are still visible and usable by all users
- All existing expense CRUD flows continue to work correctly after the change
- Unit and integration tests cover the new user-scoped expense behavior

## Open questions

- Should admins be able to view all users' expenses, or is that out of scope? Answer: No Yes Admins should be able to view 
  other user 
  expenses.
- Should there be an admin UI for user management, or just the role field for future use? Answer: No For now just a role field.
  Add a default user and admin user to the db for development purpose.
- What happens to existing (pre-migration) expenses — assign to a default user, or delete them? assign default user.
- Should "remember me" / persistent sessions be supported? Answer: No
- Is email verification required on registration, or is immediate activation acceptable? Answer: For now do not verify the email

## Testing Guidelines

Create tests in src/test/java folder for the new feature, and create meaningful tests for the following cases.

- Registering a new user stores a hashed password (not plain text)
- Duplicate email registration returns an appropriate error
- Login with correct credentials succeeds and creates a session
- Login with incorrect credentials fails with a clear error
- An authenticated user can only see their own expenses (not other users')
- An unauthenticated request to a protected route redirects to login
- Creating an expense associates it with the currently logged-in user
- Accessing another user's expense by ID returns 403 or 404
- Categories are accessible to all authenticated users regardless of who created them

## Skills to Apply

- `/clean-code` — Keep entities, services, and controllers focused and readable
- `/design-patterns` — Apply appropriate patterns (e.g., Strategy for role checks, Builder for user creation)
- `/solid-principles` — Ensure single responsibility across User, UserService, and security config
- `/spring-boot-patterns` — Follow Spring Boot conventions for security config, service layer, and repository
- `/test-quality` — Write expressive JUnit 5 tests with AssertJ and proper Spring test slices
