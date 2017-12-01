package com.rjsoft.uums.api.controller.vo.dict;

import com.rjsoft.common.vo.BaseVO;
import com.rjsoft.uums.facade.dict.entity.UmsDictionaryType;

public class DictTypeForSelectVO implements BaseVO {
	
	private String id;
	
	private String name;
	
	@Override
	public void convertPOToVO(Object poObj) {
		
		if(poObj != null){
			UmsDictionaryType dictType = null;
			if(poObj instanceof UmsDictionaryType){
				dictType = (UmsDictionaryType)poObj;
			}
			if(dictType != null){
				this.id = dictType.getId();
				this.name = dictType.getValue();
			}
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}