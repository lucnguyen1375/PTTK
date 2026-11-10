package vn.ptit.pttkm3.config;

import com.mysql.cj.jdbc.MysqlDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

public final class DatabaseDataSource {
    private DatabaseDataSource() {
    }

    public static DataSource create(AppConfig config) throws SQLException {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setServerName(config.databaseHost());
        dataSource.setPort(config.databasePort());
        dataSource.setDatabaseName(config.databaseName());
        dataSource.setUser(config.databaseUser());
        dataSource.setPassword(config.databasePassword());
        dataSource.setConnectTimeout(config.databaseConnectTimeoutMs());
        dataSource.setSocketTimeout(config.databaseConnectTimeoutMs());
        dataSource.setServerTimezone("Asia/Ho_Chi_Minh");
        dataSource.setCharacterEncoding("UTF-8");
        return dataSource;
    }
}
