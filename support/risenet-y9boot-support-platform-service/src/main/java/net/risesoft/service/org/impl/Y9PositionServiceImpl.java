package net.risesoft.service.org.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import net.risesoft.consts.OrgLevelConsts;
import net.risesoft.entity.Y9Department;
import net.risesoft.entity.Y9OrgBase;
import net.risesoft.entity.Y9Organization;
import net.risesoft.entity.Y9Position;
import net.risesoft.entity.relation.Y9PersonsToPositions;
import net.risesoft.enums.AuthorizationPrincipalTypeEnum;
import net.risesoft.enums.OrgTypeEnum;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.manager.org.CompositeOrgBaseManager;
import net.risesoft.manager.org.Y9PositionManager;
import net.risesoft.model.Position;
import net.risesoft.repository.Y9PositionRepository;
import net.risesoft.repository.identity.position.Y9PositionToResourceAndAuthorityRepository;
import net.risesoft.repository.identity.position.Y9PositionToRoleRepository;
import net.risesoft.repository.permission.Y9AuthorizationRepository;
import net.risesoft.repository.relation.Y9OrgBasesToRolesRepository;
import net.risesoft.repository.relation.Y9PersonsToPositionsRepository;
import net.risesoft.repository.relation.Y9PositionsToGroupsRepository;
import net.risesoft.service.org.Y9PositionService;
import net.risesoft.util.Y9PublishServiceUtil;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.pubsub.constant.Y9OrgEventConst;
import net.risesoft.y9.pubsub.event.Y9EntityDeletedEvent;
import net.risesoft.y9.pubsub.event.Y9EntityUpdatedEvent;
import net.risesoft.y9.pubsub.message.Y9MessageOrg;
import net.risesoft.y9.util.Y9BeanUtil;
import net.risesoft.y9.util.Y9ModelConvertUtil;

/**
 * @author dingzhaojun
 * @author qinman
 * @author mengjuhua
 * @date 2022/2/10
 */
@Transactional(value = "rsTenantTransactionManager", readOnly = true)
@Service
public class Y9PositionServiceImpl implements Y9PositionService {

    private final EntityManagerFactory entityManagerFactory;

    private final Y9PositionRepository y9PositionRepository;
    private final Y9PersonsToPositionsRepository y9PersonsToPositionsRepository;
    private final Y9PositionsToGroupsRepository y9PositionsToGroupsRepository;
    private final Y9OrgBasesToRolesRepository y9OrgBasesToRolesRepository;
    private final Y9PositionToResourceAndAuthorityRepository y9PositionToResourceAndAuthorityRepository;
    private final Y9PositionToRoleRepository y9PositionToRoleRepository;
    private final Y9AuthorizationRepository y9AuthorizationRepository;

    private final CompositeOrgBaseManager compositeOrgBaseManager;
    private final Y9PositionManager y9PositionManager;

    public Y9PositionServiceImpl(@Qualifier("rsTenantEntityManagerFactory") EntityManagerFactory entityManagerFactory,
        Y9PositionRepository y9PositionRepository, Y9PersonsToPositionsRepository y9PersonsToPositionsRepository,
        Y9PositionsToGroupsRepository y9PositionsToGroupsRepository,
        Y9OrgBasesToRolesRepository y9OrgBasesToRolesRepository,
        Y9PositionToResourceAndAuthorityRepository y9PositionToResourceAndAuthorityRepository,
        Y9PositionToRoleRepository y9PositionToRoleRepository, Y9AuthorizationRepository y9AuthorizationRepository,
        CompositeOrgBaseManager compositeOrgBaseManager, Y9PositionManager y9PositionManager) {
        this.entityManagerFactory = entityManagerFactory;
        this.y9PositionRepository = y9PositionRepository;
        this.y9PersonsToPositionsRepository = y9PersonsToPositionsRepository;
        this.y9PositionsToGroupsRepository = y9PositionsToGroupsRepository;
        this.y9OrgBasesToRolesRepository = y9OrgBasesToRolesRepository;
        this.y9PositionToResourceAndAuthorityRepository = y9PositionToResourceAndAuthorityRepository;
        this.y9PositionToRoleRepository = y9PositionToRoleRepository;
        this.y9AuthorizationRepository = y9AuthorizationRepository;
        this.compositeOrgBaseManager = compositeOrgBaseManager;
        this.y9PositionManager = y9PositionManager;
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Position createPosition(Y9Position y9Position) {
        Optional<Y9OrgBase> y9OrgBaseOptional = compositeOrgBaseManager.findOrgUnitAsParent(y9Position.getParentId());
        if (y9Position == null || y9OrgBaseOptional.isEmpty()) {
            return null;
        }
        Y9OrgBase parent = y9OrgBaseOptional.get();
        if (StringUtils.isBlank(y9Position.getId())) {
            y9Position.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        }
        y9Position.setTabIndex(Integer.MAX_VALUE);
        y9Position.setParentId(parent.getId());
        y9Position.setDn(OrgLevelConsts.getOrgLevel(OrgTypeEnum.POSITION) + y9Position.getName()
            + OrgLevelConsts.SEPARATOR + parent.getDn());
        y9Position.setDisabled(false);
        y9Position.setOrgType(OrgTypeEnum.POSITION.getEnName());
        if (y9Position.getDutyLevel() == null) {
            y9Position.setDutyLevel(0);
        }

        return save(y9Position);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(List<String> ids) {
        for (String id : ids) {
            this.deleteById(id);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteById(String positionId) {
        Y9Position y9Position = this.getById(positionId);

        // 删除岗位关联数据
        y9OrgBasesToRolesRepository.deleteByOrgId(positionId);
        y9PositionToRoleRepository.deleteByPositionId(positionId);
        y9PositionToResourceAndAuthorityRepository.deleteByPositionId(positionId);
        y9PersonsToPositionsRepository.deleteByPositionId(positionId);
        y9PositionsToGroupsRepository.deleteByPositionId(positionId);
        y9AuthorizationRepository.deleteByPrincipalIdAndPrincipalType(positionId,
            AuthorizationPrincipalTypeEnum.POSITION.getValue());

        y9PositionManager.delete(y9Position);
        // 发布事件，程序内部监听处理相关业务
        Y9Context.publishEvent(new Y9EntityDeletedEvent<>(y9Position));

        Y9MessageOrg msg = new Y9MessageOrg(Y9ModelConvertUtil.convert(y9Position, Position.class),
            Y9OrgEventConst.RISEORGEVENT_TYPE_DELETE_POSITION, Y9LoginUserHolder.getTenantId());
        Y9PublishServiceUtil.persistAndPublishMessageOrg(msg, "删除岗位", "删除" + y9Position.getName());
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteByParentId(String parentId) {
        List<Y9Position> positionList = listByParentId(parentId);
        for (Y9Position position : positionList) {
            deleteById(position.getId());
        }
    }

    @Override
    public boolean existsById(String id) {
        return y9PositionRepository.existsById(id);
    }

    @Override
    public Optional<Y9Position> findById(String id) {
        return y9PositionManager.findById(id);
    }

    @Override
    public List<Y9Position> findByJobId(String jobId) {
        return y9PositionRepository.findByJobId(jobId);
    }

    @Override
    @Transactional(readOnly = true)
    public Y9Position getById(String id) {
        return y9PositionManager.getById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean hasPosition(String positionName, String personId) {
        List<Y9Position> list = listByPersonId(personId);
        boolean exist = false;
        if (!list.isEmpty()) {
            for (Y9Position p : list) {
                if (p.getName().equals(positionName)) {
                    exist = true;
                    break;
                }
            }
        }

        return exist;
    }

    @Override
    public List<Y9Position> listAll() {
        return y9PositionRepository.findAll();
    }

    @Override
    public List<Y9Position> listByDn(String dn) {
        return y9PositionRepository.findByDn(dn);
    }

    @Override
    public List<Y9Position> listByNameLike(String name) {
        return y9PositionRepository.findByNameContainingOrderByTabIndexAsc(name);
    }

    @Override
    public List<Y9Position> listByNameLike(String name, String dn) {
        return y9PositionRepository.findByNameContainingAndDnContainingOrDnContaining(name, OrgLevelConsts.UNIT + dn,
            OrgLevelConsts.ORGANIZATION + dn);
    }

    @Override
    public List<Y9Position> listByParentId(String parentId) {
        return y9PositionRepository.findByParentIdOrderByTabIndexAsc(parentId);
    }

    @Override
    public List<Y9Position> listByPersonId(String personId) {
        List<Y9PersonsToPositions> ppsList =
            y9PersonsToPositionsRepository.findByPersonIdOrderByPositionOrderAsc(personId);
        List<Y9Position> pList = new ArrayList<>();
        for (Y9PersonsToPositions pps : ppsList) {
            pList.add(this.getById(pps.getPositionId()));
        }
        return pList;
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Position move(String positionId, String parentId) {
        Y9Position originPosition = this.getById(positionId);
        Y9Position updatedPosition = new Y9Position();
        Y9BeanUtil.copyProperties(originPosition, updatedPosition);

        Y9OrgBase parent = compositeOrgBaseManager.getOrgUnitAsParent(parentId);
        updatedPosition.setParentId(parent.getId());
        updatedPosition.setGuidPath(parent.getGuidPath() + OrgLevelConsts.SEPARATOR + updatedPosition.getId());
        updatedPosition.setDn(OrgLevelConsts.getOrgLevel(OrgTypeEnum.POSITION) + updatedPosition.getName()
            + OrgLevelConsts.SEPARATOR + parent.getDn());
        updatedPosition = this.save(updatedPosition);

        Y9Context.publishEvent(new Y9EntityUpdatedEvent<>(originPosition, updatedPosition));

        Y9MessageOrg msg = new Y9MessageOrg(Y9ModelConvertUtil.convert(updatedPosition, Position.class),
            Y9OrgEventConst.RISEORGEVENT_TYPE_UPDATE_POSITION, Y9LoginUserHolder.getTenantId());
        Y9PublishServiceUtil.persistAndPublishMessageOrg(msg, "移动岗位",
            updatedPosition.getName() + "移动到" + parent.getName());

        return updatedPosition;
    }

    @EventListener
    public void onParentDepartmentDeleted(Y9EntityDeletedEvent<Y9Department> event) {
        Y9Department parentDepartment = event.getEntity();
        // 删除部门时其下岗位也要删除
        deleteByParentId(parentDepartment.getId());
    }

    @EventListener
    public void onParentOrganizationDeleted(Y9EntityDeletedEvent<Y9Organization> event) {
        Y9Organization y9Organization = event.getEntity();
        // 删除组织时其下岗位也要删除
        deleteByParentId(y9Organization.getId());
    }

    private void recursionUpPosition(List<Y9Position> positionList, String parentId) {
        if (StringUtils.isBlank(parentId)) {
            return;
        }
        Y9Position position = this.getById(parentId);
        positionList.add(position);
        recursionUpPosition(positionList, position.getParentId());
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Position save(Y9Position position) {
        return y9PositionManager.save(position);
    }

    @Transactional(readOnly = false)
    public Y9Position saveOrder(String positionId, int tabIndex) {
        Y9Position position = this.getById(positionId);
        position.setTabIndex(tabIndex);
        position = save(position);

        Y9MessageOrg msg = new Y9MessageOrg(Y9ModelConvertUtil.convert(position, Position.class),
            Y9OrgEventConst.RISEORGEVENT_TYPE_UPDATE_POSITION, Y9LoginUserHolder.getTenantId());
        Y9PublishServiceUtil.publishMessageOrg(msg);

        return position;
    }

    @Override
    @Transactional(readOnly = false)
    public List<Y9Position> saveOrder(List<String> positionIds) {
        List<Y9Position> orgPositionList = new ArrayList<>();

        int tabIndex = 0;
        for (String positionId : positionIds) {
            orgPositionList.add(saveOrder(positionId, tabIndex++));
        }

        return orgPositionList;
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Position saveOrUpdate(Y9Position position) {
        Y9OrgBase parent = compositeOrgBaseManager.getOrgUnitAsParent(position.getParentId());
        return y9PositionManager.saveOrUpdate(position, parent);
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Position saveProperties(String positionId, String properties) {
        Y9Position position = this.getById(positionId);
        position.setProperties(properties);
        position = y9PositionManager.save(position);

        Y9MessageOrg msg = new Y9MessageOrg(Y9ModelConvertUtil.convert(position, Position.class),
            Y9OrgEventConst.RISEORGEVENT_TYPE_UPDATE_POSITION, Y9LoginUserHolder.getTenantId());
        Y9PublishServiceUtil.publishMessageOrg(msg);

        return position;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Y9Position> search(String whereClause) {
        EntityManager em = EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory);
        Assert.notNull(em, "EntityManager must not be null");
        Query query = null;
        if (whereClause == null || "".equals(whereClause.trim())) {
            query = em.createNativeQuery(" select * from Y9_ORG_POSITION ", Y9Position.class);

        } else {
            query = em.createNativeQuery(" select * from Y9_ORG_POSITION where " + whereClause, Y9Position.class);
        }
        return query.getResultList();
    }

    @Override
    public List<Y9Position> treeSearch(String name) {
        List<Y9Position> positionList = new ArrayList<>();
        List<Y9Position> positionListTemp = y9PositionRepository.findByNameContainingOrderByTabIndexAsc(name);
        positionList.addAll(positionListTemp);
        for (Y9Position p : positionListTemp) {
            recursionUpPosition(positionList, p.getParentId());
        }
        return positionList;
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Position updateTabIndex(String id, int tabIndex) {
        Y9Position position = this.getById(id);
        position.setTabIndex(tabIndex);
        position.setOrderedPath(compositeOrgBaseManager.buildOrderedPath(position));
        position = y9PositionManager.save(position);

        Y9MessageOrg msg = new Y9MessageOrg(Y9ModelConvertUtil.convert(position, Position.class),
            Y9OrgEventConst.RISEORGEVENT_TYPE_UPDATE_POSITION_TABINDEX, Y9LoginUserHolder.getTenantId());
        Y9PublishServiceUtil.persistAndPublishMessageOrg(msg, "更新岗位排序号", position.getName() + "的排序号更新为" + tabIndex);

        return position;
    }

}
