package com.eggmoney.payv.infrastructure.mybatis.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.eggmoney.payv.domain.shared.id.LongId;

public abstract class AbstractLongIdTypeHandler<T extends LongId> extends BaseTypeHandler<T> {

	@Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) 
    		throws SQLException {
        ps.setLong(i, parameter.value());
    }
	
    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        long v = rs.getLong(columnName);
        return rs.wasNull() ? null : wrap(v);
    }
    
    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        long v = rs.getLong(columnIndex);
        return rs.wasNull() ? null : wrap(v);
    }
    
    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        long v = cs.getLong(columnIndex);
        return cs.wasNull() ? null : wrap(v);
    }
    
    protected abstract T wrap(long raw);
}
