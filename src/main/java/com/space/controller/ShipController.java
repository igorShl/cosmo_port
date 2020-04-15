package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/rest")
public class ShipController {

    private ShipService shipService;

    @Autowired
    public void setShipService(ShipService shipService) {
        this.shipService = shipService;
    }



    @RequestMapping(value = "/ships", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Ship addShip(@RequestBody Ship ship) {
        return shipService.addShip(ship);
    }



    @RequestMapping(value = "/ships/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteShip(@PathVariable (value = "id") String id) {
        shipService.deleteShip(id);
    }



    @RequestMapping(value = "/ships/{id}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Ship updateShip(@RequestBody Ship ship,
                           @PathVariable (value = "id") String id) {
        return shipService.updateShip(ship, id);
    }



    @RequestMapping(value = "/ships/{id}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Ship getShip(@PathVariable (value = "id") String id) {
        return shipService.getShip(id);
    }



    @RequestMapping(value = "/ships", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<Ship> getShipsCount(@RequestParam (value = "name", required = false) String name,
                                    @RequestParam (value = "planet", required = false) String planet,
                                    @RequestParam (value = "shipType", required = false) ShipType shipType,
                                    @RequestParam (value = "after", required = false) Long after,
                                    @RequestParam (value = "before", required = false) Long before,
                                    @RequestParam (value = "isUsed", required = false) Boolean isUsed,
                                    @RequestParam (value = "minSpeed", required = false) Double minSpeed,
                                    @RequestParam (value = "maxSpeed", required = false) Double maxSpeed,
                                    @RequestParam (value = "minCrewSize", required = false) Integer minCrewSize,
                                    @RequestParam (value = "maxCrewSize", required = false) Integer maxCrewSize,
                                    @RequestParam (value = "minRating", required = false) Double minRating,
                                    @RequestParam (value = "maxRating", required = false) Double maxRating,
                                    @RequestParam (value = "order", required = false, defaultValue = "ID") ShipOrder order,
                                    @RequestParam (value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                                    @RequestParam (value = "pageSize", required = false, defaultValue = "3") Integer pageSize) {
        return shipService.getShipsList(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed,
                minCrewSize, maxCrewSize, minRating, maxRating, order, pageNumber, pageSize);
    }



    @RequestMapping(value = "/ships/count", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Integer getShipsCount(@RequestParam (value = "name", required = false) String name,
                                 @RequestParam (value = "planet", required = false) String planet,
                                 @RequestParam (value = "shipType", required = false) ShipType shipType,
                                 @RequestParam (value = "after", required = false) Long after,
                                 @RequestParam (value = "before", required = false) Long before,
                                 @RequestParam (value = "isUsed", required = false) Boolean isUsed,
                                 @RequestParam (value = "minSpeed", required = false) Double minSpeed,
                                 @RequestParam (value = "maxSpeed", required = false) Double maxSpeed,
                                 @RequestParam (value = "minCrewSize", required = false) Integer minCrewSize,
                                 @RequestParam (value = "maxCrewSize", required = false) Integer maxCrewSize,
                                 @RequestParam (value = "minRating", required = false) Double minRating,
                                 @RequestParam (value = "maxRating", required = false) Double maxRating) {
        return shipService.getShipsCount(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed,
                minCrewSize, maxCrewSize, minRating, maxRating);
    }
}
