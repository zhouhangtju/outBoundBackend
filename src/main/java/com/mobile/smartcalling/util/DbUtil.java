package com.mobile.smartcalling.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Component
public class DbUtil {

    @Autowired
    private Environment env;

    public void addPartition(String table, String partition, String ds) {
        // 执行DDL语句创建分区
        String ddl = String.format("alter table %s ADD PARTITION(PARTITION %s values in ('%s'))", table, partition, ds);
        log.info("===sql: {}",ddl);
        dbHandle(ddl);
    }

    public void createPartition(String table, String partition, String ds) {
        // 执行DDL语句创建分区
        //String ddl = String.format("alter table %s ADD PARTITION(PARTITION %s values in ('%s'))", table, partition, ds);
        String ddl = String.format("alter table %s PARTITION BY LIST COLUMNS(ds)(PARTITION %s values in ('%s'))", table, partition, ds);
        log.info("===sql: {}",ddl);
        dbHandle(ddl);
    }

    public void AaddPartition(String table, String startDateRange1, String endDateRange1, String startDateRange2, String endDateRange2) {
        String partitionName = String.format("p%s_%s_p%s_%s",
                startDateRange1.replace("-", ""),
                endDateRange1.replace("-", ""),
                startDateRange2.replace("-", ""),
                endDateRange2.replace("-", ""));

        // 假设分区的范围是基于analysis_start_time1字段，并且您想要的分区是根据startDateRange1到endDateRange1
        // 您需要确保这些日期是analysis_start_time1列可以接受的范围
        String ddl = String.format(
                "CREATE TABLE IF NOT EXISTS %s PARTITION OF %s FOR VALUES FROM ('%s') TO ('%s')",
                partitionName, table, startDateRange1, endDateRange1
        );

        dbHandle(ddl); // dbHandle 是执行SQL的方法
    }

    public void DdelPartition(String partition) {
        String ddl = String.format("DROP TABLE IF EXISTS %s", partition);
        dbHandle(ddl); // dbHandle 是执行SQL的方法
    }
    public void delPartition(String table, String ds) {
        // 执行DDL语句创建分区
        //String ddl = String.format("alter table %s DROP PARTITION if exists %s;", table, ds);
        String ddl = String.format("alter table %s DROP PARTITION %s;", table, ds);
        log.info("===sql: {}",ddl);
        dbHandle(ddl);
    }

    public void dbHandle(String sql) {
        // JDBC连接信息
        String jdbcUrl = env.getProperty("spring.datasource.druid.url");
        String username = env.getProperty("spring.datasource.druid.username");
        String password = env.getProperty("spring.datasource.druid.password");
        log.info("===db info:{},{},{}", jdbcUrl, username,password);
        Connection connection = null;
        Statement statement = null;

        try {
            // 建立数据库连接
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            // 创建Statement对象
            statement = connection.createStatement();
            // 执行DDL语句创建分区
            statement.execute(sql);
            log.info("sql操作成功: {}", sql);
        } catch (SQLException e) {
            log.error("sql execute error", e);
        } finally {
            // 关闭Statement和Connection
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("close connection error", e);
            }
        }
    }

    public void selectIntoPmlte() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String neeTable = "pm_lte_" + sdf.format(new Date());
        String ori = "select * into %s from pm_lte where cgi in ('460-00-183792-193','460-00-325793-130','460-00-413349-1'," +
                "'460-00-744860-7','460-00-185555-196','460-00-318933-139','460-00-186716-195','460-00-762517-1'," +
                "'460-00-769065-1','460-00-722618-9')";
        String sql = String.format(ori, neeTable);
        log.info("===select into sql: {}",sql);
        dbHandle(sql);
        log.info("===select into pm_lte success");
    }


    /**
     * 分区数据拷贝
     */
    public void copyDataForPartition(String table, String from, String to) {
        String toTable = table + "_1_prt_p" + to;
        String sql = String.format("insert into %s select * from %s where ds = '%s'", toTable, table, from);
        log.info("===sql: {}", sql);
        dbHandle(sql);
    }


}
