package com.ruoyi.web.controller.mall;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mall.domain.MallKnowledgeDoc;
import com.ruoyi.mall.service.IMallKnowledgeService;

@RestController
@RequestMapping("/mall/knowledge")
public class MallKnowledgeController extends BaseController
{
    @Autowired
    private IMallKnowledgeService knowledgeService;

    @PreAuthorize("@ss.hasPermi('mall:knowledge:list')")
    @GetMapping("/list")
    public TableDataInfo list(MallKnowledgeDoc query)
    {
        startPage();
        List<MallKnowledgeDoc> list = knowledgeService.selectDocList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:knowledge:query')")
    @GetMapping("/{docId}")
    public AjaxResult getInfo(@PathVariable Long docId)
    {
        return success(knowledgeService.selectDocById(docId));
    }

    @PreAuthorize("@ss.hasPermi('mall:knowledge:upload')")
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file)
    {
        return success(knowledgeService.uploadDocument(file, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('mall:knowledge:remove')")
    @DeleteMapping("/{docId}")
    public AjaxResult remove(@PathVariable Long docId)
    {
        return toAjax(knowledgeService.deleteDoc(docId));
    }

    @PreAuthorize("@ss.hasPermi('mall:knowledge:reindex')")
    @PostMapping("/reindex/{docId}")
    public AjaxResult reindex(@PathVariable Long docId)
    {
        return success(knowledgeService.reindexDoc(docId, getUsername()));
    }
}
