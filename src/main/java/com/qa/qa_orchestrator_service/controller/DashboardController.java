package com.qa.qa_orchestrator_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/qa")
public class DashboardController {

    @GetMapping("/dashboard")
    @ResponseBody
    public String dashboard() {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>QA Orchestrator Dashboard</title>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #f5f5f5; color: #1a1a1a; }
  .header { background: #1a1a1a; color: #fff; padding: 1rem 2rem; display: flex; align-items: center; justify-content: space-between; }
  .header h1 { font-size: 1.1rem; font-weight: 500; }
  .header .badge { background: #333; color: #aaa; font-size: 11px; padding: 3px 8px; border-radius: 4px; }
  .container { max-width: 1100px; margin: 0 auto; padding: 2rem; }
  .metrics { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 2rem; }
  .metric-card { background: #fff; border-radius: 10px; padding: 1.25rem; border: 1px solid #e8e8e8; }
  .metric-label { font-size: 12px; color: #888; margin-bottom: 6px; }
  .metric-value { font-size: 28px; font-weight: 600; }
  .metric-value.blue { color: #1a6ef5; }
  .metric-value.red { color: #dc2626; }
  .metric-value.amber { color: #d97706; }
  .metric-value.green { color: #16a34a; }
  .section { background: #fff; border-radius: 10px; border: 1px solid #e8e8e8; margin-bottom: 1.5rem; overflow: hidden; }
  .section-header { padding: 1rem 1.5rem; border-bottom: 1px solid #f0f0f0; display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap; }
  .section-title { font-size: 14px; font-weight: 500; }
  .controls { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
  .search-input { border: 1px solid #e0e0e0; border-radius: 6px; padding: 5px 10px; font-size: 12px; width: 160px; outline: none; }
  .search-input:focus { border-color: #1a6ef5; }
  .filter-select { border: 1px solid #e0e0e0; border-radius: 6px; padding: 5px 8px; font-size: 12px; outline: none; background: #fff; cursor: pointer; }
  .filter-select:focus { border-color: #1a6ef5; }
  table { width: 100%; border-collapse: collapse; font-size: 13px; }
  th { text-align: left; padding: 10px 1.5rem; font-size: 11px; color: #888; font-weight: 500; text-transform: uppercase; letter-spacing: 0.05em; background: #fafafa; border-bottom: 1px solid #f0f0f0; cursor: pointer; user-select: none; }
  th:hover { color: #1a1a1a; }
  th .sort-icon { margin-left: 4px; opacity: 0.4; }
  th.sorted .sort-icon { opacity: 1; color: #1a6ef5; }
  td { padding: 12px 1.5rem; border-bottom: 1px solid #f8f8f8; vertical-align: top; }
  tr:last-child td { border-bottom: none; }
  tr:hover td { background: #fafafa; }
  .badge-HIGH { display:inline-block; padding:2px 8px; border-radius:4px; font-size:11px; font-weight:500; background:#fee2e2; color:#991b1b; }
  .badge-MEDIUM { display:inline-block; padding:2px 8px; border-radius:4px; font-size:11px; font-weight:500; background:#fef3c7; color:#92400e; }
  .badge-LOW { display:inline-block; padding:2px 8px; border-radius:4px; font-size:11px; font-weight:500; background:#dcfce7; color:#166534; }
  .badge-Block { display:inline-block; padding:2px 8px; border-radius:4px; font-size:11px; font-weight:500; background:#fee2e2; color:#991b1b; }
  .badge-Caution { display:inline-block; padding:2px 8px; border-radius:4px; font-size:11px; font-weight:500; background:#fef3c7; color:#92400e; }
  .badge-Go { display:inline-block; padding:2px 8px; border-radius:4px; font-size:11px; font-weight:500; background:#dcfce7; color:#166534; }
  .verdict.approved { color: #16a34a; font-size:12px; font-weight:500; }
  .verdict.risk { color: #d97706; font-size:12px; font-weight:500; }
  .verdict.missing { color: #dc2626; font-size:12px; font-weight:500; }
  .chart-wrap { padding: 1.5rem; }
  .loading { text-align: center; padding: 3rem; color: #888; font-size: 14px; }
  .no-results { text-align: center; padding: 2rem; color: #aaa; font-size: 13px; }
  .btn { background: none; border: 1px solid #ddd; border-radius: 6px; padding: 5px 12px; font-size: 12px; cursor: pointer; color: #555; }
  .btn:hover { background: #f5f5f5; }
  .btn.active { background: #1a6ef5; color: #fff; border-color: #1a6ef5; }
  .ts { font-size: 11px; color: #aaa; }
  .count-badge { font-size: 11px; color: #888; margin-left: 6px; }
</style>
</head>
<body>
<div class="header">
  <h1>QA Orchestrator — Intelligence Dashboard</h1>
  <span class="badge">v2</span>
</div>
<div class="container">

  <div class="metrics" id="metrics">
    <div class="loading" style="grid-column:span 4">Loading...</div>
  </div>

  <div style="display:grid; grid-template-columns:1fr 1fr; gap:1.5rem; margin-bottom:1.5rem;">
    <div class="section">
      <div class="section-header"><span class="section-title">Risk distribution</span></div>
      <div class="chart-wrap"><div style="position:relative; height:200px;"><canvas id="riskChart"></canvas></div></div>
    </div>
    <div class="section">
      <div class="section-header"><span class="section-title">Release decisions</span></div>
      <div class="chart-wrap"><div style="position:relative; height:200px;"><canvas id="releaseChart"></canvas></div></div>
    </div>
  </div>

  <div class="section">
    <div class="section-header">
      <span class="section-title">Recent analyses <span class="count-badge" id="history-count"></span></span>
      <div class="controls">
        <input class="search-input" id="history-search" placeholder="Search issue or feature..." oninput="filterHistory()">
        <select class="filter-select" id="history-risk" onchange="filterHistory()">
          <option value="">All risks</option>
          <option value="HIGH">High</option>
          <option value="MEDIUM">Medium</option>
          <option value="LOW">Low</option>
        </select>
        <select class="filter-select" id="history-release" onchange="filterHistory()">
          <option value="">All releases</option>
          <option value="Block">Block</option>
          <option value="Caution">Caution</option>
          <option value="Go">Go</option>
        </select>
        <button class="btn" onclick="loadAll()">Refresh</button>
      </div>
    </div>
    <div id="history-table"><div class="loading">Loading...</div></div>
  </div>

  <div class="section">
    <div class="section-header">
      <span class="section-title">Blocked tickets <span class="count-badge" id="blocked-count"></span></span>
      <div class="controls">
        <input class="search-input" id="blocked-search" placeholder="Search..." oninput="filterBlocked()">
      </div>
    </div>
    <div id="blocked-table"><div class="loading">Loading...</div></div>
  </div>

  <div class="section">
    <div class="section-header">
      <span class="section-title">Released tickets — QA summaries <span class="count-badge" id="released-count"></span></span>
      <div class="controls">
        <input class="search-input" id="released-search" placeholder="Search..." oninput="filterReleased()">
      </div>
    </div>
    <div id="released-table"><div class="loading">Loading...</div></div>
  </div>

</div>
<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.1/chart.umd.js"></script>
<script>
let riskChartInst, releaseChartInst;
let allHistory = [], allBlocked = [], allReleased = [];

function fmt(ts) {
  if (!ts) return '-';
  const d = new Date(ts);
  return d.toLocaleDateString('en-GB', {day:'2-digit',month:'short',year:'numeric'}) + ' ' +
         d.toLocaleTimeString('en-GB', {hour:'2-digit',minute:'2-digit'});
}

function verdictClass(s) {
  if (!s) return '';
  const u = s.toUpperCase();
  if (u.startsWith('APPROVED WITH RISK')) return 'risk';
  if (u.startsWith('APPROVED')) return 'approved';
  return 'missing';
}

function verdictLine(s) {
  return s ? s.split('\\n')[0] : '-';
}

async function loadSummary() {
  const d = await fetch('/qa/api/v1/intelligence/summary').then(r=>r.json());
  document.getElementById('metrics').innerHTML = `
    <div class="metric-card"><div class="metric-label">Total analyses</div><div class="metric-value blue">${d.totalAnalyses}</div></div>
    <div class="metric-card"><div class="metric-label">Avg risk score</div><div class="metric-value amber">${Math.round(d.averageRiskScore)}</div></div>
    <div class="metric-card"><div class="metric-label">Blocked releases</div><div class="metric-value red">${d.blockedCount}</div></div>
    <div class="metric-card"><div class="metric-label">Released tickets</div><div class="metric-value green">${d.releasedCount||0}</div></div>
  `;
  const rd = d.riskDistribution||{};
  if (riskChartInst) riskChartInst.destroy();
  riskChartInst = new Chart(document.getElementById('riskChart'), {
    type:'doughnut', data:{ labels:['High','Medium','Low'], datasets:[{ data:[rd.HIGH||0,rd.MEDIUM||0,rd.LOW||0], backgroundColor:['#dc2626','#d97706','#16a34a'], borderWidth:0 }] },
    options:{ responsive:true, maintainAspectRatio:false, plugins:{ legend:{ position:'bottom', labels:{ font:{size:12}, boxWidth:12 } } } }
  });
  const rl = d.releaseDistribution||{};
  if (releaseChartInst) releaseChartInst.destroy();
  releaseChartInst = new Chart(document.getElementById('releaseChart'), {
    type:'doughnut', data:{ labels:['Block','Caution','Go'], datasets:[{ data:[rl.Block||0,rl.Caution||0,rl.Go||0], backgroundColor:['#dc2626','#d97706','#16a34a'], borderWidth:0 }] },
    options:{ responsive:true, maintainAspectRatio:false, plugins:{ legend:{ position:'bottom', labels:{ font:{size:12}, boxWidth:12 } } } }
  });
}

function renderHistory(data) {
  document.getElementById('history-count').textContent = data.length + ' records';
  if (!data.length) { document.getElementById('history-table').innerHTML='<div class="no-results">No results found</div>'; return; }
  const rows = data.map(r=>`
    <tr>
      <td><strong>${r.issueKey}</strong></td>
      <td style="max-width:280px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${r.featureSummary||'-'}</td>
      <td><span class="badge-${r.riskLevel}">${r.riskLevel||'-'}</span></td>
      <td>${r.riskScore??'-'}</td>
      <td><span class="badge-${r.releaseRecommendation}">${r.releaseRecommendation||'-'}</span></td>
      <td>${r.testCaseCount??0} cases</td>
      <td class="ts">${fmt(r.analyzedAt)}</td>
    </tr>`).join('');
  document.getElementById('history-table').innerHTML=`
    <table><thead><tr><th>Issue</th><th>Feature</th><th>Risk</th><th>Score</th><th>Release</th><th>Tests</th><th>Analyzed</th></tr></thead><tbody>${rows}</tbody></table>`;
}

function filterHistory() {
  const search = document.getElementById('history-search').value.toLowerCase();
  const risk = document.getElementById('history-risk').value;
  const release = document.getElementById('history-release').value;
  let filtered = allHistory;
  if (search) filtered = filtered.filter(r => (r.issueKey||'').toLowerCase().includes(search) || (r.featureSummary||'').toLowerCase().includes(search));
  if (risk) filtered = filtered.filter(r => r.riskLevel === risk);
  if (release) filtered = filtered.filter(r => r.releaseRecommendation === release);
  renderHistory(filtered);
}

function renderBlocked(data) {
  document.getElementById('blocked-count').textContent = data.length + ' tickets';
  if (!data.length) { document.getElementById('blocked-table').innerHTML='<div class="no-results">No blocked tickets</div>'; return; }
  const rows = data.map(r=>`
    <tr>
      <td><strong>${r.issueKey}</strong></td>
      <td>${r.featureSummary||'-'}</td>
      <td>${r.riskScore??'-'}</td>
      <td>${r.automationRecommendation||'-'}</td>
      <td class="ts">${fmt(r.analyzedAt)}</td>
    </tr>`).join('');
  document.getElementById('blocked-table').innerHTML=`
    <table><thead><tr><th>Issue</th><th>Feature</th><th>Score</th><th>Automation</th><th>Analyzed</th></tr></thead><tbody>${rows}</tbody></table>`;
}

function filterBlocked() {
  const search = document.getElementById('blocked-search').value.toLowerCase();
  let filtered = allBlocked;
  if (search) filtered = filtered.filter(r => (r.issueKey||'').toLowerCase().includes(search) || (r.featureSummary||'').toLowerCase().includes(search));
  renderBlocked(filtered);
}

function renderReleased(data) {
  document.getElementById('released-count').textContent = data.length + ' tickets';
  if (!data.length) { document.getElementById('released-table').innerHTML='<div class="no-results">No released tickets yet</div>'; return; }
  const rows = data.map(r=>`
    <tr>
      <td><strong>${r.issueKey}</strong></td>
      <td style="max-width:220px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${r.featureSummary||'-'}</td>
      <td><span class="badge-${r.riskLevel}">${r.riskLevel||'-'}</span></td>
      <td><div class="verdict ${verdictClass(r.releaseSummary)}">${verdictLine(r.releaseSummary)}</div></td>
      <td class="ts">${fmt(r.completedAt)}</td>
    </tr>`).join('');
  document.getElementById('released-table').innerHTML=`
    <table><thead><tr><th>Issue</th><th>Feature</th><th>Risk</th><th>QA Verdict</th><th>Released</th></tr></thead><tbody>${rows}</tbody></table>`;
}

function filterReleased() {
  const search = document.getElementById('released-search').value.toLowerCase();
  let filtered = allReleased;
  if (search) filtered = filtered.filter(r => (r.issueKey||'').toLowerCase().includes(search) || (r.featureSummary||'').toLowerCase().includes(search));
  renderReleased(filtered);
}

async function loadAll() {
  try {
    await loadSummary();
    allHistory = await fetch('/qa/api/v1/history').then(r=>r.json());
    renderHistory(allHistory);
    allBlocked = await fetch('/qa/api/v1/intelligence/blocked').then(r=>r.json());
    renderBlocked(allBlocked);
    allReleased = await fetch('/qa/api/v1/intelligence/released').then(r=>r.json());
    renderReleased(allReleased);
  } catch(e) { console.error(e); }
}

loadAll();
</script>
</body>
</html>
""";
    }
}