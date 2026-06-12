package com.uams.controller;

import com.uams.common.Result;
import com.uams.entity.TeamTeam;
import com.uams.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping("/create")
    public Result<?> createTeam(@RequestBody TeamTeam team) {
        log.info("=== TEST LOG: API /api/team/create called ===");
        log.info("Team data: {}", team);
        try {
            if (com.uams.common.AuthUtil.getUserId() != 1L) {
                return Result.error("只有系统管理员才能创建团队");
            }
            TeamTeam created = teamService.createTeam(team);
            log.info("=== TEST LOG: Team created successfully with ID: {} ===", created.getId());
            return Result.ok(created);
        } catch (Exception e) {
            log.error("=== TEST LOG: Team creation failed ===", e);
            return Result.error("Team creation failed: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<?> getUserTeams() {
        return Result.ok(teamService.getUserTeams());
    }

    @GetMapping("/{id}")
    public Result<?> getTeamDetail(@PathVariable Long id) {
        return Result.ok(teamService.getTeamDetail(id));
    }

    @PostMapping("/invite")
    public Result<?> inviteMember(@RequestBody java.util.Map<String, String> data) {
        try {
            teamService.inviteMember(data);
            return Result.ok("邀请成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/member/list")
    public Result<?> getTeamMembers(@RequestParam("teamId") Long teamId) {
        return Result.ok(teamService.getTeamMembers(teamId));
    }

    @GetMapping("/invites")
    public Result<?> getPendingInvites() {
        return Result.ok(teamService.getPendingInvites());
    }

    @PostMapping("/invite/accept")
    public Result<?> acceptInvite(@RequestBody java.util.Map<String, Long> data) {
        teamService.acceptInvite(data.get("inviteId"));
        return Result.ok("已同意邀请");
    }

    @PostMapping("/invite/reject")
    public Result<?> rejectInvite(@RequestBody java.util.Map<String, Long> data) {
        teamService.rejectInvite(data.get("inviteId"));
        return Result.ok("已拒绝邀请");
    }

    @DeleteMapping("/member/{id}")
    public Result<?> removeMember(@PathVariable Long id) {
        teamService.removeMember(id);
        return Result.ok("移除成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> deleteTeam(@PathVariable Long id) {
        if (com.uams.common.AuthUtil.getUserId() != 1L) {
            return Result.error("只有系统管理员才能解散团队");
        }
        teamService.deleteTeam(id);
        return Result.ok("团队已解散");
    }
}