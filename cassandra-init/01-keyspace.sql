CREATE KEYSPACE IF NOT EXISTS orders
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
