package codedriver.module.report.widget;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import freemarker.template.SimpleHash;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class DrawPagination implements TemplateMethodModelEx {
    Logger logger = LoggerFactory.getLogger(DrawPagination.class);
    private Boolean needPage;

    public DrawPagination(Boolean _needPage) {
        this.needPage = _needPage;
    }

    @SuppressWarnings({"rawtypes", "unchecked", "unused"})
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        boolean canReturn = false;
        String str = "";
        //导出不需要分页
        if (needPage) {
            int currentPage = 1;
            int pageCount = 0;
            int rowNum = 0;
            int pageSize = 10;
            String selectId = "";
            Map dataMap = null;
            if (arguments.size() == 2 && arguments.get(0) != null) {
                try {
                    SimpleHash sh = arguments.get(0) instanceof SimpleHash ? (SimpleHash)arguments.get(0) : null;
                    dataMap = sh.toMap();
                    JSONObject pageObj = new JSONObject();
                    Set<Entry<String, Object>> entrySet = dataMap.entrySet();
                    for(Entry<String, Object> entry : entrySet ) {
                        selectId = entry.getKey();
                        pageObj = JSONObject.parseObject(entry.getValue().toString());
                    }
                    if (pageObj.getIntValue("currentPage") > 0) {
                        currentPage = pageObj.getIntValue("currentPage");
                    }
                    if (pageObj.getIntValue("pageCount") > 0) {
                        pageCount = pageObj.getIntValue("pageCount");
                    }
                    
                    if (pageObj.getIntValue("rowNum") > 0) {
                        rowNum = pageObj.getIntValue("rowNum");
                    }
                    
                    if (pageObj.getIntValue("pageSize") > 0) {
                        pageSize = pageObj.getIntValue("pageSize");
                    }
                    
                    if(MapUtils.isNotEmpty(pageObj)) {
                        canReturn = true;
                    }
                } catch (Exception ex) {
                    // 非json格式
                    logger.error(ex.getMessage(), ex);
                }
            } 

            if (canReturn) {
                str = "<div class=\"tstable-page\">" + 
                        "<ul class=\"ivu-page mini\">" + 
                            "<span class=\"ivu-page-total\">共 "+rowNum+" 条</span>" ;
                //上一页
                if (currentPage <= 1) {
                    str +="<li title=\"上一页\" class=\"ivu-page-prev ivu-page-disabled\"><a><i class=\"ivu-icon ivu-icon-ios-arrow-back\"></i></a></li>" ;
                } else {
                    str +="<li title=\"上一页\" class=\"ivu-page-prev\"><a><i class=\"ivu-icon ivu-icon-ios-arrow-back\" onclick=\"window.REPORT_CHANGE_PAGE('"+selectId+"', "+(currentPage-1)+")\" ></i></a></li>" ;
                }
                //第一页
                if(currentPage >= 1) {
                    if(currentPage ==1) {
                        str += "<li title=\"1\" class=\"ivu-page-item ivu-page-item-active\"><a>1</a></li>";
                    }else {
                        str += "<li title=\"1\" class=\"ivu-page-item\" onclick=\"window.REPORT_CHANGE_PAGE('"+selectId+"', 1)\" ><a>1</a></li>";
                    }
                }
                //向前五页
                if (currentPage >5) {
                    str += "<li title=\"向前 5 页\" class=\"ivu-page-item-jump-prev\"><a><i class=\"ivu-icon ivu-icon-ios-arrow-back\" onclick=\"window.REPORT_CHANGE_PAGE('"+selectId+"', "+(currentPage-5)+")\" ></i></a></li>";
                }
                
                
                int startPage = 2;
                //prePages
                if(currentPage > 5) {
                    startPage = currentPage-2; 
                }
                for (int k = startPage; k <= currentPage; k++) {
                    if (currentPage == k) {
                        str += "<li title=\""+k+"\" class=\"ivu-page-item ivu-page-item-active\"><a>"+k+"</a></li>";
                    } else {
                        str += "<li title=\""+k+"\" class=\"ivu-page-item\" onclick=\"window.REPORT_CHANGE_PAGE('"+selectId+"', "+k+")\" ><a>"+k+"</a></li>";
                    }
                }
                //nextPages
                if(pageCount - 4 > currentPage) {
                    for (int k = currentPage+1; k <= currentPage+2; k++) {
                        str += "<li title=\""+k+"\" class=\"ivu-page-item \" onclick=\"window.REPORT_CHANGE_PAGE('"+selectId+"', "+k+")\" ><a>"+k+"</a></li>";
                    }
                    str += "<li title=\"向后 5 页\" class=\"ivu-page-item-jump-next\"><a><i class=\"ivu-icon ivu-icon-ios-arrow-forward\" onclick=\"window.REPORT_CHANGE_PAGE('"+selectId+"', "+(currentPage+5)+")\" ></i></a></li>";
                    str += "<li title=\""+pageCount+"\" class=\"ivu-page-item\" onclick=\"window.REPORT_CHANGE_PAGE('"+selectId+"', "+pageCount+")\"><a>"+pageCount+"</a></li> ";
                }else {
                    for (int k = currentPage+1; k <= pageCount; k++) {
                        if (currentPage == k) {
                            str += "<li title=\""+k+"\" class=\"ivu-page-item ivu-page-item-active\"><a>"+k+"</a></li>";
                        } else {
                            str += "<li title=\""+k+"\" class=\"ivu-page-item \" onclick=\"window.REPORT_CHANGE_PAGE('"+selectId+"', "+k+")\"><a>"+k+"</a></li>";
                        }
                    }
                }

                if (currentPage >= pageCount) {
                    str +="<li title=\"下一页\" class=\"ivu-page-next ivu-page-disabled\"><a><i class=\"ivu-icon ivu-icon-ios-arrow-forward\"></i></a></li> ";
                }else {
                    str +="<li title=\"下一页\" class=\"ivu-page-next\"><a><i class=\"ivu-icon ivu-icon-ios-arrow-forward\" onclick=\"window.REPORT_CHANGE_PAGE('"+selectId+"', "+(currentPage+1)+")\" ></i></a></li> ";
                } 
                str += "<div class=\"ivu-page-options\">" + 
                    "             <div class=\"ivu-page-options-sizer\">" + 
                    "                 <div class=\"ivu-select ivu-select-single ivu-select-small\">" + 
                    "                     <div tabindex=\"0\" class=\"ivu-select-selection\"><input type=\"hidden\" value=\"20\"> " + 
                    "                         <div class=\"\">" + 
                    "                             <select id=\""+selectId+"PageSelect\" onchange=\"window.REPORT_CHANGE_PAGESIZE('"+selectId+"', this.options[this.selectedIndex].value)\">";
                for(int i =1;i<5;i++) {
                    int tmpPageSize = i*10;
                    String defaultSeclect = StringUtils.EMPTY;
                    if(pageSize == tmpPageSize) {
                        defaultSeclect = "selected";
                    }
                    str += "<option value='"+tmpPageSize+"' "+defaultSeclect+">"+tmpPageSize+" 条/页</option>";
                }
                   
                str += "                          </select> " + 
                    "                             <i class=\"ivu-icon ivu-icon-ios-arrow-down ivu-select-arrow\"></i>" + 
                    "                         </div>" + 
                    "                     </div> " + 
                    "                 </div>" + 
                    "             </div> " + 
                    "         </div></ul>" + 
                    " </div>";
                
                return str;
            }
        }
        
        return StringUtils.EMPTY;
    }

}
