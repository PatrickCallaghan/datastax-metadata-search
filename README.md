MetaData Search 
========================
This requires DataStax Enterprise running in Solr mode.

To create the schema, run the following

	mvn clean compile exec:java -Dexec.mainClass="com.datastax.demo.SchemaSetup" -DcontactPoints=localhost
	
To create some transactions, run the following 
	
	mvn clean compile exec:java -Dexec.mainClass="com.datastax.refdata.Main"  -DcontactPoints=localhost


To create the solr core, run 

	bin/dsetool create_core datastax.metadata generateResources=true reindex=true


To remove the tables and the schema, run the following.

    mvn clean compile exec:java -Dexec.mainClass="com.datastax.demo.SchemaTeardown"
