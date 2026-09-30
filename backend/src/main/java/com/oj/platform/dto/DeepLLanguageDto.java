package com.oj.platform.dto;

public class DeepLLanguageDto {
    private String code;
    private String name;
    private String nativeName;
    private String direction;
    private String provider;

    public DeepLLanguageDto() {
    }

    public DeepLLanguageDto(String code, String name, String nativeName, String direction) {
        this(code, name, nativeName, direction, null);
    }

    public DeepLLanguageDto(String code, String name, String nativeName, String direction, String provider) {
        this.code = code;
        this.name = name;
        this.nativeName = nativeName;
        this.direction = direction != null ? direction : "ltr";
        this.provider = provider;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNativeName() {
        return nativeName;
    }

    public void setNativeName(String nativeName) {
        this.nativeName = nativeName;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
