import { readFile, writeFile } from 'node:fs/promises';
import { resolve } from 'node:path';
import { pathToFileURL } from 'node:url';
import { setTimeout as sleep } from 'node:timers/promises';

export const START = '<!-- UAB_ISSUE_LEADERBOARD_START -->';
export const END = '<!-- UAB_ISSUE_LEADERBOARD_END -->';
const EXCLUDED_LABELS = new Set([
  'duplicate', 'invalid', 'wontfix', 'rejected', 'not planned', 'not_planned', 'not-planned',
]);

// Keep completion eligibility separate from classification so an optional accepted
// category can be added later without awarding unresolved issues leaderboard points.
export function classifyIssue(issue) {
  if ('pull_request' in issue || issue.state !== 'closed' || issue.state_reason !== 'completed') {
    return null;
  }
  const labels = new Set(issue.labels.map(label => label.name.toLowerCase()));
  if ([...labels].some(label => EXCLUDED_LABELS.has(label))) return null;
  if (labels.has('bug')) return 'bugs';
  if (labels.has('enhancement')) return 'enhancements';
  return null;
}

export function isMarkedDuplicate(events) {
  // REST issue events arrive oldest first. Sort explicitly for reproducible replay.
  let duplicate = false;
  const ordered = [...events].sort((a, b) =>
    compare(a.created_at ?? '', b.created_at ?? '') || (a.id ?? 0) - (b.id ?? 0));
  for (const event of ordered) {
    if (event.event === 'marked_as_duplicate') duplicate = true;
    if (event.event === 'unmarked_as_duplicate') duplicate = false;
  }
  return duplicate;
}

function compare(a, b) {
  return a < b ? -1 : a > b ? 1 : 0;
}

export function rankIssues(issues, eventsByNumber = new Map()) {
  const submitters = new Map();
  const seen = new Set();
  for (const issue of issues) {
    if (seen.has(issue.number)) continue;
    seen.add(issue.number);
    const category = classifyIssue(issue);
    // GitHub's ghost account is a shared placeholder, not the original creator.
    const login = issue.user?.login;
    if (!category || !login || login.toLowerCase() === 'ghost'
        || isMarkedDuplicate(eventsByNumber.get(issue.number) ?? [])) continue;
    const key = login.toLowerCase();
    const row = submitters.get(key) ?? { login, bugs: 0, enhancements: 0, total: 0 };
    // Account-type and username-based automation detection are intentionally omitted.
    if (compare(login, row.login) < 0) row.login = login;
    row[category]++;
    row.total++;
    submitters.set(key, row);
  }
  return [...submitters.values()].sort((a, b) =>
    b.total - a.total || b.bugs - a.bugs || b.enhancements - a.enhancements
    || compare(a.login.toLowerCase(), b.login.toLowerCase()) || compare(a.login, b.login));
}

export function renderLeaderboard(rows) {
  const lines = [
    '## Top Issue Submitters',
    '',
    'Recognizing community members whose bug reports and enhancement suggestions have been completed. Rankings are based on issues submitted, not code contributions.',
    '',
  ];
  if (!rows.length) {
    lines.push('No qualifying completed bug reports or enhancement suggestions yet.');
  } else {
    lines.push('| Rank | Issue Submitter | Bugs Reported | Enhancements Suggested | Total |',
      '|---|---|---|---|---|');
    rows.slice(0, 10).forEach((row, index) => {
      const label = row.login.replace(/[\\[\]|]/g, '\\$&');
      lines.push(`| ${index + 1} | [@${label}](https://github.com/${encodeURIComponent(row.login)}) | ${row.bugs} | ${row.enhancements} | ${row.total} |`);
    });
  }
  return lines.join('\n');
}

export function replaceSection(readme, section) {
  if (readme.split(START).length !== 2 || readme.split(END).length !== 2) {
    throw new Error('README must contain exactly one pair of leaderboard markers.');
  }
  const start = readme.indexOf(START) + START.length;
  const end = readme.indexOf(END);
  if (start > end) throw new Error('README leaderboard markers are reversed.');
  const newline = readme.includes('\r\n') ? '\r\n' : '\n';
  return readme.slice(0, start) + newline + section.replace(/\r?\n/g, newline)
    + newline + readme.slice(end);
}

export async function updateReadme(path, rows) {
  const before = await readFile(path, 'utf8');
  const after = replaceSection(before, renderLeaderboard(rows));
  if (before === after) return false;
  await writeFile(path, after, 'utf8');
  return true;
}

export class GitHub {
  constructor({ token = process.env.GITHUB_TOKEN, fetchImpl = fetch, wait = sleep,
    now = Date.now } = {}) {
    this.fetch = fetchImpl;
    this.wait = wait;
    this.now = now;
    this.headers = {
      Accept: 'application/vnd.github+json',
      'X-GitHub-Api-Version': '2022-11-28',
      'User-Agent': 'UAB-Issue-Leaderboard',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };
  }

  async get(url) {
    if (new URL(url).origin !== 'https://api.github.com') {
      throw new Error('Refusing to send GitHub credentials to a different host.');
    }
    for (let attempt = 0; attempt < 5; attempt++) {
      let response;
      try {
        response = await this.fetch(url, {
          headers: this.headers, signal: AbortSignal.timeout(30_000), redirect: 'error',
        });
      } catch (error) {
        if (attempt === 4) throw error;
        await this.wait(1000 * 2 ** attempt);
        continue;
      }
      if (response.ok) return response;
      const body = await response.text();
      const limited = response.status === 429 || (response.status === 403
        && (response.headers.get('x-ratelimit-remaining') === '0'
          || response.headers.has('retry-after') || /secondary rate limit/i.test(body)));
      if (attempt === 4 || (!limited && ![500, 502, 503, 504].includes(response.status))) {
        throw new Error(`GitHub API returned HTTP ${response.status} for ${url}.`);
      }
      let delay = limited ? 60_000 : 1000 * 2 ** attempt;
      const retryAfter = response.headers.get('retry-after');
      if (retryAfter !== null) {
        delay = /^\d+(\.\d+)?$/.test(retryAfter)
          ? Number(retryAfter) * 1000 : Date.parse(retryAfter) - this.now();
      } else if (response.headers.get('x-ratelimit-remaining') === '0') {
        delay = Number(response.headers.get('x-ratelimit-reset')) * 1000 - this.now() + 1000;
      }
      if (!Number.isFinite(delay) || delay > 120_000) {
        throw new Error('GitHub rate-limit wait exceeds two minutes; retry the workflow later.');
      }
      await this.wait(Math.max(1000, delay));
    }
  }

  async pages(path) {
    let url = new URL(path, 'https://api.github.com').href;
    const seen = new Set();
    const records = [];
    while (url) {
      if (seen.has(url)) throw new Error('GitHub returned a repeated pagination URL.');
      seen.add(url);
      const response = await this.get(url);
      const page = await response.json();
      if (!Array.isArray(page)) throw new Error('Expected a GitHub API array.');
      records.push(...page);
      url = response.headers.get('link')?.match(/<([^>]+)>;\s*rel="next"/)?.[1];
    }
    return records;
  }
}

export async function fetchLeaderboard(repository, github = new GitHub()) {
  if (!/^[\w.-]+\/[\w.-]+$/.test(repository)) {
    throw new Error('Repository must use the owner/name format.');
  }
  const base = `/repos/${repository}/issues`;
  // No since/date cutoff or search API: include the complete repository history.
  const issues = await github.pages(`${base}?state=all&sort=created&direction=asc&per_page=100`);
  const events = new Map();
  for (const issue of issues) {
    if (!classifyIssue(issue) || events.has(issue.number)) continue;
    events.set(issue.number, await github.pages(`${base}/${issue.number}/events?per_page=100`));
  }
  return {
    issueCount: issues.filter(issue => !('pull_request' in issue)).length,
    rows: rankIssues(issues, events),
  };
}

async function main() {
  const repository = process.argv[2] ?? process.env.GITHUB_REPOSITORY;
  if (!repository) throw new Error('Usage: node tools/issue-leaderboard/update.mjs owner/repo [README.md]');
  const { issueCount, rows } = await fetchLeaderboard(repository);
  const changed = await updateReadme(process.argv[3] ?? 'README.md', rows);
  const total = rows.reduce((sum, row) => sum + row.total, 0);
  console.log(`Scanned ${issueCount} issues; counted ${total} completed submissions from ${rows.length} submitters. README ${changed ? 'updated' : 'unchanged'}.`);
}

if (process.argv[1] && import.meta.url === pathToFileURL(resolve(process.argv[1])).href) {
  main().catch(error => {
    console.error(error.message);
    process.exitCode = 1;
  });
}
