/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package YelpSwingUI;

import java.awt.Component;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;
import static javafx.application.Platform.exit;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

public class YelpSearchApplication extends javax.swing.JFrame {

    private static Connection con;

    public YelpSearchApplication() {

  
        con = DBConnection();
        initComponents();
   
        listener_subc.setVisible(false);
        listener_attr.setVisible(true);
        
        ChangeListener Listener = new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                listener_subc.doClick();
            }
        };
        for (Component c : CategoryPanel.getComponents()) {
            if (c.getClass().equals(javax.swing.JCheckBox.class)) {
                JCheckBox jcb = (JCheckBox) c;
                jcb.addChangeListener(Listener);
            }
        }
        
        ChangeListener Listener1 = new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                listener_attr.doClick();
            }
        };
        for (Component c : SubCategoryPanel.getComponents()) {
            if (c.getClass().equals(javax.swing.JCheckBox.class)) {
                JCheckBox jcb = (JCheckBox) c;
                jcb.addChangeListener(Listener1);
            }
        }
       
        
       
        
        
       
    }

    private static Connection DBConnection() {

        try {
            //Registering the Driver
            DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
        } catch (SQLException abc) {
            System.out.println("Could not register driver");
            exit();
        }
        //Getting the connection
        Connection con = null;
        String dbUrl = "jdbc:oracle:thin:@localhost:1521:XE";
        try {
            con = DriverManager.getConnection(dbUrl, "system", "newpassword");
            System.out.println("Connection setup");
        } catch (SQLException abc) {
            System.out.println("Connection Failed for some unknown reason");
            abc.printStackTrace();
        }

        return con;
    }

    private static String setDateformat(String inDate) throws ParseException {
        SimpleDateFormat req_format = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = null;
        date = format.parse(inDate);
        return req_format.format(date);
    }

    private String buildQuery() {

        //User Query to retrive User data based on user filter
        if (UserSelection.isSelected()) {
            boolean check = false;
            
            String userQuery = "select YELPING_SINCE,USER_NAME,USER_ID,AVERAGE_STARS from YELP_USERS where to_char(YELPING_SINCE) >= '1600-01-01' ";

            String userEnteredDate = userDate.getText();
            String userEnteredReviewCount = userReviewCount_value.getText();
            String userEnteredFriends = userFriends_value.getText();
            String userEnteredAvgStars = userAvgStars_value.getText();
            String and_or = userOption.getSelectedItem().toString();

            if (!userEnteredDate.equals("")) {
                if (!userEnteredDate.equals("yyyy-MM-dd")) {

                    try {
                        userQuery = userQuery.replace("1600-01-01", userDate.getText());
                   

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!userEnteredReviewCount.equals("")) {
                String symbol = userReviewCount.getSelectedItem().toString();
                userQuery = userQuery + and_or + " REVIEW_COUNT " + symbol + " " + userEnteredReviewCount + " ";

            }
            if (!userEnteredFriends.equals("")) {
                String symbol = userFriends.getSelectedItem().toString();

                userQuery = userQuery + and_or + " FRIENDS_COUNT " + symbol + " " + userEnteredFriends + " ";

            }
            if (!userEnteredAvgStars.equals("")) {
                String symbol = userAvgStars.getSelectedItem().toString();

                userQuery = userQuery + and_or + " AVERAGE_STARS " + symbol + " " + userEnteredAvgStars + " ";

            }



                return userQuery;
           
        } else { //Building Query for Business Search
     
            //Store selected Categories 
            ArrayList<String> BusiCate = new ArrayList();
            for (Component c : CategoryPanel.getComponents()) {
                if (c.getClass().equals(javax.swing.JCheckBox.class)) {
                    JCheckBox check = (JCheckBox) c;
                    if (check.isSelected()) {
                        BusiCate.add(check.getText());
                    }
                }
            }
            //Store selected sub Categories
            ArrayList<String> subCateSelected = new ArrayList();
            for (Component c : SubCategoryPanel.getComponents()) {
                if (c.getClass().equals(javax.swing.JCheckBox.class)) {
                    JCheckBox check = (JCheckBox) c;
                    if (check.isSelected()) {
                        subCateSelected.add(check.getText());
                    }
                }
            }
            //Store selected Attributes
            ArrayList<String> AttributeSelected = new ArrayList();
            for (Component c : AttributePanel.getComponents()) {
                if (c.getClass().equals(javax.swing.JCheckBox.class)) {
                    JCheckBox check = (JCheckBox) c;
                    if (check.isSelected()) {
                        AttributeSelected.add(check.getText());
                    }
                }
            }
            String and_or = userOption.getSelectedItem().toString();
            String businessQuery = "(SELECT BUSINESS_ID FROM Business_To_Category WHERE CATEGORY = ";
            String mQ = "";
            boolean isCategoryWhereInlcued = true;
          //  String reviewQuery = "";//SELECT DISTINCT  R.BUSINESS_ID  FROM REVIEWS R where to_char(DATE_OF_REVIEW) >= '1600-01-01' "+and_or+" to_char(DATE_OF_REVIEW) <= '2020-12-12 ";

            if (and_or == "AND") {

                businessQuery = businessQuery + "'" + BusiCate.get(0).trim() + "')";
                if (BusiCate.size() > 1) {
                    isCategoryWhereInlcued = true;
                    //buisnessQuery += " where BUSINESS_ID in ( select BUSINESS_ID from Business_To_Category where CATEGORY in(";
                    for (int i = 1; i < BusiCate.size(); i++) {
                        businessQuery = businessQuery + "  INTERSECT (select BUSINESS_ID from Business_To_Category where CATEGORY = '" + BusiCate.get(i).trim() + "')";
                    //if (!(i + 1 == BusiCate.size())) {
                        //  buisnessQuery = buisnessQuery + " , ";
                    }
                }
                String mainQuery = "select DISTINCT b.BUSINESS_NAME,b.BUSINESS_ID,b.CITY,b.STATE,b.STARS from BUSINESS b  WHERE b.BUSINESS_ID in (" + businessQuery+")";
                // buisnessQuery = buisnessQuery + " ) ";
                mQ = mainQuery;

                if (subCateSelected.size() == 0) {
                    //DO NOTHING
                } else {
                    mQ = mQ.replace("WHERE b.BUSINESS_ID in", " JOIN Business_To_Sub_Category bs ON bs.BUSINESS_ID = b.BUSINESS_ID WHERE b.BUSINESS_Id IN");
                    //mQ= mQ.replace("JOIN Business_To_Category bc ON b.BUSINESS_ID = bc.BUSINESS_ID WHERE b.BUSINESS_ID in","JOIN Business_To_Category bc on b.BUSINESS_ID = bc.BUSINESS_ID  JOIN Business_To_Sub_Category bs ON bs.BUSINESS_ID = b.BUSINESS_ID WHERE  b.BUSINESS_Id in");
                    mQ += " AND b.BUSINESS_ID IN ((select BUSINESS_ID FROM BUSINESS_TO_SUB_CATEGORY where Sub_Category =  '" + subCateSelected.get(0).trim() + "') ";
                    if (subCateSelected.size() > 1) {
                        for (int i = 1; i < subCateSelected.size(); i++) {
                            mQ +=  " INTERSECT (select BUSINESS_ID FROM BUSINESS_TO_SUB_CATEGORY where Sub_Category =  '" + subCateSelected.get(i).trim() + "' )";
                        }
                    }
                    mQ += ")";
                    if (AttributeSelected.size() == 0) {
                        //Do nothin
                    } else {
                        mQ = mQ.replace("WHERE b.BUSINESS_Id IN", "JOIN BUSINESS_TO_ATTRIBUTE BA on BA.BUSINESS_ID = b.Business_ID where b.Business_ID in");
                        mQ += " AND b.BUSINESS_ID IN ((select Business_id from BUSINESS_TO_ATTRIBUTE BA  where BA.ATTRIBUTE_NAME ='" + AttributeSelected.get(0).trim() + "')";
                        if (AttributeSelected.size() > 1) {
                            for (int i = 1; i < AttributeSelected.size(); i++) {
                                mQ +=  " INTERSECT (select Business_id from BUSINESS_TO_ATTRIBUTE BA  where BA.ATTRIBUTE_NAME ='" + AttributeSelected.get(i).trim() + "')";

                            }
                        }
                        mQ += ")";
                    }
                    //return mQ;
                }

            } else {
                businessQuery = businessQuery + "'" + BusiCate.get(0).trim() + "')";
                if (BusiCate.size() > 1) {
                    isCategoryWhereInlcued = true;
                    //buisnessQuery += " where BUSINESS_ID in ( select BUSINESS_ID from Business_To_Category where CATEGORY in(";
                    for (int i = 1; i < BusiCate.size(); i++) {
                        businessQuery = businessQuery + "  UNION (select BUSINESS_ID from Business_To_Category where CATEGORY = '" + BusiCate.get(i).trim() + "')";
                    //if (!(i + 1 == BusiCate.size())) {
                        //  buisnessQuery = buisnessQuery + " , ";
                    }
                }
                String mainQuery = "select DISTINCT b.BUSINESS_NAME,b.BUSINESS_ID,b.CITY,b.STATE,b.STARS from BUSINESS b  WHERE b.BUSINESS_ID in (" + businessQuery+")";
                // buisnessQuery = buisnessQuery + " ) ";
                mQ = mainQuery;

                if (subCateSelected.size() == 0) {
                    //DO NOTHING
                } else {
                    mQ = mQ.replace("WHERE b.BUSINESS_ID in", " JOIN Business_To_Sub_Category bs ON bs.BUSINESS_ID = b.BUSINESS_ID WHERE b.BUSINESS_Id IN");
                    //mQ= mQ.replace("JOIN Business_To_Category bc ON b.BUSINESS_ID = bc.BUSINESS_ID WHERE b.BUSINESS_ID in","JOIN Business_To_Category bc on b.BUSINESS_ID = bc.BUSINESS_ID  JOIN Business_To_Sub_Category bs ON bs.BUSINESS_ID = b.BUSINESS_ID WHERE  b.BUSINESS_Id in");
                    mQ += " AND b.BUSINESS_ID IN ((select BUSINESS_ID FROM BUSINESS_TO_SUB_CATEGORY where Sub_Category =  '" + subCateSelected.get(0).trim() + "') ";
                    if (subCateSelected.size() > 1) {
                        for (int i = 1; i < subCateSelected.size(); i++) {
                            mQ +=  " UNION (select BUSINESS_ID FROM BUSINESS_TO_SUB_CATEGORY where Sub_Category =  '" + subCateSelected.get(i).trim() + "' )";
                        }
                    }
                    mQ += ")";
                    if (AttributeSelected.size() == 0) {
                        //Do nothin
                    } else {
                        mQ = mQ.replace("WHERE b.BUSINESS_Id IN", "JOIN BUSINESS_TO_ATTRIBUTE BA on BA.BUSINESS_ID = b.Business_ID where b.Business_ID in");
                        mQ += " AND b.BUSINESS_ID IN ((select Business_id from BUSINESS_TO_ATTRIBUTE BA  where BA.ATTRIBUTE_NAME ='" + AttributeSelected.get(0).trim() + "')";
                        if (AttributeSelected.size() > 1) {
                            for (int i = 1; i < AttributeSelected.size(); i++) {
                                mQ +=  " UNION (select Business_id from BUSINESS_TO_ATTRIBUTE BA  where BA.ATTRIBUTE_NAME ='" + AttributeSelected.get(i).trim() + "')";

                            }
                        }
                        mQ += ")";
                    }
                    //return mQ;
                }
                

            }
            if (isReview.isSelected()) {

                String from_date = review_from_date.getText();
                String to_date = review_to_date.getText();
                String Stars = review_star_count.getText();
                String Votes = review_vote_count.getText();
                String reviewQuery = "Select DISTINCT b.BUSINESS_NAME,b.BUSINESS_ID,b.CITY,b.STATE,b.STARS from BUSINESS b where b.BUSINESS_ID in(Select Distinct Main.Business_ID from (" + mQ + ") Main intersect select distinct RSet.Business_ID from Reviews Rset where to_char(DATEE) >= '1600-01-01' " + and_or + " to_char(DATEE) <= '2020-12-12'";
                if (!from_date.equals("")) {
                    if (!from_date.equals("yyyy-MM-dd")) {

                        try {
                            reviewQuery = reviewQuery.replace("1600-01-01", from_date);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!to_date.equals("")) {
                    if (!from_date.equals("yyyy-MM-dd")) {
                        try {
                            reviewQuery = reviewQuery.replace("2020-12-12", to_date);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (!Stars.equals("")) {
                    String sysmbol = review_stars.getSelectedItem().toString();

                    reviewQuery = reviewQuery + " " + and_or + " Rset.STARS " + sysmbol + " " + Stars + " ";
                }
                if (!Votes.equals("")) {
                    String sysmbol = review_votes.getSelectedItem().toString();
                    reviewQuery = reviewQuery + " " + "intersect select distinct VR.Business_ID FROM VOTES_REVIEW VR GROUP BY VR.BUSINESS_ID HAVING SUM(VR.FUNNY+VR.COOL+VR.USEFUL) ";
                    reviewQuery = reviewQuery + " " + sysmbol + " " + Votes + " ";

                }
                reviewQuery += ")";
                return reviewQuery;
            }
            return mQ;
        }

    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        CategoryPanel = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        jCheckBox5 = new javax.swing.JCheckBox();
        jCheckBox6 = new javax.swing.JCheckBox();
        jCheckBox7 = new javax.swing.JCheckBox();
        jCheckBox8 = new javax.swing.JCheckBox();
        jCheckBox9 = new javax.swing.JCheckBox();
        jCheckBox10 = new javax.swing.JCheckBox();
        jCheckBox11 = new javax.swing.JCheckBox();
        jCheckBox12 = new javax.swing.JCheckBox();
        jCheckBox13 = new javax.swing.JCheckBox();
        jCheckBox14 = new javax.swing.JCheckBox();
        jCheckBox15 = new javax.swing.JCheckBox();
        jCheckBox16 = new javax.swing.JCheckBox();
        jCheckBox17 = new javax.swing.JCheckBox();
        jCheckBox18 = new javax.swing.JCheckBox();
        jCheckBox19 = new javax.swing.JCheckBox();
        jCheckBox20 = new javax.swing.JCheckBox();
        jCheckBox21 = new javax.swing.JCheckBox();
        jCheckBox22 = new javax.swing.JCheckBox();
        jCheckBox23 = new javax.swing.JCheckBox();
        jCheckBox24 = new javax.swing.JCheckBox();
        jCheckBox25 = new javax.swing.JCheckBox();
        jCheckBox26 = new javax.swing.JCheckBox();
        jCheckBox27 = new javax.swing.JCheckBox();
        jCheckBox28 = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        SubCategoryPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        QueryArea = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        listener_subc = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        ResultTable = new javax.swing.JTable();
        userFriends = new javax.swing.JComboBox();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        userReviewCount = new javax.swing.JComboBox();
        userAvgStars = new javax.swing.JComboBox();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        userReviewCount_value = new javax.swing.JTextField();
        userFriends_value = new javax.swing.JTextField();
        userAvgStars_value = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        userOption = new javax.swing.JComboBox();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        review_stars = new javax.swing.JComboBox();
        review_star_count = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        review_votes = new javax.swing.JComboBox();
        review_vote_count = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        BuildQuery = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        UserSelection = new javax.swing.JCheckBox();
        isReview = new javax.swing.JCheckBox();
        ExcuteQueryButton = new javax.swing.JButton();
        userDate = new javax.swing.JTextField();
        review_from_date = new javax.swing.JTextField();
        review_to_date = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        AttributePanel = new javax.swing.JPanel();
        listener_attr = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(153, 153, 255));
        setForeground(new java.awt.Color(255, 204, 204));

        jLabel1.setText("Query : ");

        jLabel2.setText("Category");

        jCheckBox1.setText("Active Life");

        jCheckBox2.setText("Arts & Entertainment");

        jCheckBox3.setText("Automotive");

        jCheckBox4.setText("Car Rental");

        jCheckBox5.setText("Cafes");

        jCheckBox6.setText("Beauty & Spas");

        jCheckBox7.setText("Convenience Stores");

        jCheckBox8.setText("Dentists");

        jCheckBox9.setText("Doctors");

        jCheckBox10.setText("Drugstores");

        jCheckBox11.setText("Department Stores");

        jCheckBox12.setText("Education");

        jCheckBox13.setText("Event Planning & Services");

        jCheckBox14.setText("Flowers & Gifts");

        jCheckBox15.setText("Food");

        jCheckBox16.setText("Health & Medical");

        jCheckBox17.setText("Home Services");

        jCheckBox18.setText("Home & Garden");

        jCheckBox19.setText("Hospitals");

        jCheckBox20.setText("Hotels & Travel");

        jCheckBox21.setText("Hardware Stores");

        jCheckBox22.setText("Grocery");

        jCheckBox23.setText("Medical Centers");

        jCheckBox24.setText("Nurseries & Gardening");

        jCheckBox25.setText("Nightlife");

        jCheckBox26.setText("Restaurants");

        jCheckBox27.setText("Shopping");

        jCheckBox28.setText("Transportation");

        javax.swing.GroupLayout CategoryPanelLayout = new javax.swing.GroupLayout(CategoryPanel);
        CategoryPanel.setLayout(CategoryPanelLayout);
        CategoryPanelLayout.setHorizontalGroup(
            CategoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CategoryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(CategoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox1)
                    .addComponent(jCheckBox2)
                    .addComponent(jCheckBox3)
                    .addComponent(jCheckBox4)
                    .addComponent(jCheckBox5)
                    .addComponent(jCheckBox6)
                    .addComponent(jCheckBox7)
                    .addComponent(jCheckBox8)
                    .addComponent(jCheckBox9)
                    .addComponent(jCheckBox10)
                    .addComponent(jCheckBox11)
                    .addComponent(jCheckBox12)
                    .addComponent(jCheckBox13)
                    .addComponent(jCheckBox14)
                    .addComponent(jCheckBox15)
                    .addComponent(jCheckBox16)
                    .addComponent(jCheckBox17)
                    .addComponent(jCheckBox18)
                    .addComponent(jCheckBox19)
                    .addComponent(jCheckBox20)
                    .addComponent(jCheckBox21)
                    .addComponent(jCheckBox22)
                    .addComponent(jCheckBox23)
                    .addComponent(jCheckBox24)
                    .addComponent(jCheckBox25)
                    .addComponent(jCheckBox26)
                    .addComponent(jCheckBox27)
                    .addComponent(jCheckBox28))
                .addContainerGap(135, Short.MAX_VALUE))
        );
        CategoryPanelLayout.setVerticalGroup(
            CategoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CategoryPanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox23)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox24)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox25)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox26)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox27)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox28)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(CategoryPanel);

        jLabel3.setText("SubCategory");

        SubCategoryPanel.setForeground(new java.awt.Color(255, 204, 204));

        javax.swing.GroupLayout SubCategoryPanelLayout = new javax.swing.GroupLayout(SubCategoryPanel);
        SubCategoryPanel.setLayout(SubCategoryPanelLayout);
        SubCategoryPanelLayout.setHorizontalGroup(
            SubCategoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 278, Short.MAX_VALUE)
        );
        SubCategoryPanelLayout.setVerticalGroup(
            SubCategoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 362, Short.MAX_VALUE)
        );

        jScrollPane2.setViewportView(SubCategoryPanel);

        QueryArea.setEditable(false);
        QueryArea.setColumns(20);
        QueryArea.setRows(5);
        jScrollPane3.setViewportView(QueryArea);

        jLabel4.setText("Users");

        jLabel6.setText("Reviews");

        listener_subc.setText("Track categories");
        listener_subc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listener_subcActionPerformed(evt);
            }
        });

        ResultTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        ResultTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ResultTableMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(ResultTable);

        userFriends.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<", ">", "=" }));

        jLabel16.setText("Member since");

        jLabel17.setText("Review count");

        jLabel18.setText("Number of friends");

        jLabel19.setText("Avg stars");

        userReviewCount.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<", ">", "=" }));

        userAvgStars.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<", ">", "=" }));

        jLabel20.setText("Value");

        jLabel21.setText("Value");

        jLabel22.setText("Value");

        jLabel23.setText("Select");

        userOption.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "AND", "OR" }));

        jLabel24.setText("From");

        jLabel25.setText("To");

        review_stars.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<", ">", "=" }));

        jLabel26.setText("Stars");

        review_votes.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<", ">", "=" }));

        review_vote_count.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                review_vote_countActionPerformed(evt);
            }
        });

        jLabel28.setText("Votes");

        BuildQuery.setText("Build Query");
        BuildQuery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BuildQueryActionPerformed(evt);
            }
        });

        UserSelection.setText("Select for user");
        UserSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UserSelectionActionPerformed(evt);
            }
        });

        isReview.setText("select for review");

        ExcuteQueryButton.setText("Execute Query");
        ExcuteQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExcuteQueryButtonActionPerformed(evt);
            }
        });

        userDate.setText("yyyy-MM-dd");

        review_from_date.setText("yyyy-MM-dd");
        review_from_date.setSelectionColor(new java.awt.Color(255, 255, 255));

        review_to_date.setText("yyyy-MM-dd");
        review_to_date.setSelectionColor(new java.awt.Color(255, 255, 0));
        review_to_date.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                review_to_dateActionPerformed(evt);
            }
        });

        jLabel5.setText("Attributes");

        javax.swing.GroupLayout AttributePanelLayout = new javax.swing.GroupLayout(AttributePanel);
        AttributePanel.setLayout(AttributePanelLayout);
        AttributePanelLayout.setHorizontalGroup(
            AttributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 267, Short.MAX_VALUE)
        );
        AttributePanelLayout.setVerticalGroup(
            AttributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 361, Short.MAX_VALUE)
        );

        jScrollPane5.setViewportView(AttributePanel);

        listener_attr.setText("Track Subcategories");
        listener_attr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listener_attrActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(1, 1, 1)
                                        .addComponent(jLabel23))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel16)
                                            .addComponent(jLabel4))
                                        .addGap(72, 72, 72)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(UserSelection)
                                            .addComponent(userDate, javax.swing.GroupLayout.PREFERRED_SIZE, 580, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(listener_subc)))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(userOption, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                            .addComponent(jLabel19)
                                            .addGap(74, 74, 74)
                                            .addComponent(userAvgStars, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                            .addComponent(jLabel18)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(userFriends, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(jLabel17)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(userReviewCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(413, 413, 413)
                                        .addComponent(BuildQuery)
                                        .addGap(44, 44, 44)
                                        .addComponent(ExcuteQueryButton))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(97, 97, 97)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel21)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(jLabel22))
                                                .addGap(18, 18, 18)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addComponent(userAvgStars_value, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(userReviewCount_value, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(userFriends_value, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(146, 146, 146)
                                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 506, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
                        .addGap(247, 247, 247)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 582, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(613, 613, 613)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(260, 260, 260)
                                .addComponent(jLabel3))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(listener_attr)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(9, 9, 9)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(39, 39, 39)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel6)
                                        .addComponent(jLabel24)
                                        .addComponent(jLabel25)
                                        .addComponent(jLabel26))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel28)
                                        .addGap(19, 19, 19)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(isReview)
                                        .addGap(30, 30, 30))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(review_from_date, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(review_to_date, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGroup(layout.createSequentialGroup()
                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(review_votes, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(review_stars, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(review_star_count, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(review_vote_count, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                        .addGap(6, 6, 6))))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(729, 729, 729)))
                .addContainerGap(313, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(46, 46, 46)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel24)
                                            .addComponent(review_from_date, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(isReview)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(19, 19, 19)
                                        .addComponent(jLabel25))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addComponent(review_to_date, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel26))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(14, 14, 14)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(review_star_count, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(review_stars, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(25, 25, 25)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel28)
                                    .addComponent(review_votes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(review_vote_count, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jScrollPane5)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(listener_subc)
                    .addComponent(listener_attr))
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(UserSelection))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(userDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(jLabel17)
                                .addGap(27, 27, 27)
                                .addComponent(jLabel19))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(49, 49, 49)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(userReviewCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel20)
                                    .addComponent(userReviewCount_value, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(userAvgStars_value, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(16, 16, 16)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(userAvgStars, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel22))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(userFriends, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel21)
                                            .addComponent(userFriends_value, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel18))))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel23)
                            .addComponent(userOption, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(38, 38, 38)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(BuildQuery)
                            .addComponent(ExcuteQueryButton))
                        .addGap(37, 37, 37)))
                .addGap(101, 101, 101))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void executeAppQuery(String Query, boolean isUserQurey) {
        try {
            QueryArea.setText(Query);
            Statement QueryStatemnet = con.createStatement();
            if (isUserQurey) {
                QueryStatemnet.executeUpdate("alter session set nls_date_format = 'yyyy-MM-dd'");
            } else {
                QueryStatemnet.executeUpdate("alter session set nls_date_format = 'yyyy-MM-dd'");
            }
            ResultSet tablerow = QueryStatemnet.executeQuery(Query);
            ResultSetMetaData rsmd = tablerow.getMetaData();
            DefaultTableModel model = new DefaultTableModel();
            model.setColumnCount(rsmd.getColumnCount());
            Vector<String> cols = new Vector();
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                cols.add(rsmd.getColumnName(i + 1));
            }
            model.setColumnIdentifiers(cols);
            while (tablerow.next()) {
                Vector<String> rows = new Vector();
                for (int i = 0; i < rsmd.getColumnCount(); i++) {
                    rows.add(tablerow.getString(rsmd.getColumnName(i + 1)));
                }
                model.addRow(rows);
            }
            ResultTable.setModel(model);

        } catch (Exception abc) {
            abc.printStackTrace();
        }
    }

    private void listener_subcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listener_subcActionPerformed
        // TODO add your handling code here:
        
        String query = null;
        ArrayList<String> BusiCateg = new ArrayList();
        for (Component c : CategoryPanel.getComponents()) {
            if (c.getClass().equals(javax.swing.JCheckBox.class)) {
                JCheckBox jcb = (JCheckBox) c;
                if (jcb.isSelected()) {
                    BusiCateg.add(jcb.getText());
                }
            }
        }


        query = "select distinct SUB_CATEGORY from Business_To_Sub_Category where BUSINESS_ID in(select BUSINESS_ID from Business_To_Category where CATEGORY in (";
        for (int i = 0; i < BusiCateg.size(); i++) {
            query = query + "'" + BusiCateg.get(i) + "'";
            if (!(i + 1 == BusiCateg.size())) {
                query = query + " , ";
            }

        }
        query = query + " ))";
        // System.out.println(query);
        SubCategoryPanel.removeAll();
        SubCategoryPanel.repaint();
        if (BusiCateg.size() == 0) {
            return;
        }
        try {
            Statement subCatStatement = con.createStatement();
            ResultSet res = subCatStatement.executeQuery(query);
            subCatCheckBoxs = new ArrayList();
            while (res.next()) {
                JCheckBox newCheckBox = new JCheckBox();
                newCheckBox.setText(res.getString(1) + "\n");
                subCatCheckBoxs.add(newCheckBox);
            }
            SubCategoryPanel.setLayout(new GridLayout(0, 1, 10, 10));
            for (JCheckBox ch : subCatCheckBoxs) {
                SubCategoryPanel.add(ch);
                SubCategoryPanel.revalidate();
                SubCategoryPanel.repaint();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
       
   

    }//GEN-LAST:event_listener_subcActionPerformed

    private void BuildQueryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BuildQueryActionPerformed
        // TODO add your handling code here:
        String query = buildQuery();
        QueryArea.setText(query);
        System.out.println(query);
    }//GEN-LAST:event_BuildQueryActionPerformed

    private void UserSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UserSelectionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_UserSelectionActionPerformed

    private void ResultTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ResultTableMouseClicked
        // TODO add your handling code here:

        String reviewQuery = "";
        String from_date = review_from_date.getText();
        String to_date = review_to_date.getText();
        String Stars = review_star_count.getText();
        String Votes = review_vote_count.getText();
        String and_or = userOption.getSelectedItem().toString();
        //String reviewQuery = "SELECT DISTINCT  R.BUSINESS_ID  FROM REVIEWS R where to_char(DATE_OF_REVIEW) >= '1600-01-01' "+and_or+" to_char(DATE_OF_REVIEW) <= '2020-12-12 ";
        int selectedRowID = ResultTable.getSelectedRow();
        if (UserSelection.isSelected()) {
            String id = (String) ResultTable.getValueAt(selectedRowID, ResultTable.getColumnModel().getColumnIndex("USER_ID"));
            reviewQuery = reviewQuery + " Select R.REVIEW_ID,R.TEXT,Yu.USER_NAME from reviews R, YELP_USERS Yu WHERE R.USER_ID = Yu.USER_ID AND R.USER_ID = '" + id + "' ";// + and_or + " to_char(DATEE) >= '1600-01-01' " + and_or + " to_char(DATEE) <= '2020-12-12' ";
            if (!from_date.equals("")) {
                if (!from_date.equals("yyyy-MM-dd")) {

                    try {
                       // reviewQuery = reviewQuery.replace("1600-01-01", from_date);
                        reviewQuery = " Select R.REVIEW_ID,R.TEXT,Yu.USER_NAME from reviews R, YELP_USERS Yu WHERE R.USER_ID = Yu.USER_ID AND R.USER_ID = '" + id + "' " + and_or + " to_char(DATEE) >= " + from_date + and_or + " to_char(DATEE) <= '2020-12-12' ";

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!to_date.equals("")) {
                if (!from_date.equals("yyyy-MM-dd")) {
                    try {
                        reviewQuery = reviewQuery.replace("2020-12-12", to_date);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (!Stars.equals("")) {
                String sysmbol = review_stars.getSelectedItem().toString();

                reviewQuery = reviewQuery + and_or + " R.STARS " + sysmbol + " " + Stars + " ";
            }
            // if (!Votes.equals("")) {
            //      String sysmbol = review_votes.getSelectedItem().toString();

                ///        reviewQuery = reviewQuery + and_or + " R.VOTES " + sysmbol + " " + Votes + " ";
            ///  } 
        } else {
            String id = (String) ResultTable.getValueAt(selectedRowID, ResultTable.getColumnModel().getColumnIndex("BUSINESS_ID"));

            reviewQuery = reviewQuery + "Select R.REVIEW_ID,R.TEXT,Yu.USER_NAME from reviews R ,YELP_USERS Yu WHERE R.USER_ID = Yu.USER_ID AND R.BUSINESS_ID = '" + id + "' " ;//+ and_or + " to_char(DATEE) >= '1600-01-01' " + and_or + " to_char(DATEE) <= '2020-12-12' ";
            if (!from_date.equals("")) {
                if (!from_date.equals("yyyy-MM-dd")) {

                    try {
                        reviewQuery = reviewQuery.replace("1600-01-01", from_date);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!to_date.equals("")) {
                if (!from_date.equals("yyyy-MM-dd")) {
                    try {
                        reviewQuery = reviewQuery.replace("2020-12-12", to_date);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (!Stars.equals("")) {
                String sysmbol = review_stars.getSelectedItem().toString();

                reviewQuery = reviewQuery + " AND R.STARS " + sysmbol + " " + Stars + " ";
            }
            if (!Votes.equals("")) {
                String sysmbol = review_votes.getSelectedItem().toString();

                reviewQuery = reviewQuery +  "AND  '" + id + "' in (select distinct VR.Business_ID FROM VOTES_REVIEW VR GROUP BY VR.BUSINESS_ID HAVING SUM(VR.FUNNY+VR.COOL+VR.USEFUL)  " + sysmbol + " " + Votes + " )";

            }

        }
        //back_button.setVisible(true);
        executeAppQuery(reviewQuery, false);

        QueryArea.setText(reviewQuery);

    }//GEN-LAST:event_ResultTableMouseClicked

    private void ExcuteQueryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExcuteQueryButtonActionPerformed
        // TODO add your handling code here:
        String query = buildQuery();
        if (UserSelection.isSelected()) {
            executeAppQuery(query, true);
        } else {
            executeAppQuery(query, false);
        }
    }//GEN-LAST:event_ExcuteQueryButtonActionPerformed

    private void review_to_dateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_review_to_dateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_review_to_dateActionPerformed

    private void review_vote_countActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_review_vote_countActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_review_vote_countActionPerformed

    private void listener_attrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listener_attrActionPerformed
        // TODO add your handling code here:
        
        String query = null;
        String mQ = null;
        ArrayList<String> BusiCateg = new ArrayList();
        for (Component c : CategoryPanel.getComponents()) {
            if (c.getClass().equals(javax.swing.JCheckBox.class)) {
                JCheckBox jcb = (JCheckBox) c;
                if (jcb.isSelected()) {
                    BusiCateg.add(jcb.getText());
                }
            }
        }
         ArrayList<String> BusiSUBCateg = new ArrayList();

        for (Component c : SubCategoryPanel.getComponents()) {
            if (c.getClass().equals(javax.swing.JCheckBox.class)) {
                JCheckBox jcb = (JCheckBox) c;
                if (jcb.isSelected()) {
                    BusiSUBCateg.add(jcb.getText());
                }
            }
        }
          if (BusiSUBCateg.size()==0){
            AttributePanel.removeAll();
            AttributePanel.repaint();
            return;
        }
        String and_or = userOption.getSelectedItem().toString();
        String businessQuery = "(SELECT BUSINESS_ID FROM Business_To_Category WHERE CATEGORY = ";
        if (and_or == "AND") {

                businessQuery = businessQuery + "'" + BusiCateg.get(0).trim() + "')";
                if (BusiCateg.size() > 1) {
                    //isCategoryWhereInlcued = true;
                    //buisnessQuery += " where BUSINESS_ID in ( select BUSINESS_ID from Business_To_Category where CATEGORY in(";
                    for (int i = 1; i < BusiCateg.size(); i++) {
                        businessQuery = businessQuery + "  INTERSECT (select BUSINESS_ID from Business_To_Category where CATEGORY = '" + BusiCateg.get(i).trim() + "')";
                    //if (!(i + 1 == BusiCate.size())) {
                        //  buisnessQuery = buisnessQuery + " , ";
                    }
                }
                String mainQuery = "select distinct ATTRIBUTE_NAME FROM BUSINESS_TO_ATTRIBUTE BA  WHERE BA.BUSINESS_ID in (" + businessQuery+")";
                // buisnessQuery = buisnessQuery + " ) ";
                mQ = mainQuery;

                if (BusiSUBCateg.size() == 0) {
                    //DO NOTHING
                } else {
                    mQ = mQ.replace("WHERE BA.BUSINESS_ID in", " JOIN Business_To_Sub_Category bs ON bs.BUSINESS_ID = BA.BUSINESS_ID WHERE BA.BUSINESS_Id IN");
                    //mQ= mQ.replace("JOIN Business_To_Category bc ON b.BUSINESS_ID = bc.BUSINESS_ID WHERE b.BUSINESS_ID in","JOIN Business_To_Category bc on b.BUSINESS_ID = bc.BUSINESS_ID  JOIN Business_To_Sub_Category bs ON bs.BUSINESS_ID = b.BUSINESS_ID WHERE  b.BUSINESS_Id in");
                    mQ += " AND BA.BUSINESS_ID IN ((select BUSINESS_ID FROM BUSINESS_TO_SUB_CATEGORY where Sub_Category =  '" + BusiSUBCateg.get(0).trim() + "') ";
                    if (BusiSUBCateg.size() > 1) {
                        for (int i = 1; i < BusiSUBCateg.size(); i++) {
                            mQ +=  " INTERSECT (select BUSINESS_ID FROM BUSINESS_TO_SUB_CATEGORY where Sub_Category =  '" + BusiSUBCateg.get(i).trim() + "' )";
                        }
                    }
                    mQ += ")";
                   
                    //return mQ;
                }

        }else{
                     businessQuery = businessQuery + "'" + BusiCateg.get(0).trim() + "')";
                if (BusiCateg.size() > 1) {
                    //isCategoryWhereInlcued = true;
                    //buisnessQuery += " where BUSINESS_ID in ( select BUSINESS_ID from Business_To_Category where CATEGORY in(";
                    for (int i = 1; i < BusiCateg.size(); i++) {
                        businessQuery = businessQuery + "  UNION (select BUSINESS_ID from Business_To_Category where CATEGORY = '" + BusiCateg.get(i).trim() + "')";
                    //if (!(i + 1 == BusiCate.size())) {
                        //  buisnessQuery = buisnessQuery + " , ";
                    }
                }
                    String mainQuery = "select distinct ATTRIBUTE_NAME FROM BUSINESS_TO_ATTRIBUTE BA  WHERE BA.BUSINESS_ID in (" + businessQuery+")";
                // buisnessQuery = buisnessQuery + " ) ";
                mQ = mainQuery;

                if (BusiSUBCateg.size() == 0) {
                    //DO NOTHING
                } else {
                    mQ = mQ.replace("WHERE BA.BUSINESS_ID in", " JOIN Business_To_Sub_Category bs ON bs.BUSINESS_ID = BA.BUSINESS_ID WHERE BA.BUSINESS_Id IN");
                    //mQ= mQ.replace("JOIN Business_To_Category bc ON b.BUSINESS_ID = bc.BUSINESS_ID WHERE b.BUSINESS_ID in","JOIN Business_To_Category bc on b.BUSINESS_ID = bc.BUSINESS_ID  JOIN Business_To_Sub_Category bs ON bs.BUSINESS_ID = b.BUSINESS_ID WHERE  b.BUSINESS_Id in");
                    mQ += " AND BA.BUSINESS_ID IN ((select BUSINESS_ID FROM BUSINESS_TO_SUB_CATEGORY where Sub_Category =  '" + BusiSUBCateg.get(0).trim() + "') ";
                    if (BusiSUBCateg.size() > 1) {
                        for (int i = 1; i < BusiSUBCateg.size(); i++) {
                            mQ +=  " UNION (select BUSINESS_ID FROM BUSINESS_TO_SUB_CATEGORY where Sub_Category =  '" + BusiSUBCateg.get(i).trim() + "' )";
                        }
                    }
                    mQ += ")";
                 
                  
                    //return mQ;
                }
                
        }
     
             //String query2 = null;
   
      

        
        AttributePanel.removeAll();
        AttributePanel.repaint();
System.out.println(mQ);
        try {
            Statement subCatAttStatement = con.createStatement();
            ResultSet res = subCatAttStatement.executeQuery(mQ);
            subCatAttCheckBoxs = new ArrayList();
            while (res.next()) {
                JCheckBox newCheckBox = new JCheckBox();
                newCheckBox.setText(res.getString(1) + "\n");
                subCatAttCheckBoxs.add(newCheckBox);
            }
            AttributePanel.setLayout(new GridLayout(0, 1, 10, 10));
            for (JCheckBox ch : subCatAttCheckBoxs) {
                AttributePanel.add(ch);
                AttributePanel.revalidate();
                AttributePanel.repaint();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_listener_attrActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(YelpSearchApplication.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(YelpSearchApplication.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(YelpSearchApplication.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(YelpSearchApplication.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new YelpSearchApplication().setVisible(true);
            }
        });
    }

    private ArrayList<JCheckBox> subCatCheckBoxs;
    private ArrayList<JCheckBox> subCatAttCheckBoxs;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AttributePanel;
    private javax.swing.JButton BuildQuery;
    private javax.swing.JPanel CategoryPanel;
    private javax.swing.JButton ExcuteQueryButton;
    private javax.swing.JTextArea QueryArea;
    private javax.swing.JTable ResultTable;
    private javax.swing.JPanel SubCategoryPanel;
    private javax.swing.JCheckBox UserSelection;
    private javax.swing.JCheckBox isReview;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox10;
    private javax.swing.JCheckBox jCheckBox11;
    private javax.swing.JCheckBox jCheckBox12;
    private javax.swing.JCheckBox jCheckBox13;
    private javax.swing.JCheckBox jCheckBox14;
    private javax.swing.JCheckBox jCheckBox15;
    private javax.swing.JCheckBox jCheckBox16;
    private javax.swing.JCheckBox jCheckBox17;
    private javax.swing.JCheckBox jCheckBox18;
    private javax.swing.JCheckBox jCheckBox19;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox20;
    private javax.swing.JCheckBox jCheckBox21;
    private javax.swing.JCheckBox jCheckBox22;
    private javax.swing.JCheckBox jCheckBox23;
    private javax.swing.JCheckBox jCheckBox24;
    private javax.swing.JCheckBox jCheckBox25;
    private javax.swing.JCheckBox jCheckBox26;
    private javax.swing.JCheckBox jCheckBox27;
    private javax.swing.JCheckBox jCheckBox28;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JCheckBox jCheckBox7;
    private javax.swing.JCheckBox jCheckBox8;
    private javax.swing.JCheckBox jCheckBox9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton listener_attr;
    private javax.swing.JButton listener_subc;
    private javax.swing.JTextField review_from_date;
    private javax.swing.JTextField review_star_count;
    private javax.swing.JComboBox review_stars;
    private javax.swing.JTextField review_to_date;
    private javax.swing.JTextField review_vote_count;
    private javax.swing.JComboBox review_votes;
    private javax.swing.JComboBox userAvgStars;
    private javax.swing.JTextField userAvgStars_value;
    private javax.swing.JTextField userDate;
    private javax.swing.JComboBox userFriends;
    private javax.swing.JTextField userFriends_value;
    private javax.swing.JComboBox userOption;
    private javax.swing.JComboBox userReviewCount;
    private javax.swing.JTextField userReviewCount_value;
    // End of variables declaration//GEN-END:variables
}
