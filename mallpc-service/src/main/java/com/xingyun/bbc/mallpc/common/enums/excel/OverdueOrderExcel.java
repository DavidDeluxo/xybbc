package com.xingyun.bbc.mallpc.common.enums.excel;

/**
 * @author lucky_yh
 * 2019/9/18 14:11
 */
public class OverdueOrderExcel extends ExcelMapTemplate{

    public OverdueOrderExcel() {
        relationList.add(new ExcelTemplate("", "fuid"));
        relationList.add(new ExcelTemplate("会员名称", "fnickname"));
        relationList.add(new ExcelTemplate("会员等级", "levelName"));
        relationList.add(new ExcelTemplate("会员类型", "operate"));
        relationList.add(new ExcelTemplate("可用余额", "balance"));
    }
}
