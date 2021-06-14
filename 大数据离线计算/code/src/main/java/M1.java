import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.*;

public class M1 {
    private JPanel panel1;
    private JButton executeQueryButton;
    private JTextArea textArea1;
    private JTable table1;
    private JList tableList;
    private JButton executeUpdateButton;
    static JMenuItem configurationItem;

    public M1() {

        configurationItem = new JMenuItem("配置");
        configurationItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String jdbcUrl= JOptionPane.showInputDialog(null,"输入JDBCURL","jdbc:hive2://bigdata129.depts.bingosoft.net:22129/user31_db");
                if (jdbcUrl!=null)
                {
                    String user = JOptionPane.showInputDialog(null,"输入User","user31");
                    if(user!=null)
                    {
                        String psw = JOptionPane.showInputDialog(null,"输入Password","pass@bingo31");
                        if (psw!=null)
                        {
                            HiveTasks.jdbcURL= jdbcUrl;
                            HiveTasks.user=user;
                            HiveTasks.password =psw;

                            //载入tables
                            ListModel<String> listModel = HiveTasks.showTables();
                            if (listModel!=null)
                                tableList.setModel(listModel);
                            else
                                JOptionPane.showMessageDialog(null, "获取tables列表出错", "Error",JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        //载入tables
        ListModel<String> listModel = HiveTasks.showTables();
        if (listModel!=null)
            tableList.setModel(listModel);
        else
            JOptionPane.showMessageDialog(null, "获取tables列表出错", "Error",JOptionPane.ERROR_MESSAGE);


        executeQueryButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (textArea1.getText().equals(""))
                {
                    JOptionPane.showMessageDialog(null, "SQL语句不能为空", "WARNING",JOptionPane.WARNING_MESSAGE);
                    return;
                }

                TableModel tableModel = HiveTasks.executeQuerySql(textArea1.getText());
                if (tableModel!=null)
                    table1.setModel(tableModel);
                else
                    JOptionPane.showMessageDialog(null, "获取结果出错", "Error",JOptionPane.ERROR_MESSAGE);

            }
        });

        tableList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount()==2)
                {
                    TableModel tableModel = HiveTasks.executeQuerySql("select * from "+ tableList.getSelectedValue());
                    if (tableModel!=null)
                        table1.setModel(tableModel);
                    else
                        JOptionPane.showMessageDialog(null, "获取结果出错", "Error",JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        executeUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (textArea1.getText().equals(""))
                {
                    JOptionPane.showMessageDialog(null, "SQL语句不能为空", "WARNING",JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int i = HiveTasks.executeUpdateSql(textArea1.getText());
                if (i==-1)
                    JOptionPane.showMessageDialog(null, "语句执行出错", "ERROR",JOptionPane.ERROR_MESSAGE);
                else
                    JOptionPane.showMessageDialog(null, i+" 条语句被执行", "SUCCEED",JOptionPane.PLAIN_MESSAGE);
            }
        });
    }

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        JFrame frame = new JFrame("M1");
        frame.setLocationRelativeTo(null);
        frame.setContentPane(new M1().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);



        JMenuBar jMenuBar = new JMenuBar();
        JMenu configurationMenu = new JMenu("菜单");
        jMenuBar.add(configurationMenu);
        JMenuItem exitItem = new JMenuItem("退出");
        configurationMenu.add(configurationItem);
        configurationMenu.add(exitItem);
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        frame.setJMenuBar(jMenuBar);
        frame.pack();
        frame.setVisible(true);


    }
}
