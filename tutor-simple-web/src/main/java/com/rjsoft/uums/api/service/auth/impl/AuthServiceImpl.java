package com.rjsoft.uums.api.service.auth.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.rjsoft.common.entity.enums.AvailableEnum;
import com.rjsoft.common.model.search.Searchable;
import com.rjsoft.common.vo.ListVO;
import com.rjsoft.uums.api.constant.FaySysRoleConstant;
import com.rjsoft.uums.api.constant.FayUserConstant;
import com.rjsoft.uums.api.controller.vo.common.ControllerResourceForAuthVO;
import com.rjsoft.uums.api.controller.vo.common.MenuResourceForAuthVO;
import com.rjsoft.uums.api.service.auth.AuthService;
import com.rjsoft.uums.api.service.role.RoleService;
import com.rjsoft.uums.api.util.tree.service.ITreeNode;
import com.rjsoft.uums.api.util.tree.service.impl.Tree;
import com.rjsoft.uums.facade.Resource;
import com.rjsoft.uums.facade.app.entity.UmsApp;
import com.rjsoft.uums.facade.app.service.UmsAppFacade;
import com.rjsoft.uums.facade.auth.entity.UmsAcl;
import com.rjsoft.uums.facade.auth.service.UmsAuthFacade;
import com.rjsoft.uums.facade.org.service.UmsOrgFacade;
import com.rjsoft.uums.facade.resource.entity.UmsControllerResources;
import com.rjsoft.uums.facade.resource.entity.UmsMenuResources;
import com.rjsoft.uums.facade.resource.service.UmsControllerResFacade;
import com.rjsoft.uums.facade.resource.service.UmsMenuResFacade;
import com.rjsoft.uums.facade.user.entity.UmsUser;
import com.rjsoft.uums.facade.user.service.UmsUserFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {
	
	@javax.annotation.Resource
	private UmsOrgFacade umsOrgFacade;
	
	@Autowired
	private RoleService roleService;

	@javax.annotation.Resource
	private UmsAppFacade umsAppFacade;

	@javax.annotation.Resource
	private UmsMenuResFacade umsMenuResFacade;

	@javax.annotation.Resource
	private UmsControllerResFacade umsControllerResFacade;

	@javax.annotation.Resource
	private UmsUserFacade umsUserFacade;

	@javax.annotation.Resource
	private UmsAuthFacade umsAuthFacade;

	@Override
	public JSONObject find(Object currentUsername, String appId, String menuId, String dataId, String pType, String rType) {
		if(Resource.RESOURCE_MENU.equals(rType)){
			return findMenus(currentUsername, appId, dataId, pType);
		}else if(Resource.RESOURCE_CONTROLLER.equals(rType)){
			return findControllers(currentUsername, appId, menuId, dataId, pType);
		}
		return null;
	}
	
	private JSONObject findMenus(Object currentUsername, String appId, String dataId, String pType){
		Set<UmsMenuResources> umsMenuResources = null;
		List<UmsMenuResources> menuList = null;
		ListVO<MenuResourceForAuthVO> listVO = null;
		List<String> forbiddenMenuIds = new ArrayList<>();
		Object data = null;
		JSONObject jsonObj = new JSONObject();
		if(appId == null || currentUsername == null){
			jsonObj.put("menuList", new ArrayList<>());
			jsonObj.put("checkedList", new String[0]);
			jsonObj.put("forbiddenMenuIds", forbiddenMenuIds);
		}else{
			String username = (String)currentUsername;
			String appSn = null;
			UmsApp app = umsAppFacade.getById(appId);
			if(app != null){
				appSn = app.getSn();
			}
			if(Arrays.asList(FayUserConstant.SUPER_MANAGE_USERNAME).contains(username) || roleService.validate(username, FaySysRoleConstant.SUPER_MANAGE_ROLE_SN)){
				Searchable searchable = Searchable.newSearchable();
				searchable.addSearchParam("isAvailable_eq", AvailableEnum.TRUE.getValue());
				searchable.addSearchParam("application.sn_eq", appSn);
				searchable.addSort(Direction.ASC, "menuOrder");
				menuList = umsMenuResFacade.listUmsMenuResources(searchable);
				listVO = new ListVO<>(menuList, MenuResourceForAuthVO.class);
			}else{
				UmsUser user = umsUserFacade.findByUsername(username);
				umsMenuResources = umsMenuResFacade.getMenusByUserAndAppSn(user, appSn, AvailableEnum.TRUE.getValue());
				listVO = new ListVO<>(umsMenuResources, MenuResourceForAuthVO.class);
			}
			
			if(listVO.getVoList().isEmpty()){
				jsonObj.put("menuList", data);
				jsonObj.put("checkedList", new String[0]);
				jsonObj.put("forbiddenMenuIds", forbiddenMenuIds);
			}else{
				List<ITreeNode> list = new ArrayList<>();
				list.addAll(listVO.getVoList());
				Tree tree = new Tree(list);
				SimplePropertyPreFilter filter = new SimplePropertyPreFilter(); // 构造方法里，也可以直接传需要序列化的属性名字
		        filter.getExcludes().add("parent");
		        filter.getExcludes().add("allChildren");
		        String treeJsonString = JSONObject.toJSONString(tree.getRoot(), filter);
		        data = JSONObject.parse(treeJsonString);
				List<UmsAcl> aclList = umsAuthFacade.findAcl(dataId, pType, Resource.RESOURCE_MENU);
				int size = aclList.size();
				String[] menuIds = new String[size];
				for(int i = 0; i < size; i++){
					UmsAcl acl = aclList.get(i);
					String rid = acl.getRid();
					menuIds[i] = rid;
					if(acl.getAclState() != null && acl.getAclState() < 0) forbiddenMenuIds.add(rid);
				}
				jsonObj.put("menuList", data);
				jsonObj.put("checkedList", menuIds);
				jsonObj.put("forbiddenMenuIds", forbiddenMenuIds);
			}
		}
		return jsonObj; 
	}
	
	private JSONObject findControllers(Object currentUsername, String appId, String menuId, String dataId, String pType){
		Set<UmsControllerResources> umsControllerResources = null;
		List<UmsControllerResources> controllerList = null;
		ListVO<ControllerResourceForAuthVO> listVO = null;
		List<String> forbiddenControllerIds = new ArrayList<>();
		Object data = null;
		JSONObject jsonObj = new JSONObject();
		if(appId == null || currentUsername == null){
			jsonObj.put("controllerList", new ArrayList<>());
			jsonObj.put("checkedList", new String[0]);
			jsonObj.put("forbiddenControllerIds", forbiddenControllerIds);
		}else{
			String username = (String)currentUsername;
			String appSn = null;
			UmsApp app = umsAppFacade.getById(appId);
			if(app != null){
				appSn = app.getSn();
			}
			if(Arrays.asList(FayUserConstant.SUPER_MANAGE_USERNAME).contains(username) || roleService.validate(username, FaySysRoleConstant.SUPER_MANAGE_ROLE_SN)){
				Searchable searchable = Searchable.newSearchable();
				searchable.addSearchParam("isAvailable_eq", AvailableEnum.TRUE.getValue());
				searchable.addSearchParam("application.sn_eq", appSn);
				searchable.addSearchParam("menu.id_eq", menuId);
				searchable.addSort(Direction.ASC, "controllerOrder");
				controllerList = umsControllerResFacade.listUmsControllerRes(searchable);
				listVO = new ListVO<>(controllerList, ControllerResourceForAuthVO.class);
			}else{
				UmsUser user = umsUserFacade.findByUsername(username);
				umsControllerResources = umsControllerResFacade.getControllerResByUserAndAppSn(user, appSn, menuId, AvailableEnum.TRUE.getValue());
				listVO = new ListVO<>(umsControllerResources, ControllerResourceForAuthVO.class);
			}
			
			if(listVO.getVoList().isEmpty()){
				jsonObj.put("controllerList", data);
				jsonObj.put("checkedList", new String[0]);
				jsonObj.put("forbiddenControllerIds", forbiddenControllerIds);
			}else{
				List<ITreeNode> list = new ArrayList<>();
				list.addAll(listVO.getVoList());
				Tree tree = new Tree(list);
				SimplePropertyPreFilter filter = new SimplePropertyPreFilter(); // 构造方法里，也可以直接传需要序列化的属性名字
		        filter.getExcludes().add("parent");
		        filter.getExcludes().add("allChildren");
		        String treeJsonString = JSONObject.toJSONString(tree.getRoot(), filter);
		        data = JSONObject.parse(treeJsonString);
				List<UmsAcl> aclList = umsAuthFacade.findAcl(dataId, pType, Resource.RESOURCE_CONTROLLER);
				int size = aclList.size();
				String[] controllerIds = new String[size];
				for(int i = 0; i < size; i++){
					UmsAcl acl = aclList.get(i);
					String rid = acl.getRid();
					controllerIds[i] = rid;
					if(acl.getAclState() != null && acl.getAclState() < 0) forbiddenControllerIds.add(rid);
				}
				jsonObj.put("controllerList", data);
				jsonObj.put("checkedList", controllerIds);
				jsonObj.put("forbiddenControllerIds", forbiddenControllerIds);
			}
		}
		return jsonObj; 
	}
}
