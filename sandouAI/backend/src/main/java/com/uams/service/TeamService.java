package com.uams.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uams.common.AuthUtil;
import com.uams.entity.TeamMember;
import com.uams.entity.TeamTeam;
import com.uams.mapper.TeamMemberMapper;
import com.uams.mapper.TeamTeamMapper;
import com.uams.entity.SysUser;
import com.uams.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService extends ServiceImpl<TeamTeamMapper, TeamTeam> {

    private final TeamMemberMapper teamMemberMapper;
    private final FolderService folderService;
    private final SysUserMapper sysUserMapper;

    @Transactional
    public TeamTeam createTeam(TeamTeam team) {
        long count = this.count(new LambdaQueryWrapper<TeamTeam>().eq(TeamTeam::getName, team.getName()));
        if (count > 0) {
            throw new RuntimeException("团队名称已存在");
        }
        
        Long userId = AuthUtil.getUserId();
        team.setOwnerId(userId);
        team.setStatus(1);
        this.save(team);

        TeamMember member = new TeamMember();
        member.setTeamId(team.getId());
        member.setUserId(userId);
        member.setRole("owner");
        member.setStatus(1);
        teamMemberMapper.insert(member);

        folderService.create(team.getName() + "的团队文件夹", 0L, userId);

        return team;
    }

    public List<TeamTeam> getUserTeams() {
        Long userId = AuthUtil.getUserId();
        List<TeamMember> members = teamMemberMapper.selectList(
                new LambdaQueryWrapper<TeamMember>()
                    .eq(TeamMember::getUserId, userId)
                    .eq(TeamMember::getStatus, 1)
        );
        if (members.isEmpty()) {
            return List.of();
        }
        List<Long> teamIds = members.stream().map(TeamMember::getTeamId).collect(Collectors.toList());
        return this.listByIds(teamIds);
    }

    public TeamTeam getTeamDetail(Long id) {
        return this.getById(id);
    }

    @Transactional
    public void inviteMember(Map<String, String> data) {
        Long teamId = Long.valueOf(data.get("teamId"));
        String username = data.get("username");
        
        // Find user
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(SysUser::getUsername, username);
        SysUser user = sysUserMapper.selectOne(userWrapper);
        if (user == null) {
            throw new RuntimeException("找不到该用户名对应的用户：" + username);
        }
        
        // Check if already in team
        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getTeamId, teamId).eq(TeamMember::getUserId, user.getId());
        if (teamMemberMapper.selectCount(memberWrapper) > 0) {
            throw new RuntimeException("该用户已经在这个团队中了");
        }
        
        // Add member
        TeamMember member = new TeamMember();
        member.setTeamId(teamId);
        member.setUserId(user.getId());
        member.setRole("member");
        member.setStatus(0); // 0 means pending
        teamMemberMapper.insert(member);
    }

    public List<Map<String, Object>> getPendingInvites() {
        Long userId = com.uams.common.AuthUtil.getUserId();
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getUserId, userId).eq(TeamMember::getStatus, 0);
        List<TeamMember> invites = teamMemberMapper.selectList(wrapper);
        
        return invites.stream().map(invite -> {
            TeamTeam team = this.getById(invite.getTeamId());
            return Map.of(
                "id", (Object) invite.getId(),
                "teamId", invite.getTeamId(),
                "teamName", team != null ? team.getName() : "未知团队",
                "createTime", invite.getJoinTime() != null ? invite.getJoinTime().toString() : ""
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public void acceptInvite(Long inviteId) {
        TeamMember invite = teamMemberMapper.selectById(inviteId);
        if (invite != null && invite.getUserId().equals(com.uams.common.AuthUtil.getUserId())) {
            invite.setStatus(1);
            teamMemberMapper.updateById(invite);
        }
    }

    @Transactional
    public void rejectInvite(Long inviteId) {
        TeamMember invite = teamMemberMapper.selectById(inviteId);
        if (invite != null && invite.getUserId().equals(com.uams.common.AuthUtil.getUserId())) {
            teamMemberMapper.deleteById(inviteId);
        }
    }

    public List<Map<String, Object>> getTeamMembers(Long teamId) {
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, teamId).eq(TeamMember::getStatus, 1);
        List<TeamMember> members = teamMemberMapper.selectList(wrapper);
        return members.stream().map(m -> {
            SysUser u = sysUserMapper.selectById(m.getUserId());
            return Map.of(
                "id", (Object) m.getId(),
                "userId", m.getUserId(),
                "userName", u != null ? u.getUsername() : "Unknown",
                "role", m.getRole()
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public void removeMember(Long id) {
        teamMemberMapper.deleteById(id);
    }

    @Transactional
    public void deleteTeam(Long id) {
        // Delete all members
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, id);
        teamMemberMapper.delete(wrapper);
        // Delete team
        this.removeById(id);
    }
}