package com.greenhouse.mock;

import com.greenhouse.client.dto.*;
import com.greenhouse.dto.BindRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/mock-esp")
public class MockEspController {

    private final Map<Integer, Double> portValues = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> portBindings = new ConcurrentHashMap<>();

    public MockEspController() {
        portValues.put(1, 21.5);
        portValues.put(2, 0.0);
    }

    @GetMapping("/info")
    public InfoResponse getInfo() {
        InfoResponse r = new InfoResponse();
        r.setModuleId(123);
        return r;
    }

    @GetMapping("/drivers")
    public DriversResponse getDrivers() {
        EspDriver dht = new EspDriver();
        dht.setId(1);
        dht.setName("DHT22");
        dht.setType(0); // analog input

        EspDriver relay = new EspDriver();
        relay.setId(2);
        relay.setName("Relay");
        relay.setType(2); // output

        DriversResponse resp = new DriversResponse();
        resp.setDrivers(List.of(dht, relay));
        return resp;
    }

    @GetMapping("/ports")
    public PortsResponse getPorts() {
        EspPort p1 = new EspPort();
        p1.setId(1);
        p1.setType(0);
        EspPort p2 = new EspPort();
        p2.setId(2);
        p2.setType(2);
        PortsResponse resp = new PortsResponse();
        resp.setPorts(List.of(p1, p2));
        return resp;
    }

    @PutMapping(path = "/ports/{port}/bind", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void bind(@PathVariable int port, @RequestBody BindRequest request) {
        int driverId = request.driverId();
        int portType = (port == 1) ? 0 : 2;
        if ((driverId == 1 && portType != 0) || (driverId == 2 && portType != 2)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT);
        }
        if (!portValues.containsKey(port)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        portBindings.put(port, driverId);
    }

    @GetMapping("/ports/{port}/bind")
    public BindResponse getBind(@PathVariable int port) {
        if (!portValues.containsKey(port)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        Integer driverId = portBindings.get(port);
        if (driverId == null) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return new BindResponse(driverId);
    }

    @GetMapping("/ports/{port}/control")
    public ReadResponse readValue(@PathVariable int port) {
        if (!portValues.containsKey(port)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (!portBindings.containsKey(port)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (port == 2) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
        }
        ReadResponse r = new ReadResponse();
        r.setValue(portValues.get(port));
        return r;
    }

    @PostMapping(path = "/ports/{port}/control", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void writeValue(@PathVariable int port, @RequestBody LevelRequest req) {
        if (!portValues.containsKey(port)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (!portBindings.containsKey(port)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (port == 1) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
        }
        int level = req.getLevel();
        if (level < 0 || level > 255) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        portValues.put(port, (double) level);
    }

    @GetMapping("/test/temperature")
    public ReadResponse getTemperature() {
        ReadResponse r = new ReadResponse();
        r.setValue(portValues.getOrDefault(1, 0.0));
        return r;
    }

    @PostMapping(path = "/test/temperature", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void setTemperature(@RequestBody ReadResponse body) {
        portValues.put(1, body.getValue());
    }
    
    @GetMapping("/test/output")
    public ReadResponse getOutputLevel() {
        ReadResponse r = new ReadResponse();
        r.setValue(portValues.getOrDefault(2, 0.0));
        return r;
    }

    @PostMapping("/test/reset")
    @ResponseStatus(HttpStatus.OK)
    public void reset() {
        portValues.clear();
        portBindings.clear();
        portValues.put(1, 21.5);
        portValues.put(2, 0.0);
    }

    @GetMapping("/test/state")
    public Map<String, Object> getState() {
        Map<String, Object> state = new HashMap<>();
        state.put("portValues", new HashMap<>(portValues));
        state.put("portBindings", new HashMap<>(portBindings));
        return state;
    }
}
