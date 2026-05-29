package com.uams.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uams.entity.SysMenu;
import com.uams.mapper.SysMenuMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysMenuService extends ServiceImpl<SysMenuMapper, SysMenu> {

    public List<SysMenu> tree() {
        List<SysMenu> all = baseMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSortOrder));
        return buildTree(all, 0L);
    }

    private List<SysMenu> buildTree(List<SysMenu> all, Long parentId) {
        List<SysMenu> result = new ArrayList<>();
        for (SysMenu menu : all) {
            if (menu.getParentId().equals(parentId)) {
                List<SysMenu> children = buildTree(all, menu.getId());
                menu.setChildren(children);
                result.add(menu);
            }
        }
        return result;
    }

    @Transactional
    public void add(SysMenu menu) {
        baseMapper.insert(menu);
    }

    @Transactional
    public void update(SysMenu menu) {
        baseMapper.updateById(menu);
    }

    @Transactional
    public void delete(Long id) {
        baseMapper.deleteById(id);
    }

    public List<SysMenu> listAll() {
        return baseMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSortOrder));
    }
}
