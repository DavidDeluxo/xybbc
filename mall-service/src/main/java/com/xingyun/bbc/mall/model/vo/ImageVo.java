package com.xingyun.bbc.mall.model.vo;

import com.xingyun.bbc.mall.common.utils.FileUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-27
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
@Accessors(chain = true)
public class ImageVo implements Serializable {

    private static final long serialVersionUID = -9146839868480610514L;

    /**
     * 文件绝对路径
     */
    @ApiModelProperty(value = "文件绝对路径")
    public String url;

    /**
     * 文件相对路径
     */
    @ApiModelProperty(value = "文件相对路径")
    public String path;

    public ImageVo() {
    }

    public ImageVo(String path) {
        this.url = FileUtils.getFileUrl(path);
        this.path = path;
    }

}
