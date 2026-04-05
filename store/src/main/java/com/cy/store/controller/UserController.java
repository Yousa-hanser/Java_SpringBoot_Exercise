package com.cy.store.controller;

import com.cy.store.controller.ex.FileEmptyException;
import com.cy.store.controller.ex.FileSizeException;
import com.cy.store.controller.ex.FileTypeException;
import com.cy.store.controller.ex.FileUploadIOException;
import com.cy.store.entity.User;
import com.cy.store.service.IUserService;
import com.cy.store.service.ex.InsertException;
import com.cy.store.service.ex.UsernameDuplicatedException;
import com.cy.store.util.JsonResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// @Controller
@RestController // @Controller + @ResponseBody
@RequestMapping("users")
public class UserController extends BaseController {
    @Autowired
    private IUserService userService;

    /*
    * 1. 接收数据方式：请求处理方法的参数列表设置为pojo类型来接收前端的数据，
    *    SpringBoot会将前端的url地址中的参数名和pojo类的属性名进行比较，
    *    如果这两个名称相同，则将值注入到pojo类中对应的属性上
    */
    @RequestMapping("reg")
    // @ResponseBody 表示此方法的响应结果以json格式进行数据的响应给到前端
    public JsonResult<Void> reg(User user){
        userService.reg(user);
        return new JsonResult<>(OK);
    }

    /*
    @RequestMapping("reg")
    // @ResponseBody 表示此方法的响应结果以json格式进行数据的响应给到前端
    public JsonResult<Void> reg(User user){
        // 创建响应结果对象
        JsonResult<Void> result = new JsonResult<>();
        try {
            userService.reg(user);
            result.setState(200);
            result.setMessage("用户注册成功");
        } catch (UsernameDuplicatedException e) {
            result.setState(4000);
            result.setMessage("用户名被占用");
        } catch (InsertException e) {
            result.setState(5000);
            result.setMessage("注册时产生未知的异常");
        }

        return result;
    }
    */

    /*
    * 2. 接收数据方式：请求处理方法的参数列表设置为非pojo类型，
    * SpringBoot会将请求的参数名和方法的参数名直接进行比较，
    * 如果名称相同则自动完成值的依赖注入
    */
    @RequestMapping("login")
    public JsonResult<User> login(String username, String password , HttpSession session){
        User data = userService.login(username, password);
        // 向session对象中完成数据的绑定(session对象是全局的)
        session.setAttribute("uid", data.getUid());
        session.setAttribute("username", data.getUsername());

        return new JsonResult<>(OK, data);
    }

    @RequestMapping("change_password")
    public JsonResult<Void> changePassword(String oldPassword, String newPassword, HttpSession session) {
        userService.changePassword(
                getUidFromSession(session),
                getUsernameFromSession(session),
                oldPassword, newPassword);
        return new JsonResult<>(OK);
    }

    @RequestMapping("get_by_uid")
    public JsonResult<User> getByUid(HttpSession session) {
        User user = userService.getByUid(getUidFromSession(session));
        return new JsonResult<>(OK, user);
    }

    @RequestMapping("change_info")
    public JsonResult<Void> changeInfo(User user, HttpSession session) {
        Integer uid = getUidFromSession(session);
        String username = getUsernameFromSession(session);
        userService.changeInfo(uid, username, user);
        return new JsonResult<>(OK);
    }

    // 上传文件的大小最大为10MB
    private static final int AVATAR_MAX_SIZE = 10 * 1024 * 1024;
    // 限制上传文件的类型
    private static final List<String> AVATAR_TYPE = new ArrayList<>();
    static {
        AVATAR_TYPE.add("image/jpeg");
        AVATAR_TYPE.add("image/png");
        AVATAR_TYPE.add("image/bmp");
        AVATAR_TYPE.add("image/gif");
    }
    /**
     * MultipartFile是SpringMVC提供的一个接口，它可以获取任何类型文件的数据
     * SpringBoot整合了SpringMVC，只需要在处理请求的方法参数列表上声明一个类型为MultipartFile的参数，
     * SpringBoot就会自动将从前端接收到的文件数据传递给这个参数
     *
     * @RequesParam 如果请求中的参数与请求处理方法的参数名称不一致，可以使用该注解进行标记和映射
     *
     * @param session session对象
     * @param file 文件数据
     * @return 文件路径
     */
    @RequestMapping("change_avatar")
    public JsonResult<String> changeAvatar(HttpSession session,
                                           @RequestParam("file") MultipartFile file) {
        // 判断上传的文件是否为空
        if (file.isEmpty()) {
            throw new FileEmptyException("文件为空");
        }
        // 判断文件大小是否合法
        if (file.getSize() > AVATAR_MAX_SIZE) {
            throw new FileSizeException("文件大小超过限制");
        }
        // 获取上传的文件类型
        String contentType = file.getContentType();
        // 判断文件类型是否合法
        if (!AVATAR_TYPE.contains(contentType)) {
            throw new FileTypeException("请上传jpg/png/bmp/gif格式的文件");
        }

        // 上传的文件存放在 项目路径/uploads/文件名.jpg
        String parent = System.getProperty("user.dir") + "/uploads/";

        // 通过File对象检查路径是否存在
        File dir = new File(parent);
        if (!dir.exists()) {
            // 路径不存在，创建路径
            dir.mkdirs();
        }
        // 先获取到上传的文件名，然后利用UUID工具类来生成一个新的字符串作为文件名，避免文件名冲突覆盖
        String originalFilename = file.getOriginalFilename();
        // avatar01.png
        int index = originalFilename.lastIndexOf('.');
        String suffix = originalFilename.substring(index);
        String filename = UUID.randomUUID().toString().toUpperCase() + suffix;

        File dest = new File(dir, filename);
        // 将参数file中的数据写入到目标文件中
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new FileUploadIOException("写入文件数据时产生未知的异常");
        }

        Integer uid = getUidFromSession(session);
        String username = getUsernameFromSession(session);
        // 数据库中存储文件的相对路径即可
        String avatar = "/uploads/" + filename;
        userService.changeAvatar(uid, avatar, username);

        // 返回用户头像的路径给前端页面，用于展示头像
        return new JsonResult<>(OK, avatar);
    }
}
