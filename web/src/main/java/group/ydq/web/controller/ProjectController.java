package group.ydq.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import group.ydq.model.dto.BaseResponse;
import group.ydq.model.entity.dm.ExpertProject;
import group.ydq.model.entity.dm.Project;
import group.ydq.model.entity.dm.ProjectFile;
import group.ydq.service.service.FileService;
import group.ydq.service.service.ProjectService;
import group.ydq.utils.DateUtil;
import group.ydq.utils.FileUtil;
import group.ydq.utils.RetResponse;
import group.ydq.utils.UpdateUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Daylight
 * @date 2018/11/29 0:41
 */
@Controller
@RequestMapping("/project")
public class ProjectController {
    @Resource
    private ProjectService projectService;

    @Resource
    private FileService fileService;

    @RequestMapping("/save")
    @ResponseBody
    public BaseResponse saveProject(@RequestBody Project project){
        if (projectService.isProjectExist(project.getId())) {
            Project targetProject=projectService.getProject(project.getId());
            UpdateUtil.copyNullProperties(targetProject,project);
            project.setMajor(targetProject.getMajor());
            project.setLevel(targetProject.getLevel());
            Date date=new Date();
            project.setUpdateTime(date);
            projectService.save(project);
            return RetResponse.success(DateUtil.dateToStr(date,DateUtil.format1));
        }
        return RetResponse.error();
    }

    @RequestMapping("/new/{type:.+}")
    @ResponseBody
    public BaseResponse saveOrSubmitProject(@PathVariable String type,@RequestBody Project project){
        if (type.equals("save")){
            project.setState(-1);
            project.setSubmit(false);
        }else{
            project.setState(0);
            project.setSubmit(true);
        }
        project.setCreateTime(new Date());
        project.setUpdateTime(new Date());
        return RetResponse.success(projectService.save(project).getId());
    }

    @RequestMapping("/delete")
    @ResponseBody
    public BaseResponse deleteProject(long id){
        if (projectService.isProjectExist(id)){
            Project project=projectService.getProject(id);
            deleteFile(project.getDeclaration());
            for (ProjectFile file:project.getCommitmentPics()) {
                deleteFile(file);
            }
            projectService.deleteProject(id);
            return RetResponse.success();
        }
        return RetResponse.error();
    }

    @RequestMapping("/getDetails/{id:.+}")
    public String getDetails(@PathVariable long id, Model model){
        Project project=projectService.getProject(id);
        ProjectFile file=project.getDeclaration();
        JSONArray array= JSON.parseArray(project.getMembers());

        StringBuilder members= new StringBuilder();
        for (int i=0;i<array.size()-1;i++){
            members.append(array.getString(i)).append("，");
        }
        members.append(array.getString(array.size()-1));

        List<String> pics=new ArrayList<>();
        for (ProjectFile pic:project.getCommitmentPics())
            pics.add("/file/"+pic.getId());

        model.addAttribute("submit",project.isSubmit());
        model.addAttribute("name",project.getName());
        model.addAttribute("leader",project.getLeader().getNick());
        model.addAttribute("major",project.getMajor());
        model.addAttribute("level",project.getLevel());
        model.addAttribute("phone",project.getPhone());
        model.addAttribute("email",project.getEmail());
        model.addAttribute("members",members);
        model.addAttribute("filename",file.getName());
        model.addAttribute("filepath","/file/"+file.getId());
        model.addAttribute("commitmentPics",pics);
        return "projectDetails";
    }

    @RequestMapping("/{role:.+}/list")
    @ResponseBody
    public BaseResponse getProjectList(@PathVariable String role, int page,int limit,String userNumber){
        Map<String, Object> obj = new HashMap<>();
        List<Map<String,Object>> list=new ArrayList<>();
        if (role.equals("expert")){
            Page<ExpertProject> projects=projectService.getProjectOfExpert(page, limit, userNumber);
            for (ExpertProject project:projects){
                Map<String,Object> map=new HashMap<>();
                map.put("id",project.getId());
                map.put("mark",project.isMark());
                map.put("score",project.getScore());
                map.put("remark",project.getRemark());
                map.put("level",project.getProject().getLevel());
                map.put("name",project.getProject().getName());
                map.put("leader",project.getProject().getLeader().getNick());
                map.put("major",project.getProject().getMajor());
                map.put("createTime", DateUtil.dateToStr(project.getProject().getCreateTime(),DateUtil.format1));
                list.add(map);
            }
            obj.put("count",projects.getTotalElements());
            obj.put("data",list);
        }else {
            Page<Project> projects;
            if (role.equals("teacher")) {
                projects = projectService.getProjectsOfLeader(page, limit, userNumber);
                for (Project project:projects){
                    Map<String,Object> map=new HashMap<>();
                    map.put("id",project.getId());
                    map.put("name",project.getName());
                    map.put("state",project.getState());
                    map.put("major",project.getMajor());
                    map.put("level",project.getLevel());
                    map.put("createTime",DateUtil.dateToStr(project.getCreateTime(),DateUtil.format1));
                    map.put("updateTime",DateUtil.dateToStr(project.getUpdateTime(),DateUtil.format1));
                    map.put("remark",project.getRemark());
                    map.put("submit",project.isSubmit());
                    list.add(map);
                }
            }else {
                projects = projectService.getProjectsOfManager(page, limit, userNumber);
                for (Project project:projects){
                    Map<String,Object> map=new HashMap<>();
                    map.put("id",project.getId());
                    map.put("name",project.getName());
                    map.put("leader",project.getLeader().getNick());
                    map.put("state",project.getState());
                    map.put("major",project.getMajor());
                    map.put("level",project.getLevel());
                    map.put("createTime",DateUtil.dateToStr(project.getCreateTime(),DateUtil.format1));
                    map.put("updateTime",DateUtil.dateToStr(project.getUpdateTime(),DateUtil.format1));
                    map.put("experts",project.getExperts());
                    map.put("remark",project.getRemark());
                    list.add(map);
                }
            }
            obj.put("count", projects.getTotalElements());
            obj.put("data", list);
        }
        return RetResponse.success(obj);
    }

    private void deleteFile(ProjectFile file){
        if (FileUtil.delete(file.getUuid(),FileUtil.getSuffix(file.getName())))
            fileService.deleteFile(file.getId());
    }
}
