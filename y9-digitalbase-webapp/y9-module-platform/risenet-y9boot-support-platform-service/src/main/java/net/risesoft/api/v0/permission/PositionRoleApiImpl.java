package net.risesoft.api.v0.permission;

import javax.validation.constraints.NotBlank;

import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.api.platform.v0.permission.PositionRoleApi;
import net.risesoft.service.identity.Y9PositionToRoleService;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * 岗位角色组件
 *
 * @author shidaobang
 * @date 2022/11/11
 * @since 9.6.0
 */
@Primary
@Validated
@RestController(value = "v0PositionRoleApiImpl")
@RequestMapping(value = "/services/rest/positionRole", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Deprecated
public class PositionRoleApiImpl implements PositionRoleApi {

    private final Y9PositionToRoleService y9PositionToRoleService;

    /**
     * 根据岗位id判断该岗位是否拥有roleName这个公共角色
     *
     * @param tenantId 租户id
     * @param roleName 角色名称
     * @param positionId 岗位id
     * @return boolean
     * @since 9.6.0
     */
    @Override
    public Boolean hasPublicRole(@RequestParam("tenantId") @NotBlank String tenantId,
        @RequestParam("roleName") @NotBlank String roleName, @RequestParam("positionId") @NotBlank String positionId) {
        Y9LoginUserHolder.setTenantId(tenantId);

        return y9PositionToRoleService.hasPublicRole(positionId, roleName);
    }

    /**
     * 判断岗位是否拥有角色
     *
     * @param tenantId 租户id
     * @param roleId 角色id
     * @param positionId 岗位id
     * @return {@link Boolean}
     * @since 9.6.0
     */
    @Override
    public Boolean hasRole(@RequestParam("tenantId") @NotBlank String tenantId,
        @RequestParam("roleId") @NotBlank String roleId, @RequestParam("positionId") @NotBlank String positionId) {
        Y9LoginUserHolder.setTenantId(tenantId);

        return y9PositionToRoleService.hasRole(positionId, roleId);
    }

    /**
     * 根据岗位id判断该岗位是否拥有 roleName 这个角色
     *
     * @param tenantId 租户id
     * @param systemName 系统标识
     * @param properties 角色扩展属性
     * @param roleName 角色名称
     * @param positionId 岗位id
     * @return Boolean 是否拥有
     * @since 9.6.0
     */
    @Override
    public Boolean hasRole(@RequestParam("tenantId") @NotBlank String tenantId,
        @RequestParam("systemName") @NotBlank String systemName,
        @RequestParam(value = "properties", required = false) String properties,
        @RequestParam("roleName") @NotBlank String roleName, @RequestParam("positionId") @NotBlank String positionId) {
        Y9LoginUserHolder.setTenantId(tenantId);

        return y9PositionToRoleService.hasRole(positionId, systemName, roleName, properties);
    }

    /**
     * 判断岗位是否拥有 customId 对应的角色
     *
     * @param tenantId 租户id
     * @param positionId 岗位id
     * @param customId 自定义id
     * @return Boolean 是否拥有角色
     * @since 9.6.0
     */
    @Override
    public Boolean hasRoleByCustomId(@RequestParam("tenantId") @NotBlank String tenantId,
        @RequestParam("positionId") @NotBlank String positionId, @RequestParam("customId") @NotBlank String customId) {
        Y9LoginUserHolder.setTenantId(tenantId);

        return y9PositionToRoleService.hasRoleByCustomId(positionId, customId);
    }
}
