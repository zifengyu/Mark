package markit;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkItDb {

	private static String driver = "org.apache.derby.jdbc.EmbeddedDriver";	
	private static String dbName = "markit";
	private static String connectionURL = "jdbc:derby:" + dbName;

	private static String insertStatusStatement = "insert into status(ID, UID, RID, TEXT, OPIC, PROCESSED) values (?,?,?,?,?,?)";
	private static String queryStatusStatement = "select ID, UID, RID, TEXT, OPIC, PROCESSED from status";

	private static MarkItDb db;
	private PreparedStatement psInsert;

	private Connection conn;

	public static MarkItDb getInstance() throws Exception {
		if (db == null)
			db = new MarkItDb();
		return db;		
	}

	public void shutdown() {

		/*** In embedded mode, an application should shut down Derby.
           Shutdown throws the XJ015 exception to confirm success. ***/			
		if (driver.equals("org.apache.derby.jdbc.EmbeddedDriver")) {
			boolean gotSQLExc = false;
			try {
				DriverManager.getConnection("jdbc:derby:;shutdown=true");
			} catch (SQLException se)  {	
				if ( se.getSQLState().equals("XJ015") ) {		
					gotSQLExc = true;
				}
			}
			if (!gotSQLExc) {
				System.out.println("Database did not shut down normally");
			}  else  {
				System.out.println("Database shut down normally");	
			}  
		}

	}

	public long getMaxStatusId() {
		long maxId = 0;
		Statement s;
		try {
			s = conn.createStatement();

			ResultSet rs = s.executeQuery("select max(id) from status");
			if (rs.next()) {
				maxId = rs.getLong(1);
			}
			s.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return maxId == 0 ? 1 : maxId;
	}

	public void insertStatus(String id, String rid, String text, String opic, String uid, int processed) throws SQLException {
		psInsert.setBigDecimal(1, new BigDecimal(id));
		psInsert.setBigDecimal(2, new BigDecimal(uid));
		psInsert.setBigDecimal(3, new BigDecimal(rid));
		psInsert.setString(4, text);
		psInsert.setString(5, opic);				
		psInsert.setInt(6, processed);
		try {
			psInsert.executeUpdate();
		} catch (Exception ex) {

		}
	}

	public ResultSet getStatus(String id) throws SQLException {
		Statement s = conn.createStatement();
		return s.executeQuery(queryStatusStatement + " where id = " + id);		
	}

	public String getStatusHTML(String id) {
		String html = "";
		try {
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery("select TEXT, RID, OPIC from status where id = " + id);
			if (rs.next()) {				
				long rid = rs.getLong(2);
				String text = rs.getString(1);
				if (text.contains("@标记为")) {
					text = text.substring(text.indexOf("@标记为") + 4).trim();
				}
				String opic = rs.getString(3);
				if (rid != 0) {
					ResultSet rs2 = s.executeQuery("select TEXT, RID, OPIC from status where id = " + rid);
					if (rs2.next()) {
						html += rs2.getString(1);
						
						int index;
						String html2 = "";
						while ((index = html.indexOf("http://")) != -1) {
							html2 = html.substring(0, index);
							html = html.substring(index);
														
							index = html.indexOf(' ');
							if (index == -1)
								index = html.length();
							String url = html.substring(0, index);
							html2 += "<a href=\"" + url + "\">" + url + "</a>";
							html = html.substring(index);
						}
						
						html = html2 + html;
						
						
						html += "<br><br>";
						String opic2 = rs2.getString(3);
						if (opic2.trim().length() > 0) {
							html += "<img src=\"" + opic2.trim() + "\"><br><br>";
						}
					}
					rs2.close();
				}
				html += text + "<br>";

				if (opic.trim().length() > 0) {
					html += "<img src=\"" + opic.trim() + "\"><br>";
				}
			}
			rs.close();
			s.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return html;

	}

	public String getUserTarget(String status_id) {
		String target = null;
		try {
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery("select target from users, status where users.id = status.uid and status.id = " + status_id);
			if (rs.next()) {
				target = rs.getString(1);
				rs.close();				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return target;		
	}

	public String getTitle(String status_id) {
		String title = null;
		ResultSet rs = null;
		try {
			Statement s = conn.createStatement();			
			rs = s.executeQuery("select text from status where status.id = " + status_id);
			if (rs.next()) {
				title = rs.getString(1);
				title = title.substring(title.indexOf("@标记为") + 4).trim();
				if (title.contains("//")) {
					title = title.substring(0, title.indexOf("//")).trim();
				}								
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		return title;	

	}

	public ArrayList<String> getUnprocessedStatus() {
		ArrayList<String> idList = new ArrayList<String>();
		ResultSet rs = null;
		try {
			Statement s = conn.createStatement();
			rs = s.executeQuery("select id from status where processed = 0");
			while (rs.next()) {
				idList.add(rs.getString(1));								
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return idList;

	}

	public void updateProcessedStatus(String status_id) {

		try {
			Statement s = conn.createStatement();			
			s.executeUpdate("update status set processed = 1 where status.id = " + status_id);
			s.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}

	private MarkItDb() throws Exception {
		Class.forName(driver);
		conn = DriverManager.getConnection(connectionURL);
		psInsert = conn.prepareStatement(insertStatusStatement);
	}


}
