const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');

const env = Object.fromEntries(fs.readFileSync(path.join(__dirname, '../.env'), 'utf8')
  .split(/\r?\n/).filter(x => /^[A-Z_]+=/.test(x)).map(x => {
    const i = x.indexOf('=');
    return [x.slice(0, i), x.slice(i + 1).trim().replace(/^(['"])(.*)\1$/, '$2')];
  }));
const base = 'http://localhost:8080';
const cookies = new Map();
let checks = 0;

async function req(url, method = 'GET', body) {
  const headers = { Cookie: [...cookies].map(([k, v]) => `${k}=${v}`).join('; ') };
  if (body) headers['Content-Type'] = 'application/x-www-form-urlencoded';
  const response = await fetch(base + url, {
    method, headers,
    body: body ? new URLSearchParams(body) : undefined,
    redirect: 'manual'
  });
  for (const cookie of response.headers.getSetCookie()) {
    const pair = cookie.split(';')[0];
    const i = pair.indexOf('=');
    cookies.set(pair.slice(0, i), pair.slice(i + 1));
  }
  return { status: response.status, location: response.headers.get('location'), text: await response.text() };
}

function csrf(response) {
  const match = response.text.match(/name="_csrf" value="([^"]+)"/);
  assert.ok(match, `CSRF field missing (HTTP ${response.status})`);
  return match[1];
}

function check(name, assertion) {
  assertion();
  checks++;
  console.log(`PASS ${name}`);
}

async function report(from, to) {
  let response = await req(`/app/reports/suppliers?run=1&from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`);
  assert.equal(response.status, 302);
  response = await req(response.location);
  return response;
}

(async () => {
  cookies.clear();
  let response = await req('/login');
  response = await req('/login', 'POST', { username: 'a', password: env.DEMO_PASSWORD, _csrf: csrf(response) });
  check('employee login', () => assert.equal(response.location, '/app/home'));

  response = await req('/app/reports/suppliers?run=1&from=&to=2025-01-31');
  check('missing start date is a field error', () => {
    assert.equal(response.status, 400);
    assert.ok(response.text.includes('Ngày bắt đầu là bắt buộc.'));
  });
  response = await req('/app/reports/suppliers?run=1&from=2025%2F01%2F01&to=2025-01-31');
  check('malformed date is rejected', () => {
    assert.equal(response.status, 400);
    assert.ok(response.text.includes('Ngày bắt đầu không đúng định dạng ngày.'));
  });
  response = await req('/app/reports/suppliers?run=1&from=2025-02-01&to=2025-01-01');
  check('reversed date range is rejected', () => {
    assert.equal(response.status, 400);
    assert.ok(response.text.includes('Ngày bắt đầu không được sau ngày kết thúc.'));
  });

  response = await report('2025-01-01', '2025-01-31');
  const snapshot = (response.location || '').match(/snapshot=([0-9a-f-]{36})/)?.[1]
    || (response.text.match(/snapshot=([0-9a-f-]{36})/) || [])[1];
  const link = (response.text.match(/\/app\/reports\/suppliers\/details\?snapshot=([0-9a-f-]{36})&amp;supplierId=(\d+)/) || []);
  check('January report orders positive-revenue suppliers and shows inclusive dates', () => {
    assert.equal(response.status, 200);
    assert.ok(response.text.includes('2025-01-01'));
    assert.ok(response.text.includes('2025-01-31'));
    assert.ok(response.text.indexOf('NCC000101') < response.text.indexOf('NCC000102'));
    assert.ok(response.text.includes('37.000 VND'));
    assert.ok(response.text.includes('30.000 VND'));
    assert.ok(!response.text.includes('NCC000103'));
    assert.ok(link[1]);
  });

  const reportSnapshot = link[1] || snapshot;
  response = await req(`/app/reports/suppliers/details?snapshot=${reportSnapshot}&supplierId=101`);
  check('supplier detail contains only its portion of return invoices', () => {
    assert.equal(response.status, 200);
    assert.ok(response.text.includes('HDT000001'));
    assert.ok(response.text.includes('Tổng phần doanh thu hóa đơn: 37.000 VND'));
    assert.ok(response.text.includes('Quay lại thống kê'));
  });
  response = await req(`/app/reports/suppliers?snapshot=${reportSnapshot}`);
  check('back link restores the same report snapshot and filter', () => {
    assert.equal(response.status, 200);
    assert.ok(response.text.includes('2025-01-01'));
    assert.ok(response.text.includes('2025-01-31'));
    assert.ok(response.text.includes('37.000 VND'));
    assert.ok(response.text.includes('30.000 VND'));
  });

  response = await report('2025-01-03', '2025-01-03');
  check('single-day range includes both boundary dates and separates two suppliers', () => {
    assert.equal(response.status, 200);
    assert.ok(response.text.includes('20.000 VND'));
    assert.ok(response.text.includes('30.000 VND'));
  });
  response = await report('2025-02-01', '2025-02-01');
  check('February range reports the seeded 15,000 VND return', () => {
    assert.equal(response.status, 200);
    assert.ok(response.text.includes('15.000 VND'));
    assert.ok(!response.text.includes('NCC000101'));
  });
  response = await report('2024-01-01', '2024-12-31');
  check('zero-result range is an empty state, not a server error', () => {
    assert.equal(response.status, 200);
    assert.ok(response.text.includes('Không có doanh thu trong khoảng ngày'));
  });

  response = await req('/app/home');
  await req('/logout', 'POST', { _csrf: csrf(response) });
  console.log(`${checks}/${checks} P7 HTTP checks passed.`);
})().catch(error => {
  console.error('FAIL P7 acceptance:', error.message);
  process.exitCode = 1;
});
