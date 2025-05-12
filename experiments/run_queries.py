import csv
import datetime
import os
import random
import timeit

import os.path as path

from tqdm import tqdm

from grew import Grew
from neo4jud import Neo4j

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


logfile = None

def log(msg=''):
	global logfile
	timestamp = datetime.datetime.now().strftime("%Y-%m-%d-%H:%M:%S")
	logfile.write(f'[{timestamp}] {msg}\n')
	logfile.flush()

def load_queries(base):
	query_files = [f for f in os.listdir(base) if path.isfile(path.join(base, f))]
	query_files = sorted(query_files)
	random.shuffle(query_files)

	def load_file(file):
		with open(path.join(base, file), 'r') as inp:
			return '\n'.join(inp.readlines())
	return [(path.basename(f), load_file(f)) for f in query_files]

def iterate_queries(queries, iterations, description, query_callback, iteration_callback):
	with tqdm(desc=description, total = len(queries) * iterations, position=1, leave=False) as pbar:
		for iteration in range(iterations):
			for index, (name, query) in enumerate(queries):
				query_callback(name, query)
				pbar.update(1)
			iteration_callback()


def measure_runtime(executor, queries, warmup, repetitions, output):
	recorded_result_counts = {}
	def warmup_query(name, query):
		try:
			count = executor(query)
		except:
			count = 0
		recorded_result_counts[name] = count

	def finish_round():
		pass

	iterate_queries(queries, warmup, f'Warmup', warmup_query, finish_round)
	ordered_stats = sorted(recorded_result_counts.items(), key=lambda entry: -entry[1])
	for query, count in ordered_stats:
		log(f'Query {query} returned {count}')

	with open(output, 'w', newline='') as csvfile:
		writer = csv.writer(csvfile)
		writer.writerow([q for q, _ in queries])

		timings = []

		def time_query(name, query):
			try:
				before = timeit.default_timer()
				executor(query)
				after = timeit.default_timer()
				timings.append(after - before)
			except Exception as e:
				log(f'{name} caused {e}')
				timings.append('ERROR')

		def write_row():
			writer.writerow(timings)
			timings.clear()

		iterate_queries(queries, repetitions, f'Measuring', time_query, write_row)


WARMUP_RUNS = 10
MEASUREMENT_RUNS = 100

def main():
	random.seed(59287)

	timestamp = datetime.datetime.now().strftime("%Y-%m-%d-%H%M%S")
	result_dir = f'results/{timestamp}'
	os.makedirs(result_dir)

	global logfile
	logfile = open(f'{result_dir}/query.log', 'w')

	grew_data_dir = path.abspath('grew-data')

	total_score = len(CORPORA) * 3
	with tqdm(desc='Overall progress', total = total_score, position=0) as pbar:
		for corpus in CORPORA:
			for use_nodes in [True, False]:
				tag = 'node' if use_nodes else 'property'

				output_file = f'{result_dir}/{corpus}-{tag}.csv'
				query_dir = f'queries/{corpus}/{tag}/'
				data_dir = f'data/{corpus}/{tag}'

				queries = load_queries(query_dir)
				log(f'Testing Neo4j on {corpus} ({tag})')
				with Neo4j(path.abspath(data_dir), path.abspath('logs')) as neo4j:
					measure_runtime(lambda q: neo4j.query(q), queries, WARMUP_RUNS, MEASUREMENT_RUNS, output=output_file)

				pbar.update(1)

			output_file = f'{result_dir}/{corpus}-grew.csv'
			query_dir = f'queries/{corpus}/grew/'

			log(f'Testing grew on {corpus}')
			queries = load_queries(query_dir)
			with Grew(grew_data_dir, corpus) as grew:
				measure_runtime(lambda q: grew.query(q), queries, WARMUP_RUNS, MEASUREMENT_RUNS, output=output_file)

			pbar.update(1)
	logfile.close()



if __name__ == '__main__':
	main()

