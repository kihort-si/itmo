package web3.services;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import web3.Result;
import web3.database.ResultsRepository;

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
    ResultsRepository resultsRepository;

    public void process(boolean fromGraph) {
        Result result = areaChecker.checkArea(input, fromGraph);
        resultsRepository.addResult(result);
    }
}
