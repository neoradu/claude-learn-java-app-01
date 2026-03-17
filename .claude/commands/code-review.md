---
description: Reviews uncommitted code changes made on the current branch.
allowed-tools: Bash(git diff), Bash(git diff --staged)
---

Your job is to coordinate reviewer subagent:

- **code-quality-reviewer**

Goal:
1. Gather the current branch diff including BOTH staged and unstaged changes.
2. Run reviewer subagent on the diff.
3. Produce a proposed edit plan (ordered checklist) to address the feedback.
4. Ask the user for explicit approval BEFORE making any code changes.

Process:
- First, collect the diff:
    - Use `git diff` for unstaged
    - Use `git diff --staged` for staged
    - If both are empty, say so and stop (DO NOT PROCEED).

- Then invoke subagent.
    - Provide agent:
        - the combined diff output
        - brief repo context if needed (tech stack, lint/test commands if available)
    - Tell it to be evidence-based: file paths, line/snippet references, no guessing.
    - Tell it NOT to review any code outside the diff.

- Merge results into:
    1. Summary (max 8 bullets total)
    2. Accessibility findings (Blocker/Major/Minor/Nit)
    3. Code quality findings (Blocker/Major/Minor/Nit)
    4. Combined action plan (ordered checklist)
    5. Questions/uncertainties (anything that needs human intent)

Rules:
- Do NOT edit any files yet.
- Do NOT run formatting-only changes unless they fix a cited issue.

Finish by asking:
"Do you want me to implement the action plan now?"

Wait for user confirmation before making any changes.