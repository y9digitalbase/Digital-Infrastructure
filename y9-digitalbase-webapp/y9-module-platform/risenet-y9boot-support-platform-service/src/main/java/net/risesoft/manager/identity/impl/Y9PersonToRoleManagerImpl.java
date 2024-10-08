package net.risesoft.manager.identity.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.Y9Person;
import net.risesoft.entity.identity.person.Y9PersonToRole;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.manager.identity.Y9PersonToRoleManager;
import net.risesoft.repository.identity.person.Y9PersonToRoleRepository;
import net.risesoft.y9public.entity.role.Y9Role;

/**
 * 人员角色关联 Manager 实现类
 *
 * @author shidaobang
 * @date 2023/06/14
 * @since 9.6.2
 */
@Service
@Transactional(value = "rsTenantTransactionManager", readOnly = true)
@RequiredArgsConstructor
public class Y9PersonToRoleManagerImpl implements Y9PersonToRoleManager {

    private final Y9PersonToRoleRepository y9PersonToRoleRepository;

    @Override
    public List<String> findRoleIdByPersonId(String personId) {
        return y9PersonToRoleRepository.findRoleIdByPersonId(personId);
    }

    @Override
    @Transactional(readOnly = false)
    public void removeByPersonIdAndRoleId(String personId, String roleId) {
        Optional<Y9PersonToRole> y9PersonToRoleOptional =
            y9PersonToRoleRepository.findByPersonIdAndRoleId(personId, roleId);
        if (y9PersonToRoleOptional.isPresent()) {
            y9PersonToRoleRepository.deleteById(y9PersonToRoleOptional.get().getId());
        }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void save(Y9Person person, Y9Role role) {
        Optional<Y9PersonToRole> personToRoleOptional =
            y9PersonToRoleRepository.findByPersonIdAndRoleId(person.getId(), role.getId());
        if (personToRoleOptional.isEmpty()) {
            Y9PersonToRole y9PersonToRole = new Y9PersonToRole();
            y9PersonToRole.setId(Y9IdGenerator.genId());
            y9PersonToRole.setTenantId(person.getTenantId());
            y9PersonToRole.setPersonId(person.getId());
            y9PersonToRole.setRoleId(role.getId());
            y9PersonToRole.setAppId(role.getAppId());
            y9PersonToRole.setSystemId(role.getSystemId());
            y9PersonToRoleRepository.save(y9PersonToRole);
        } else {
            Y9PersonToRole y9PersonToRole = personToRoleOptional.get();
            y9PersonToRole.setAppId(role.getAppId());
            y9PersonToRole.setSystemId(role.getSystemId());
            y9PersonToRoleRepository.save(y9PersonToRole);
        } 
    }

}
