package org.mokey.acupple.dashcam.agent.spring;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.text.DateFormat;
import java.util.*;

/**
 * Created by Forest on 2015/12/16.
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
        @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class })})
public class MybatisInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        TraceContext context = TraceContext.create(invocation);
        Tracer tracer = new Tracer(context);
        Object returnValue = null;
        SQLStatement sqlStatement = getSql(invocation);
        long start = System.currentTimeMillis();
        try{
            returnValue = invocation.proceed();
            sqlStatement.setTime(System.currentTimeMillis() - start);
            return  returnValue;
        }catch (Throwable ex){
            sqlStatement.setTime(System.currentTimeMillis() - start);
            tracer.error(sqlStatement.output(returnValue), sqlStatement.getParams(), ex);
            throw ex;
        }finally {
            sqlStatement.setTime(System.currentTimeMillis() - start);
            tracer.log(sqlStatement.output(returnValue), sqlStatement.getParams());
            tracer.stop();
        }
    }

    private SQLStatement getSql(Invocation invocation){
        SQLStatement sqlStatement = new SQLStatement();
        try {
            Object parameter = null;
            if (invocation.getArgs().length > 1) {
                parameter = invocation.getArgs()[1];
            }
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            String sqlId = mappedStatement.getId();
            BoundSql boundSql = mappedStatement.getBoundSql(parameter);
            Configuration configuration = mappedStatement.getConfiguration();
            sqlStatement.setSqlId(sqlId);
            sqlStatement.setParams(new HashMap<String, String>());

            Object parameterObject = boundSql.getParameterObject();
            List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
            String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
            int index = 0;
            if (parameterMappings.size() > 0 && parameterObject != null) {
                TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
                if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                    String pv = getParameterValue(parameterObject);
                    sql = sql.replaceFirst("\\?", pv);
                    sqlStatement.getParams().put("param" + index++, pv);
                } else {
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    for (ParameterMapping parameterMapping : parameterMappings) {
                        String propertyName = parameterMapping.getProperty();
                        if (metaObject.hasGetter(propertyName)) {
                            Object obj = metaObject.getValue(propertyName);
                            String pv = getParameterValue(obj);
                            sql = sql.replaceFirst("\\?", pv);
                            sqlStatement.getParams().put("param" + index++, pv);
                        } else if (boundSql.hasAdditionalParameter(propertyName)) {
                            Object obj = boundSql.getAdditionalParameter(propertyName);
                            String pv = getParameterValue(obj);
                            sql = sql.replaceFirst("\\?", getParameterValue(obj));
                            sqlStatement.getParams().put("param" + index++, pv);
                        }
                    }
                }
            }
            sqlStatement.setSql(sql);
        }catch (Throwable ex){}
        return  sqlStatement;
    }

    private static String getParameterValue(Object obj) {
        String value;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(new Date()) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }

        }
        return value;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {}
}
