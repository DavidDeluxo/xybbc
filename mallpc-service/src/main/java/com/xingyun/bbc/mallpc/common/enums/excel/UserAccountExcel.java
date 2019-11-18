package com.xingyun.bbc.mallpc.common.enums.excel;

/**
 * @author nick
 * @ClassName: UserAccountExcel
 * @Description:
 * @date 2019年08月22日 15:40:05
 */
public class UserAccountExcel extends ExcelMapTemplate{

    public UserAccountExcel() {
        relationList.add(new ExcelTemplate("用户Id", "fuid"));
        relationList.add(new ExcelTemplate("会员名称", "fnickname"));
        relationList.add(new ExcelTemplate("会员等级", "levelName"));
        relationList.add(new ExcelTemplate("会员类型", "operate"));
        relationList.add(new ExcelTemplate("可用余额", "balance"));
    }
}
