create keyspace if not exists datastax WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

use datastax;

drop table metadata;
CREATE TABLE IF NOT EXISTS metadata (
	id uuid, 
	updated_date timestamp,
	hierarchy set<text>,
	alias set<text>,
	attributes_ map<text,text>,
	variant set<text>,
	ratings_ map<text,text>,
	default_rating text,
	ts_id text,
	PRIMARY KEY(id, updated_date)
);

CREATE TABLE if not exists historic_data ( 
	key text,
	date timestamp,
	value double,
	PRIMARY KEY ((key), date, value)
) WITH CLUSTERING ORDER BY (date DESC);

CREATE TABLE if not exists dividends ( 
	key text,
	date timestamp,
	dividend double,
	PRIMARY KEY ((key), date, dividend)
) WITH CLUSTERING ORDER BY (date DESC);


