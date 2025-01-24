package com.vip.file.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.vip.file.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author alex
 * @since 2020-06-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("business_files")
@ApiModel(value="Files对象", description="")
public class Files extends BaseEntity {

    private static final long serialVersionUID=1L;

    /** 原文件名 */
    @ApiModelProperty(name = "原文件名",notes = "")
    @TableField(value = "origin_file_name", fill = FieldFill.INSERT)
    private String originFileName ;
    /** 文件目录;文件上传所在目录名称 */
    @ApiModelProperty(name = "文件目录",notes = "文件上传所在目录名称")
    @TableField(value = "file_path", fill = FieldFill.INSERT)
    private String filePath ;
    /** 文件名称 */
    @ApiModelProperty(name = "文件名称",notes = "")
    @TableField(value = "file_name", fill = FieldFill.INSERT)
    private String fileName ;
    /** 文件类型 */
    @ApiModelProperty(name = "文件类型",notes = "")
    @TableField(value = "file_type", fill = FieldFill.INSERT)
    private String fileType ;
    /** 文件大小 */
    @ApiModelProperty(name = "文件大小",notes = "")
    @TableField(value = "file_size", fill = FieldFill.INSERT)
    private String fileSize ;
    /** 文件地址 */
    @ApiModelProperty(name = "文件地址",notes = "")
    @TableField(value = "file_url", fill = FieldFill.INSERT)
    private String fileUrl ;

}
