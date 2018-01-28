package com.derongan.minecraft.mineinabyss.Database;

import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtils {
    protected static String JDBC_SQLITE_STRING = "jdbc:sqlite:";

    public static Connection getDBConnection(String dBFilePath){
        Connection conn = null;
        try{
            String url = JDBC_SQLITE_STRING + dBFilePath;
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            //todo better exceptions
            throw new RuntimeException(e);
        }

        return conn;
    }

    public static void closeConnection(Connection connection){
        DbUtils.closeQuietly(connection);
    }

}
