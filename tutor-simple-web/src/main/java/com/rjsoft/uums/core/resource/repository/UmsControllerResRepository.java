package com.rjsoft.uums.core.resource.repository;

import com.rjsoft.common.repository.CustomRepository;
import com.rjsoft.uums.facade.app.entity.UmsApp;
import com.rjsoft.uums.facade.resource.entity.UmsControllerResources;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UmsControllerResRepository extends CustomRepository<UmsControllerResources, String> {

    @Query("select o from UmsControllerResources o where o.controllerSn=?1 and o.application=?2")
    UmsControllerResources findControllerResBySnAndApp(String controllerResSn, UmsApp app);

    @Query("select b from UmsAcl a,UmsControllerResources b where a.rid=b.id and a.rtype=?1 and a.pid in ?2 and a.ptype=?3 and b.application.sn=?4 and b.isAvailable=?5")
    List<UmsControllerResources> getControllerResList(String rtype, List<String> pid, String ptype, String appSn, Short isAvailable);

    @Query("select b from UmsAcl a,UmsControllerResources b where a.rid=b.id and a.rtype=?1 and a.pid in ?2 and a.ptype=?3 and b.application.sn=?4 and b.menu.id=?5 and b.isAvailable=?6")
    List<UmsControllerResources> getControllerResList(String rtype, List<String> pid, String ptype, String appSn, String menuId, Short isAvailable);

    @Query("select b from UmsAcl a,UmsControllerResources b where a.rid=b.id and b.controllerUrlMapping=?1 and a.rtype=?2 and a.pid in ?3 and a.ptype=?4 and b.application.sn=?5 and b.isAvailable=?6")
    List<UmsControllerResources> getControllerResList(String url, String rtype, List<String> pid, String ptype, String appSn, Short isAvailable);

    @Query("select o.id from UmsControllerResources o where o.parent.id=?1")
    public List<String> findControllerByParentId(String parentId);

    @Query("select o.id from UmsControllerResources o where o.menu.id=?1")
    public List<String> findControllerByMenuId(String menuId);

    @Modifying
    @Query("delete from UmsControllerResources where application.id in ?1")
    void deleteByAppIds(List<String> appIds);

}
