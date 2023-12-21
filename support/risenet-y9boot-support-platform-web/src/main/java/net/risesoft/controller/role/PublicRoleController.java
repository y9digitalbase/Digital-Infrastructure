package net.risesoft.controller.role;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.consts.InitDataConsts;
import net.risesoft.controller.role.vo.RoleTreeNodeVO;
import net.risesoft.controller.role.vo.RoleVO;
import net.risesoft.enums.platform.RoleTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.pojo.Y9Result;
import net.risesoft.y9.util.Y9ModelConvertUtil;
import net.risesoft.y9public.entity.role.Y9Role;
import net.risesoft.y9public.service.role.Y9RoleService;

/**
 * 系统公共角色
 *
 * @author mengjuhua
 * @since 9.6.0
 *
 */
@RestController
@RequestMapping(value = "/api/rest/publicRole", produces = "application/json")
@RequiredArgsConstructor
public class PublicRoleController {

    private final Y9RoleService y9RoleService;

    /**
     * 获取系统公共角色顶节点
     *
     * @return
     */
    @RiseLog(operationName = "获取系统公共角色顶节点")
    @RequestMapping(value = "/treeRoot")
    @Deprecated
    public Y9Result<List<RoleVO>> treeRoot() {
        Y9Role y9Role = y9RoleService.getById(InitDataConsts.TOP_PUBLIC_ROLE_ID);
        List<RoleVO> y9RoleList = new ArrayList<>();
        RoleVO role = Y9ModelConvertUtil.convert(y9Role, RoleVO.class);
        role.setHasChild(Boolean.TRUE);
        y9RoleList.add(role);
        return Y9Result.success(y9RoleList, "展开应用角色树成功");
    }

    /**
     * 根据角色名称，查询公共角色节点
     *
     * @param name 角色名称
     * @return
     */
    @RiseLog(operationName = "根据角色名称，查询公共角色节点")
    @RequestMapping(value = "/treeSearch")
    @Deprecated
    public Y9Result<List<RoleVO>> treeSearch(@RequestParam String name) {
        List<RoleVO> roleVOList = new ArrayList<>();
        List<Y9Role> y9RoleList = y9RoleService.treeSearch(name, InitDataConsts.TOP_PUBLIC_ROLE_ID);
        for (Y9Role y9Role : y9RoleList) {
            RoleVO roleVO = Y9ModelConvertUtil.convert(y9Role, RoleVO.class);
            if (RoleTypeEnum.FOLDER.equals(y9Role.getType())) {
                roleVO.setHasChild(true);
            }
            roleVO.setGuidPath(y9Role.getAppId() + "," + y9Role.getGuidPath());
            roleVOList.add(roleVO);
        }
        return Y9Result.success(roleVOList, "根据角色名称查询角色节点成功");
    }

    /**
     * 获取系统公共角色顶节点
     *
     * @return
     */
    @RiseLog(operationName = "获取系统公共角色顶节点")
    @RequestMapping(value = "/treeRoot2")
    public Y9Result<List<RoleTreeNodeVO>> treeRoot2() {
        Y9Role y9Role = y9RoleService.getById(InitDataConsts.TOP_PUBLIC_ROLE_ID);
        List<Y9Role> y9RoleList = List.of(y9Role);
        return Y9Result.success(RoleTreeNodeVO.convertY9RoleList(y9RoleList), "展开应用角色树成功");
    }

    /**
     * 根据角色名称，查询公共角色节点
     *
     * @param name 角色名称
     * @return
     */
    @RiseLog(operationName = "根据角色名称，查询公共角色节点")
    @RequestMapping(value = "/treeSearch2")
    public Y9Result<List<RoleTreeNodeVO>> treeSearch2(@RequestParam String name) {
        List<Y9Role> y9RoleList = y9RoleService.treeSearch(name, InitDataConsts.TOP_PUBLIC_ROLE_ID);
        return Y9Result.success(RoleTreeNodeVO.convertY9RoleList(y9RoleList), "根据角色名称查询角色节点成功");
    }
}
