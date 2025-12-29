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
}
