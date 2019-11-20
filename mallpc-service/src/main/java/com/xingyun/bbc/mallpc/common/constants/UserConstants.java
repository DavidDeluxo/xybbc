package com.xingyun.bbc.mallpc.common.constants;

/**
 * @author ZSY
 * @Title:
 * @Description:
 * @date 2019-09-03 11:00
 */
public interface UserConstants {
    class Email {
        /**
         * 行云服务平台URL
         */
        public static final String XY_HEALPER_SERVER_ADDRESS = "http://localhost:8084/";
        /**
         * 行云服务平台的短信URL
         */
        public static final String XY_SMS_HELPER_URL = XY_HEALPER_SERVER_ADDRESS + "sms/sendSmsByPost";

        public static final String XY_EMAIL_HELPER_URL = XY_HEALPER_SERVER_ADDRESS + "mail/commonSendEmail";
        /**
         * 发送邮件携带附件
         */
        public static final String XY_EMAIL_HELPER_ADD_ACCESSORY_URL = XY_HEALPER_SERVER_ADDRESS + "mail/batchSendEmailAddAccessory";
        /**
         * 邮箱验证码
         */
        public static final String EMAIL_VERIFICATION_CODE = "email_verification_code";

        public static final long EMAIL_AUTH_CODE_REDIS_TIME = 60 * 5L;

        public static final long EMAIL_AUTH_CODE_TOKEN_TIME = 1000 * 60 * 5L;

    }

    class Token {
        /**
         * token有效时间：24小时
         */
        public static final long TOKEN_EXPIRATION = 60 * 60 * 1000 * 24L;
        /**
         * PC登录的token有效时间
         */
        public static final long TOKEN_AUTO_LOGIN_EXPIRATION = 60 * 60 * 1000 * 24 * 30L;
    }

    class Sms {
        /**
         * 短信验证码 有效时间 5分钟
         */
        public static final long MOBILE_AUTH_CODE_EXPIRE_TIME = 1000 * 60 * 5L;
        /**
         * 同一ip发送验证码触发图形验证阈值 5次
         */
        public static final int CAPTCHA_THRESHOLD = 5;
        /**
         * 同一ip地址每天发送验证码上限
         */
        public static final int MAX_IP_SMS_SEND_TIME = 10;
        /**
         * 手机发送短信间隔时间 60秒
         */
        public static final long MOBILE_SEND_SMS_TIME = 1000 * 60;
    }

    class Cookie {
        /**
         * pc 登录cookie有效期 30天
         */
        public static final int COOKIE_EXPIRE_TIME = 60 * 60 * 24 * 30;
    }
}
