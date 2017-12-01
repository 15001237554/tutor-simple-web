package com.rjsoft.uums.api.controller.resource;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rjsoft.common.entity.enums.AvailableEnum;
import com.rjsoft.common.model.search.Searchable;
import com.rjsoft.common.vo.ListVO;
import com.rjsoft.common.vo.PageVO;
import com.rjsoft.common.vo.ViewerResult;
import com.rjsoft.uums.api.constant.FaySysRoleConstant;
import com.rjsoft.uums.api.constant.FayUserConstant;
import com.rjsoft.uums.api.controller.vo.resource.MenuResourceForSelectVO;
import com.rjsoft.uums.api.controller.vo.resource.MenuResourceForTreeSelectVO;
import com.rjsoft.uums.api.controller.vo.resource.MenuResourceVO;
import com.rjsoft.uums.api.service.role.RoleService;
import com.rjsoft.uums.api.util.tree.FayTreeUtil;
import com.rjsoft.uums.api.util.tree.TreeUtil;
import com.rjsoft.uums.facade.app.entity.UmsApp;
import com.rjsoft.uums.facade.app.service.UmsAppFacade;
import com.rjsoft.uums.facade.resource.entity.UmsMenuResources;
import com.rjsoft.uums.facade.resource.service.UmsMenuResFacade;
import com.rjsoft.uums.facade.user.entity.UmsUser;
import com.rjsoft.uums.facade.user.service.UmsUserFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/menuResource")
public class MenuResourceController {

    @Resource
    private UmsMenuResFacade umsMenuResFacade;

    @Resource
    private UmsAppFacade umsAppFacade;

    @Resource
    private UmsUserFacade umsUserFacade;

    @Autowired
    private RoleService roleService;

    /**
     * get all menu resources by conditions for page
     *
     * @param name
     * @param number
     * @param size
     * @return
     */
    @RequestMapping(value = "/findForPage", method = RequestMethod.POST)
    public ViewerResult findForPage(@RequestBody JSONObject obj) {
        ViewerResult result = new ViewerResult();
        Page<UmsMenuResources> pageMenuResources = null;
        PageVO<MenuResourceVO> pageVO = null;
        try {
            //get JSON format parameters
            String name = obj.getString("name");
            String appId = obj.getString("appId");
            int number = obj.getInteger("number");
            int size = obj.getInteger("size");
            Pageable page = PageRequest.of(number, size);
            Searchable searchable = Searchable.newSearchable();
            searchable.setPage(page);
            searchable.addSort(Direction.ASC, "menuOrder");
            searchable.addSearchParam("menuName_like", name);
            searchable.addSearchParam("application.id_eq", appId);
            //get all menu resources by conditions
            pageMenuResources = umsMenuResFacade.listPageUmsMenuResources(searchable);
            //convert to PageVO for view
            pageVO = new PageVO<>(pageMenuResources, MenuResourceVO.class);
            result.setSuccess(true);
            result.setData(pageVO);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrMessage(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value = "/findInTree", method = RequestMethod.POST)
    public ViewerResult findInTree(HttpServletRequest request, @RequestBody JSONObject obj) {
        ViewerResult result = new ViewerResult();
        List<UmsMenuResources> listOrg = null;
        ListVO<MenuResourceVO> listVO = null;
        List<UmsApp> appList = null;
        try {
            String name = obj.getString("name");
            String appId = obj.getString("appId");
            Searchable searchable = Searchable.newSearchable();

            Object currentUsername = request.getAttribute("currentUsername");
            if (currentUsername == null) {
                appList = new ArrayList<UmsApp>();
            } else {
                String username = (String) currentUsername;
                if (Arrays.asList(FayUserConstant.SUPER_MANAGE_USERNAME).contains(username) || roleService.validate(username, FaySysRoleConstant.SUPER_MANAGE_ROLE_SN)) {
                    searchable.addSearchParam("application.id_eq", appId);
                } else {
                    UmsUser user = umsUserFacade.findByUsername((String) currentUsername);
                    appList = umsAppFacade.findAppByUserRoleRelation(user.getId(), AvailableEnum.TRUE.getValue(), FaySysRoleConstant.MANAGE_ROLE_SN);
                    List<String> appIds = new ArrayList<>();
                    boolean flag = true;
                    for (UmsApp app : appList) {
                        appIds.add(app.getId());
                        if (app.getId().equals(appId)) {
                            flag = false;
                            searchable.addSearchParam("application.id_eq", appId);
                            break;
                        }
                    }
                    if (flag)
                        searchable.addSearchParam("application.id_in", appIds);
                }
            }
            searchable.addSort(Direction.ASC, "menuOrder");
            searchable.addSearchParam("menuName_like", name);
            listOrg = umsMenuResFacade.listUmsMenuResources(searchable);
            listVO = new ListVO<>(listOrg, MenuResourceVO.class);

            Object data = FayTreeUtil.getTreeInJsonObject(listVO.getVoList());

            result.setSuccess(true);
            result.setData(data);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrMessage(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value = "/findById", method = RequestMethod.POST)
    public ViewerResult findById(@RequestBody JSONObject obj) {
        ViewerResult result = new ViewerResult();
        UmsMenuResources mr = null;
        try {
            String id = obj.getString("id");
            mr = umsMenuResFacade.findById(id);
            MenuResourceVO menuResourceVO = new MenuResourceVO();
            menuResourceVO.convertPOToVO(mr);
            result.setSuccess(true);
            result.setData(menuResourceVO);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrMessage(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ViewerResult add(@RequestBody UmsMenuResources mr) {
        ViewerResult result = new ViewerResult();
        try {
            mr = umsMenuResFacade.create(mr);
            MenuResourceVO menuResourceVO = new MenuResourceVO();
            menuResourceVO.convertPOToVO(mr);
            result.setSuccess(true);
            result.setData(menuResourceVO);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrMessage(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ViewerResult delete(@RequestBody JSONObject obj) {
        ViewerResult result = new ViewerResult();
        try {
            JSONArray ja = obj.getJSONArray("ids");
            String[] ids = ja.toJavaObject(String[].class);
            umsMenuResFacade.deleteMenu(ids);
            result.setSuccess(true);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrMessage(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ViewerResult update(@RequestBody UmsMenuResources mr) {
        ViewerResult result = new ViewerResult();
        UmsMenuResources currentMr = null;
        try {
            currentMr = umsMenuResFacade.findById(mr.getId());
            currentMr.setMenuName(mr.getMenuName());
            currentMr.setMenuSn(mr.getMenuSn());
            currentMr.setMenuUrl(mr.getMenuUrl());
            currentMr.setMenuIcon(mr.getMenuIcon());
            currentMr.setMenuOrder(mr.getMenuOrder());
            currentMr.setApplication(mr.getApplication());
            currentMr.setParent(mr.getParent());
            mr = umsMenuResFacade.update(currentMr);
            MenuResourceVO menuResourceVO = new MenuResourceVO();
            menuResourceVO.convertPOToVO(mr);
            result.setSuccess(true);
            result.setData(menuResourceVO);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrMessage(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * get all menu resources by conditions for select
     *
     * @return
     */
    @RequestMapping(value = "/findForSelect", method = RequestMethod.POST)
    public ViewerResult findForSelect(@RequestBody JSONObject obj) {
        ViewerResult result = new ViewerResult();
        List<UmsMenuResources> menuResourcesList = null;
        ListVO<MenuResourceForSelectVO> listVO = null;
        try {
            //get JSON format parameters
            String appId = obj.getString("appId");
            Searchable searchable = Searchable.newSearchable();
            searchable.addSearchParam("application.id_eq", appId);
            searchable.addSort(Direction.ASC, "menuOrder");
            //get all by conditions
            menuResourcesList = umsMenuResFacade.listUmsMenuResources(searchable);
            //convert to ListVO for view
            listVO = new ListVO<>(menuResourcesList, MenuResourceForSelectVO.class);
            result.setSuccess(true);
            result.setData(listVO);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrMessage(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value = "/findForTreeSelect", method = RequestMethod.POST)
    public ViewerResult findForTreeSelect(@RequestBody JSONObject obj) {
        ViewerResult result = new ViewerResult();
        List<UmsMenuResources> menuResourcesList = null;
        ListVO<MenuResourceForTreeSelectVO> listVO = null;
        try {
            String appId = obj.getString("appId");
            String deleteId = obj.getString("deleteId");
            Searchable searchable = Searchable.newSearchable();
            searchable.addSearchParam("application.id_eq", appId);
            searchable.addSearchParam("id_ne", deleteId);
            searchable.addSort(Direction.ASC, "menuOrder");
            menuResourcesList = umsMenuResFacade.listUmsMenuResources(searchable);
            listVO = new ListVO<>(menuResourcesList, MenuResourceForTreeSelectVO.class);
            Object data = TreeUtil.getTreeSelectInJsonObject(listVO.getVoList());
            result.setSuccess(true);
            result.setData(data);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrMessage(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value = "/updAvailable", method = RequestMethod.POST)
    public ViewerResult updAvailable(@RequestBody JSONObject obj) {
        ViewerResult result = new ViewerResult();
        try {
            String id = obj.getString("id");
            short isAvailable = new Short(obj.getString("isAvailable"));
            UmsMenuResources mr = umsMenuResFacade.updAvailable(id, isAvailable);
            MenuResourceVO menuResourceVO = new MenuResourceVO();
            menuResourceVO.convertPOToVO(mr);
            result.setSuccess(true);
            result.setData(menuResourceVO);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrMessage(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}
