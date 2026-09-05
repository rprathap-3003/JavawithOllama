package com.prathap.ai.mcpagentclient.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AgentController {

    private final ChatClient chatClient;
    private final ToolCallbackProvider mcpTools;

    public AgentController(ChatClient chatClient, ToolCallbackProvider mcpTools) {
        this.chatClient = chatClient;
        this.mcpTools = mcpTools;
    }

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return Map.of("response", "Please provide a non-empty message.");
        }

        try {
            // Prompts the LLM. The LLM will automatically trigger the MCP tools (create, update, list, delete) as needed.
            String aiResponse = chatClient.prompt()
                    .user(userMessage)
                    .call()
                    .content();
            
            return Map.of("response", aiResponse != null ? aiResponse : "No response from AI model.");
        } catch (Exception e) {
            return Map.of("response", "Error: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        
        ToolCallback[] callbacks = mcpTools.getToolCallbacks();
        int toolCount = (callbacks != null) ? callbacks.length : 0;
        
        status.put("mcpServerConnected", toolCount > 0);
        status.put("discoveredToolsCount", toolCount);
        
        List<Map<String, String>> toolsList = new ArrayList<>();
        if (callbacks != null) {
            for (ToolCallback tc : callbacks) {
                Map<String, String> toolInfo = new HashMap<>();
                toolInfo.put("name", tc.getToolDefinition().name());
                toolInfo.put("description", tc.getToolDefinition().description());
                toolsList.add(toolInfo);
            }
        }
        status.put("tools", toolsList);
        
        return status;
    }
}
