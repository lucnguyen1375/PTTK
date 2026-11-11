// HTTP acceptance against a running local app; no schema or business data changes.
const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { randomUUID } = require('node:crypto');
const env = Object.fromEntries(fs.readFileSync(path.join(__dirname, '../.env'), 'utf8').split(/\r?\n/)
  .filter(line => /^[A-Z_]+=/.test(line)).map(line => {
    const at = line.indexOf('=');
    return [line.slice(0, at), line.slice(at + 1).trim().replace(/^(['"])(.*)\1$/, '$2')];
  }));
assert.ok(env.DEMO_PASSWORD, 'Local DEMO_PASSWORD is required');
const base = 'http://localhost:8080';
const cookies = new Map();
let checks = 0;
function check(name, fn) { fn(); checks++; console.log('PASS ' + name); }
async function request(url, method = 'GET', body) {
  const headers = { Cookie: [...cookies].map(([k, v]) => `${k}=${v}`).join('; ') };
  if (body) headers['Content-Type'] = 'application/x-www-form-urlencoded';
  const response = await fetch(base + url, { method, headers, body: body ? new URLSearchParams(body) : undefined, redirect: 'manual' });
  for (const cookie of response.headers.getSetCookie()) {
    const pair = cookie.split(';')[0]; const at = pair.indexOf('=');
    cookies.set(pair.slice(0, at), pair.slice(at + 1));
  }
  return { status: response.status, headers: response.headers, text: await response.text() };
}
const token = page => {
  const match = page.text.match(/name="_csrf" value="([^"]+)"/);
  assert.ok(match, 'Expected a CSRF field'); return match[1];
};
(async () => {
  let r = await request('/health/ready'); check('readiness', () => assert.equal(r.status, 200));
  r = await request('/'); check('root redirects to login', () => { assert.equal(r.status, 302); assert.equal(r.headers.get('location'), '/login'); });
  r = await request('/app/home'); check('unauthenticated GET rejected', () => assert.equal(r.headers.get('location'), '/login?expired=1'));
  r = await request('/app/home', 'POST', {}); check('unauthenticated write rejected', () => assert.equal(r.status, 401));
  cookies.clear(); // Previous error JSP may already have created an anonymous session.
  r = await request('/login'); const csrf = token(r), oldId = cookies.get('JSESSIONID');
  check('session cookie flags', () => { const c = r.headers.get('set-cookie'); assert.match(c, /HttpOnly/i); assert.match(c, /SameSite=Lax/i); });
  r = await request('/login', 'POST', { username: 'a', password: env.DEMO_PASSWORD, _csrf: 'invalid' });
  check('login rejects invalid CSRF', () => assert.equal(r.status, 403));
  r = await request('/login', 'POST', { username: ' ', password: 'x', _csrf: csrf });
  check('blank username rejected', () => assert.equal(r.status, 400));
  const probe = 'review_' + randomUUID().slice(0, 8);
  for (let i = 1; i <= 5; i++) {
    r = await request('/login', 'POST', { username: probe, password: 'invalid', _csrf: csrf });
    check('invalid attempt ' + i, () => { assert.equal(r.status, 401); assert.ok(r.text.includes('Tên đăng nhập hoặc mật khẩu không đúng.')); });
  }
  r = await request('/login', 'POST', { username: probe.toUpperCase(), password: 'invalid', _csrf: csrf });
  check('sixth attempt blocked across case variants', () => { assert.equal(r.status, 429); assert.ok(Number(r.headers.get('retry-after')) > 0); });
  r = await request('/login', 'POST', { username: 'a', password: env.DEMO_PASSWORD, _csrf: csrf });
  check('employee login and session rotation', () => { assert.equal(r.status, 302); assert.equal(r.headers.get('location'), '/app/home'); assert.notEqual(cookies.get('JSESSIONID'), oldId); });
  r = await request('/app/home?employeeId=999'); const logoutToken = token(r);
  check('employee identity comes from authenticated session', () => { assert.equal(r.status, 200); assert.ok(r.text.includes('Nguyễn Văn A')); assert.ok(r.text.includes('NV000001')); assert.notEqual(logoutToken, csrf); assert.ok(!r.text.includes(env.DEMO_PASSWORD)); });
  r = await request('/logout', 'POST', { _csrf: csrf }); check('old CSRF token rejected', () => assert.equal(r.status, 403));
  r = await request('/logout'); check('GET cannot log out', () => assert.equal(r.status, 405));
  r = await request('/logout', 'POST', { _csrf: logoutToken }); check('logout succeeds', () => { assert.equal(r.status, 303); assert.equal(r.headers.get('location'), '/login?loggedOut=1'); });
  r = await request('/app/home'); check('old session cannot access home', () => assert.equal(r.headers.get('location'), '/login?expired=1'));
  r = await request('/app/home', 'POST', { _csrf: logoutToken }); check('old session cannot write', () => assert.equal(r.status, 401));
  console.log(`${checks}/${checks} HTTP checks passed`);
})().catch(error => { console.error('FAIL auth acceptance:', error.message); process.exitCode = 1; });
