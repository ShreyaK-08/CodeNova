package com.oj.platform.dto;

public class AiSupportDraftRequest {

    private String tone; // PROFESSIONAL, EMPATHETIC, CONCISE
    private String customInstruction;

    public String getTone() { return tone; }
    public void setTone(String tone) { this.tone = tone; }

    public String getCustomInstruction() { return customInstruction; }
    public void setCustomInstruction(String customInstruction) { this.customInstruction = customInstruction; }
}
