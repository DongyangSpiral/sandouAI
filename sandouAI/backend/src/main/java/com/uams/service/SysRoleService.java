package com.uams.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uams.entity.SysMenu;
import com.uams.entity.SysRole;
import com.uams.entity.SysRoleMenu;
import com.uams.mapper.SysMenuMapper;
import com.uams.mapper.SysRoleMapper;
import com.uams.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysMenuMapper sysMenuMapper;

    public Page<SysRole> page(Page<SysRole> page, String roleName) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(roleName != null && !roleName.isEmpty(), SysRole::getRoleName, roleName);
        wrapper.orderByAsc(SysRole::getSortOrder);
        return baseMapper.selectPage(page, wrapper);
    }

    public List<SysRole> listAll() {
        return baseMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getSortOrder));
    }

    @Transactional
    public void add(SysRole role) {
        if (exists(role.getRoleKey())) {
            throw new RuntimeException("角色标识已存在");
        }
        baseMapper.insert(role);
    }

    @Transactional
    public void update(SysRole role) {
        baseMapper.updateById(role);
    }

    @Transactional
    public void delete(Long id) {
        baseMapper.deleteById(id);
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, id);
        sysRoleMenuMapper.delete(wrapper);
    }

    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId);
        sysRoleMenuMapper.delete(wrapper);
        if (menuIds != null && !menuIds.isEmpty()) {
            menuIds.forEach(menuId -> {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                sysRoleMenuMapper.insert(rm);
            });
        }
    }

    public List<Long> getRoleMenuIds(Long roleId) {
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId);
        return sysRoleMenuMapper.selectList(wrapper).stream()
                .map(SysRoleMenu::getMenuId).collect(Collectors.toList());
    }

    public List<SysMenu> getMenusByRoleId(Long roleId) {
        List<Long> menuIds = getRoleMenuIds(roleId);
        if (menuIds.isEmpty()) {
            return new ArrayList<>();
        }
        return sysMenuMapper.selectBatchIds(menuIds);
    }

    private boolean exists(String roleKey) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleKey, roleKey);
        return baseMapper.selectCount(wrapper) > 0;
    }
}
