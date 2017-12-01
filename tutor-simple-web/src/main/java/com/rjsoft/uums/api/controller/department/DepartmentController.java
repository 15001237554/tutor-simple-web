package com.rjsoft.uums.api.controller.department;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rjsoft.common.entity.enums.AvailableEnum;
import com.rjsoft.common.model.search.Searchable;
import com.rjsoft.common.vo.ListVO;
import com.rjsoft.common.vo.PageVO;
import com.rjsoft.common.vo.ViewerResult;
import com.rjsoft.uums.api.constant.FayOrgRoleConstant;
import com.rjsoft.uums.api.constant.FaySysRoleConstant;
import com.rjsoft.uums.api.constant.FayUserConstant;
import com.rjsoft.uums.api.controller.vo.department.DepartmentForSelectVO;
import com.rjsoft.uums.api.controller.vo.department.DepartmentVO;
import com.rjsoft.uums.api.service.orgRole.OrgRoleService;
import com.rjsoft.uums.api.service.role.RoleService;
import com.rjsoft.uums.api.service.user.UserService;
import com.rjsoft.uums.facade.department.entity.UmsDepartment;
import com.rjsoft.uums.facade.department.entity.UmsUserDepartmentRelation;
import com.rjsoft.uums.facade.department.service.UmsDepartmentFacade;
import com.rjsoft.uums.facade.org.entity.UmsOrg;
import com.rjsoft.uums.facade.org.service.UmsOrgFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/department")
public class DepartmentController {

    @Resource
    private UmsOrgFacade umsOrgFacade;

    @Resource
    private UmsDepartmentFacade umsDepartmentFacade;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private OrgRoleService orgRoleService;

    @RequestMapping(value = "/findForPage", method = RequestMethod.POST)
    public ViewerResult findForPage(HttpServletRequest request, @RequestBody JSONObject obj) {
        ViewerResult result = new ViewerResult();
        Page<UmsDepartment> pageDepartment = null;
        Page<UmsUserDepartmentRelation> uuors = null;
        PageVO<DepartmentVO> pageVO = null;
        try {
            Object currentUsername = request.getAttribute("currentUsername");
            if (currentUsername != null) {
                String username = (String) currentUsername;
                String name = obj.getString("name");
                String orgId = obj.getString("orgId");
                int number = obj.getInteger("number");
                int size = obj.getInteger("size");
                Pageable page = PageRequest.of(number, size);
                Searchable searchable = Searchable.newSearchable();
                searchable.setPage(page);
                if (Arrays.asList(FayUserConstant.SUPER_MANAGE_USERNAME).contains(username) || roleService.validate(username, FaySysRoleConstant.SUPER_MANAGE_ROLE_SN)) {
                    searchable.addSearchParam("name_like", name);
                    searchable.addSearchParam("orgId_eq", orgId);
                    pageDepartment = umsDepartmentFacade.listPage(searchable);
                    pageVO = new PageVO<>(pageDepartment, DepartmentVO.class);
                } else if (orgRoleService.validate(username, FayOrgRoleConstant.ORG_SUPER_MANAGE_ROLE_SN)) {
                    searchable.addSearchParam("name_like", name);
                    List<String> orgIds = userService.getOrgIdsForManageByLoginUser(username);
                    if (orgId == null) {
                        searchable.addSearchParam("orgId_in", orgIds);
                        pageDepartment = umsDepartmentFacade.listPage(searchable);
                        pageVO = new PageVO<>(pageDepartment, DepartmentVO.class);
                    } else if (orgIds.contains(orgId)) {
                        searchable.addSearchParam("orgId_eq", orgId);
                        pageDepartment = umsDepartmentFacade.listPage(searchable);
                        pageVO = new PageVO<>(pageDepartment, DepartmentVO.class);
                    } else {
                        pageVO = new PageVO<>(new ArrayList<>(), DepartmentVO.class);
                    }
                } else {
                    searchable.addSearchParam("department.name_like", name);
                    searchable.addSearchParam("department.orgId_eq", orgId);
                    searchable.addSearchParam("user.username_eq", username);
                    uuors = umsDepartmentFacade.listUmsUserDepartmentRelationPage(searchable);
                    pageVO = new PageVO<>(uuors, DepartmentVO.class);
                }
                for (DepartmentVO vo : pageVO.getPageData()) {
                    String oid = vo.getOrgId();
                    if (oid != null) {
                        UmsOrg org = umsOrgFacade.getById(vo.getOrgId());
                        if (org != null) vo.setOrgName(org.getName());
                    }

                }
            } else {
                pageVO = new PageVO<>(new ArrayList<>(), DepartmentVO.class);
            }
            result.setSuccess(true);
            result.setData(pageVO);
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
        UmsDepartment department = null;
        try {
            String id = obj.getString("id");
            department = umsDepartmentFacade.getById(id);
            DepartmentVO departmentVO = new DepartmentVO();
            departmentVO.convertPOToVO(department);
            result.setSuccess(true);
            result.setData(departmentVO);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrMessage(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ViewerResult add(@RequestBody UmsDepartment department) {
        ViewerResult result = new ViewerResult();
        try {
            department = umsDepartmentFacade.create(department);
            DepartmentVO departmentVO = new DepartmentVO();
            departmentVO.convertPOToVO(department);
            result.setSuccess(true);
            result.setData(departmentVO);
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
            umsDepartmentFacade.delete(ids);
            result.setSuccess(true);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrMessage(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ViewerResult update(@RequestBody UmsDepartment department) {
        ViewerResult result = new ViewerResult();
        UmsDepartment currentDepartment = null;
        try {
            currentDepartment = umsDepartmentFacade.getById(department.getId());
            currentDepartment.setName(department.getName());
            currentDepartment.setDescription(department.getDescription());
            currentDepartment.setOrgId(department.getOrgId());
            department = umsDepartmentFacade.update(currentDepartment);
            DepartmentVO departmentVO = new DepartmentVO();
            departmentVO.convertPOToVO(department);
            result.setSuccess(true);
            result.setData(departmentVO);
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
            UmsDepartment department = umsDepartmentFacade.updAvailable(id, isAvailable);
            DepartmentVO departmentVO = new DepartmentVO();
            departmentVO.convertPOToVO(department);
            result.setSuccess(true);
            result.setData(departmentVO);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrMessage(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value = "/findByOrgForSelect", method = RequestMethod.POST)
    public ViewerResult findForSelect(HttpServletRequest request, @RequestBody JSONObject obj) {
        ViewerResult result = new ViewerResult();
        List<UmsDepartment> departmentList = null;
        List<UmsUserDepartmentRelation> userDepartmentRelationList = null;
        ListVO<DepartmentForSelectVO> listVO = null;
        try {
            Object currentUsername = request.getAttribute("currentUsername");
            if (currentUsername != null) {
                String username = (String) currentUsername;
                String orgId = obj.getString("orgId");
                Searchable searchable = Searchable.newSearchable();
                if (Arrays.asList(FayUserConstant.SUPER_MANAGE_USERNAME).contains(username) || roleService.validate(username, FaySysRoleConstant.SUPER_MANAGE_ROLE_SN) || roleService.validate(username, FaySysRoleConstant.MANAGE_ROLE_SN)) {
                    searchable.addSearchParam("orgId_eq", orgId);
                    searchable.addSearchParam("isAvailable_eq", AvailableEnum.TRUE.getValue());
                    departmentList = umsDepartmentFacade.list(searchable);
                    listVO = new ListVO<>(departmentList, DepartmentForSelectVO.class);
                } else if (orgRoleService.validate(username, FayOrgRoleConstant.ORG_SUPER_MANAGE_ROLE_SN)) {
                    List<String> orgIds = userService.getOrgIdsForManageByLoginUser(username);
                    if (orgId == null) {
                        searchable.addSearchParam("orgId_in", orgIds);
                        departmentList = umsDepartmentFacade.list(searchable);
                        listVO = new ListVO<>(departmentList, DepartmentForSelectVO.class);
                    } else if (orgIds.contains(orgId)) {
                        searchable.addSearchParam("orgId_eq", orgId);
                        departmentList = umsDepartmentFacade.list(searchable);
                        listVO = new ListVO<>(departmentList, DepartmentForSelectVO.class);
                    } else {
                        listVO = new ListVO<>(new ArrayList<>(), DepartmentForSelectVO.class);
                    }
                } else {
                    searchable.addSearchParam("department.orgId_eq", orgId);
                    searchable.addSearchParam("department.isAvailable_eq", AvailableEnum.TRUE.getValue());
                    searchable.addSearchParam("user.username_eq", username);
                    userDepartmentRelationList = umsDepartmentFacade.listUmsUserDepartmentRelation(searchable);
                    listVO = new ListVO<>(userDepartmentRelationList, DepartmentForSelectVO.class);
                }
            } else {
                listVO = new ListVO<>(departmentList, DepartmentForSelectVO.class);
            }
            result.setSuccess(true);
            result.setData(listVO);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrMessage(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}
