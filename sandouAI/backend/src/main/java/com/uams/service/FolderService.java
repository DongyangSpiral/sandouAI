package com.uams.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uams.entity.DfsFile;
import com.uams.entity.DfsFileFolder;
import com.uams.entity.DfsFolder;
import com.uams.mapper.DfsFileMapper;
import com.uams.mapper.DfsFileFolderMapper;
import com.uams.mapper.DfsFolderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderService extends ServiceImpl<DfsFolderMapper, DfsFolder> {

    private final DfsFileFolderMapper dfsFileFolderMapper;
    private final DfsFileMapper dfsFileMapper;

    @Transactional
    public DfsFolder create(String name, Long parentId, Long userId) {
        DfsFolder folder = new DfsFolder();
        folder.setName(name);
        folder.setParentId(parentId != null ? parentId : 0);
        folder.setOwnerId(userId);
        folder.setType("personal");
        folder.setSortOrder(0);
        baseMapper.insert(folder);
        return folder;
    }

    @Transactional
    public void rename(Long id, String newName) {
        DfsFolder folder = baseMapper.selectById(id);
        if (folder == null) {
            throw new RuntimeException("目录不存在");
        }
        folder.setName(newName);
        baseMapper.updateById(folder);
    }

    @Transactional
    public void deleteFolder(Long id) {
        List<Long> allIds = new ArrayList<>();
        collectDescendantIds(id, allIds);
        allIds.add(id);
        baseMapper.deleteBatchIds(allIds);
        LambdaQueryWrapper<DfsFileFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(DfsFileFolder::getFolderId, allIds);
        dfsFileFolderMapper.delete(wrapper);
    }

    public List<DfsFolder> tree(Long userId) {
        LambdaQueryWrapper<DfsFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DfsFolder::getOwnerId, userId);
        wrapper.eq(DfsFolder::getType, "personal");
        wrapper.orderByAsc(DfsFolder::getSortOrder);
        List<DfsFolder> all = baseMapper.selectList(wrapper);
        return buildTree(all, 0L);
    }

    @Transactional
    public void move(Long id, Long newParentId) {
        DfsFolder folder = baseMapper.selectById(id);
        if (folder == null) {
            throw new RuntimeException("目录不存在");
        }
        folder.setParentId(newParentId);
        baseMapper.updateById(folder);
    }

    public Map<String, Object> listContent(Long folderId, Long userId, Page<DfsFile> filePage) {
        if (folderId == null || folderId == 0) {
            LambdaQueryWrapper<DfsFolder> rootWrapper = new LambdaQueryWrapper<>();
            rootWrapper.eq(DfsFolder::getOwnerId, userId);
            rootWrapper.eq(DfsFolder::getParentId, 0);
            rootWrapper.eq(DfsFolder::getType, "personal");
            List<DfsFolder> roots = baseMapper.selectList(rootWrapper);
            if (roots.isEmpty()) {
                DfsFolder root = create("根目录", 0L, userId);
                folderId = root.getId();
            } else {
                folderId = roots.get(0).getId();
            }
        }

        LambdaQueryWrapper<DfsFolder> subFolderWrapper = new LambdaQueryWrapper<>();
        subFolderWrapper.eq(DfsFolder::getParentId, folderId);
        List<DfsFolder> subFolders = baseMapper.selectList(subFolderWrapper);

        LambdaQueryWrapper<DfsFile> fileWrapper = new LambdaQueryWrapper<>();
        fileWrapper.inSql(DfsFile::getId, "SELECT file_id FROM dfs_file_folder WHERE folder_id=" + folderId);
        fileWrapper.orderByDesc(DfsFile::getCreateTime);
        Page<DfsFile> fileResult = dfsFileMapper.selectPage(filePage, fileWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("folders", subFolders);
        result.put("files", fileResult.getRecords());
        result.put("total", fileResult.getTotal());
        result.put("currentFolder", baseMapper.selectById(folderId));
        return result;
    }

    private void collectDescendantIds(Long parentId, List<Long> ids) {
        LambdaQueryWrapper<DfsFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DfsFolder::getParentId, parentId);
        List<DfsFolder> children = baseMapper.selectList(wrapper);
        for (DfsFolder child : children) {
            ids.add(child.getId());
            collectDescendantIds(child.getId(), ids);
        }
    }

    private List<DfsFolder> buildTree(List<DfsFolder> nodes, Long parentId) {
        return nodes.stream()
                .filter(n -> Objects.equals(n.getParentId(), parentId))
                .peek(n -> n.setChildren(buildTree(nodes, n.getId())))
                .collect(Collectors.toList());
    }
}
