package web3.services;


import web3.Result;

import java.io.Serializable;

public interface AreaCheck extends Serializable {
    Result checkArea(Input input, boolean fromGraph);
}
