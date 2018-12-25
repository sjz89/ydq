package group.ydq.web.controller;

import group.ydq.authority.SubjectUtils;
import group.ydq.model.entity.rbac.User;
import group.ydq.service.service.PageRenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Daylight
 * @date 2018/11/26 23:12
 */
@Controller
public class PageController {

    @Autowired
    private PageRenderService pageRenderService;

    @RequestMapping("/")
    public String index(Model model) {
        pageRenderService.render(model, (User) SubjectUtils.getSubject().getBindMap("user"));
        return "home";
    }

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/register")
    public String register() {
        return "register";
    }

}
