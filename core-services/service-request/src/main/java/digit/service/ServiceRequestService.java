package digit.service;

import digit.kafka.Producer;
import digit.repository.ServiceRepository;
import digit.validators.Validator;
import digit.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceRequestService {

    @Autowired
    private Validator validator;

    @Autowired
    private ServiceRequestEnrichmentService enrichmentService;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private Producer producer;

    public ServiceDefinition createServiceDefinition(ServiceDefinitionRequest serviceDefinitionRequest) {

        ServiceDefinition serviceDefinition = serviceDefinitionRequest.getServiceDefinition();

        // Validate incoming service definition request
        validator.validateServiceDefinitionRequest(serviceDefinitionRequest);

        // Enrich incoming service definition request
        enrichmentService.enrichServiceDefinitionRequest(serviceDefinitionRequest);

        // Producer statement to emit service definition to kafka for persisting
        producer.push("save-service-definition", serviceDefinitionRequest);

        return serviceDefinition;
    }

    public List<ServiceDefinition> searchServiceDefinition(ServiceDefinitionSearchRequest serviceDefinitionSearchRequest){

        ServiceDefinitionCriteria criteria = serviceDefinitionSearchRequest.getServiceDefinitionCriteria();

        List<ServiceDefinition> listOfServiceDefinitions = serviceRepository.getServiceDefinitions(serviceDefinitionSearchRequest);

        if(CollectionUtils.isEmpty(listOfServiceDefinitions))
            return new ArrayList<>();

        return listOfServiceDefinitions;
    }

    public ServiceDefinition updateServiceDefinition(ServiceDefinitionRequest serviceDefinitionRequest) {

        // Validate update request - validate existence, validate fields being updated
        validator.validateUpdateRequest(serviceDefinitionRequest);

        /*
        documentValidator.validateCategoryFromMdms(documentRequest);
        DocumentEntity existingEntity = documentValidator.validateDocumentExistence(documentRequest.getDocumentEntity());
        DocumentEntity documentEntity = documentRequest.getDocumentEntity();
        documentEntity.setAuditDetails(existingEntity.getAuditDetails());
        documentEntity.getAuditDetails().setLastModifiedBy(documentRequest.getRequestInfo().getUserInfo().getUuid());
        documentEntity.getAuditDetails().setLastModifiedTime(System.currentTimeMillis());
        documentEntity.setPostedBy(documentRequest.getRequestInfo().getUserInfo().getName());
        //log.info(documentEntity.toString());

         */
        producer.push("update-service-definition", serviceDefinitionRequest);

        return serviceDefinitionRequest.getServiceDefinition();
    }

    public digit.web.models.Service createService(ServiceRequest serviceRequest) {

        digit.web.models.Service service = serviceRequest.getService();

        // Validate incoming service definition request
        validator.validateServiceRequest(service);

        // Enrich incoming service definition request
        enrichmentService.enrichServiceRequest(service);

        // Producer statement to emit service definition to kafka for persisting
        producer.push("save-service", serviceRequest);

        return service;
    }

    public List<digit.web.models.Service> searchService(RequestInfo requestInfo, ServiceCriteria criteria){

        List<digit.web.models.Service> listOfServices = serviceRepository.getService(criteria);

        if(CollectionUtils.isEmpty(listOfServices))
            return new ArrayList<>();

        return listOfServices;
    }

    public digit.web.models.Service updateService(ServiceRequest serviceRequest) {

        // Validate update request - validate existence, validate fields being updated
        validator.validateServiceUpdateRequest(serviceRequest);

        /*
        documentValidator.validateCategoryFromMdms(documentRequest);
        DocumentEntity existingEntity = documentValidator.validateDocumentExistence(documentRequest.getDocumentEntity());
        DocumentEntity documentEntity = documentRequest.getDocumentEntity();
        documentEntity.setAuditDetails(existingEntity.getAuditDetails());
        documentEntity.getAuditDetails().setLastModifiedBy(documentRequest.getRequestInfo().getUserInfo().getUuid());
        documentEntity.getAuditDetails().setLastModifiedTime(System.currentTimeMillis());
        documentEntity.setPostedBy(documentRequest.getRequestInfo().getUserInfo().getName());
        //log.info(documentEntity.toString());

         */

        producer.push("update-service", serviceRequest);

        return serviceRequest.getService();
    }

}

