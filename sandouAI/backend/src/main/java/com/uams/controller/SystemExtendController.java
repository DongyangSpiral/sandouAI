package com.uams.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uams.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemExtendController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/dept/list")
    public Result<?> deptList() {
        return Result.ok(jdbcTemplate.queryForList(
            "SELECT * FROM sys_dept WHERE del_flag=0 ORDER BY order_num ASC"));
    }

    @GetMapping("/dept/{id}")
    public Result<?> getDept(@PathVariable Long id) {
        return Result.ok(jdbcTemplate.queryForMap(
            "SELECT * FROM sys_dept WHERE id=?", id));
    }

    @PostMapping("/dept")
    public Result<?> addDept(@RequestBody Map<String, Object> body) {
        jdbcTemplate.update(
            "INSERT INTO sys_dept (parent_id, ancestors, dept_name, order_num, leader, phone, email, status) VALUES (?,?,?,?,?,?,?,?)",
            body.getOrDefault("parent_id", 0), body.getOrDefault("ancestors", ""),
            body.get("dept_name"), body.getOrDefault("order_num", 0),
            body.getOrDefault("leader", ""), body.getOrDefault("phone", ""),
            body.getOrDefault("email", ""), body.getOrDefault("status", 1));
        return Result.ok();
    }

    @PutMapping("/dept")
    public Result<?> updateDept(@RequestBody Map<String, Object> body) {
        jdbcTemplate.update(
            "UPDATE sys_dept SET parent_id=?,dept_name=?,order_num=?,leader=?,phone=?,email=?,status=? WHERE id=?",
            body.getOrDefault("parent_id", 0), body.get("dept_name"),
            body.getOrDefault("order_num", 0), body.getOrDefault("leader", ""),
            body.getOrDefault("phone", ""), body.getOrDefault("email", ""),
            body.getOrDefault("status", 1), body.get("id"));
        return Result.ok();
    }

    @DeleteMapping("/dept/{id}")
    public Result<?> deleteDept(@PathVariable Long id) {
        jdbcTemplate.update("UPDATE sys_dept SET del_flag=1 WHERE id=?", id);
        return Result.ok();
    }

    @GetMapping("/post/list")
    public Result<?> postList() {
        return Result.ok(jdbcTemplate.queryForList(
            "SELECT * FROM sys_post WHERE del_flag=0 ORDER BY post_sort ASC"));
    }

    @PostMapping("/post")
    public Result<?> addPost(@RequestBody Map<String, Object> body) {
        jdbcTemplate.update(
            "INSERT INTO sys_post (post_code, post_name, post_sort, status) VALUES (?,?,?,?)",
            body.get("post_code"), body.get("post_name"),
            body.getOrDefault("post_sort", 0), body.getOrDefault("status", 1));
        return Result.ok();
    }

    @PutMapping("/post")
    public Result<?> updatePost(@RequestBody Map<String, Object> body) {
        jdbcTemplate.update(
            "UPDATE sys_post SET post_code=?,post_name=?,post_sort=?,status=? WHERE id=?",
            body.get("post_code"), body.get("post_name"),
            body.getOrDefault("post_sort", 0), body.getOrDefault("status", 1),
            body.get("id"));
        return Result.ok();
    }

    @DeleteMapping("/post/{id}")
    public Result<?> deletePost(@PathVariable Long id) {
        jdbcTemplate.update("UPDATE sys_post SET del_flag=1 WHERE id=?", id);
        return Result.ok();
    }

    @GetMapping("/dict/type/list")
    public Result<?> dictTypeList() {
        return Result.ok(jdbcTemplate.queryForList(
            "SELECT * FROM sys_dict_type WHERE del_flag=0 ORDER BY create_time DESC"));
    }

    @PostMapping("/dict/type")
    public Result<?> addDictType(@RequestBody Map<String, Object> body) {
        jdbcTemplate.update(
            "INSERT INTO sys_dict_type (dict_name, dict_type, status) VALUES (?,?,?)",
            body.get("dict_name"), body.get("dict_type"), body.getOrDefault("status", 1));
        return Result.ok();
    }

    @PutMapping("/dict/type")
    public Result<?> updateDictType(@RequestBody Map<String, Object> body) {
        jdbcTemplate.update(
            "UPDATE sys_dict_type SET dict_name=?,dict_type=?,status=? WHERE id=?",
            body.get("dict_name"), body.get("dict_type"),
            body.getOrDefault("status", 1), body.get("id"));
        return Result.ok();
    }

    @DeleteMapping("/dict/type/{id}")
    public Result<?> deleteDictType(@PathVariable Long id) {
        jdbcTemplate.update("UPDATE sys_dict_type SET del_flag=1 WHERE id=?", id);
        return Result.ok();
    }

    @GetMapping("/dict/data/list")
    public Result<?> dictDataList(@RequestParam(required = false) String dictType) {
        String sql = "SELECT * FROM sys_dict_data WHERE del_flag=0";
        Object[] params = {};
        if (dictType != null && !dictType.isEmpty()) {
            sql += " AND dict_type=?";
            params = new Object[] { dictType };
        }
        sql += " ORDER BY dict_sort ASC";
        return Result.ok(jdbcTemplate.queryForList(sql, params));
    }

    @PostMapping("/dict/data")
    public Result<?> addDictData(@RequestBody Map<String, Object> body) {
        jdbcTemplate.update(
            "INSERT INTO sys_dict_data (dict_sort,dict_label,dict_value,dict_type,status) VALUES (?,?,?,?,?)",
            body.getOrDefault("dict_sort", 0), body.get("dict_label"),
            body.get("dict_value"), body.get("dict_type"),
            body.getOrDefault("status", 1));
        return Result.ok();
    }

    @PutMapping("/dict/data")
    public Result<?> updateDictData(@RequestBody Map<String, Object> body) {
        jdbcTemplate.update(
            "UPDATE sys_dict_data SET dict_sort=?,dict_label=?,dict_value=?,dict_type=?,status=? WHERE id=?",
            body.getOrDefault("dict_sort", 0), body.get("dict_label"),
            body.get("dict_value"), body.get("dict_type"),
            body.getOrDefault("status", 1), body.get("id"));
        return Result.ok();
    }

    @DeleteMapping("/dict/data/{id}")
    public Result<?> deleteDictData(@PathVariable Long id) {
        jdbcTemplate.update("UPDATE sys_dict_data SET del_flag=1 WHERE id=?", id);
        return Result.ok();
    }

    @GetMapping("/config/list")
    public Result<?> configList() {
        return Result.ok(jdbcTemplate.queryForList(
            "SELECT * FROM sys_config WHERE del_flag=0 ORDER BY id ASC"));
    }

    @PostMapping("/config")
    public Result<?> addConfig(@RequestBody Map<String, Object> body) {
        jdbcTemplate.update(
            "INSERT INTO sys_config (config_name,config_key,config_value,config_type) VALUES (?,?,?,?)",
            body.get("config_name"), body.get("config_key"),
            body.get("config_value"), body.getOrDefault("config_type", "N"));
        return Result.ok();
    }

    @PutMapping("/config")
    public Result<?> updateConfig(@RequestBody Map<String, Object> body) {
        jdbcTemplate.update(
            "UPDATE sys_config SET config_name=?,config_key=?,config_value=?,config_type=? WHERE id=?",
            body.get("config_name"), body.get("config_key"),
            body.get("config_value"), body.getOrDefault("config_type", "N"),
            body.get("id"));
        return Result.ok();
    }

    @DeleteMapping("/config/{id}")
    public Result<?> deleteConfig(@PathVariable Long id) {
        jdbcTemplate.update("UPDATE sys_config SET del_flag=1 WHERE id=?", id);
        return Result.ok();
    }

    @GetMapping("/notice/list")
    public Result<?> noticeList() {
        return Result.ok(jdbcTemplate.queryForList(
            "SELECT * FROM sys_notice WHERE del_flag=0 ORDER BY create_time DESC"));
    }

    @PostMapping("/notice")
    public Result<?> addNotice(@RequestBody Map<String, Object> body) {
        jdbcTemplate.update(
            "INSERT INTO sys_notice (notice_title,notice_type,notice_content,status) VALUES (?,?,?,?)",
            body.get("notice_title"), body.getOrDefault("notice_type", "1"),
            body.getOrDefault("notice_content", ""), body.getOrDefault("status", 1));
        return Result.ok();
    }

    @PutMapping("/notice")
    public Result<?> updateNotice(@RequestBody Map<String, Object> body) {
        jdbcTemplate.update(
            "UPDATE sys_notice SET notice_title=?,notice_type=?,notice_content=?,status=? WHERE id=?",
            body.get("notice_title"), body.getOrDefault("notice_type", "1"),
            body.getOrDefault("notice_content", ""), body.getOrDefault("status", 1),
            body.get("id"));
        return Result.ok();
    }

    @DeleteMapping("/notice/{id}")
    public Result<?> deleteNotice(@PathVariable Long id) {
        jdbcTemplate.update("UPDATE sys_notice SET del_flag=1 WHERE id=?", id);
        return Result.ok();
    }

    @GetMapping("/operlog/list")
    public Result<?> operlogList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String operName,
            @RequestParam(required = false) String title) {
        StringBuilder sql = new StringBuilder("SELECT * FROM sys_oper_log WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (operName != null && !operName.isEmpty()) { sql.append(" AND oper_name LIKE ?"); params.add("%" + operName + "%"); }
        if (title != null && !title.isEmpty()) { sql.append(" AND title LIKE ?"); params.add("%" + title + "%"); }
        int total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM (" + sql + ") t", Long.class, params.toArray()).intValue();
        sql.append(" ORDER BY oper_time DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((pageNum - 1) * pageSize);
        List<Map<String, Object>> records = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("size", pageSize);
        result.put("current", pageNum);
        return Result.ok(result);
    }

    @DeleteMapping("/operlog/{id}")
    public Result<?> deleteOperlog(@PathVariable Long id) {
        jdbcTemplate.update("DELETE FROM sys_oper_log WHERE id=?", id);
        return Result.ok();
    }

    @GetMapping("/operlog/clean")
    public Result<?> cleanOperlog() {
        jdbcTemplate.update("DELETE FROM sys_oper_log");
        return Result.ok();
    }
}
