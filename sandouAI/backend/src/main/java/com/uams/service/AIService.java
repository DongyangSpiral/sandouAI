package com.uams.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.uams.entity.DfsFile;
import com.uams.mapper.DfsFileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    @Value("${ai.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.deepseek.endpoint}")
    private String endpoint;

    private final DfsFileMapper fileMapper;
    private final StorageService storageService;

    public String chat(String prompt) {
        return callDeepSeek(prompt);
    }

    public String summarize(Long fileId) {
        String content = extractText(fileId);
        if (content.isEmpty()) return "无法提取该文件的文本内容。";
        String prompt = "请为以下内容生成一段精准的摘要，字数在200字以内：\n\n" + content;
        return callDeepSeek(prompt);
    }

    public String analyze(Long fileId, String question) {
        String content = extractText(fileId);
        if (content.isEmpty()) return "无法提取该文件的文本内容。";
        String prompt = "根据以下文档内容回答问题。如果文档中没有相关信息，请明确告知。\n\n文档内容：\n" + content + "\n\n问题：" + question;
        return callDeepSeek(prompt);
    }

    private String extractText(Long fileId) {
        try {
            DfsFile dfsFile = fileMapper.selectById(fileId);
            if (dfsFile == null) return "";
            
            String ext = dfsFile.getExtension() != null ? dfsFile.getExtension().toLowerCase() : "";
            if (ext.matches("\\.(png|jpg|jpeg|gif|webp|bmp|zip|rar|7z|tar|gz)")) {
                return "【系统提示】您选择的是图片或压缩包等非文本文件，AI 无法直接从二进制中提取纯文本。请尝试上传 PDF、Word、TXT 或代码文档。";
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            storageService.download(dfsFile.getBucket() + "/" + dfsFile.getStoragePath(), baos);
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            
            Tika tika = new Tika();
            String text = tika.parseToString(is);
            
            if (text != null && text.length() > 8000) {
                text = text.substring(0, 8000);
            }
            return text != null ? text.trim() : "";
        } catch (Exception e) {
            log.error("Failed to extract text for AI analysis", e);
            return "";
        }
    }

    private String callDeepSeek(String prompt) {
        try {
            JSONObject body = new JSONObject();
            body.set("model", "deepseek-chat");
            
            JSONArray messages = new JSONArray();
            JSONObject msg = new JSONObject();
            msg.set("role", "user");
            msg.set("content", prompt);
            messages.add(msg);
            body.set("messages", messages);
            
            HttpResponse response = HttpRequest.post(endpoint)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(body.toString())
                    .timeout(60000)
                    .execute();
                    
            if (response.isOk()) {
                JSONObject resObj = JSONUtil.parseObj(response.body());
                return resObj.getJSONArray("choices")
                             .getJSONObject(0)
                             .getJSONObject("message")
                             .getStr("content");
            } else {
                log.error("DeepSeek API error: {}", response.body());
                return "AI 响应错误，请检查后台配置。";
            }
        } catch (Exception e) {
            log.error("DeepSeek call failed", e);
            return "AI 调用超时或失败，请检查网络。";
        }
    }
}