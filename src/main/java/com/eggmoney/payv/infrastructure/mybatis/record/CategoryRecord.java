package com.eggmoney.payv.infrastructure.mybatis.record;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryRecord {
	
    private String categoryId;
    private String ledgerId;
    private String name;
    private String isSystemCategory;	// ('Y', 'N')
    private String parentId;
    private Integer sortOrder;
    private String isDeleted;			// ('Y', 'N')
}
