package com.rjsoft.uums.core.org.service;

import com.rjsoft.common.service.BaseService;
import com.rjsoft.uums.core.org.repository.UmsOrgRepository;
import com.rjsoft.uums.core.org.repository.UmsOrgTypeRepository;
import com.rjsoft.uums.core.org.repository.UmsOrgTypeRuleRepository;
import com.rjsoft.uums.facade.org.entity.UmsOrgType;
import com.rjsoft.uums.facade.org.entity.UmsOrgTypeRule;
import com.rjsoft.uums.facade.org.exception.OrgTypeNotDeleteException;
import com.rjsoft.uums.facade.org.exception.OrgTypeSnExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("umsOrgTypeService")
public class UmsOrgTypeService extends BaseService<UmsOrgType, String> {

    @Autowired
    private UmsOrgRepository umsOrgRepository;
    @Autowired
    private UmsOrgTypeRepository umsOrgTypeRepository;
    @Autowired
    private UmsOrgTypeRuleRepository umsOrgTypeRuleRepository;

    /**
     * 添加组织机构类型
     *
     * @param umsOrgTypeRule
     */
    public void saveOrgType(UmsOrgType orgType) {
        if (this.getBySn(orgType.getSn()) != null) {
            throw new OrgTypeSnExistsException();
        }
        save(orgType);
    }

    /**
     * 更新组织机构类型
     *
     * @param umsOrgTypeRule
     * @return
     */
    public void updateOrgType(UmsOrgType orgType) {
        update(orgType);
    }

    /**
     * 删除组织机构类型
     *
     * @param ids
     */
    public void deleteOrgType(String... ids) {
        for (String id : ids) {
            Integer c = umsOrgRepository.getOrgNumsByType(id);
            if (c > 0) {
                throw new OrgTypeNotDeleteException();
            }
            delete(id);
            deleteOrgTypeRuleByOrgType(id);
        }
    }

    /**
     * 添加组织机构类型规则
     *
     * @param umsOrgTypeRule
     */
    public UmsOrgTypeRule saveOrgTypeRule(UmsOrgTypeRule umsOrgTypeRule) {
        return umsOrgTypeRuleRepository.save(umsOrgTypeRule);
    }

    /**
     * 更新组织机构类型规则
     *
     * @param umsOrgTypeRule
     */
    public UmsOrgTypeRule updateOrgTypeRule(UmsOrgTypeRule umsOrgTypeRule) {
        return umsOrgTypeRuleRepository.save(umsOrgTypeRule);
    }

    /**
     * 删除组织机构类型规则
     *
     * @param umsOrgTypeRule
     */
    public void deleteOrgTypeRule(String... orgTypeRuleId) {
        umsOrgTypeRuleRepository.delete(orgTypeRuleId);
    }

    /**
     * 删除组织机构类型规则
     *
     * @param umsOrgTypeRule
     */
    public void deleteOrgTypeRuleByOrgType(String pTypeId) {
        umsOrgTypeRuleRepository.deleteOrgTypeRuleByOrgType(pTypeId);
    }

    /**
     * 根据组织机构类型标识符查询组织机构类型实体信息
     *
     * @param sn
     * @return
     */
    public UmsOrgType getBySn(String sn) {
        return umsOrgTypeRepository.findOrgTypeBySn(sn);
    }

    /**
     * 根据组织机构类型ID查询组织机构类型实体信息
     *
     * @param id
     * @return
     */
    public UmsOrgType getById(String id) {
        return umsOrgTypeRepository.getOne(id);
    }

    /**
     * 根据父亲id获取该组织的所有可以管理的子节点
     *
     * @param pid
     * @return
     */
    public List<UmsOrgTypeRule> listByRule(String pid) {
        return umsOrgTypeRuleRepository.listByRule(pid);
    }

}
