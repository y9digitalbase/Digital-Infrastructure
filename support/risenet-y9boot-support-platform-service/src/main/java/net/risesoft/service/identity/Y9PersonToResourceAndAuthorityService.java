package net.risesoft.service.identity;

import java.util.List;

import net.risesoft.entity.Y9Person;
import net.risesoft.entity.identity.person.Y9PersonToResourceAndAuthority;
import net.risesoft.entity.permission.Y9Authorization;
import net.risesoft.enums.platform.AuthorityEnum;
import net.risesoft.enums.platform.ResourceTypeEnum;
import net.risesoft.y9public.entity.resource.Y9App;
import net.risesoft.y9public.entity.resource.Y9Menu;
import net.risesoft.y9public.entity.resource.Y9ResourceBase;

/**
 * @author dingzhaojun
 * @author qinman
 * @author mengjuhua
 * @date 2022/2/10
 */
public interface Y9PersonToResourceAndAuthorityService {

    /**
     * 按应用id删除
     *
     * @param appId 应用id
     */
    void deleteByAppId(String appId);

    /**
     * 根据授权配置id删除权限缓存
     *
     * @param authorizationId
     */
    void deleteByAuthorizationId(String authorizationId);

    /**
     * 根据授权配置id和组织机构id删除权限缓存
     *
     * @param authorizationId
     * @param orgUnitId
     */
    void deleteByAuthorizationIdAndOrgUnitId(String authorizationId, String orgUnitId);

    /**
     * 根据授权配置id和人员id删除权限缓存
     *
     * @param authorizationId
     * @param personId
     */
    void deleteByAuthorizationIdAndPersonId(String authorizationId, String personId);

    /**
     * 根据组织id删除其下所有所有人员的权限缓存
     *
     * @param orgUnitId
     */
    void deleteByOrgUnitId(String orgUnitId);

    /**
     * 根据人员id删除
     *
     * @param personId
     */
    void deleteByPersonId(String personId);

    /**
     * 按资源id删除
     *
     * @param resourceId id
     */
    void deleteByResourceId(String resourceId);

    /**
     * 判断人对资源是否有相应的权限
     *
     * @param personId
     * @param resourceId
     * @param authority
     * @return
     */
    boolean hasPermission(String personId, String resourceId, AuthorityEnum authority);

    /**
     * 判断人对资源是否有相应权限
     *
     * @param personId
     * @param resourceCustomId
     * @param authority
     * @return
     */
    boolean hasPermissionByCustomId(String personId, String resourceCustomId, AuthorityEnum authority);

    List<Y9PersonToResourceAndAuthority> list(String personId);

    /**
     * 根据人员id、父资源id及授权类型查找
     *
     * @param personId
     * @param parentResourceId
     * @param authority
     * @return
     */
    List<Y9PersonToResourceAndAuthority> list(String personId, String parentResourceId, AuthorityEnum authority);

    /**
     * 根据人员id、父资源id、资源类型及授权类型查找
     *
     * @param personId
     * @param parentResourceId
     * @param resourceType 资源类型{@link ResourceTypeEnum}
     * @param authority 授权类型{@link AuthorityEnum}
     * @return
     * @see AuthorityEnum
     */
    List<Y9PersonToResourceAndAuthority> list(String personId, String parentResourceId, ResourceTypeEnum resourceType,
        AuthorityEnum authority);

    /**
     * 根据人员id 及授权类型查找应用列表
     *
     * @param personId 人员id
     * @param authority {@link AuthorityEnum}
     * @return
     */
    List<Y9App> listAppsByAuthority(String personId, AuthorityEnum authority);

    /**
     * 更新或保存
     *
     * @param y9ResourceBase
     * @param person
     * @param y9Authorization
     * @param inherit
     */
    void saveOrUpdate(Y9ResourceBase y9ResourceBase, Y9Person person, Y9Authorization y9Authorization, Boolean inherit);

    /**
     * 更新
     *
     * @param resourceId
     * @param resourceName
     * @param systemName
     * @param systemCnName
     * @param description
     */
    void update(String resourceId, String resourceName, String systemName, String systemCnName, String description);

    /**
     * 获得某一资源下,有相应操作权限的子节点
     *
     * @param personId 人员id
     * @param resourceId 资源id
     * @param authority 权限类型
     * @return {@link List}<{@link Y9ResourceBase}>
     */
    List<Y9ResourceBase> listSubResources(String personId, String resourceId, AuthorityEnum authority);

    /**
     * 子菜单列表
     *
     * @param personId 人员id
     * @param resourceId 资源id
     * @param resourceType 资源类型
     * @param authority 权限类型
     * @return {@link List}<{@link Y9Menu}>
     */
    List<Y9Menu> listSubMenus(String personId, String resourceId, Integer resourceType, AuthorityEnum authority);
}
