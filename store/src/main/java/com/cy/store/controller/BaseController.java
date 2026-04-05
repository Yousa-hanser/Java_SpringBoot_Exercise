package com.cy.store.controller;

import com.cy.store.controller.ex.*;
import com.cy.store.service.ex.*;
import com.cy.store.util.JsonResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ExceptionHandler;

/* 控制层类的基类 */
public class BaseController {
    /* 操作成功的状态码 */
    public static final int OK = 200;

    // 请求处理方法，这个方法的返回值就是需要传递给前端的数据
    // 自动将异常对象传递给此方法的参数列表上
    // 当前项目中产生了异常，会被统一拦截到此方法中，这个方法此时就充当的是请求处理方法，方法的返回值直接给到前端
    @ExceptionHandler({ServiceException.class, FileUploadException.class}) // 用于统一处理抛出的异常
    public JsonResult<Void> handleException(Throwable e) {
        JsonResult<Void> result = new JsonResult<>(e);
        if (e instanceof UsernameDuplicatedException) {
            result.setState(4000);
            result.setMessage("用户名已经被占用");
        } else if (e instanceof UserNotFoundException) {
            result.setState(4001);
            result.setMessage("用户数据不存在的异常");
        } else if (e instanceof PasswordNotMatchException) {
            result.setState(4002);
            result.setMessage("用户密码错误的异常");
        } else if (e instanceof InsertException) {
            result.setState(5000);
            result.setMessage("注册时产生未知的异常");
        } else if (e instanceof UpdateException) {
            result.setState(5001);
            result.setMessage("更新密码时产生未知的异常");
        } else if (e instanceof FileEmptyException) {
            result.setState(6000);
            result.setMessage("上传的文件为空");
        } else if (e instanceof FileSizeException) {
            result.setState(6001);
            result.setMessage("上传的文件过大");
        } else if (e instanceof FileStateException) {
            result.setState(6002);
            result.setMessage("上传的文件状态异常，可能正在被使用");
        } else if (e instanceof FileTypeException) {
            result.setState(6003);
            result.setMessage("上传的文件类型错误，请上传图片文件");
        } else if (e instanceof FileUploadIOException) {
            result.setState(6004);
            result.setMessage("读写上传文件时产生未知的异常");
        }
        return result;
    }

    /**
     * 获取session对象中的uid
     * @param session session对象
     * @return 当前登录的用户uid的值
     */
    protected final Integer getUidFromSession(HttpSession session) {
        return Integer.valueOf(session.getAttribute("uid").toString());
    }

    /**
     * 获取当前登录用户的username
     * @param session session对象
     * @return 当前登录用户的用户名
     */
    protected final String getUsernameFromSession(HttpSession session) {
        return session.getAttribute("username").toString();
    }

//    /**
//     * 获取当前登录用户的avatar
//     * @param session session对象
//     * @return 当前登录用户的头像文件路径
//     */
//    protected final String getAvatarFromSession(HttpSession session) {
//        return session.getAttribute("avatar").toString();
//    }
}
