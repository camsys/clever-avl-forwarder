package org.onebusaway.forwarder.service;

import org.onebusaway.forwarder.sql.connection.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.onebusaway.forwarder.util.ConfigUtil.*;

@Singleton
public class ConfigurationService {

    private String _configFile;

    private static final Logger _log = LoggerFactory.getLogger(ConfigurationService.class);
    Properties properties = new Properties();

    @PostConstruct
    public void start(){
        try {
            String configFile = getConfigValue(_configFile, "./config.properties");
            properties.load(new FileInputStream(configFile));
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

    public void setConfigFile(String configFile){
        _configFile = configFile;
    }


}
