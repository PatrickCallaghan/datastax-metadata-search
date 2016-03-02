create keyspace if not exists datastax WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

use datastax;

drop table metadata;
CREATE TABLE IF NOT EXISTS metadata (
id uuid, 
updated_date text,
hierarchy set<text>,
alias set<text>,
attributes_ map<text,text>,
variant set<text>,
ratings_ map<text,text>,
default_rating text,
ts_id text,
PRIMARY KEY(id, updated_date)
);

insert into datastax.metadata (id, updated_date, hierarchy, alias, attributes_, variant, ratings_, default_rating, ts_id) values
(now(), '2016-01-01 15:00:00', {'AssetClass', 'Entity', 'RiskFactor', 'variant'},
{'Alphabet Inc', 'GOOG', 'GOOGLE'},
{'attributes_AssetClass' : 'Equity', 'attributes_Entity':'STOCK', 'attributes_RiskFactor':'GOOG.L', 'Exchange' : 'NASDAQ', 
{'bid','ask','volume'}, {'ratings_Moodys' : 'Aa', 'ratings_Fitch': 'AAA'}, 'A', 'ABC123');

insert into datastax.metadata (id, updated_date, hierarchy, alias, attributes_, variant, ratings_, default_rating, ts_id) values
(now(), '2016-01-01 15:00:00', {'AssetClass', 'Entity', 'RiskFactor', 'variant'},
{'Apple Inc', 'APPLE', 'AAPL'},
{'attributes_AssetClass' : 'Equity', 'attributes_Entity':'STOCK', 'attributes_RiskFactor':'AAPL.L', 'Exchange' : 'NASDAQ'}, 
{'bid','ask','volume'}, {'ratings_Moodys' : 'Aaa', 'ratings_Fitch': 'AAA'}, 'A', 'ABC1234');


select * from metadata where solr_query = '{"q":"attributes_AssetClass:Equity"}';



