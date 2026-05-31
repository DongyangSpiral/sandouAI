package com.uams.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uams.common.AuthUtil;
import com.uams.entity.TeamMember;
import com.uams.entity.TeamTeam;
import com.uams.mapper.TeamMemberMapper;
import com.uams.mapper.TeamTeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService extends ServiceImpl<TeamTeamMapper, TeamTeam> {

    private final TeamMemberMapper teamMemberMapper;
    private final FolderService folderService;

    @Transactional
    public TeamTeam createTeam(TeamTeam team) {
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
                new LambdaQueryWrapper<TeamMember>().eq(TeamMember::getUserId, userId)
        );
        if (members.isEmpty()) {
            return List.of();
        }
        List<Long> teamIds = members.stream().map(TeamMember::getTeamId).collect(Collectors.toList());
        return this.listByIds(teamIds);
    }
}