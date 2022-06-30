package com.jump.utils.report.base;

import org.apache.poi.xwpf.usermodel.Document;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 将图片文件后缀转换成poi Document枚举
 *
 * @author Jump
 */
public enum DocumentPicEnum {

    /**
     * Windows Meta File "emf", "wmf", "pict", "jpg", "jpeg", "png", "dib", "gif", "tiff", "eps", "bmp", "wpg"
     */
    PICTURE_TYPE_EMF("emf", Document.PICTURE_TYPE_EMF),
    PICTURE_TYPE_WMF("wmf", Document.PICTURE_TYPE_WMF),
    PICTURE_TYPE_PICT("pict", Document.PICTURE_TYPE_PICT),
    PICTURE_TYPE_JPG("jpg", Document.PICTURE_TYPE_JPEG),
    PICTURE_TYPE_JPEG("jpeg", Document.PICTURE_TYPE_JPEG),
    PICTURE_TYPE_PNG("png", Document.PICTURE_TYPE_PNG),
    PICTURE_TYPE_DIB("dib", Document.PICTURE_TYPE_DIB),
    PICTURE_TYPE_GIF("gif", Document.PICTURE_TYPE_GIF),
    PICTURE_TYPE_TIFF("tiff", Document.PICTURE_TYPE_TIFF),
    PICTURE_TYPE_EPS("eps", Document.PICTURE_TYPE_EPS),
    PICTURE_TYPE_BMP("bmp", Document.PICTURE_TYPE_BMP),
    PICTURE_TYPE_WPG("wpg", Document.PICTURE_TYPE_WPG);

    private static final Map<String, DocumentPicEnum> MAP = Arrays.stream(DocumentPicEnum.values()).collect(Collectors.toMap(DocumentPicEnum::getFileSuffix, Function.identity()));
    private String fileSuffix;
    private int pictureType;

    DocumentPicEnum(String fileSuffix, int pictureType) {
        this.fileSuffix = fileSuffix;
        this.pictureType = pictureType;
    }

    public static DocumentPicEnum find(String fileSuffix) {
        return MAP.get(fileSuffix);
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public int getPictureType() {
        return pictureType;
    }

    public void setPictureType(int pictureType) {
        this.pictureType = pictureType;
    }
}
