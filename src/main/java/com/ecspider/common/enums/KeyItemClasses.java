package com.ecspider.common.enums;

/**
 * @author lyifee
 * on 2020/12/23
 */
public enum KeyItemClasses {
    PHONE("手机"),
    ;
    private String classWord;

    KeyItemClasses(String classWord) {
        this.classWord = classWord;
    }

    public String getClassWord() {
        return classWord;
    }

    public void setClassWord(String classWord) {
        this.classWord = classWord;
    }
}
