/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modulgame;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author Fauzan
 */
public class dbConnection {
    public static Connection con;
    public static Statement stm;
    
    public void connect(){//untuk membuka koneksi ke database
        try {
            String url ="jdbc:mysql://localhost/db_gamepbo";
            String user="root";
            String pass="";
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(url,user,pass);
            stm = con.createStatement();
            System.out.println("koneksi berhasil;");
        } catch (Exception e) {
            System.err.println("koneksi gagal" +e.getMessage());
        }
    }
    
    public DefaultTableModel readTable(){
        
        DefaultTableModel dataTabel = null;
        try{
            Object[] column = {"No", "Username", "Score"};
            connect();
            dataTabel = new DefaultTableModel(null, column);
            
            String sql = "Select * from highscore order by score DESC";
            ResultSet res = stm.executeQuery(sql);
            
            int no = 1;
            while(res.next()){
                Object[] hasil = new Object[3];
                hasil[0] = no;
                hasil[1] = res.getString("Username");
                hasil[2] = res.getString("Score");
                no++;
                dataTabel.addRow(hasil);
            }
        }catch(Exception e){
            System.err.println("Read gagal " +e.getMessage());
        }
        
        return dataTabel;
    }
    
    public void uploadScore(String username, int score, int update){
        connect();
        // Untuk insert
        if(update == 0){
            String sql = "INSERT INTO highscore VALUES(NULL, ?, ?)";
            PreparedStatement pstmt;
            try {
                pstmt = con.prepareStatement(sql);
                pstmt.setString(1, username);
                pstmt.setString(2, String.valueOf(score));
                pstmt.executeUpdate();
            } catch (Exception e) {
                System.err.println("Upload gagal " +e.getMessage());
            }
        }
        // Untuk update
        else{
            String sql = "UPDATE highscore SET Score = ? WHERE username = ?";
            PreparedStatement pstmt;
            try {
                pstmt = con.prepareStatement(sql);
                pstmt.setString(1, String.valueOf(score));
                pstmt.setString(2, username);
                pstmt.executeUpdate();
            } catch (Exception e) {
                System.err.println("Upload gagal " +e.getMessage());
            }
        }        
    }
    
    // Mereturn nilai score sebelumnya suatu username
    public int getScore(String username){
        connect();
        try{
            String sql = "Select * from highscore where username = '" + username + "'";
            //System.out.println(sql);
            ResultSet res = stm.executeQuery(sql);
            if(res.next()){
                String stemp = res.getString("Score");
                return Integer.parseInt(stemp);
            }
            // Jika username tidak ditemukan
            else{
                System.out.println("Empty Set");
                return -1;
            }
            
        }catch(Exception e){
            System.err.println("Read gagal " +e.getMessage());
        }
        return -1;
    }
}
