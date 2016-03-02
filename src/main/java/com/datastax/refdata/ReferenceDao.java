package com.datastax.refdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.datastax.refdata.model.Dividend;
import com.datastax.refdata.model.HistoricData;

public class ReferenceDao {
	
	private AtomicLong TOTAL_POINTS = new AtomicLong(0);
	private Session session;
	private static String keyspaceName = "datastax";
	private static String tableNameHistoric = keyspaceName + ".historic_data";
	private static String tableNameDividends = keyspaceName + ".dividends";

	private static final String INSERT_INTO_HISTORIC = "Insert into " + tableNameHistoric
			+ " (key,date,value) values (?,?,?);";
	private static final String INSERT_INTO_DIVIDENDS = "Insert into " + tableNameDividends
			+ " (key,date,dividend) values (?,?,?);";
	private static final String INSERT_INTO_METADATA = "insert into datastax.metadata (id, updated_date, hierarchy, alias, "
			+ "attributes_, variant, ratings_, default_rating, ts_id) values (?,?,?,?,?,?,?,?,?);";
	
	
	private static final String SELECT_ALL = "select * from " + tableNameHistoric + " where date > '2009-05-01' and date < '2009-06-01' allow filtering";
	private static final String SELECT_ALL_BY_KEY = "select * from " + tableNameHistoric + " where key=? and date > '2009-05-01' and date < '2009-06-01'";

	private PreparedStatement insertStmtHistoric;
	private PreparedStatement insertStmtDividend;
	private PreparedStatement insertStmtMetaData;
	private PreparedStatement selectStmtByKey;
	private Set<String> hierarchy = new HashSet<String>(Arrays.asList("Exchange", "Ticker", "variant"));
	private Set<String> variant = new HashSet<String>(Arrays.asList("low","high","open", "close", "volume","adjclose"));

	private AtomicInteger requestCount = new AtomicInteger(0);

	public ReferenceDao(String[] contactPoints) {

		Cluster cluster = Cluster.builder().addContactPoints(contactPoints).build();
		this.session = cluster.connect();

		this.insertStmtHistoric = session.prepare(INSERT_INTO_HISTORIC);
		this.insertStmtDividend = session.prepare(INSERT_INTO_DIVIDENDS);
		this.insertStmtMetaData = session.prepare(INSERT_INTO_METADATA);
		this.selectStmtByKey = session.prepare(SELECT_ALL_BY_KEY);
		
		this.insertStmtHistoric.setConsistencyLevel(ConsistencyLevel.ONE);
		this.insertStmtDividend.setConsistencyLevel(ConsistencyLevel.ONE);
		this.insertStmtMetaData.setConsistencyLevel(ConsistencyLevel.ONE);
	}

	public void insertHistoricData(List<HistoricData> list) throws Exception{
		BoundStatement boundStmt = new BoundStatement(this.insertStmtHistoric);
		List<ResultSetFuture> results = new ArrayList<ResultSetFuture>();

		Date mostRecentDate = new Date(0);		
		HistoricData mostRecent = null;
		
		for (HistoricData historicData : list) {

			Date insertDate = historicData.getDate();
			boundStmt.setString("key", historicData.getKey()+ "-open");
			boundStmt.setDate("date", insertDate);
			boundStmt.setDouble("value", historicData.getOpen());
			results.add(session.executeAsync(boundStmt));
			
			boundStmt = new BoundStatement(this.insertStmtHistoric);
			boundStmt.setString("key", historicData.getKey()+ "-low");
			boundStmt.setDate("date", insertDate);
			boundStmt.setDouble("value", historicData.getLow());
			results.add(session.executeAsync(boundStmt));
			
			boundStmt = new BoundStatement(this.insertStmtHistoric);
			boundStmt.setString("key", historicData.getKey()+ "-high");
			boundStmt.setDate("date", insertDate);
			boundStmt.setDouble("value", historicData.getHigh());
			results.add(session.executeAsync(boundStmt));

			boundStmt = new BoundStatement(this.insertStmtHistoric);
			boundStmt.setString("key", historicData.getKey()+ "-close");
			boundStmt.setDate("date", insertDate);
			boundStmt.setDouble("value", historicData.getClose());
			results.add(session.executeAsync(boundStmt));

			boundStmt = new BoundStatement(this.insertStmtHistoric);
			boundStmt.setString("key", historicData.getKey()+ "-volume");
			boundStmt.setDate("date", insertDate);
			boundStmt.setDouble("value", historicData.getVolume());
			results.add(session.executeAsync(boundStmt));

			boundStmt = new BoundStatement(this.insertStmtHistoric);
			boundStmt.setString("key", historicData.getKey()+ "-adjclose");
			boundStmt.setDate("date", insertDate);
			boundStmt.setDouble("value", historicData.getAdjClose());					
			results.add(session.executeAsync(boundStmt));
			
			if (historicData.getDate().after(mostRecentDate)){				
				mostRecentDate = insertDate;
				mostRecent = historicData;
			}
						
			TOTAL_POINTS.incrementAndGet();			
		}
		
		Set<String> alias = new HashSet<String>();
		alias.add(mostRecent.getKey());		
		
		String exchange = mostRecent.getKey().substring(0, mostRecent.getKey().indexOf("-"));
		String ticker = mostRecent.getKey().substring(mostRecent.getKey().indexOf("-") + 1, mostRecent.getKey().length());
		Map<String, String> ratings = new HashMap<String,String>();
		ratings.put("ratings_Moody", "Aaa");
		ratings.put("ratings_Fitch", "AAA");
		
		Map<String, String> attributes = new HashMap<String,String>();	
		attributes.put("attributes_Exchange", exchange);
		attributes.put("attributes_Ticker", ticker);
		

		//Insert most recent date.
		if (mostRecent != null){
			
			UUID uuid = UUID.randomUUID();
			
			BoundStatement boundMetaDataStmt = new BoundStatement(this.insertStmtMetaData);
			boundMetaDataStmt.setUUID("id", uuid);
			boundMetaDataStmt.setDate("updated_date", new Date());
			boundMetaDataStmt.setSet("hierarchy", this.hierarchy);
			boundMetaDataStmt.setSet("alias", alias);
			boundMetaDataStmt.setSet("variant", variant);			
			boundMetaDataStmt.setMap("ratings_",ratings);
			boundMetaDataStmt.setMap("attributes_",attributes);
			boundMetaDataStmt.setString("default_rating", "A");
			boundMetaDataStmt.setString("ts_id", exchange+"-"+ticker);
			
			results.add(session.executeAsync(boundMetaDataStmt));
		}
		
		//Wait till we have everything back.
		for (ResultSetFuture result : results) {				
			result.getUninterruptibly();
		}
		return;
	}
	
	public void insertDividend(List<Dividend> list) {
		BoundStatement boundStmt = new BoundStatement(this.insertStmtDividend);
		List<ResultSetFuture> results = new ArrayList<ResultSetFuture>();

		for (Dividend dividend: list) {

			boundStmt.setString("key", dividend.getKey());
			boundStmt.setDate("date", dividend.getDate());
			boundStmt.setDouble("dividend", dividend.getDividend());

			results.add(session.executeAsync(boundStmt));
		}

		//Wait till we have everything back.
		boolean wait = true;
		while (wait) {
			wait = false;
			for (ResultSetFuture result : results) {
				if (!result.isDone()) {
					wait = true;
					break;
				}
			}
		}
		return;
	}
	
	public void selectAllHistoricData(int fetchSize){
		Statement stmt = new SimpleStatement(SELECT_ALL);
		stmt.setFetchSize(fetchSize);
		ResultSet rs = session.execute(stmt);
		
		Iterator<Row> iterator = rs.iterator();
		
		while (iterator.hasNext()){
			iterator.next().getDouble("close");
		}		
	}
	
	public long getTotalPoints(){
		return TOTAL_POINTS.get();
	}
	
	public int getRequestCount(){
		return this.requestCount.get();
	}

	public void selectAllHistoricData(String key) {
		requestCount.incrementAndGet(); 
		
		BoundStatement bound = new BoundStatement(selectStmtByKey);
		
		ResultSetFuture results = session.executeAsync(bound.bind(key)); 
		
		for (Row row : results.getUninterruptibly()) {			
			row.getString("symbol");			
		}
	}
}
