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
  .section { background: #fff; border-radius: 10px; border: 1px solid #e8e8e8; margin-bottom: 1.5rem; overflow: hidden; }
  .section-header { padding: 1rem 1.5rem; border-bottom: 1px solid #f0f0f0; display: flex; align-items: center; justify-content: space-between; }
  .section-title { font-size: 14px; font-weight: 500; }
  table { width: 100%; border-collapse: collapse; font-size: 13px; }
  th { text-align: left; padding: 10px 1.5rem; font-size: 11px; color: #888; font-weight: 500; text-transform: uppercase; letter-spacing: 0.05em; background: #fafafa; border-bottom: 1px solid #f0f0f0; }
  td { padding: 12px 1.5rem; border-bottom: 1px solid #f8f8f8; }
  tr:last-child td { border-bottom: none; }
  tr:hover td { background: #fafafa; }
  .badge-HIGH { display:inline-block; padding:2px 8px; border-radius:4px; font-size:11px; font-weight:500; background:#fee2e2; color:#991b1b; }
  .badge-MEDIUM { display:inline-block; padding:2px 8px; border-radius:4px; font-size:11px; font-weight:500; background:#fef3c7; color:#92400e; }
  .badge-LOW { display:inline-block; padding:2px 8px; border-radius:4px; font-size:11px; font-weight:500; background:#dcfce7; color:#166534; }
  .badge-Block { display:inline-block; padding:2px 8px; border-radius:4px; font-size:11px; font-weight:500; background:#fee2e2; color:#991b1b; }
  .badge-Caution { display:inline-block; padding:2px 8px; border-radius:4px; font-size:11px; font-weight:500; background:#fef3c7; color:#92400e; }
  .badge-Go { display:inline-block; padding:2px 8px; border-radius:4px; font-size:11px; font-weight:500; background:#dcfce7; color:#166534; }
  .chart-wrap { padding: 1.5rem; }
  .loading { text-align: center; padding: 3rem; color: #888; font-size: 14px; }
  .refresh-btn { background: none; border: 1px solid #ddd; border-radius: 6px; padding: 4px 12px; font-size: 12px; cursor: pointer; color: #555; }
  .refresh-btn:hover { background: #f5f5f5; }
  .ts { font-size: 11px; color: #aaa; }
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
      <span class="section-title">Recent analyses</span>
      <button class="refresh-btn" onclick="loadAll()">Refresh</button>
    </div>
    <div id="history-table"><div class="loading">Loading...</div></div>
  </div>
  <div class="section">
    <div class="section-header"><span class="section-title">Blocked tickets</span></div>
    <div id="blocked-table"><div class="loading">Loading...</div></div>
  </div>
</div>
<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.1/chart.umd.js"></script>
<script>
let riskChartInst, releaseChartInst;

function fmt(ts) {
  if (!ts) return '-';
  const d = new Date(ts);
  return d.toLocaleDateString('en-GB', {day:'2-digit',month:'short',year:'numeric'}) + ' ' +
         d.toLocaleTimeString('en-GB', {hour:'2-digit',minute:'2-digit'});
}

async function loadSummary() {
  const res = await fetch('/qa/api/v1/intelligence/summary');
  const d = await res.json();

  document.getElementById('metrics').innerHTML = `
    <div class="metric-card">
      <div class="metric-label">Total analyses</div>
      <div class="metric-value blue">${d.totalAnalyses}</div>
    </div>
    <div class="metric-card">
      <div class="metric-label">Avg risk score</div>
      <div class="metric-value amber">${Math.round(d.averageRiskScore)}</div>
    </div>
    <div class="metric-card">
      <div class="metric-label">High risk tickets</div>
      <div class="metric-value red">${d.highRiskCount}</div>
    </div>
    <div class="metric-card">
      <div class="metric-label">Blocked releases</div>
      <div class="metric-value red">${d.blockedCount}</div>
    </div>
  `;

  const rd = d.riskDistribution || {};
  if (riskChartInst) riskChartInst.destroy();
  riskChartInst = new Chart(document.getElementById('riskChart'), {
    type: 'doughnut',
    data: {
      labels: ['High', 'Medium', 'Low'],
      datasets: [{ data: [rd.HIGH||0, rd.MEDIUM||0, rd.LOW||0], backgroundColor: ['#dc2626','#d97706','#16a34a'], borderWidth: 0 }]
    },
    options: { responsive:true, maintainAspectRatio:false, plugins:{ legend:{ position:'bottom', labels:{ font:{size:12}, boxWidth:12 } } } }
  });

  const rl = d.releaseDistribution || {};
  if (releaseChartInst) releaseChartInst.destroy();
  releaseChartInst = new Chart(document.getElementById('releaseChart'), {
    type: 'doughnut',
    data: {
      labels: ['Block','Caution','Go'],
      datasets: [{ data: [rl.Block||0, rl.Caution||0, rl.Go||0], backgroundColor: ['#dc2626','#d97706','#16a34a'], borderWidth: 0 }]
    },
    options: { responsive:true, maintainAspectRatio:false, plugins:{ legend:{ position:'bottom', labels:{ font:{size:12}, boxWidth:12 } } } }
  });
}

async function loadHistory() {
  const res = await fetch('/qa/api/v1/history');
  const data = await res.json();
  if (!data.length) { document.getElementById('history-table').innerHTML='<div class="loading">No data yet</div>'; return; }
  const rows = data.map(r => `
    <tr>
      <td><strong>${r.issueKey}</strong></td>
      <td style="max-width:300px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${r.featureSummary||'-'}</td>
      <td><span class="badge-${r.riskLevel}">${r.riskLevel||'-'}</span></td>
      <td>${r.riskScore??'-'}</td>
      <td><span class="badge-${r.releaseRecommendation}">${r.releaseRecommendation||'-'}</span></td>
      <td>${r.testCaseCount??0} cases</td>
      <td class="ts">${fmt(r.analyzedAt)}</td>
    </tr>`).join('');
  document.getElementById('history-table').innerHTML = `
    <table>
      <thead><tr><th>Issue</th><th>Feature</th><th>Risk</th><th>Score</th><th>Release</th><th>Tests</th><th>Analyzed</th></tr></thead>
      <tbody>${rows}</tbody>
    </table>`;
}

async function loadBlocked() {
  const res = await fetch('/qa/api/v1/intelligence/blocked');
  const data = await res.json();
  if (!data.length) { document.getElementById('blocked-table').innerHTML='<div class="loading">No blocked tickets</div>'; return; }
  const rows = data.map(r => `
    <tr>
      <td><strong>${r.issueKey}</strong></td>
      <td>${r.featureSummary||'-'}</td>
      <td>${r.riskScore??'-'}</td>
      <td>${r.automationRecommendation||'-'}</td>
      <td class="ts">${fmt(r.analyzedAt)}</td>
    </tr>`).join('');
  document.getElementById('blocked-table').innerHTML = `
    <table>
      <thead><tr><th>Issue</th><th>Feature</th><th>Score</th><th>Automation</th><th>Analyzed</th></tr></thead>
      <tbody>${rows}</tbody>
    </table>`;
}

async function loadAll() {
  try { await Promise.all([loadSummary(), loadHistory(), loadBlocked()]); }
  catch(e) { console.error(e); }
}

loadAll();
</script>
</body>
</html>
""";
    }
}