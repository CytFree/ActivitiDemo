package com.cyt.activiti.core.mapper;

import com.cyt.activiti.core.pojo.SignProcessDO;
import com.cyt.activiti.core.pojo.SignProcessDOExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SignProcessDOMapper {
    int countByExample(SignProcessDOExample example);

    int deleteByExample(SignProcessDOExample example);

    int deleteByPrimaryKey(Long processId);

    int insert(SignProcessDO record);

    int insertSelective(SignProcessDO record);

    List<SignProcessDO> selectByExample(SignProcessDOExample example);

    SignProcessDO selectByPrimaryKey(Long processId);

    int updateByExampleSelective(@Param("record") SignProcessDO record, @Param("example") SignProcessDOExample example);

    int updateByExample(@Param("record") SignProcessDO record, @Param("example") SignProcessDOExample example);

    int updateByPrimaryKeySelective(SignProcessDO record);

    int updateByPrimaryKey(SignProcessDO record);

    int insertAndGetId(SignProcessDO record);
}