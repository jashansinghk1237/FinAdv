import { fetchAllMetals } from '../src/fetcher.js';
import { writeEntry, trimHistory } from '../src/storage.js';

const run = async () => {
	const data = await fetchAllMetals();
	writeEntry(data);
	trimHistory(730);
	console.log('Saved snapshot at', data.timestamp);
};

run().catch((e) => {
	console.error(e);
	process.exitCode = 1;
});


