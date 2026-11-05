# Issue submitter leaderboard

The README's Top Issue Submitters table awards one point per completed issue to
`issue.user.login`, its original creator. It never uses assignees, closure actors,
commits, pull request authors, or fix authorship. Account automation detection and
bot filtering are intentionally omitted.

An issue must currently be closed with `state_reason: completed` and have a `bug`
or `enhancement` label (case insensitive). Bug classification takes priority when
both labels exist. Pull requests and issues labeled `duplicate`, `invalid`,
`wontfix`, `rejected`, `not planned`, `not_planned`, or `not-planned` are excluded.
GitHub's duplicate-marking events are also checked, including subsequent undo
events. Rejection must be recorded with an exclusion label or a non-completed
closure reason; the generator does not infer it from free-text discussions.
Missing creators and GitHub's shared deleted-account placeholder `ghost` are
skipped, rather than crediting someone else.

Rankings sort by total, bugs, enhancements (descending), then username
(alphabetically, case insensitive). Only the first ten accounts are displayed.
Every refresh scans all historical issues using paginated REST requests and
recalculates current eligibility, including reopened and relabeled issues.
Accepted but unresolved issues receive no points. Completion eligibility is kept
separate from category classification for future additions.

Run with Node.js 24; no packages or database are needed:

```sh
node --test tools/issue-leaderboard/update.test.mjs
node tools/issue-leaderboard/update.mjs RagexPrince683/UniversalAdaptiveBackporter
```

Set `GITHUB_TOKEN` for authenticated API limits (required for private repositories).
In Actions, `GITHUB_REPOSITORY` supplies the repository automatically. An optional
second argument selects another README path. The markers must already exist
exactly once; only their interior is replaced. Unchanged output is not written.
Transient failures and rate limits are retried up to five attempts per request;
waits above two minutes fail without editing the README so a later refresh can
retry. API failures never publish a partial table.

`.github/workflows/issue-leaderboard.yml` refreshes on issue closure, reopening,
label changes, editing, deletion and transfer; label definition edits/deletions;
manual dispatch; and daily at 06:23 UTC. Duplicate-marking changes without a
matching issue webhook are picked up on the next refresh, at latest by the daily
fallback when Actions is running normally. There are no push triggers or generated
timestamps, and a commit is made only when the table changes.

The workflow must be installed on the default branch and Actions enabled.
Its normal job uses the built-in token with `issues: read` and `contents: write`.
If a direct push fails, a separate job with the additional `pull-requests: write`
permission regenerates against the latest default branch and creates or updates
one PR on the reserved `codex/issue-leaderboard` branch. Enable **Settings → Actions
→ General → Allow GitHub Actions to create and approve pull requests** for this
fallback. Repository/organization policy must permit the requested token writes;
the workflow cannot bypass branch protection or automatically merge its PR.
Required checks on an automatically created PR may require maintainer approval
in GitHub before running. Neither publication path needs a personal access token.
