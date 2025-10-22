import fs from 'fs';
import path from 'path';

const DB_PATH = path.resolve('data/prices.json');

function ensureDbFile() {
	try {
		if (!fs.existsSync(DB_PATH)) {
			fs.mkdirSync(path.dirname(DB_PATH), { recursive: true });
			fs.writeFileSync(DB_PATH, JSON.stringify({ history: [] }, null, 2));
		}
	} catch (error) {
		// ignore; will be handled on read/write
	}
}

export function readHistory() {
	ensureDbFile();
	try {
		const raw = fs.readFileSync(DB_PATH, 'utf8');
		const json = JSON.parse(raw || '{"history":[]}');
		if (!Array.isArray(json.history)) json.history = [];
		return json.history;
	} catch (e) {
		return [];
	}
}

export function writeEntry(entry) {
	const history = readHistory();
	history.push(entry);
	const db = { history };
	fs.writeFileSync(DB_PATH, JSON.stringify(db, null, 2));
	return entry;
}

export function getLatest() {
	const history = readHistory();
	return history[history.length - 1] || null;
}

export function getYesterday() {
	const history = readHistory();
	if (history.length < 2) return null;
	return history[history.length - 2];
}

export function trimHistory(maxEntries = 365) {
	const history = readHistory();
	if (history.length > maxEntries) {
		const trimmed = history.slice(history.length - maxEntries);
		fs.writeFileSync(DB_PATH, JSON.stringify({ history: trimmed }, null, 2));
	}
}


