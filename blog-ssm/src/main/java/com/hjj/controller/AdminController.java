package com.hjj.controller;

import com.hjj.domain.*;
import com.hjj.utils.StringUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.hjj.utils.StringUtil.listToString;

@Controller
public class AdminController extends BaseController {

	/**
	 * 跳转dashboard.html后台首页
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin")
	public String toDashboard(Model model) {
		model.addAttribute("totalContent",contentService.selectNumberOfArticles());
		model.addAttribute("totalCategory",categoryService.selectCountOfCategory());
		model.addAttribute("totalTag",tagService.selectCountOfTag());
		model.addAttribute("totalComment",99);
		return "dashboard";
	}

	/**
	 * 跳转edit.html新增博客页面
	 * @param model
	 * @return
	 */
	@RequestMapping("/adminEdit")
	public String adminEdit(Model model) {
		model.addAttribute("serverName",request.getServerName());

		List<Category> categories = categoryService.selectAllCategory();
		model.addAttribute("categories",categories);

		List<Tag> tags = tagService.selectAllTag();
		List<String> tags1 = new ArrayList<>();
		for (Tag tag:tags) {
			tags1.add(tag.getName());
		}
		model.addAttribute("tags",listToString(tags1));

		return "edit";
	}

	/**
	 * 提交博客编写表单
	 * @param model
	 * @return
	 */
	@RequestMapping("/adminEditBlog")
	public String adminEditBlog(Model model) {
		String blogTitle = request.getParameter("blogTitle");
		String blogSlug = request.getParameter("blogSlug");
		String blogCategory = request.getParameter("blogCategory");

		List<String> blogTag = new ArrayList<>();
		if (request.getParameterValues("blogTag") != null) {
			blogTag = Arrays.asList(request.getParameterValues("blogTag"));
		}

		String blogDescription = request.getParameter("blogDescription");
		String blogType = request.getParameter("blogType");
		String blogContentsOrder = "0";
		if ("on".equals(request.getParameter("blogContentOrder"))) {
			blogContentsOrder = "1";
		}
		String blogContentStatus = request.getParameter("blogContentStatus");
		String blogPassword = request.getParameter("blogPassword");
		String blogThumb = request.getParameter("blogThumb");
		String blogContent = request.getParameter("content");

		Content content = new Content();
		content.setCgid(categoryService.selectCategoryBySlug(blogCategory));
		content.setTitle(blogTitle);
		content.setSlug(blogSlug);

		try {
			content.setCreatedTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.parse(request.getParameter("blogCreatedTime")));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		content.setModifiedTime(new Date());
		content.setContentText(blogContent);
		content.setContentOrder(Integer.parseInt(blogContentsOrder));
		User user = (User) session.getAttribute("USER_SESSION");
		content.setAuthorId(user.getUid());
		content.setContentType(blogType);
		content.setContentStatus(blogContentStatus);
		content.setContentStatus(blogContentStatus);
		content.setPassword(blogPassword);
		content.setViews(0);
		if ("".equals(blogThumb) || blogThumb == null) {
			content.setThumb(StringUtil.randomContentThumb());
		} else {
			content.setThumb(blogThumb);
		}
		content.setDescription(blogDescription);

		List<String> tagListNew = new ArrayList<>();
		for (String s:blogTag) {
			int i = tagService.findTagIdByName(s);
			if (i != -1) {
				//根据tagid增加1次count
				//tagService.updateTagCount(i);
				tagListNew.add(String.valueOf(i));
			} else {
				tagService.insert(new Tag(null,s,1));
				tagListNew.add(String.valueOf(tagService.findTagIdByName(s)));
			}
			content.setTagList(listToString(tagListNew));
		}

		int result =  contentService.insert(content);
		if (result == 1) {

			//插入文章后全局修正tag数量
			List<Content> contents = contentService.selectAllContent();
			StringBuilder a = new StringBuilder();
			for (Content c : contents) {
				a.append(",").append(c.getTagList());
			}
			String[] b = a.toString().split(",");
			List<Tag> tags = tagService.selectAllTag();
			for (Tag tag : tags) {
				int tagCount = 0;
				for (String s : b) {
					if (s.equals(String.valueOf(tag.getTid()))) {
						tagCount++;
					}
				}
				tagService.updateTagCount(tag.getTid(),tagCount);
			}

			model.addAttribute("message", "1");
		} else {
			model.addAttribute("message", "0");
		}
		return "redirect:/adminBlog";

	}

	/**
	 * 获取便签列表
	 * @return
	 */
	@RequestMapping("/adminGetTagList")
	@ResponseBody
	public Map<String,Object> adminGetTagList() {
		Map<String, Object> map = new HashMap<>();
		List<Tag> tags = tagService.selectAllTag();
		map.put("tags",tags);
		return map;
	}

	/**
	 * 模糊查询标签列表
	 * @param tagName
	 * @return
	 */
	@RequestMapping("/fuzzyQueryTag")
	@ResponseBody
	public Map<String,Object> fuzzyQueryTag(@RequestParam(value="tagName")String tagName){
		Map<String, Object> map = new HashMap<>();
		List<Tag> tags = tagService.fuzzyQueryTag(tagName);
		map.put("tags",tags);
		return map;
	}

	/**
	 * 通过文章缩略名获取文章实体
	 * @param slugName
	 * @return
	 */
	@RequestMapping("/findContentBySlugName")
	@ResponseBody
	public Map<String,Object> findContentBySlugName(@RequestParam(value="slugName")String slugName){
		Map<String, Object> map = new HashMap<>();
		Content c = contentService.findContentBySlugName(slugName);
		boolean slugExist = false;
		if (c != null) {
			slugExist = true;
		}
		map.put("slugExist", slugExist);
		return map;
	}

	/**
	 * 跳转blog-mgr.html页面
	 * @param model
	 * @return
	 */
	@RequestMapping("/adminBlog")
	public String adminBlog(Model model) {
		return "blog-mgr";
	}

	/**
	 * 跳转user-mgr.html页面
	 * @param model
	 * @return
	 */
	@RequestMapping("/adminUser")
	public String adminUser(Model model) {
		return "user-mgr";
	}

	/**
	 * 跳转category-mgr.html页面
	 * @param model
	 * @return
	 */
	@RequestMapping("/adminCategory")
	public String adminCategory(Model model) {
		List<Category> categories = categoryService.selectAllCategory();
		for (Category c : categories) {
			c.setCount(contentService.selectContentCountByCgid(c.getCgid()));
		}
		model.addAttribute("categories",categories);
		return "category-mgr";
	}

	/**
	 * 跳转tag-mgr.html页面
	 * @param model
	 * @return
	 */
	@RequestMapping("/adminTag")
	public String adminTag(Model model) {
		List<Tag> tags = tagService.selectAllTag();
		model.addAttribute("tags",tags);
		return "tag-mgr";
	}

	/**
	 * 跳转comment-mgr.html页面
	 * @param model
	 * @return
	 */
	@RequestMapping("/adminComment")
	public String adminComment(Model model) {
		List<Comment> comments = commentService.selectAllComment();
		model.addAttribute("comments",comments);
		return "comment-mgr";
	}

	/**
	 * 跳转configuration.html页面
	 * @param model
	 * @return
	 */
	@RequestMapping("/adminConfig")
	public String adminConfig(Model model) {
		return "configuration";
	}

}
