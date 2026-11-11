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
const supplierEmail = `phase5-acceptance-${randomUUID()}@example.test`;
const supplierPhone = `091${String(Date.now()).slice(-7)}`;
const supplierName = `P5 Acceptance ${randomUUID().slice(-8)}`;
const titleName = `Phase6 Acceptance ${randomUUID()}`;
let importKey = randomUUID();
let checks = 0;

async function req(url, method = 'GET', body) {
  const headers = { Cookie: [...cookies].map(([k, v]) => `${k}=${v}`).join('; ') };
  if (body) headers['Content-Type'] = 'application/x-www-form-urlencoded';
  const response = await fetch(base + url, {
    method,
    headers,
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
  if (result.status !== 0) throw new Error('Database acceptance query failed.');
  return result.stdout.trim();
}

function cleanup() {
  const safeKey = importKey.replace(/[^0-9a-f-]/gi, '');
  const safeEmail = supplierEmail.replace(/[^a-z0-9@._+-]/gi, '');
  const safeTitle = titleName.replace(/[^a-z0-9 -]/gi, '');
  sql(`SET @acceptance_import_id=(SELECT id FROM tblPhieuNhap WHERE requestKey='${safeKey}' LIMIT 1);
DELETE FROM tblQuyenTruyen WHERE chiTietPhieuNhapId IN (SELECT id FROM tblChiTietPhieuNhap WHERE phieuNhapId=@acceptance_import_id);
DELETE FROM tblChiTietPhieuNhap WHERE phieuNhapId=@acceptance_import_id;
DELETE FROM tblPhieuNhap WHERE id=@acceptance_import_id;
DELETE FROM tblDauTruyen WHERE tenDauTruyen='${safeTitle}';
SET @acceptance_supplier_id=(SELECT id FROM tblNhaCungCap WHERE emailChuan='${safeEmail}' LIMIT 1);
DELETE FROM tblNhaCungCap WHERE id=@acceptance_supplier_id;
DELETE FROM tblThanhVien WHERE id=@acceptance_supplier_id;`);
  const remaining = sql(`SELECT CONCAT((SELECT COUNT(*) FROM tblDauTruyen WHERE tenDauTruyen='${safeTitle}'),'|',(SELECT COUNT(*) FROM tblNhaCungCap WHERE emailChuan='${safeEmail}'));`);
  if (remaining !== '0|0') throw new Error('Acceptance cleanup left test rows behind.');
}

async function run() {
  cookies.clear();
  let response = await req('/login');
  response = await req('/login', 'POST', { username: 'a', password: env.DEMO_PASSWORD, _csrf: csrf(response) });
  check('employee login', () => assert.equal(response.location, '/app/home'));

  response = await req('/app/suppliers/add?returnTo=find');
  response = await req('/app/suppliers/add', 'POST', {
    _csrf: csrf(response), returnTo: 'find', name: supplierName,
    address: 'Acceptance test only', phone: supplierPhone, email: supplierEmail
  });
  check('supplier create commits and returns to supplier search', () => {
    assert.equal(response.status, 302);
    assert.match(response.location, /\/app\/suppliers\?returnTo=find/);
  });
  response = await req(response.location);
  const createdList = response;
  response = await req(`/app/suppliers?q=${encodeURIComponent(supplierName)}`);
  const editId = (response.text.match(/\/app\/suppliers\/edit\?id=(\d+)/) || [])[1];
  check('created supplier is searchable with a success message', () => {
    assert.ok(createdList.text.includes(supplierEmail));
    assert.ok(createdList.text.includes('Đã thêm nhà cung cấp thành công.'));
    assert.ok(response.text.includes(supplierName));
    assert.ok(editId);
  });
  response = await req(`/app/suppliers/edit?id=${editId}`);
  const editToken = (response.text.match(/name="editToken" value="([^"]+)"/) || [])[1];
  const version = (response.text.match(/name="version" value="(\d+)"/) || [])[1];
  response = await req('/app/suppliers/edit', 'POST', {
    _csrf: csrf(response), id: editId, version, editToken,
    name: `P5 Updated ${titleName.slice(-8)}`, address: 'Acceptance test updated',
    phone: supplierPhone, email: supplierEmail
  });
  check('supplier update redirects to employee home', () => {
    assert.equal(response.status, 302);
    assert.equal(response.location, '/app/home');
  });
  response = await req('/app/home');
  check('supplier update success message appears on employee home', () => {
    assert.ok(response.text.includes('Đã cập nhật nhà cung cấp thành công.'));
  });

  response = await req('/app/imports');
  response = await req('/app/imports/create', 'POST', { _csrf: csrf(response), supplierId: '101' });
  check('import draft is attached to the selected supplier', () => assert.equal(response.status, 302));
  let draftId = new URL(response.location, base).searchParams.get('id');
  importKey = draftId;
  response = await req(`/app/imports/draft?id=${draftId}`);
  response = await req('/app/imports/line/add-existing', 'POST', {
    _csrf: csrf(response), draftId, version: '0', titleId: '1', cost: '1000', rent: '200', quantity: '2'
  });
  response = await req(`/app/imports/draft?id=${draftId}`);
  response = await req('/app/imports/line/add-new', 'POST', {
    _csrf: csrf(response), draftId, version: '1', name: titleName, author: 'Acceptance Author',
    category: 'Test', publisher: 'PTTK', year: '', cost: '2000', rent: '300', quantity: '3'
  });
  response = await req(`/app/imports/draft?id=${draftId}`);
  const titleCountBeforeSave = sql(`SELECT COUNT(*) FROM tblDauTruyen WHERE tenDauTruyen='${titleName}';`);
  const invoiceRowsBeforeSave = sql(`SELECT CONCAT(
(SELECT COUNT(*) FROM tblPhieuNhap WHERE requestKey='${importKey}'), '|',
(SELECT COUNT(*) FROM tblChiTietPhieuNhap d JOIN tblPhieuNhap p ON p.id=d.phieuNhapId WHERE p.requestKey='${importKey}'), '|',
(SELECT COUNT(*) FROM tblQuyenTruyen q JOIN tblChiTietPhieuNhap d ON d.id=q.chiTietPhieuNhapId JOIN tblPhieuNhap p ON p.id=d.phieuNhapId WHERE p.requestKey='${importKey}')
);`);
  check('new title and five requested copies remain temporary before save', () => {
    assert.equal(response.status, 200);
    assert.ok(response.text.includes(titleName));
    assert.ok(response.text.includes('8000'));
    assert.equal(titleCountBeforeSave, '0');
    assert.equal(invoiceRowsBeforeSave, '0|0|0');
  });
  const saveVersion = (response.text.match(/Phiên bản (\d+)/) || [])[1];
  const saveToken = csrf(response);
  response = await req('/app/imports/save', 'POST', {
    _csrf: saveToken, draftId, version: saveVersion, supplierId: '102', employeeId: '999', total: '1'
  });
  check('import save commits one receipt and redirects to it', () => {
    assert.equal(response.status, 302);
    assert.match(response.location, /\/app\/imports\/receipt\?id=\d+/);
  });
  const receiptUrl = response.location;
  response = await req(receiptUrl);
  check('receipt reports two lines, five copies and the expected total', () => {
    assert.ok(response.text.includes('Số dòng: 2'));
    assert.ok(response.text.includes('Số quyển đã tạo: 5'));
    assert.ok(response.text.includes('Tổng tiền: 8000'));
  });

  response = await req('/app/imports/save', 'POST', { _csrf: saveToken, draftId, version: saveVersion });
  check('replaying the same save returns the existing receipt', () => {
    assert.equal(response.status, 302);
    assert.equal(response.location, receiptUrl);
  });

  const dbResult = sql(`SELECT CONCAT(
(SELECT COUNT(*) FROM tblPhieuNhap WHERE requestKey='${importKey}'), '|',
(SELECT COUNT(*) FROM tblChiTietPhieuNhap d JOIN tblPhieuNhap p ON p.id=d.phieuNhapId WHERE p.requestKey='${importKey}'), '|',
(SELECT COALESCE(SUM(qt.id IS NOT NULL),0) FROM tblQuyenTruyen qt JOIN tblChiTietPhieuNhap d ON d.id=qt.chiTietPhieuNhapId JOIN tblPhieuNhap p ON p.id=d.phieuNhapId WHERE p.requestKey='${importKey}'), '|',
(SELECT COALESCE(SUM(d.soLuong*d.donGiaNhap),0) FROM tblChiTietPhieuNhap d JOIN tblPhieuNhap p ON p.id=d.phieuNhapId WHERE p.requestKey='${importKey}'), '|',
(SELECT COUNT(*) FROM tblPhieuNhap WHERE requestKey='${importKey}' AND nhaCungCapId=101 AND nhanVienId=1), '|',
(SELECT COUNT(*) FROM tblDauTruyen WHERE tenDauTruyen='${titleName}')
);`);
  check('database contains one invoice, two lines, five copies and one new title', () => assert.equal(dbResult, '1|2|5|8000|1|1'));
  const accountCount = sql(`SELECT COUNT(*) FROM tblThanhVien tv JOIN tblNhaCungCap n ON n.id=tv.id WHERE n.emailChuan='${supplierEmail}';`);
  check('supplier creation owns exactly one linked member account', () => assert.equal(accountCount, '1'));
  console.log(`${checks}/${checks} business commit checks passed.`);
}

run().catch(error => {
  console.error('FAIL business commit acceptance:', error.message);
  process.exitCode = 1;
}).finally(() => {
  try {
    cleanup();
    console.log('Acceptance rows cleaned by unique request key and email.');
  } catch (error) {
    console.error('CLEANUP FAILED: acceptance rows may remain; cleanup by unique test identifiers.', error.message);
    process.exitCode = 1;
  }
});
