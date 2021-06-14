import java.sql.*;
import java.util.regex.Pattern;

public class Test {
    /*hiverserver 版本使用此驱动*/
    //private static String driverName = "org.apache.hadoop.hive.jdbc.HiveDriver";
    /*hiverserver2 版本使用此驱动*/
    private static String driverName = "org.apache.hive.jdbc.HiveDriver";

    public static void main(String[] args) throws Exception {
        System.out.println(Pattern.matches("(del|asd) .*?","asdd asdw"));
//        try {
//            Class.forName(driverName);
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//        Connection conn = DriverManager.getConnection("jdbc:hive2://bigdata129.depts.bingosoft.net:22129/user31_db", "user31", "pass@bingo311");
//        try {
//            Statement st = conn.createStatement();
//            ResultSet ret = st.executeQuery("SHOW TABLES");
//
//            if (ret.next()) {
//                while (ret.next()) {
//                    StringBuilder sb = new StringBuilder();
////                    sb.append("id:" + ret.getInt("id"));
//                    sb.append("  name:" + ret.getString(1));
////                    sb.append("  age:" + ret.getInt("age"));
//                    //sb.append("  deptName:" + ret.getString("dept_name"));
//                    System.out.println(sb.toString());
//                }
////                System.out.println(ret.getInt(1));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            conn.close();
//        }
    }
}