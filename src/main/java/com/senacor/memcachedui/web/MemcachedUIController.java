package com.senacor.memcachedui.web;

import com.senacor.memcachedui.model.Key;
import com.senacor.memcachedui.model.MemorySize;
import com.senacor.memcachedui.model.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class MemcachedUIController {

    private final MemcachedUIService service;

    @Autowired
    public MemcachedUIController(MemcachedUIService service) {
        this.service = service;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/")
    public String root(Model model) {
        service.updateLocalCache(false);
        List<Namespace> namespaces = new ArrayList<>(service.getNamespaces());
        namespaces.sort(Comparator.comparing(Namespace::getName));
        var keysNumber = namespaces.stream()
                .mapToLong(namespace -> namespace.getKeys().size())
                .sum();
        var memSize = new MemorySize(namespaces.stream()
                .map(Namespace::getMemSize)
                .map(MemorySize::getBytes)
                .reduce(0L, Long::sum));
        model.addAttribute("keysNumber", keysNumber);
        model.addAttribute("memSize", memSize.asString());
        model.addAttribute("namespaces", namespaces);
        return "index";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/namespace")
    public String namespace(
            @RequestParam(value = "id") String id,
            @RequestParam(required = false, value = "term") String term,
            @RequestParam(required = false, value = "page", defaultValue = "1") Integer page,
            Model model) {
        List<Key> keys;
        Map<Integer, Boolean> pagination = new HashMap<>();
        if (term != null) {
            keys = service.searchForKeysInNamespace(id, term);
            model.addAttribute("searchTerm", term);
        } else {

            keys = service.getKeysForNamespace(id, Objects.requireNonNullElse(page, 1));
            pagination = service.getPagination(id, page);
        }
        model.addAttribute("keys", keys);
        model.addAttribute("namespace", id);
        model.addAttribute("pagination", pagination);
        return "namespace";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/stats")
    public String stats(Model model) {
        Map<String, String> stats = service.getStats();
        model.addAttribute("stats", stats);
        return "stats";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/refresh")
    public ResponseEntity<?> refresh() {
        service.updateLocalCache(true);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/value")
    @ResponseBody
    public ResponseEntity<String> getValue(
            @RequestParam(value = "namespace") String namespace,
            @RequestParam(value = "key") String key) {
        return ResponseEntity.status(HttpStatus.OK).body(service.getValue(namespace, key));
    }

    /**
     * Delete a namespace.
     * @param namespace Namespace to delete
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/namespace/{namespace}")
    @ResponseBody
    public ResponseEntity<?> deleteNamespace(@PathVariable("namespace") String namespace) {
        service.deleteNamespace(namespace);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Delete the key-value pair from specified namespace.
     * @param namespace Namespace
     * @param key Key to delete
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/key/{namespace}/{key}")
    @ResponseBody
    public ResponseEntity<?> deleteKey(@PathVariable("namespace") String namespace, @PathVariable("key") String key) {
        service.deleteKey(namespace, key);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Delete all keys from cache.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/keys")
    @ResponseBody
    public ResponseEntity<?> deleteKeys() {
        service.deleteKeys();
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
