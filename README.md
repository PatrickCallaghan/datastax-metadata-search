MetaData Search 
========================
This demo shows how to use a discovery table to lookup keys for a time series instrument. In this example we will be using data from the AMEX and NASDAQ stock exchanges. We will have a metadata table which will we make searchable and a time series table to hold historic data from the financial instruments. 

This requires DataStax Enterprise running in Solr mode - 

    http://docs.datastax.com/en/datastax_enterprise/4.8/datastax_enterprise/startStop/refDseStartStopDse.html

Clone this github project and run the following commands from the root directory.

To create the schema, run the following

    mvn clean compile exec:java -Dexec.mainClass="com.datastax.demo.SchemaSetup" -DcontactPoints=localhost
	
To create some metedata and time series data, run the following 

    mvn clean compile exec:java -Dexec.mainClass="com.datastax.refdata.Main"  -DcontactPoints=localhost

To create the solr core, run 

    bin/dsetool create_core datastax.metadata generateResources=true reindex=true

This will create an index on all the columns by default. 

In cqlsh we can now search for our data by any field, for example we will search by some attributes

    select hierarchy, attributes_, variant from datastax.metadata where solr_query = '{"q":"attributes_Exchange:NASDAQ", "fq":"attributes_Ticker:APKT"}';
    
From the results of this query, we can then generate the time series id

```
 hierarchy                         | attributes_                                                    | variant
-----------------------------------+----------------------------------------------------------------+--------------------------------------------------------
 {'Exchange', 'Ticker', 'variant'} | {'attributes_Exchange': 'NASDAQ', 'attributes_Ticker': 'APKT'} | {'adjclose', 'close', 'high', 'low', 'open', 'volume'}
```

From the hierarchy, we know that to make the key, we need the Exchange(NASDAQ) and the Ticker (APKT) and a variant (eg close). So the time series key will be 'NASDAQ-APKT-close'

    select * from datastax.historic_data where key = 'NASDAQ-APKT-close' limit 50;
    
Alternatively, when the metadata is created for the first time, we can create a unique key that will always be the key (along with a variant) for the time series data. Eg. 

    select ts_id from datastax.metadata where solr_query = '{"q":"attributes_Exchange:NASDAQ", "fq":"attributes_Ticker:APKT"}';
    
To remove the tables and the schema, run the following.

    mvn clean compile exec:java -Dexec.mainClass="com.datastax.demo.SchemaTeardown"
