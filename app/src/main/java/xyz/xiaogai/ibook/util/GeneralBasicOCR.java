//package xyz.xiaogai.ibook.util;
//
//import com.tencentcloudapi.common.Credential;
//import com.tencentcloudapi.common.exception.TencentCloudSDKException;
//import com.tencentcloudapi.common.profile.ClientProfile;
//import com.tencentcloudapi.common.profile.HttpProfile;
//import com.tencentcloudapi.ocr.v20181119.OcrClient;
//import com.tencentcloudapi.ocr.v20181119.models.*;
//
//public class GeneralBasicOCR
//{
//    public String getOcrResult(String base64Image){
//        try{
//            // 实例化一个认证对象，入参需要传入腾讯云账户secretId，secretKey,此处还需注意密钥对的保密
//            // 密钥可前往https://console.cloud.tencent.com/cam/capi网站进行获取
//            Credential cred = new Credential("AKIDjHacNr40g5C2buwhBOKzNVMwFH2WjfLk", "91cwzlHWUBbleGbgUNzLRGo55oGQJvGK");
//            // 实例化一个http选项，可选的，没有特殊需求可以跳过
//            HttpProfile httpProfile = new HttpProfile();
//            httpProfile.setEndpoint("ocr.tencentcloudapi.com");
//            // 实例化一个client选项，可选的，没有特殊需求可以跳过
//            ClientProfile clientProfile = new ClientProfile();
//            clientProfile.setHttpProfile(httpProfile);
//            // 实例化要请求产品的client对象,clientProfile是可选的
//            OcrClient client = new OcrClient(cred, "ap-beijing", clientProfile);
//            // 实例化一个请求对象,每个接口都会对应一个request对象
//            GeneralBasicOCRRequest req = new GeneralBasicOCRRequest();
//            req.setImageBase64(base64Image);
//            // 返回的resp是一个GeneralBasicOCRResponse的实例，与请求对象对应
//            GeneralBasicOCRResponse resp = client.GeneralBasicOCR(req);
//            // 输出json格式的字符串回包
//            return(GeneralBasicOCRResponse.toJsonString(resp));
//        } catch (TencentCloudSDKException e) {
//            return null;
//        }
//
//    }
//}