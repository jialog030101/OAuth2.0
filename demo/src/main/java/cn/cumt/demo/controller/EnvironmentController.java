package cn.cumt.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping("/api")
public class EnvironmentController {

    @GetMapping("/env")
    public Map<String, Object> getEnvironment() {
        Map<String, Object> envInfo = new LinkedHashMap<>(); // 使用LinkedHashMap保持顺序
        
        // Java版本信息
        Properties props = System.getProperties();
        Map<String, Object> javaInfo = new LinkedHashMap<>();
        javaInfo.put("版本", props.getProperty("java.version"));
        javaInfo.put("供应商", props.getProperty("java.vendor"));
        javaInfo.put("供应商URL", props.getProperty("java.vendor.url"));
        javaInfo.put("安装路径", props.getProperty("java.home"));
        javaInfo.put("类版本", props.getProperty("java.class.version"));
        javaInfo.put("规范版本", props.getProperty("java.specification.version"));
        javaInfo.put("虚拟机版本", props.getProperty("java.vm.version"));
        javaInfo.put("虚拟机名称", props.getProperty("java.vm.name"));
        
        // 操作系统信息
        Map<String, Object> osInfo = new LinkedHashMap<>();
        osInfo.put("名称", props.getProperty("os.name"));
        osInfo.put("架构", props.getProperty("os.arch"));
        osInfo.put("版本", props.getProperty("os.version"));
        
        // 用户信息
        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("用户名", props.getProperty("user.name"));
        userInfo.put("主目录", props.getProperty("user.home"));
        userInfo.put("当前目录", props.getProperty("user.dir"));
        userInfo.put("时区", props.getProperty("user.timezone"));
        userInfo.put("语言", props.getProperty("user.language"));
        userInfo.put("国家", props.getProperty("user.country"));
        
        // 运行时信息
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> runtimeInfo = new LinkedHashMap<>();
        runtimeInfo.put("处理器数量", runtime.availableProcessors());
        runtimeInfo.put("空闲内存", formatSize(runtime.freeMemory()));
        runtimeInfo.put("最大内存", formatSize(runtime.maxMemory()));
        runtimeInfo.put("总内存", formatSize(runtime.totalMemory()));
        runtimeInfo.put("已用内存", formatSize(runtime.totalMemory() - runtime.freeMemory()));
        
        // 添加一些系统属性
        Map<String, Object> systemInfo = new LinkedHashMap<>();
        systemInfo.put("文件编码", props.getProperty("file.encoding"));
        systemInfo.put("文件分隔符", props.getProperty("file.separator"));
        systemInfo.put("行分隔符", props.getProperty("line.separator").replace("\n", "\\n").replace("\r", "\\r"));
        systemInfo.put("路径分隔符", props.getProperty("path.separator"));
        
        envInfo.put("Java信息", javaInfo);
        envInfo.put("操作系统", osInfo);
        envInfo.put("用户信息", userInfo);
        envInfo.put("运行时", runtimeInfo);
        envInfo.put("系统属性", systemInfo);
        
        return envInfo;
    }
    
    /**
     * 格式化字节大小为人类可读格式
     */
    private String formatSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
    
    @GetMapping("/hello")
    public String hello() {
        return "World hello cumter！！！";
    }
}