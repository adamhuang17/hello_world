package com.hjj.controller;

import com.hjj.domain.Comment;
import com.hjj.domain.Content;
import com.hjj.domain.User;
import com.hjj.utils.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hjj.utils.StringUtil.*;

@Controller
public class CommentController extends BaseController {

	@RequestMapping("/comment")
	public String comment() {
		String commentText = request.getParameter("commentText");
		commentText = SmileUtil.StringConvertSmile(commentText);
		String author = request.getParameter("author");
		String mail = request.getParameter("mail");
		String url = request.getParameter("url");
		boolean isHaveAuthorAndMail = author == null || "".equals(author) || mail == null || "".equals(mail);

		//判断机器人评论则直接返回到首页，后期可以添加屏蔽敏感词汇、网址关键字、邮箱关键字、ip等。
		boolean isRobot = !isContainChinese(commentText) && !isContainChinese(author);
		int contentId = 0;
		if (request.getParameter("contentId") == null || "".equals(request.getParameter("contentId"))) {
		} else {
			contentId = Integer.parseInt(request.getParameter("contentId"));
		}

		Comment comment = new Comment();
		comment.setCid(contentId);
		comment.setCreated(new Date());

		User sessionUser = (User) session.getAttribute("USER_SESSION");
		if (sessionUser == null) {
			if (isHaveAuthorAndMail || isRobot) {
				return "redirect:/index";
			} else {
				comment.setAuthorid(0);
				comment.setMail(mail);
				comment.setUrl(url);
				comment.setAuthor(author);
			}
		} else {
			comment.setAuthorid(sessionUser.getUid());
			comment.setMail(sessionUser.getMail());
			comment.setUrl(sessionUser.getUrl());
			comment.setAuthor(sessionUser.getScreenname());
		}

		comment.setIp(getIpAddr(request));
		comment.setAgent(request.getHeader("User-Agent"));
		comment.setText(commentText);

		comment.setParent(0);
		int a = commentService.insert(comment);
		CookieUtil.setUserLoginCookie(author, mail, url, request, response);
		if (contentId == 0) {
			return "redirect:/guestbook";
		} else {
			return "redirect:/articles/"+contentService.selectByPrimaryKey(contentId).getSlug();
		}
	}

	@RequestMapping("/comment/{cid}/{coid}")
	public String commentReply(@PathVariable int cid,@PathVariable int coid) {
		String commentText = request.getParameter("commentText");
		commentText = SmileUtil.StringConvertSmile(commentText);
		String author = request.getParameter("author");
		String mail = request.getParameter("mail");
		String url = request.getParameter("url");
		boolean isHaveAuthorAndMail = author == null || "".equals(author) || mail == null || "".equals(mail);
		boolean isRobot = !isContainChinese(commentText) && !isContainChinese(author);
		Comment comment = new Comment();
		comment.setCid(cid);
		comment.setParent(coid);
		comment.setCreated(new Date());
		User sessionUser = (User) session.getAttribute("USER_SESSION");
		if (sessionUser == null) {
			if (isHaveAuthorAndMail || isRobot) {
				return "redirect:/index";
			} else {
				comment.setAuthorid(0);
				comment.setMail(mail);
				comment.setUrl(url);
				comment.setAuthor(author);
			}
			comment.setAuthorid(0);
			comment.setMail(mail);
			comment.setUrl(url);
			comment.setAuthor(author);

		} else {
			comment.setAuthorid(sessionUser.getUid());
			comment.setMail(sessionUser.getMail());
			comment.setUrl(sessionUser.getUrl());
			comment.setAuthor(sessionUser.getScreenname());
		}
		comment.setIp(getIpAddr(request));
		comment.setAgent(request.getHeader("User-Agent"));
		comment.setText(commentText);

		int a = commentService.insert(comment);
		CookieUtil.setUserLoginCookie(author, mail, url, request, response);

		if (cid != 0) {
			return "redirect:/articles/"+contentService.selectByPrimaryKey(cid).getSlug();
		} else {
			return "redirect:/guestbook";
		}
	}

	/**
	 * 获取博客列表
	 *
	 * @return
	 */
	@RequestMapping("/adminGetCommentList")
	@ResponseBody
	public Map<String, Object> adminGetCommentList() {
		Map<String, Object> map = new HashMap<>();
		List<Comment> comments = commentService.selectAllComment();
		for (Comment comment : comments) {
			comment.setContentSlug(contentService.selectSlugByCid(comment.getCid()));
			comment.setCreatedDisplay(DateTimeUtil.dateWord(comment.getCreated()));
		}
		map.put("data", comments);
		return map;
	}

	/**
	 * 获取博客列表
	 *
	 * @return
	 */
	@RequestMapping("/userGetCommentList")
	@ResponseBody
	public Map<String, Object> userGetCommentList(@RequestParam(value="userId") int userId) {
		Map<String, Object> map = new HashMap<>();
		List<Comment> comments = commentService.selectCommentListWithUserId(userId);
		for (Comment comment : comments) {
			comment.setContentSlug(contentService.selectSlugByCid(comment.getCid()));
			comment.setCreatedDisplay(DateTimeUtil.dateWord(comment.getCreated()));
		}
		map.put("data", comments);
		return map;
	}

	@RequestMapping("/guestbook")
	public String showGuestbook(Model model) {

		//插入日志
		logService.insert(LogUtil.insertLog(request,"/guestbook"));

		List<Content> contents = contentService.selectAllContent();
		for (Content c : contents) {
			c.setCategorySlug(categoryService.selectByPrimaryKey(c.getCgid()).getCgSlug());
		}
		model.addAttribute("contents",contents);

		List<Content> recommendContents = contentService.selectRecommendContent();
		model.addAttribute("recommendContents",recommendContents);

		List<Comment> comments = commentService.selectCommentListByContentId(0);
		for (Comment comment : comments) {
			comment.setMail(GravatarUtil.getGravatarUrlByEmail(comment.getMail()));
			comment.setCreatedDisplay(DateTimeUtil.dateWord(comment.getCreated()));
			comment.setParentNickName(commentService.selectCommentAuthorById(comment.getParent()));
		}
		model.addAttribute("comments",comments);

		return "guestbook";
	}

	@RequestMapping("/deleteComment/{coid}")
	public String deleteContent(@PathVariable int coid) {
		int a = commentService.deleteByPrimaryKey(coid);
		return "redirect:/adminComment";
	}

	@RequestMapping("/replyComment")
	@ResponseBody
	public Map<String, Object> replyComment(int coid, int cid, String text, Model model) {
		Map<String, Object> map = new HashMap<>();
		Comment comment = new Comment();
		comment.setCreated(new Date());
		comment.setParent(coid);
		comment.setCid(cid);
		comment.setAuthorid(((User)session.getAttribute("USER_SESSION")).getUid());
		comment.setAuthor(((User)session.getAttribute("USER_SESSION")).getScreenname());
		comment.setMail(((User)session.getAttribute("USER_SESSION")).getMail());
		comment.setUrl(((User)session.getAttribute("USER_SESSION")).getUrl());
		comment.setIp(StringUtil.getIpAddr(request));
		comment.setAgent(request.getHeader("User-Agent"));
		comment.setText(text);
		int a = commentService.insert(comment);
		if (a == 1) {
			map.put("data", "success");
		} else {
			map.put("data","fail");
		}
		return map;
	}

	@RequestMapping("/deleteSelectComment")
	public String deleteSelectComment() {
		String[] coids = request.getParameterValues("coid");
		int a = commentService.deleteSelectComment(coids);
		return "redirect:/adminComment";
	}

}
