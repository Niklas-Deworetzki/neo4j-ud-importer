from typing import Optional

from dockerized import Container
from neo4j import GraphDatabase

import os
import os.path as path
import requests
import time

class Neo4j:
	DEFAULT_PORT = 7687

	def __init__(self, data_dir: str, log_dir: str, port: int = DEFAULT_PORT):
		self.data_dir = data_dir
		self.log_dir = log_dir
		self.db_url = f'neo4j://localhost:{port}'

	def __enter__(self):
		mounts = {
			self.log_dir: '/logs',
			self.data_dir: '/data'
		}
		environment = {
			'NEO4J_AUTH': 'none',
			'NEO4J_PLUGINS': '["apoc"]',
			'NEO4J_dbms_security_procedures_unrestricted': 'apoc.\\*',
			'NEO4J_db_tx__log_rotation_retention__policy': 'keep_none'
		}
		self.container = Container(
			'neo4j:latest',
			mounts=mounts,
			env=environment,
			ports=[7474, 7687])
		self.container.start()
		time.sleep(15)
		return self

	def __exit__(self, exception_type, exception_value, exception_traceback):
		self.container.stop()

	def query(self, query: str) -> int:
		with GraphDatabase.driver(self.db_url) as driver:
			records, _, _ = driver.execute_query(query)
			count = 0
			for _ in records:
				count += 1
			return count
