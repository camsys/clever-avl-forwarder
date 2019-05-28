package org.onebusaway.forwarder.dao;

import org.apache.commons.dbutils.DbUtils;
import org.onebusaway.forwarder.models.CleverAvlData;
import org.onebusaway.forwarder.service.ConfigurationService;
import org.onebusaway.forwarder.sql.DatabaseSource;
import org.onebusaway.forwarder.sql.ResultSetDecrypt;
import org.onebusaway.forwarder.sql.connection.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.onebusaway.forwarder.util.ConfigUtil.*;

@Singleton
public class CleverAvlDao {

    private static final Logger _log = LoggerFactory.getLogger(CleverAvlDao.class);

    @Inject
    private DatabaseSource _databaseSource;

    @Inject
    ConfigurationService _configurationService;

    private int _queryTimeout =30;

    private String cleverAvlQuery = null;

    private static final String DEFAULT_CLEVER_AVL_QUERY =
            "SELECT " +
            "DISTINCT [vehicle_position_date_time] as time" +
            ",RTRIM([vehicle_id]) as vehicle_id" +
            ",[loc_x] as longitude" +
            ",[loc_y] as latitude" +
            ",[heading] as heading" +
            ",[current_speed] as speed" +
            ",RTRIM([fix]) as fix" +
            ",RTRIM([block_id]) as clever_block_id" +
            ",RTRIM([b1].[BlockID]) as gtfs_block_id" +
            ",[b1].[VersionID] as block_calendar_version" +
            ",RTRIM([driver_id]) as driver_id" +
            ",[t].[DayMapID] as cc_calendarID" +
            ",[d].[DayMapID] as day_map_id" +
            ",[d].DayMap as encoded_calendar_string " +
            "FROM [CleverCAD].[dbo].[v_CleverReports_RealTimeAVLDataView] as avl " +
            "LEFT JOIN [CleverCAD].[dbo].[BT_Block] as b1 on (avl.block_id = b1.BTBlockID) " +
            "LEFT JOIN [CleverCAD].[dbo].[BT_Trip] as t on (b1.BlockID = t.BlockID) " +
            "LEFT JOIN [CleverCAD].[dbo].[BT_DayMap] as d on (t.DayMapID = d.DayMapID) " +
            "WHERE " +
            "(" +
            "(b1.VersionID = (Select MAX(VersionID) from [CleverCAD].[dbo].[BT_Version] where ActivationDTS <= GETDATE()) " +
            "AND " +
            "SUBSTRING(DayMap, (DATEPART(dw, GETDATE()+@@DATEFIRST-1)%7+1),1) = '1') " +
            "OR " +
            "b1.VersionID = (Select MAX(VersionID) from [CleverCAD].[dbo].[BT_CalendarEvents] where convert(date, CalendarDTS) = convert(date, GETDATE()))" +
            ") " +
            "AND t.VersionID = b1.VersionID " +
            "ORDER BY vehicle_position_date_time DESC";


    @PostConstruct
    public void start() {
        Properties configProperties = _configurationService.getConfigProperties();
        _queryTimeout = getConfigValue(configProperties.getQueryTimeout(), 30);
    }

    public List<CleverAvlData> getCleverAvlData() throws Exception {
        String query = getQuery();

        Connection conn = null;
        ResultSet rs = null;
        Statement stmt = null;

        try {
            conn =_databaseSource.getConnection();
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setQueryTimeout(_queryTimeout);
            _log.info("Executing SELECT query...");
            rs = stmt.executeQuery(query);

            if(rs == null){
                _log.warn("ResultSet for SELECT query is null");
                throw new Exception("ResultSet for SELECT query is null");
            }

            ResultSetDecrypt rsd = new ResultSetDecrypt(rs);
            ArrayList<CleverAvlData> avlData = rsd.decrypt();
            return avlData;
        } catch (SQLException e) {
            _log.error("Failed to execute SELECT query: " + e.getMessage(), e);
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(conn);
        }
        return Collections.emptyList();
    }

    private String getQuery() {
        if(cleverAvlQuery == null){
            return DEFAULT_CLEVER_AVL_QUERY;
        }
        return cleverAvlQuery;
    }

    public String getCleverAvlQuery() {
        return cleverAvlQuery;
    }

    public void setCleverAvlQuery(String cleverAvlQuery) {
        this.cleverAvlQuery = cleverAvlQuery;
    }
}

