package org.onebusaway.forwarder.service;

import org.onebusaway.forwarder.sql.connection.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Singleton
public class ConfigurationService {

    private static final Logger _log = LoggerFactory.getLogger(ConfigurationService.class);
    Properties properties = new Properties();

    public ConfigurationService(){
        try {
            properties.load(new FileInputStream("config.properties"));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            _log.error("Config file is not found: " + e.getMessage());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            _log.error("Failed to read connection properties: " + e.getMessage());
        }
        _log.info(properties.getDatabaseName());
    }

    public Properties getConfigProperties(){
        return properties;
    }


}
