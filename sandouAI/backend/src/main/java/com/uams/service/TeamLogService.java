package com.uams.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uams.entity.TeamLog;
import com.uams.mapper.TeamLogMapper;
import org.springframework.stereotype.Service;

@Service
public class TeamLogService extends ServiceImpl<TeamLogMapper, TeamLog> {
}