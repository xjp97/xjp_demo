package com.ccq.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * @author xujunping
 * @date 2019/10/31
 */
public class PropertiesSupport {
    private Properties props;

    public PropertiesSupport(Properties props) {
        this.props = props;
    }

    public PropertiesSupport(InputStream inputStream) throws IOException {
        this.props = new Properties();
        this.props.load(new InputStreamReader(inputStream));
    }

    public boolean getBoolean(String name) {
        return this.getBoolean(name, false);
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        String prop = this.getString(name);
        return prop == null ? defaultValue : "true".equalsIgnoreCase(prop) || "yes".equalsIgnoreCase(prop);
    }

    public double getDouble(String name, double defaultValue) {
        String prop = this.getString(name);
        if (prop != null) {
            try {
                return Double.parseDouble(prop);
            } catch (Exception var6) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public int getInteger(String name, int defaultValue) {
        String prop = this.getString(name);
        if (prop != null) {
            try {
                return Integer.parseInt(prop);
            } catch (Exception var5) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public long getLong(String name, long defaultValue) {
        String prop = this.getString(name);
        if (prop != null) {
            try {
                return Long.parseLong(prop);
            } catch (Exception var6) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public String[] getArrayString(String name, String delimiter) {
        String prop = this.getString(name);
        return prop != null ? StringUtils.split(prop, delimiter) : null;
    }

    public String getString(String name) {
        String prop = null;

        try {
            prop = System.getProperty(name);
        } catch (SecurityException var4) {
        }

        return prop == null ? this.props.getProperty(name) : prop;
    }

    public String getString(String name, String defaultValue) {
        String prop = this.getString(name);
        return prop == null ? defaultValue : prop;
    }
}
