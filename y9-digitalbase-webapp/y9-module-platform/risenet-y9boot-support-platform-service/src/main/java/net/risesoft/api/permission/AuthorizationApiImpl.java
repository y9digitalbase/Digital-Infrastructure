package net.risesoft.api.permission;

import javax.validation.constraints.NotBlank;

import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.api.platform.permission.AuthorizationApi;
import net.risesoft.entity.Y9Person;
import net.risesoft.entity.permission.Y9Authorization;
import net.risesoft.enums.platform.AuthorityEnum;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.authorization.Y9AuthorizationService;
import net.risesoft.service.org.Y9PersonService;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * 权限管理组件
 *
 * @author dingzhaojun
 * @author qinman
 * @author mengjuhua
 * @date 2022/2/10
 * @since 9.6.0
 */
@Primary
@Validated
@RestController
@RequestMapping(value = "/services/rest/v1/authorization", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthorizationApiImpl implements AuthorizationApi {

    private final Y9AuthorizationService y9AuthorizationService;
    private final Y9PersonService y9PersonService;

    /**
     * 保存授权信息
     *
     * @param tenantId 租户id
     * @param personId 人员id
     * @param resourceId 资源id
     * @param roleId 角色id
     * @param authority 操作类型 {@link AuthorityEnum}
     * @return {@code Y9Result<Object>}
     * @since 9.6.0
     */
    @Override
    public Y9Result<Object> save(@RequestParam("tenantId") @NotBlank String tenantId,
        @RequestParam("personId") @NotBlank String personId, @RequestParam("resourceId") @NotBlank String resourceId,
        @RequestParam("roleId") @NotBlank String roleId, @RequestParam("authority") AuthorityEnum authority) {
        Y9LoginUserHolder.setTenantId(tenantId);

        Y9Person y9Person = y9PersonService.getById(personId);
        Y9Authorization y9Authorization = new Y9Authorization();
        y9Authorization.setAuthorizer(y9Person.getName());
        y9Authorization.setAuthority(authority);
        y9Authorization.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        y9Authorization.setResourceId(resourceId);
        y9Authorization.setPrincipalId(roleId);
        y9Authorization.setTenantId(tenantId);

        y9AuthorizationService.saveOrUpdateRole(y9Authorization);
        return Y9Result.success();
    }

}
