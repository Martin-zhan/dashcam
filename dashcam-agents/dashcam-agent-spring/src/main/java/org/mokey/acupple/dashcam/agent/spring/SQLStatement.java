package org.mokey.acupple.dashcam.agent.spring;

import org.mokey.acupple.dashcam.agent.spring.utils.PrettyFormat;
import org.mokey.acupple.dashcam.agent.spring.utils.SQLFormatter;

import java.util.Map;

/**
 * Created by Forest on 2015/12/16.
 */
public class SQLStatement {
    private static SQLFormatter formatter = new SQLFormatter();

    private String sqlId;
    private String sql;
    private Map<String, String> params;
    private long time;

    public String getSqlId() {
        return sqlId;
    }

    public void setSqlId(String sqlId) {
        this.sqlId = sqlId;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return this.getSql() + System.lineSeparator() + "time: " + this.time + "(ms)";
    }

    public String output(Object returnValue){
        StringBuffer sb = new StringBuffer();
        sb.append(formatter.prettyPrint(this.getSql()).toString()).append(System.lineSeparator())
                .append("time: " + this.getTime() + "(ms)").append(System.lineSeparator())
                .append("return: ");
        if (returnValue == null) {
            sb.append("null");
        }else {
            sb.append(PrettyFormat.toString(returnValue));
        }
        return sb.toString();
    }
}
