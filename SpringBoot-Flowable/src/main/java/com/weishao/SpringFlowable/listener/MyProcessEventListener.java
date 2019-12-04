package com.weishao.SpringFlowable.listener;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.common.engine.impl.event.FlowableEngineEventImpl;
import org.flowable.engine.ManagementService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngines;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 实例的事件监听处理器
 * 
 * @author Tang
 *
 */
public class MyProcessEventListener implements FlowableEventListener  {

	private static final Logger logger = LoggerFactory.getLogger(MyProcessEventListener.class);

	@Override
	public void onEvent(FlowableEvent event) {
		FlowableEngineEventImpl  engineEvent=(FlowableEngineEventImpl)event;
		ProcessEngine processEngine=ProcessEngines.getDefaultProcessEngine();
		String processInstanceId = engineEvent.getProcessInstanceId();
		
		if(event.getType() == FlowableEngineEventType.JOB_EXECUTION_SUCCESS) {
			if (null != processInstanceId) {
				logger.info("### A job well done,processInstanceId="+ processInstanceId);
			}
		} else if (event.getType() == FlowableEngineEventType.JOB_EXECUTION_FAILURE) {
			logger.error("### A job has failed,processInstanceId="+ processInstanceId);
				
			try {
				RepositoryService repositoryService = processEngine.getRepositoryService();
				RuntimeService runtimeService = processEngine.getRuntimeService();
				ManagementService managementService=processEngine.getManagementService();
				ProcessInstance pi = runtimeService.createProcessInstanceQuery()
						.processInstanceId(processInstanceId).singleResult();
				if(null!=pi) {
					ProcessDefinition pdef=repositoryService.createProcessDefinitionQuery()
							.processDefinitionId(pi.getProcessDefinitionId()).singleResult();
					int retry=managementService.createTimerJobQuery().processInstanceId(processInstanceId).list().size();
					if (null != pdef && !pdef.isSuspended() && retry == 0) {
						repositoryService.suspendProcessDefinitionById(pi.getProcessDefinitionId());
						logger.info("### The job is 【suspend】,processInstanceId=" + processInstanceId);
					} else {
						logger.info("### The job is 【retring】,retry={}, processInstanceId={}", getJobRetryCount(processInstanceId),processInstanceId);
					}
				}
			} catch (Exception e) {
				logger.error("The failed job's processInstanceId="+ processInstanceId);
				logger.error("Error when suspend process definition:", e);
			}
			
		}else {
			//logger.info("Event received: " + event.getType());
		}
	}

	@Override
	public boolean isFailOnException() {
		logger.info("### MyTaskEventListener->isFailOnException()");
		return false;
	}

	@Override
	public boolean isFireOnTransactionLifecycleEvent() {
		return false;
	}

	@Override
	public String getOnTransaction() {
		return null;
	}

	private int getJobRetryCount(String processInstanceId) {
		ApplicationContext context = com.weishao.SpringFlowable.config.SpringContextManager.getApplicationContext();
		JdbcTemplate jdbcTemplate = (JdbcTemplate) context.getBean("jdbcTemplate");
		String sql = String.format("SELECT RETRIES_ FROM `ACT_RU_TIMER_JOB` where PROCESS_INSTANCE_ID_='%s' ",processInstanceId);
		try {
			return jdbcTemplate.queryForObject(sql, Integer.class);
		} catch (EmptyResultDataAccessException e) {
			return 0;
		}
	}
}
