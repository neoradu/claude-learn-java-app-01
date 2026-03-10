# Spec file for Expense CRUD

branch: claude/feature/expense-crud

## Summary

Add an `Expense` entity to the application with full CRUD (Create, Read, Update, Delete) functionality. Each expense must be associated with a `Category` (already implemented). The feature includes the service layer, repository, web controllers with Thymeleaf templates, and integration tests. Special attention must be paid to database query performance to avoid the N+1 problem when loading expenses with their associated categories.

## Functional Requirements

- An `Expense` entity must be persisted in the database with the following fields:
  - `id` (auto-generated)
  - `description` (required, non-blank)
  - `amount` (required, positive decimal)
  - `date` (required, date of the expense)
  - `category` (required, many-to-one association with `Category`)
  - `createdAt` / `updatedAt` (audit timestamps, auto-managed)
- Users can list all expenses, showing description, amount, date, and category name
- Users can create a new expense via a form with a category dropdown populated from existing categories
- Users can edit an existing expense
- Users can delete an existing expense
- Category dropdown must reflect all available categories at the time of form rendering
- Expenses list should be sortable/displayable by date descending by default

## Possible Edge Cases

- Attempting to create or update an expense with a non-existent category ID
- Deleting a category that still has expenses associated with it (should be handled gracefully — prevent deletion or show a clear error message)
- Submitting the expense form with missing or invalid fields (validation errors shown inline)
- Amount field must reject negative or zero values
- Date field must reject future dates or handle them per business decision
- Empty expenses list should show a friendly empty state in the UI

## Acceptance Criteria

- Expense list page is accessible at `/expenses` and renders all expenses with category name, description, amount, and date
- Create expense page is accessible at `/expenses/new` and submits to `POST /expenses`
- Edit expense page is accessible at `/expenses/{id}/edit` and submits to `POST /expenses/{id}`
- Delete is triggered via `POST /expenses/{id}/delete`
- All form fields are validated; validation errors are displayed next to the relevant fields
- The expenses list query uses a JOIN FETCH or equivalent to load categories in a single query (no N+1)
- Deleting a category that has associated expenses must be blocked with a clear user-facing error
- Page titles and navigation breadcrumbs are consistent with the rest of the app

## Open questions

- Should expenses support pagination or infinite scroll for large datasets, or is a simple list sufficient for now? Answer: pagination
- Should the date field allow future dates (e.g. scheduled expenses)? Answer: no
- Is there a requirement to filter or search expenses by category, date range, or amount? Answer: yes add filtering by all those params
- Should currency/locale formatting be applied to the amount field in the list view? Answer: No

## Testing Guidelines

Create tests in src/test/java folder for the new feature, and create meaningful tests for the following cases.

- Repository integration test: verify that loading expenses fetches categories without triggering N+1 queries (assert single query using query counter or SQL logging assertions)
- Repository integration test: verify CRUD operations (save, find, update, delete) on the `Expense` entity
- Service unit/integration test: verify that referencing a non-existent category throws `ResourceNotFoundException`
- Service integration test: verify that deleting a category in use by expenses throws `CategoryInUseException` (or equivalent)
- Controller integration test: `GET /expenses` returns 200 and renders the list template with expense data
- Controller integration test: `GET /expenses/new` returns 200 and renders the form with a populated category dropdown
- Controller integration test: `POST /expenses` with valid data creates an expense and redirects to `/expenses`
- Controller integration test: `POST /expenses` with invalid data re-renders the form with validation errors
- Controller integration test: `GET /expenses/{id}/edit` returns 200 with the expense pre-filled
- Controller integration test: `POST /expenses/{id}` with valid data updates the expense and redirects
- Controller integration test: `POST /expenses/{id}/delete` deletes the expense and redirects to `/expenses`
- Controller integration test: `POST /expenses/{id}/delete` with a non-existent ID returns a 404 or appropriate error page
