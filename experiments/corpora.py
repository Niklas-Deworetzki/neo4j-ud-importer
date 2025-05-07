import os
import os.path as path
import re

from tqdm import tqdm
from neo4jud import Neo4j

UD_REGEX = r'^UD_([^-]+)-(.*)$'

def list_dir(dir):
	return [f'{dir}/{d}' for d in os.listdir(dir) if path.isdir(f'{dir}/{d}')]

def on_each_treebank(action):
	treebank_dirs = list_dir('data')
	print(f'Found {len(treebank_dirs)} treebanks')
	for treebank_dir in tqdm(sorted(treebank_dirs)):
		if match := re.search(UD_REGEX, path.basename(treebank_dir)):
			language = match.group(1)
			name = match.group(2)
			action(treebank_dir, language, name)
