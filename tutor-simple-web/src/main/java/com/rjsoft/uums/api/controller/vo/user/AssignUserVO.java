package com.rjsoft.uums.api.controller.vo.user;

import com.rjsoft.common.vo.BaseVO;
import com.rjsoft.uums.facade.user.entity.UmsPerson;
import com.rjsoft.uums.facade.user.entity.UmsUser;

public class AssignUserVO implements BaseVO {
	
	private String id;
	
	private String nickname;
	
	private String username;
	
	private String name;
	
	private String sn;
	
	@Override
	public void convertPOToVO(Object poObj) {
		
		if(poObj != null){
			if(poObj instanceof UmsUser){
				UmsUser user = (UmsUser)poObj;
				this.id = user.getId();
				this.nickname = user.getNickname();
				this.username = user.getUsername();
				UmsPerson person = user.getPerson();
				if(person != null){
					this.name = person.getName();
					this.sn = person.getSn();
				}
			}
		}
	}

	public String getNickname() {
		return nickname;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}
}