import test from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, readFile, rm, stat, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import {
  START, END, classifyIssue, isMarkedDuplicate, rankIssues, renderLeaderboard,
  replaceSection, updateReadme, GitHub, fetchLeaderboard,
} from './update.mjs';

function issue(number, login = 'reporter', labels = ['bug'], extra = {}) {
  return { number, user: { login, type: 'User' }, labels: labels.map(name => ({ name })),
    state: 'closed', state_reason: 'completed', ...extra };
}

function response(data, { status = 200, headers = {} } = {}) {
  return new Response(JSON.stringify(data), { status, headers });
}

test('completed issues classify once, with bug priority and case-insensitive labels', () => {
  assert.equal(classifyIssue(issue(1)), 'bugs');
  assert.equal(classifyIssue(issue(2, 'a', ['enhancement'])), 'enhancements');
  assert.equal(classifyIssue(issue(3, 'a', ['ENHANCEMENT', 'Bug'])), 'bugs');
});

test('only completed, classified, actual issues qualify', () => {
  for (const invalid of [
    issue(1, 'a', ['bug'], { state: 'open', state_reason: 'reopened' }),
    issue(2, 'a', ['bug'], { state_reason: 'not_planned' }),
    issue(3, 'a', ['bug'], { state_reason: 'duplicate' }),
    issue(4, 'a', ['bug'], { state_reason: null }),
    issue(5, 'a', ['documentation']),
    issue(6, 'a', ['accepted'], { state: 'open' }),
    issue(7, 'a', ['bug'], { pull_request: { url: 'irrelevant' } }),
    ...['duplicate', 'invalid', 'wontfix', 'rejected', 'not planned', 'not_planned',
      'not-planned'].map((label, i) => issue(8 + i, 'a', ['bug', label.toUpperCase()])),
  ]) assert.equal(classifyIssue(invalid), null);
});

test('credit belongs exclusively to original creators, regardless of who fixed or closed an issue', () => {
  const report = issue(1, 'original-creator');
  for (const field of ['assignee', 'assignees', 'closed_by', 'commits', 'commit_author',
    'pull_request_author', 'author', 'performed_via_github_app']) {
    Object.defineProperty(report, field, {
      get() { throw new Error(`Attribution must not inspect ${field}`); },
    });
  }
  assert.deepEqual(rankIssues([report]), [
    { login: 'original-creator', bugs: 1, enhancements: 0, total: 1 },
  ]);
});

test('multiple submissions aggregate, repeated issues and both-label issues award only one point', () => {
  const reports = [issue(1), issue(2, 'reporter', ['enhancement']),
    issue(3, 'reporter', ['enhancement', 'bug'])];
  assert.deepEqual(rankIssues([...reports, reports[0]]), [
    { login: 'reporter', bugs: 2, enhancements: 1, total: 3 },
  ]);
});

test('reopening, relabeling or rejecting a completed issue removes its point', () => {
  const report = issue(1);
  assert.equal(rankIssues([report])[0].total, 1);
  assert.deepEqual(rankIssues([{ ...report, state: 'open' }]), []);
  assert.deepEqual(rankIssues([{ ...report, labels: [] }]), []);
  assert.deepEqual(rankIssues([{ ...report, state_reason: 'not_planned' }]), []);
  assert.equal(rankIssues([{ ...report, labels: [{ name: 'enhancement' }] }])[0].enhancements, 1);
});

test('duplicate marks and undo events are replayed chronologically', () => {
  const marked = { id: 1, created_at: '2025-01-01T00:00:00Z', event: 'marked_as_duplicate' };
  const unmarked = { id: 2, created_at: '2025-01-02T00:00:00Z', event: 'unmarked_as_duplicate' };
  assert.equal(isMarkedDuplicate([marked]), true);
  assert.equal(isMarkedDuplicate([unmarked, marked]), false);
  assert.deepEqual(rankIssues([issue(1)], new Map([[1, [marked]]])), []);
  assert.equal(rankIssues([issue(1)], new Map([[1, [unmarked, marked]]]))[0].total, 1);
});

test('rankings use totals, bug counts, then alphabetical usernames independent of API order', () => {
  const reports = [
    issue(1, 'zeta'), issue(2, 'zeta'), issue(3, 'zeta', ['enhancement']),
    issue(4, 'Alpha'), issue(5, 'Alpha', ['enhancement']),
    issue(6, 'Beta'), issue(7, 'Beta'),
    issue(8, 'charlie'), issue(9, 'charlie', ['enhancement']),
  ];
  const expected = ['zeta', 'Beta', 'Alpha', 'charlie'];
  assert.deepEqual(rankIssues(reports).map(row => row.login), expected);
  assert.deepEqual(rankIssues([...reports].reverse()), rankIssues(reports));
});

test('case-insensitive logins aggregate deterministically without detecting automated accounts', () => {
  const reports = [issue(1, 'REPORTER'), issue(2, 'reporter'),
    issue(3, 'automation[bot]', ['bug'], { user: { login: 'automation[bot]', type: 'Bot' } })];
  assert.deepEqual(rankIssues(reports), [
    { login: 'REPORTER', bugs: 2, enhancements: 0, total: 2 },
    { login: 'automation[bot]', bugs: 1, enhancements: 0, total: 1 },
  ]);
  assert.deepEqual(rankIssues(reports), rankIssues([...reports].reverse()));
});

test('missing and deleted creators are skipped rather than assigned to another account', () => {
  assert.deepEqual(rankIssues([
    issue(1, 'a', ['bug'], { user: null }),
    issue(2, 'a', ['bug'], { user: {} }), issue(3, 'ghost'), issue(4, 'Ghost'),
  ]), []);
});

test('rendering shows at most ten profile links or a clear empty message', () => {
  const rows = rankIssues(Array.from({ length: 12 }, (_, i) => issue(i + 1, `user-${i}`)));
  const table = renderLeaderboard(rows);
  assert.equal((table.match(/https:\/\/github.com\//g) ?? []).length, 10);
  assert.match(table, /\| 1 \| \[@user-0\]\(https:\/\/github.com\/user-0\)/);
  assert.match(renderLeaderboard([]), /No qualifying completed/);
  assert.doesNotMatch(renderLeaderboard([]), /ExampleUser|\| Rank/);
  assert.match(renderLeaderboard(rankIssues([issue(1, 'automation[bot]')])), /automation%5Bbot%5D/);
});

test('marker replacement preserves all outside bytes including BOM and line endings', () => {
  for (const newline of ['\n', '\r\n']) {
    const prefix = `\uFEFF# Existing README${newline}${newline}${START}`;
    const suffix = `${END}${newline}Unrelated trailing content  ${newline}`;
    const before = prefix + newline + 'old' + newline + suffix;
    const after = replaceSection(before, 'new\nsection');
    assert.equal(after, prefix + newline + `new${newline}section` + newline + suffix);
    assert.equal(replaceSection(after, 'new\nsection'), after);
  }
});

test('missing, repeated and reversed markers fail instead of overwriting README', () => {
  for (const before of ['', START, END, END + START, START + START + END,
    START + END + END]) assert.throws(() => replaceSection(before, 'new'), /markers/);
});

test('unchanged output is not written on a second run', async () => {
  const directory = await mkdtemp(join(tmpdir(), 'uab-leaderboard-'));
  try {
    const path = join(directory, 'README.md');
    await writeFile(path, `before\n${START}\nold\n${END}\nafter\n`);
    const rows = rankIssues([issue(1)]);
    assert.equal(await updateReadme(path, rows), true);
    const first = await stat(path, { bigint: true });
    const contents = await readFile(path, 'utf8');
    assert.equal(await updateReadme(path, rows), false);
    assert.equal((await stat(path, { bigint: true })).mtimeNs, first.mtimeNs);
    assert.equal(await readFile(path, 'utf8'), contents);
  } finally {
    await rm(directory, { recursive: true });
  }
});

test('paginated history includes more than 100 issues and follows next rather than last', async () => {
  const calls = [];
  const second = 'https://api.github.com/repos/o/r/issues?per_page=100&page=2';
  const github = new GitHub({ token: 'test-token', fetchImpl: async (url, options) => {
    calls.push(url);
    assert.equal(options.headers.Authorization, 'Bearer test-token');
    if (calls.length === 1) return response(
      Array.from({ length: 100 }, (_, i) => issue(i + 1)),
      { headers: { link: `<${second}>; rel="next", <${second}>; rel="last"` } });
    assert.equal(url, second);
    return response(Array.from({ length: 51 }, (_, i) => issue(i + 101)));
  } });
  const reports = await github.pages('/repos/o/r/issues?per_page=100');
  assert.equal(calls.length, 2);
  assert.equal(reports.length, 151);
  assert.equal(rankIssues(reports)[0].total, 151);
});

test('live retrieval reads only issues and candidate issue events, never code contributions', async () => {
  const calls = [];
  const github = { async pages(path) {
    calls.push(path);
    if (calls.length === 1) return [issue(1, 'reporter'),
      issue(2, 'suggestion-author', ['enhancement']), issue(3, 'a', ['bug'], { state: 'open' }),
      issue(4, 'pr-author', ['bug'], { pull_request: {} })];
    if (path.includes('/1/events')) return [{ event: 'marked_as_duplicate' }];
    if (path.includes('/2/events')) return [{ event: 'closed', actor: { login: 'maintainer' } }];
    throw new Error(`Unexpected API path: ${path}`);
  } };
  const result = await fetchLeaderboard('owner/repo', github);
  assert.equal(result.issueCount, 3);
  assert.deepEqual(result.rows, [
    { login: 'suggestion-author', bugs: 0, enhancements: 1, total: 1 },
  ]);
  assert.equal(calls.length, 3);
  assert.match(calls[0], /state=all.*sort=created.*per_page=100/);
  await assert.rejects(fetchLeaderboard('bad/path/extra', github), /owner\/name/);
});

test('candidate event pagination detects a duplicate on a later page', async () => {
  const github = new GitHub({ fetchImpl: async url => {
    if (!url.includes('/events')) return response([issue(1)]);
    if (!url.includes('page=2')) return response([{ event: 'closed' }], {
      headers: { link: '<https://api.github.com/repos/o/r/issues/1/events?page=2>; rel="next"' },
    });
    return response([{ event: 'marked_as_duplicate' }]);
  } });
  assert.deepEqual((await fetchLeaderboard('o/r', github)).rows, []);
});

test('transient server and network failures retry with bounded backoff', async () => {
  const waits = [];
  let attempts = 0;
  const github = new GitHub({ wait: async delay => waits.push(delay), fetchImpl: async () => {
    attempts++;
    if (attempts === 1) throw new TypeError('temporary network failure');
    return attempts === 2 ? response({}, { status: 503 }) : response([]);
  } });
  assert.deepEqual(await github.pages('/repos/o/r/issues'), []);
  assert.deepEqual(waits, [1000, 2000]);
});

test('rate limiting respects Retry-After seconds, dates, and primary reset times', async () => {
  for (const headers of [
    { 'retry-after': '2' },
    { 'retry-after': 'Thu, 01 Jan 1970 00:00:02 GMT' },
    { 'x-ratelimit-remaining': '0', 'x-ratelimit-reset': '1' },
  ]) {
    const waits = [];
    let attempts = 0;
    const github = new GitHub({ now: () => 0, wait: async delay => waits.push(delay),
      fetchImpl: async () => ++attempts === 1
        ? response({}, { status: headers['retry-after'] ? 429 : 403, headers }) : response([]) });
    await github.pages('/repos/o/r/issues');
    assert.deepEqual(waits, [2000]);
  }
});

test('secondary rate limits back off and long primary limits fail without partial results', async () => {
  const waits = [];
  let attempts = 0;
  const github = new GitHub({ wait: async delay => waits.push(delay), fetchImpl: async () =>
    ++attempts === 1 ? response({ message: 'secondary rate limit' }, { status: 403 }) : response([]) });
  await github.pages('/repos/o/r/issues');
  assert.deepEqual(waits, [60_000]);
  const limited = new GitHub({ now: () => 0, fetchImpl: async () => response({}, {
    status: 403, headers: { 'x-ratelimit-remaining': '0', 'x-ratelimit-reset': '3600' },
  }) });
  await assert.rejects(limited.pages('/repos/o/r/issues'), /exceeds two minutes/);
});

test('permanent errors fail immediately and transient retries stop after five attempts', async () => {
  for (const status of [401, 403, 404, 503]) {
    let calls = 0;
    const github = new GitHub({ wait: async () => {}, fetchImpl: async () => {
      calls++;
      return response({}, { status });
    } });
    await assert.rejects(github.pages('/repos/o/r/issues'), new RegExp(`HTTP ${status}`));
    assert.equal(calls, status === 503 ? 5 : 1);
  }
});

test('API errors cannot return a silently incomplete leaderboard', async () => {
  const github = new GitHub({ fetchImpl: async url => url.includes('/events')
    ? response({}, { status: 404 }) : response([issue(1)]) });
  await assert.rejects(fetchLeaderboard('o/r', github), /HTTP 404/);
});

test('malformed API data, pagination cycles and untrusted hosts fail safely', async () => {
  const malformed = new GitHub({ fetchImpl: async () => response({ unexpected: true }) });
  await assert.rejects(malformed.pages('/repos/o/r/issues'), /array/);
  const cyclic = new GitHub({ fetchImpl: async () => response([], {
    headers: { link: '<https://api.github.com/repos/o/r/issues>; rel="next"' },
  }) });
  await assert.rejects(cyclic.pages('/repos/o/r/issues'), /repeated pagination/);
  const untrusted = new GitHub({ fetchImpl: async () => response([], {
    headers: { link: '<https://untrusted.example/issues>; rel="next"' },
  }) });
  await assert.rejects(untrusted.pages('/repos/o/r/issues'), /different host/);
});
