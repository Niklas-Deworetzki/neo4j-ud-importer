import csv
import os.path as path
import statistics
import sys

CORPORA = [
	'UD_Chinese-GSDSimp',
	'UD_French-GSD',
	'UD_Hindi-HDTB',
	'UD_Spanish-AnCora',
	'UD_Coptic-Scriptorium',
	'UD_German-HDT',
	'UD_Swedish-Talbanken',
	'UD_English-EWT',
	'UD_Hebrew-HTB',
	'UD_Portuguese-CINTIL'
]

VARIANTS = ['grew', 'property', 'node']

QUERIES = ['question', 'conditional', 'existential', 'npn']

RESULT_DIR = '2025-04-16-101145'
OUTPUT_FILE = 'accumulated_data.csv'

def load_data(corpus, system):
	data = { q: [] for q in QUERIES }

	filepath = f'results/{RESULT_DIR}/{corpus}-{system}.csv'
	with open(filepath, newline='') as csvfile:
		reader = csv.DictReader(csvfile)
		for row in reader:
			for q in QUERIES:
				data[q].append(float(row[q]))
	return data

if len(sys.argv) > 1:
	RESULT_DIR = sys.argv[1]
if len(sys.argv) > 2:
	OUTPUT_FILE = sys.argv[2]

with open(OUTPUT_FILE, 'w', newline='') as csvfile:
	writer = csv.writer(csvfile, delimiter='\t')
	writer.writerow(['query'] + VARIANTS)

	for corpus in CORPORA:
		try:
			data = {s: load_data(corpus, s) for s in VARIANTS}
		except:
			continue

		for query in QUERIES:
			try:
				row = []
				row.append(f'{query} ({corpus})')
				for variant in VARIANTS:
					measurement = statistics.median(data[variant][query])
					row.append(measurement)

				writer.writerow(row)
			except Exception as e:
				print(e)
