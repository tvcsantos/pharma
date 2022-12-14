/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ExpiredGUI.java
 *
 * Created on 2/Ago/2010, 15:29:55
 */

package pharma;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author tvcsantos
 */
public class ListGUI extends javax.swing.JDialog {

    /** Creates new form ExpiredGUI */
    public ListGUI(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    public ListGUI(java.awt.Frame parent, boolean modal, ResultSet rs) {
        this(parent, modal);
        DefaultTableModel dtm = (DefaultTableModel) listTable.getModel();
        dtm.getDataVector().removeAllElements();
        try {
            while (rs.next()) {
                long barcode = rs.getLong(1);
                String name = rs.getString(2);
                java.sql.Date date = rs.getDate(3);
                int units = rs.getInt(4);
                System.out.println(barcode + " " + name + " " +
                        date + " " + units);
                dtm.addRow(new Object[]{ barcode, name, date, units});
            }
        } catch (SQLException ex) {
            Logger.getLogger(ListGUI.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        listTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(pharma.PharmaApp.class).getContext().getResourceMap(ListGUI.class);
        setTitle(resourceMap.getString("ListGUIForm.title")); // NOI18N
        setName("ListGUIForm"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        listTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Bar Code", "Name", "Expiration", "Units"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Long.class, java.lang.String.class, java.lang.Object.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        listTable.setName("listTable"); // NOI18N
        listTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(listTable);
        listTable.getColumnModel().getColumn(0).setResizable(false);
        listTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("listTable.columnModel.title0")); // NOI18N
        listTable.getColumnModel().getColumn(1).setResizable(false);
        listTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("listTable.columnModel.title1")); // NOI18N
        listTable.getColumnModel().getColumn(2).setResizable(false);
        listTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("listTable.columnModel.title2")); // NOI18N
        listTable.getColumnModel().getColumn(3).setResizable(false);
        listTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("listTable.columnModel.title3")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ListGUI dialog = new ListGUI(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable listTable;
    // End of variables declaration//GEN-END:variables

}
