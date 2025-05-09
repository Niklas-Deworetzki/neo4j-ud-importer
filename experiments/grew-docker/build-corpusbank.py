import os
import json

data_directory = '/data/'
frontend_config_file = '/home/grewmatch/grew_match/instance.json'
corpus_config_file = '/home/grewmatch/corpusbank/corpora.json'

def load_json(filename):
	with open(filename) as f:
		return json.load(f)

def write_json(filename, data):
	with open(filename, 'w') as f:
		json.dump(data, f)

corpus_dirs = [f.path for f in os.scandir(data_directory) if f.is_dir()]

complete_config = []
corpus_ids = []
for corpus_dir in corpus_dirs:
	print(f'Processing {corpus_dir}/corpus.json')
	try:
		config = load_json(corpus_dir + '/corpus.json')
		config['directory'] = corpus_dir

		complete_config.append(config)
		corpus_ids.append(config['id'])
	except Exception as e:
		print('Skipped: ' + str(e))

frontend_config = load_json(frontend_config_file)
frontend_config['desc'][0]['corpora'] = corpus_ids
write_json(frontend_config_file, frontend_config)
write_json(corpus_config_file, complete_config)
