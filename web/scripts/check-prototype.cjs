// Regression checks for prototype state and event handlers, using a minimal DOM stub.
// This does not replace browser layout/accessibility/interaction acceptance.
const fs = require('node:fs');
const vm = require('node:vm');
const assert = require('node:assert/strict');
const path = require('node:path');
const html = fs.readFileSync(path.join(__dirname, '../prototype/index.html'), 'utf8');
const source = html.slice(html.indexOf('<script>') + 8, html.lastIndexOf('</script>'));
function fixture() {
  const handlers = {};
  const alert = { textContent: '', classList: { add() {} } };
  const app = { innerHTML: '', insertAdjacentHTML(position, html) { this.innerHTML += html; },
    querySelector(selector) { return selector === '.modal-back' ? { remove() { app.innerHTML = app.innerHTML.split('<div class="modal-back">')[0]; } } : alert; },
    addEventListener(type, fn) { (handlers[type] ??= []).push(fn); } };
  const context = vm.createContext({
    document: { querySelector: selector => selector === '#app' ? app : null,
      createElement: () => ({ remove() {} }), body: { append() {} } },
    window: { addEventListener() {} }, location: { search: '' }, URLSearchParams,
    setTimeout() {}, prompt: () => '0',
    FormData: class { constructor(form) { this.values = form.values; } get(k) { return this.values[k]; } }
  });
  vm.runInContext(source, context);
  const run = code => vm.runInContext(code, context);
  return { run, app, alert,
    click(act, extra = {}) { const button = { dataset: { act, ...extra } }; for (const fn of handlers.click) fn({ target: { closest: () => button } }); },
    submit(id, values) { for (const fn of handlers.submit) fn({ preventDefault() {}, target: { id, values } }); }
  };
}
const tests = [];
const test = (name, fn) => tests.push([name, fn]);
const supplier = { name: 'NCC mới', address: 'Hà Nội', phone: '0987654321', email: 'new@example.test' };
test('existing title renders and identical lines merge', () => {
  const f = fixture();
  f.run("state.supplier=101; addTitleLine({...state.titles[0],cost:40000,rent:10000,qty:2,isNew:false}); addTitleLine({...state.titles[0],cost:40000,rent:10000,qty:3,isNew:false})");
  assert.equal(f.run('state.draft.length'), 1);
  assert.equal(f.run('state.draft[0].qty'), 5);
  assert.match(f.app.innerHTML, /Doraemon Tập 1/);
});
test('new title renders and duplicate is rejected', () => {
  const f = fixture();
  f.run("state.supplier=101; const t={id:3,name:'Mới',author:'A',category:'B',publisher:'C',year:'',cost:100,rent:10,qty:1,isNew:true}; addTitleLine(t); addTitleLine(t)");
  assert.equal(f.run('state.titles.length'), 3);
  assert.equal(f.run('state.draft.length'), 1);
  assert.match(f.alert.textContent, /đã có/);
});
for (const action of ['nav-home', 'nav-supplier', 'nav-report']) test(`leaving title form via ${action} requires confirmation`, () => {
  const f = fixture();
  f.run("state.supplier=101; state.draft=[{title:state.titles[0],qty:1,cost:100,rent:10}]; setView('title-new')");
  f.click(action);
  assert.equal(f.run('!!state.modal'), true);
  f.click('modal-no');
  assert.equal(f.run('state.view'), 'title-new');
  assert.equal(f.run('state.draft.length'), 1);
  f.click(action); f.click('modal-yes');
  assert.equal(f.run('state.draft.length'), 0);
});
test('invoice detail uses selected supplier and closes', () => {
  const f = fixture(); f.run("state.supplier=102; setView('invoice')");
  f.click('invoice-detail', { id: 'HDT000001' });
  assert.equal(f.run('state.modal.text.includes(money(30000))'), true);
  f.click('modal-yes');
  assert.equal(f.run('state.modal'), null);
  assert.doesNotMatch(f.app.innerHTML, /role="dialog"/);
});
test('whitespace-only supplier fields are rejected', () => {
  const f = fixture(); f.run("setView('supplier-add')");
  f.submit('supplier-form', { ...supplier, name: '   ' });
  assert.equal(f.run('state.suppliers.length'), 2);
  assert.notEqual(f.alert.textContent, '');
});
test('whitespace-only title fields are rejected', () => {
  const f = fixture(); f.run("setView('title-new')");
  f.submit('title-form', { name: ' ', author: 'A', category: 'B', publisher: 'C', year: '', cost: '100', rent: '10', qty: '1' });
  assert.equal(f.run('state.draft.length'), 0);
  assert.notEqual(f.alert.textContent, '');
});
test('supplier success waits for return and has no repeat-submit stay action', () => {
  const f = fixture(); f.run("state.tab='import'; setView('supplier-add')");
  f.submit('supplier-form', supplier);
  assert.equal(f.run('state.suppliers.length'), 3);
  assert.doesNotMatch(f.app.innerHTML, /data-act="modal-no"/);
  f.click('modal-yes');
  assert.equal(f.run('state.view'), 'import-supplier');
});
test('report load error does not blame valid dates', () => {
  const f = fixture(); f.run("state.scenario='error'; setView('report')");
  assert.match(f.app.innerHTML, /Không tải được báo cáo/);
  assert.doesNotMatch(f.app.innerHTML, /Khoảng ngày không hợp lệ/);
});
test('report January totals remain 37000 and 30000', () => {
  const f = fixture(); f.run("setView('report')");
  assert.equal(f.run('report().includes(money(37000)) && report().includes(money(30000))'), true);
});
test('canceling modal preserves the underlying form markup', () => {
  const f = fixture(); f.run("setView('title-new')"); const before = f.app.innerHTML;
  f.run("ask('Rời trang?', 'Kiểm tra', ()=>setView('home'))"); f.click('modal-no');
  assert.equal(f.app.innerHTML, before);
});
test('supplier edit confirms and returns home', () => {
  const f = fixture(); f.run("state.supplier=101; setView('supplier-edit')");
  f.submit('edit-supplier-form', supplier); f.click('modal-yes');
  assert.equal(f.run('state.view'), 'home');
  assert.equal(f.run('selectedSupplier().code'), 'NCC000101');
});
test('changing supplier preserves draft on cancel and clears on confirm', () => {
  const f = fixture(); f.run("state.supplier=101; addTitleLine({...state.titles[0],cost:100,rent:10,qty:2})");
  f.click('change-supplier'); f.click('modal-no'); assert.equal(f.run('state.draft.length'), 1);
  f.click('change-supplier'); f.click('modal-yes'); assert.equal(f.run('state.draft.length'), 0);
  assert.equal(f.run('state.view'), 'import-supplier');
});
test('invoice return preserves report filter', () => {
  const f = fixture(); f.run("state.filter={from:'2025-01-03',to:'2025-01-10'}; setView('report')");
  f.click('report-detail', { id: '101' }); f.click('back-report');
  assert.equal(f.run('state.filter.from'), '2025-01-03'); assert.equal(f.run('state.filter.to'), '2025-01-10');
});
test('invalid quantity edit has a visible error region', () => {
  const f = fixture(); f.run("state.supplier=101; addTitleLine({...state.titles[0],cost:100,rent:10,qty:2})");
  assert.match(f.app.innerHTML, /class="alert /); f.click('edit-line', { index: '0' });
  assert.match(f.alert.textContent, /Số lượng không hợp lệ/);
});
let failed = 0;
for (const [name, fn] of tests) {
  try { fn(); console.log(`PASS ${name}`); }
  catch (error) { failed++; console.error(`FAIL ${name}: ${error.message}`); }
}
console.log(`${tests.length - failed}/${tests.length} checks passed`);
process.exitCode = failed ? 1 : 0;
