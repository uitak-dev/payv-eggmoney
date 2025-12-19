package com.eggmoney.payv.infrastructure.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TimeMapper {

	// health check( Mybatis + DB )
	String now(); 
}
