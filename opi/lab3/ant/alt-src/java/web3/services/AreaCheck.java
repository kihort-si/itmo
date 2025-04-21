package web3.services;


import web3.AltResult;

import java.io.Serializable;

public interface AreaCheck extends Serializable {
    AltResult checkArea(Input input, boolean fromGraph);
}
