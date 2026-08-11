package com.hackathon.kiosk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps directly to the JSON schema defined in the Gemini system prompt.
 */
public class GeminiParsedResponse {
    @JsonProperty("detected_language")
    private String detectedLanguage;
    @JsonProperty("language_name")
    private String languageName;
    private String intent;
    private Entities entities;
    @JsonProperty("reply_text")
    private String replyText;
    @JsonProperty("needs_followup")
    private boolean needsFollowup;
    @JsonProperty("followup_question")
    private String followupQuestion;

    public String getDetectedLanguage() { return detectedLanguage; }
    public void setDetectedLanguage(String detectedLanguage) { this.detectedLanguage = detectedLanguage; }
    public String getLanguageName() { return languageName; }
    public void setLanguageName(String languageName) { this.languageName = languageName; }
    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
    public Entities getEntities() { return entities; }
    public void setEntities(Entities entities) { this.entities = entities; }
    public String getReplyText() { return replyText; }
    public void setReplyText(String replyText) { this.replyText = replyText; }
    public boolean isNeedsFollowup() { return needsFollowup; }
    public void setNeedsFollowup(boolean needsFollowup) { this.needsFollowup = needsFollowup; }
    public String getFollowupQuestion() { return followupQuestion; }
    public void setFollowupQuestion(String followupQuestion) { this.followupQuestion = followupQuestion; }
}
