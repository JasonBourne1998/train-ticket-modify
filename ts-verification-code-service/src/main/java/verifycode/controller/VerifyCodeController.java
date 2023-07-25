package verifycode.controller;


import org.springframework.beans.factory.annotation.Autowired;

















import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import verifycode.service.VerifyCodeService;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author fdse
 */
@RestController
@RequestMapping("/api/v1/verifycode")
public class VerifyCodeController { 
    private static final Logger logger = LoggerFactory.getLogger(VerifyCodeController.class);



















    @Autowired
    private VerifyCodeService verifyCodeService;

    @GetMapping("/generate")
    public void imageCode(@RequestHeader HttpHeaders headers,
                          HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
        logger.info("[function name:{}, API:Get /api/v1/verifycode/generate][headers:{}, request:{}, response:{}]","imageCode",(headers != null ? headers.toString(): null), (request != null ? request.toString(): null), (response != null ? response.toString(): null));
        OutputStream os = response.getOutputStream();
        Map<String, Object> map = verifyCodeService.getImageCode(60, 20, os, request, response, headers);
        String simpleCaptcha = "simpleCaptcha";
        request.getSession().setAttribute(simpleCaptcha, map.get("strEnsure").toString().toLowerCase());
        request.getSession().setAttribute("codeTime", System.currentTimeMillis());
        try {
            ImageIO.write((BufferedImage) map.get("image"), "JPEG", os);
        } catch (IOException e) {
            //error
            String error = "Can't generate verification code";
            os.write(error.getBytes());
        }
    }

    @GetMapping(value = "/verify/{verifyCode}")
    public boolean verifyCode(@PathVariable String verifyCode, HttpServletRequest request,
                              HttpServletResponse response, @RequestHeader HttpHeaders headers) {
        logger.info("[function name:{}, API:Get /api/v1/verifycode/verify/{verifyCode}][verifyCode:{}, request:{}, response:{}, headers:{}]","verifyCode",verifyCode, (request != null ? request.toString(): null), (response != null ? response.toString(): null), (headers != null ? headers.toString(): null));

        boolean result = verifyCodeService.verifyCode(request, response, verifyCode, headers);
        return true;
    }
}
