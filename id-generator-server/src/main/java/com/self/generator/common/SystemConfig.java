package com.self.generator.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 系统配置
 * Created by shaojieyue on 11/8/15.
 */
public class SystemConfig{
    private static final Properties properties = new Properties();

    private static final String GENERATOR_CONFIG_PROPERTIES = "generator-config.properties";

    static {
        final InputStream resourceAsStream =
            SystemConfig.class.getClassLoader().getResourceAsStream(GENERATOR_CONFIG_PROPERTIES);
        if(resourceAsStream==null){
            throw new IllegalStateException(GENERATOR_CONFIG_PROPERTIES+" not found");
        }
        try{
            properties.load(resourceAsStream);
        }catch(IOException e){
            throw new IllegalStateException("load "+GENERATOR_CONFIG_PROPERTIES+" fail.",e);
        }
    }

    /**
     * zk 集群地址
     */
    public static final String ZK_CONNECTION_STRING = properties.getProperty("zk.connection.string");
}
