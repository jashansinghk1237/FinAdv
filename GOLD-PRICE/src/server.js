import express from 'express';
import cors from 'cors';
import path from 'path';
import { fileURLToPath } from 'url';
import dayjs from 'dayjs';
import { fetchAllMetals } from './fetcher.js';
import { readHistory, writeEntry, getYesterday, getLatest, trimHistory } from './storage.js';

const app = express();
app.use(cors());
app.use(express.json());

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Serve frontend
app.use(express.static(path.resolve(__dirname, '../public')));

app.get('/api/prices', (req, res) => {
	res.json({ history: readHistory() });
});

app.get('/api/prices/latest', (req, res) => {
	res.json({ latest: getLatest() });
});

app.get('/api/prices/yesterday', (req, res) => {
	res.json({ yesterday: getYesterday() });
});

app.post('/api/fetch', async (req, res) => {
	try {
		const data = await fetchAllMetals();
		writeEntry(data);
		trimHistory(730); // keep up to 2 years
		res.json({ ok: true, savedAt: dayjs().toISOString(), data });
	} catch (error) {
		res.status(500).json({ ok: false, error: String(error) });
	}
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
	console.log(`Server listening on http://localhost:${PORT}`);
});


