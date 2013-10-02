package fi.muikku.plugins.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.ejb.Stateful;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

@Dependent
@Stateful
public class MySQLDataPluginScriptHandler implements DataPluginScriptHandler {
	
	@Inject
	private Logger logger;

	@Override
	public String getName() {
		return "MySQL";
	}

	@Override
	public void executeScript(String uri, Map<String, String> parameters) {
		try {
			URL url = new URL(uri);
			
			URLConnection connection = url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			
			InputStream inputStream = connection.getInputStream();
			try {
	  		String sqlString = IOUtils.toString(inputStream);
	  		if (StringUtils.isNotBlank(sqlString)) {
	  			StringTokenizer sqlTokenizer = new StringTokenizer(sqlString, ";");
	  			while (sqlTokenizer.hasMoreTokens()) {
	  				String sql = StringUtils.trim(sqlTokenizer.nextToken());
	  				if (StringUtils.isNotBlank(sql)) {
	  				  executeSql(sql);
	  				}
	  			}
	  		}
			} finally {
				inputStream.close();
			}
		} catch (SQLException e) {
			// TODO Proper error handling
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (MalformedURLException e) {
			// TODO Proper error handling
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			// TODO Proper error handling
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	private DataSource getDataSource() {
		try {
			Context context = new InitialContext();
			return (javax.sql.DataSource) context.lookup("java:/jdbc/muikku");
		} catch (NamingException e) {
		}

		return null;
	}
	
	@Override
	public Connection getConnection(Map<String, String> parameters) throws SQLException {
		return getDataSource().getConnection();
	}
	
	private void executeSql(String sql) throws SQLException {
		// TODO: Transaction?
		
		logger.info("Executing sql: " + sql);
		
		Connection connection = getConnection(null);
		Statement statement = connection.createStatement();
		statement.execute(sql);
	}

}
