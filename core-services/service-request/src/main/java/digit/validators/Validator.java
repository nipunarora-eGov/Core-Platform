package digit.validators;

import digit.repository.ServiceRepository;
import digit.web.models.Service;
import digit.web.models.ServiceDefinitionRequest;
import digit.web.models.ServiceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Validator {

    @Autowired
    private ServiceRepository serviceRepository;

    public void validateServiceDefinitionRequest(ServiceDefinitionRequest serviceDefinitionRequest){

    }

    public void validateUpdateRequest(ServiceDefinitionRequest serviceDefinitionRequest) {
    }

    public void validateServiceRequest(Service service) {
    }

    public void validateServiceUpdateRequest(ServiceRequest serviceRequest) {
    }
}
