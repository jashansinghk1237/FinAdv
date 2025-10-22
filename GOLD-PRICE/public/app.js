async function api(path, opts) {
	const res = await fetch(path, opts);
	if (!res.ok) throw new Error('API error');
	return res.json();
}

function renderPrices(latest) {
	const body = document.getElementById('prices-body');
	body.innerHTML = '';
	if (!latest) return;
	const { timestamp, entries } = latest;
	document.getElementById('timestamp').textContent = `As of ${new Date(timestamp).toLocaleString()}`;
	for (const key of Object.keys(entries)) {
		const e = entries[key] || {};
		const tr = document.createElement('tr');
		const valueCell = e.value == null ? 'n/a' : e.value;
		const unit = e.unit ? ` ${e.unit}` : '';
		tr.innerHTML = `<td>${key}</td><td>${e.currency || ''}</td><td>${valueCell}${unit}</td><td title="${(e.raw||'').replace(/"/g,'&quot;')}">${(e.raw || '').slice(0, 60)}${(e.raw||'').length>60?'…':''}</td>`;
		body.appendChild(tr);
	}
}

function renderHistory(history) {
	const ul = document.getElementById('history');
	ul.innerHTML = '';
	for (const item of [...history].slice(-7).reverse()) {
		const li = document.createElement('li');
		const metals = Object.entries(item.entries || {})
			.map(([k, v]) => `${k}: ${v.currency || ''}${v.value == null ? 'n/a' : v.value}`)
			.join(' | ');
		li.textContent = `${new Date(item.timestamp).toLocaleString()} — ${metals}`;
		ul.appendChild(li);
	}
}

function renderNotice(yesterday) {
	const el = document.getElementById('notice');
	if (!yesterday) {
		el.textContent = 'No yesterday data yet. Fetch now to build history.';
		return;
	}
	const metals = Object.entries(yesterday.entries || {})
		.map(([k, v]) => `${k}: ${v.currency || ''}${v.value == null ? 'n/a' : v.value}`)
		.join(' | ');
	el.textContent = `Yesterday — ${metals}`;
}

async function loadAll() {
	const [y, h, l] = await Promise.all([
		api('/api/prices/yesterday'),
		api('/api/prices'),
		api('/api/prices/latest')
	]);
	renderNotice(y.yesterday);
	renderHistory(h.history || []);
	renderPrices(l.latest);
}

async function fetchNow() {
	const btn = document.getElementById('refresh');
	btn.disabled = true; btn.textContent = 'Fetching…';
	try {
		await api('/api/fetch', { method: 'POST' });
		await loadAll();
	} catch (e) {
		alert('Failed to fetch latest.');
	} finally {
		btn.disabled = false; btn.textContent = 'Fetch latest now';
	}
}

document.addEventListener('DOMContentLoaded', () => {
	document.getElementById('refresh').addEventListener('click', fetchNow);
	loadAll();
});


