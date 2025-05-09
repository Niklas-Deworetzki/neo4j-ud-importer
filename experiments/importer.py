import csv
import datetime
import glob
import os
import os.path as path
import sys
import re
import subprocess
import time
import timeit
import traceback
import xml.etree.ElementTree as ET

from corpora import on_each_treebank
from neo4jud import Neo4j
from neo4j import GraphDatabase

NODE_QUERY = 'MATCH (n) RETURN COUNT(n)'
EDGE_QUERY = 'MATCH ()-[r]-() RETURN COUNT (r)'

start_time = timeit.default_timer()
def log(msg):
	seconds_passed = int(timeit.default_timer() - start_time)
	print(datetime.timedelta(seconds=seconds_passed), msg)


UD_REGEX = r'^UD_([^-]+)-(.*)$'

IMPORTER_JAR = path.abspath('neo4jud.jar')
DATA_DIR = path.abspath('data')
LOGS_DIR = path.abspath('logs')
os.makedirs(DATA_DIR, exist_ok=True)
os.makedirs(LOGS_DIR, exist_ok=True)


def get_size(p):
    total_size = 0
    for dirpath, dirnames, filenames in os.walk(p):
        for f in filenames:
            fp = path.join(dirpath, f)
            if not path.islink(fp):
                total_size += path.getsize(fp)
    return total_size

def get_query_result(instance, query):
	with GraphDatabase.driver(instance.db_url) as driver:
		records, _, keys = driver.execute_query(query)
		return records[0][keys[0]]

def encode_corpus(treebank_root, encode_as_properties):
	treebank_name = path.basename(treebank_root)

	# Make sure the files have been loaded by the OS once.
	log(f'[{treebank_name}] Loading files')
	subprocess.run(['wc', '-l'] + glob.glob(f'{treebank_root}/*.conllu'), check=True)

	# Make directory for corpus data.
	tag = 'property' if encode_as_properties else 'node'
	neo4j_root = f'{DATA_DIR}/{treebank_name}/{tag}'
	os.makedirs(neo4j_root)

	log(f'[{treebank_name}] Starting {tag} container')
	with Neo4j(neo4j_root, LOGS_DIR) as container:
		time.sleep(15)

		log(f'[{treebank_name}] Importing files...')
		import_option = '--as-properties' if encode_as_properties else '--as-nodes'

		before = timeit.default_timer()
		subprocess.run(['java', '-jar', IMPORTER_JAR, import_option, treebank_root], check=True)
		after = timeit.default_timer()

		nr_nodes = get_query_result(container, NODE_QUERY)
		nr_edges = get_query_result(container, EDGE_QUERY)

		database_data_dir = f'{neo4j_root}/databases'
	filesize = get_size(database_data_dir)

	return [after - before, nr_nodes, nr_edges, filesize]

def iterate_corpora(dir):
	for filename in os.listdir(dir):
		treebank_path = path.join(dir, filename)
		if not path.isdir(treebank_path):
			continue

		if match := re.search(UD_REGEX, filename):
			language = match.group(1)
			name = match.group(2)

			yield treebank_path, name, language

def import_corpora_from(dir, data_file):
	with open(data_file, 'w', newline='') as csvfile:
		writer = csv.writer(csvfile, delimiter='\t')
		header = ['corpus', 'language', '# tokens']
		for tag in ['property', 'node']:
			for column in ['time', 'nodes', 'relationships', 'size on disk']:
				header.append(f'{column} ({tag})')

		writer.writerow(header)
		csvfile.flush()

		for treebank_dir, name, language in iterate_corpora(dir):
			try:
				try:
					tree = ET.parse(path.join(treebank_dir, 'stats.xml'))
					nr_tokens = int(tree.getroot().find('./size/total/tokens').text)
				except Exception as e:
					nr_tokens = 'unknown'

				data_row = [name, language, nr_tokens]
				data_row += encode_corpus(treebank_dir, True)
				data_row += encode_corpus(treebank_dir, False)
				writer.writerow(data_row)
				csvfile.flush()
			except Exception as e:			
				traceback.print_exc()
				log(f'[{treebank_dir}] FAILED. Skipping.')

if __name__ == '__main__':
	if len(sys.argv) != 2:
		print(f'{sys.argv[0]} CORPORA')
	else:
		timestamp = datetime.datetime.now().strftime("%Y-%m-%d-%H%M%S")
		import_corpora_from(sys.argv[1], f'encoding-{timestamp}.csv')
