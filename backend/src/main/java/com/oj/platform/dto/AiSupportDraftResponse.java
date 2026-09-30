package com.oj.platform.dto;

public class AiSupportDraftResponse {

    private String draftReply;
    private String suggestedStatus;
    private String suggestedPriority;
    private String rationale;

    public AiSupportDraftResponse() {}

    public AiSupportDraftResponse(String draftReply, String suggestedStatus, String suggestedPriority, String rationale) {
        this.draftReply = draftReply;
        this.suggestedStatus = suggestedStatus;
        this.suggestedPriority = suggestedPriority;
        this.rationale = rationale;
    }

    public String getDraftReply() { return draftReply; }
    public void setDraftReply(String draftReply) { this.draftReply = draftReply; }

    public String getSuggestedStatus() { return suggestedStatus; }
    public void setSuggestedStatus(String suggestedStatus) { this.suggestedStatus = suggestedStatus; }

    public String getSuggestedPriority() { return suggestedPriority; }
    public void setSuggestedPriority(String suggestedPriority) { this.suggestedPriority = suggestedPriority; }

    public String getRationale() { return rationale; }
    public void setRationale(String rationale) { this.rationale = rationale; }
}
