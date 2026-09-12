package org.iflytek.web.model.dto;

import java.util.Map;

public class OnlyOfficeConfigDto {
    private String documentType;
    private Map<String, Object> document;
    private Map<String, Object> editorConfig;
    private String token;
    
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    
    public Map<String, Object> getDocument() { return document; }
    public void setDocument(Map<String, Object> document) { this.document = document; }
    
    public Map<String, Object> getEditorConfig() { return editorConfig; }
    public void setEditorConfig(Map<String, Object> editorConfig) { this.editorConfig = editorConfig; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
