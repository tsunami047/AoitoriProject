package io.aoitori043.aoitoriproject.database.point.mysql;

import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.aoitoriproject.database.mysql.HikariConnectionPool;
import io.aoitori043.aoitoriproject.database.point.DataAccess;
import io.aoitori043.aoitoriproject.database.point.PointManager;
import io.aoitori043.aoitoriproject.database.point.DataType;
import io.aoitori043.aoitoriproject.database.point.ObjectDataAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-09-20  22:33
 * @Description: ?
 */
public class MySQLExecutor implements DatabaseAccess {

    private static final String AP_VARIABLES =
            "CREATE TABLE ap_var_entry (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "player_name VARCHAR(255) NOT NULL," +
            "data_name VARCHAR(255) NOT NULL," +
            "data_value TEXT NOT NULL," +
            "UNIQUE KEY unique_player_data (player_name, data_name)," +  // 唯一组合
            "INDEX idx_player_name (player_name))";

    public String getEntry(String playerName, String dataName) {
        String sql = "SELECT player_name, data_name, data_value FROM ap_var_entry WHERE player_name = ? AND data_name = ?";
        try (Connection conn = HikariConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            pstmt.setString(2, dataName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String value = rs.getString("data_value");
                    AoitoriProject.plugin.getLogger().warning("access players offline: "+playerName + " "+dataName+" data： "+value);
                    return value;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void insertEntry(String playerName, String dataName, String dataValue) {
        String sql = "INSERT INTO ap_var_entry (player_name, data_name, data_value) VALUES (?, ?, ?)";
        try (Connection conn = HikariConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            pstmt.setString(2, dataName);
            pstmt.setString(3, dataValue);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteEntry(String playerName, String dataName) {
        String sql = "DELETE FROM ap_var_entry WHERE player_name = ? AND data_name = ?";

        try (Connection conn = HikariConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, playerName);
            pstmt.setString(2, dataName);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateEntry(String playerName, String dataName, String newDataValue) {
        String sql = "UPDATE ap_var_entry SET data_value = ? WHERE player_name = ? AND data_name = ?";

        try (Connection conn = HikariConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newDataValue);
            pstmt.setString(2, playerName);
            pstmt.setString(3, dataName);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ConcurrentHashMap<String, String> getVariablesByPlayerName(String playerName) {
        String sql = "SELECT data_name, data_value FROM ap_var_entry WHERE player_name = ?";
        ConcurrentHashMap<String,String> tempMap = new ConcurrentHashMap<>();
        try (Connection conn = HikariConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String dataName = rs.getString("data_name");
                    String dataValue = rs.getString("data_value");
                    DataAccess dataAccess = PointManager.map.get(dataName);
                    DataType varType = dataAccess.getVarType();
                    switch (varType) {
                        case OBJECT_DATA:{
                            ObjectDataAccess access = (ObjectDataAccess) dataAccess;
//                            tempMap.put(dataName,access.deserialize(dataValue));
                            break;
                        }
                        default:{
                            tempMap.put(dataName,dataValue);
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tempMap;
    }

}
