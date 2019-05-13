package org.onebusaway.forwarder.sql;

import org.apache.commons.dbcp2.*;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.onebusaway.forwarder.service.ConfigurationService;
import org.onebusaway.forwarder.sql.connection.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.sql.*;

@Singleton
public class DatabaseSource {

    private static final Logger _log = LoggerFactory.getLogger(DatabaseSource.class);

    @Inject
    private ConfigurationService _configService;

    private DataSource _dataSource = null;

    @PostConstruct
    public void start() throws ClassNotFoundException {
        Properties configProperties = _configService.getConfigProperties();
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        String connectionURI = getConnectionURI(configProperties);
        _dataSource = setupDataSource(connectionURI);
    }

    public static String getConnectionURI(Properties connProps){
        String connString = "jdbc:sqlserver://" + connProps.getHost();

        if(connProps.getPortNumber() != null){
            connString += ":" + connProps.getPortNumber();
        }
        connString += ";";

        if(StringUtils.isNotBlank(connProps.getDatabaseName())){
            connString += "databaseName=" + connProps.getDatabaseName() + ";";
        }
        if(StringUtils.isNotBlank(connProps.getInstanceName())){
            connString += "instanceName=" + connProps.getInstanceName() + ";";
        }
        if(StringUtils.isNotBlank(connProps.getUser())){
            connString += "user=" + connProps.getUser() + ";";
        }
        if(StringUtils.isNotBlank(connProps.getPassword())){
            connString += "password=" + connProps.getPassword() + ";";
        }

        _log.info("Connection String: "+ connString);

        return connString;
    }

    public static DataSource setupDataSource(String connectURI) {
        //
        // First, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string passed in the command line
        // arguments.
        //
        ConnectionFactory connectionFactory =
                new DriverManagerConnectionFactory(connectURI,null);

        //
        // Next we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
        PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(connectionFactory, null);

        //
        // Now we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
        ObjectPool<PoolableConnection> connectionPool =
                new GenericObjectPool<>(poolableConnectionFactory);

        // Set the factory's pool property to the owning pool
        poolableConnectionFactory.setPool(connectionPool);

        //
        // Finally, we create the PoolingDriver itself,
        // passing in the object pool we created.
        //
        PoolingDataSource<PoolableConnection> dataSource =
                new PoolingDataSource<PoolableConnection>(connectionPool);


        return dataSource;
    }

    public Connection getConnection() throws SQLException {
        return _dataSource.getConnection();
    }

}
