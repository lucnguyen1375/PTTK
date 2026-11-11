// One-off integration acceptance for transaction rollback. Run only against the local development database.
const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { randomUUID } = require('node:crypto');
const { spawnSync } = require('node:child_process');

const env = Object.fromEntries(fs.readFileSync(path.join(__dirname, '../.env'), 'utf8')
  .split(/\r?\n/).filter(x => /^[A-Z_]+=/.test(x)).map(x => {
    const i = x.indexOf('=');
    return [x.slice(0, i), x.slice(i + 1).trim().replace(/^(['"])(.*)\1$/, '$2')];
  }));
const root = path.resolve(__dirname, '..');
const base = 'http://localhost:8080';
const cookies = new Map();
let draftId = '';
const titleName = `Rollback Acceptance ${randomUUID()}`;
const trigger = 'm3_acceptance_fail_copy_insert';
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

function sql(query) {
  const result = spawnSync('docker', [
    'compose', 'exec', '-T', 'db', 'sh', '-c',
    'mysql --user=root --password="$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE" --batch --skip-column-names'
  ], { cwd: root, input: query, encoding: 'utf8' });
  if (result.status !== 0) throw new Error('Database rollback acceptance query failed.');
  return result.stdout.trim();
}

function cleanup() {
  const safeKey = draftId;
  const safeTitle = titleName.replace(/[^a-z0-9 -]/gi, '');
  sql(`DROP TRIGGER IF EXISTS ${trigger};
SET @rollback_acceptance_id=(SELECT id FROM tblPhieuNhap WHERE requestKey='${safeKey}' LIMIT 1);
DELETE FROM tblQuyenTruyen WHERE chiTietPhieuNhapId IN (SELECT id FROM tblChiTietPhieuNhap WHERE phieuNhapId=@rollback_acceptance_id);
DELETE FROM tblChiTietPhieuNhap WHERE phieuNhapId=@rollback_acceptance_id;
DELETE FROM tblPhieuNhap WHERE id=@rollback_acceptance_id;
DELETE FROM tblDauTruyen WHERE tenDauTruyen='${safeTitle}';`);
}

async function run() {
  const dbName = sql('SELECT DATABASE();');
  if (!/^pttk_m3(?:_|$)/i.test(dbName)) throw new Error('Refusing to add a temporary trigger outside the local PTTK development schema.');
  sql(`DROP TRIGGER IF EXISTS ${trigger};
CREATE TRIGGER ${trigger} BEFORE INSERT ON tblQuyenTruyen FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='forced acceptance failure';`);

  cookies.clear();
  let response = await req('/login');
  response = await req('/login', 'POST', { username: 'a', password: env.DEMO_PASSWORD, _csrf: csrf(response) });
  check('employee login', () => assert.equal(response.location, '/app/home'));
  response = await req('/app/imports');
  response = await req('/app/imports/create', 'POST', { _csrf: csrf(response), supplierId: '101' });
  draftId = new URL(response.location, base).searchParams.get('id');
  check('rollback draft created', () => assert.equal(response.status, 302));
  response = await req(`/app/imports/draft?id=${draftId}`);
  response = await req('/app/imports/line/add-existing', 'POST', {
    _csrf: csrf(response), draftId, version: '0', titleId: '1', cost: '1000', rent: '200', quantity: '2'
  });
  response = await req(`/app/imports/draft?id=${draftId}`);
  response = await req('/app/imports/line/add-new', 'POST', {
    _csrf: csrf(response), draftId, version: '1', name: titleName, author: 'Rollback Author',
    category: 'Test', publisher: 'PTTK', year: '', cost: '2000', rent: '300', quantity: '3'
  });
  response = await req(`/app/imports/draft?id=${draftId}`);
  check('two import lines are present before save', () => {
    assert.equal(response.status, 200);
    assert.ok(response.text.includes(titleName));
  });

  const version = (response.text.match(/Phiên bản (\d+)/) || [])[1];
  response = await req('/app/imports/save', 'POST', { _csrf: csrf(response), draftId, version });
  check('forced copy insert failure reports a handled save error', () => {
    assert.equal(response.status, 503);
    assert.ok(response.text.includes('Giao dịch đã được hoàn tác.'));
    assert.ok(!response.text.includes('forced acceptance failure'));
  });

  const persisted = sql(`SELECT CONCAT(
(SELECT COUNT(*) FROM tblPhieuNhap WHERE requestKey='${draftId}'), '|',
(SELECT COUNT(*) FROM tblChiTietPhieuNhap d JOIN tblPhieuNhap p ON p.id=d.phieuNhapId WHERE p.requestKey='${draftId}'), '|',
(SELECT COUNT(*) FROM tblQuyenTruyen q JOIN tblChiTietPhieuNhap d ON d.id=q.chiTietPhieuNhapId JOIN tblPhieuNhap p ON p.id=d.phieuNhapId WHERE p.requestKey='${draftId}'), '|',
(SELECT COUNT(*) FROM tblDauTruyen WHERE tenDauTruyen='${titleName}')
);`);
  check('transaction left no invoice, lines, copies or new title', () => assert.equal(persisted, '0|0|0|0'));
  response = await req(`/app/imports/draft?id=${draftId}`);
  check('temporary draft remains available for retry', () => {
    assert.equal(response.status, 200);
    assert.ok(response.text.includes(titleName));
  });

  response = await req('/app/imports/cancel', 'POST', {
    _csrf: csrf(response), draftId, version, confirmed: '1'
  });
  check('failed draft can be canceled after retry decision', () => assert.equal(response.status, 302));
  console.log(`${checks}/${checks} transaction rollback checks passed.`);
}

run().catch(error => {
  console.error('FAIL transaction rollback acceptance:', error.message);
  process.exitCode = 1;
}).finally(() => {
  try {
    cleanup();
    console.log('Temporary trigger removed and rollback test identifiers cleaned.');
  } catch (error) {
    console.error('CLEANUP FAILED: remove the acceptance trigger and rows before continuing.', error.message);
    process.exitCode = 1;
  }
});
