import csv
import os

from corpora import on_each_treebank
from neo4jud import Neo4j
from neo4j import GraphDatabase

log_dir = os.path.abspath('logs')
os.makedirs(log_dir, exist_ok=True)

NODE_QUERY = 'MATCH (n) RETURN COUNT(n)'
EDGE_QUERY = 'MATCH ()-[r]-() RETURN COUNT (r)'
OUTPUT_FILE = 'size_info.csv'

def main():
	with open(OUTPUT_FILE, 'w', newline='') as csvfile:
		writer = csv.writer(csvfile, delimiter='\t')

		analysed_properties = ['nodes', 'relationships', 'size on disk']
		analysed_properties = ['size on disk']

		header = ['language', 'name']
		header += [f'{column} (property)' for column in analysed_properties]
		header += [f'{column} (node)' for column in analysed_properties]
		writer.writerow(header)


		def analyse_db(treebank_dir, language, name):
			row = [language, name]

			for variant in ['property', 'node']:
				data_dir = os.path.abspath(f'{treebank_dir}/{variant}')
				with Neo4j(data_dir, log_dir) as instance:
					nr_nodes = get_query_result(instance, NODE_QUERY)
					nr_edges = get_query_result(instance, EDGE_QUERY)

					row += [nr_nodes, nr_edges]

				database_dir = f'{data_dir}/databases'
				row.append(get_size(database_dir))
			writer.writerow(row)

		on_each_treebank(analyse_db)

def get_size(path):
    total_size = 0
    for dirpath, dirnames, filenames in os.walk(path):
        for f in filenames:
            fp = os.path.join(dirpath, f)
            if not os.path.islink(fp):
                total_size += os.path.getsize(fp)
    return total_size
	
def get_query_result(instance, query):
	with GraphDatabase.driver(instance.db_url) as driver:
		records, _, keys = driver.execute_query(query)
		return records[0][keys[0]]

if __name__ == '__main__':
	main()
