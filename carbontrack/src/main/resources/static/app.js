const API = "/api";
let households = [];
let challenges = [];

function toast(msg, isError) {
  const el = document.getElementById("toast");
  el.textContent = msg;
  el.className = "toast show" + (isError ? " error" : "");
  setTimeout(() => el.classList.remove("show"), 3200);
}

async function apiCall(path, options) {
  const res = await fetch(API + path, { headers: { "Content-Type": "application/json" }, ...options });
  if (!res.ok) {
    let msg = res.statusText;
    try {
      const body = await res.json();
      msg = body.message || body.error || msg;
    } catch (e) {}
    throw new Error(msg);
  }
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

const get = (p) => apiCall(p);
const post = (p, d) => apiCall(p, { method: "POST", body: JSON.stringify(d) });
const put = (p, d) => apiCall(p, { method: "PUT", body: d === undefined ? undefined : JSON.stringify(d) });
const del = (p) => apiCall(p, { method: "DELETE" });

function esc(v) {
  return String(v ?? "").replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}
function badge(s) { return s ? `<span class="badge ${esc(s)}">${esc(s)}</span>` : "—"; }
function num(v) { return v === null || v === undefined ? "—" : Number(v).toFixed(2); }
function pct(v) {
  if (v === null || v === undefined) return "—";
  return `<span class="${v >= 0 ? "pos" : "neg"}">${Number(v).toFixed(2)}%</span>`;
}
function table(headers, rows) {
  if (!rows.length) return `<p class="empty">No records found.</p>`;
  return `<table><thead><tr>${headers.map((h) => `<th>${h}</th>`).join("")}</tr></thead><tbody>${rows.join("")}</tbody></table>`;
}
function formData(id) {
  const data = {};
  new FormData(document.getElementById(id)).forEach((v, k) => { data[k] = v === "" ? null : v; });
  return data;
}
function fillForm(id, obj) {
  const form = document.getElementById(id);
  Object.keys(obj).forEach((k) => { if (form.elements[k]) form.elements[k].value = obj[k] ?? ""; });
  window.scrollTo({ top: 0, behavior: "smooth" });
}
function resetForm(id) {
  const form = document.getElementById(id);
  form.reset();
  if (form.elements.id) form.elements.id.value = "";
}
async function run(fn) {
  try { await fn(); } catch (e) { toast(e.message, true); }
}
function currentMonth(offset = 0) {
  const d = new Date();
  d.setMonth(d.getMonth() + offset, 1);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`;
}
function today() { return new Date().toISOString().slice(0, 10); }

document.querySelectorAll(".nav-btn").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".nav-btn").forEach((b) => b.classList.remove("active"));
    document.querySelectorAll(".tab-panel").forEach((p) => p.classList.remove("active"));
    btn.classList.add("active");
    document.getElementById(btn.dataset.tab).classList.add("active");
    loadTab(btn.dataset.tab);
  });
});

function loadTab(tab) {
  run(async () => {
    if (tab === "households") await loadHouseholds();
    if (tab === "activities") await loadActivities();
    if (tab === "footprint") await loadFootprintTab();
    if (tab === "challenges") await loadChallenges();
    if (tab === "participation") await loadParticipation();
    if (tab === "leaderboard") await loadLeaderboard();
  });
}

async function refreshLookups() {
  [households, challenges] = await Promise.all([get("/household/getall"), get("/challenge/getall")]);
  const options = households.map((h) => `<option value="${h.id}">${esc(h.name)} (#${h.id})</option>`).join("");
  document.querySelectorAll(".household-select").forEach((sel) => {
    const prev = sel.value;
    sel.innerHTML = `<option value="">Select household</option>` + options;
    sel.value = prev;
  });
  const filter = document.getElementById("activityFilter");
  const prevFilter = filter.value;
  filter.innerHTML = `<option value="">All households</option>` + options;
  filter.value = prevFilter;
  const joinSel = document.getElementById("joinChallenge");
  const prevJoin = joinSel.value;
  joinSel.innerHTML = `<option value="">Select challenge</option>` +
    challenges.map((c) => `<option value="${c.id}">${esc(c.title)} [${c.status}]</option>`).join("");
  joinSel.value = prevJoin;
}

async function loadHouseholds() {
  await refreshLookups();
  const rows = households.map((h) => `<tr>
    <td>${h.id}</td><td>${esc(h.name)}</td><td>${esc(h.email)}</td><td>${esc(h.city)}</td>
    <td>${h.numberOfMembers}</td><td>${badge(h.status)}</td>
    <td><button class="small" onclick='editHousehold(${h.id})'>Edit</button>
        <button class="small danger" onclick='deleteHousehold(${h.id})'>Delete</button></td></tr>`);
  document.getElementById("householdTable").innerHTML =
    table(["ID", "Name", "Email", "City", "Members", "Status", "Actions"], rows);
}
function editHousehold(id) { fillForm("householdForm", households.find((h) => h.id === id)); }
function deleteHousehold(id) {
  if (!confirm("Delete this household and all its logs and participations?")) return;
  run(async () => { await del(`/household/delete/${id}`); toast("Household deleted"); await loadHouseholds(); });
}
document.getElementById("householdForm").addEventListener("submit", (e) => {
  e.preventDefault();
  run(async () => {
    const d = formData("householdForm");
    d.numberOfMembers = Number(d.numberOfMembers);
    if (d.id) { d.id = Number(d.id); await put("/household/update", d); toast("Household updated"); }
    else { delete d.id; await post("/household/create", d); toast("Household created"); }
    resetForm("householdForm");
    await loadHouseholds();
  });
});

async function loadActivities() {
  await refreshLookups();
  if (!document.getElementById("activityTypeSelect").options.length) {
    const factors = await get("/footprint/factors");
    document.getElementById("activityTypeSelect").innerHTML = factors
      .map((f) => `<option value="${f.activityType}">${f.activityType} (${f.unit})</option>`).join("");
  }
  const form = document.getElementById("activityForm");
  if (!form.elements.logDate.value) form.elements.logDate.value = today();
  const hid = document.getElementById("activityFilter").value;
  const logs = await get(hid ? `/activity/byhousehold/${hid}` : "/activity/getall");
  const rows = logs.map((l) => `<tr>
    <td>${l.id}</td><td>${esc(l.household.name)}</td><td>${l.logDate}</td><td>${l.activityType}</td>
    <td>${num(l.quantity)} ${esc(l.unit)}</td><td>${l.emissionFactor}</td><td>${num(l.emissionKg)}</td>
    <td><button class="small" onclick='editActivity(${l.id})'>Edit</button>
        <button class="small danger" onclick='deleteActivity(${l.id})'>Delete</button></td></tr>`);
  window.currentLogs = logs;
  document.getElementById("activityTable").innerHTML =
    table(["ID", "Household", "Date", "Type", "Quantity", "Factor", "kg CO2e", "Actions"], rows);
}
function editActivity(id) {
  const l = window.currentLogs.find((x) => x.id === id);
  fillForm("activityForm", { id: l.id, householdId: l.household.id, logDate: l.logDate,
    activityType: l.activityType, quantity: l.quantity, notes: l.notes });
}
function deleteActivity(id) {
  run(async () => { await del(`/activity/delete/${id}`); toast("Activity deleted"); await loadActivities(); });
}
document.getElementById("activityFilter").addEventListener("change", () => run(loadActivities));
document.getElementById("activityForm").addEventListener("submit", (e) => {
  e.preventDefault();
  run(async () => {
    const d = formData("activityForm");
    d.householdId = Number(d.householdId);
    d.quantity = Number(d.quantity);
    if (d.id) { d.id = Number(d.id); await put("/activity/update", d); toast("Activity updated"); }
    else { delete d.id; await post("/activity/create", d); toast("Activity logged"); }
    resetForm("activityForm");
    document.getElementById("activityForm").elements.logDate.value = today();
    await loadActivities();
  });
});

async function loadFootprintTab() {
  await refreshLookups();
  const monthInput = document.getElementById("footprintMonth");
  if (!monthInput.value) monthInput.value = currentMonth();
  const factors = await get("/footprint/factors");
  document.getElementById("factorTable").innerHTML = table(
    ["Activity type", "Category", "Unit", "kg CO2e per unit", "Max per entry"],
    factors.map((f) => `<tr><td>${f.activityType}</td><td>${f.category}</td><td>${f.unit}</td>
      <td>${f.kgCo2ePerUnit}</td><td>${f.maxPerEntry} ${f.unit}</td></tr>`));
}
document.getElementById("footprintBtn").addEventListener("click", () => run(async () => {
  const hid = document.getElementById("footprintHousehold").value;
  if (!hid) throw new Error("Select a household first");
  const month = document.getElementById("footprintMonth").value;
  const f = await get(`/footprint/household/${hid}?month=${month}`);
  const breakdown = Object.entries(f.byActivity)
    .map(([k, v]) => `<tr><td>${k}</td><td>${num(v)}</td></tr>`).join("");
  const cats = Object.entries(f.byCategory).map(([k, v]) => `${k}: <b>${num(v)}</b> kg`).join(" &nbsp;|&nbsp; ");
  document.getElementById("footprintResult").innerHTML = `
    <div class="summary">
      <div>${esc(f.householdName)} — ${f.month} ${f.monthComplete ? "" : "(month in progress, projected)"}</div>
      <div class="big">${num(f.estimatedMonthlyKg)} kg CO2e</div>
      <div>Logged so far: ${num(f.loggedTotalKg)} kg over ${f.loggedDays} day(s), ${f.logCount} log(s)</div>
      <div>${cats}</div>
    </div>
    <div class="table-wrap"><table><thead><tr><th>Activity</th><th>kg CO2e</th></tr></thead><tbody>${breakdown}</tbody></table></div>`;
  const history = await get(`/footprint/history/${hid}?months=6`);
  document.getElementById("historyTable").innerHTML = table(
    ["Month", "Logs", "Estimated kg CO2e", "Change vs previous month"],
    history.map((m) => `<tr><td>${m.month}${m.monthComplete ? "" : " *"}</td><td>${m.logCount}</td>
      <td>${num(m.estimatedMonthlyKg)}</td><td>${pct(m.reductionPercentVsPreviousMonth)}</td></tr>`));
}));

async function loadChallenges() {
  await refreshLookups();
  const rows = challenges.map((c) => `<tr>
    <td>${c.id}</td><td>${esc(c.title)}</td><td>${esc(c.description)}</td><td>${c.targetReductionPercent}%</td>
    <td>${c.startDate}</td><td>${c.endDate}</td><td>${badge(c.status)}</td>
    <td><button class="small" onclick='editChallenge(${c.id})'>Edit</button>
        <button class="small danger" onclick='deleteChallenge(${c.id})'>Delete</button></td></tr>`);
  document.getElementById("challengeTable").innerHTML =
    table(["ID", "Title", "Description", "Target", "Start", "End", "Status", "Actions"], rows);
}
function editChallenge(id) { fillForm("challengeForm", challenges.find((c) => c.id === id)); }
function deleteChallenge(id) {
  run(async () => { await del(`/challenge/delete/${id}`); toast("Challenge deleted"); await loadChallenges(); });
}
document.getElementById("challengeForm").addEventListener("submit", (e) => {
  e.preventDefault();
  run(async () => {
    const d = formData("challengeForm");
    d.targetReductionPercent = Number(d.targetReductionPercent);
    if (d.id) { d.id = Number(d.id); await put("/challenge/update", d); toast("Challenge updated"); }
    else { delete d.id; await post("/challenge/create", d); toast("Challenge created"); }
    resetForm("challengeForm");
    await loadChallenges();
  });
});

async function loadParticipation() {
  await refreshLookups();
  const list = await get("/participation/getall");
  const rows = list.map((p) => `<tr>
    <td>${p.id}</td><td>${esc(p.household.name)}</td><td>${esc(p.challenge.title)}</td>
    <td>${p.baselineMonth}: ${num(p.baselineFootprintKg)}</td><td>${num(p.currentFootprintKg)}</td>
    <td>${pct(p.reductionPercent)} / ${p.challenge.targetReductionPercent}%</td><td>${badge(p.status)}</td>
    <td><button class="small" onclick='refreshProgress(${p.id})'>Refresh</button>
        <button class="small danger" onclick='withdraw(${p.id})'>Withdraw</button></td></tr>`);
  document.getElementById("participationTable").innerHTML =
    table(["ID", "Household", "Challenge", "Baseline (kg)", "Current (kg)", "Reduction / Target", "Status", "Actions"], rows);
}
function refreshProgress(id) {
  run(async () => { await put(`/participation/progress/${id}`); toast("Progress updated"); await loadParticipation(); });
}
function withdraw(id) {
  run(async () => { await del(`/participation/withdraw/${id}`); toast("Withdrawn"); await loadParticipation(); });
}
document.getElementById("joinForm").addEventListener("submit", (e) => {
  e.preventDefault();
  run(async () => {
    const d = formData("joinForm");
    await post("/participation/join", { householdId: Number(d.householdId), challengeId: Number(d.challengeId) });
    toast("Joined challenge");
    await loadParticipation();
  });
});

async function loadLeaderboard() {
  const input = document.getElementById("leaderboardMonth");
  if (!input.value) input.value = currentMonth(-1);
  const lb = await get(`/leaderboard?month=${input.value}`);
  const rows = lb.entries.map((e) => `<tr>
    <td><b>#${e.rank}</b></td><td>${esc(e.householdName)}</td><td>${esc(e.city)}</td>
    <td>${num(e.previousMonthKg)}</td><td>${num(e.currentMonthKg)}</td><td>${pct(e.reductionPercent)}</td></tr>`);
  document.getElementById("leaderboardTable").innerHTML = table(
    ["Rank", "Household", "City", `${lb.previousMonth} (kg)`, `${lb.month} (kg)${lb.monthComplete ? "" : " projected"}`, "Reduction"], rows);
}
document.getElementById("leaderboardBtn").addEventListener("click", () => run(loadLeaderboard));

loadTab("households");
