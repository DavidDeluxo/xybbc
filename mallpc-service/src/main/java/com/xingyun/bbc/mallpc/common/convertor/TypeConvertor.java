package com.xingyun.bbc.mallpc.common.convertor;

import com.xingyun.bbc.core.user.po.UserVerify;
import com.xingyun.bbc.mallpc.common.utils.ImageVOUtils;
import com.xingyun.bbc.mallpc.model.dto.user.UserVerifyDTO;
import com.xingyun.bbc.mallpc.model.vo.user.UserVerifyVO;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ClassName UserConvertor
 * @Description
 * @Author pengaoluo
 * @Date 2019/8/18 11:10
 * @Version 1.0
 */
@Component
public class TypeConvertor {

    private static Mapper dozerBeanMapper;

    @Autowired
    public void setDozerBeanMapper(Mapper dozerBeanMapper) {
        TypeConvertor.dozerBeanMapper = dozerBeanMapper;
    }


    public static UserVerifyVO convertUserVerifyToUserVerifyVO(UserVerify user) {
        UserVerifyVO userVerifyVO = dozerBeanMapper.map(user, UserVerifyVO.class);
        userVerifyVO.setFbusinessLicensePicImage(ImageVOUtils.toImageVo(userVerifyVO.getFbusinessLicensePic()));
        userVerifyVO.setFidcardFrontImage(ImageVOUtils.toImageVo(userVerifyVO.getFidcardFront()));
        userVerifyVO.setFidcardBackImage(ImageVOUtils.toImageVo(userVerifyVO.getFidcardBack()));
        userVerifyVO.setFshopFrontImage(ImageVOUtils.toImageVo(userVerifyVO.getFshopFront()));
        userVerifyVO.setFshopInsideImage(ImageVOUtils.toImageVo(userVerifyVO.getFshopInside()));
        return userVerifyVO;
    }

    public static UserVerify convertUserVerifyDTOToUserVerify(UserVerifyDTO userVerifyDTO) {
        return dozerBeanMapper.map(userVerifyDTO, UserVerify.class);
    }



}
