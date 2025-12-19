package com.eggmoney.payv.infrastructure.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.eggmoney.payv.infrastructure.mybatis.record.UserRecord;

/**
 * User MyBatis Mapper 인터페이스 - 수정된 버전 파라미터 이름을 XML과 일치시켜 BindingException 해결
 * @author 강기범
 */
@Mapper
public interface UserMapper {

	UserRecord selectById(@Param("userId") String userId);
    UserRecord selectByEmail(@Param("email") String email);
    int insert(UserRecord rec);
    int update(UserRecord rec);
}
