package io.shulie.amdb.service.impl;

import java.util.List;

import javax.annotation.Resource;

import io.shulie.amdb.entity.ReportActivityInterfaceDO;
import io.shulie.amdb.mapper.ReportActivityInterfaceMapper;
import io.shulie.amdb.request.query.ReportInterfaceQueryRequest;
import io.shulie.amdb.service.ReportActivityInterfaceService;
import org.springframework.stereotype.Service;

@Service
public class ReportActivityInterfaceServiceImpl implements ReportActivityInterfaceService {

    @Resource
    private ReportActivityInterfaceMapper mapper;

    @Override
    public List<ReportActivityInterfaceDO> queryReportActivityInterface(ReportInterfaceQueryRequest request) {
        return mapper.selectByRequest(request);
    }
}
