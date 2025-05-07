from typing import Optional

from dockerized import Container
from tempfile import TemporaryDirectory, NamedTemporaryFile
from subprocess import Popen, PIPE, DEVNULL

import json
import os
import os.path as path
import time

class Grew:

	def __init__(self, data_dir: str, corpus: str):
		self.data_dir = data_dir
		self.corpus = corpus

	def __enter__(self):
		self.query_dir = TemporaryDirectory(prefix='grew-query-')
		os.chmod(self.query_dir.name, 0o777)

		mounts = {
			path.abspath(self.data_dir): '/data',
			self.query_dir.name: '/query'
		}
		self.container = Container('grewmatch:latest', 'sleep', ['inf'], mounts)
		self.container.start()
		time.sleep(1)
		return self

	def __exit__(self, exception_type, exception_value, exception_traceback):
		self.container.stop()
		self.query_dir.cleanup()


	def _run_grew_command(self, command, query, consumer):
		with NamedTemporaryFile(dir=self.query_dir.name, mode='w') as query_file:
			os.chmod(query_file.name, 0o666)
			query_file.write(query)
			query_file.flush()

			query_file_name = path.basename(query_file.name)
			proc = self.container.exec('grew', command, '-request', f'/query/{query_file_name}', '-i', f'/data/{self.corpus}')
			return consumer(proc.stdout)

	def query(self, query: str) -> int:
		def count_json_entities(stream):
			count = 0
			for _ in json.load(stream):
				count += 1
			return count
		return self._run_grew_command('grep', query, count_json_entities)
