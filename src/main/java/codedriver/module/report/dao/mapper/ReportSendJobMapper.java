package codedriver.module.report.dao.mapper;

import codedriver.module.report.dto.ReportReceiverVo;
import codedriver.module.report.dto.ReportSendJobRelationVo;
import codedriver.module.report.dto.ReportSendJobVo;

import java.util.List;

public interface ReportSendJobMapper {

    public ReportSendJobVo getJobBaseInfoById(Long id);

    public int checkJobExists(Long id);

    public int checkNameIsRepeat(ReportSendJobVo job);

    public ReportSendJobVo getJobById(Long id);

    public int searchJobCount(ReportSendJobVo job);

    public List<ReportSendJobVo> searchJob(ReportSendJobVo job);

    public int updateJob(ReportSendJobVo job);

    public int updateJobStatus(ReportSendJobVo job);

    public int insertJob(ReportSendJobVo job);

    public int batchInsertReportReceiver(List<ReportReceiverVo> list);

    public int batchInsertReportRelation(List<ReportSendJobRelationVo> list);

    public int deleteReportReceiver(Long id);

    public int deleteReportRelation(Long id);

    public int deleteJobById(Long id);

}
