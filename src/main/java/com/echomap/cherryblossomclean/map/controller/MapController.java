package com.echomap.cherryblossomclean.map.controller;

import com.echomap.cherryblossomclean.map.entity.GarbageCan;
import com.echomap.cherryblossomclean.map.service.GarbageCanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/maps")
public class MapController {

    private final GarbageCanService garbageCanService;

    @GetMapping("/garbage-can")
    public ResponseEntity<?> getGarbageCanLocation(String value) {
        List<GarbageCan> garbageCanLocations = garbageCanService.getGarbageCanLocations(value);
        log.warn("value : {}", value);
        return ResponseEntity.ok().body(garbageCanLocations);
    }
}
