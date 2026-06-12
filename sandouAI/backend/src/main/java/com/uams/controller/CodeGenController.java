package com.uams.controller;

import cn.hutool.core.util.StrUtil;
import com.uams.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/tool")
@RequiredArgsConstructor
public class CodeGenController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/gen/tables")
    public Result<?> tables() {
        List<Map<String, Object>> tables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME, TABLE_COMMENT, CREATE_TIME FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'uams' ORDER BY CREATE_TIME DESC");
        return Result.ok(tables);
    }

    @GetMapping("/gen/columns")
    public Result<?> columns(@RequestParam String tableName) {
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT, IS_NULLABLE, COLUMN_KEY, " +
                "CHARACTER_MAXIMUM_LENGTH, ORDINAL_POSITION " +
                "FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'uams' AND TABLE_NAME = ? " +
                "ORDER BY ORDINAL_POSITION", tableName);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> col : columns) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("columnName", col.get("COLUMN_NAME"));
            item.put("dataType", col.get("DATA_TYPE"));
            item.put("columnComment", StrUtil.nullToDefault((String) col.get("COLUMN_COMMENT"), ""));
            item.put("isNullable", "YES".equals(col.get("IS_NULLABLE")));
            item.put("isPk", "PRI".equals(col.get("COLUMN_KEY")));
            item.put("columnLength", col.get("CHARACTER_MAXIMUM_LENGTH"));
            item.put("sort", col.get("ORDINAL_POSITION"));
            result.add(item);
        }
        return Result.ok(result);
    }

    @PostMapping("/gen/generate")
    public Result<?> generate(@RequestBody Map<String, Object> params) {
        String tableName = (String) params.get("tableName");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> columns = (List<Map<String, Object>>) params.get("columns");

        StringBuilder javaCode = new StringBuilder();
        String className = StrUtil.upperFirst(StrUtil.toCamelCase(tableName.startsWith("sys_") || tableName.startsWith("u_")
                ? tableName.substring(2) : tableName));

        javaCode.append("package com.uams.entity;\n\n");
        javaCode.append("import com.baomidou.mybatisplus.annotation.*;\n");
        javaCode.append("import lombok.Data;\n");
        javaCode.append("import java.time.LocalDateTime;\n\n");
        javaCode.append("@Data\n");
        javaCode.append("@TableName(\"").append(tableName).append("\")\n");
        javaCode.append("public class ").append(className).append(" {\n\n");

        for (Map<String, Object> col : columns) {
            String colName = (String) col.get("columnName");
            String fieldName = StrUtil.toCamelCase(colName);
            String dataType = (String) col.get("dataType");
            boolean isPk = Boolean.TRUE.equals(col.get("isPk"));

            if (isPk && "id".equals(colName)) {
                javaCode.append("    @TableId(type = IdType.AUTO)\n");
            }

            String javaType = mapDbType(dataType);
            String comment = (String) col.get("columnComment");

            if ("create_time".equals(colName)) {
                javaCode.append("    @TableField(fill = FieldFill.INSERT)\n");
            } else if ("update_time".equals(colName)) {
                javaCode.append("    @TableField(fill = FieldFill.INSERT_UPDATE)\n");
            }

            if (StrUtil.isNotBlank(comment)) {
                javaCode.append("    // ").append(comment).append("\n");
            }
            javaCode.append("    private ").append(javaType).append(" ").append(fieldName).append(";\n\n");
        }

        javaCode.append("}\n");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("className", className);
        result.put("javaCode", javaCode.toString());
        return Result.ok(result);
    }

    private String mapDbType(String dbType) {
        return switch (dbType.toLowerCase()) {
            case "varchar", "char", "text", "longtext", "mediumtext", "tinytext" -> "String";
            case "int", "tinyint", "smallint", "mediumint" -> "Integer";
            case "bigint" -> "Long";
            case "decimal", "double", "float" -> java.math.BigDecimal.class.getSimpleName();
            case "datetime", "timestamp", "date", "time" -> "LocalDateTime";
            case "bit", "boolean" -> "Boolean";
            default -> "String";
        };
    }
}
