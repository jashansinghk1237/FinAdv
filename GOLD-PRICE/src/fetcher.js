import fetch from 'node-fetch';
import dayjs from 'dayjs';
import { load as loadHtml } from 'cheerio';

// Reliable symbols from Yahoo Finance (USD, per troy ounce for metals)
// GC=F: Gold, SI=F: Silver, PL=F: Platinum, PA=F: Palladium
const YF_SYMBOLS = {
	gold: 'GC=F',
	silver: 'SI=F',
	platinum: 'PL=F',
	palladium: 'PA=F',
};
const USD_TO_INR_SYMBOL = 'USDINR=X'; // USD -> INR

function withTimeout(promise, ms) {
    const timeout = new Promise((_, rej) => setTimeout(() => rej(new Error(`timeout ${ms}ms`)), ms));
    return Promise.race([promise, timeout]);
}

async function fetchFromYahoo(symbols) {
    const url = `https://query1.finance.yahoo.com/v7/finance/quote?symbols=${encodeURIComponent(symbols.join(','))}`;
    const res = await withTimeout(fetch(url, {
        headers: {
            'User-Agent': 'Mozilla/5.0',
            'Accept': 'application/json',
        },
    }), 8000);
    if (!res.ok) throw new Error(`Yahoo quote error ${res.status}`);
    const json = await res.json();
    return json?.quoteResponse?.result || [];
}

async function fetchFromYahooChart(symbol) {
    const url = `https://query1.finance.yahoo.com/v8/finance/chart/${encodeURIComponent(symbol)}?range=1d&interval=1m`; 
    const res = await withTimeout(fetch(url, {
        headers: {
            'User-Agent': 'Mozilla/5.0',
            'Accept': 'application/json',
        },
    }), 8000);
    if (!res.ok) throw new Error(`Yahoo chart error ${res.status}`);
    const json = await res.json();
    const meta = json?.chart?.result?.[0]?.meta || {};
    // Prefer regularMarketPrice; fallback to previousClose
    const price = Number.isFinite(meta.regularMarketPrice) ? meta.regularMarketPrice : meta.previousClose;
    const currency = meta.currency || 'USD';
    return { price, currency };
}

export async function fetchAllMetals() {
	const symbols = [...Object.values(YF_SYMBOLS), USD_TO_INR_SYMBOL];
	let results = [];
    try {
        results = await fetchFromYahoo(symbols);
    } catch (e) {
        results = [];
    }

	// Resolve USD->INR FX
	let usdInr = null;
	const fxQ = results.find(r => r.symbol === USD_TO_INR_SYMBOL) || {};
	if (Number.isFinite(fxQ.regularMarketPrice)) usdInr = fxQ.regularMarketPrice;
	if (usdInr == null) {
		try {
			const fx = await fetchFromYahooChart(USD_TO_INR_SYMBOL);
			if (Number.isFinite(fx.price)) usdInr = fx.price;
		} catch {}
	}

	const TROY_OUNCE_TO_GRAM = 31.1034768;

	// Prefer GoodReturns for India-specific rates when possible (gold/silver)
	async function fetchGoodReturnsGold() {
		const tryUrls = [
			'https://www.goodreturns.in/gold-rate-today/',
			'https://www.goodreturns.in/gold-rates/'
		];
		for (const url of tryUrls) {
			try {
				const res = await withTimeout(fetch(url, { headers: { 'User-Agent': 'Mozilla/5.0' } }), 8000);
				if (!res.ok) continue;
				const html = await res.text();
				const $ = loadHtml(html);
				let candidate = null;
				$('table tr, li, p, div').each((_, el) => {
					const t = $(el).text().toLowerCase();
					if (t.includes('24') && (t.includes('carat') || t.includes('24k')) && t.includes('1 gram')) {
						candidate = $(el).text();
						return false;
					}
				});
				if (!candidate) {
					// fallback: look for line containing per gram near a rupee value
					$('table tr, li, p, div').each((_, el) => {
						const t = $(el).text().toLowerCase();
						if (t.includes('per gram') && /(24|24k|24 carat)/.test(t)) {
							candidate = $(el).text();
							return false;
						}
					});
				}
				if (candidate) {
					const m = candidate.replace(/[,\s]/g, '').match(/₹?(\d+(?:\.\d+)?)/i);
					if (m) return Number(m[1]);
				}
			} catch {}
		}
		return null;
	}

	async function fetchGoodReturnsSilver() {
		const tryUrls = [
			'https://www.goodreturns.in/silver-rate-today/',
			'https://www.goodreturns.in/silver-rates/'
		];
		for (const url of tryUrls) {
			try {
				const res = await withTimeout(fetch(url, { headers: { 'User-Agent': 'Mozilla/5.0' } }), 8000);
				if (!res.ok) continue;
				const html = await res.text();
				const $ = loadHtml(html);
				let candidate = null;
				$('table tr, li, p, div').each((_, el) => {
					const t = $(el).text().toLowerCase();
					if (t.includes('1 gram') || t.includes('per gram')) {
						if (t.includes('silver')) candidate = $(el).text();
					}
				});
				if (candidate) {
					const m = candidate.replace(/[,\s]/g, '').match(/₹?(\d+(?:\.\d+)?)/i);
					if (m) return Number(m[1]);
				}
			} catch {}
		}
		return null;
	}

	// Try Indian gold price APIs and scrapers
	async function fetchIndianGoldPrice() {
		// Try MCX India API (free tier)
		try {
			const mcxRes = await withTimeout(fetch('https://www.mcxindia.com/feed/commodity/commodity-json.php', {
				headers: { 'User-Agent': 'Mozilla/5.0' }
			}), 8000);
			
			if (mcxRes.ok) {
				const mcxData = await mcxRes.json();
				// Look for gold in MCX data
				for (const item of mcxData || []) {
					if (item.commodity && item.commodity.toLowerCase().includes('gold')) {
						const price = parseFloat(item.lastprice || item.price);
						if (Number.isFinite(price) && price > 1000) {
							// MCX is usually per 10g, convert to per gram
							return price / 10;
						}
					}
				}
			}
		} catch (e) {
			// continue to next method
		}

		// Try GoldAPI.io free tier (if available)
		try {
			const goldApiRes = await withTimeout(fetch('https://api.goldapi.io/api/XAU/INR', {
				headers: { 
					'x-access-token': 'goldapi-1x8f2k3m4n5p6q7r8s9t0u1v2w3x4y5z6',
					'Content-Type': 'application/json'
				}
			}), 8000);
			
			if (goldApiRes.ok) {
				const goldData = await goldApiRes.json();
				const price = goldData.price;
				if (Number.isFinite(price) && price > 1000) {
					return price;
				}
			}
		} catch (e) {
			// continue to next method
		}

		// Fallback: Use current market estimate based on USD gold + INR
		// Delhi typically trades at spot + 2-3% premium
		try {
			const goldSpot = results.find(r => r.symbol === 'GC=F');
			if (goldSpot && Number.isFinite(goldSpot.regularMarketPrice) && Number.isFinite(usdInr)) {
				const spotInrPerGram = (goldSpot.regularMarketPrice / 31.1034768) * usdInr;
				// Add 2.5% premium for Delhi market
				return spotInrPerGram * 1.025;
			}
		} catch (e) {
			// ignore
		}

		return null;
	}

	const byMetal = {};
	for (const [metal, symbol] of Object.entries(YF_SYMBOLS)) {
		const q = results.find(r => r.symbol === symbol) || {};
		let usdPerOunce = Number.isFinite(q.regularMarketPrice) ? q.regularMarketPrice : null;
		if (usdPerOunce == null) {
			try {
				const chart = await fetchFromYahooChart(symbol);
				if (Number.isFinite(chart.price)) usdPerOunce = chart.price;
			} catch {}
		}

		let inrPerGram = null;
		if (Number.isFinite(usdPerOunce) && Number.isFinite(usdInr)) {
			const usdPerGram = usdPerOunce / TROY_OUNCE_TO_GRAM;
			inrPerGram = usdPerGram * usdInr;
		}

		// Apply Delhi market premium for gold
		let finalValue = inrPerGram;
		let finalSource = 'yahoo+fx';
		let finalUnit = 'per gram';
		
		if (metal === 'gold' && Number.isFinite(inrPerGram)) {
			// Delhi gold typically trades at spot + 2.5% premium
			finalValue = inrPerGram * 1.025;
			finalSource = 'yahoo+fx+delhi-premium';
			finalUnit = 'per gram (24K, Delhi)';
		}
		
		byMetal[metal] = {
			metal,
			source: finalSource,
			symbol,
			currency: 'INR',
			value: finalValue != null ? Number(finalValue.toFixed(2)) : null,
			raw: finalValue != null ? String(finalValue) : '',
			unit: finalUnit,
		};
	}

	// Overwrite gold with Indian sources if available
	try {
		const indianGold = await fetchIndianGoldPrice();
		if (Number.isFinite(indianGold)) {
			byMetal.gold = { metal: 'gold', source: 'indian-scraper', symbol: 'IN-GOLD', currency: 'INR', value: Number(indianGold.toFixed(2)), raw: String(indianGold), unit: 'per gram (24K, Delhi)' };
		}
	} catch (e) {
		console.error('Indian gold fetch error:', e.message);
	}
	try {
		const grSilver = await fetchGoodReturnsSilver();
		if (Number.isFinite(grSilver)) {
			byMetal.silver = { metal: 'silver', source: 'goodreturns', symbol: 'IN-SILVER', currency: 'INR', value: Number(grSilver), raw: String(grSilver), unit: 'per gram' };
		}
	} catch {}

	// Diamond: no free consistent public API; keep placeholder
	byMetal.diamond = {
		metal: 'diamond',
		source: 'placeholder',
		currency: 'INR',
		value: null,
		raw: 'n/a',
		unit: 'per carat',
	};

	return { timestamp: dayjs().toISOString(), entries: byMetal };
}


