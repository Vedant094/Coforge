package com.hackathon.kiosk.dto;

import java.util.Map;

public class ChatResponse {
    private String detectedLanguage;
    private String languageName;
    private String intent;
    private String replyText;
    private boolean needsFollowup;
    private String followupQuestion;
    private Map<String, Object> data; // extra structured payload (seat map, boarding pass, etc.)

    public String getDetectedLanguage() { return detectedLanguage; }
    public void setDetectedLanguage(String detectedLanguage) { this.detectedLanguage = detectedLanguage; }
    public String getLanguageName() { return languageName; }
    public void setLanguageName(String languageName) { this.languageName = languageName; }
    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
    public String getReplyText() { return replyText; }
    public void setReplyText(String replyText) { this.replyText = replyText; }
    public boolean isNeedsFollowup() { return needsFollowup; }
    public void setNeedsFollowup(boolean needsFollowup) { this.needsFollowup = needsFollowup; }
    public String getFollowupQuestion() { return followupQuestion; }
    public void setFollowupQuestion(String followupQuestion) { this.followupQuestion = followupQuestion; }
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
}
