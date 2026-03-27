package com.housekeeping.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.auth.entity.SysRoleEntity;
import com.housekeeping.auth.entity.SysUserEntity;
import com.housekeeping.auth.entity.SysUserRoleEntity;
import com.housekeeping.auth.mapper.SysRoleMapper;
import com.housekeeping.auth.mapper.SysUserMapper;
import com.housekeeping.auth.mapper.SysUserRoleMapper;
import com.housekeeping.auth.util.PasswordUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AuthDataInitializer implements CommandLineRunner {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    public AuthDataInitializer(SysUserMapper sysUserMapper,
                               SysRoleMapper sysRoleMapper,
                               SysUserRoleMapper sysUserRoleMapper) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
    }

    @Override
    public void run(String... args) {
        SysRoleEntity userRole = ensureRole("USER", "普通用户");
        SysRoleEntity workerRole = ensureRole("WORKER", "家政服务人员");
        SysRoleEntity adminRole = ensureRole("ADMIN", "平台管理员");

        SysUserEntity user = ensureUser("13800000011", "演示用户");
        SysUserEntity worker = ensureUser("13800000022", "演示服务人员");
        SysUserEntity admin = ensureUser("13800000033", "演示管理员");

        ensureUserRole(user.getId(), userRole.getId());
        ensureUserRole(worker.getId(), workerRole.getId());
        ensureUserRole(admin.getId(), adminRole.getId());
    }

    private SysRoleEntity ensureRole(String roleCode, String roleName) {
        SysRoleEntity entity = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRoleEntity>()
                .eq(SysRoleEntity::getRoleCode, roleCode)
                .last("limit 1"));
        if (entity != null) {
            return entity;
        }
        entity = new SysRoleEntity(roleCode, roleName);
        sysRoleMapper.insert(entity);
        return entity;
    }

    private SysUserEntity ensureUser(String phone, String realName) {
        SysUserEntity entity = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUserEntity>()
                .eq(SysUserEntity::getPhone, phone)
                .last("limit 1"));
        if (entity != null) {
            return entity;
        }
        entity = new SysUserEntity(phone, PasswordUtil.sha256("123456"), realName, "ACTIVE");
        sysUserMapper.insert(entity);
        return entity;
    }

    private void ensureUserRole(Long userId, Long roleId) {
        Long count = sysUserRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRoleEntity>()
                .eq(SysUserRoleEntity::getUserId, userId)
                .eq(SysUserRoleEntity::getRoleId, roleId));
        if (count == 0) {
            sysUserRoleMapper.insert(new SysUserRoleEntity(userId, roleId));
        }
    }
}
