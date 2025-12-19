package com.eggmoney.payv.infrastructure.mybatis.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.eggmoney.payv.domain.shared.id.StringId;

public abstract class AbstractStringIdTypeHandler<T extends StringId> extends BaseTypeHandler<T> {

	@Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) 
    		throws SQLException {
        ps.setString(i, parameter.value());
    }
	
    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String v = rs.getString(columnName);
        return v == null ? null : wrap(v);
    }
    
    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String v = rs.getString(columnIndex);
        return v == null ? null : wrap(v);
    }
    
    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String v = cs.getString(columnIndex);
        return v == null ? null : wrap(v);
    }
    
    protected abstract T wrap(String raw);
}
