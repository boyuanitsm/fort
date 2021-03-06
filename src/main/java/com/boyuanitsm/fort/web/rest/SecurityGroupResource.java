/*
 * Copyright 2016-2017 Shanghai Boyuan IT Services Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.boyuanitsm.fort.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.boyuanitsm.fort.domain.SecurityGroup;
import com.boyuanitsm.fort.service.SecurityGroupService;
import com.boyuanitsm.fort.web.rest.util.HeaderUtil;
import com.boyuanitsm.fort.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing SecurityGroup.
 */
@RestController
@RequestMapping("/api")
public class SecurityGroupResource {

    private final Logger log = LoggerFactory.getLogger(SecurityGroupResource.class);

    @Inject
    private SecurityGroupService securityGroupService;

    /**
     * POST  /security-groups : Create a new securityGroup.
     *
     * @param securityGroup the securityGroup to create
     * @return the ResponseEntity with status 201 (Created) and with body the new securityGroup, or with status 400 (Bad Request) if the securityGroup has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/security-groups",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<SecurityGroup> createSecurityGroup(@Valid @RequestBody SecurityGroup securityGroup) throws URISyntaxException {
        log.debug("REST request to save SecurityGroup : {}", securityGroup);
        if (securityGroup.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("securityGroup", "idexists", "A new securityGroup cannot already have an ID")).body(null);
        }
        if (securityGroupService.findByAppAndName(securityGroup.getApp(), securityGroup.getName()) != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("securityGroup", "nameexists", "A new securityGroup cannot already have an name")).body(null);
        }
        SecurityGroup result = securityGroupService.save(securityGroup);
        return ResponseEntity.created(new URI("/api/security-groups/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("securityGroup", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /security-groups : Updates an existing securityGroup.
     *
     * @param securityGroup the securityGroup to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated securityGroup,
     * or with status 400 (Bad Request) if the securityGroup is not valid,
     * or with status 500 (Internal Server Error) if the securityGroup couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/security-groups",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<SecurityGroup> updateSecurityGroup(@Valid @RequestBody SecurityGroup securityGroup) throws URISyntaxException {
        log.debug("REST request to update SecurityGroup : {}", securityGroup);
        if (securityGroup.getId() == null) {
            return createSecurityGroup(securityGroup);
        }
        SecurityGroup result = securityGroupService.save(securityGroup);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("securityGroup", securityGroup.getId().toString()))
            .body(result);
    }

    /**
     * GET  /security-groups : get all the securityGroups.
     *
     * @param pageable the pagination information
     * @param appId the id of the app
     * @return the ResponseEntity with status 200 (OK) and the list of securityGroups in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/security-groups",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<SecurityGroup>> getAllSecurityGroups(Pageable pageable, Long appId)
        throws URISyntaxException {
        log.debug("REST request to get a page of SecurityGroups");
        Page<SecurityGroup> page = securityGroupService.findAll(pageable, appId);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/security-groups");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /security-groups/:id : get the "id" securityGroup.
     *
     * @param id the id of the securityGroup to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the securityGroup, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/security-groups/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<SecurityGroup> getSecurityGroup(@PathVariable Long id) {
        log.debug("REST request to get SecurityGroup : {}", id);
        SecurityGroup securityGroup = securityGroupService.findOne(id);
        return Optional.ofNullable(securityGroup)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /security-groups/:id : delete the "id" securityGroup.
     *
     * @param id the id of the securityGroup to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/security-groups/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteSecurityGroup(@PathVariable Long id) {
        log.debug("REST request to delete SecurityGroup : {}", id);
        securityGroupService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("securityGroup", id.toString())).build();
    }

    /**
     * SEARCH  /_search/security-groups?query=:query : search for the securityGroup corresponding
     * to the query.
     *
     * @param query the query of the securityGroup search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/security-groups",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<SecurityGroup>> searchSecurityGroups(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of SecurityGroups for query {}", query);
        Page<SecurityGroup> page = securityGroupService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/security-groups");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

}
