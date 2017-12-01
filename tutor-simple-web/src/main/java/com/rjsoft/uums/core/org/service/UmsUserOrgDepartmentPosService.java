package com.rjsoft.uums.core.org.service;

import com.rjsoft.common.service.BaseService;
import com.rjsoft.uums.core.org.repository.UmsUserOrgDepartmentPosRepository;
import com.rjsoft.uums.facade.org.entity.UmsUserOrgDepartmentPos;
import com.rjsoft.uums.facade.user.entity.UmsUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("umsPersonOrgPosService")
public class UmsUserOrgDepartmentPosService extends BaseService<UmsUserOrgDepartmentPos, String> {

    @Autowired
    private UmsUserOrgDepartmentPosRepository umsUserOrgDepartmentPosRepository;

    /**
     * 建立用户与组织机构下的职位的关系
     *
     * @param orgId
     * @param positionId
     * @param userIds
     */
    public void buildUserOrgPositionRelation(String orgId, String positionId, List<String> userIds) {
        if (userIds == null || userIds.size() == 0) {
            return;
        } else {
            if (positionId == "") positionId = null;
            clearUserOrgPositionRelation(orgId, positionId, userIds);
            for (String userId : userIds) {
                UmsUserOrgDepartmentPos r = new UmsUserOrgDepartmentPos();
                r.setOrgId(orgId);
                r.setPosId(positionId);
                r.setUserId(userId);
                umsUserOrgDepartmentPosRepository.save(r);
            }
        }
    }

    /**
     * 删除用户与组织机构的职位关系
     *
     * @param orgId
     * @param positionId
     * @param userIds
     */
    public void clearUserOrgPositionRelation(String orgId, String positionId, List<String> userIds) {
        if (positionId == null || positionId == "") {
            umsUserOrgDepartmentPosRepository.clearUserOrgPositionRelation(orgId, userIds);
        } else {
            umsUserOrgDepartmentPosRepository.clearUserOrgPositionRelation(orgId, positionId, userIds);
        }

    }

    /**
     * 根据组织机构下的职位获取所有用户
     *
     * @param orgId
     * @param positionId
     * @param nickname
     * @param username
     * @param page
     * @return
     */
    public Page<UmsUser> findUserByOrgIdAndPositionId(String orgId, String positionId, String nickname, String username, Pageable page) {
        if (positionId == null || positionId == "") {
            return umsUserOrgDepartmentPosRepository.findUserByOrgIdAndPositionId(orgId, nickname, username, page);
        } else {
            return umsUserOrgDepartmentPosRepository.findUserByOrgIdAndPositionId(orgId, positionId, nickname, username, page);
        }
    }

    /**
     * 根据组织机构下的职位获取所有以外的用户
     *
     * @param orgId
     * @param positionId
     * @param nickname
     * @param username
     * @param page
     * @return
     */
    public Page<UmsUser> findNotUserByOrgIdAndPositionId(String orgId, String positionId, String nickname, String username, Pageable page) {
        if (positionId == null || positionId == "") {
            return umsUserOrgDepartmentPosRepository.findNotUserByOrgIdAndPositionId(orgId, nickname, username, page);
        } else {
            return umsUserOrgDepartmentPosRepository.findNotUserByOrgIdAndPositionId(orgId, positionId, nickname, username, page);
        }
    }

}
