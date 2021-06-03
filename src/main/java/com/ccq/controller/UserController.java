package com.ccq.controller;

import com.ccq.fastdfs.FastdfsConnectionFactory;
import com.ccq.pojo.Log;
import com.ccq.pojo.User;
import com.ccq.service.FileSystemService;
import com.ccq.service.LogService;
import com.ccq.service.UserService;
import com.ccq.service.impl.FastDfsFileSystemService;
import com.ccq.utils.CommonDate;
import com.ccq.utils.LogUtil;
import com.ccq.utils.NetUtil;
import com.ccq.utils.WordDefined;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

/**
 * @author ccq
 * @Description
 * @date 2017/11/28 22:30
 */
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private LogService logService;


    /**
     * 跳转到聊天页面
     *
     * @return
     */
    @RequestMapping("/chat")
    public ModelAndView getIndex() {
	ModelAndView mv = new ModelAndView("index");
	return mv;
    }

    /**
     * 获取个人头像路径
     *
     * @param userid
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/{userid}/head")
    @ResponseBody
    public User head(@PathVariable("userid") String userid, HttpServletRequest request, HttpServletResponse response) {
	User user = userService.getUserById(userid);
	return user;
    }

    /**
     * 跳转到个人信息页面
     *
     * @param userid
     * @return
     */
    @RequestMapping(value = "/{userid}", method = RequestMethod.GET)
    public ModelAndView toInformation(@PathVariable("userid") String userid) {
	ModelAndView view = new ModelAndView("information");
	User user = userService.getUserById(userid);
	view.addObject("user", user);
	return view;
    }

    /**
     * 显示个人信息编辑页面
     *
     * @param userid
     * @return
     */
    @RequestMapping(value = "{userid}/config")
    public ModelAndView setting(@PathVariable("userid") String userid) {
	ModelAndView view = new ModelAndView("info-setting");
	User user = userService.getUserById(userid);
	view.addObject("user", user);
	return view;
    }

    @PostMapping("/add")
    @ApiOperation(value = "批量插入数据", httpMethod = "POST", notes = "1.0", response = String.class)
    public void addUser(@RequestBody User user) {

    }

    /**
     * 更新用户个人信息
     *
     * @param userid
     * @param user
     * @param request
     * @return
     */
    @RequestMapping(value = "{userid}/update", method = RequestMethod.POST)
    public String updateUser(@PathVariable("userid") String userid, HttpSession session, User user, RedirectAttributes attributes, HttpServletRequest request) {
	int flag = userService.updateUser(user);
	if (flag > 0) {
	    user = userService.getUserById(userid);
	    session.setAttribute("user", user);
	    Log log = LogUtil.setLog(userid, CommonDate.getTime24(), WordDefined.LOG_TYPE_UPDATE, WordDefined.LOG_DETAIL_UPDATE_PROFILE, NetUtil.getIpAddress(request));
	    logService.insertLog(log);
	    attributes.addFlashAttribute("message", "[" + userid + "]资料更新成功!");
	} else {
	    attributes.addFlashAttribute("error", "[" + userid + "]资料更新失败!");
	}
	return "redirect:/{userid}/config";
    }

    /**
     * 修改密码
     *
     * @param userid
     * @param oldpass
     * @param newpass
     * @param attributes
     * @param request
     * @return
     */
    @RequestMapping(value = "{userid}/pass", method = RequestMethod.POST)
    public String updateUserPassword(@PathVariable("userid") String userid, String oldpass, String newpass, RedirectAttributes attributes, HttpServletRequest request) {
	User user = userService.getUserById(userid);
	if (oldpass.equals(user.getPassword())) {
	    user.setPassword(newpass);
	    int flag = userService.updateUser(user);
	    if (flag > 0) {
		Log log = LogUtil.setLog(userid, CommonDate.getTime24(), WordDefined.LOG_TYPE_UPDATE, WordDefined.LOG_DETAIL_UPDATE_PASSWORD, NetUtil.getIpAddress(request));
		logService.insertLog(log);
		attributes.addFlashAttribute("message", "[" + userid + "]密码更新成功!");
	    } else {
		attributes.addFlashAttribute("error", "[" + userid + "]密码更新失败!");
	    }
	} else {
	    attributes.addFlashAttribute("error", "原密码错误!");
	}
	return "redirect:/{userid}/config";
    }

    @RequestMapping(value = "{userid}/upload", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String updateUserPassword(@PathVariable("userid") String userid, HttpServletRequest request,HttpServletResponse response) {

	JSONObject responseJson = new JSONObject();
	MultipartHttpServletRequest multipartRequest=(MultipartHttpServletRequest)request;
	MultipartFile file = multipartRequest.getFile("file");
	byte[] bytes = null;
	try {
	     bytes = file.getBytes();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	FastdfsConnectionFactory fastdfsConnectionFactory = new FastdfsConnectionFactory("/admin/fastdfs_client.conf");
        FileSystemService fileSystemService = new FastDfsFileSystemService(fastdfsConnectionFactory);

	String object = fileSystemService.uploadObject(bytes, "jpg");

	object = "http://47.106.108.178:8888" + File.separator + object;

	if (!StringUtils.isEmpty(object)) {//是img的
	    User user = userService.getUserById(userid);
	    user.setProfilehead(object);
	    int flag = userService.updateUser(user);
	    if (flag > 0) {
		Log log = LogUtil.setLog(userid, CommonDate.getTime24(), WordDefined.LOG_TYPE_UPDATE, WordDefined.LOG_DETAIL_UPDATE_PROFILEHEAD, NetUtil.getIpAddress(request));
		logService.insertLog(log);
	    } else {
		responseJson.put("result", "error");
		responseJson.put("msg", "上传失败！");
	    }

	}

	responseJson.put("result", "ok");
	responseJson.put("msg", "上传成功！");
	responseJson.put("fileUrl", object);

	try {
	    response.sendRedirect("config");
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return null;
    }

}
