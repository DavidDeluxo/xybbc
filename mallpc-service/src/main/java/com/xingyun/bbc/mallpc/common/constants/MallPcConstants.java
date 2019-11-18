package com.xingyun.bbc.mallpc.common.constants;

import java.util.regex.Pattern;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-17
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public interface MallPcConstants {

    String FULL_STANDARD_PATTERN = "yyyy-MM-dd HH:mm:ss";

    String FULL_STANDARD_PATTERN_HOUR = "yyyy-MM-dd HH";

    String FULL_STANDARD_PATTERN_1 = "yyyy.MM.dd HH:mm:ss";

    int EASYPOI_EXPORT_EXCEL_APPEND_OFFSET = 1000;

    int MAX_EASYPOI_EXPORT_EXCEL_APPEND_OFFSET = 10000;


    /**
     * 初始密码
     */
    String INIT_PASSWORD = "888888";

    /**
     *
     */
    String DESENSITIZATION_MARK = "*";

    /**
     * 手机号正则
     */
    String MOBILE_REGEXP = "^1\\d{10}$";
    Pattern MOBLIE_PATTERN = Pattern.compile(MOBILE_REGEXP);

    /**
     * 邮箱正则
     */
    String EMAIL_REGEXP = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEXP);

    /**
     * 身份证号正则
     */
    String IDCARD_REGEXP = "^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$|^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{2}$";
    Pattern IDCARD_PATTERN = Pattern.compile(IDCARD_REGEXP);

    //会员常量
    //用户认证状态：1未认证，2 认证中，3 已认证，4未通过，5冻结  现在用户状态只有前4种,冻结状态5为与前端约定代表冻结
    int USER_STATUS_UNAUTHORIZED = 1;
    int USER_STATUS_INAUTHORIZATION = 2;
    int USER_STATUS_AUTHENTICATED = 3;
    int USER_STATUS_AUTHORIZATION_FAILED = 4;
    int USER_STATUS_FROZEN = 5;

    //中国在城市区域表中的id
    int CHINA_FREGIONID = 1;

    /**
     * 供应商工单类型 1供应商售后工单 2供应商调整工单 3供应商补偿工单
     */
    int FWORK_TYPE_SUPPLIER_AFTERSALE = 1;
    int FWORK_TYPE_SUPPLIER_ADJUST = 2;
    int FWORK_TYPE_SUPPLIER_COMPENSATION = 3;


    //运费模板是否使用 2 未使用
    int IS_USED = 1;
    int IS_NOT_USED = 2;

    //运费模板类型：1全国包邮 2部分地区包邮 3不包邮")
    int ALL_COUNTRY_POST = 1;
    int PART_COUNTRY_POST = 2;
    int NOT_COUNTRY_POST = 3;

    //模板类型  1 规则模板  2运费模板 */
    int TEMPLATE_TYPE_RULE = 1;
    int TEMPLATE_TYPE_REGION = 2;

    //  1 按照实际重量 2 按照续重单元
    int FREIGHT_AFTER_FREIGHT_RULE_OF_ACTUAL = 1;
    int FREIGHT_AFTER_FREIGHT_RULE_OF_UNIT = 2;

    //运费模板计件方式
    int VALUATION_TYPE_OF_PIECE_ = 1;
    int VALUATION_TYPE_OF_WEIGHT = 2;

    //运费模板城市关联表省市默认值
    Long FREIGHT_RELATION_CITY_DEFAULT = 0L;

    //运费模板是否被删除 0是没被删除 1是删除
    int TEMPLATE_IS_DELETE = 0;
    int TEMPLATE_HAS_DELETE = 1;

    //调整工单搜索时 searchtype 1 工单号 2 订单 3 采购单
    int WORK_ORDER_ID = 1;
    int ORDER_ID = 2;
    int SUPPLIER_ID = 3;


    //BD区域是否删除
    int MARKET_REGION_IS_DELETE = 0;
    int MARKET_REGION_HAS_DELETE = 1;

    //BD区域父亲区域ID默认值0 代表1级区域
    long MARKET_REGION_FPID = 0L;
    //BD区域等级
    int MARKET_REGION_ONE_LEVEL = 1;
    int MARKET_REGION_LOWER_LEVEL = 2;

    /**
     * 账号状态：1正常，2冻结，3禁用
     */
    int MARKET_USER_NORMAL = 1;
    int MARKET_USER_FORZEN = 2;
    int MARKET_USER_PROHIBIT = 3;
    /**
     * 市场BD推广码前缀
     */
    String MARKET_EXTENSIONCODE = "BD";

    /**
     * 城市区域状态
     */
    int CITY_REGION_INVALID = 0;
    int CITY_REGION_EFFECTIVE = 1;
    /**
     * 城市区域是否父级
     */
    int CITY_REGION_PARENT = 1;
    int CITY_REGION_NOT_PARENT = 0;
    /**
     * 城市区域中国
     */
    int CITY_REGION_CHINA = 1;
    String CITY_REGION_CHINA_NAME = "中国";
    /**
     * 城市区域类型
     */
    int CITY_REGION_COUNTRY = 1;
    int CITY_REGION_PROVINCE = 2;
    int CITY_REGION_CITY = 3;
    int CITY_REGION_REGION = 4;

    //经营类型：1实体门店，2网络店铺，3网络平台，4批采企业，5微商代购
    int FOPERATE_TYPE_PHYSICAL_STORE = 1;
    int FOPERATE_TYPE_NETWORK_STORE = 2;
    int FOPERATE_TYPE_NETWORK_PLATFORM = 3;
    int FOPERATE_TYPE_BATCH_MINING_ENTERPRISES = 4;
    int FOPERATE_TYPE_MICRO_SHOPPING_AGENT = 5;

    //sku批次详情运费明细
    String EXPRESS_DELIVERY = "快递";
    //sku批次详情包装单位
    String BATCH_PACKAGE_UNIT = "件";

    //供应商工单类别 1供应商售后工单 2供应商调整工单
    int SUPPLIER_WORK_ORDER_AFTER = 1;
    int SUPPLIER_WORK_ORDER_ADJUST = 2;

    //客户工单类别 1用户售后工单 2客户充值工单
    int USER_WORK_ORDER_RECHAGE = 1;
    int USER_WORK_ORDER_AFTER = 2;

    String PACKET_UNIT = "件";

    //库存中台搜索条件 1 SKU名称 2 SKU编码 3 批次号 4 发货单号
    int SKU_NAME = 1;
    int SKU_CODE = 2;
    int SKU_BATCH_ID = 3;
    int TRANSPORT_ORDER_ID = 4;

}
