import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.sql.*;
import java.util.Vector;

public class HiveTasks {
    static String jdbcURL="jdbc:hive2://bigdata129.depts.bingosoft.net:22129/user31_db";
    static String user="user31";
    static String password = "pass@bingo31";

    public static ListModel<String> showTables()
    {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
            System.exit(1);
        }
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(jdbcURL, user, password);
            Statement st = conn.createStatement();
            st.setQueryTimeout(10);
            ResultSet ret = st.executeQuery("SHOW TABLES");

            if (ret.next()) {
                while (ret.next()) {
                    listModel.addElement(ret.getString(1));
                }
            }
            return listModel;
        } catch (Exception e1) {
            e1.printStackTrace();
            return null;
        } finally {
            try {
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public static TableModel executeQuerySql(String sql)
    {
        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
            System.exit(1);
        }
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(jdbcURL, user, password);
            Statement st = conn.createStatement();
            st.setQueryTimeout(10);
            ResultSet ret = st.executeQuery(sql);
            int columnCount = ret.getMetaData().getColumnCount();
            System.out.println(columnCount);
            Vector<String> columnData = new Vector<>();
            for (int i=0;i<columnCount;i++)
            {
                columnData.add( ret.getMetaData().getColumnName(i+1));
            }
            DefaultTableModel defaultTableModel =new DefaultTableModel(new Vector<>(),columnData);
            if (ret.next()) {
                while (ret.next()) {
                    String[] row = new String[columnCount];
                    for (int i =0;i<columnCount;i++)
                    {
                        row[i] = ret.getString(i+1);
                    }
                    defaultTableModel.addRow(row);
                }
            }
            return defaultTableModel;
        } catch (Exception e1) {
            e1.printStackTrace();
            return null;
        } finally {
            try {
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
    public static int executeUpdateSql(String sql)
    {
        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
            System.exit(1);
        }
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(jdbcURL, user, password);
            Statement st = conn.createStatement();
            st.setQueryTimeout(10);
            int i = st.executeUpdate(sql);
            return i;
        } catch (Exception e1) {
            e1.printStackTrace();
            return -1;
        } finally {
            try {
                conn.close();
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
        }
    }
}
