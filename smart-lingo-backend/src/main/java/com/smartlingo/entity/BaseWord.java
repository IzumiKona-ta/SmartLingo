package com.smartlingo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class BaseWord {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("word")
    private String word;
    
    @TableField("translate")
    private String translate;

    @TableField("phonetic")
    private String phonetic;

    @TableField("definition_en")
    private String definitionEn;

    @TableField("example_en")
    private String exampleEn;

    @TableField("example_cn")
    private String exampleCn;

    @TableField("mnemonic")
    private String mnemonic;

    @TableField("cloze")
    private String cloze;

    @TableField("tags")
    private String tags;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
    public String getTranslate() { return translate; }
    public void setTranslate(String translate) { this.translate = translate; }
    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }
    public String getDefinitionEn() { return definitionEn; }
    public void setDefinitionEn(String definitionEn) { this.definitionEn = definitionEn; }
    public String getExampleEn() { return exampleEn; }
    public void setExampleEn(String exampleEn) { this.exampleEn = exampleEn; }
    public String getExampleCn() { return exampleCn; }
    public void setExampleCn(String exampleCn) { this.exampleCn = exampleCn; }
    public String getMnemonic() { return mnemonic; }
    public void setMnemonic(String mnemonic) { this.mnemonic = mnemonic; }
    public String getCloze() { return cloze; }
    public void setCloze(String cloze) { this.cloze = cloze; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
}
