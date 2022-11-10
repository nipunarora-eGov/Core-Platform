package org.egov.swservice.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.swservice.config.SWConfiguration;
import org.egov.swservice.repository.rowmapper.OpenSewerageRowMapper;
import org.egov.swservice.web.models.Connection;
import org.egov.swservice.web.models.SearchCriteria;
import org.egov.swservice.web.models.SewerageConnection;
import org.egov.swservice.web.models.SewerageConnectionRequest;
import org.egov.swservice.producer.SewarageConnectionProducer;
import org.egov.swservice.repository.builder.SWQueryBuilder;
import org.egov.swservice.repository.rowmapper.SewerageRowMapper;
import org.egov.swservice.util.SWConstants;
import org.omg.PortableInterceptor.INACTIVE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SewerageDaoImpl implements SewerageDao {

	@Autowired
	private SewarageConnectionProducer sewarageConnectionProducer;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private SWQueryBuilder swQueryBuilder;

	@Autowired
	private SewerageRowMapper sewarageRowMapper;

	@Autowired
	private OpenSewerageRowMapper openSewerageRowMapper;

	@Autowired
	private SWConfiguration swConfiguration;

	@Value("${egov.sewarageservice.createconnection.topic}")
	private String createSewarageConnection;

	@Value("${egov.sewarageservice.updateconnection.topic}")
	private String updateSewarageConnection;

	@Override
	public void saveSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest) {
		sewarageConnectionProducer.push(createSewarageConnection, sewerageConnectionRequest);
	}

	@Override
	public List<SewerageConnection> getSewerageConnectionList(SearchCriteria criteria, RequestInfo requestInfo) {
		List<Object> preparedStatement = new ArrayList<>();
		String query = swQueryBuilder.getSearchQueryString(criteria, preparedStatement, requestInfo);
		if (query == null)
			return Collections.emptyList();
		Boolean isOpenSearch = isSearchOpen(requestInfo.getUserInfo());
		List<SewerageConnection> sewerageConnectionList = new ArrayList<>();
		if(isOpenSearch)
			sewerageConnectionList = jdbcTemplate.query(query, preparedStatement.toArray(),
					openSewerageRowMapper);
		else
			sewerageConnectionList = jdbcTemplate.query(query, preparedStatement.toArray(),
					sewarageRowMapper);

		if (sewerageConnectionList == null) {
			return Collections.emptyList();
		}
		return sewerageConnectionList;
	}

	public Integer getSewerageConnectionsCount(SearchCriteria criteria, RequestInfo requestInfo) {
		List<Object> preparedStatement = new ArrayList<>();
		String query = swQueryBuilder.getSearchCountQueryString(criteria, preparedStatement, requestInfo);
		if (query == null)
			return 0;

		Integer count = jdbcTemplate.queryForObject(query, preparedStatement.toArray(), Integer.class);
		return count;
	}
	
	public Boolean isSearchOpen(User userInfo) {

		return userInfo.getType().equalsIgnoreCase("SYSTEM")
				&& userInfo.getRoles().stream().map(Role::getCode).collect(Collectors.toSet()).contains("ANONYMOUS");
	}

	public void updateSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest,
			boolean isStateUpdatable) {
		String reqAction = sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getAction();
		if (isStateUpdatable) {
			if (SWConstants.EXECUTE_DISCONNECTION.equalsIgnoreCase(reqAction)) {
				sewerageConnectionRequest.getSewerageConnection().setStatus(Connection.StatusEnum.INACTIVE);
			}
			sewarageConnectionProducer.push(updateSewarageConnection, sewerageConnectionRequest);
		} else if (SWConstants.EXECUTE_DISCONNECTION.equalsIgnoreCase(sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getAction())) {
			sewerageConnectionRequest.getSewerageConnection().setStatus(Connection.StatusEnum.INACTIVE);
			sewarageConnectionProducer.push(updateSewarageConnection, sewerageConnectionRequest);
		} else {
			sewarageConnectionProducer.push(swConfiguration.getWorkFlowUpdateTopic(), sewerageConnectionRequest);
		}
	}

	/**
	 * push object for edit notification
	 * 
	 * @param sewerageConnectionRequest - Sewerage Connection Request Object
	 */
	public void pushForEditNotification(SewerageConnectionRequest sewerageConnectionRequest) {
		if (!SWConstants.EDIT_NOTIFICATION_STATE
				.contains(sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getAction())) {
			sewarageConnectionProducer.push(swConfiguration.getEditNotificationTopic(), sewerageConnectionRequest);
		}
	}

	/**
	 * Enrich file store Id's
	 * 
	 * @param sewerageConnectionRequest - Sewerage Connection Request Object
	 */
	public void enrichFileStoreIds(SewerageConnectionRequest sewerageConnectionRequest) {
		sewarageConnectionProducer.push(swConfiguration.getFileStoreIdsTopic(), sewerageConnectionRequest);
	}

	/**
	 * Save file store Id's
	 * 
	 * @param sewerageConnectionRequest - Sewerage Connection Request Object
	 */
	public void saveFileStoreIds(SewerageConnectionRequest sewerageConnectionRequest) {
		sewarageConnectionProducer.push(swConfiguration.getSaveFileStoreIdsTopic(), sewerageConnectionRequest);
	}
	
	@Override
	public List<SewerageConnection> getSewerageConnectionPlainSearchList(SearchCriteria criteria, RequestInfo requestInfo) {
		List<Object> preparedStatement = new ArrayList<>();
		String query = swQueryBuilder.getSearchQueryStringForPlainSearch(criteria, preparedStatement, requestInfo);
		if (query == null)
			return Collections.emptyList();
		System.out.println("\nFinal Query ::" + query);

		Boolean isOpenSearch = isSearchOpen(requestInfo.getUserInfo());
		List<SewerageConnection> sewerageConnectionList = new ArrayList<>();
		if(isOpenSearch)
			sewerageConnectionList = jdbcTemplate.query(query, preparedStatement.toArray(),
					openSewerageRowMapper);
		else
			sewerageConnectionList = jdbcTemplate.query(query, preparedStatement.toArray(),
					sewarageRowMapper);

		if (sewerageConnectionList == null) {
			return Collections.emptyList();
		}
		return sewerageConnectionList;
	}

	/* Method to push the encrypted data to the 'update' topic  */
	@Override
	public void updateOldSewerageConnections(SewerageConnectionRequest sewerageConnectionRequest) {
		sewarageConnectionProducer.push(updateSewarageConnection, sewerageConnectionRequest);

	}

	/* Method to find the total count of applications present in dB */
	@Override
	public Integer getTotalApplications(SearchCriteria criteria) {

		String query = swQueryBuilder.getTotalApplicationsCountQueryString(criteria);
		if (query == null)
			return 0;
		Integer count = jdbcTemplate.queryForObject(query, Integer.class);
		return count;
	}
}