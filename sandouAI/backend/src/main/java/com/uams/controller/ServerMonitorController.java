package com.uams.controller;

import com.uams.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.*;
import oshi.util.FormatUtil;

import java.lang.management.*;
import java.util.*;

@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class ServerMonitorController {

    @GetMapping("/server")
    public Result<?> serverInfo() {
        Map<String, Object> result = new LinkedHashMap<>();

        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();

        Map<String, Object> cpu = new LinkedHashMap<>();
        CentralProcessor processor = hal.getProcessor();
        cpu.put("cpuNum", processor.getLogicalProcessorCount());
        cpu.put("total", (double) Math.round(processor.getSystemCpuLoad(1000) * 10000) / 100);
        cpu.put("sys", (double) Math.round(hal.getProcessor().getSystemCpuLoad(1000) * 10000) / 100);
        cpu.put("used", (double) Math.round(hal.getProcessor().getSystemCpuLoad(1000) * 10000) / 100);
        cpu.put("wait", 0.0);
        cpu.put("free", (double) Math.round((1 - hal.getProcessor().getSystemCpuLoad(1000)) * 10000) / 100);
        result.put("cpu", cpu);

        Map<String, Object> mem = new LinkedHashMap<>();
        GlobalMemory memory = hal.getMemory();
        mem.put("total", memory.getTotal());
        mem.put("used", memory.getTotal() - memory.getAvailable());
        mem.put("free", memory.getAvailable());
        mem.put("usage", (double) Math.round((memory.getTotal() - memory.getAvailable()) * 10000.0 / memory.getTotal()) / 100);
        result.put("mem", mem);

        Map<String, Object> jvm = new LinkedHashMap<>();
        Runtime runtime = Runtime.getRuntime();
        jvm.put("total", runtime.totalMemory());
        jvm.put("max", runtime.maxMemory());
        jvm.put("free", runtime.freeMemory());
        jvm.put("used", runtime.totalMemory() - runtime.freeMemory());
        jvm.put("usage", (double) Math.round((runtime.totalMemory() - runtime.freeMemory()) * 10000.0 / runtime.totalMemory()) / 100);
        jvm.put("version", System.getProperty("java.version"));
        jvm.put("home", System.getProperty("java.home"));
        result.put("jvm", jvm);

        Map<String, Object> sys = new LinkedHashMap<>();
        sys.put("computerName", hal.getComputerSystem().getHardwareUUID());
        sys.put("osName", os.toString());
        sys.put("osArch", System.getProperty("os.arch"));
        sys.put("userDir", System.getProperty("user.dir"));
        result.put("sys", sys);

        List<Map<String, Object>> sysFiles = new ArrayList<>();
        for (OSFileStore fs : os.getFileSystem().getFileStores()) {
            Map<String, Object> fileMap = new LinkedHashMap<>();
            fileMap.put("dirName", fs.getMount());
            fileMap.put("typeName", fs.getType());
            fileMap.put("total", fs.getTotalSpace());
            fileMap.put("free", fs.getFreeSpace());
            fileMap.put("used", fs.getTotalSpace() - fs.getFreeSpace());
            fileMap.put("usage", (double) Math.round((fs.getTotalSpace() - fs.getFreeSpace()) * 10000.0 / fs.getTotalSpace()) / 100);
            sysFiles.add(fileMap);
        }
        result.put("sysFiles", sysFiles);

        return Result.ok(result);
    }

    @GetMapping("/cache")
    public Result<?> cacheInfo() {
        Map<String, Object> result = new LinkedHashMap<>();
        Runtime runtime = Runtime.getRuntime();

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("redisEnabled", false);
        info.put("cacheType", "内存缓存 (Sa-Token)");
        result.put("info", info);

        Map<String, Object> memory = new LinkedHashMap<>();
        memory.put("total", runtime.totalMemory());
        memory.put("used", runtime.totalMemory() - runtime.freeMemory());
        memory.put("free", runtime.freeMemory());
        memory.put("max", runtime.maxMemory());
        result.put("memory", memory);

        result.put("commandStats", new ArrayList<>());

        return Result.ok(result);
    }
}
