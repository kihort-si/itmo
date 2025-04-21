package web3.services;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import web3.AltResult;
import web3.database.AltResultsRepository;

import java.io.Serializable;

@SessionScoped
@Named("requestBean")
public class Request implements Serializable {
    @Inject
    Input input;
    @Inject
    @AreaCheckQualifier
    AreaChecker areaChecker;
    @Inject
    AltResultsRepository resultsRepository;

    public void process(boolean fromGraph) {
        AltResult result = areaChecker.checkArea(input, fromGraph);
        resultsRepository.addResult(result);
    }
}
