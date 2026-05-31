package com.uams.controller;

import com.uams.common.Result;
import com.uams.entity.TeamTeam;
import com.uams.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping("/create")
    public Result<?> createTeam(@RequestBody TeamTeam team) {
        return Result.ok(teamService.createTeam(team));
    }

    @GetMapping("/list")
    public Result<?> getUserTeams() {
        return Result.ok(teamService.getUserTeams());
    }
}