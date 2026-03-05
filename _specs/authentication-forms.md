# Spec file for Authentication Forms

branch: claude/feature/authentication-forms

## Summary

Add `/login` and `/signup` pages, each with an authentication form containing email and password fields, a toggle to show/hide the password, and a submit button. For now, form submissions only log the entered details to the browser console. Users can easily navigate between the two forms via a link on each page.

## Functional Requirements

- A `/login` page renders a login form with:
  - An email input field
  - A password input field with a show/hide toggle icon
  - A "Login" submit button
  - A link to navigate to the `/signup` page
- A `/signup` page renders a signup form with:
  - An email input field
  - A password input field with a show/hide toggle icon
  - A "Sign Up" submit button
  - A link to navigate to the `/login` page
- Clicking the show/hide icon toggles the password field between masked and plain text
- On form submission, the email and password values are logged to the browser console (no backend processing)
- Form submission does not navigate away from the current page (prevent default or use a no-op action)

## Possible Edge Cases

- User submits the form with empty fields — still logs (possibly empty) values to the console
- Password visibility state resets on page reload
- Multiple rapid clicks on the show/hide icon should reliably toggle the state

## Acceptance Criteria

- Navigating to `/login` shows a login form with email, password (masked), show/hide icon, and a "Login" button
- Navigating to `/signup` shows a signup form with email, password (masked), show/hide icon, and a "Sign Up" button
- Clicking the show/hide icon on either form toggles the password field between `type="password"` and `type="text"`
- Submitting either form logs `{ email: "...", password: "..." }` (or equivalent) to the browser console
- Each page contains a visible link that navigates to the other authentication page
- No backend endpoint or database interaction is required at this stage

## Open Questions

- Should the show/hide icon use a specific icon library (e.g., Bootstrap Icons, Font Awesome) or an inline SVG/text toggle? Answer: SVG/text
- Should there be any client-side validation (e.g., email format, minimum password length) before logging, or is a bare log sufficient? Answer: you could add client side validation.
- Should the two forms share a single Thymeleaf template with a mode parameter, or be separate templates? Answer: use separate templates

## Testing Guidelines

Create tests in src/test/java folder for the new feature, and create meaningful tests for the following cases.

- `GET /login` returns HTTP 200 and renders the login form template
- `GET /signup` returns HTTP 200 and renders the signup form template
- The login page contains a link pointing to `/signup`
- The signup page contains a link pointing to `/login`
- Both pages include an email input, a password input, and a submit button in the rendered HTML
